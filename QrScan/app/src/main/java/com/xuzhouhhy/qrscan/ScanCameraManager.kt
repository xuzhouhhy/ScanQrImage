@file:Suppress("DEPRECATION")

package com.xuzhouhhy.qrscan

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera

/**
 * created by hanhongyun on 2018/11/13 14:14
 *
 */
class ScanCameraManager constructor(val context: Context, val camera: Camera) {

    companion object {
        val TAG = ScanCameraManager::class.java.simpleName
    }

    private var cameraConfigurationManager = CameraConfigurationManager(context, camera)

    /**
     *  取景框
     */
    val framingRect: Rect by lazy {
        val screenResolution = cameraConfigurationManager.screenResolution
        val screenResolutionX = screenResolution.x
        val width = (screenResolutionX * 0.6).toInt()
        //水平居中  偏上显示
        val leftOffset = (screenResolution.x - width) / 2
        val topOffset = (screenResolution.y - width) / 5
        return@lazy Rect(leftOffset, topOffset, leftOffset + width, topOffset + width)
    }

    val framingRectInPreview: Rect by lazy {
        val rect = Rect(framingRect)
        val cameraResolution = cameraConfigurationManager.cameraResolution
        val screenResolution = cameraConfigurationManager.screenResolution
        return@lazy Rect(rect.left * cameraResolution.x / screenResolution.x,
                rect.top * cameraResolution.y / screenResolution.y,
                rect.right * cameraResolution.x / screenResolution.x,
                rect.bottom * cameraResolution.y / screenResolution.y)
    }

    val cameraResolution
        get() = cameraConfigurationManager.cameraResolution

    val bestPreviewSize
        get() = cameraConfigurationManager.bestPreviewSize
}