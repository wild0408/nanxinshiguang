package com.wild0408.nanxinshiguang

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.wild0408.nanxinshiguang.data.model.StartScreen
import com.wild0408.nanxinshiguang.ui.components.AdaptiveNavigationScaffold
import com.wild0408.nanxinshiguang.ui.material.schedule.WeeklyScheduleScreen

import com.wild0408.nanxinshiguang.ui.material.settings.main.SettingsScreen
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.main.SettingsViewModel
import com.wild0408.nanxinshiguang.ui.material.settings.about.LanguageSettingScreen
import com.wild0408.nanxinshiguang.ui.material.settings.main.MoreOptionsScreen
import com.wild0408.nanxinshiguang.ui.material.settings.about.OpenSourceLicensesScreen
import com.wild0408.nanxinshiguang.ui.material.settings.backup.BackupScreen
import com.wild0408.nanxinshiguang.ui.material.settings.course.CourseTableConversionScreen
import com.wild0408.nanxinshiguang.ui.material.settings.course.AddEditCourseScreen
import com.wild0408.nanxinshiguang.ui.material.settings.course.CourseInstanceListScreen
import com.wild0408.nanxinshiguang.ui.material.settings.course.CourseNameListScreen
import com.wild0408.nanxinshiguang.ui.material.settings.course.ManageCourseTablesScreen
import com.wild0408.nanxinshiguang.ui.material.settings.notification.NotificationSettingsScreen
import com.wild0408.nanxinshiguang.ui.material.settings.quickactions.QuickActionsScreen
import com.wild0408.nanxinshiguang.ui.material.settings.quickactions.QuickDeleteScreen
import com.wild0408.nanxinshiguang.ui.material.settings.quickactions.TweakScheduleScreen
import com.wild0408.nanxinshiguang.ui.material.settings.style.StyleSettingsScreen
import com.wild0408.nanxinshiguang.ui.material.settings.style.ThemeSettingsScreen
import com.wild0408.nanxinshiguang.ui.material.settings.time.ComboScheduleEditScreen
import com.wild0408.nanxinshiguang.ui.material.settings.time.SingleScheduleEditScreen
import com.wild0408.nanxinshiguang.ui.material.settings.time.TimeScheduleManagementScreen

import com.wild0408.nanxinshiguang.ui.theme.ShiguangScheduleTheme
import com.wild0408.nanxinshiguang.ui.material.today.TodayScheduleScreen
import com.wild0408.nanxinshiguang.ui.material.service.electricity.ElectricityCenterScreen
import com.wild0408.nanxinshiguang.ui.material.service.grade.GradeCenterScreen
import com.wild0408.nanxinshiguang.ui.material.service.grade.LaborScoreDetailsScreen
import com.wild0408.nanxinshiguang.ui.material.service.ServiceScreen
import com.wild0408.nanxinshiguang.ui.material.service.bus.BusMapScreen
import com.wild0408.nanxinshiguang.ui.material.portal.MaterialPortalAccountScreen
import com.wild0408.nanxinshiguang.ui.portal.PortalBindScreen
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun App(
    targetDestinationFlow: MutableStateFlow<Destination?>? = null
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    if (state.isReady) {
        ShiguangScheduleTheme(settings = state.appSettings) {
            val startDest = remember(state.appSettings.startScreen) {
                when (state.appSettings.startScreen) {
                    StartScreen.COURSE_SCHEDULE -> Destination.CourseSchedule
                    StartScreen.TODAY_SCHEDULE -> Destination.TodaySchedule
                }
            }
            AppNavigation(
                startDestination = startDest,
                targetDestinationFlow = targetDestinationFlow
            )
        }
    } else {
        Surface(modifier = Modifier.fillMaxSize()) {}
    }
}

@Composable
fun AppNavigation(
    startDestination: Destination,
    targetDestinationFlow: MutableStateFlow<Destination?>? = null
) {
    val backStack = rememberNavBackStack(
        configuration = navSavedStateConfig,
        startDestination
    )

    val pendingDestination by targetDestinationFlow?.collectAsState() ?: remember { mutableStateOf(null) }

    val currentDestination = backStack.lastOrNull() as? Destination ?: startDestination

    var navHideFraction by remember { mutableFloatStateOf(0f) }

    val onNavigate: (Destination) -> Unit = remember(backStack) {
        { dest ->
            if (dest.isMainScreen) {
                if (backStack.lastOrNull() != dest) {
                    backStack.clear()
                    backStack.add(dest)
                }
            } else {
                if (backStack.lastOrNull() != dest) {
                    backStack.add(dest)
                }
            }
        }
    }

    LaunchedEffect(pendingDestination) {
        pendingDestination?.let { dest ->
            onNavigate(dest)
            targetDestinationFlow?.value = null
        }
    }

    val onBack: () -> Unit = remember(backStack) {
        {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        }
    }

    val animSpec = tween<IntOffset>(300)

    AdaptiveNavigationScaffold(
        currentDestination = currentDestination,
        onTabSelected = onNavigate,
        showNavigation = currentDestination.isMainScreen,
        navHideFractionProvider = { navHideFraction }
    ) { _ ->
        NavDisplay(
            backStack = backStack,
            onBack = onBack,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val initialDest = initialState.findDestination()
                val targetDest = targetState.findDestination()
                val initialRank = initialDest?.mainDestinationRank ?: -1
                val targetRank = targetDest?.mainDestinationRank ?: -1

                if (initialRank != -1 && targetRank != -1) {
                    if (targetRank < initialRank) {
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = animSpec) togetherWith
                                slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = animSpec) + fadeOut()
                    } else {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = animSpec) togetherWith
                                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = animSpec) + fadeOut()
                    }
                } else {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = animSpec) togetherWith
                            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = animSpec) + fadeOut()
                }
            },
            popTransitionSpec = {
                val initialDest = initialState.findDestination()
                val targetDest = targetState.findDestination()
                val initialRank = initialDest?.mainDestinationRank ?: -1
                val targetRank = targetDest?.mainDestinationRank ?: -1

                if (initialRank != -1 && targetRank != -1) {
                    if (targetRank < initialRank) {
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = animSpec) togetherWith
                                slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = animSpec) + fadeOut()
                    } else {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = animSpec) togetherWith
                                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = animSpec) + fadeOut()
                    }
                } else {
                    slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = animSpec) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { it }, animationSpec = animSpec)
                }
            },
            predictivePopTransitionSpec = {
                val initialDest = initialState.findDestination()
                val targetDest = targetState.findDestination()
                val initialRank = initialDest?.mainDestinationRank ?: -1
                val targetRank = targetDest?.mainDestinationRank ?: -1

                if (initialRank != -1 && targetRank != -1) {
                    if (targetRank < initialRank) {
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = animSpec) togetherWith
                                slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = animSpec) + fadeOut()
                    } else {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = animSpec) togetherWith
                                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = animSpec) + fadeOut()
                    }
                } else {
                    slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = animSpec) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { it }, animationSpec = animSpec)
                }
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            )
        ) { key ->
            val destination = key as Destination

            NavEntry(
                key = key,
                metadata = metadata {
                    put(ShiguangNavMetadata.IsMainScreenKey, destination.isMainScreen)
                    put(ShiguangNavMetadata.DestinationKey, destination)
                }
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScreenContent(
                        targetDest = destination,
                        onNavigate = onNavigate,
                        onBack = onBack,
                        onNavHideFractionChanged = { navHideFraction = it }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenContent(
    targetDest: Destination,
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    onNavHideFractionChanged: (Float) -> Unit
) {
    when (targetDest) {
        Destination.CourseSchedule -> WeeklyScheduleScreen(
            onNavigate = onNavigate,
            onBack = onBack,
            onNavHideFractionChanged = onNavHideFractionChanged
        )
        Destination.Settings -> SettingsScreen(onNavigate, onBack)
        Destination.TodaySchedule -> TodayScheduleScreen(onNavigate, onBack)
        Destination.Service -> ServiceScreen(onNavigate, onBack)
        Destination.GradeCenter -> GradeCenterScreen(onNavigate, onBack)
        Destination.GradeDetails -> GradeCenterScreen(onNavigate, onBack, details = true)
        Destination.LaborScoreDetails -> LaborScoreDetailsScreen(onBack, onBind = { onNavigate(Destination.PortalBind) })
        Destination.ElectricityCenter -> ElectricityCenterScreen(onBack)
        Destination.BusMap -> BusMapScreen(onBack)
        Destination.PortalAccount -> MaterialPortalAccountScreen(onNavigate, onBack)
        Destination.PortalBind -> PortalBindScreen(onBack = onBack, onCompleted = onBack)
        Destination.ManageCourseTables -> ManageCourseTablesScreen(onBack)

        Destination.CourseTableConversion -> CourseTableConversionScreen(onNavigate, onBack)
        Destination.NotificationSettings -> NotificationSettingsScreen(onBack)
        Destination.MoreOptions -> MoreOptionsScreen(onNavigate, onBack)
        Destination.OpenSourceLicenses -> OpenSourceLicensesScreen(onBack)
        Destination.QuickActions -> QuickActionsScreen(onNavigate, onBack)
        Destination.TweakSchedule -> TweakScheduleScreen(onBack)
        Destination.CourseManagementList -> CourseNameListScreen(onNavigate, onBack)
        Destination.StyleSettings -> StyleSettingsScreen(onBack)
        Destination.QuickDelete -> QuickDeleteScreen(onBack)
        Destination.ThemeSettings -> ThemeSettingsScreen(onBack)
        Destination.BackupAndRestore -> BackupScreen(onBack)
        Destination.LanguageSettings -> LanguageSettingScreen(onBack)

        Destination.TimeScheduleManagement -> TimeScheduleManagementScreen(
            onBack = onBack,
            onEditSingleSchedule = { tableId, isPublic, copyFromId ->
                onNavigate(Destination.SingleScheduleEdit(tableId, isPublic, copyFromId))
            },
            onEditComboSchedule = { comboId, copyFromId ->
                onNavigate(Destination.ComboScheduleEdit(comboId, copyFromId))
            }
        )

        // 单一/公共作息编辑页面路由
        is Destination.SingleScheduleEdit -> SingleScheduleEditScreen(
            tableId = targetDest.tableId,
            isPublic = targetDest.isPublic,
            copyFromId = targetDest.copyFromId,
            onBack = onBack
        )

        // 组合作息编辑页面路由
        is Destination.ComboScheduleEdit -> ComboScheduleEditScreen(
            comboId = targetDest.comboId,
            copyFromId = targetDest.copyFromId,
            onBack = onBack
        )


        is Destination.AddEditCourse -> AddEditCourseScreen(
            onBack, targetDest.courseId
        )
        is Destination.CourseManagementDetail -> CourseInstanceListScreen(
            targetDest.courseName, onBack, onNavigate
        )
    }
}

private fun Scene<*>.findDestination(): Destination? {
    return metadata[ShiguangNavMetadata.DestinationKey.toString()] as? Destination
}
