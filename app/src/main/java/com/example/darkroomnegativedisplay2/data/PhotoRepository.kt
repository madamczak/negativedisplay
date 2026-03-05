package com.example.darkroomnegativedisplay2.data

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing photo data and current photo state
 */
class PhotoRepository {

    companion object {
        @Volatile
        private var INSTANCE: PhotoRepository? = null

        fun getInstance(): PhotoRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PhotoRepository().also { INSTANCE = it }
            }
        }
    }

    private val _photos = MutableStateFlow<List<PhotoModel>>(emptyList())
    val photos: StateFlow<List<PhotoModel>> = _photos.asStateFlow()

    private val _currentPhotoIndex = MutableStateFlow(0)
    val currentPhotoIndex: StateFlow<Int> = _currentPhotoIndex.asStateFlow()

    fun addPhotos(newPhotos: List<PhotoModel>) {
        _photos.value = newPhotos
        _currentPhotoIndex.value = 0
    }

    fun getCurrentPhoto(): PhotoModel? {
        val photoList = _photos.value
        val index = _currentPhotoIndex.value
        return if (photoList.isNotEmpty() && index in photoList.indices) {
            photoList[index]
        } else null
    }

    fun nextPhoto(): Boolean {
        val photoList = _photos.value
        val currentIndex = _currentPhotoIndex.value
        return if (currentIndex < photoList.size - 1) {
            _currentPhotoIndex.value = currentIndex + 1
            true
        } else false
    }

    fun previousPhoto(): Boolean {
        val currentIndex = _currentPhotoIndex.value
        return if (currentIndex > 0) {
            _currentPhotoIndex.value = currentIndex - 1
            true
        } else false
    }

    fun updateCurrentPhoto(updatedPhoto: PhotoModel) {
        val currentList = _photos.value.toMutableList()
        val currentIndex = _currentPhotoIndex.value
        if (currentIndex in currentList.indices) {
            currentList[currentIndex] = updatedPhoto
            _photos.value = currentList
        }
    }

    fun clearPhotos() {
        _photos.value = emptyList()
        _currentPhotoIndex.value = 0
    }
}
