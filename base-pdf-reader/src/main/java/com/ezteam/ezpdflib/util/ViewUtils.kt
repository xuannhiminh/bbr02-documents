package com.ezteam.ezpdflib.util

import android.animation.Animator
import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo

object ViewUtils {
    @JvmStatic
    fun showView(isTopView: Boolean, viewShow: View?, duration: Long) {
        if (viewShow != null && !viewShow.isShown) {
            viewShow.visibility = View.VISIBLE
            YoYo.with(if (isTopView) Techniques.SlideInDown else Techniques.SlideInUp)
                    .duration(duration)
                    .playOn(viewShow)
        }
    }

    @JvmStatic
    fun hideView(isTopView: Boolean, viewHide: View?, duration: Long) {
        if (viewHide != null && viewHide.isShown) {
            YoYo.with(if (isTopView) Techniques.SlideOutUp else Techniques.SlideOutDown)
                    .duration(duration)
                    .onEnd { animator: Animator? -> viewHide.visibility = View.GONE }
                    .playOn(viewHide)
        }
    }

    @JvmStatic
    fun showViewBase(techniques: Techniques?, view: View?, duration: Long, delay: Long = 0) {
        view?.let {
            if (!it.isShown) {
                view.visibility = View.VISIBLE
                YoYo.with(techniques)
                        .duration(duration)
                        .delay(delay)
                        .playOn(view)
            }
        }
    }

    @JvmStatic
    fun showViewBase(techniques: Techniques?, view: View?, duration: Long) {
        showViewBase(techniques, view, duration, 0)
    }

    @JvmStatic
    fun hideViewBase(techniques: Techniques?, view: View?, duration: Long) {
        view?.let {
            if (it.isShown) {
                YoYo.with(techniques)
                        .duration(duration)
                        .onEnd { view.visibility = View.GONE }
                        .playOn(view)
            }
        }
    }
}