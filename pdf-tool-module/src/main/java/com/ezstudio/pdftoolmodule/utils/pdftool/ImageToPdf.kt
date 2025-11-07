package com.ezstudio.pdftoolmodule.utils.pdftool

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.text.TextUtils
import com.ezstudio.pdftoolmodule.utils.Config
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.ezstudio.pdftoolmodule.utils.pdftool.model.ImageToPDFOptions
import com.ezstudio.pdftoolmodule.utils.pdftool.model.WatermarkPageEvent
import com.itextpdf.text.*
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageToPdf {

    private const val DEFAULT_PAGE_SIZE = "A4"
    private const val IMAGE_SCALE_TYPE_ASPECT_RATIO = "maintain_aspect_ratio"
    private const val PG_NUM_STYLE_PAGE_X_OF_N = "pg_num_style_page_x_of_n"
    private const val PG_NUM_STYLE_X_OF_N = "pg_num_style_x_of_n"

    suspend fun start(
        context: Context,
        filename: String,
        folderSave: String,
        password: String? = null,
        lstImage: ArrayList<String>
    ): String? {
        return withContext(Dispatchers.IO) {
            val options = ImageToPDFOptions().apply {
                imagesUri = lstImage
                pageSize = DEFAULT_PAGE_SIZE
                imageScaleType = IMAGE_SCALE_TYPE_ASPECT_RATIO
                pageColor = Color.WHITE
                outFileName = filename
                if (!password.isNullOrEmpty()) {
                    isPasswordProtected = true
                    this.password = password
                }
            }
            var error = false
            val finPath = "$folderSave/$filename.pdf"
            val pageSize = Rectangle(PageSize.getRectangle(options.pageSize))
            pageSize.backgroundColor = BaseColor(
                Color.red(options.pageColor),
                Color.green(options.pageColor),
                Color.blue(options.pageColor)
            )
            val document = Document(
                pageSize,
                options.marginLeft.toFloat(),
                options.marginRight.toFloat(),
                options.marginTop.toFloat(),
                options.marginBottom.toFloat()
            )
            document.setMargins(
                options.marginLeft.toFloat(),
                options.marginRight.toFloat(),
                options.marginTop.toFloat(),
                options.marginBottom.toFloat()
            )
            val documentRect = document.pageSize

            try {
                val writer = PdfWriter.getInstance(document, FileOutputStream(finPath))
                if (options.isPasswordProtected) {
                    writer.setEncryption(
                        options.password.toByteArray(), Config.MASTER_PASSWORD.toByteArray(),
                        PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
                        PdfWriter.ENCRYPTION_AES_128
                    )
                }
                if (options.isWatermarkAdded) {
                    val watermarkPageEvent = WatermarkPageEvent()
                    watermarkPageEvent.watermark = options.watermark
                    writer.pageEvent = watermarkPageEvent
                }
                document.open()
                for (i in lstImage.indices) {
                    var quality: Int
                    quality = 30
                    if (!TextUtils.isEmpty(options.qualityString)) {
                        quality = options.qualityString.toInt()
                    }
                    val image: Image =
                        Image.getInstance(lstImage[i])
                    val qualityMod = quality * 0.09
                    image.compressionLevel = qualityMod.toInt()
                    image.border = Rectangle.BOX
                    image.borderWidth = options.borderWidth.toFloat()
//                val bmOptions = BitmapFactory.Options()
//                val bitmap = BitmapFactory.decodeFile(lstImage[i], bmOptions)
                    val pageWidth: Float =
                        document.pageSize.width - (options.marginLeft + options.marginRight)
                    val pageHeight: Float =
                        document.pageSize.height - (options.marginBottom + options.marginTop)
                    if (options.imageScaleType == IMAGE_SCALE_TYPE_ASPECT_RATIO) image.scaleToFit(
                        pageWidth,
                        pageHeight
                    ) else image.scaleAbsolute(pageWidth, pageHeight)
                    image.setAbsolutePosition(
                        (documentRect.width - image.scaledWidth) / 2,
                        (documentRect.height - image.scaledHeight) / 2
                    )
                    if (options.pageNumStyle != null) {
                        ColumnText.showTextAligned(
                            writer.directContent,
                            Element.ALIGN_BOTTOM,
                            getPhrase(writer, options.pageNumStyle, lstImage.size),
                            (documentRect.right + documentRect.left) / 2,
                            documentRect.bottom + 25, 0f
                        )
                    }
                    document.add(image)
                    document.newPage()
                }
                document.close()
            } catch (e: Exception) {
                e.printStackTrace()
                error = true
                File(finPath).delete()
            }
            FileUtils.scanFile(context, finPath, null)
            if (error) {
                null
            } else {
                finPath
            }
        }
    }

    private fun getPhrase(writer: PdfWriter, pageNumStyle: String, size: Int): Phrase? {
        val phrase: Phrase = when (pageNumStyle) {
            PG_NUM_STYLE_PAGE_X_OF_N -> Phrase(
                String.format(
                    "Page %d of %d",
                    writer.pageNumber,
                    size
                )
            )
            PG_NUM_STYLE_X_OF_N -> Phrase(
                String.format(
                    "%d of %d",
                    writer.pageNumber,
                    size
                )
            )
            else -> Phrase(String.format("%d", writer.pageNumber))
        }
        return phrase
    }

    private fun convertToUriCache(context: Context, uri: String): Uri? {
        val input = context.contentResolver.openInputStream(Uri.parse(uri))
        val bitmap = BitmapFactory.decodeStream(input)
        input!!.close()
        val fileSave = File(context.cacheDir.path, System.currentTimeMillis().toString() + "")
        val outputStream = FileOutputStream(fileSave)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(fileSave)
    }
}