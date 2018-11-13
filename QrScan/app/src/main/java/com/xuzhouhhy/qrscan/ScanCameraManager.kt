@file:Suppress("DEPRECATION")

package com.xuzhouhhy.qrscan

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera

/**
 * created by hanhongyun on 2018/11/13 14:14
 *
 */
class ScanCameraManager(val context: Context) {

    companion object {
        val TAG = ScanCameraManager::class.java.simpleName
    }

    var camera: Camera? = null

    var cameraConfigurationManager = CameraConfigurationManager(context)

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
        val ratio = cameraResolution.y / screenResolution.x
        return@lazy Rect(rect.left * ratio, rect.top * ratio, rect.right * ratio, rect.bottom * ratio)
    }

}