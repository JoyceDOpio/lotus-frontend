package com.example.circularplanner.utils

import android.media.MediaRecorder
import android.util.Log
import java.io.IOException

private const val LOG_TAG = "AudioRecorder"

class AudioRecorder () {
    private var recorder: MediaRecorder? = null

    fun startRecording(filePath: String) {
        recorder = MediaRecorder().apply {
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