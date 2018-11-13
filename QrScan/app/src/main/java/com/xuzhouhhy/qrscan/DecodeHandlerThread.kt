package com.xuzhouhhy.qrscan

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.Process
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

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
                var data = msg.obj
                if (data is ByteArray && previewFormat != null && size != null) {
                    var width = size!!.width
                    var height = size!!.height

//                    val rotatedData = ByteArray(data.size)
//                    for (y in 0 until width) {
//                        for (x in 0 until height) {
//                            rotatedData[x * width + width - y - 1] = data[x + y * height]
//                        }
//                    }
//                    val temp = width
//                    width = height
//                    height = temp
//                    data = rotatedData


                    val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
                    val bitmap = BinaryBitmap(HybridBinarizer(source))
                    val decode = try {
                        val hints = mutableMapOf<DecodeHintType, Any>()
                        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
                        hints[DecodeHintType.POSSIBLE_FORMATS] = BarcodeFormat.QR_CODE
                        hints[DecodeHintType.TRY_HARDER] = true
                        val reader = QRCodeReader()
                        reader.decode(bitmap, hints)
                    } catch (e: Exception) {
                        null
                    }
                    if (decode == null) {
                        Log.i(ScanFragment.TAG, "decode result is null")
                        uiHandler?.obtainMessage(ScanFragment.MSG_HANDLER_DECODE_FAIL)
                                ?.sendToTarget()
                    } else {
                        Log.i(ScanFragment.TAG, decode.text)
                    }
//                    val yuvImage: YuvImage? = try {
//                        YuvImage(obj, previewFormat!!, width, height, null)
//                    } catch (e: IllegalArgumentException) {
//                        Log.e(ScanFragment.TAG, Log.getStackTraceString(e))
//                        null
//                    }
//                    val stream = ByteArrayOutputStream()
//                    yuvImage?.compressToJpeg(Rect(0, 0, width, height), 100, stream)
//                    if (stream.size() > 0) {
//                        val byteArray = stream.toByteArray()
//                        val l = System.currentTimeMillis()
//                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//                        Log.i(ScanFragment.TAG, "data size:${byteArray.size},  cost time:${System.currentTimeMillis() - l},  bitmap size:${bitmap?.byteCount}")
//                        bitmap.getPixels()
////                        Reader()
//                        bitmap.recycle()
//                    }
                }
//                uiHandler?.obtainMessage(ScanFragment.MSG_HANDLER_DECODE_FAIL)
//                        ?.sendToTarget()
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