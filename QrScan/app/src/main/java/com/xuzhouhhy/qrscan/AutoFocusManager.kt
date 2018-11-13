package com.xuzhouhhy.qrscan

import android.hardware.Camera
import android.os.AsyncTask
import android.util.Log
import java.util.concurrent.RejectedExecutionException

/**
 * created by hanhongyun on 2018/11/12 16:07
 *
 */
@Suppress("DEPRECATION")
class AutoFocusManager(val camera: Camera) : Camera.AutoFocusCallback {

    companion object {
        private val AUTO_FOCUS_INTERVAL_MS = 1000L
        val TAG = AutoFocusManager::class.java.simpleName
    }

    private var focusing: Boolean = false
    private var outstandingTask: AsyncTask<*, *, *>? = null


    init {
        start()
    }

    override fun onAutoFocus(success: Boolean, camera: Camera?) {
        focusing = false
        autoFocusAgainLater()
    }

    @Synchronized
    fun start() {
        outstandingTask = null
        if (!focusing) {
            try {
                camera.autoFocus(this)
                focusing = true
            } catch (e: RuntimeException) {
                Log.w(TAG, Log.getStackTraceString(e))
                autoFocusAgainLater()
            }
        }
    }

    @Synchronized
    private fun autoFocusAgainLater() {
        if (outstandingTask == null) {
            val newTask = AutoFocusTask(this)
            try {
                newTask.execute(AsyncTask.THREAD_POOL_EXECUTOR)
                outstandingTask = newTask
            } catch (e: RejectedExecutionException) {
                Log.w(TAG, Log.getStackTraceString(e))
            }
        }
    }

    @Synchronized
    fun stop() {
        cancelOutstandingTask()
        try {
            camera.cancelAutoFocus()
        } catch (e: RuntimeException) {
            Log.w(TAG, Log.getStackTraceString(e))
        }
    }

    @Synchronized
    private fun cancelOutstandingTask() {
        outstandingTask?.let {
            if (it.status != AsyncTask.Status.FINISHED) {
                it.cancel(true)
            }
            outstandingTask = null
        }
    }

    class AutoFocusTask(val autoFocusManager: AutoFocusManager) : AsyncTask<Any, Any, Any>() {

        override fun doInBackground(vararg params: Any?): Any {
            try {
                Thread.sleep(AUTO_FOCUS_INTERVAL_MS)
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
            }
            autoFocusManager.start()
            return Any()
        }
    }
}