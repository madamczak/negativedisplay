package com.example.darkroomnegativedisplay2.data

import android.graphics.Bitmap
import android.net.Uri

/**
 * Data model for a photo in the darkroom app
 */
data class PhotoModel(
    val uri: Uri,
    val bitmap: Bitmap? = null,
    val negativeBitmap: Bitmap? = null,
    val isNegativeConverted: Boolean = false
)

/**
 * Settings for the darkroom display timing and interface
 */
data class AppSettings(
    val preDisplayBlackSeconds: Int = 2,        // X value
    val displayDurationSeconds: Int = 10,       // Y value
    val postDisplayBlackSeconds: Int = 2,       // Z value
    val testIrradiationParts: Int = 10,        // A value
    val isInterfaceRed: Boolean = false,
    val useDeviceBrightness: Boolean = true     // Use device brightness instead of maximum
)
