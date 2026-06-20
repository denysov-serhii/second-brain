package com.secondbrain.android.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.secondbrain.android.presentation.logs.LogListIntent
import com.secondbrain.android.presentation.logs.LogListScreen
import com.secondbrain.android.presentation.logs.LogListViewModel
import com.secondbrain.android.presentation.newlog.NewLogScreen
import com.secondbrain.android.presentation.newlog.NewLogViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val ROUTE_LOGS = "logs"
private const val ROUTE_NEW_LOG = "new_log"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_LOGS
                    ) {
                        composable(ROUTE_LOGS) {
                            val viewModel: LogListViewModel = hiltViewModel()
                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                            LogListScreen(
                                uiState = uiState,
                                onSyncClick = { viewModel.onIntent(LogListIntent.SyncNow) },
                                onNewLogClick = { navController.navigate(ROUTE_NEW_LOG) }
                            )
                        }
                        composable(ROUTE_NEW_LOG) {
                            val viewModel: NewLogViewModel = hiltViewModel()
                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                            val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
                            NewLogScreen(
                                uiState = uiState,
                                recordingState = recordingState,
                                onSaveText = { viewModel.saveTextLog(it) },
                                onStartRecording = { viewModel.startRecording() },
                                onStopRecording = { viewModel.stopRecording() },
                                onSaveAudio = {
                                    val stopped = recordingState
                                    if (stopped is com.secondbrain.android.presentation.newlog.RecordingState.Stopped) {
                                        viewModel.saveAudioLog(stopped.uri)
                                    }
                                },
                                onDiscardRecording = { viewModel.discardRecording() },
                                onNavigateBack = {
                                    viewModel.resetState()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

