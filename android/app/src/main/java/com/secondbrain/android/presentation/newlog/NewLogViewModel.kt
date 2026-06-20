package com.secondbrain.android.presentation.newlog

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.android.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NewLogUiState {
    data object Idle : NewLogUiState
    data object Saving : NewLogUiState
    data object Success : NewLogUiState
    data class Error(val message: String) : NewLogUiState
}

sealed interface RecordingState {
    data object Idle : RecordingState
    data object Recording : RecordingState
    data class Stopped(val uri: Uri) : RecordingState
}

@HiltViewModel
class NewLogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewLogUiState>(NewLogUiState.Idle)
    val uiState: StateFlow<NewLogUiState> = _uiState.asStateFlow()

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val audioRecorderManager = AudioRecorderManager(context)

    fun saveTextLog(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.value = NewLogUiState.Saving
            runCatching {
                logRepository.saveAndSyncLog(text = text, fileUri = null, type = "PERSONAL")
            }.onSuccess {
                _uiState.value = NewLogUiState.Success
            }.onFailure {
                _uiState.value = NewLogUiState.Error("Failed to save: ${it.message}")
            }
        }
    }

    fun saveAudioLog(audioUri: Uri) {
        viewModelScope.launch {
            _uiState.value = NewLogUiState.Saving
            runCatching {
                logRepository.saveAndSyncLog(text = null, fileUri = audioUri, type = "AUDIO")
            }.onSuccess {
                _uiState.value = NewLogUiState.Success
                _recordingState.value = RecordingState.Idle
            }.onFailure {
                _uiState.value = NewLogUiState.Error("Failed to save: ${it.message}")
            }
        }
    }

    fun startRecording() {
        runCatching { audioRecorderManager.startRecording() }
            .onSuccess { _recordingState.value = RecordingState.Recording }
            .onFailure { _uiState.value = NewLogUiState.Error("Cannot start recording: ${it.message}") }
    }

    fun stopRecording() {
        val uri = audioRecorderManager.stopRecording()
        _recordingState.value = if (uri != null) {
            RecordingState.Stopped(uri)
        } else {
            RecordingState.Idle
        }
    }

    fun discardRecording() {
        audioRecorderManager.release()
        _recordingState.value = RecordingState.Idle
    }

    fun resetState() {
        _uiState.value = NewLogUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorderManager.release()
    }
}
