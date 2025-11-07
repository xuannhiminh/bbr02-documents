package com.ezteam.nativepdf

import android.graphics.RectF
import androidx.annotation.Keep

@Keep
class Annotation(x0: Float, y0: Float, x1: Float, y1: Float, _type: Int) : RectF(x0, y0, x1, y1) {
    enum class Type {
        TEXT, LINK, FREETEXT, LINE, SQUARE, CIRCLE, POLYGON, POLYLINE, HIGHLIGHT, UNDERLINE,
        SQUIGGLY, STRIKEOUT, STAMP, CARET, INK, POPUP, FILEATTACHMENT, SOUND, MOVIE, WIDGET,
        SCREEN, PRINTERMARK, TRAPNET, WATERMARK, A3D, UNKNOWN
    }

    val type: Type = if (_type == -1) Type.UNKNOWN else Type.values()[_type]

}