package com.ezstudio.pdftoolmodule.utils.pdftool

import androidx.annotation.Keep
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Font

@Keep
data class WatermarkModel(
    var watermarkText: String? = null,
    var fileName: String? = null,
    var rotationAngle: Float = 0f,
    var textColor: BaseColor? = null,
    var textSize: Float = 0f,
    var fontFamily: Font.FontFamily? = null,
    var fontStyle: Int = 0,
)