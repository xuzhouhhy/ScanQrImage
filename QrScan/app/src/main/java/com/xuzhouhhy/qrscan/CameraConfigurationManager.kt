package com.xuzhouhhy.qrscan

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

/**
 * created by hanhongyun on 2018/11/13 14:19
 *
 */

@Suppress("DEPRECATION")
class CameraConfigurationManager(val context: Context) {

    companion object {
        val TAG = CameraConfigurationManager::class.java.simpleName
    }

    val screenResolution: Point by lazy {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        return@lazy Point(display.width, display.height)
    }

    val cameraResolution: Point by lazy {
        val screenResolutionForCamera = Point(screenResolution.x, screenResolution.y)
        if (screenResolution.x < screenResolution.y) {
            screenResolutionForCamera.x = screenResolution.y
            screenResolutionForCamera.y = screenResolution.x
        }
        // Ensure that the camera resolution is a multiple of 8, as the screen may not be.
        return@lazy Point(screenResolution.x shr 3 shl 3, screenResolution.y shr 3 shl 3)
    }

}
