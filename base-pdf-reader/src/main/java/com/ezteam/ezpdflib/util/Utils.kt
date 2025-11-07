package com.ezteam.ezpdflib.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.ezteam.ezpdflib.R
import java.io.File

object Utils {

    fun getDisplaySize(activity: Activity): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            // Optionally exclude system bars if you care about content area:
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            val bounds = windowMetrics.bounds
            val width = bounds.width() - insets.left - insets.right
            val height = bounds.height() - insets.top - insets.bottom
            width to height
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels to displayMetrics.heightPixels
        }
    }


    fun getWidthScreen(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun geHeightScreen(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun createSingleImageFromMultipleImages(firstImage: Bitmap, secondImage: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(
            firstImage.width,
            firstImage.height,
            firstImage.config ?: Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(firstImage, 0f, 0f, null)
        val paint2 = Paint()
        paint2.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(secondImage, 0f, 0f, paint2)
        return result
    }

    fun copyText(context: Context, text: String) {
        val currentApiVersion = Build.VERSION.SDK_INT
        if (currentApiVersion >= Build.VERSION_CODES.HONEYCOMB) {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("EzPdf", text))
        } else {
            val cm =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
            cm.text = text
        }
    }

    fun getHeightStatusBar(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    fun setStatusBarHomeTransparent(activity: FragmentActivity) {
        val window = activity.window
        window.navigationBarColor = Color.BLACK
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //make fully Android Transparent Status bar
        setWindowFlag(
            activity as AppCompatActivity,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            false
        )
        window.statusBarColor = Color.parseColor("#818181")
    }

    fun scanFile(context: Context, pathFile: String, listener: ((Unit) -> Unit)? = null) {
        val mediaScanIntent = Intent(
            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
        )
        val contentUri = Uri.fromFile(File(pathFile))
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
        MediaScannerConnection.scanFile(
            context, arrayOf(pathFile), null
        ) { path: String?, uri: Uri? ->
            listener?.invoke(Unit)
        }
    }

    fun setWindowFlag(activity: AppCompatActivity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

}