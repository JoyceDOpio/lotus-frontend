package com.eternalfairy.lotus.viewmodel.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.IOException

private const val LOG_TAG = "AudioRecorder"

//@AndroidEntryPoint
//class AudioRecorder @Inject constructor(private val context: Context) {
class AudioRecorder (private val context: Context) {
    private var recorder: MediaRecorder? = null

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(filePath: String) {
        recorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setOutputFile(filePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
                // TODO: You CANNOT show a Toast on non-UI thread. You need to call Toast.makeText() (and most other functions dealing with the UI) from within the main thread.
            }

            start()
        }
    }

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }

        recorder = null
    }
}