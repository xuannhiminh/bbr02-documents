package com.ezteam.ezpdflib.widget

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class MyLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    var isScrollEnabled = true

    override fun canScrollVertically(): Boolean {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return isScrollEnabled && super.canScrollHorizontally()
    }
}