package com.xuzhouhhy.qrscan

import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.Process
import android.util.Log
import java.io.ByteArrayOutputStream

/**
 * created by hanhongyun on 2018/11/6 13:14
 *
 */
class DecodeHandlerThread constructor(name: String, priority: Int)
    : HandlerThread(name, priority), Handler.Callback {

    constructor(name: String) : this(name, Process.THREAD_PRIORITY_DEFAULT)

    companion object {
        const val MSG_HANDLER_DECODE_BITMAP = 0
        const val MSG_HANDLER_BITMAP_SIZE = MSG_HANDLER_DECODE_BITMAP + 1
        const val MSG_HANDLER_PREVIEW_FORMAT = MSG_HANDLER_DECODE_BITMAP + 2
        val TAG = DecodeHandlerThread::class.java.simpleName
    }

    var decodeHandler: Handler? = null

    var uiHandler: Handler? = null

    var size: Size? = null

    //ImageFormat
    var previewFormat: Int? = null

    override fun onLooperPrepared() {
        decodeHandler = Handler(looper, this)
    }

    override fun handleMessage(msg: Message): Boolean {
        return when (msg.what) {
            MSG_HANDLER_DECODE_BITMAP -> {
                Log.i(ScanFragment.TAG, "receive MSG_HANDLER_DECODE_BITMAP")
                val obj = msg.obj
                if (obj is ByteArray && previewFormat != null && size != null) {
                    val width = size!!.width
                    val height = size!!.height
                    val yuvImage: YuvImage? = try {
                        YuvImage(obj, previewFormat!!, width, height, null)
                    } catch (e: IllegalArgumentException) {
                        Log.e(ScanFragment.TAG, Log.getStackTraceString(e))
                        null
                    }
                    val stream = ByteArrayOutputStream()
                    yuvImage?.compressToJpeg(Rect(0, 0, width, height), 100, stream)
                    if (stream.size() > 0) {
                        val byteArray = stream.toByteArray()
                        val l = System.currentTimeMillis()
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        Log.i(ScanFragment.TAG, "data size:${byteArray.size},  cost time:${System.currentTimeMillis() - l},  bitmap size:${bitmap?.byteCount}")
                        bitmap.recycle()
                    }
                }
                uiHandler?.obtainMessage(ScanFragment.MSG_HANDLER_DECODE_FAIL)
                        ?.sendToTarget()
                true
            }
            MSG_HANDLER_BITMAP_SIZE -> {
                Log.i(ScanFragment.TAG, "receive MSG_HANDLER_BITMAP_SIZE")
                val obj = msg.obj
                if (obj is Size) {
                    size = obj
                }
                true
            }
            MSG_HANDLER_PREVIEW_FORMAT -> {
                Log.i(ScanFragment.TAG, "receive MSG_HANDLER_PREVIEW_FORMAT")
                val obj = msg.obj
                if (obj is Int) {
                    previewFormat = obj
                }
                true
            }
            else -> false
        }
    }
}