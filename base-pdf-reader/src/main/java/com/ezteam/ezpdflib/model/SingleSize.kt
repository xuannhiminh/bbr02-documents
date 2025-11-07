package com.ezteam.ezpdflib.model

class SingleSize private constructor() {

    var screenW = 0
    var screenH = 0
    var pointX = 0.0f
    var pointY = 0.0f
    var pageWidth = 0.0f
    var pageHeight = 0.0f

    private object Holder {
        val INSTANCE = SingleSize()
    }

    companion object {
        @JvmStatic
        fun getInstance(): SingleSize {
            return Holder.INSTANCE
        }
    }
}