package com.example.darkroomnegativedisplay2.ui

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.darkroomnegativedisplay2.data.PhotoRepository
import com.example.darkroomnegativedisplay2.ui.theme.DarkroomNegativeDisplay2Theme
import com.example.darkroomnegativedisplay2.utils.ImageProcessor
import com.example.darkroomnegativedisplay2.utils.ScreenController
import com.example.darkroomnegativedisplay2.utils.SoundManager
import kotlinx.coroutines.delay

class FullscreenDisplayActivity : ComponentActivity() {

    companion object {
        var sharedPhotoRepository: PhotoRepository? = null
    }

    private lateinit var imageProcessor: ImageProcessor
    private lateinit var soundManager: SoundManager
    private lateinit var screenController: ScreenController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize utilities
        imageProcessor = ImageProcessor()
        soundManager = SoundManager(this)
        screenController = ScreenController(this)

        // Get intent extras
        val mode = intent.getStringExtra("mode") ?: "display_negative"
        val xSeconds = intent.getIntExtra("x_seconds", 2)
        val ySeconds = intent.getIntExtra("y_seconds", 10)
        val zSeconds = intent.getIntExtra("z_seconds", 2)
        val aParts = intent.getIntExtra("a_parts", 10)
        val photoIndex = intent.getIntExtra("photo_index", -1)
        val useDeviceBrightness = intent.getBooleanExtra("use_device_brightness", true)

        // Setup fullscreen mode with brightness preference
        screenController.setupFullscreenDisplay(useDeviceBrightness)

        setContent {
            DarkroomNegativeDisplay2Theme {
                FullscreenDisplay(
                    mode = mode,
                    xSeconds = xSeconds,
                    ySeconds = ySeconds,
                    zSeconds = zSeconds,
                    aParts = aParts,
                    photoIndex = photoIndex,
                    onFinish = { finish() }
                )
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

@Composable
fun FullscreenDisplay(
    mode: String,
    xSeconds: Int,
    ySeconds: Int,
    zSeconds: Int,
    aParts: Int,
    photoIndex: Int,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isDisplaying by remember { mutableStateOf(false) }

    // Use shared components
    val photoRepository = FullscreenDisplayActivity.sharedPhotoRepository
    val imageProcessor = remember { ImageProcessor() }
    val soundManager = remember { SoundManager(context) }

        LaunchedEffect(Unit) {
            when (mode) {
                "display_negative" -> {
                    executeDisplayNegativeSequence(
                        photoRepository = photoRepository,
                        soundManager = soundManager,
                        photoIndex = photoIndex,
                        xSeconds = xSeconds,
                        ySeconds = ySeconds,
                        zSeconds = zSeconds,
                        onBitmapChange = { currentBitmap = it },
                        onDisplayChange = { isDisplaying = it },
                        onFinish = onFinish
                    )
                }
                "test_irradiation" -> {
                    executeTestIrradiationSequence(
                        photoRepository = photoRepository,
                        imageProcessor = imageProcessor,
                        soundManager = soundManager,
                        photoIndex = photoIndex,
                        xSeconds = xSeconds,
                        zSeconds = zSeconds,
                        aParts = aParts,
                        onBitmapChange = { currentBitmap = it },
                        onDisplayChange = { isDisplaying = it },
                        onFinish = onFinish
                    )
                }
                "multi_copy_test" -> {
                    executeMultiCopyTestSequence(
                        photoRepository = photoRepository,
                        imageProcessor = imageProcessor,
                        soundManager = soundManager,
                        photoIndex = photoIndex,
                        xSeconds = xSeconds,
                        zSeconds = zSeconds,
                        aParts = aParts,
                        onBitmapChange = { currentBitmap = it },
                        onDisplayChange = { isDisplaying = it },
                        onFinish = onFinish
                    )
                }
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Ensure all margins/areas are black
        contentAlignment = Alignment.Center
    ) {
        currentBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit // This ensures the image fits within bounds without stretching
            )
        }
        // When currentBitmap is null, the Box will show pure black background
    }
}

private suspend fun executeDisplayNegativeSequence(
    photoRepository: PhotoRepository?,
    soundManager: SoundManager,
    photoIndex: Int,
    xSeconds: Int,
    ySeconds: Int,
    zSeconds: Int,
    onBitmapChange: (Bitmap?) -> Unit,
    onDisplayChange: (Boolean) -> Unit,
    onFinish: () -> Unit
) {
    try {
        // Get the negative bitmap from current photo
        val currentPhoto = photoRepository?.getCurrentPhoto()
        val negativeBitmap = currentPhoto?.negativeBitmap

        if (negativeBitmap == null) {
            onFinish()
            return
        }

        // Phase 1: Pre-display black (X seconds)
        onBitmapChange(null)
        delay(xSeconds * 1000L)

        // Phase 2: Display negative (Y seconds)
        soundManager.playStartSound()
        onBitmapChange(negativeBitmap)
        onDisplayChange(true)
        delay(ySeconds * 1000L)
        soundManager.playEndSound()
        onDisplayChange(false)

        // Phase 3: Post-display black (Z seconds)
        onBitmapChange(null)
        delay(zSeconds * 1000L)

        onFinish()
    } catch (e: Exception) {
        e.printStackTrace()
        onFinish()
    }
}

private suspend fun executeTestIrradiationSequence(
    photoRepository: PhotoRepository?,
    imageProcessor: ImageProcessor,
    soundManager: SoundManager,
    photoIndex: Int,
    xSeconds: Int,
    zSeconds: Int,
    aParts: Int,
    onBitmapChange: (Bitmap?) -> Unit,
    onDisplayChange: (Boolean) -> Unit,
    onFinish: () -> Unit
) {
    try {
        // Get the negative bitmap and create test irradiation bitmaps
        val currentPhoto = photoRepository?.getCurrentPhoto()
        val negativeBitmap = currentPhoto?.negativeBitmap

        if (negativeBitmap == null) {
            onFinish()
            return
        }

        val testBitmaps = imageProcessor.createTestIrradiationBitmaps(negativeBitmap, aParts)

        if (testBitmaps.isEmpty()) {
            onFinish()
            return
        }

        // Phase 1: Pre-display black (X seconds)
        onBitmapChange(null)
        delay(xSeconds * 1000L)

        // Phase 2: Progressive display (A seconds total)
        soundManager.playStartSound()
        onDisplayChange(true)

        for (bitmap in testBitmaps) {
            onBitmapChange(bitmap)
            delay(1000L) // 1 second per part
        }

        soundManager.playEndSound()
        onDisplayChange(false)

        // Phase 3: Post-display black (Z seconds)
        onBitmapChange(null)
        delay(zSeconds * 1000L)

        onFinish()
    } catch (e: Exception) {
        e.printStackTrace()
        onFinish()
    }
}

private suspend fun executeMultiCopyTestSequence(
    photoRepository: PhotoRepository?,
    imageProcessor: ImageProcessor,
    soundManager: SoundManager,
    photoIndex: Int,
    xSeconds: Int,
    zSeconds: Int,
    aParts: Int,
    onBitmapChange: (Bitmap?) -> Unit,
    onDisplayChange: (Boolean) -> Unit,
    onFinish: () -> Unit
) {
    try {
        // Get the negative bitmap and create multi-copy test bitmaps
        val currentPhoto = photoRepository?.getCurrentPhoto()
        val negativeBitmap = currentPhoto?.negativeBitmap

        if (negativeBitmap == null) {
            onFinish()
            return
        }

        val testBitmaps = imageProcessor.createMultiCopyTestBitmaps(negativeBitmap, aParts)

        if (testBitmaps.isEmpty()) {
            onFinish()
            return
        }

        // Phase 1: Pre-display black (X seconds)
        onBitmapChange(null)
        delay(xSeconds * 1000L)

        // Phase 2: Progressive multi-copy display (A seconds total)
        soundManager.playStartSound()
        onDisplayChange(true)

        for (bitmap in testBitmaps) {
            onBitmapChange(bitmap)
            delay(1000L) // 1 second per step
        }

        soundManager.playEndSound()
        onDisplayChange(false)

        // Phase 3: Post-display black (Z seconds)
        onBitmapChange(null)
        delay(zSeconds * 1000L)

        onFinish()
    } catch (e: Exception) {
        e.printStackTrace()
        onFinish()
    }
}
