@file:Suppress("DEPRECATION")

package com.xuzhouhhy.qrscan

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
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

    private var manager: ScanCameraManager? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?)
            : View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager?.let { finderView.setCameraManager(it) }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity?.let { manager = ScanCameraManager(it) }
    }

    override fun onResume() {
        super.onResume()
        manager?.startDecodeThread(this)
        if (textureView.isAvailable) {
            openCamera()
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
                    openCamera()
                }
            }
        }
    }

    private fun openCamera() {
        activity?.let { act ->
            if (ContextCompat.checkSelfPermission(act, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
                return@let
            }
            manager?.openCamera(textureView)
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
                manager?.capturePreview()
                true
            }
            else -> false
        }
    }

    override fun onPause() {
        super.onPause()
        manager?.closeCamera()
        manager?.stopDecodeThread()
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