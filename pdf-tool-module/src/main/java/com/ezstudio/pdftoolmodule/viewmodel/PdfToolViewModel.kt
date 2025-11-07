package com.ezstudio.pdftoolmodule.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ezstudio.pdftoolmodule.model.FileModel
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.ezteam.baseproject.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PdfToolViewModel(application: Application) : BaseViewModel(application) {

    var lstPdfFile = MutableLiveData<MutableList<FileModel>>()
    var lstFileSuccess = mutableListOf<String>()

    var openFile = MutableLiveData<String>()
    var pdfCreated = MutableLiveData<MutableList<String>?>()

    fun getAllFileInDevice() {
        viewModelScope.launch(Dispatchers.IO) {
            FileUtils.checkFolderSystem(getApplication())
            val fileInDevice = FileUtils.getPdfFileList(getApplication())
            lstPdfFile.value?.clear()
            viewModelScope.launch(Dispatchers.Main) {
                lstPdfFile.value = fileInDevice.sortedBy { it.name }.toMutableList()
            }
        }
    }

}