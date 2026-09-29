package com.wild0408.nanxinshiguang.ui.miuix

import androidx.compose.runtime.Composable
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixGradeCenterScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixElectricityCenterScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixQuickActionsScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixTweakScheduleScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixQuickDeleteScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixNotificationSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixCourseTableConversionScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixLanguageSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixThemeSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixMoreOptionsScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixOpenSourceLicensesScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixUpdateRepoScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixBackupScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixManageCourseTablesScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixStyleSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixCourseNameListScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixCourseInstanceListScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixAddEditCourseScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixTimeScheduleManagementScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixSchoolSelectionListScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixAdapterSelectionScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixComboScheduleEditScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixSingleScheduleEditScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixWebViewScreen
import com.wild0408.nanxinshiguang.ui.miuix.screens.MiuixPortalAccountScreen
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
