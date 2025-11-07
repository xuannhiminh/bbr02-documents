package officepro.document.reader.viewer.editor.model

import android.util.Log
import androidx.annotation.Keep
import androidx.room.*
import com.ezteam.baseproject.utils.DateUtils
import java.io.File
import java.io.Serializable
import java.math.BigDecimal

/**
 * Created by admin on 1/8/2018.
 */
@Keep
@Entity(tableName = "file")
class FileModel : Serializable {
    @ColumnInfo(name = "path")
    @PrimaryKey
    var path: String = ""

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "size")
    var size: String? = null

    @ColumnInfo(name = "date")
    var date: Long = 0

    @ColumnInfo(name = "image")
    var image = 0

    @ColumnInfo(name = "time")
    var time: String? = null

    @ColumnInfo(name = "isNoticed")
    var isNoticed = false

    @ColumnInfo(name = "timeAdd")
    var timeAdd: Long = 0

    @ColumnInfo(name = "timeRecent")
    var timeRecent: String? = null

    @ColumnInfo(name = "unixTimeRecent")
    var unixTimeRecent: Long = 0

    @ColumnInfo(name = "isFavorite")
    var isFavorite = false

    @ColumnInfo(name = "currentPage")
    var currentPage = -1

    @ColumnInfo(name = "isReadDone")
    var isReadDone = false

    @ColumnInfo(name = "isRecent")
    var isRecent = false

    @Ignore
    var isAds: Boolean = false

    @Ignore
    var fromDatabase: Boolean = true

    @Ignore
    var hasPassword: Boolean = false

    @ColumnInfo(name = "isSample")
    var isSample: Boolean = false

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

    val fileExtension: String
        get() {
            return if (path.contains(".")) {
                path.substring(path.lastIndexOf(".") + 1)
            } else {
                ""
            }
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