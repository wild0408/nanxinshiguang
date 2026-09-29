package com.wild0408.nanxinshiguang.ui.schoolselection.web

import com.wild0408.nanxinshiguang.data.model.CourseImportExport
import com.wild0408.nanxinshiguang.data.parser.parseGradeRecords
import com.wild0408.nanxinshiguang.data.repository.GradeImportStore
import com.wild0408.nanxinshiguang.data.repository.GradeRepository
import com.wild0408.nanxinshiguang.data.repository.CourseConversionRepository
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * JS Bridge 消息处理器，负责路由通信请求并与 Native 业务及 UI 进行交互。
 */
class WebBridgeHandler(
    private val coroutineScope: CoroutineScope,
    private val uiEventChannel: SendChannel<WebUiEvent>,
    private val courseConversionRepository: CourseConversionRepository,
    private val gradeRepository: GradeRepository,
    private val onTaskCompleted: () -> Unit,
    private val evaluateJs: (script: String, callback: ((String?) -> Unit)?) -> Unit
) {
    private val json = CourseImportExport.json
    private var importTableId: String? = null

    /**
     * 设置当前导入的目标课表 ID。
     */
    fun setImportTableId(tableId: String?) {
        this.importTableId = tableId
    }

    /**
     * 接收并解析来自 JS 端的消息 JSON 字符串。
     */
    fun onMessageReceived(jsonString: String) {
        try {
            val message = bridgeJson.decodeFromString<JsBridgeMessage>(jsonString)
            val callbackId = message.callbackId

            when (message.action) {
                "showToast" -> parsePayload<ShowToastPayload>(message.payload)?.let {
                    showToast(it.message)
                }

                "showAlert" -> parsePayload<ShowAlertPayload>(message.payload)?.let {
                    showAlert(it.titleText, it.contentText, it.confirmText, callbackId)
                }

                "showPrompt" -> parsePayload<ShowPromptPayload>(message.payload)?.let {
                    showPrompt(it.titleText, it.tipText, it.defaultText, it.validatorJsFunction, callbackId)
                }

                "showSingleSelection" -> parsePayload<ShowSingleSelectionPayload>(message.payload)?.let {
                    showSingleSelection(it.titleText, it.itemsJsonString, it.defaultSelectedIndex, callbackId)
                }

                "saveImportedCourses" -> parsePayload<SaveCoursesPayload>(message.payload)?.let {
                    saveImportedCourses(it.coursesJsonString, callbackId)
                }

                "saveImportedGrades" -> parsePayload<SaveGradesPayload>(message.payload)?.let {
                    saveImportedGrades(it.gradesJsonString, callbackId)
                }

                "saveCourseConfig" -> parsePayload<SaveConfigPayload>(message.payload)?.let {
                    saveCourseConfig(it.configJsonString, callbackId)
                }

                "savePresetTimeSlots" -> parsePayload<SaveTimeSlotsPayload>(message.payload)?.let {
                    savePresetTimeSlots(it.timeSlotsJsonString, callbackId)
                }

                "notifyTaskCompletion" -> notifyTaskCompletion()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 显示短提示 Toast。
     */
    fun showToast(message: String) {
        ToastManager.show(message)
    }

    /**
     * 显示 Alert 确认弹窗。
     */
    fun showAlert(
        titleText: String,
        contentText: String,
        confirmText: String? = null,
        callbackId: String? = null
    ) {
        val resolvedConfirmText = confirmText ?: "确定"
        val data = AlertDialogData(titleText, contentText, resolvedConfirmText)

        val promiseCallback: (Boolean) -> Unit = { confirmed ->
            if (callbackId != null) {
                resolveJsPromise(callbackId, if (confirmed) "true" else "false")
            }
        }

        val sendResult = uiEventChannel.trySend(WebUiEvent.ShowAlert(data, promiseCallback))
        if (sendResult.isFailure && callbackId != null) {
            rejectJsPromise(callbackId, "无法显示弹窗：事件队列已满")
        }
    }

    /**
     * 显示 Prompt 输入弹窗并支持 JS 校验。
     */
    fun showPrompt(
        titleText: String,
        tipText: String,
        defaultText: String = "",
        validatorJsFunction: String? = null,
        callbackId: String? = null
    ) {
        val data = PromptDialogData(
            title = titleText,
            tip = tipText,
            defaultText = defaultText,
            validatorJsFunction = validatorJsFunction
        )

        val errorFlow = MutableSharedFlow<String?>(extraBufferCapacity = 1)

        val onCancel: () -> Unit = {
            if (callbackId != null) {
                resolveJsPromise(callbackId, "null")
            }
        }

        val onRequestValidation: (String, () -> Unit) -> Unit = { input, onSuccess ->
            val encodedInput = bridgeJson.encodeToString(input)

            if (data.validatorJsFunction.isNullOrEmpty()) {
                if (callbackId != null) {
                    resolveJsPromise(callbackId, encodedInput)
                }
                onSuccess()
            } else {
                val jsScript = "${data.validatorJsFunction}($encodedInput)"
                evaluateJs(jsScript) { result ->
                    val validationResult = result?.trim('\"')
                    if (validationResult.isNullOrEmpty() || validationResult.equals("false", ignoreCase = true)) {
                        if (callbackId != null) {
                            resolveJsPromise(callbackId, encodedInput)
                        }
                        onSuccess()
                    } else {
                        coroutineScope.launch {
                            errorFlow.emit(validationResult)
                        }
                    }
                }
            }
        }

        val sendResult = uiEventChannel.trySend(
            WebUiEvent.ShowPrompt(data, onRequestValidation, errorFlow.asSharedFlow(), onCancel)
        )
        if (sendResult.isFailure && callbackId != null) {
            rejectJsPromise(callbackId, "无法显示输入框：事件队列已满")
        }
    }

    /**
     * 显示单选列表弹窗。
     */
    fun showSingleSelection(
        titleText: String,
        itemsJsonString: String,
        defaultSelectedIndex: Int = -1,
        callbackId: String? = null
    ) {
        try {
            val items = json.decodeFromString<List<String>>(itemsJsonString)
            val data = SingleSelectionDialogData(titleText, items, defaultSelectedIndex)

            val promiseCallback: (Int?) -> Unit = { selectedIndex ->
                if (callbackId != null) {
                    resolveJsPromise(callbackId, selectedIndex?.toString() ?: "null")
                }
            }

            val sendResult = uiEventChannel.trySend(WebUiEvent.ShowSingleSelection(data, promiseCallback))
            if (sendResult.isFailure && callbackId != null) {
                rejectJsPromise(callbackId, "无法显示列表：事件队列已满")
            }
        } catch (e: Exception) {
            ToastManager.show("单选列表数据错误，无法显示。")
            if (callbackId != null) {
                rejectJsPromise(callbackId, "选项列表 JSON 无效: ${e.message}")
            }
        }
    }

    /**
     * 解析并保存导入的课程数据。
     */
    fun saveImportedCourses(coursesJsonString: String, callbackId: String? = null) {
        coroutineScope.launch(Dispatchers.Default) {
            val tableId = importTableId
            if (tableId == null) {
                coroutineScope.launch(Dispatchers.Main) {
                    ToastManager.show("导入失败：未选择课表。")
                    if (callbackId != null) rejectJsPromise(callbackId, "课表选择已取消。")
                }
                return@launch
            }

            val result = runCatching {
                val importedCoursesList = json.decodeFromString<List<CourseImportExport.ImportCourseJsonModel>>(coursesJsonString)
                courseConversionRepository.importCoursesFromList(tableId, importedCoursesList)
            }

            coroutineScope.launch(Dispatchers.Main) {
                result.onSuccess {
                    ToastManager.show("课程导入成功！课表已更新。")
                    if (callbackId != null) resolveJsPromise(callbackId, "true")
                }.onFailure { e ->
                    ToastManager.show("课程导入失败: ${e.message}")
                    if (callbackId != null) rejectJsPromise(callbackId, "课程导入失败: ${e.message}")
                }
            }
        }
    }

    /** 解析脚本输出的统一成绩数据，并在通知页面完成前写入本地数据库。 */
    fun saveImportedGrades(gradesJsonString: String, callbackId: String? = null) {
        coroutineScope.launch(Dispatchers.Default) {
            val result = try {
                val records = parseGradeRecords(gradesJsonString).also {
                    require(it.isNotEmpty()) { "脚本未返回有效成绩记录。" }
                }
                gradeRepository.replaceAll(records)
                Result.success(records)
            } catch (error: Throwable) {
                Result.failure(error)
            }
            coroutineScope.launch(Dispatchers.Main) {
                result.onSuccess { records ->
                    GradeImportStore.replace(records)
                    ToastManager.show("已解析 ${records.size} 条成绩记录。")
                    if (callbackId != null) resolveJsPromise(callbackId, records.size.toString())
                }.onFailure { error ->
                    ToastManager.show("成绩解析失败: ${error.message}")
                    if (callbackId != null) rejectJsPromise(callbackId, "成绩解析失败: ${error.message}")
                }
            }
        }
    }

    /**
     * 解析并保存课表配置信息。
     */
    fun saveCourseConfig(configJsonString: String, callbackId: String? = null) {
        coroutineScope.launch(Dispatchers.Default) {
            val tableId = importTableId
            if (tableId == null) {
                coroutineScope.launch(Dispatchers.Main) {
                    ToastManager.show("配置导入失败：未选择目标课表。")
                    if (callbackId != null) rejectJsPromise(callbackId, "课表选择已取消或未设置。")
                }
                return@launch
            }

            val result = runCatching {
                val importedConfig = json.decodeFromString<CourseImportExport.CourseConfigJsonModel>(configJsonString)
                courseConversionRepository.importCourseConfig(tableId, importedConfig)
            }

            coroutineScope.launch(Dispatchers.Main) {
                result.onSuccess {
                    ToastManager.show("课表配置导入成功！")
                    if (callbackId != null) resolveJsPromise(callbackId, "true")
                }.onFailure { e ->
                    ToastManager.show("课表配置导入失败: ${e.message}")
                    if (callbackId != null) rejectJsPromise(callbackId, "课表配置导入失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 解析并保存预设时间段信息。
     */
    fun savePresetTimeSlots(timeSlotsJsonString: String, callbackId: String? = null) {
        coroutineScope.launch(Dispatchers.Default) {
            val tableId = importTableId
            if (tableId == null) {
                coroutineScope.launch(Dispatchers.Main) {
                    ToastManager.show("导入失败：未选择课表。")
                    if (callbackId != null) rejectJsPromise(callbackId, "课表选择已取消。")
                }
                return@launch
            }

            val result = runCatching {
                val importedTimeSlotsJson = json.decodeFromString<List<CourseImportExport.TimeSlotJsonModel>>(timeSlotsJsonString)
                courseConversionRepository.importTimeSlots(tableId, importedTimeSlotsJson)
            }

            coroutineScope.launch(Dispatchers.Main) {
                result.onSuccess {
                    ToastManager.show("预设时间段导入成功！")
                    if (callbackId != null) resolveJsPromise(callbackId, "true")
                }.onFailure { e ->
                    ToastManager.show("预设时间段导入失败: ${e.message}")
                    if (callbackId != null) rejectJsPromise(callbackId, "预设时间段导入失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 通知 Web 任务执行完成，清理上下文状态。
     */
    fun notifyTaskCompletion() {
        importTableId = null
        onTaskCompleted()
    }

    private inline fun <reified T> parsePayload(payloadJson: String?): T? {
        if (payloadJson == null) return null
        return try {
            bridgeJson.decodeFromString<T>(payloadJson)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resolveJsPromise(callbackId: String, resultRawJs: String) {
        val script = buildJsCallbackScript(callbackId, isSuccess = true, resultRawJs = resultRawJs)
        coroutineScope.launch(Dispatchers.Main) {
            evaluateJs(script, null)
        }
    }

    private fun rejectJsPromise(callbackId: String, errorText: String) {
        val safeErrorJson = bridgeJson.encodeToString(errorText)
        val script = buildJsCallbackScript(callbackId, isSuccess = false, resultRawJs = safeErrorJson)
        coroutineScope.launch(Dispatchers.Main) {
            evaluateJs(script, null)
        }
    }
}
