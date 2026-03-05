package com.example.darkroomnegativedisplay2

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.darkroomnegativedisplay2.data.AppSettings
import com.example.darkroomnegativedisplay2.data.PhotoRepository
import com.example.darkroomnegativedisplay2.ui.FullscreenDisplayActivity
import com.example.darkroomnegativedisplay2.ui.MainViewModel
import com.example.darkroomnegativedisplay2.ui.theme.DarkroomNegativeDisplay2Theme
import com.example.darkroomnegativedisplay2.utils.ImageProcessor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DarkroomNegativeDisplay2Theme {
                DarkroomApp()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DarkroomApp() {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel { MainViewModel(context) }

    // Set shared reference for FullscreenDisplayActivity
    LaunchedEffect(Unit) {
        FullscreenDisplayActivity.sharedPhotoRepository = PhotoRepository.getInstance()
    }

    // Permission handling
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.loadPhotos(uris)
        }
    }

    // Collect states
    val photos by viewModel.photos.collectAsState()
    val currentPhotoIndex by viewModel.currentPhotoIndex.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState(initial = AppSettings())
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingMessage by viewModel.loadingMessage.collectAsState()
    val isDisplayNegativeEnabled by viewModel.isDisplayNegativeEnabled.collectAsState(initial = false)

    // Help dialog state
    var showHelpDialog by remember { mutableStateOf(false) }

    // Red theme colors
    val backgroundColor = if (appSettings.isInterfaceRed) Color.Red else MaterialTheme.colorScheme.background
    val contentColor = if (appSettings.isInterfaceRed) Color.Black else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title with help button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Darkroom Negative Display",
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { showHelpDialog = true }
            ) {
                Text(
                    "?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = contentColor
                )
            }
        }

        // Photo preview section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = if (appSettings.isInterfaceRed)
                CardDefaults.cardColors(containerColor = Color.DarkGray)
            else CardDefaults.cardColors()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (photos.isNotEmpty() && currentPhotoIndex in photos.indices) {
                    val currentPhoto = photos[currentPhotoIndex]
                    var displayBitmap = currentPhoto.negativeBitmap ?: currentPhoto.bitmap

                    displayBitmap?.let { bitmap ->
                        // Apply red tint for UI preview when red interface is enabled
                        val finalBitmap = if (appSettings.isInterfaceRed) {
                            remember(bitmap, appSettings.isInterfaceRed) {
                                ImageProcessor().applyRedTint(bitmap)
                            }
                        } else bitmap

                        Image(
                            bitmap = finalBitmap.asImageBitmap(),
                            contentDescription = "Current Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Text(
                        "No photo loaded",
                        color = if (appSettings.isInterfaceRed) Color.Black else Color.Gray
                    )
                }
            }
        }

        // Photo navigation
        if (photos.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.navigatePrevious() },
                    enabled = currentPhotoIndex > 0,
                    colors = if (appSettings.isInterfaceRed)
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B0000),
                            contentColor = Color.Black
                        )
                    else ButtonDefaults.buttonColors()
                ) {
                    Text("Previous")
                }

                Text(
                    text = "Photo ${currentPhotoIndex + 1} of ${photos.size}",
                    color = contentColor
                )

                Button(
                    onClick = { viewModel.navigateNext() },
                    enabled = currentPhotoIndex < photos.size - 1,
                    colors = if (appSettings.isInterfaceRed)
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B0000),
                            contentColor = Color.Black
                        )
                    else ButtonDefaults.buttonColors()
                ) {
                    Text("Next")
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (storagePermission.status.isGranted) {
                        photoPickerLauncher.launch("image/*")
                    } else {
                        storagePermission.launchPermissionRequest()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = if (appSettings.isInterfaceRed)
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B0000),
                        contentColor = Color.Black
                    )
                else ButtonDefaults.buttonColors()
            ) {
                Text("Load Photos")
            }

            Button(
                onClick = { viewModel.convertCurrentToNegative() },
                enabled = photos.isNotEmpty(),
                modifier = Modifier.weight(1f),
                colors = if (appSettings.isInterfaceRed)
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B0000),
                        contentColor = Color.Black
                    )
                else ButtonDefaults.buttonColors()
            ) {
                Text("Convert to Negative")
            }
        }

        // Interface toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Red Interface (Safety Mode)", color = contentColor)
            Switch(
                checked = appSettings.isInterfaceRed,
                onCheckedChange = { viewModel.toggleInterfaceRed(it) },
                colors = if (appSettings.isInterfaceRed)
                    SwitchDefaults.colors(checkedThumbColor = Color.Black)
                else SwitchDefaults.colors()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Use Device Brightness", color = contentColor)
            Switch(
                checked = appSettings.useDeviceBrightness,
                onCheckedChange = { viewModel.updateDeviceBrightness(it) },
                colors = if (appSettings.isInterfaceRed)
                    SwitchDefaults.colors(checkedThumbColor = Color.Black)
                else SwitchDefaults.colors()
            )
        }

        // Settings section
        Text(
            text = "Display Settings",
            style = MaterialTheme.typography.titleMedium,
            color = contentColor
        )

        // Settings inputs
        SettingInput(
            label = "Pre-display black (X seconds)",
            value = appSettings.preDisplayBlackSeconds.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { seconds ->
                    viewModel.updatePreDisplayBlackSeconds(seconds)
                }
            },
            isRed = appSettings.isInterfaceRed
        )

        SettingInput(
            label = "Display duration (Y seconds)",
            value = appSettings.displayDurationSeconds.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { seconds ->
                    viewModel.updateDisplayDurationSeconds(seconds)
                }
            },
            isRed = appSettings.isInterfaceRed
        )

        SettingInput(
            label = "Post-display black (Z seconds)",
            value = appSettings.postDisplayBlackSeconds.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { seconds ->
                    viewModel.updatePostDisplayBlackSeconds(seconds)
                }
            },
            isRed = appSettings.isInterfaceRed
        )

        SettingInput(
            label = "Test irradiation parts (A)",
            value = appSettings.testIrradiationParts.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { parts ->
                    viewModel.updateTestIrradiationParts(parts)
                }
            },
            isRed = appSettings.isInterfaceRed
        )

        // Display buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, FullscreenDisplayActivity::class.java).apply {
                            putExtra("mode", "display_negative")
                            putExtra("x_seconds", appSettings.preDisplayBlackSeconds)
                            putExtra("y_seconds", appSettings.displayDurationSeconds)
                            putExtra("z_seconds", appSettings.postDisplayBlackSeconds)
                            putExtra("use_device_brightness", appSettings.useDeviceBrightness)
                            if (photos.isNotEmpty() && currentPhotoIndex in photos.indices) {
                                putExtra("photo_index", currentPhotoIndex)
                            }
                        }
                        context.startActivity(intent)
                    },
                    enabled = isDisplayNegativeEnabled,
                    modifier = Modifier.weight(1f),
                    colors = if (appSettings.isInterfaceRed)
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B0000),
                            contentColor = Color.Black
                        )
                    else ButtonDefaults.buttonColors()
                ) {
                    Text("Display Negative")
                }

                Button(
                    onClick = {
                        val intent = Intent(context, FullscreenDisplayActivity::class.java).apply {
                            putExtra("mode", "test_irradiation")
                            putExtra("x_seconds", appSettings.preDisplayBlackSeconds)
                            putExtra("z_seconds", appSettings.postDisplayBlackSeconds)
                            putExtra("a_parts", appSettings.testIrradiationParts)
                            putExtra("use_device_brightness", appSettings.useDeviceBrightness)
                            if (photos.isNotEmpty() && currentPhotoIndex in photos.indices) {
                                putExtra("photo_index", currentPhotoIndex)
                            }
                        }
                        context.startActivity(intent)
                    },
                    enabled = isDisplayNegativeEnabled,
                    modifier = Modifier.weight(1f),
                    colors = if (appSettings.isInterfaceRed)
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B0000),
                            contentColor = Color.Black
                        )
                    else ButtonDefaults.buttonColors()
                ) {
                    Text("Test Strips")
                }
            }

            Button(
                onClick = {
                    val intent = Intent(context, FullscreenDisplayActivity::class.java).apply {
                        putExtra("mode", "multi_copy_test")
                        putExtra("x_seconds", appSettings.preDisplayBlackSeconds)
                        putExtra("z_seconds", appSettings.postDisplayBlackSeconds)
                        putExtra("a_parts", appSettings.testIrradiationParts)
                        putExtra("use_device_brightness", appSettings.useDeviceBrightness)
                        if (photos.isNotEmpty() && currentPhotoIndex in photos.indices) {
                            putExtra("photo_index", currentPhotoIndex)
                        }
                    }
                    context.startActivity(intent)
                },
                enabled = isDisplayNegativeEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = if (appSettings.isInterfaceRed)
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B0000),
                        contentColor = Color.Black
                    )
                else ButtonDefaults.buttonColors()
            ) {
                Text("Multi-Copy Test")
            }
        }

        // Loading/message display
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = if (appSettings.isInterfaceRed) Color.Black else MaterialTheme.colorScheme.primary
            )
        }

        if (loadingMessage.isNotEmpty()) {
            Text(
                text = loadingMessage,
                color = contentColor,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Safety warning if interface is not red
        if (!appSettings.isInterfaceRed && photos.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE082))
            ) {
                Text(
                    text = "⚠️ Red interface must be enabled before displaying negatives. This is a safety feature to prevent accidental paper exposure.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B4513)
                )
            }
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    "How to Use",
                    color = if (appSettings.isInterfaceRed) Color.Black else MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    """
                    1. Load photos from your device
                    2. Convert images to negatives (inverts RGB values)
                    3. Enable Red Interface for safety - prevents accidental exposure
                    4. Set timing values:
                       • X: Pre-display black time
                       • Y: Display duration for exposure
                       • Z: Post-display black time
                       • A: Number of test parts
                    5. Display Negative: Shows full negative for Y seconds
                    6. Test Strips: Shows progressive strips for timing tests
                    7. Multi-Copy Test: Shows multiple small copies appearing progressively
                    
                    Test Strip Results:
                    • Part 1 (leftmost): Y seconds exposure
                    • Part 2: (Y-1) seconds exposure
                    • Part A (rightmost): (Y-A+1) seconds exposure
                    
                    Multi-Copy Test Results:
                    • Copy 1: Y seconds exposure
                    • Copy 2: (Y-1) seconds exposure
                    • Copy A: (Y-A+1) seconds exposure
                    """.trimIndent(),
                    color = if (appSettings.isInterfaceRed) Color.Black else MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showHelpDialog = false }
                ) {
                    Text(
                        "Got it",
                        color = if (appSettings.isInterfaceRed) Color.Black else MaterialTheme.colorScheme.primary
                    )
                }
            },
            containerColor = if (appSettings.isInterfaceRed) Color(0xFF8B0000) else MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun SettingInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRed: Boolean
) {
    Column {
        Text(
            text = label,
            color = if (isRed) Color.Black else MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = if (isRed) OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ) else OutlinedTextFieldDefaults.colors()
        )
    }
}