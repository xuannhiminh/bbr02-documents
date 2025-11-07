package com.ezteam.ezpdflib.extension

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun Uri.uriToBitmap(context: Context): Bitmap {
//    return MediaStore.Images.Media.getBitmap(context.contentResolver, this)
    val input = context.contentResolver.openInputStream(this)
    val bitmap = BitmapFactory.decodeStream(input)
    input?.close()
    return bitmap
}

fun Bitmap.bitmapToUri(context: Context): Uri {
    /**Will create image in device*/
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path: String =
        MediaStore.Images.Media.insertImage(
            context.contentResolver,
            this,
            "image_" + System.currentTimeMillis(),
            null
        )
    return Uri.parse(path)
}

fun Bitmap.bitmapToUriCache(context: Context): Uri {
    /**Only create image in cache application*/
    var saveFile =
        File(context.cacheDir.path, System.currentTimeMillis().toString())
    try {
        val fos = FileOutputStream(saveFile)
        compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return saveFile.toUri()
}

fun Bitmap.bitmapToFileCachePng(context: Context): File {
    /**Only create image in cache application*/
    val saveFile =
        File(context.cacheDir.path, "${System.currentTimeMillis()}.png")
    try {
        val fos = FileOutputStream(saveFile)
        compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return saveFile
}

fun Bitmap.bitmapToUriCacheLow(context: Context): Uri {
    /**Only create image in cache application*/
    val saveFile =
        File(context.cacheDir.path, "${System.currentTimeMillis()}.jpg")
    try {
        val fos = FileOutputStream(saveFile)
        compress(Bitmap.CompressFormat.JPEG, 80, fos)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return saveFile.toUri()
}

fun Bitmap.scaleBitmap(widthScale: Float, heightScale: Float): Bitmap {
    val m = Matrix()
    m.setRectToRect(
        RectF(0f, 0f, width.toFloat(), height.toFloat()), RectF(
            0f, 0f,
            widthScale,
            heightScale
        ), Matrix.ScaleToFit.CENTER
    )
    return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
}

fun Bitmap.rotate(orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_NORMAL -> return this
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
            matrix.setRotate(180f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
        else -> return this
    }
    val bmRotated =
        Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, false)
    this.recycle()
    return bmRotated
}

fun Bitmap.toByteArray(): ByteArray? {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val byteArray = stream.toByteArray()
    this.recycle()
    return byteArray
}

fun Bitmap.padingBitmap(sizePading: Int): Bitmap {
    val result = Bitmap.createBitmap(
        this.width + (2 * sizePading),
        this.height + (2 * sizePading),
        this.config ?: Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(result)
    canvas.drawColor(Color.WHITE)
    canvas.drawBitmap(this, sizePading.toFloat(), sizePading.toFloat(), null)
    return result
}

fun Bitmap.resizeBitmapByCanvas(imageViewWidth: Float, imageViewHeight: Float): Bitmap {
    val width: Float
    val heigth: Float
    val orginalWidth = this.width.toFloat()
    val orginalHeight = this.height.toFloat()
    if (orginalWidth > orginalHeight) {
        width = imageViewWidth
        heigth = imageViewWidth * orginalHeight / orginalWidth
    } else {
        heigth = imageViewHeight
        width = imageViewHeight * orginalWidth / orginalHeight
    }
    if (width > orginalWidth || heigth > orginalHeight) {
        return this
    }
    val background = Bitmap.createBitmap(
        width.toInt(),
        heigth.toInt(), Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(background)
    val scale = width / orginalWidth
    val yTranslation = (heigth - orginalHeight * scale) / 2.0f
    val transformation = Matrix()
    transformation.postTranslate(0.0f, yTranslation)
    transformation.preScale(scale, scale)
    val paint = Paint()
    paint.isFilterBitmap = true
    canvas.drawBitmap(this, transformation, paint)
    return background
}


fun Bitmap.createBitmapQuanlity(quality: Int): Bitmap {
    val newWidth: Int
    val newHeight: Int
    if (width >= height) {
        val newH = quality * height / width
        newWidth = quality
        newHeight = newH
    } else {
        val newW = quality * width / height
        newWidth = newW
        newHeight = quality
    }
    val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
    val ratioX: Float = newWidth / this.width.toFloat()
    val ratioY: Float = newHeight / this.height.toFloat()
    val middleX: Float = newWidth / 2.0f
    val middleY: Float = newHeight / 2.0f

    val scaleMatrix = Matrix()
    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

    val canvas = Canvas(scaledBitmap)
    canvas.setMatrix(scaleMatrix)
    canvas.drawBitmap(
        this,
        middleX - this.width / 2,
        middleY - this.height / 2,
        Paint(Paint.FILTER_BITMAP_FLAG)
    )
    return scaledBitmap
}

fun Bitmap.reverstBitmap(): Bitmap {
    val height = this.height
    val width = this.width
    val bitmap = Bitmap.createBitmap(width, height, this.config ?: Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    val matrixGrayscale = ColorMatrix()
    val matrixInvert = ColorMatrix()
    matrixInvert.set(
        floatArrayOf(
            -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
            0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
            0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        )
    )
    matrixInvert.preConcat(matrixGrayscale)
    val filter = ColorMatrixColorFilter(matrixInvert)
    paint.colorFilter = filter
    canvas.drawBitmap(this, 0f, 0f, paint)
    return bitmap
}




