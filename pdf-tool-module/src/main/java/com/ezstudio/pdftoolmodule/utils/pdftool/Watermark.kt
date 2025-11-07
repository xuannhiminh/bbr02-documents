package com.ezstudio.pdftoolmodule.utils.pdftool

import android.content.Context
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.itextpdf.text.*
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object Watermark {

    suspend fun start(
        context: Context,
        filePath: String,
        folderSave: String,
        watermarkModel: WatermarkModel
    ): String? {
        return withContext(Dispatchers.IO) {
            var error = false
            val finPath = "$folderSave/${watermarkModel.fileName}.pdf"
            try {
                val reader = PdfReader(filePath)
                val stamper = PdfStamper(reader, FileOutputStream(finPath))
                val font = Font(
                    watermarkModel.fontFamily, watermarkModel.textSize,
                    watermarkModel.fontStyle, watermarkModel.textColor
                )
                val p = Phrase(watermarkModel.watermarkText, font)
                var over: PdfContentByte?
                var pagesize: Rectangle
                var x: Float
                var y: Float
                val n = reader.numberOfPages
                for (i in 1..n) {

                    pagesize = reader.getPageSizeWithRotation(i)
                    x = (pagesize.left + pagesize.right) / 2
                    y = (pagesize.top + pagesize.bottom) / 2
                    over = stamper.getOverContent(i)
                    ColumnText.showTextAligned(
                        over,
                        Element.ALIGN_CENTER,
                        p,
                        x,
                        y,
                        watermarkModel.rotationAngle
                    )
                }
                stamper.close()
                reader.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
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
}