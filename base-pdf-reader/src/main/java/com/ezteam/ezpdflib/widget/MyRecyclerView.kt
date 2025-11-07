package com.ezteam.ezpdflib.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class MyRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    private var lastClickTime: Long = 0
    private var pointDownX = 0f
    private var pointDownY = 0f
    var clickCount = 0

    private val tapPageMargin by lazy {
        val dm = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)
        var tmp = dm.xdpi.toInt()
        if (tmp < 100) tmp = 100
        if (tmp > dm.widthPixels / 5) tmp = dm.widthPixels / 5
        tmp
    }
    var touchListener: TouchListener? = null
    var canTouchAble = true
    var canTouchArea = true

    interface TouchListener {
        fun onClickNextPage()
        fun onClickPreviousPage()
        fun onClickMainArea()
        fun doubleClickMainArea()
    }

    init {
        initView()
    }

    private fun initView() {

    }

    private fun aVoidDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickTime < 200) {
            return true
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!canTouchAble)
            return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            pointDownX = event.x
            pointDownY = event.y
            clickCount++
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (abs(pointDownX - event.x) < 50
                && abs(pointDownY - event.y) < 50
            ) {
                when {
                    event.x < tapPageMargin -> {
                        if (!aVoidDoubleClick() && canTouchArea) {
                            touchListener?.onClickPreviousPage()
                        }
                        clickCount = 0
                    }
                    event.x > width - tapPageMargin -> {
                        if (!aVoidDoubleClick() && canTouchArea) {
                            touchListener?.onClickNextPage()
                        }
                        clickCount = 0
                    }
                    else -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (clickCount == 1) {
                                touchListener?.onClickMainArea()
                            } else if (clickCount == 2) {
                                touchListener?.doubleClickMainArea()
                            }
                            clickCount = 0
                        }, 150)
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        clickCount = 0
    }
}