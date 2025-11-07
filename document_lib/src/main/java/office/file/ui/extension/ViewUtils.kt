package office.file.ui.extension

import android.animation.Animator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


fun View.layoutInflater(): LayoutInflater = LayoutInflater.from(this.context)

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun TextView.string() = text.toString()

suspend fun Context.downloadImage(url: String): Bitmap? = suspendCoroutine {
    try {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    it.resume(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    it.resumeWithException(Throwable("Image cleared"))
                }
            })
    } catch (e: Exception) {
    }
}

fun Int.percent(percent: Int): Int {
    return (this * percent) / 100
}

fun ViewGroup.inflate(@LayoutRes view: Int): View {
    return LayoutInflater.from(this.context).inflate(view, this, false)
}

fun View.inflate(@LayoutRes view: Int): View {
    return LayoutInflater.from(this.context).inflate(view, null, false)
}

fun Context.getLinearVerticalLayoutManager(
    reverseLayout: Boolean = false
): LinearLayoutManager {
    return LinearLayoutManager(this, LinearLayoutManager.VERTICAL, reverseLayout)
}

fun Context.getLinearHorizontalLayoutManager(
    reverseLayout: Boolean = false
): LinearLayoutManager {
    return LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, reverseLayout)
}

fun Context.getGridLayoutManager(
    spanCount: Int = 3
): GridLayoutManager {
    return GridLayoutManager(this, spanCount)
}

fun RecyclerView.setDivider(@DrawableRes drawableRes: Int) {
    val divider = DividerItemDecoration(
        this.context,
        DividerItemDecoration.VERTICAL
    )
    val drawable = ContextCompat.getDrawable(
        this.context,
        drawableRes
    )
    drawable?.let {
        divider.setDrawable(it)
        addItemDecoration(divider)
    }
}

fun Context.getDisplayMetrics(): DisplayMetrics {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return DisplayMetrics().apply {
        windowManager.defaultDisplay.getMetrics(this)
    }
}

fun Context.getDisplayWidth() = getDisplayMetrics().widthPixels

fun Context.getDisplayHeight() = getDisplayMetrics().heightPixels

fun View.setScaleWithDisplay(percent: Int) {
    val scaledWith = context.getDisplayWidth().percent(percent)
    layoutParams.width = scaledWith
}

fun View.setScaleHeightDisplay(percent: Int) {
    val scaledHeight = context.getDisplayHeight().percent(percent)
    layoutParams.height = scaledHeight
}

fun View.setScaleDisplay(percent: Int) {
    setScaleHeightDisplay(percent)
    setScaleWithDisplay(percent)
}

fun View.setEnable(value: Boolean){
    if(value){
        alpha = 1f
    } else {
        alpha = 0.5f
    }
    isEnabled = value
}

fun View.expand() {
    /*val matchParentMeasureSpec =
        View.MeasureSpec.makeMeasureSpec((parent as View).width, View.MeasureSpec.EXACTLY)
    val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    measure(matchParentMeasureSpec, wrapContentMeasureSpec)
    val targetHeight = measuredHeight

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    layoutParams.height = 1
    visibility = View.VISIBLE
    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            layoutParams.height =
                if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    // Expansion speed of 1dp/ms
    a.duration = ((targetHeight / context.resources.displayMetrics.density)).toLong()
    startAnimation(a)*/
    if (!this.isShown) {
        this.visibility = View.VISIBLE
        YoYo.with(Techniques.FadeInDown)
            .duration(200)
            .playOn(this)
    }
}

fun View.collapse() {
    /*val initialHeight = measuredHeight
    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1f) {
                visibility = View.GONE
            } else {
                layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    // Collapse speed of 1dp/ms
    a.duration = (initialHeight / context.resources.displayMetrics.density).toLong()
    startAnimation(a)*/
    if (this.isShown) {
        YoYo.with(Techniques.FadeOutUp)
            .duration(200)
            .onEnd { animator: Animator? -> this.visibility = View.GONE }
            .playOn(this)
    }
}

fun View.showUp() {
    if (!this.isShown) {
        this.visibility = View.VISIBLE
        YoYo.with(Techniques.FadeInUp)
            .duration(200)
            .playOn(this)
    }
}

fun View.hideDown() {
    if (this.isShown) {
        YoYo.with(Techniques.FadeOutDown)
            .duration(200)
            .onEnd { animator: Animator? -> this.visibility = View.GONE }
            .playOn(this)
    }
}