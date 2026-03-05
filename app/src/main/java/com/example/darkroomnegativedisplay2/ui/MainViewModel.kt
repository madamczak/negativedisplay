package com.example.darkroomnegativedisplay2.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.darkroomnegativedisplay2.data.AppSettings
import com.example.darkroomnegativedisplay2.data.PhotoModel
import com.example.darkroomnegativedisplay2.data.PhotoRepository
import com.example.darkroomnegativedisplay2.data.SettingsRepository
import com.example.darkroomnegativedisplay2.utils.ImageProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(
    private val context: Context
) : ViewModel() {

    private val photoRepository = PhotoRepository.getInstance()
    private val settingsRepository = SettingsRepository(context)
    private val imageProcessor = ImageProcessor()

    val photos = photoRepository.photos
    val currentPhotoIndex = photoRepository.currentPhotoIndex
    val appSettings = settingsRepository.appSettings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingMessage = MutableStateFlow("")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()

    // Derived state for button enabled status
    val isDisplayNegativeEnabled = combine(
        photos,
        currentPhotoIndex,
        appSettings
    ) { photoList, index, settings ->
        val currentPhoto = if (photoList.isNotEmpty() && index in photoList.indices) {
            photoList[index]
        } else null

        currentPhoto?.isNegativeConverted == true && settings.isInterfaceRed
    }

    fun loadPhotos(uris: List<Uri>) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Loading photos..."

            try {
                val photoModels = uris.mapNotNull { uri ->
                    val bitmap = imageProcessor.loadScaledBitmap(context, uri, 1920, 1080)
                    if (bitmap != null) {
                        PhotoModel(uri = uri, bitmap = bitmap)
                    } else null
                }

                photoRepository.addPhotos(photoModels)
                _loadingMessage.value = "Photos loaded successfully"
            } catch (e: Exception) {
                _loadingMessage.value = "Error loading photos: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun convertCurrentToNegative() {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Converting to negative..."

            try {
                val currentPhoto = photoRepository.getCurrentPhoto()
                currentPhoto?.bitmap?.let { bitmap ->
                    val negativeBitmap = imageProcessor.convertToNegative(bitmap)
                    val updatedPhoto = currentPhoto.copy(
                        negativeBitmap = negativeBitmap,
                        isNegativeConverted = true
                    )
                    photoRepository.updateCurrentPhoto(updatedPhoto)
                    _loadingMessage.value = "Image converted to negative"
                }
            } catch (e: Exception) {
                _loadingMessage.value = "Error converting image: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun navigateNext() {
        photoRepository.nextPhoto()
    }

    fun navigatePrevious() {
        photoRepository.previousPhoto()
    }

    fun updatePreDisplayBlackSeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.updatePreDisplayBlackSeconds(seconds)
        }
    }

    fun updateDisplayDurationSeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.updateDisplayDurationSeconds(seconds)
        }
    }

    fun updatePostDisplayBlackSeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.updatePostDisplayBlackSeconds(seconds)
        }
    }

    fun updateTestIrradiationParts(parts: Int) {
        viewModelScope.launch {
            settingsRepository.updateTestIrradiationParts(parts)
        }
    }

    fun toggleInterfaceRed(isRed: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateInterfaceRed(isRed)
        }
    }

    fun updateDeviceBrightness(useDeviceBrightness: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDeviceBrightness(useDeviceBrightness)
        }
    }
}
