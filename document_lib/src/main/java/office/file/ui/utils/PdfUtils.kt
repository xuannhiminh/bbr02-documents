package office.file.ui.utils

import android.content.Context
import android.text.TextUtils
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object PdfUtils {

    fun removePassword(
        context: Context,
        filePath: String,
        inputPassword: String?
    ): String {
        val newPath = "${context.cacheDir.path}/${FilenameUtils.getName(filePath)}"
        val file = File(newPath)
        if (file.exists()) {
            file.delete()
        }
        try {
            val reader = PdfReader(filePath, inputPassword?.toByteArray())
            val stamper = PdfStamper(
                unlockPdf(reader),
                FileOutputStream(newPath)
            )
            stamper.close()
            reader.close()
            return newPath
        } catch (e: DocumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return ""
    }

    fun unlockPdf(reader: PdfReader?): PdfReader? {
        if (reader == null) {
            return reader
        }
        try {
            val f = reader.javaClass.getDeclaredField("encrypted")
            f.isAccessible = true
            f[reader] = false
        } catch (e: Exception) {
        }
        return reader
    }

    private fun removePasswordMaster(
        filePath: String,
        finPath: String,
        inputPassword: String
    ): String {
        try {
            val reader = PdfReader(filePath, "xxx".toByteArray())
            val password: ByteArray = reader.computeUserPassword()
            val input = inputPassword.toByteArray()
            if (input.contentEquals(password)) {
                val stamper = PdfStamper(
                    reader,
                    FileOutputStream(finPath)
                )
                stamper.close()
                reader.close()
                return finPath
            }
        } catch (e: DocumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


}