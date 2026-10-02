package com.wild0408.nanxinshiguang.ui.miuix.schedule

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wild0408.nanxinshiguang.data.db.main.Course
import com.wild0408.nanxinshiguang.data.model.ScheduleGridStyle
import com.wild0408.nanxinshiguang.ui.viewmodel.today.CourseDisplayModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import java.io.File

/**
 * 渲染真实的 Miuix 今日课表列表项并截图，验证"已结束"的视觉区分。
 *
 * 断言用像素"墨量"（相对白底的平均暗度）：已结束的课程整体 50% 不透明度 + 删除线，
 * 其墨量必然低于未结束的同款课程。截图会写到应用外部目录，便于 `adb pull` 人工复核。
 */
@RunWith(AndroidJUnit4::class)
class MiuixTodayCourseStyleTest {

    @get:Rule
    val compose = createComposeRule()

    private fun model(name: String, start: String, end: String) = CourseDisplayModel(
        course = Course(
            id = name,
            courseTableId = "table",
            name = name,
            teacher = "王教授",
            position = "物理实验室",
            day = 1,
            startSection = null,
            endSection = null,
            isCustomTime = true,
            customStartTime = start,
            customEndTime = end,
            colorInt = 0,
        ),
        startTime = start,
        endTime = end,
    )

    private fun ink(bitmap: Bitmap): Double {
        var sum = 0.0
        var count = 0
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val c = bitmap.getPixel(x, y)
                val luminance = (
                    0.299 * android.graphics.Color.red(c) +
                        0.587 * android.graphics.Color.green(c) +
                        0.114 * android.graphics.Color.blue(c)
                    ) / 255.0
                sum += 1.0 - luminance
                count++
            }
        }
        return sum / count
    }

    /**
     * 截图写入系统相册目录：该位置不属于应用数据目录，仪器测试结束后的自动卸载不会删掉它，
     * 因此可以用 `adb pull /sdcard/Pictures/nanxinshiguang-test/` 取回人工复核。
     */
    private fun save(bitmap: Bitmap, fileName: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/nanxinshiguang-test",
            )
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        // 同时写一份到应用外部目录，便于在保留安装的运行方式下直接取用
        context.getExternalFilesDir(null)?.let { dir ->
            File(dir, fileName).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    @Test
    fun finishedCourseRendersDimmerAndStruckThrough() {
        compose.setContent {
            MiuixTheme(colors = lightColorScheme()) {
                Column(Modifier.fillMaxWidth().background(Color.White)) {
                    androidx.compose.foundation.layout.Box(Modifier.testTag("finished")) {
                        MiuixTodayCourse(
                            model = model("大学物理", "08:00", "10:00"),
                            gridStyle = ScheduleGridStyle(),
                            dark = false,
                            isFinished = true,
                        )
                    }
                    androidx.compose.foundation.layout.Box(Modifier.testTag("upcoming")) {
                        MiuixTodayCourse(
                            model = model("计算机网络", "14:00", "16:00"),
                            gridStyle = ScheduleGridStyle(),
                            dark = false,
                            isFinished = false,
                        )
                    }
                }
            }
        }

        val finished = compose.onNodeWithTag("finished").captureToImage().asAndroidBitmap()
        val upcoming = compose.onNodeWithTag("upcoming").captureToImage().asAndroidBitmap()
        save(finished, "miuix_today_finished.png")
        save(upcoming, "miuix_today_upcoming.png")

        val finishedInk = ink(finished)
        val upcomingInk = ink(upcoming)
        assertTrue(
            "已结束课程应比未结束更淡：finished=$finishedInk upcoming=$upcomingInk",
            finishedInk < upcomingInk,
        )
    }
}
