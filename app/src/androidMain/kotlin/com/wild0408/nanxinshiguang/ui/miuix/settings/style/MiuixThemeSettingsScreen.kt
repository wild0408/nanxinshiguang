package com.wild0408.nanxinshiguang.ui.miuix.settings.style

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.data.model.AppThemeMode
import com.wild0408.nanxinshiguang.data.model.AppUiStyle
import com.wild0408.nanxinshiguang.ui.components.AdvancedColorPicker
import com.wild0408.nanxinshiguang.ui.components.ColorPickerConfig
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.main.SettingsViewModel
import com.wild0408.nanxinshiguang.ui.theme.LocalIsDarkTheme
import com.wild0408.nanxinshiguang.ui.theme.supportsDynamicColor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.action_reset
import nanxinshiguang.generated.resources.custom_color_title
import nanxinshiguang.generated.resources.dark_primary_color
import nanxinshiguang.generated.resources.dynamic_color_desc
import nanxinshiguang.generated.resources.dynamic_color_title
import nanxinshiguang.generated.resources.light_primary_color
import nanxinshiguang.generated.resources.theme_color_hint
import nanxinshiguang.generated.resources.theme_mode_label
import nanxinshiguang.generated.resources.theme_settings_title
import nanxinshiguang.generated.resources.ui_style_label
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun MiuixThemeSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val settings = viewModel.uiState.collectAsState().value.appSettings
    val modes = AppThemeMode.entries
    val styles = AppUiStyle.entries
    val isMaterial = settings.uiStyle == AppUiStyle.MATERIAL
    val isDark = LocalIsDarkTheme.current
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current
    val direction = LocalLayoutDirection.current
    var showColorPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = stringResource(Res.string.theme_settings_title),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { alpha, shadow ->
                    HyperLiquidTopBarButton(
                        onClick = onBack,
                        backdrop = backdrop,
                        icon = MiuixIcons.ChevronBackward,
                        contentDescription = stringResource(Res.string.a11y_back),
                        backdropAlpha = alpha,
                        shadowAlpha = shadow,
                    )
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = padding.calculateLeftPadding(direction) + 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    end = padding.calculateRightPadding(direction) + 20.dp,
                    bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { SmallTitle(stringResource(Res.string.theme_mode_label)) }
                item {
                    top.yukonga.miuix.kmp.preference.WindowDropdownPreference(
                        modifier = Modifier.fillMaxWidth(),
                        items = modes.map { stringResource(it.labelRes) },
                        selectedIndex = modes.indexOf(settings.themeMode).coerceAtLeast(0),
                        title = stringResource(Res.string.theme_mode_label),
                        summary = stringResource(settings.themeMode.labelRes),
                        onSelectedIndexChange = { modes.getOrNull(it)?.let(viewModel::onThemeModeChanged) },
                    )
                }
                item { SmallTitle(stringResource(Res.string.ui_style_label)) }
                item {
                    top.yukonga.miuix.kmp.preference.WindowDropdownPreference(
                        modifier = Modifier.fillMaxWidth(),
                        items = styles.map { stringResource(it.labelRes) },
                        selectedIndex = styles.indexOf(settings.uiStyle).coerceAtLeast(0),
                        title = stringResource(Res.string.ui_style_label),
                        summary = stringResource(settings.uiStyle.labelRes),
                        onSelectedIndexChange = { styles.getOrNull(it)?.let(viewModel::onUiStyleChanged) },
                    )
                }
                if (isMaterial && supportsDynamicColor) {
                    item { SmallTitle(stringResource(Res.string.dynamic_color_title)) }
                    item {
                        BasicComponent(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(Res.string.dynamic_color_title),
                            summary = stringResource(Res.string.dynamic_color_desc),
                            onClick = { viewModel.onUseDynamicColorChanged(!settings.useDynamicColor) },
                            endActions = {
                                Switch(
                                    checked = settings.useDynamicColor,
                                    onCheckedChange = viewModel::onUseDynamicColorChanged,
                                )
                            },
                        )
                    }
                }
                if (isMaterial && (!supportsDynamicColor || !settings.useDynamicColor)) {
                    item { SmallTitle(stringResource(Res.string.custom_color_title)) }
                    item {
                        val label = stringResource(if (isDark) Res.string.dark_primary_color else Res.string.light_primary_color)
                        val color = Color(if (isDark) settings.customDarkPrimary else settings.customLightPrimary)
                        BasicComponent(
                            modifier = Modifier.fillMaxWidth(),
                            title = label,
                            summary = stringResource(Res.string.theme_color_hint),
                            onClick = { showColorPicker = true },
                            endActions = {
                                Box(Modifier.size(30.dp).clip(CircleShape).background(color))
                            },
                        )
                    }
                }
            }
        }
    }

    if (showColorPicker) {
        val label = stringResource(if (isDark) Res.string.dark_primary_color else Res.string.light_primary_color)
        val color = Color(if (isDark) settings.customDarkPrimary else settings.customLightPrimary)
        WindowDialog(show = true, title = label, onDismissRequest = { showColorPicker = false }, insideMargin = DpSize(12.dp, 12.dp)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    text = stringResource(Res.string.action_reset),
                    onClick = {
                        if (isDark) viewModel.onCustomDarkPrimaryChanged() else viewModel.onCustomLightPrimaryChanged()
                    },
                )
                AdvancedColorPicker(
                    initialColor = color,
                    onColorChanged = {
                        if (isDark) viewModel.onCustomDarkPrimaryChanged(it) else viewModel.onCustomLightPrimaryChanged(it)
                    },
                    config = ColorPickerConfig(showAlpha = false, showInputMode = true),
                )
            }
        }
    }
}
