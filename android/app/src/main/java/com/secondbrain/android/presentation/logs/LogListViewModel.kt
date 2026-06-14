package com.secondbrain.android.presentation.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.android.domain.model.Log
import com.secondbrain.android.domain.usecase.GetAndSyncLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LogListUiState {
    data object Loading : LogListUiState
    data class Success(val logs: List<Log>) : LogListUiState
    data class Error(val message: String) : LogListUiState
}

sealed interface LogListIntent {
    data object Load : LogListIntent
    data object SyncNow : LogListIntent
}

@HiltViewModel
class LogListViewModel @Inject constructor(
    private val getAndSyncLogsUseCase: GetAndSyncLogsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LogListUiState>(LogListUiState.Loading)
    val uiState: StateFlow<LogListUiState> = _uiState.asStateFlow()

    init {
        onIntent(LogListIntent.Load)
    }

    fun onIntent(intent: LogListIntent) {
        when (intent) {
            LogListIntent.Load -> loadLogs(forceSync = false)
            LogListIntent.SyncNow -> loadLogs(forceSync = true)
        }
    }

    private fun loadLogs(forceSync: Boolean) {
        viewModelScope.launch {
            _uiState.value = LogListUiState.Loading
            runCatching { getAndSyncLogsUseCase(forceSync) }
                .onSuccess { _uiState.value = LogListUiState.Success(it) }
                .onFailure {
                    _uiState.value = LogListUiState.Error(it.message ?: "Failed to load logs")
                }
        }
    }
}

