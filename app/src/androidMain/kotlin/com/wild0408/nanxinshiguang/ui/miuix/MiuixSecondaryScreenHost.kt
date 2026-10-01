package com.wild0408.nanxinshiguang.ui.miuix

import androidx.compose.runtime.Composable
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.ui.miuix.service.grade.MiuixGradeCenterScreen
import com.wild0408.nanxinshiguang.ui.miuix.service.grade.MiuixLaborScoreDetailsScreen
import com.wild0408.nanxinshiguang.ui.miuix.service.electricity.MiuixElectricityCenterScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.quickactions.MiuixQuickActionsScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.quickactions.MiuixTweakScheduleScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.quickactions.MiuixQuickDeleteScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.notification.MiuixNotificationSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.course.MiuixCourseTableConversionScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.about.MiuixLanguageSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.style.MiuixThemeSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.main.MiuixMoreOptionsScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.about.MiuixOpenSourceLicensesScreen

import com.wild0408.nanxinshiguang.ui.miuix.settings.backup.MiuixBackupScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.course.MiuixManageCourseTablesScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.style.MiuixStyleSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.course.MiuixCourseNameListScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.course.MiuixCourseInstanceListScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.course.MiuixAddEditCourseScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.time.MiuixTimeScheduleManagementScreen

import com.wild0408.nanxinshiguang.ui.miuix.settings.time.MiuixComboScheduleEditScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.time.MiuixSingleScheduleEditScreen
import com.wild0408.nanxinshiguang.ui.miuix.portal.MiuixPortalAccountScreen
import com.wild0408.nanxinshiguang.ui.portal.PortalBindScreen
import com.wild0408.nanxinshiguang.ui.miuix.service.MiuixBusMapScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wild0408.nanxinshiguang.tool.AppLog
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.label_unsupported_destination
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val TAG = "MiuixSecondaryScreenHost"

@Composable
internal fun MiuixSecondaryScreenHost(
    destination: Destination,
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
) {
    when (destination) {

        Destination.TimeScheduleManagement -> MiuixTimeScheduleManagementScreen(
            onBack = onBack,
            onEditSingleSchedule = { tableId, isPublic, copyFromId ->
                onNavigate(Destination.SingleScheduleEdit(tableId, isPublic, copyFromId))
            },
            onEditComboSchedule = { comboId, copyFromId ->
                onNavigate(Destination.ComboScheduleEdit(comboId, copyFromId))
            },
        )
        Destination.GradeCenter -> MiuixGradeCenterScreen(onNavigate, onBack)
        Destination.GradeDetails -> MiuixGradeCenterScreen(onNavigate, onBack, details = true)
        Destination.LaborScoreDetails -> MiuixLaborScoreDetailsScreen(onBack, onBind = { onNavigate(Destination.PortalBind) })
        Destination.ElectricityCenter -> MiuixElectricityCenterScreen(onBack)
        Destination.BusMap -> MiuixBusMapScreen(onBack)
        Destination.PortalAccount -> MiuixPortalAccountScreen(onNavigate, onBack)
        Destination.PortalBind -> PortalBindScreen(onBack = onBack, onCompleted = onBack)
        Destination.QuickActions -> MiuixQuickActionsScreen(onNavigate, onBack)
        Destination.TweakSchedule -> MiuixTweakScheduleScreen(onBack)
        Destination.QuickDelete -> MiuixQuickDeleteScreen(onBack)
        Destination.NotificationSettings -> MiuixNotificationSettingsScreen(onBack)
        Destination.CourseTableConversion -> MiuixCourseTableConversionScreen(onNavigate, onBack)
        Destination.LanguageSettings -> MiuixLanguageSettingsScreen(onBack)
        Destination.ThemeSettings -> MiuixThemeSettingsScreen(onBack)
        Destination.MoreOptions -> MiuixMoreOptionsScreen(onNavigate, onBack)
        Destination.OpenSourceLicenses -> MiuixOpenSourceLicensesScreen(onBack)

        Destination.BackupAndRestore -> MiuixBackupScreen(onBack)
        Destination.ManageCourseTables -> MiuixManageCourseTablesScreen(onBack)
        Destination.StyleSettings -> MiuixStyleSettingsScreen(onBack)
        Destination.CourseManagementList -> MiuixCourseNameListScreen(onNavigate, onBack)
        is Destination.CourseManagementDetail -> MiuixCourseInstanceListScreen(destination.courseName, onBack, onNavigate)
        is Destination.AddEditCourse -> MiuixAddEditCourseScreen(onBack, destination.courseId)
        is Destination.SingleScheduleEdit -> MiuixSingleScheduleEditScreen(
            tableId = destination.tableId,
            isPublic = destination.isPublic,
            copyFromId = destination.copyFromId,
            onBack = onBack,
        )
        is Destination.ComboScheduleEdit -> MiuixComboScheduleEditScreen(
            comboId = destination.comboId,
            copyFromId = destination.copyFromId,
            onBack = onBack,
        )
        // 主页面（MainDestination）由 MiuixMainShell 消费，不会进入本宿主。
        // 这里保留兜底，但不再静默：Material 侧的 when 没有 else、漏登记会编译失败，
        // 而本宿主以前会渲染空白页，两侧保障强度不对等。现在记录日志并给出可见提示，
        // 便于新增路由时第一时间发现漏登记。
        else -> {
            AppLog.e(TAG, "未注册的 Miuix 二级页面: ${destination::class.simpleName ?: "Unknown"}")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.label_unsupported_destination),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}
