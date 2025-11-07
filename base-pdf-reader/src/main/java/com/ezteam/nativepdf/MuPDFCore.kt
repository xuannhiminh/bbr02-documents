package com.ezteam.nativepdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.ezteam.ezpdflib.R
import java.io.Serializable
import java.util.*
import androidx.annotation.Keep

@Keep
open class MuPDFCore : Serializable {

    companion object {
        init {
            System.loadLibrary("ezpdf")
        }
    }

    var globals: Long = 0
    var fileFormat: String? = null
    var isUnencryptedPDF = false
    var wasOpenedFromBuffer = false
    var numPages = -1
    private var pageWidth = 0f
    private var pageHeight = 0f
    var fileBuffer: ByteArray? = null

    /* The native functions */
    private external fun openFile(filename: String): Long
    private external fun fileFormatInternal(): String?
    private external fun isUnencryptedPDFInternal(): Boolean
    private external fun countPagesInternal(): Int
    private external fun gotoPageInternal(localActionPageNum: Int)
    private external fun getPageWidth(): Float
    private external fun getPageHeight(): Float
    private external fun createCookie(): Long
    private external fun destroyCookie(cookie: Long)
    private external fun abortCookie(cookie: Long)
    private external fun openBuffer(magic: String): Long
    private external fun needsPasswordInternal(): Boolean
    private external fun authenticatePasswordInternal(password: String): Boolean
    private external fun searchPage(text: String): Array<RectF>
    private external fun getAnnotationsInternal(page: Int): Array<Annotation>
    private external fun getOutlineInternal(): Array<OutlineItem>
    private external fun deleteAnnotationInternal(indexAnnotation: Int)
    private external fun hasChangesInternal(): Boolean
    private external fun saveInternal()
    private external fun hasOutlineInternal(): Boolean
    private external fun addInkAnnotationInternal(
        arcs: Array<Array<PointF>?>,
        colorR: Float,
        colorG: Float,
        colorB: Float,
        thickness: Float,
    )

    private external fun text(): Array<Array<Array<Array<TextChar>>>>
    private external fun addMarkupAnnotationInternal(
        quadPoints: Array<PointF>,
        type: Int,
        colorR: Float,
        colorG: Float,
        colorB: Float,
    )

    private external fun drawPage(
        bitmap: Bitmap,
        pageW: Int, pageH: Int,
        patchX: Int, patchY: Int,
        patchW: Int, patchH: Int,
        cookiePtr: Long
    )

    private external fun updatePageInternal(
        bitmap: Bitmap,
        page: Int,
        pageW: Int, pageH: Int,
        patchX: Int, patchY: Int,
        patchW: Int, patchH: Int,
        cookiePtr: Long
    )
    /**/

    @Throws(java.lang.Exception::class)
    fun openFileFromPath(context: Context, filePath: String) {
        globals = openFile(filePath)
        if (globals != 0L) {
            fileFormat = fileFormatInternal()
            isUnencryptedPDF = isUnencryptedPDFInternal()
            wasOpenedFromBuffer = false
        }
    }

    @Throws(java.lang.Exception::class)
    fun openFileBuffer(context: Context, buffer: ByteArray, magic: String?) {
        fileBuffer = buffer
        globals = openBuffer(magic ?: "")
        if (globals == 0L) {
            throw java.lang.Exception(context.getString(R.string.cannot_open_buffer))
        }
        fileFormat = fileFormatInternal()
        isUnencryptedPDF = isUnencryptedPDFInternal()
        wasOpenedFromBuffer = true
    }

    @Synchronized
    fun addInkAnnotation(
        page: Int, arcs: Array<Array<PointF>?>, colorR: Float,
        colorG: Float,
        colorB: Float,
        thickness: Float,
    ) {
        gotoPage(page)
        addInkAnnotationInternal(arcs, colorR, colorG, colorB, thickness)
    }

    @Synchronized
    fun addMarkupAnnotation(
        page: Int, quadPoints: Array<PointF>,
        type: Annotation.Type,
        colorR: Float,
        colorG: Float,
        colorB: Float,
    ) {
        gotoPage(page)
        addMarkupAnnotationInternal(quadPoints, type.ordinal, colorR, colorG, colorB)
    }

    @Synchronized
    fun hasChanges(): Boolean {
        return hasChangesInternal()
    }

    @Synchronized
    fun save() {
        saveInternal()
    }

    @Synchronized
    fun getAnnoations(page: Int): Array<Annotation> {
        return getAnnotationsInternal(page)
    }

    @Synchronized
    fun needsPassword(): Boolean {
        return needsPasswordInternal()
    }

    @Synchronized
    fun authenticatePassword(password: String): Boolean {
        return authenticatePasswordInternal(password)
    }

    @Synchronized
    fun deleteAnnotation(page: Int, indexAnnotation: Int) {
        gotoPage(page)
        deleteAnnotationInternal(indexAnnotation)
    }

    @Synchronized
    fun drawPage(
        bm: Bitmap?, page: Int,
        pageW: Int, pageH: Int,
        patchX: Int, patchY: Int,
        patchW: Int, patchH: Int,
        cookie: MuPDFCore.Cookie
    ) {
        gotoPage(page)
        drawPage(bm!!, pageW, pageH, patchX, patchY, patchW, patchH, cookie.cookiePtr)
    }

    @Synchronized
    fun updatePage(
        bm: Bitmap?, page: Int,
        pageW: Int, pageH: Int,
        patchX: Int, patchY: Int,
        patchW: Int, patchH: Int,
        cookie: Cookie
    ) {
        updatePageInternal(
            bm!!,
            page,
            pageW,
            pageH,
            patchX,
            patchY,
            patchW,
            patchH,
            cookie.cookiePtr
        )
    }

    private fun gotoPage(page: Int) {
        var page = page
        if (page > numPages - 1) page = numPages - 1 else if (page < 0) page = 0
        gotoPageInternal(page)
        this.pageWidth = getPageWidth()
        this.pageHeight = getPageHeight()
    }

    inner class Cookie {
        val cookiePtr: Long = createCookie()
        fun abort() {
            abortCookie(cookiePtr)
        }

        fun destroy() {
            destroyCookie(cookiePtr)
        }
    }

    @JvmName("setFileBuffer1")
    fun setFileBuffer(fileBuffer: ByteArray?) {
        this.fileBuffer = fileBuffer
    }

    @Synchronized
    fun getPageSize(page: Int): PointF {
        gotoPage(page)
        return PointF(pageWidth, pageHeight)
    }

    @Synchronized
    private fun countPagesSynchronized(): Int {
        return countPagesInternal()
    }

    @Synchronized
    fun searchPage(page: Int, text: String): Array<RectF> {
        gotoPage(page)
        return searchPage(text)
    }

    @Synchronized
    fun textLines(page: Int): Array<Array<TextWord>> {
        gotoPage(page)
        var chars: Array<Array<Array<Array<TextChar>>>>? = text()
        // The text of the page held in a hierarchy (blocks, lines, spans).
        // Currently we don't need to distinguish the blocks level or
        // the spans, and we need to collect the text into words.
        val lns: ArrayList<Array<TextWord>> = ArrayList<Array<TextWord>>()
        chars?.let {
            for (bl in it) {
                for (ln in bl) {
                    val wds: ArrayList<TextWord> = ArrayList<TextWord>()
                    var wd = TextWord()
                    for (sp in ln) {
                        for (tc in sp) {
                            if (tc.c !== ' ') {
                                wd.Add(tc)
                            } else if (wd.w.isNotEmpty()) {
                                wds.add(wd)
                                wd = TextWord()
                            }
                        }
                    }
                    if (wd.w.isNotEmpty()) wds.add(wd)
                    if (wds.size > 0) lns.add(wds.toArray(arrayOfNulls<TextWord>(wds.size)))
                }
            }
        }
        chars = null
        return lns.toTypedArray()
    }

    @Synchronized
    fun hasOutline(): Boolean {
        return hasOutlineInternal()
    }

    @Synchronized
    open fun getOutline(): Array<OutlineItem> {
        return getOutlineInternal()
    }

    fun readData(
        intent: Intent,
        intentType: String?,
        context: Context,
        result: (MuPDFCore, String?) -> Unit
    ) {
        val muPDFCore = MuPDFCore()
        var uri = intent.data
        var urlFile: String? = null
        var buffer: ByteArray? = null
        if (uri.toString().startsWith("content://")) {
            var reason: String? = null
            try {
                context.contentResolver.openInputStream(uri!!)?.let {
                    val len = it.available()
                    buffer = ByteArray(len)
                    it.read(buffer, 0, len)
                    it.close()
                }
            } catch (e: OutOfMemoryError) {
                reason = e.toString()
                Log.e("Reader_1", reason)
            } catch (e: Exception) {
                try {
                    val cursor =
                        context.contentResolver.query(uri!!, arrayOf("_data"), null, null, null)
                    if (cursor!!.moveToFirst()) {
                        val str = cursor.getString(0)
                        if (str == null) {
                            reason = context.getString(R.string.could_parse_data)
                            Log.e("Reader_2", reason)
                        } else {
                            uri = Uri.parse(str)
                        }
                    }
                } catch (e2: Exception) {
                    reason = e2.toString()
                    Log.e("Reader_3", reason)
                }
            }
            reason?.let {
                result(muPDFCore, urlFile)
                Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
                return
            }
        }
        buffer?.let {
            try {
                muPDFCore.openFileBuffer(
                    context,
                    it,
                    intentType ?: intent.type ?: "application/pdf"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
                Log.e("Reader_5", e.message.toString())
            } finally {
                result(muPDFCore, urlFile)
            }
        } ?: run {
            uri.let {
                urlFile = it?.encodedPath
                if (TextUtils.isEmpty(urlFile)) {
                    urlFile = uri.toString()
                }
                urlFile?.let { path ->
                    if (muPDFCore.globals == 0L) {
                        try {
                            muPDFCore.openFileFromPath(context, path)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            Log.e("Reader_5", ex.message.toString())
                        }
                    }
                    if (muPDFCore.globals == 0L) Toast.makeText(
                        context,
                        context.getString(R.string.cant_open_file),
                        Toast.LENGTH_SHORT
                    ).show()
                    result(muPDFCore, urlFile)
                    return
                }
            }
        }
    }

    fun countPages(): Int = if (numPages < 0) countPagesSynchronized().also { numPages = it } else numPages

}