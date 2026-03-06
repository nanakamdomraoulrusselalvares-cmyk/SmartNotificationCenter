package com.smartnotification.presentation.priority

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotification.domain.model.Priority
import com.smartnotification.domain.model.PrioritySettings
import com.smartnotification.domain.usecase.GetPrioritySettingsUseCase
import com.smartnotification.domain.usecase.UpdatePrioritySettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PriorityUiState(
    val settings: List<PrioritySettings> = emptyList(),
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null
)

@HiltViewModel
class PriorityViewModel @Inject constructor(
    private val getSettings: GetPrioritySettingsUseCase,
    private val updateSettings: UpdatePrioritySettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriorityUiState())
    val uiState: StateFlow<PriorityUiState> = _uiState.asStateFlow()

    init {
        getSettings()
            .onEach { list ->
                val sorted = list.sortedByDescending { it.priority.level }
                _uiState.update { it.copy(settings = sorted, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleSound(priority: Priority, enabled: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.settings.find { it.priority == priority } ?: return@launch
            updateSettings(current.copy(soundEnabled = enabled))
        }
    }

    fun toggleVibration(priority: Priority, enabled: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.settings.find { it.priority == priority } ?: return@launch
            updateSettings(current.copy(vibrationEnabled = enabled))
        }
    }

    fun toggleHeadsUp(priority: Priority, enabled: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.settings.find { it.priority == priority } ?: return@launch
            updateSettings(current.copy(headsUpEnabled = enabled))
        }
    }

    fun snackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }
}
