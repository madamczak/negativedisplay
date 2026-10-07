package com.example.darkroomnegativedisplay2.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.darkroomnegativedisplay2.data.PhotoRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Gum bichromate mode: long exposure in minutes (15 min steps),
 * no pre/post black, no test strips, normal (non-red) UI, maximum brightness.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GumScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel { MainViewModel(context) }
    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        FullscreenDisplayActivity.sharedPhotoRepository = PhotoRepository.getInstance()
    }

    val prefs = remember { context.getSharedPreferences("gum_mode", Context.MODE_PRIVATE) }
    var minutes by remember { mutableIntStateOf(prefs.getInt("minutes", 60)) }
    var blackMinutes by remember { mutableIntStateOf(prefs.getInt("black_minutes", 0)) }
    val scalePrefs = remember { context.getSharedPreferences("image_scale", Context.MODE_PRIVATE) }
    var scalePercent by remember { mutableIntStateOf(scalePrefs.getInt("gum", 100)) }

    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.loadPhotos(uris)
    }

    val photos by viewModel.photos.collectAsState()
    val index by viewModel.currentPhotoIndex.collectAsState()
    val current = photos.getOrNull(index)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Modes") }
            Text("Gum Bichromate", style = MaterialTheme.typography.headlineSmall)
        }

        Card(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val bmp = current?.negativeBitmap ?: current?.bitmap
                if (bmp != null) {
                    Image(bmp.asImageBitmap(), "Photo", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else Text("No photo loaded")
            }
        }

        if (photos.size > 1) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { viewModel.navigatePrevious() }, enabled = index > 0) { Text("Previous") }
                Text("Photo ${index + 1} of ${photos.size}")
                Button(onClick = { viewModel.navigateNext() }, enabled = index < photos.size - 1) { Text("Next") }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (storagePermission.status.isGranted) picker.launch("image/*")
                    else storagePermission.launchPermissionRequest()
                },
                modifier = Modifier.weight(1f)
            ) { Text("Load Photos") }
            Button(
                onClick = { viewModel.convertCurrentToNegative() },
                enabled = current != null,
                modifier = Modifier.weight(1f)
            ) { Text("Convert to Negative") }
        }

        Text("Exposure time", style = MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                minutes = (minutes - 15).coerceAtLeast(15)
                prefs.edit().putInt("minutes", minutes).apply()
            }) { Text("−15 min") }
            Text(formatMinutes(minutes), style = MaterialTheme.typography.headlineSmall)
            Button(onClick = {
                minutes = (minutes + 15).coerceAtMost(600)
                prefs.edit().putInt("minutes", minutes).apply()
            }) { Text("+15 min") }
        }

        Text("Black screen after exposure", style = MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                blackMinutes = (blackMinutes - 15).coerceAtLeast(0)
                prefs.edit().putInt("black_minutes", blackMinutes).apply()
            }) { Text("−15 min") }
            Text(
                if (blackMinutes == 0) "0 min" else formatMinutes(blackMinutes),
                style = MaterialTheme.typography.headlineSmall
            )
            Button(onClick = {
                blackMinutes = (blackMinutes + 15).coerceAtMost(600)
                prefs.edit().putInt("black_minutes", blackMinutes).apply()
            }) { Text("+15 min") }
        }
        Text("Tap the screen 5 times to exit.", style = MaterialTheme.typography.bodySmall)

        Text("Image size: $scalePercent%", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = scalePercent.toFloat(),
            onValueChange = {
                scalePercent = it.toInt()
                scalePrefs.edit().putInt("gum", scalePercent).apply()
            },
            valueRange = 10f..100f
        )

        Button(
            onClick = {
                val intent = Intent(context, FullscreenDisplayActivity::class.java).apply {
                    putExtra("scale_percent", scalePercent)
                    putExtra("mode", "display_negative")
                    putExtra("x_seconds", 0)
                    putExtra("y_seconds", minutes * 60)
                    putExtra("z_seconds", blackMinutes * 60)
                    putExtra("forced_brightness", 1.0f)
                    putExtra("exit_on_five_taps", true)
                    putExtra("photo_index", index)
                }
                context.startActivity(intent)
            },
            enabled = current?.isNegativeConverted == true,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) { Text("Display Negative") }
    }
}

private fun formatMinutes(m: Int): String {
    val h = m / 60
    val r = m % 60
    return when {
        h == 0 -> "$r min"
        r == 0 -> "$h h"
        else -> "$h h $r min"
    }
}
