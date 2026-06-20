package com.secondbrain.android.presentation.newlog

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLogScreen(
    uiState: NewLogUiState,
    recordingState: RecordingState,
    onSaveText: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSaveAudio: () -> Unit,
    onDiscardRecording: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var textInput by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onStartRecording()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is NewLogUiState.Success -> onNavigateBack()
            is NewLogUiState.Error -> snackbarHostState.showSnackbar(uiState.message)
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Text") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Audio") }
                )
            }

            when (selectedTab) {
                0 -> TextInputTab(
                    text = textInput,
                    onTextChange = { textInput = it },
                    onSave = { onSaveText(textInput) },
                    isSaving = uiState is NewLogUiState.Saving,
                    modifier = Modifier.padding(16.dp)
                )
                1 -> AudioInputTab(
                    recordingState = recordingState,
                    isSaving = uiState is NewLogUiState.Saving,
                    onRequestRecord = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onStopRecording = onStopRecording,
                    onSaveAudio = onSaveAudio,
                    onDiscardRecording = onDiscardRecording,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TextInputTab(
    text: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("What's on your mind?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            maxLines = Int.MAX_VALUE
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSave,
            enabled = text.isNotBlank() && !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text("Save")
            }
        }
    }
}

@Composable
private fun AudioInputTab(
    recordingState: RecordingState,
    isSaving: Boolean,
    onRequestRecord: () -> Unit,
    onStopRecording: () -> Unit,
    onSaveAudio: () -> Unit,
    onDiscardRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (recordingState) {
                RecordingState.Idle -> {
                    Text(
                        text = "Tap to start recording",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = onRequestRecord,
                        modifier = Modifier.size(80.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Record",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                RecordingState.Recording -> {
                    Text(
                        text = "Recording…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = onStopRecording,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.size(80.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Filled.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                is RecordingState.Stopped -> {
                    Text(
                        text = "Recording ready",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = onSaveAudio,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Recording")
                        }
                    }
                    Button(
                        onClick = onDiscardRecording,
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Discard")
                    }
                }
            }
        }
    }
}
