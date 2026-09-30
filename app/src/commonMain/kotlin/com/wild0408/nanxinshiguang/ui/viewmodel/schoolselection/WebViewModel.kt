package com.wild0408.nanxinshiguang.ui.viewmodel.schoolselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wild0408.nanxinshiguang.data.repository.AppSettingsRepository
import com.wild0408.nanxinshiguang.data.repository.CourseConversionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import okio.FileSystem
import okio.Path
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class WebViewModel(
    val courseConversionRepository: CourseConversionRepository,
    private val appSettingsRepository: AppSettingsRepository,
    @Named("FilesDir") val filesDir: Path,
    val fileSystem: FileSystem
) : ViewModel() {
    val isDeveloperModeEnabled: StateFlow<Boolean> = appSettingsRepository.getAppSettings()
        .map { it.developerModeEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}