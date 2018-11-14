@file:Suppress("DEPRECATION")

package com.xuzhouhhy.qrscan

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.view.WindowManager

/**
 * created by hanhongyun on 2018/11/13 14:19
 *
 */

class CameraConfigurationManager(val context: Context, private val camera: Camera) {

    val screenResolution: Point by lazy {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return@lazy point
    }

    val cameraResolution: Point by lazy {
        return@lazy CameraConfigUtils.findBestPreviewSizeValue(camera.parameters, screenResolution)
    }

    val bestPreviewSize: Point by lazy {
        return@lazy CameraConfigUtils.findBestPreviewSizeValue(camera.parameters, screenResolution)
    }

    val bestPreviewSizeOnScreen: Point by lazy {
        val defaultSize = bestPreviewSize
        val isScreenPortrait = screenResolution.x < screenResolution.y
        val isPreviewSizePortrait = defaultSize.x < defaultSize.y
        return@lazy if (isScreenPortrait == isPreviewSizePortrait) {
            defaultSize
        } else {
            Point(defaultSize.y, defaultSize.x)
        }
    }

}
