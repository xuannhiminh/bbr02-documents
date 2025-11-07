package com.ezstudio.pdftoolmodule.model

import android.graphics.Bitmap

data class PdfPageModel(
    var thumbnail: Bitmap,
    var page: Int,
    var selected: Boolean = false
)