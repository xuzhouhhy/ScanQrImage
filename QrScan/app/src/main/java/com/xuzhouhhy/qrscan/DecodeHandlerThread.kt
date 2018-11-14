package com.xuzhouhhy.qrscan

import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
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

    companion object {
        const val MSG_HANDLER_DECODE_BITMAP = 0
        const val MSG_HANDLER_BITMAP_SIZE = MSG_HANDLER_DECODE_BITMAP + 1
        const val MSG_HANDLER_PREVIEW_FORMAT = MSG_HANDLER_DECODE_BITMAP + 2
        val TAG = DecodeHandlerThread::class.java.simpleName
    }

    var decodeHandler: Handler? = null

    var uiHandler: Handler? = null

//    var size: Size? = null

    var scanManager: ScanCameraManager? = null

    private val reader = QRCodeReader()

    var rect: Rect? = null
    val hints: MutableMap<DecodeHintType, Any> by lazy {
        val hints = mutableMapOf<DecodeHintType, Any>()
        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
        hints[DecodeHintType.POSSIBLE_FORMATS] = BarcodeFormat.QR_CODE
        return@lazy hints
    }

//    private val decodeFormats: Collection<BarcodeFormat> by lazy {
//
//    }
//
//    val multiFormatReader:MultiFormatReader by lazy {
//        MultiFormatReader()
//    }


    //ImageFormat
//    var previewFormat: Int? = null

    override fun onLooperPrepared() {
        decodeHandler = Handler(looper, this)
    }

    override fun handleMessage(msg: Message): Boolean {
        return when (msg.what) {
            MSG_HANDLER_DECODE_BITMAP -> {
                Log.i(ScanFragment.TAG, "receive MSG_HANDLER_DECODE_BITMAP")
                val data = msg.obj
                if (data is ByteArray) {
                    scanManager?.framingRectInPreview?.let {
                        val width = msg.arg1
                        val height = msg.arg2
                        val source = PlanarYUVLuminanceSource(data, width, height, 0, 0,
                                width, height, false)
                        val bitmap = BinaryBitmap(HybridBinarizer(source))
                        val decode = try {
                            reader.decode(bitmap, hints)
                        } catch (e: Exception) {
                            null
                        } finally {
                            reader.reset()
                        }
                        if (decode == null) {
                            Log.i(ScanFragment.TAG, "decode result is null")
                            uiHandler?.obtainMessage(ScanFragment.MSG_HANDLER_DECODE_FAIL)
                                    ?.sendToTarget()
                        } else {
                            Log.i(ScanFragment.TAG, "decode success ${decode.text}")
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
                }
                uiHandler?.obtainMessage(ScanFragment.MSG_HANDLER_DECODE_FAIL)
                        ?.sendToTarget()
                true
            }
            else -> false
        }
    }
}

