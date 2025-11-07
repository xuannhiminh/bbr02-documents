package com.ezteam.ezpdflib.model

import com.ezteam.nativepdf.Annotation

class SingleAnnotation private constructor() {

    var annotation: Array<Annotation>? = null

    private object Holder {
        val INSTANCE = SingleAnnotation()
    }

    companion object {
        @JvmStatic
        fun getInstance(): SingleAnnotation {
            return Holder.INSTANCE
        }
    }
}