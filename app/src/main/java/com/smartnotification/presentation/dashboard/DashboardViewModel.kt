package com.smartnotification.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotification.domain.model.NotificationItem
import com.smartnotification.domain.model.Priority
import com.smartnotification.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedPriority: Priority? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getAllNotifications: GetAllNotificationsUseCase,
    private val searchNotifications: SearchNotificationsUseCase,
    private val deleteNotification: DeleteNotificationUseCase,
    private val cancelNotification: CancelNotificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val selectedPriority = MutableStateFlow<Priority?>(null)

    init {
        observeNotifications()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeNotifications() {
        combine(searchQuery, selectedPriority) { query, priority -> query to priority }
            .flatMapLatest { (query, priority) ->
                when {
                    priority != null -> searchNotifications(query).map { list ->
                        list.filter { it.priority == priority }
                    }
                    else -> searchNotifications(query)
                }
            }
            .onEach { list ->
                _uiState.update { it.copy(notifications = list, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onPriorityFilterChange(priority: Priority?) {
        selectedPriority.value = priority
        _uiState.update { it.copy(selectedPriority = priority) }
    }

    fun deleteItem(item: NotificationItem) {
        viewModelScope.launch {
            deleteNotification(item).fold(
                onSuccess = { _uiState.update { it.copy(snackbarMessage = "Notification deleted") } },
                onFailure = { _uiState.update { it.copy(snackbarMessage = "Failed to delete") } }
            )
        }
    }

    fun cancelItem(item: NotificationItem) {
        viewModelScope.launch {
            cancelNotification(item).fold(
                onSuccess = { _uiState.update { it.copy(snackbarMessage = "Notification cancelled") } },
                onFailure = { _uiState.update { it.copy(snackbarMessage = "Failed to cancel") } }
            )
        }
    }

    fun snackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
