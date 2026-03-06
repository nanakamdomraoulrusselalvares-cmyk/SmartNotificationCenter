package com.smartnotification.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnotification.domain.model.HistoryItem
import com.smartnotification.domain.usecase.ClearHistoryUseCase
import com.smartnotification.domain.usecase.SearchHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val history: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val snackbarMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val searchHistory: SearchHistoryUseCase,
    private val clearHistory: ClearHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        searchQuery
            .flatMapLatest { q -> searchHistory(q) }
            .onEach { list -> _uiState.update { it.copy(history = list, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(q: String) {
        searchQuery.value = q
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun clearAll() {
        viewModelScope.launch {
            clearHistory()
            _uiState.update { it.copy(snackbarMessage = "History cleared") }
        }
    }

    fun snackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }
}
