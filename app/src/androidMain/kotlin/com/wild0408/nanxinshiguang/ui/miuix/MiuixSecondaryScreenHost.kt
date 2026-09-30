package com.wild0408.nanxinshiguang.ui.miuix

import androidx.compose.runtime.Composable
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.ui.miuix.service.grade.MiuixGradeCenterScreen
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
import com.wild0408.nanxinshiguang.ui.miuix.settings.about.MiuixUpdateRepoScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.backup.MiuixBackupScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.course.MiuixManageCourseTablesScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.style.MiuixStyleSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.course.MiuixCourseNameListScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.course.MiuixCourseInstanceListScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.course.MiuixAddEditCourseScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.time.MiuixTimeScheduleManagementScreen
import com.wild0408.nanxinshiguang.ui.miuix.schoolselection.MiuixSchoolSelectionListScreen
import com.wild0408.nanxinshiguang.ui.miuix.schoolselection.MiuixAdapterSelectionScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.time.MiuixComboScheduleEditScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.time.MiuixSingleScheduleEditScreen
import com.wild0408.nanxinshiguang.ui.miuix.schoolselection.MiuixWebViewScreen
import com.wild0408.nanxinshiguang.ui.miuix.portal.MiuixPortalAccountScreen
import com.wild0408.nanxinshiguang.ui.portal.PortalBindScreen

@Composable
internal fun MiuixSecondaryScreenHost(
    destination: Destination,
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
) {
    when (destination) {
        Destination.SchoolSelectionListScreen -> MiuixSchoolSelectionListScreen(
            onNavigate = onNavigate,
            onBack = onBack,
        )
        is Destination.AdapterSelection -> MiuixAdapterSelectionScreen(
            onNavigate = onNavigate,
            onBack = onBack,
            schoolId = destination.schoolId,
            schoolName = destination.schoolName,
            categoryNumber = destination.categoryNumber,
            resourceFolder = destination.resourceFolder,
        )
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
        Destination.ElectricityCenter -> MiuixElectricityCenterScreen(onBack)
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
        Destination.UpdateRepo -> MiuixUpdateRepoScreen(onBack)
        Destination.BackupAndRestore -> MiuixBackupScreen(onBack)
        Destination.ManageCourseTables -> MiuixManageCourseTablesScreen(onBack)
        Destination.StyleSettings -> MiuixStyleSettingsScreen(onBack)
        Destination.CourseManagementList -> MiuixCourseNameListScreen(onNavigate, onBack)
        is Destination.CourseManagementDetail -> MiuixCourseInstanceListScreen(destination.courseName, onBack, onNavigate)
        is Destination.AddEditCourse -> MiuixAddEditCourseScreen(onBack, destination.courseId)
        is Destination.WebView -> MiuixWebViewScreen(
            onNavigate = onNavigate,
            onBack = onBack,
            initialUrl = destination.initialUrl,
            assetJsPath = destination.assetJsPath,
        )
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
        // Main destinations are consumed by MiuixMainShell and never reach this host.
        else -> Unit
    }
}
