package com.secondbrain.android.presentation.newlog

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    @Throws(IOException::class)
    fun startRecording(): Uri {
        val file = File(
            context.cacheDir,
            "audio_${System.currentTimeMillis()}.m4a"
        )
        outputFile = file

        @Suppress("DEPRECATION")
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mediaRecorder
        return Uri.fromFile(file)
    }

    fun stopRecording(): Uri? {
        return runCatching {
            recorder?.stop()
            outputFile?.let { Uri.fromFile(it) }
        }.also {
            release()
        }.getOrNull()
    }

    fun release() {
        runCatching { recorder?.release() }
        recorder = null
    }
}
