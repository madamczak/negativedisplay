package com.example.darkroomnegativedisplay2.utils

import android.content.Context
import android.media.ToneGenerator
import android.media.AudioManager

/**
 * Manager for audio feedback during exposure sequences
 */
class SoundManager(
    private val context: Context
) {

    private var toneGenerator: ToneGenerator? = null

    init {
        initializeToneGenerator()
    }

    private fun initializeToneGenerator() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            toneGenerator = null
        }
    }

    /**
     * Play sound when irradiation/exposure starts
     */
    fun playStartSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Play sound when irradiation/exposure ends
     */
    fun playEndSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Release resources
     */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
