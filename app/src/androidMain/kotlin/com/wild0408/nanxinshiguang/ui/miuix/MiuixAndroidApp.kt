package com.wild0408.nanxinshiguang.ui.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperAppScaffold
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperNavigationItem
import com.wild0408.nanxinshiguang.ui.miuix.hyper.components.HyperPageTransition
import com.wild0408.nanxinshiguang.ui.miuix.hyper.navigation.HyperSecondaryNavigationHost
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.isMainScreen
import com.wild0408.nanxinshiguang.mainDestinationRank
import com.wild0408.nanxinshiguang.navSavedStateConfig
import com.wild0408.nanxinshiguang.data.model.AppSettingsModel
import com.wild0408.nanxinshiguang.data.model.AppUiStyle
import com.wild0408.nanxinshiguang.data.model.StartScreen
import com.wild0408.nanxinshiguang.ui.miuix.service.MiuixServiceScreen
import com.wild0408.nanxinshiguang.ui.miuix.schedule.MiuixTodayScheduleScreen
import com.wild0408.nanxinshiguang.ui.miuix.schedule.MiuixWeeklyScheduleScreen
import com.wild0408.nanxinshiguang.ui.miuix.settings.main.MiuixSettingsScreen
import com.wild0408.nanxinshiguang.ui.miuix.theme.ShiguangMiuixTheme
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostEnabled
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.account_circle_filled_24px
import nanxinshiguang.generated.resources.nav_course_schedule
import nanxinshiguang.generated.resources.nav_service
import nanxinshiguang.generated.resources.nav_settings
import nanxinshiguang.generated.resources.nav_today_schedule
import nanxinshiguang.generated.resources.service_filled_24px
import nanxinshiguang.generated.resources.view_agenda_filled_24px
import nanxinshiguang.generated.resources.view_week_filled_24px
import top.yukonga.miuix.kmp.theme.MiuixTheme

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Android-only Miuix entry point. Material/Common navigation is intentionally
 * not part of this tree; Android can evolve independently from other targets.
 */
@Composable
fun MiuixAndroidApp(
    settings: AppSettingsModel,
    settingsViewModel: SettingsViewModel,
    targetDestinationFlow: MutableStateFlow<Destination?>? = null,
) {
    ShiguangMiuixTheme(settings = settings.copy(uiStyle = AppUiStyle.MIUIX)) {
        MiuixRootNavigation(
            startDestination = when (settings.startScreen) {
                StartScreen.TODAY_SCHEDULE -> Destination.TodaySchedule
                StartScreen.COURSE_SCHEDULE -> Destination.CourseSchedule
            },
            settingsViewModel = settingsViewModel,
            targetDestinationFlow = targetDestinationFlow,
        )
    }
}

@Composable
private fun MiuixRootNavigation(
    startDestination: Destination,
    settingsViewModel: SettingsViewModel,
    targetDestinationFlow: MutableStateFlow<Destination?>? = null,
) {
    var selectedMain by remember { mutableStateOf(startDestination) }
    val backStack = rememberNavBackStack(
        configuration = navSavedStateConfig,
        startDestination,
    )

    fun navigate(destination: Destination) {
        if (destination.isMainScreen) {
            selectedMain = destination
            while (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        } else if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    val pendingDestination by targetDestinationFlow?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }

    LaunchedEffect(pendingDestination) {
        pendingDestination?.let { dest ->
            navigate(dest)
            targetDestinationFlow?.value = null
        }
    }

    fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    val courseContent = remember { movableContentOf { MiuixWeeklyScheduleScreen(::navigate, ::back, koinViewModel()) } }
    val todayContent = remember { movableContentOf { MiuixTodayScheduleScreen(::navigate, koinViewModel()) } }
    val serviceContent = remember { movableContentOf { MiuixServiceScreen(::navigate) } }
    val settingsContent = remember {
        movableContentOf { MiuixSettingsScreen(::navigate, settingsViewModel) }
    }

    val mainDestinations = listOf(
        Destination.TodaySchedule,
        Destination.CourseSchedule,
        Destination.Service,
        Destination.Settings,
    )
    val navigationItems = listOf(
        HyperNavigationItem(
            key = "today",
            label = stringResource(Res.string.nav_today_schedule),
            icon = vectorResource(Res.drawable.view_agenda_filled_24px),
        ),
        HyperNavigationItem(
            key = "schedule",
            label = stringResource(Res.string.nav_course_schedule),
            icon = vectorResource(Res.drawable.view_week_filled_24px),
        ),
        HyperNavigationItem(
            key = "service",
            label = stringResource(Res.string.nav_service),
            icon = vectorResource(Res.drawable.service_filled_24px),
        ),
        HyperNavigationItem(
            key = "settings",
            label = stringResource(Res.string.nav_settings),
            icon = vectorResource(Res.drawable.account_circle_filled_24px),
        ),
    )
    HyperSecondaryNavigationHost(
        backStack = backStack,
        onBack = ::back,
        modifier = Modifier.fillMaxSize(),
        entryProvider = { key ->
            val destination = key as Destination
            NavEntry(key = key) {
                if (destination.isMainScreen) {
                    MiuixMainShell(
                        selectedMain = selectedMain,
                        mainDestinations = mainDestinations,
                        navigationItems = navigationItems,
                        onNavigate = ::navigate,
                        courseContent = courseContent,
                        todayContent = todayContent,
                        serviceContent = serviceContent,
                        settingsContent = settingsContent,
                    )
                } else {
                    MiuixSecondaryScreenHost(
                        destination = destination,
                        onNavigate = ::navigate,
                        onBack = ::back,
                    )
                }
            }
        },
    )
}

@Composable
private fun MiuixMainShell(
    selectedMain: Destination,
    mainDestinations: List<Destination>,
    navigationItems: List<HyperNavigationItem>,
    onNavigate: (Destination) -> Unit,
    courseContent: @Composable () -> Unit,
    todayContent: @Composable () -> Unit,
    serviceContent: @Composable () -> Unit,
    settingsContent: @Composable () -> Unit,
) {
    val selectedIndex = mainDestinations.indexOf(selectedMain).coerceAtLeast(0)
    val pageBackground = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(pageBackground)
        drawContent()
    }

    HyperAppScaffold(
        items = navigationItems,
        selectedIndex = selectedIndex,
        onItemSelected = { index -> onNavigate(mainDestinations[index]) },
        backdrop = backdrop,
    ) { contentPadding ->
        CompositionLocalProvider(
            LocalNavigationHostEnabled provides false,
            LocalNavigationHostPadding provides contentPadding,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(pageBackground)
                    .layerBackdrop(backdrop),
            ) {
                HyperPageTransition(
                    targetState = selectedMain,
                    order = ::mainDestinationRank,
                    modifier = Modifier.fillMaxSize(),
                ) { destination ->
                    when (destination) {
                        Destination.CourseSchedule -> courseContent()
                        Destination.TodaySchedule -> todayContent()
                        Destination.Service -> serviceContent()
                        Destination.Settings -> settingsContent()
                        else -> courseContent()
                    }
                }
            }
        }
    }
}

private fun mainDestinationRank(destination: Destination): Int = destination.mainDestinationRank.let {
    if (it != -1) it else 1
}
