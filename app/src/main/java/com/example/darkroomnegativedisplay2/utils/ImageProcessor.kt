package com.example.darkroomnegativedisplay2.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Utility class for image processing operations specific to darkroom photography
 */
class ImageProcessor {

    /**
     * Converts a bitmap to its negative by inverting RGB values
     * Formula: newR = 255 - oldR, newG = 255 - oldG, newB = 255 - oldB
     */
    fun convertToNegative(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val negativeBitmap = Bitmap.createBitmap(width, height, config)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = 255 - Color.red(pixel)
            val green = 255 - Color.green(pixel)
            val blue = 255 - Color.blue(pixel)

            pixels[i] = Color.argb(alpha, red, green, blue)
        }

        negativeBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return negativeBitmap
    }

    /**
     * Creates progressive reveal bitmaps for test irradiation
     * Divides image width into A parts and creates A bitmaps showing progressive reveals
     * @param bitmap The negative bitmap to process
     * @param parts Number of test parts (A value)
     * @return List of bitmaps showing progressive reveals from left to right
     */
    fun createTestIrradiationBitmaps(bitmap: Bitmap, parts: Int): List<Bitmap> {
        if (parts <= 0) return emptyList()

        val width = bitmap.width
        val height = bitmap.height
        val partWidth = width / parts
        val testBitmaps = mutableListOf<Bitmap>()

        for (i in 1..parts) {
            val config = bitmap.config ?: Bitmap.Config.ARGB_8888
            val testBitmap = Bitmap.createBitmap(width, height, config)
            val canvas = Canvas(testBitmap)

            // Fill entire bitmap with black first
            canvas.drawColor(Color.BLACK)

            // Calculate how much to reveal (leftmost i parts)
            val revealWidth = i * partWidth

            // Create a cropped version of the original that shows only the left portion
            val srcRect = android.graphics.Rect(0, 0, revealWidth, height)
            val destRect = android.graphics.Rect(0, 0, revealWidth, height)

            canvas.drawBitmap(bitmap, srcRect, destRect, null)

            testBitmaps.add(testBitmap)
        }

        return testBitmaps
    }

    /**
     * Efficiently loads and scales a bitmap from URI to prevent memory issues
     * Automatically rotates vertical images to horizontal orientation for darkroom use
     * @param uri The image URI to load
     * @param maxWidth Maximum width for the loaded bitmap
     * @param maxHeight Maximum height for the loaded bitmap
     */
    fun loadScaledBitmap(
        context: android.content.Context,
        uri: android.net.Uri,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = android.graphics.BitmapFactory.Options()

            // First pass - just get dimensions
            options.inJustDecodeBounds = true
            android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

            // Second pass - actually decode the bitmap
            options.inJustDecodeBounds = false
            val secondInputStream = context.contentResolver.openInputStream(uri)
            var bitmap = android.graphics.BitmapFactory.decodeStream(secondInputStream, null, options)
            secondInputStream?.close()

            // Ensure all images are horizontal (rotate vertical images)
            bitmap?.let { ensureHorizontalOrientation(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(
        options: android.graphics.BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Creates a red-tinted version of a bitmap for UI preview purposes
     * This applies a red overlay to simulate the red interface theme on images
     */
    fun applyRedTint(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val redTintedBitmap = Bitmap.createBitmap(width, height, config)

        val canvas = Canvas(redTintedBitmap)
        val paint = Paint()

        // Draw original bitmap
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // Apply red tint overlay
        paint.color = Color.argb(80, 255, 0, 0) // Semi-transparent red
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.MULTIPLY)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        return redTintedBitmap
    }

    /**
     * Ensures bitmap is in horizontal orientation for darkroom use
     * If the image is vertical (height > width), rotates it 90 degrees clockwise
     */
    fun ensureHorizontalOrientation(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // If image is already horizontal, return as is
        if (width >= height) {
            return bitmap
        }

        // Image is vertical, rotate 90 degrees clockwise to make it horizontal
        val matrix = android.graphics.Matrix()
        matrix.postRotate(90f)

        return try {
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, width, height, matrix, true
            )
            // Recycle original bitmap to free memory
            if (bitmap != rotatedBitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            rotatedBitmap
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            // Return original if rotation fails due to memory
            bitmap
        }
    }

    /**
     * Creates multiple small copies of a bitmap arranged to fit the screen
     * Used for multi-copy test irradiation where each copy gets different exposure times
     * @param bitmap The negative bitmap to process
     * @param copies Number of copies to create (default 10)
     * @return List of bitmaps, each containing all small copies but with progressive masking
     */
    fun createMultiCopyTestBitmaps(bitmap: Bitmap, copies: Int = 10): List<Bitmap> {
        if (copies <= 0) return emptyList()

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // Add spacing between copies (in pixels)
        val spacing = 8 // 8 pixels separation between each copy

        // Calculate grid layout (try to make it as square as possible)
        val cols = kotlin.math.ceil(kotlin.math.sqrt(copies.toDouble())).toInt()
        val rows = kotlin.math.ceil(copies.toDouble() / cols).toInt()

        // Calculate size of each small copy to fit screen with spacing
        val copyWidth = (originalWidth - (spacing * (cols - 1))) / cols
        val copyHeight = (originalHeight - (spacing * (rows - 1))) / rows

        val testBitmaps = mutableListOf<Bitmap>()

        // Create scaled down version of the original bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, copyWidth, copyHeight, true)

        for (i in 1..copies) {
            val config = bitmap.config ?: Bitmap.Config.ARGB_8888
            val testBitmap = Bitmap.createBitmap(originalWidth, originalHeight, config)
            val canvas = Canvas(testBitmap)

            // Fill with black background
            canvas.drawColor(Color.BLACK)

            // Draw the small copies in a grid with spacing, but only show the first i copies
            var copyIndex = 0
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    if (copyIndex < i && copyIndex < copies) {
                        val x = col * (copyWidth + spacing)
                        val y = row * (copyHeight + spacing)
                        canvas.drawBitmap(scaledBitmap, x.toFloat(), y.toFloat(), null)
                    }
                    copyIndex++
                    if (copyIndex >= copies) break
                }
                if (copyIndex >= copies) break
            }

            testBitmaps.add(testBitmap)
        }

        // Clean up scaled bitmap
        if (scaledBitmap != bitmap && !scaledBitmap.isRecycled) {
            scaledBitmap.recycle()
        }

        return testBitmaps
    }
}
