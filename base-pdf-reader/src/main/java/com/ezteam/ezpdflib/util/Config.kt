package com.ezteam.ezpdflib.util

import android.graphics.Color
import com.ezteam.ezpdflib.activity.Mode

object Config {
    //    const val HIGHLIGHT_COLOR = -0x7fda8d54
    var HIGHLIGHT_COLOR = Color.parseColor("#66F90201")
    const val LINK_COLOR = -0x7f538ddb
    const val BOX_COLOR = -0xbbbb01
    const val INK_COLOR = -0x10000
    const val INK_THICKNESS = 10.0f

    val colorHexString = arrayListOf(
        "#FF0000",
        "#000000", "#c43030", "#971670", "#ffb683", "#cc7cff",
        "#2b51bc", "#217ca9", "#2ca9bf", "#59c19c", "#94109a", "#15b9b0",
        "#ee534f", "#f03d81", "#ab46bc", "#7e57c2", "#5e6ac0", "#28b6f6",
        "#25c6da", "#66bb6a", "#9ccc66", "#d4e056", "#ffee58",
        "#ffa827", "#ff7143", "#8c6e63", "#bdbdbd", "#78909c"
    )

    object PreferencesKey {
        const val inkValue = "INK_VALUE"
        const val unlineValue = "UNLINE_VALUE"
        const val highLighValue = "HIGHLIGHT_VALUE"
        const val strikeValue = "STRIKE_VALUE"
    }

    fun getPreferencesKeyByMode(mode: Mode?): String {
        return when (mode) {
            Mode.Ink -> PreferencesKey.inkValue
            Mode.Unline -> PreferencesKey.unlineValue
            Mode.HighLight -> PreferencesKey.highLighValue
            Mode.Strikeout -> PreferencesKey.strikeValue
            else -> ""
        }
    }

    object PdfLib {
        const val IMAGE_SCALE_TYPE_ASPECT_RATIO = "maintain_aspect_ratio"
        const val IMAGE_SCALE_TYPE_STRETCH = "stretch_image"
        const val PG_NUM_STYLE_PAGE_X_OF_N = "pg_num_style_page_x_of_n"
        const val PG_NUM_STYLE_X_OF_N = "pg_num_style_x_of_n"
        const val DEFAULT_PAGE_SIZE_TEXT = "DefaultPageSize"
        const val DEFAULT_PAGE_SIZE = "A4"
        const val DEFAULT_MASTER_PW = "PDF Converter"
    }

    object Constant {
        const val DATA_URI_PDF = "data uri pdf"
        const val DATA_MU_PDF_CORE = "data mupdf core"
        const val DATA_PAGE = "data page"
        const val SORT_TYPE = "sort_type"
        const val RATE_APP = "rate_app"
        const val COUNT_ACTION = "count_action"
        const val GET_START = "get start"
    }

    object IntentResult {
        const val SELECT_PAGE = 111
        const val SELECT_SIGNATURE = 993
        const val SELECT_IMAGE = 994
        const val UPDATE_FILE = 112
        const val PRINT_REQUEST = 1
    }
}