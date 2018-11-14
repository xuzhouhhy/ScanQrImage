@file:Suppress("DEPRECATION")

package com.xuzhouhhy.qrscan

import android.app.Activity
import android.graphics.Rect
import android.hardware.Camera
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.TextureView

/**
 * created by hanhongyun on 2018/11/13 14:14
 *
 */
class ScanCameraManager constructor(val activity: Activity) {

    companion object {
        private const val MIN_FRAME_WIDTH = 240
        private const val MIN_FRAME_HEIGHT = 240
        private const val MAX_FRAME_WIDTH = 1200 // = 5/8 * 1920
        private const val MAX_FRAME_HEIGHT = 675 // = 5/8 * 1080
    }

    private var mCamera: Camera? = null

    private var focusManager: AutoFocusManager? = null

    private var mDecodeThread: DecodeHandlerThread? = null

    private lateinit var cameraConfigurationManager: CameraConfigurationManager

    /**
     *  取景框
     */
    val framingRect: Rect
        get() {
            val screenResolution = cameraConfigurationManager.screenResolution
            val width = CameraConfigUtils.findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH)
            val height = CameraConfigUtils.findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT)
            val leftOffset = (screenResolution.x - width) / 2
            val topOffset = (screenResolution.y - height) / 2
            return Rect(leftOffset, topOffset, leftOffset + width, topOffset + height)
        }

    /**
     * 二维码框
     */
    val framingRectInPreview: Rect
        get() {
            val rect = Rect(framingRect)
            val cameraResolution = cameraConfigurationManager.cameraResolution
            val screenResolution = cameraConfigurationManager.screenResolution
            return Rect(rect.left * cameraResolution.x / screenResolution.x,
                    rect.top * cameraResolution.y / screenResolution.y,
                    rect.right * cameraResolution.x / screenResolution.x,
                    rect.bottom * cameraResolution.y / screenResolution.y)
        }

    fun openCamera(textureView: TextureView) {
        setCameraInfo()
        mCamera?.let { camera ->
            camera.setPreviewTexture(textureView.surfaceTexture)
            camera.startPreview()
            focusManager = AutoFocusManager(camera)
            capturePreview()
        }
    }

    fun capturePreview() {
        mCamera?.setOneShotPreviewCallback { data, _ ->
            Log.i(ScanFragment.TAG, "focus success and capture data size:${data.size}")
            cameraConfigurationManager.cameraResolution.let {
                mDecodeThread?.decodeHandler
                        ?.obtainMessage(DecodeHandlerThread.MSG_HANDLER_DECODE_BITMAP, it.x, it.y, data)
                        ?.sendToTarget()
            }
        }
    }

    fun startDecodeThread(callback: Handler.Callback) {
        mDecodeThread = DecodeHandlerThread("decode thread", Thread.NORM_PRIORITY)
        mDecodeThread?.start()
        mDecodeThread?.uiHandler = Handler(Looper.getMainLooper(), callback)
    }

    fun stopDecodeThread() {
        mDecodeThread?.quitSafely()
        try {
            mDecodeThread?.join()
            mDecodeThread = null
        } catch (e: InterruptedException) {
            Log.e(ScanFragment.TAG, Log.getStackTraceString(e))
        }
    }

    fun closeCamera() {
        mCamera = mCamera?.let {
            it.startPreview()
            it.release()
            null
        }
        focusManager?.let {
            it.stop()
            focusManager = null
        }
    }

    private fun setCameraInfo() {
        val numberOfCameras = Camera.getNumberOfCameras()
        val cameraInfo = Camera.CameraInfo()
        var cameraId = 0
        for (i in 0 until numberOfCameras) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                continue
            }
            cameraId = i
            break
        }
        mCamera = Camera.open(cameraId).apply {
            cameraConfigurationManager = CameraConfigurationManager(activity, this)
            val parameters = this.parameters
            val bestPreviewSize = cameraConfigurationManager.bestPreviewSize
            parameters.setPreviewSize(bestPreviewSize.x, bestPreviewSize.y)
            mDecodeThread?.scanManager = this@ScanCameraManager
            //自动对焦
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            //闪光灯关闭
            parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
            //barcode扫描模式
            parameters.sceneMode = Camera.Parameters.SCENE_MODE_BARCODE
            setCameraOrientation(cameraInfo, this)
        }
    }

    private fun setCameraOrientation(cameraInfo: Camera.CameraInfo, camera: Camera) {
        val degrees = when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        val result: Int = if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            (360 - ((cameraInfo.orientation + degrees) % 360)) % 360
        } else {
            (cameraInfo.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }

}