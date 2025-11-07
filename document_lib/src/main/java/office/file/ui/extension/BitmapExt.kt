package office.file.ui.extension

import android.graphics.*
import android.graphics.Bitmap





//* @param bmp input bitmap
//* @param contrast 0..10 1 is default
//* @param brightness -255..255 0 is default
//* @return new bitmap


//fun Bitmap.adjustedContrast(contrast: Float, brightness: Float = 0f): Bitmap? {
//    val cm = ColorMatrix(
//        floatArrayOf(
//            contrast,
//            0f,
//            0f,
//            0f,
//            brightness,
//            0f,
//            contrast,
//            0f,
//            0f,
//            brightness,
//            0f,
//            0f,
//            contrast,
//            0f,
//            brightness,
//            0f,
//            0f,
//            0f,
//            1f,
//            0f
//        )
//    )
//
//    val ret = Bitmap.createBitmap(getWidth(), getHeight(), getConfig())
//
//    val canvas = Canvas(ret)
//
//    val paint = Paint()
//    paint.colorFilter = ColorMatrixColorFilter(cm)
//    canvas.drawBitmap(this, 0f, 0f, paint)
//
//    return ret
//}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.flip(): Bitmap {
    val matrix = Matrix()
    matrix.postScale(-1f, 1f, width / 2f, height / 2f);
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)

}

