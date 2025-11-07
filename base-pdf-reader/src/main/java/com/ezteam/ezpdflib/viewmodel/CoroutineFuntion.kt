package com.ezteam.ezpdflib.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import androidx.annotation.Keep
import com.ezteam.nativepdf.MuPDFCore
import com.ezteam.ezpdflib.extension.bitmapToUriCacheLow
import com.ezteam.ezpdflib.extension.reverstBitmap
import com.ezteam.ezpdflib.model.SingleSize
import com.ezteam.ezpdflib.util.Config
import com.ezteam.ezpdflib.util.PreferencesKey
import com.ezteam.ezpdflib.util.PreferencesUtils
import kotlinx.coroutines.*

@Keep
class CoroutineFuntion(var scope: CoroutineScope, private var application: Application) {

    companion object {
        private const val BYTES_PER_PIXEL = 4
        private const val MAX_BITMAP_BUDGET_BYTES = 100 * 1024 * 1024 // 100 MB

        private fun clampToBudget(width: Int, height: Int): Pair<Int, Int> {
            val maxPixels = MAX_BITMAP_BUDGET_BYTES / BYTES_PER_PIXEL
            val area = width.toLong() * height.toLong()
            if (area <= maxPixels) return width to height
            val scale = kotlin.math.sqrt(maxPixels.toDouble() / area.toDouble())
            val w = (width * scale).toInt().coerceAtLeast(1)
            val h = (height * scale).toInt().coerceAtLeast(1)
            return w to h
        }
    }

    fun loadPage(
        muPDFCore: MuPDFCore,
        page: Int,
        recf: Array<RectF>? = null,
        success: (Int, Uri) -> Unit
    ) {
        val cookie = muPDFCore.Cookie()
        val sizeX: Int = SingleSize.getInstance().screenW
        val sizeY: Int = SingleSize.getInstance().screenH
        val point = muPDFCore.getPageSize(page)
        val ratio = point.x / point.y
        val sizeDrawX: Float
        val sizeDrawY: Float
        if (ratio < 1.0f) {
            sizeDrawY = sizeY.toFloat()
            sizeDrawX = sizeDrawY * ratio
        } else {
            sizeDrawX = sizeX.toFloat()
            sizeDrawY = sizeDrawX / ratio
        }
        val scaleHeight: Float = sizeDrawY / point.y
        val scaleWidth: Float = sizeDrawX / point.x
        try {
            var entireBm: Bitmap? =
                Bitmap.createBitmap(sizeDrawX.toInt(), sizeDrawY.toInt(), Bitmap.Config.ARGB_8888)
            entireBm?.let {
                it.eraseColor(0)
                muPDFCore.drawPage(
                    it,
                    page,
                    sizeDrawX.toInt(),
                    sizeDrawY.toInt(),
                    0,
                    0,
                    sizeDrawX.toInt(),
                    sizeDrawY.toInt(),
                    cookie
                )
                val nightMode = PreferencesUtils.getBoolean(
                    PreferencesKey.KeyPress.PDF_VIEWER_NIGHT_MODE
                )
                val canvas = Canvas(it)
                recf?.let {
                    val paint = Paint()
                    paint.color = Config.HIGHLIGHT_COLOR
                    for (rect in it) canvas.drawRect(
                        rect.left * scaleWidth, rect.top * scaleHeight,
                        rect.right * scaleWidth, rect.bottom * scaleHeight,
                        paint
                    )
                }

                val data = if (nightMode) {
                    it.reverstBitmap().bitmapToUriCacheLow(application)
                } else {
                    it.bitmapToUriCacheLow(application)
                }
                it.recycle()
                entireBm = null
                cookie.destroy()
                success(page, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
    }

    fun updatePage(
        muPDFCore: MuPDFCore,
        page: Int,
        zoom: Float = 1f,
        success: (Int, Uri) -> Unit
    ) {
        val cookie = muPDFCore.Cookie()
        val point = muPDFCore.getPageSize(page)
        val ratio = point.x / point.y.toFloat()

        // Compute the base screen-fit size (same as loadPage)
        val screenW = SingleSize.getInstance().screenW.toFloat()
        val screenH = SingleSize.getInstance().screenH.toFloat()
        val baseDrawX: Float
        val baseDrawY: Float
        if (ratio < 1.0f) {
            baseDrawY = screenH
            baseDrawX = baseDrawY * ratio
        } else {
            baseDrawX = screenW
            baseDrawY = baseDrawX / ratio
        }

        // Apply zoom factor
        var targetWidth = (baseDrawX * zoom).toInt()
        var targetHeight = (baseDrawY * zoom).toInt()

        val (cappedW, cappedH) = clampToBudget(targetWidth, targetHeight)
        targetWidth = cappedW
        targetHeight = cappedH

        var entireBm: Bitmap? = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        entireBm?.let {
            it.eraseColor(0)
            muPDFCore.updatePage(
                it,
                page,
                targetWidth,
                targetHeight,
                0,
                0,
                targetWidth,
                targetHeight,
                cookie
            )
            it.recycle()
            entireBm = null
            cookie.destroy()
            loadZoomedPage(muPDFCore, page,zoom, success = { page, uri ->
                success(page, uri)
            })
        }

    }

    /**
     * Render the full page at a higher resolution based on zoom factor (e.g., 2x, 3x)
     * and return a Uri to the bitmap. This avoids blurry scaling by asking MuPDF
     * to draw the page at enlarged size.
     */
    fun loadZoomedPage(
        muPDFCore: MuPDFCore,
        page: Int,
        zoom: Float,
        recf: Array<RectF>? = null, // optional highlights in PDF coords
        success: (Int, Uri) -> Unit
    ) {
        Log.d("loadZoomedPage", "zoom: $zoom")
        if (zoom <= 1f) {
            // fallback to normal size
            loadPage(muPDFCore, page, recf, success)
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                val cookie = muPDFCore.Cookie()

                // Get PDF intrinsic size
                val point = muPDFCore.getPageSize(page)
                val ratio = point.x / point.y.toFloat()

                // Compute the base screen-fit size (same as loadPage)
                val screenW = SingleSize.getInstance().screenW.toFloat()
                val screenH = SingleSize.getInstance().screenH.toFloat()
                val baseDrawX: Float
                val baseDrawY: Float
                if (ratio < 1.0f) {
                    baseDrawY = screenH
                    baseDrawX = baseDrawY * ratio
                } else {
                    baseDrawX = screenW
                    baseDrawY = baseDrawX / ratio
                }

                // Apply zoom factor
                var targetWidth = (baseDrawX * zoom).toInt()
                var targetHeight = (baseDrawY * zoom).toInt()

                val (cappedW, cappedH) = clampToBudget(targetWidth, targetHeight)
                targetWidth = cappedW
                targetHeight = cappedH

                // Allocate and render
                var bmp: Bitmap? = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                bmp?.let {
                    it.eraseColor(0)
                    muPDFCore.drawPage(
                        it,
                        page,
                        targetWidth,
                        targetHeight,
                        0,
                        0,
                        targetWidth,
                        targetHeight,
                        cookie
                    )

                    // Overlay highlights if provided (they are in PDF coords)
                    recf?.let { rects ->
                        val canvas = Canvas(it)
                        val paint = Paint().apply {
                            style = Paint.Style.FILL
                            color = Config.HIGHLIGHT_COLOR
                        }
                        // Compute scale from PDF coords to this zoomed bitmap
                        val scaleX = targetWidth / point.x.toFloat()
                        val scaleY = targetHeight / point.y.toFloat()
                        for (r in rects) {
                            canvas.drawRect(
                                r.left * scaleX,
                                r.top * scaleY,
                                r.right * scaleX,
                                r.bottom * scaleY,
                                paint
                            )
                        }
                    }

                    // Cache to Uri (respect night mode)
                    val nightMode = PreferencesUtils.getBoolean(PreferencesKey.KeyPress.PDF_VIEWER_NIGHT_MODE)
                    val data = if (nightMode) {
                        it.reverstBitmap().bitmapToUriCacheLow(application)
                    } else {
                        it.bitmapToUriCacheLow(application)
                    }
                    it.recycle()
                    bmp = null
                    cookie.destroy()

                    // Callback on main thread
                    withContext(Dispatchers.Main) {
                        success(page, data)
                    }
                }
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}