@file:Suppress("DEPRECATION")

package com.xuzhouhhy.qrscan

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_scan.*

class ScanFragment : Fragment(), Handler.Callback {

    companion object {
        val TAG = ScanFragment::class.java.simpleName

        const val REQUEST_CAMERA_PERMISSION = 1

        const val FRAGMENT_TAG = "dialog"

        // message what of ui handler
        const val MSG_HANDLER_DECODE_SUCCESS = 0
        const val MSG_HANDLER_DECODE_FAIL = MSG_HANDLER_DECODE_SUCCESS + 1

        //the key map to scan result
        const val SCAN_RESULT = "SCAN_RESULT"
    }

    private var mCameraId: Int? = null

    private var mCamera: Camera? = null

    private var focusManager: AutoFocusManager? = null

    private var mDecodeThread: DecodeHandlerThread? = null

    private var manager: ScanCameraManager? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?)
            : View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onResume() {
        super.onResume()
        startDecodeThread()
        mDecodeThread?.uiHandler = Handler(Looper.getMainLooper(), this)
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                    //Orientation is fixed to ,so do nothing for now ,if orientation can change,
                    // should invoke chanconfigureTransform(width, height)
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    return true
                }

                override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                    openCamera(width, height)
                }
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        return when (msg.what) {
            MSG_HANDLER_DECODE_SUCCESS -> {
                val obj = msg.obj
                if (obj is String) {
                    activity?.let {
                        it.setResult(Activity.RESULT_OK, Intent().apply { this.putExtra(SCAN_RESULT, obj) })
                        it.finish()
                    }
                }
                true
            }
            MSG_HANDLER_DECODE_FAIL -> {
                Log.i(TAG, "receive MSG_HANDLER_DECODE_FAIL")
                mCamera?.let { capturePreview(it) }
                true
            }
            else -> false
        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
        stopDecodeThread()
    }

    private fun startDecodeThread() {
        mDecodeThread = DecodeHandlerThread("decode thread", Thread.NORM_PRIORITY)
        mDecodeThread?.start()
    }

    private fun stopDecodeThread() {
        mDecodeThread?.quitSafely()
        try {
            mDecodeThread?.join()
            mDecodeThread = null
        } catch (e: InterruptedException) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    private fun openCamera(width: Int, height: Int) {
        activity?.let { act ->
            if (ContextCompat.checkSelfPermission(act, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
                return
            }
            setCameraInfo(act)
            mCamera?.let { camera ->
                camera.setPreviewTexture(textureView.surfaceTexture)
                camera.startPreview()
                focusManager = AutoFocusManager(camera)
                focusManager?.start()
                capturePreview(camera)
            }
        }
    }

    private fun capturePreview(camera: Camera) {
        camera.setOneShotPreviewCallback { data, callBackCamera ->
            Log.i(TAG, "focus success and capture data size:${data.size}")
            manager?.cameraResolution?.let {
                mDecodeThread?.decodeHandler
                        ?.obtainMessage(DecodeHandlerThread.MSG_HANDLER_DECODE_BITMAP, it.x, it.y, data)
                        ?.sendToTarget()
            }
        }
    }

    private fun setCameraInfo(act: FragmentActivity) {
        val numberOfCameras = Camera.getNumberOfCameras()
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until numberOfCameras) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                continue
            }
            mCameraId = i
            break
        }
        mCamera = mCameraId?.let { cameraId ->
            val camera = Camera.open(cameraId)
            val parameters = camera.parameters
            manager = ScanCameraManager(act, camera).apply {
                finderView.setCameraManager(this)
                val bestPreviewSize = this.bestPreviewSize
                parameters.setPreviewSize(bestPreviewSize.x, bestPreviewSize.y)
            }
            mDecodeThread?.scanManager = manager
            //自动对焦
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            //闪光灯关闭
            parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
            //barcode扫描模式
            parameters.sceneMode = Camera.Parameters.SCENE_MODE_BARCODE
            setCameraOrientation(act, cameraInfo, camera)
            camera
        }
    }

    private fun setCameraOrientation(act: FragmentActivity, cameraInfo: Camera.CameraInfo, camera: Camera) {
        val degrees = when (act.windowManager.defaultDisplay.rotation) {
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

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            ConfirmationDialog().show(childFragmentManager, FRAGMENT_TAG)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                    ScanFragment.REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                activity?.let {
                    Toast.makeText(activity, "没有Camera权限", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun closeCamera() {
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

    class ConfirmationDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val parentFragment = parentFragment
            return AlertDialog.Builder(activity as Context)
                    .setMessage("扫描二维码需要Camera权限")
                    .setPositiveButton("OK") { _, _ ->
                        parentFragment?.requestPermissions(arrayOf(Manifest.permission.CAMERA),
                                ScanFragment.REQUEST_CAMERA_PERMISSION)
                    }
                    .setNegativeButton("NO") { _, _ ->
                        activity?.finish()
                    }
                    .create()
        }
    }
}