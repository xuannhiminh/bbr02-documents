package com.ezteam.ezpdflib.model

import com.ezteam.nativepdf.TextWord

class SingleTextword private constructor() {

    var textword: Array<Array<TextWord>>? = null

    private object Holder {
        val INSTANCE = SingleTextword()
    }

    companion object {
        @JvmStatic
        fun getInstance(): SingleTextword {
            return Holder.INSTANCE
        }
    }
}