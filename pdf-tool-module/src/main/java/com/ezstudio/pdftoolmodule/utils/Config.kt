package com.ezstudio.pdftoolmodule.utils

import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.model.Function
import com.itextpdf.text.Font

object Config {

    val lstFunction = fun(): MutableList<Function> {
        return mutableListOf(
            Function(
                FUCNID.MERGE.id,
                R.string.merge_pdf,
                R.drawable.ic_tool_merge,
                R.color.color_77CEFF
            ),
            Function(
                FUCNID.SPLIT.id,
                R.string.split_pdf,
                R.drawable.ic_tool_split,
                R.color.color_C7F3A4
            ),
            Function(
                FUCNID.INVERT.id,
                R.string.invert_pdf,
                R.drawable.ic_tool_invert,
                R.color.color_FDA7FF
            ),
            Function(
                FUCNID.REMOVE_DUPLICATE.id,
                R.string.remove_duplicate_page,
                R.drawable.ic_tool_remove_page,
                R.color.color_FF9898
            ),
            Function(
                FUCNID.ADD_PASSWORD.id,
                R.string.add_password,
                R.drawable.ic_tool_lock,
                R.color.color_8DE8D2
            ),
            Function(
                FUCNID.REMOVE_PASSWORD.id,
                R.string.remove_password,
                R.drawable.ic_tool_unlock,
                R.color.color_FFC759
            ),
            Function(
                FUCNID.ROTATE_PAGE.id,
                R.string.rotate_pages,
                R.drawable.ic_tool_rotate,
                R.color.color_77CEFF
            ),
            Function(
                FUCNID.ADD_WATERMARK.id,
                R.string.add_watermark,
                R.drawable.ic_tool_add_watermark,
                R.color.color_C7F3A4
            ),
            Function(
                FUCNID.ADD_IMAGE.id,
                R.string.add_image,
                R.drawable.ic_tool_add_image,
                R.color.color_FDA7FF
            ),
            Function(
                FUCNID.IMAGE_TO_PDF.id,
                R.string.image_to_pdf,
                R.drawable.ic_tool_image_pdf,
                R.color.color_FF9898
            ),
            Function(
                FUCNID.EXTRACT_IMAGE.id,
                R.string.extract_image,
                R.drawable.ic_tool_extract,
                R.color.color_FFC759
            )/*,
            Function(
                FUCNID.ORGANIZE_PAGES.id,
                R.string.organize_pages,
                R.drawable.ic_tool_organize_page,
                R.color.color_8DE8D2
            )*/
        )
    }

    enum class FUCNID(var id: Int) {
        MERGE(0), SPLIT(1), INVERT(2), REMOVE_DUPLICATE(3), ADD_PASSWORD(4),
        REMOVE_PASSWORD(5), ROTATE_PAGE(6), ADD_WATERMARK(7), ADD_IMAGE(8),
        IMAGE_TO_PDF(9), ORGANIZE_PAGES(10), EXTRACT_IMAGE(11)
    }

    const val MASTER_PASSWORD = "EzMobile"

    fun getStyleValueFromName(name: String?): Int {
        return when (name) {
            "NORMAL" -> Font.NORMAL
            "BOLD" -> Font.BOLD
            "ITALIC" -> Font.ITALIC
            "UNDERLINE" -> Font.UNDERLINE
            "STRIKETHRU" -> Font.STRIKETHRU
            "BOLDITALIC" -> Font.BOLDITALIC
            else -> Font.NORMAL
        }
    }
}