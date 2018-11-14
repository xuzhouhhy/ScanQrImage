package com.xuzhouhhy.qrscan

import android.graphics.Point
import android.hardware.Camera
import android.util.Log

/**
 * created by hanhongyun on 2018/11/13 19:00
 *
 */
@Suppress("DEPRECATION")
class CameraConfigUtils {

    companion object {

        val TAG = CameraConfigUtils::class.java.simpleName

        private const val MIN_PREVIEW_PIXELS = 480 * 320 // normal screen
        private const val MAX_ASPECT_DISTORTION = 0.15

        fun findBestPreviewSizeValue(parameters: Camera.Parameters, screenResolution: Point): Point {

            val rawSupportedSizes = parameters.supportedPreviewSizes
            if (rawSupportedSizes == null) {
                Log.w(TAG, "Device returned no supported preview sizes; using default")
                val defaultSize = parameters.previewSize
                        ?: throw IllegalStateException("Parameters contained no preview size!")
                return Point(defaultSize.width, defaultSize.height)
            }

            if (Log.isLoggable(TAG, Log.INFO)) {
                val previewSizesString = StringBuilder()
                for (size in rawSupportedSizes) {
                    previewSizesString.append(size.width).append('x').append(size.height).append(' ')
                }
                Log.i(TAG, "Supported preview sizes: $previewSizesString")
            }

            val screenAspectRatio = screenResolution.x / screenResolution.y.toDouble()

            // Find a suitable size, with max resolution
            var maxResolution = 0
            var maxResPreviewSize: Camera.Size? = null
            for (size in rawSupportedSizes) {
                val realWidth = size.width
                val realHeight = size.height
                val resolution = realWidth * realHeight
                if (resolution < MIN_PREVIEW_PIXELS) {
                    continue
                }

                val isCandidatePortrait = realWidth < realHeight
                val maybeFlippedWidth = if (isCandidatePortrait) realHeight else realWidth
                val maybeFlippedHeight = if (isCandidatePortrait) realWidth else realHeight
                val aspectRatio = maybeFlippedWidth / maybeFlippedHeight.toDouble()
                val distortion = Math.abs(aspectRatio - screenAspectRatio)
                if (distortion > MAX_ASPECT_DISTORTION) {
                    continue
                }

                if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
                    val exactPoint = Point(realWidth, realHeight)
                    Log.i(TAG, "Found preview size exactly matching screen size: $exactPoint")
                    return exactPoint
                }

                // Resolution is suitable; record the one with max resolution
                if (resolution > maxResolution) {
                    maxResolution = resolution
                    maxResPreviewSize = size
                }
            }

            // If no exact match, use largest preview size. This was not a great idea on older devices because
            // of the additional computation needed. We're likely to get here on newer Android 4+ devices, where
            // the CPU is much more powerful.
            if (maxResPreviewSize != null) {
                val largestSize = Point(maxResPreviewSize.width, maxResPreviewSize.height)
                Log.i(TAG, "Using largest suitable preview size: $largestSize")
                return largestSize
            }

            // If there is nothing at all suitable, return current preview size
            val defaultPreview = parameters.previewSize
                    ?: throw IllegalStateException("Parameters contained no preview size!")
            val defaultSize = Point(defaultPreview.width, defaultPreview.height)
            Log.i(TAG, "No suitable preview sizes, using default: $defaultSize")
            return defaultSize
        }

        fun findDesiredDimensionInRange(resolution: Int, hardMin: Int, hardMax: Int): Int {
            val dim = 5 * resolution / 8 // Target 5/8 of each dimension
            if (dim < hardMin) {
                return hardMin
            }
            return if (dim > hardMax) {
                hardMax
            } else dim
        }
    }

}