package com.xuzhouhhy.qrscan

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.view.WindowManager

/**
 * created by hanhongyun on 2018/11/13 14:19
 *
 */

@Suppress("DEPRECATION")
class CameraConfigurationManager(val context: Context, val camera: Camera) {

    companion object {
        val TAG = CameraConfigurationManager::class.java.simpleName
    }

    val screenResolution: Point by lazy {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return@lazy point
    }

    val cameraResolution: Point by lazy {
        return@lazy CameraConfigUtils.findBestPreviewSizeValue(camera.parameters, screenResolution)
//        val screenResolutionForCamera = Point(screenResolution.x, screenResolution.y)
//        if (screenResolution.x < screenResolution.y) {
//            screenResolutionForCamera.x = screenResolution.y
//            screenResolutionForCamera.y = screenResolution.x
//        }
//         Ensure that the camera resolution is a multiple of 8, as the screen may not be.
//        return@lazy Point(screenResolution.x shr 3 shl 3, screenResolution.y shr 3 shl 3)
    }

    val bestPreviewSize: Point by lazy {
        return@lazy CameraConfigUtils.findBestPreviewSizeValue(camera.parameters, screenResolution)
    }

}
