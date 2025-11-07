package com.ezteam.ezpdflib.activity.outline

import com.ezteam.nativepdf.OutlineItem

class SingleOutline private constructor() {

    var lstOutline: Array<OutlineItem>? = null

    private object Holder {
        val INSTANCE = SingleOutline()
    }

    companion object {
        @JvmStatic
        fun getInstance(): SingleOutline {
            return Holder.INSTANCE
        }
    }
}