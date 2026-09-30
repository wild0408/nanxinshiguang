package com.wild0408.nanxinshiguang

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wild0408.nanxinshiguang.data.model.AppUiStyle
import com.wild0408.nanxinshiguang.ui.miuix.MiuixAndroidApp
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.main.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.viewmodel.koinViewModel

/** Android owns the UI-family switch; Material and Miuix never share a page tree. */
@Composable
fun AndroidAppRoot(
    targetDestinationFlow: MutableStateFlow<Destination?>? = null
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (!state.isReady) {
        Box(Modifier.fillMaxSize())
        return
    }

    when (state.appSettings.uiStyle) {
        AppUiStyle.MIUIX -> MiuixAndroidApp(state.appSettings, viewModel, targetDestinationFlow)
        AppUiStyle.MATERIAL -> App(targetDestinationFlow)
    }
}
