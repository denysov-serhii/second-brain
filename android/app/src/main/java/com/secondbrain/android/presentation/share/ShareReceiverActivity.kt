package com.secondbrain.android.presentation.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.secondbrain.android.data.repository.LogRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {

    @Inject
    lateinit var logRepository: LogRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action != Intent.ACTION_SEND) {
            finish()
            return
        }

        val mimeType = intent.type.orEmpty()
        when {
            mimeType.startsWith("text/") -> handleTextShare(intent)
            mimeType.startsWith("audio/") || mimeType.startsWith("image/") -> handleFileShare(intent, mimeType)
            else -> finish()
        }
    }

    private fun handleTextShare(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText.isNullOrBlank()) {
            finish()
            return
        }
        lifecycleScope.launch {
            runCatching {
                logRepository.saveAndSyncLog(
                    text = sharedText,
                    fileUri = null,
                    type = "PERSONAL"
                )
            }.onSuccess {
                Toast.makeText(this@ShareReceiverActivity, "Saved to Second Brain", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@ShareReceiverActivity, "Failed to save", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private fun handleFileShare(intent: Intent, mimeType: String) {
        @Suppress("DEPRECATION")
        val fileUri: Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        if (fileUri == null) {
            finish()
            return
        }

        val logType = if (mimeType.startsWith("audio/")) "AUDIO" else "PERSONAL"
        lifecycleScope.launch {
            runCatching {
                logRepository.saveAndSyncLog(
                    text = null,
                    fileUri = fileUri,
                    type = logType
                )
            }.onSuccess {
                Toast.makeText(this@ShareReceiverActivity, "Saved to Second Brain", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@ShareReceiverActivity, "Failed to save", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
