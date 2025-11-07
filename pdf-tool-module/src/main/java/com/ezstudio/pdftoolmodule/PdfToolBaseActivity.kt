package com.ezstudio.pdftoolmodule

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.ezstudio.pdftoolmodule.activity.MergeActivity
import com.ezstudio.pdftoolmodule.activity.SelectFileActivity
import com.ezstudio.pdftoolmodule.activity.SuccessActivity
import com.ezstudio.pdftoolmodule.bottomsheet.BottomSheetSelectFile
import com.ezstudio.pdftoolmodule.dialog.CreateFileDialog
import com.ezstudio.pdftoolmodule.model.FileModel
import com.ezstudio.pdftoolmodule.utils.pdftool.AddImage
import com.ezstudio.pdftoolmodule.utils.pdftool.ImageToPdf
import com.ezstudio.pdftoolmodule.utils.pdftool.Merge
import com.ezstudio.pdftoolmodule.utils.pdftool.Password
import com.ezstudio.pdftoolmodule.viewmodel.PdfToolViewModel
import com.ezteam.baseproject.activity.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

abstract class PdfToolBaseActivity<B : ViewBinding> : BaseActivity<B>() {

    val toolViewModel by inject<PdfToolViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EzAdControl.getInstance(this).showAds()
    }

    override fun initListener() {
        toolViewModel.isLoading.observe(this) {
            showHideLoading(it)
        }
    }

    fun openSuccessScreen(lstPath: MutableList<String>) {
        toolViewModel.lstFileSuccess.apply {
            clear()
            addAll(lstPath)
        }
        SuccessActivity.start(this)
    }

    fun showBottomAddFile(
        maxItem: Int,
        filter: BottomSheetSelectFile.Filter = BottomSheetSelectFile.Filter.ALL,
        result: (MutableList<FileModel>) -> Unit
    ) {
        BottomSheetSelectFile.getInstance(maxItem).apply {
            listFilter = filter
            doneListener = {
                result(it)
            }
            show(
                supportFragmentManager,
                BottomSheetSelectFile::class.java.name
            )
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

    fun callFunctionMerge(lstPath: MutableList<String>) {
        val fileNameDefault =
            "${getString(R.string.merge)}_${System.currentTimeMillis()}"
        CreateFileDialog.ExtendBuilder(this)
            .setFileName(fileNameDefault)
            .setTitle(resources.getString(R.string.merge_pdf))
            .onSetPositiveButton(resources.getString(R.string.save)) { dialog, data ->
                dialog?.dismiss()
                val fileName = data[CreateFileDialog.KEY_FILE_NAME] as String
                val password = data[CreateFileDialog.KEY_PASSWORD] as String?
                lifecycleScope.launch(Dispatchers.Main) {
                    toolViewModel.isLoading.postValue(true)
                    val filePath =
                        Merge.start(
                            this@PdfToolBaseActivity,
                            fileName,
                            getFolderAppSave(),
                            lstPath,
                            password
                        )
                    toolViewModel.isLoading.postValue(false)
                    if (TextUtils.isEmpty(filePath)) {
                        toast(getString(com.ezteam.baseproject.R.string.app_error))
                    } else {
                        toolViewModel.pdfCreated.postValue(mutableListOf(filePath!!))
                        val intent = Intent().apply {
                            putExtra(MergeActivity.FILE_PATH, filePath)
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }

            }
            .onSetNegativeButton(resources.getString(R.string.cancel)) { }
            .build()
            .show()
    }

    fun callFunctionAddImage(filePath: String, lstPath: MutableList<Uri>) {
        val fileNameDefault =
            "${getString(R.string.add_image_2)}_${System.currentTimeMillis()}"
        CreateFileDialog.ExtendBuilder(this)
            .setDisplayPassword(false)
            .setFileName(fileNameDefault)
            .setTitle(resources.getString(R.string.add_image))
            .onSetPositiveButton(resources.getString(R.string.save)) { dialog, data ->
                dialog?.dismiss()
                val fileName = data[CreateFileDialog.KEY_FILE_NAME] as String
                lifecycleScope.launch(Dispatchers.Main) {
                    toolViewModel.isLoading.postValue(true)
                    val filePath =
                        AddImage.start(
                            this@PdfToolBaseActivity,
                            filePath,
                            fileName,
                            getFolderAppSave(),
                            lstPath
                        )
                    toolViewModel.isLoading.postValue(false)
                    if (TextUtils.isEmpty(filePath)) {
                        toast(getString(com.ezteam.baseproject.R.string.app_error))
                    } else {
                        toolViewModel.pdfCreated.postValue(mutableListOf(filePath!!))
                        val intent = Intent().apply {
                            putExtra(MergeActivity.FILE_PATH, filePath)
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }

            }
            .onSetNegativeButton(resources.getString(R.string.cancel)) { }
            .build()
            .show()
    }

    fun callFunctionImageToPdf(
        lstPath: ArrayList<String>,
        finishCurrentScreen: Boolean = true,
        success: ((Unit) -> Unit)? = null
    ) {
        val fileNameDefault =
            "${getString(R.string.image_to_pdf_2)}_${System.currentTimeMillis()}"
        CreateFileDialog.ExtendBuilder(this)
            .setDisplayPassword(true)
            .setFileName(fileNameDefault)
            .setTitle(resources.getString(R.string.image_to_pdf))
            .onSetPositiveButton(resources.getString(R.string.save)) { dialog, data ->
                dialog?.dismiss()
                val fileName = data[CreateFileDialog.KEY_FILE_NAME] as String
                val password = data[CreateFileDialog.KEY_PASSWORD] as String?
                lifecycleScope.launch(Dispatchers.Main) {
                    toolViewModel.isLoading.postValue(true)
                    val filePath =
                        ImageToPdf.start(
                            this@PdfToolBaseActivity,
                            fileName,
                            getFolderAppSave(),
                            password,
                            lstPath
                        )
                    toolViewModel.isLoading.postValue(false)
                    if (TextUtils.isEmpty(filePath)) {
                        toast(getString(com.ezteam.baseproject.R.string.app_error))
                    } else {
                        toolViewModel.pdfCreated.postValue(mutableListOf(filePath!!))
                        success?.invoke(Unit)
                        if (finishCurrentScreen) {
                            val intent = Intent().apply {
                                putExtra(MergeActivity.FILE_PATH, filePath)
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }
                }

            }
            .onSetNegativeButton(resources.getString(R.string.cancel)) { }
            .build()
            .show()
    }

}