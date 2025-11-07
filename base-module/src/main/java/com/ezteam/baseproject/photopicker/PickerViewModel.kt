package com.ezteam.baseproject.photopicker

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.ezteam.baseproject.extensions.autoRotate
import com.ezteam.baseproject.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.switchMap

class PickerViewModel(application: Application) : BaseViewModel(application) {
    val dirsLiveData: MutableLiveData<MutableList<PhotoDirectory>> =
        MutableLiveData(mutableListOf())
    val dirCurrentLiveData: MutableLiveData<PhotoDirectory> = MutableLiveData()
    val photosSelectedLiveData: MutableLiveData<MutableList<Photo>> =
        MutableLiveData(mutableListOf())
    val photosLiveData: MutableLiveData<MutableList<Photo>> = MutableLiveData(mutableListOf())

    fun loadImages() {
        photosSelectedLiveData.postValue(mutableListOf())
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            dirsLiveData.value?.let { dirs ->
                if (dirs.isEmpty()) {
                    val photoDirectoryAll = PhotoDirectory()
                    photoDirectoryAll.name = "All Images"
                    photoDirectoryAll.id = "ALL"
                    dirs.add(photoDirectoryAll)
                }
                ImageStorageUtils.getPhotoDirs(getApplication()) { photo ->
                    photosLiveData.value?.let {
                        try {
                            if (!it.contains(photo)) {
                                it.add(photo)

                                photosLiveData.postValue(it)
                                dirs.find {
                                    it.id == photo.bucketId || it.name == photo.bucketName
                                }?.let {
                                    it.photos.add(photo)
                                } ?: kotlin.run {
                                    val dir = PhotoDirectory()
                                    dir.id = photo.bucketId
                                    dir.name = photo.bucketName
                                    dir.coverPath = photo.path
                                    dir.photos.add(photo)
                                    dirs.add(dir)
                                }

                                dirs.find {
                                    it.id == "ALL"
                                }?.let {
                                    it.coverPath = photo.path
                                    it.photos.add(photo)
                                }
                                dirsLiveData.postValue(dirs)
                            }
                        } catch (e: Exception) {
                        }
                    }

                    isLoading.postValue(false)
                }
                dirCurrentLiveData.postValue(dirs[0])
            }
        }
    }

    fun autoRotatePhoto(data: MutableList<Uri>, result: (MutableList<Uri>) -> Unit) {
        isLoading.postValue(true)
        val lstOutput = mutableListOf<Uri>()
        viewModelScope.launch(Dispatchers.IO) {
            data.forEach {
                it.autoRotate(getApplication()).let { uriRotate ->
                    lstOutput.add(uriRotate)
                }
            }
            viewModelScope.launch(Dispatchers.Main) {
                isLoading.postValue(false)
                result(lstOutput)
            }
        }
    }

//    fun getPhotosLiveData(): LiveData<List<Photo>> {
//        return Transformations.switchMap(dirCurrentLiveData) { dir ->
//            return@switchMap Transformations.switchMap(photosLiveData) { photos ->
//                val data: MutableLiveData<List<Photo>> = MutableLiveData()
//                try {
//                    photos.filter {
//                        try {
//                            it.bucketId == dir.id || it.bucketName == dir.name || dir.id == "ALL"
//                        } catch (e: Exception) {
//                            false
//                        }
//                    }.let {
//                        data.postValue(it)
//                    }
//                } catch (e: Exception) {
//                    data.postValue(photos)
//                }
//                return@switchMap data
//            }
//        }
//    }

    fun getPhotosLiveData(): LiveData<List<Photo>> {
        return dirCurrentLiveData.switchMap { dir ->
            photosLiveData.switchMap { photos ->
                val data = MutableLiveData<List<Photo>>()
                try {
                    val filteredPhotos = photos.filter {
                        try {
                            it.bucketId == dir.id || it.bucketName == dir.name || dir.id == "ALL"
                        } catch (e: Exception) {
                            false
                        }
                    }
                    data.postValue(filteredPhotos)
                } catch (e: Exception) {
                    data.postValue(photos)
                }
                data
            }
        }
    }


    fun addPhotoSelected(photo: Photo) {
        val photosSelected = photosSelectedLiveData.value
        photo.isSelected = true
        photosSelected?.add(photo)
        photosSelectedLiveData.postValue(photosSelected)
    }

    fun removePhoto(photo: Photo) {
        val photosSelected = photosSelectedLiveData.value
        photo.isSelected = false
        if (photosSelected?.contains(photo) == true) {
            photosSelected.remove(photo)
        }
        photosSelectedLiveData.postValue(photosSelected)
    }
}