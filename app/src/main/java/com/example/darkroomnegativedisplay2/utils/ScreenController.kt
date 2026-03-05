package com.example.darkroomnegativedisplay2.utils

import android.app.Activity
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Utility class for managing screen brightness and fullscreen mode
 * Specifically designed for darkroom photography requirements
 */
class ScreenController(private val activity: Activity) {

    private var originalBrightness: Float = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

    /**
     * Sets up fullscreen mode with configurable brightness
     * - Hides all system UI (status bar, navigation bar)
     * - Uses device brightness or maximum based on setting
     * - Keeps screen on during operation
     * - Ensures black background for margins
     */
    fun setupFullscreenDisplay(useDeviceBrightness: Boolean = true) {
        // Store original brightness setting
        originalBrightness = activity.window.attributes.screenBrightness

        // Hide system UI completely
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        val windowInsetsController = WindowCompat.getInsetsController(
            activity.window,
            activity.window.decorView
        )
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Keep screen on
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Ensure black background for window
        activity.window.decorView.setBackgroundColor(android.graphics.Color.BLACK)
        activity.window.statusBarColor = android.graphics.Color.BLACK
        activity.window.navigationBarColor = android.graphics.Color.BLACK

        // Set brightness based on user preference
        if (useDeviceBrightness) {
            preserveDeviceBrightness()
        } else {
            setMaximumBrightness()
        }
    }


    /**
     * Preserves the device's current brightness setting
     * Uses BRIGHTNESS_OVERRIDE_NONE to respect system brightness
     */
    private fun preserveDeviceBrightness() {
        val layoutParams = activity.window.attributes
        // Use BRIGHTNESS_OVERRIDE_NONE to respect device brightness settings
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.window.attributes = layoutParams
    }

    /**
     * Sets screen brightness to maximum (1.0f) - only use if needed
     * This method is kept for compatibility but not used by default
     */
    fun setMaximumBrightness() {
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = 1.0f
        activity.window.attributes = layoutParams
    }

    /**
     * Restores normal screen behavior
     * - Shows system UI
     * - Restores original brightness setting
     * - Removes keep screen on flag
     */
    fun restoreNormalDisplay() {
        // Show system UI
        WindowCompat.setDecorFitsSystemWindows(activity.window, true)
        val windowInsetsController = WindowCompat.getInsetsController(
            activity.window,
            activity.window.decorView
        )
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

        // Restore original brightness
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = originalBrightness
        activity.window.attributes = layoutParams

        // Remove keep screen on flag
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Simulates "turning off" the screen by displaying pure black with minimum brightness
     * Note: Android doesn't allow truly turning off the screen programmatically
     */
    fun simulateScreenOff() {
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = 0.01f  // Minimum brightness
        activity.window.attributes = layoutParams
    }
}
