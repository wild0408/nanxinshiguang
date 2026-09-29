package com.wild0408.nanxinshiguang.ui.miuix.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.wild0408.nanxinshiguang.data.model.AppSettingsModel
import com.wild0408.nanxinshiguang.data.model.AppThemeMode
import com.wild0408.nanxinshiguang.data.model.AppUiStyle
import com.wild0408.nanxinshiguang.ui.theme.LocalIsDarkTheme
import com.wild0408.nanxinshiguang.ui.theme.LocalUiStyle
import com.wild0408.nanxinshiguang.ui.theme.SetupPlatformThemeEffects
import com.wild0408.nanxinshiguang.ui.theme.Typography
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColorScheme

/** Android Miuix root using the library's fixed palette without MaterialKolor work. */
@Composable
internal fun ShiguangMiuixTheme(
    settings: AppSettingsModel,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (settings.themeMode) {
        AppThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val miuixColors = remember(darkTheme) {
        if (darkTheme) miuixDarkColorScheme() else miuixLightColorScheme()
    }
    // A static Material scheme remains only for secondary pages that have not
    // yet moved into the isolated Android Miuix tree.
    val compatibilityColors = remember(darkTheme, miuixColors) {
        if (darkTheme) {
            darkColorScheme(
                primary = miuixColors.primary,
                onPrimary = miuixColors.onPrimary,
                primaryContainer = miuixColors.primaryContainer,
                onPrimaryContainer = miuixColors.onPrimaryContainer,
                secondary = miuixColors.secondary,
                onSecondary = miuixColors.onSecondary,
                secondaryContainer = miuixColors.secondaryContainer,
                onSecondaryContainer = miuixColors.onSecondaryContainer,
                error = miuixColors.error,
                onError = miuixColors.onError,
                errorContainer = miuixColors.errorContainer,
                onErrorContainer = miuixColors.onErrorContainer,
                background = miuixColors.background,
                onBackground = miuixColors.onBackground,
                surface = miuixColors.surface,
                onSurface = miuixColors.onSurface,
                surfaceVariant = miuixColors.surfaceVariant,
                outline = miuixColors.outline,
                outlineVariant = miuixColors.dividerLine,
            )
        } else {
            lightColorScheme(
                primary = miuixColors.primary,
                onPrimary = miuixColors.onPrimary,
                primaryContainer = miuixColors.primaryContainer,
                onPrimaryContainer = miuixColors.onPrimaryContainer,
                secondary = miuixColors.secondary,
                onSecondary = miuixColors.onSecondary,
                secondaryContainer = miuixColors.secondaryContainer,
                onSecondaryContainer = miuixColors.onSecondaryContainer,
                error = miuixColors.error,
                onError = miuixColors.onError,
                errorContainer = miuixColors.errorContainer,
                onErrorContainer = miuixColors.onErrorContainer,
                background = miuixColors.background,
                onBackground = miuixColors.onBackground,
                surface = miuixColors.surface,
                onSurface = miuixColors.onSurface,
                surfaceVariant = miuixColors.surfaceVariant,
                outline = miuixColors.outline,
                outlineVariant = miuixColors.dividerLine,
            )
        }
    }

    SetupPlatformThemeEffects(
        colorScheme = compatibilityColors,
        darkTheme = darkTheme,
        themeMode = settings.themeMode,
    )
    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalUiStyle provides AppUiStyle.MIUIX,
    ) {
        MaterialTheme(colorScheme = compatibilityColors, typography = Typography) {
            MiuixTheme(colors = miuixColors, content = content)
        }
    }
}
