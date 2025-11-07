package com.ezteam.ezpdflib.activity.signature

import android.content.Context
import android.graphics.BitmapFactory
import com.ezteam.ezpdflib.extension.toByteArray
import com.ezteam.ezpdflib.model.SingleSize
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object SignatureUtils {

    fun signaturePDF(
        context: Context,
        bitmapPoints: FloatArray,
        document: PDSPDFDocument,
        fileSignature: File,
        indexPage: Int,
        widthScreen: Float,
        heightScreen: Float,
        ratioByWidth: Boolean
    ): String? {
        val file = File(context.cacheDir.path, "test_signature.pdf")
        if (file.exists()) {
            file.delete()
        }
        val stream: InputStream = document.stream
        val os = FileOutputStream(file)
        val reader = PdfReader(stream)
        var signer: PdfStamper? = null
        val mediabox = reader.getPageSize(indexPage + 1)
        val ratio = if (ratioByWidth) {
            mediabox.width * 1.0f / widthScreen
        } else {
            mediabox.height * 1.0f / heightScreen
        }

        val btm =
            BitmapFactory.decodeFile(fileSignature.path)
        val byteArray = btm.toByteArray()

        val sigimage = Image.getInstance(byteArray)

        if (signer == null) signer = PdfStamper(reader, os, '\u0000')
        val contentByte = signer.getOverContent(indexPage + 1)
        sigimage.alignment = Image.ALIGN_CENTER
        sigimage.scaleToFit(
            (bitmapPoints[2] - bitmapPoints[0]) * ratio,
            (bitmapPoints[5] - bitmapPoints[1] * ratio)
        )
        sigimage.setAbsolutePosition(
            bitmapPoints[0] * ratio,
            mediabox.top
                    - bitmapPoints[5] * ratio
        )
        contentByte.addImage(sigimage)

        signer.close()
        reader.close()
        os.close()
        return file.path
    }

    fun signaturePDF2(
        context: Context,
        document: PDSPDFDocument,
        fileSignature: File,
        indexPage: Int
    ): String? {
        val file = File(context.cacheDir.path, "test_signature.pdf")
        if (file.exists()) {
            file.delete()
        }
        try {
            val stream: InputStream = document.stream
            val os = FileOutputStream(file)
            val reader = PdfReader(stream)
            var signer: PdfStamper? = null
            val mediabox = reader.getPageSize(indexPage + 1)

            val btm =
                BitmapFactory.decodeFile(fileSignature.path)
            val byteArray = btm?.toByteArray()

            val sigimage = Image.getInstance(byteArray)

            if (signer == null) signer = PdfStamper(reader, os, '\u0000')
            val contentByte = signer.getOverContent(indexPage + 1)
            sigimage.alignment = Image.ALIGN_CENTER
            sigimage.scaleToFit(
                mediabox.width,
                mediabox.height
            )
            sigimage.setAbsolutePosition(
                0.0f,
                0.0f
            )
            contentByte.addImage(sigimage)

            signer.close()
            reader.close()
            os.close()
            return file.path
        } catch (e: Exception) {
            e.printStackTrace()
            return file.path
        }
    }

}