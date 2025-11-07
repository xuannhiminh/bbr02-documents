package office.file.ui.extension

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ezteam.ezpdflib.activity.PdfDetailActivity
import com.ezteam.ezpdflib.listener.BroadcastSubmodule
import office.file.ui.OpenFileActivity
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

fun isX86Device(): Boolean {
    val abi = System.getProperty("os.arch") ?: return false
    return abi.contains("x86", ignoreCase = true)
}

fun Activity.openDocuments(
    path: String,
    flag: Int = 0,
    allowEdit: Boolean = true,
    list: ArrayList<String> = arrayListOf(),
    copyString: String = "",
    lastPage: Int = 0,
    isFavorite: Boolean = false,
    isReadDone: Boolean = false
) {
    if (isX86Device()) {
        Toast.makeText(this, "Not support x86 device!", Toast.LENGTH_SHORT).show()
        return
    }

//    if (FilenameUtils.getExtension(path) == "pdf") {
//        PdfDetailActivity.start(this, path, isFavorite, isReadDone, allowEdit)
//    } else {
        val intent = Intent(this, OpenFileActivity::class.java)
        intent.setAction(Intent.ACTION_EDIT)
        intent.data = FileProvider.getUriForFile(
            this,
            "${this.packageName}.provider",
            File(path)
        )
        intent.putExtra("STARTED_FROM_EXPLORER", true)
        intent.putExtra("START_PAGE", flag)
        intent.putExtra("ALLOW_EDIT", allowEdit)
        intent.putExtra("LAST_PAGE", lastPage)
        if(allowEdit)
            sendBroadcast(
                Intent().apply {
                    action = BroadcastSubmodule.ACTION_RECENT
                    putExtra(BroadcastSubmodule.PATH, path)
                }
            )

        if (copyString != "") {
            intent.putExtra("content", copyString)
        }

        if (list.isNotEmpty()) {
            intent.putStringArrayListExtra("list_img", list)
        }
        Log.d("openDocuments", "startActivityForResult")
        this.startActivityForResult(intent, 1000)
//    }

}

fun Activity.setLocale(language: String) {
    val myLocale = Locale(language)
    val res = resources
    val dm = res.displayMetrics
    val conf = res.configuration
    conf.locale = myLocale
    res.updateConfiguration(conf, dm)
}

fun Context.getBaseImageFolder(): String {
    val path = this.getExternalFilesDir(null)?.path + File.separator + "images"
    val folder = File(path)
    if (!folder.exists()) {
        folder.mkdirs()
    }
    return folder.path
}

fun Context.getBaseDocumentFolder(): String {
    val path = this.getExternalFilesDir(null)?.path + File.separator + "documents"
    val folder = File(path)
    if (!folder.exists()) {
        folder.mkdirs()
    }
    return folder.path
}

fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}

fun Activity.isKeyboardOpened(): Boolean {
    val r = Rect()

    val activityRoot = getActivityRoot()
    val visibleThreshold = dip(100)

    activityRoot.getWindowVisibleDisplayFrame(r)

    val heightDiff = activityRoot.rootView.height - r.height()

    return heightDiff > visibleThreshold;
}

fun dip(value: Int): Int {
    return (value * Resources.getSystem().displayMetrics.density).toInt()
}

fun Activity.getActivityRoot(): View {
    return (findViewById<ViewGroup>(android.R.id.content)).getChildAt(0);
}

val type = "type"
val type_scanner = "type_scanner"
val type_ocr = "type_ocr"
val type_ocr_text = "type_ocr_text"

val language = "LANGUAGE"
val en = "en"
val es = "es"
val hi = "hi"

val lastRateDate = "WAS_RATE"


val fileDOC = "document-blank.docx"
val filePPT = "presentation-blank.pptx"
val fileXLS = "spreadsheet-blank.xlsx"

val openFavourite = "open_favourite"
val openRecent = "open_recent"

val RELOAD_FAVOURITE = "reload_favourite"
val RELOAD_CREATE = "reload_create"
val RELOAD_DELETE = "reload_delete"
val KEY_NAME = "name"
val SHOW_RATE = "show_rate"