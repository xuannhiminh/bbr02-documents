package com.ezstudio.pdftoolmodule.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.ezstudio.pdftoolmodule.PdfToolBaseActivity
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.adapter.PageEditAdapter
import com.ezstudio.pdftoolmodule.databinding.ActivityEditPdfBinding
import com.ezstudio.pdftoolmodule.model.EditPageModel
import com.ezstudio.pdftoolmodule.utils.FileSaveManager
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.ezstudio.pdftoolmodule.utils.pdftool.Rotate
import com.ezstudio.pdftoolmodule.utils.pdftool.Thumbnail
import com.ezteam.baseproject.extensions.bitmapToUriCache
import com.ezteam.baseproject.utils.PathUtils
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.StringBuilder

class PdfEditActivity : PdfToolBaseActivity<ActivityEditPdfBinding>(), View.OnClickListener {

    companion object {
        const val FILE_PATH = "file path"

        fun start(context: Context, path: String) {
            val intent = Intent(context, PdfEditActivity::class.java).apply {
                putExtra(ExtractActivity.FILE_PATH, path)
            }
            context.startActivity(intent)

        }
    }

    private val filePath by lazy {
        intent?.getStringExtra(FILE_PATH) ?: ""
    }
    private val pageAdapter by lazy {
        PageEditAdapter(this, mutableListOf())
    }
    private val isNightMode by lazy {
        when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                true
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                false
            }
            else -> {
                false
            }
        }
    }

    override fun initView() {
//        window.statusBarColor = resources.getColor(R.color.toolbar_color)
        binding.rcvPage.adapter = pageAdapter
    }

    override fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            Thumbnail.start(filePath, 1.0f, false, result = { bitmap, index ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (!isDestroyed) {
                        val page = EditPageModel(bitmap, index)
                        pageAdapter.apply {
                            list.add(page)
                            notifyItemInserted(index)
                        }
                    }
                }
            })
        }
    }

    override fun initListener() {
        super.initListener()
        binding.ivBack.setOnClickListener(this)
        binding.ivSave.setOnClickListener(this)
    }

    override fun viewBinding(): ActivityEditPdfBinding {
        return ActivityEditPdfBinding.inflate(LayoutInflater.from(this))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> {
                finish()
            }
            R.id.iv_save -> {
                saveFile(pageAdapter.list)
            }
        }
    }

    private fun saveFile(lstData: MutableList<EditPageModel>) {
        showHideLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val folderSave = File(cacheDir.path, "pdf_edit")
            if (!folderSave.exists()) {
                folderSave.mkdirs()
            }
            val finPath = "${folderSave.path}/Final_${System.currentTimeMillis()}.pdf"
            val pathSort = "${folderSave.path}/Sort_${System.currentTimeMillis()}.pdf"
            try {
                /**Sort*/
                val sequence = StringBuilder("")
                lstData.forEachIndexed { index, editPageModel ->
                    if (editPageModel.position != -2) {
                        sequence.append(editPageModel.position + 1).append(",")
                    }
                }
                val mPages: String = sequence.toString()
                val readerSort = PdfReader(filePath)
                PdfReader.unethicalreading = true
                readerSort.selectPages(mPages)
                val pdfStamper = PdfStamper(readerSort, FileOutputStream(pathSort))
                pdfStamper.close()

                /**Add image*/
                val pathAddImage = "${folderSave.path}/AddImage_${System.currentTimeMillis()}.pdf"
                val readerAdd = PdfReader(pathSort)
                val document = Document()
                val writer = PdfWriter.getInstance(document, FileOutputStream(pathAddImage))
                document.open()
                var page = 1
                lstData.forEachIndexed { index, editPageModel ->
                    if (editPageModel.position == -2) {
                        document.newPage()
                        val image: Image =
                            Image.getInstance(editPageModel.btmEdit?.bitmapToUriCache(this@PdfEditActivity)?.path)
                        image.border = 0
                        val pageWidth = document.pageSize.width
                        val pageHeight = document.pageSize.height
                        image.scaleToFit(pageWidth, pageHeight)
                        image.setAbsolutePosition(
                            (document.pageSize.width - image.scaledWidth) / 2,
                            (document.pageSize.height - image.scaledHeight) / 2
                        )
                        document.add(image)
                    } else {
                        val importedPage = writer.getImportedPage(readerAdd, page)
                        document.newPage()
                        writer.directContent.addTemplate(importedPage, 0f, 0f)
                        page++
                    }
                }
                document.close()

                /**Rotate*/
                val pathRotate = "${folderSave.path}/Rotate_${System.currentTimeMillis()}.pdf"
                val readerRotate = PdfReader(pathAddImage)
                val stamperRotate = PdfStamper(readerRotate, FileOutputStream(pathRotate))
                for (page in 1..readerRotate.numberOfPages) {
                    Rotate.rotatePage(readerRotate, page, lstData[page - 1].rotate)
                }
                stamperRotate.close()
                readerRotate.close()

                /**invert*/
                val pathInvert = "${folderSave.path}/Invert_${System.currentTimeMillis()}.pdf"
                val readerInvert = PdfReader(pathRotate)
                val os: OutputStream = FileOutputStream(finPath)
                val stamperInvert = PdfStamper(readerInvert, os)
                for (page in 1..readerInvert.numberOfPages) {
                    if (lstData[page - 1].isInvert) {
                        val rect = stamperInvert.reader.getPageSize(page)
                        var cb = stamperInvert.getOverContent(page)
                        val gs = PdfGState()
                        gs.setBlendMode(PdfGState.BM_DIFFERENCE)
                        cb.setGState(gs)
                        cb.setColorFill(GrayColor(1.0f))
                        cb.rectangle(rect.left, rect.bottom, rect.width, rect.height)
                        cb.fill()
                        cb = stamperInvert.getUnderContent(page)
                        cb.setColorFill(GrayColor(1.0f))
                        cb.rectangle(rect.left, rect.bottom, rect.width, rect.height)
                        cb.fill()
                    }
                }
                stamperInvert.close()
                os.close()

            } catch (e: Exception) {
                e.printStackTrace()
                folderSave.delete()
            }
            FileSaveManager.deleteFile(this@PdfEditActivity, filePath) {
                val uri: Uri? = FileSaveManager.saveFileStorage(
                    this@PdfEditActivity,
                    finPath,
                    File(filePath).parent,
                    FilenameUtils.getBaseName(filePath)
                )
                val newRealPath: String? = uri?.let {
                    PathUtils.getPath(this@PdfEditActivity, uri)
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    folderSave.delete()
                    showHideLoading(false)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }
}