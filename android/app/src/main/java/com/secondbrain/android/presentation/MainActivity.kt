package com.secondbrain.android.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.secondbrain.android.presentation.logs.LogListIntent
import com.secondbrain.android.presentation.logs.LogListScreen
import com.secondbrain.android.presentation.logs.LogListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: LogListViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MaterialTheme {
                Surface {
                    LogListScreen(
                        uiState = uiState,
                        onSyncClick = { viewModel.onIntent(LogListIntent.SyncNow) }
                    )
                }
            }
        }
    }
}

