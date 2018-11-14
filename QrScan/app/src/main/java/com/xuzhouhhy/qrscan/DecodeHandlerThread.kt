package com.xuzhouhhy.qrscan

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
        val TAG = DecodeHandlerThread::class.java.simpleName!!
    }

    var decodeHandler: Handler? = null

    var uiHandler: Handler? = null

    var scanManager: ScanCameraManager? = null

    private val reader = QRCodeReader()

    private val hints: MutableMap<DecodeHintType, Any> by lazy {
        val hints = mutableMapOf<DecodeHintType, Any>()
        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
        hints[DecodeHintType.POSSIBLE_FORMATS] = BarcodeFormat.QR_CODE
        return@lazy hints
    }

    override fun onLooperPrepared() {
        decodeHandler = Handler(looper, this)
    }

    override fun handleMessage(msg: Message): Boolean {
        return when (msg.what) {
            MSG_HANDLER_DECODE_BITMAP -> {
                val data = msg.obj
                if (data is ByteArray) {
                    scanManager?.framingRectInPreview?.let {
                        val width = msg.arg1
                        val height = msg.arg2
                        val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
                        val bitmap = BinaryBitmap(HybridBinarizer(source))
                        val decode = try {
                            reader.decode(bitmap, hints)
                        } catch (e: Exception) {
                            null
                        } finally {
                            reader.reset()
                        }
                        if (decode == null) {
                            Log.i(TAG, "decode result is null")
                            uiHandler?.obtainMessage(ScanFragment.MSG_HANDLER_DECODE_FAIL)
                                    ?.sendToTarget()
                        } else {
                            Log.i(TAG, "decode success ${decode.text}")
                        }
                    }
                } else {
                    uiHandler?.obtainMessage(ScanFragment.MSG_HANDLER_DECODE_FAIL)
                            ?.sendToTarget()
                }
                true
            }
            else -> false
        }
    }
}

