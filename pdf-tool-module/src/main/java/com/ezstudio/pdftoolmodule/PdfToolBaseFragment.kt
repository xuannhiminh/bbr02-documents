package com.ezstudio.pdftoolmodule

import android.os.Environment
import android.system.Os.remove
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.ezstudio.pdftoolmodule.activity.SelectFileActivity
import com.ezstudio.pdftoolmodule.activity.SuccessActivity
import com.ezstudio.pdftoolmodule.bottomsheet.BottomSheetSelectFile
import com.ezstudio.pdftoolmodule.dialog.AddWatermarkDialog
import com.ezstudio.pdftoolmodule.dialog.CreateFileDialog
import com.ezstudio.pdftoolmodule.dialog.PasswordDialog
import com.ezstudio.pdftoolmodule.dialog.RotatePageDialog
import com.ezstudio.pdftoolmodule.model.FileModel
import com.ezstudio.pdftoolmodule.utils.pdftool.*
import com.ezstudio.pdftoolmodule.viewmodel.PdfToolViewModel
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.fragment.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

abstract class PdfToolBaseFragment<B : ViewBinding> : BaseFragment<B>() {

    val toolViewModel by inject<PdfToolViewModel>()

    fun openSuccessScreen(lstPath: MutableList<String>) {
        toolViewModel.lstFileSuccess.apply {
            clear()
            addAll(lstPath)
        }
        SuccessActivity.start(requireActivity())
    }

    fun showBottomAddFile(
        maxItem: Int,
        filter: BottomSheetSelectFile.Filter = BottomSheetSelectFile.Filter.ALL,
        result: (MutableList<FileModel>) -> Unit
    ) {
        val bottomSelectFile = BottomSheetSelectFile.getInstance(maxItem)
        bottomSelectFile.listFilter = filter
        bottomSelectFile.doneListener = {
            result(it)
        }
        bottomSelectFile.show(
            childFragmentManager,
            BottomSheetSelectFile::class.java.name
        )
    }

    private fun getFolderAppSave(): String {
        val mediaStorageDir = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + Environment.DIRECTORY_DOCUMENTS, "EzPdfReader"
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return mediaStorageDir.absolutePath
            }
        }
        return mediaStorageDir.absolutePath
    }

    fun callFunctionInvert() {
        showBottomAddFile(1) {
            if (checkFileProtected(it[0])) {
                return@showBottomAddFile
            }
            val fileModel = it[0]
            val fileNameDefault =
                "${getString(R.string.invert)}_${System.currentTimeMillis()}"
            CreateFileDialog.ExtendBuilder(requireActivity())
                .setFileName(fileNameDefault)
                .setDisplayPassword(false)
                .setTitle(resources.getString(R.string.invert_pdf))
                .onSetPositiveButton(resources.getString(R.string.save)) { dialog, data ->
                    dialog?.dismiss()
                    val fileName = data[CreateFileDialog.KEY_FILE_NAME] as String
                    lifecycleScope.launch(Dispatchers.Main) {
                        toolViewModel.isLoading.value = true
                        val filePath =
                            Invert.start(
                                requireActivity(),
                                fileModel.path,
                                fileName,
                                getFolderAppSave()
                            )
                        toolViewModel.isLoading.value = false
                        if (TextUtils.isEmpty(filePath)) {
                            toast(getString(com.ezteam.baseproject.R.string.app_error))
                        } else {
                            toolViewModel.pdfCreated.postValue(mutableListOf(filePath!!))
                            openSuccessScreen(mutableListOf(filePath))
                        }
                    }

                }
                .onSetNegativeButton(resources.getString(R.string.cancel)) { }
                .build()
                .show()
        }
    }

    fun callFunctionRemoveDuplicate() {
        showBottomAddFile(1) {
            if (checkFileProtected(it[0])) {
                return@showBottomAddFile
            }
            val fileModel = it[0]
            val fileNameDefault =
                "${getString(R.string.duplicate)}_${System.currentTimeMillis()}"
            CreateFileDialog.ExtendBuilder(requireActivity())
                .setFileName(fileNameDefault)
                .setDisplayPassword(false)
                .setTitle(resources.getString(R.string.remove_duplicate_page))
                .onSetPositiveButton(resources.getString(R.string.save)) { dialog, data ->
                    dialog?.dismiss()
                    val fileName = data[CreateFileDialog.KEY_FILE_NAME] as String
                    lifecycleScope.launch(Dispatchers.Main) {
                        toolViewModel.isLoading.postValue(true)
                        val filePath =
                            RemoveDuplicatePage.start(
                                requireActivity(),
                                fileModel.path,
                                fileName,
                                getFolderAppSave()
                            )
                        toolViewModel.isLoading.postValue(false)
                        when {
                            TextUtils.isEmpty(filePath) -> {
                                toast(getString(com.ezteam.baseproject.R.string.app_error))
                            }
                            filePath == fileModel.path -> {
                                toast(getString(R.string.no_repetition_found))
                            }
                            else -> {
                                toolViewModel.pdfCreated.postValue(mutableListOf(filePath!!))
                                openSuccessScreen(mutableListOf(filePath))
                            }
                        }
                    }

                }
                .onSetNegativeButton(resources.getString(R.string.cancel)) { }
                .build()
                .show()
        }
    }

    fun callFunctionSplit() {
        showBottomAddFile(1) {
            if (checkFileProtected(it[0])) {
                return@showBottomAddFile
            }
            val fileModel = it[0]
            CreateFileDialog.ExtendBuilder(requireActivity())
                .setFileName("")
                .setHint(getString(R.string.enter_page_split))
                .setDisplayPassword(false)
                .setMessage(getString(R.string.split_suggest))
                .setTitle(resources.getString(R.string.split_pdf))
                .onSetPositiveButton(resources.getString(R.string.save)) { dialog, data ->
                    dialog?.dismiss()
                    val fileName = data[CreateFileDialog.KEY_FILE_NAME] as String
                    lifecycleScope.launch(Dispatchers.Main) {
                        toolViewModel.isLoading.postValue(true)
                        when (val errorCode = Split.isInputValid(fileModel.path, fileName)) {
                            0 -> {
                                val lstPath =
                                    Split.start(
                                        requireActivity(),
                                        fileModel.path,
                                        fileName,
                                        getFolderAppSave()
                                    )
                                if (lstPath.isNullOrEmpty()) {
                                    toast(getString(com.ezteam.baseproject.R.string.app_error))
                                } else {
                                    toolViewModel.pdfCreated.postValue(lstPath)
                                    openSuccessScreen(lstPath)
                                }
                            }
                            else -> {
                                toast(getString(errorCode))
                            }
                        }
                        toolViewModel.isLoading.postValue(false)
                    }

                }
                .onSetNegativeButton(resources.getString(R.string.cancel)) { }
                .build()
                .show()
        }
    }

    fun checkFileProtected(fileModel: FileModel, showToast: Boolean = true): Boolean {
        if (Password.isPDFEncrypted(fileModel.path)) {
            if (showToast) {
                toast(getString(R.string.file_is_protected))
            }
            return true
        }
        return false
    }

    fun callFunctionAddPassword() {
        showBottomAddFile(1, BottomSheetSelectFile.Filter.ENCRYPTION) {
            if (checkFileProtected(it[0])) {
                return@showBottomAddFile
            }
            val fileModel = it[0]
            val finPath = "${getString(R.string.encryption)}_${
                System.currentTimeMillis()
            }".replace("_${getString(R.string.decryption)}", "")
            PasswordDialog.ExtendBuilder(requireActivity())
                .setFileName(finPath)
                .setTitle(resources.getString(R.string.add_password))
                .onSetPositiveButton(resources.getString(R.string.add)) { dialog, data ->
                    dialog?.dismiss()
                    val password = data[PasswordDialog.KEY_PASSWORD] as String
                    val fileName = data[PasswordDialog.KEY_FILE_NAME] as String
                    lifecycleScope.launch(Dispatchers.Main) {
                        toolViewModel.isLoading.postValue(true)
                        val finPath = Password.encryption(
                            requireActivity(),
                            fileModel.path,
                            "${getFolderAppSave()}/$fileName.pdf",
                            password
                        )
                        toolViewModel.isLoading.postValue(false)
                        if (finPath.isNullOrEmpty()) {
                            toast(getString(com.ezteam.baseproject.R.string.app_error))
                        } else {
                            toolViewModel.pdfCreated.postValue(mutableListOf(finPath))
                            openSuccessScreen(mutableListOf(finPath))
                        }
                    }
                }
                .onSetNegativeButton(resources.getString(R.string.cancel)) { }
                .build()
                .show()
        }
    }

    fun callFunctionRemovePassword() {
        showBottomAddFile(1, BottomSheetSelectFile.Filter.DECRYPTION) {
            if (!checkFileProtected(it[0], false)) {
                toast(getString(R.string.file_not_protected))
                return@showBottomAddFile
            }
            val fileModel = it[0]
            val finPath = "${getString(R.string.decryption)}_${
                System.currentTimeMillis()
            }".replace("_${getString(R.string.encryption)}", "")
            PasswordDialog.ExtendBuilder(requireActivity())
                .setFileName(finPath)
                .setTitle(resources.getString(R.string.remove_password))
                .onSetPositiveButton(resources.getString(R.string.remove)) { dialog, data ->
                    dialog?.dismiss()
                    val password = data[PasswordDialog.KEY_PASSWORD] as String
                    val fileName = data[PasswordDialog.KEY_FILE_NAME] as String
                    lifecycleScope.launch(Dispatchers.Main) {
                        toolViewModel.isLoading.postValue(true)
                        val finPath = Password.decryption(
                            requireActivity(),
                            fileModel.path,
                            "${getFolderAppSave()}/$fileName.pdf",
                            password
                        )
                        toolViewModel.isLoading.postValue(false)
                        if (finPath.isNullOrEmpty()) {
                            toast(getString(R.string.remove_password_error))
                        } else {
                            toolViewModel.pdfCreated.postValue(mutableListOf(finPath))
                            openSuccessScreen(mutableListOf(finPath))
                        }
                    }
                }
                .onSetNegativeButton(resources.getString(R.string.cancel)) { }
                .build()
                .show()
        }
    }

    fun callFunctionRotatePage() {
        showBottomAddFile(1) {
            if (checkFileProtected(it[0])) {
                return@showBottomAddFile
            }
            val fileModel = it[0]
            val fileNameDefault =
                "${getString(R.string.rotate)}_${System.currentTimeMillis()}"
            RotatePageDialog.ExtendBuilder(requireActivity())
                .setFileName(fileNameDefault)
                .setTitle(resources.getString(R.string.rotate_pages))
                .onSetPositiveButton(resources.getString(R.string.save)) { dialog, data ->
                    dialog?.dismiss()
                    val fileName = data[RotatePageDialog.KEY_FILE_NAME] as String
                    val angle = data[RotatePageDialog.KEY_ANGLE] as Int
                    lifecycleScope.launch(Dispatchers.Main) {
                        toolViewModel.isLoading.postValue(true)
                        val finPath =
                            Rotate.start(
                                requireActivity(),
                                angle,
                                fileModel.path,
                                fileName,
                                getFolderAppSave()
                            )
                        toolViewModel.isLoading.postValue(false)
                        if (finPath.isNullOrEmpty()) {
                            toast(getString(com.ezteam.baseproject.R.string.app_error))
                        } else {
                            toolViewModel.pdfCreated.postValue(mutableListOf(finPath))
                            openSuccessScreen(mutableListOf(finPath))
                        }
                    }

                }
                .onSetNegativeButton(resources.getString(R.string.cancel)) { }
                .build()
                .show()
        }
    }

    fun callFunctionAddWatermark() {
        showBottomAddFile(1) {
            if (checkFileProtected(it[0])) {
                return@showBottomAddFile
            }
            val fileModel = it[0]
            val fileNameDefault =
                "${getString(R.string.watermark)}_${System.currentTimeMillis()}"
            val dialog = AddWatermarkDialog.ExtendBuilder(requireActivity())
                .setFileName(fileNameDefault)
                .setTitle(resources.getString(R.string.add_watermark))
                .onSetPositiveButton(resources.getString(R.string.save)) { _, _ -> }
                .onSetNegativeButton(resources.getString(R.string.cancel)) { }
                .build() as AddWatermarkDialog
            dialog.result = {
                lifecycleScope.launch(Dispatchers.Main) {
                    toolViewModel.isLoading.postValue(true)
                    val finPath =
                        Watermark.start(
                            requireActivity(),
                            fileModel.path,
                            getFolderAppSave(),
                            it
                        )
                    toolViewModel.isLoading.postValue(false)
                    if (finPath.isNullOrEmpty()) {
                        toast(getString(com.ezteam.baseproject.R.string.app_error))
                    } else {
                        toolViewModel.pdfCreated.postValue(mutableListOf(finPath))
                        openSuccessScreen(mutableListOf(finPath))
                    }
                }
            }
            try {
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PdfToolBaseFragment", "Error showing AddWatermarkDialog: ${e.message}", e)
            }
        }
    }
}