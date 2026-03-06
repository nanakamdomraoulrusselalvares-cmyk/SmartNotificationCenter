package com.smartnotification.presentation.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotification.data.repository.NotificationRepository
import com.smartnotification.data.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

data class SettingsUiState(
    val darkMode: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        context.dataStore.data
            .map { prefs -> prefs[DARK_MODE_KEY] ?: false }
            .onEach { dark -> _uiState.update { it.copy(darkMode = dark) } }
            .launchIn(viewModelScope)
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[DARK_MODE_KEY] = enabled }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.getAllNotifications().first().forEach { item ->
                scheduler.cancel(item.id)
            }
            repository.deleteAllNotifications()
            _uiState.update { it.copy(snackbarMessage = "All notifications cleared") }
        }
    }

    fun resetAppData() {
        viewModelScope.launch {
            repository.getAllNotifications().first().forEach { item ->
                scheduler.cancel(item.id)
            }
            repository.deleteAllNotifications()
            repository.clearHistory()
            _uiState.update { it.copy(snackbarMessage = "App data reset successfully") }
        }
    }

    fun snackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }
}
