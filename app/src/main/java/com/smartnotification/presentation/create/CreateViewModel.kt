package com.smartnotification.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotification.domain.model.*
import com.smartnotification.domain.usecase.CreateNotificationUseCase
import com.smartnotification.domain.usecase.UpdateNotificationUseCase
import com.smartnotification.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class CreateUiState(
    val id: Int = 0,
    val title: String = "",
    val message: String = "",
    val selectedDate: LocalDate = LocalDate.now().plusDays(1),
    val selectedTime: LocalTime = LocalTime.of(9, 0),
    val priority: Priority = Priority.MEDIUM,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val titleError: String? = null,
    val messageError: String? = null
)

@HiltViewModel
class CreateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createNotification: CreateNotificationUseCase,
    private val updateNotification: UpdateNotificationUseCase,
    private val repository: NotificationRepository
) : ViewModel() {

    private val notificationId: Int = savedStateHandle["notificationId"] ?: -1

    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    init {
        if (notificationId != -1) {
            loadNotification(notificationId)
        }
    }

    private fun loadNotification(id: Int) {
        viewModelScope.launch {
            val item = repository.getNotificationById(id)
            item?.let {
                _uiState.update { s ->
                    s.copy(
                        id = it.id,
                        title = it.title,
                        message = it.message,
                        selectedDate = it.scheduledTime.toLocalDate(),
                        selectedTime = it.scheduledTime.toLocalTime(),
                        priority = it.priority,
                        repeatMode = it.repeatMode
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, titleError = null) }
    fun onMessageChange(value: String) = _uiState.update { it.copy(message = value, messageError = null) }
    fun onDateChange(value: LocalDate) = _uiState.update { it.copy(selectedDate = value) }
    fun onTimeChange(value: LocalTime) = _uiState.update { it.copy(selectedTime = value) }
    fun onPriorityChange(value: Priority) = _uiState.update { it.copy(priority = value) }
    fun onRepeatModeChange(value: RepeatMode) = _uiState.update { it.copy(repeatMode = value) }

    fun save() {
        val s = _uiState.value
        // Validate
        var hasError = false
        if (s.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            hasError = true
        }
        if (s.message.isBlank()) {
            _uiState.update { it.copy(messageError = "Message is required") }
            hasError = true
        }
        val scheduledTime = LocalDateTime.of(s.selectedDate, s.selectedTime)
        if (scheduledTime.isBefore(LocalDateTime.now()) && notificationId == -1) {
            _uiState.update { it.copy(errorMessage = "Scheduled time must be in the future") }
            return
        }
        if (hasError) return

        val item = NotificationItem(
            id = if (notificationId == -1) 0 else notificationId,
            title = s.title.trim(),
            message = s.message.trim(),
            scheduledTime = scheduledTime,
            priority = s.priority,
            repeatMode = s.repeatMode,
            status = NotificationStatus.SCHEDULED
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = if (notificationId == -1) createNotification(item) else updateNotification(item)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
            )
        }
    }

    fun errorShown() = _uiState.update { it.copy(errorMessage = null) }
}
