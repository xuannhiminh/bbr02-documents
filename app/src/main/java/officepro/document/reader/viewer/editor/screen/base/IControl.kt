package office.pdf.document.reader.viewer.editor.screen.base

import android.net.Uri
import com.google.android.gms.ads.nativead.NativeAd
import office.pdf.document.reader.viewer.editor.model.FileModel

interface IControl {
    fun shareFile(fileModel: FileModel)

    fun showDialogConfirm(title: String, message: String, onConfirm: () -> Unit)

    fun openFile(fileModel: FileModel)

    fun openFile(uri: Uri)

    fun showRenameFile(fileName: String, listener: (String) -> Unit)

    fun openAppOnStore()

    fun sendFeedback()

    fun shareApp()

    fun showAppRating(isHasShow: Boolean, complete: () -> Unit)

}