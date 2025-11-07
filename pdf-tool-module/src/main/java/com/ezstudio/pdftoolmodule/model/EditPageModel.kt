package com.ezstudio.pdftoolmodule.model

import android.graphics.Bitmap

data class EditPageModel(
    var thumbnail: Bitmap,
    /**
     * index page in file ,if page add new position = -2
     * */
    var position: Int,
    var rotate: Int = 0,
    var btmEdit: Bitmap? = null,
    var isInvert: Boolean = false
)