package dev.local.ytclient.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.local.ytclient.core.datastore.SettingsRepository
import dev.local.ytclient.core.datastore.ShortsMode
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Which tabs exist. Shorts Off removes one and the bar reflows. */
data class MainUiState(
    val tabs: List<KiteTab> = KiteTab.visibleFor(ShortsMode.Default),
)

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = settingsRepository.settings
        .map { settings -> MainUiState(tabs = KiteTab.visibleFor(settings.shortsMode)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState(),
        )
}
