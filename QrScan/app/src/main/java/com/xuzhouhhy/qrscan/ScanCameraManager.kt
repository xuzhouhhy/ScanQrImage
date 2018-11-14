@file:Suppress("DEPRECATION")

package com.xuzhouhhy.qrscan

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera

/**
 * created by hanhongyun on 2018/11/13 14:14
 *
 */
class ScanCameraManager constructor(val context: Context, camera: Camera) {

    companion object {
        val TAG = ScanCameraManager::class.java.simpleName
        private const val MIN_FRAME_WIDTH = 240
        private const val MIN_FRAME_HEIGHT = 240
        private const val MAX_FRAME_WIDTH = 1200 // = 5/8 * 1920
        private const val MAX_FRAME_HEIGHT = 675 // = 5/8 * 1080
    }

    private var cameraConfigurationManager = CameraConfigurationManager(context, camera)

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

    val cameraResolution
        get() = cameraConfigurationManager.cameraResolution

    val bestPreviewSize
        get() = cameraConfigurationManager.bestPreviewSize
}