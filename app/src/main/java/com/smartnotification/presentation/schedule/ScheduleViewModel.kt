package com.smartnotification.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotification.domain.model.NotificationItem
import com.smartnotification.domain.model.NotificationStatus
import com.smartnotification.domain.usecase.CancelNotificationUseCase
import com.smartnotification.domain.usecase.GetScheduledNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ScheduleUiState(
    val grouped: Map<LocalDate, List<NotificationItem>> = emptyMap(),
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getScheduled: GetScheduledNotificationsUseCase,
    private val cancelNotification: CancelNotificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        getScheduled()
            .onEach { list ->
                val grouped = list
                    .filter { it.status == NotificationStatus.SCHEDULED }
                    .groupBy { it.scheduledTime.toLocalDate() }
                    .toSortedMap()
                _uiState.update { it.copy(grouped = grouped, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun cancelItem(item: NotificationItem) {
        viewModelScope.launch {
            cancelNotification(item).fold(
                onSuccess = { _uiState.update { it.copy(snackbarMessage = "Notification cancelled") } },
                onFailure = { _uiState.update { it.copy(snackbarMessage = "Failed") } }
            )
        }
    }

    fun snackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }
}
