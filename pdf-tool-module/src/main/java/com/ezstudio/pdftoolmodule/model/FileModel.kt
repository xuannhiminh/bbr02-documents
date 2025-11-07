package com.ezstudio.pdftoolmodule.model

import android.util.Log
import androidx.annotation.Keep
import com.ezteam.baseproject.utils.DateUtils
import java.io.File
import java.io.Serializable
import java.math.BigDecimal

/**
 * Created by admin on 1/8/2018.
 */
@Keep
class FileModel : Serializable {
    var path: String = ""
    var name: String? = null
    var size: String? = null
    var date: Long = 0
    var image = 0
    var time: String? = null
    var timeAdd: Long = 0
    var timeRecent: String? = null
    var isFavorite = false
    var isRecent = false
    var isAds: Boolean = false
    var fromDatabase: Boolean = true
    var hasPassword: Boolean = false

    val sizeString: String
        get() = if (size == null) {
            ""
        } else getFileLength(size!!.toDouble())

    val sizeDouble: Double
        get() = size!!.toDouble()
    val file: File
        get() = File(path)
    val infoDetail: String
        get() {
            val stringBuilder = StringBuilder()
            stringBuilder.append("File Name: $name")
            stringBuilder.append("\n\n")
            stringBuilder.append("Path: $path")
            stringBuilder.append("\n\n")
            stringBuilder.append(
                "Last modified: " + DateUtils.longToDateString(
                    date, DateUtils.DATE_FORMAT_5
                )
            )
            stringBuilder.append("\n\n")
            stringBuilder.append("Size: $sizeString")
            return stringBuilder.toString()
        }

    companion object {
        fun getFileLength(paramDouble: Double): String {
            val d1 = paramDouble / 1024.0
            val d2 = d1 / 1024.0
            val d3 = d2 / 1024.0
            if (paramDouble < 1024.0) {
                return "$paramDouble bytes"
            }
            if (d1 < 1024.0) {
                return BigDecimal(d1).setScale(2, 4).toString() + " kb"
            }
            return if (d2 < 1024.0) {
                BigDecimal(d2).setScale(2, 4).toString() + " mb"
            } else BigDecimal(d3).setScale(2, 4).toString() + " gb"
        }

        fun getInstanceFromUrl(url: String): FileModel {
            Log.e("XXXX", "getInstanceFromUrl: $url")
            val fileInfo = FileModel()
            val file = File(url)
            fileInfo.path = url
            fileInfo.name = file.name
            fileInfo.size = file.length().toString() + ""
            fileInfo.date = file.lastModified()
            return fileInfo
        }
    }
}