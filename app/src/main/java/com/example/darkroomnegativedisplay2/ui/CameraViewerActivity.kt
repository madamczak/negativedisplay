package com.example.darkroomnegativedisplay2.ui

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraViewerActivity : ComponentActivity() {

    private lateinit var imageView: ImageView
    private lateinit var cameraExecutor: ExecutorService
    private var brightness = 1.0f // multiplier for brightness adjustment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on, full brightness
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.also {
            it.screenBrightness = 1.0f
        }

        // Full screen immersive
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )

        // Build layout programmatically
        val root = FrameLayout(this)
        root.setBackgroundColor(0xFF000000.toInt())

        imageView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        root.addView(imageView)

        // Label
        val label = TextView(this).apply {
            text = "📷 Negative → Positive Viewer  (tap to close)"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x88000000.toInt())
            setPadding(16, 8, 16, 8)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also { lp ->
                lp.gravity = android.view.Gravity.TOP or android.view.Gravity.START
                lp.topMargin = 32
                lp.leftMargin = 32
            }
        }
        root.addView(label)

        // Brightness seekbar
        val seekLabel = TextView(this).apply {
            text = "Brightness"
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x88000000.toInt())
            setPadding(8, 4, 8, 4)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also { lp ->
                lp.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.START
                lp.bottomMargin = 220
                lp.leftMargin = 32
            }
        }
        root.addView(seekLabel)

        val seekBar = SeekBar(this).apply {
            max = 200
            progress = 100 // default = 1.0x brightness
            layoutParams = FrameLayout.LayoutParams(400, FrameLayout.LayoutParams.WRAP_CONTENT).also { lp ->
                lp.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.START
                lp.bottomMargin = 170
                lp.leftMargin = 32
            }
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                    brightness = progress / 100f
                }
                override fun onStartTrackingTouch(sb: SeekBar) {}
                override fun onStopTrackingTouch(sb: SeekBar) {}
            })
        }
        root.addView(seekBar)

        // Close on tap
        imageView.setOnClickListener { finish() }

        setContentView(root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processFrame(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processFrame(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        imageProxy.close()

        // Apply invert + brightness
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)

        val b = brightness
        // Invert matrix: output = -1*input + 255, then scale brightness
        val invertMatrix = ColorMatrix(floatArrayOf(
            -b,  0f,  0f, 0f, 255f * b,
             0f, -b,  0f, 0f, 255f * b,
             0f,  0f, -b, 0f, 255f * b,
             0f,  0f,  0f, 1f, 0f
        ))

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(invertMatrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        bitmap.recycle()

        runOnUiThread {
            imageView.setImageBitmap(result)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
