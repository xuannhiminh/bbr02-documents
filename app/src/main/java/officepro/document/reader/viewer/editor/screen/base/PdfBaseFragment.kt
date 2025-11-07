package officepro.document.reader.viewer.editor.screen.base

import android.net.Uri
import androidx.viewbinding.ViewBinding
import officepro.document.reader.viewer.editor.model.FileModel
import com.ezteam.baseproject.fragment.BaseFragment

abstract class PdfBaseFragment<B: ViewBinding> : BaseFragment<B>(), IControl {

    override fun shareFile(fileModel: FileModel) {
        (requireActivity() as PdfBaseActivity<*>).shareFile(fileModel)
    }

    override fun showDialogConfirm(title: String, message: String, onConfirm: () -> Unit) {
        (requireActivity() as PdfBaseActivity<*>).showDialogConfirm(title, message, onConfirm)
    }

//    open fun showDetailFile(fileModel: FileModel) {
//        (requireActivity() as PdfBaseActivity<*>).showDetailFile(fileModel)
//    }

    override fun openFile(fileModel: FileModel) {
        (requireActivity() as PdfBaseActivity<*>).openFile(fileModel)
    }

    override fun openFile(uri: Uri) {
        (requireActivity() as PdfBaseActivity<*>).openFile(uri)
    }

    override fun showRenameFile(fileName: String, listener: (String) -> Unit) {
        (requireActivity() as PdfBaseActivity<*>).showRenameFile(fileName, listener)
    }

    override fun openAppOnStore() {
        (requireActivity() as PdfBaseActivity<*>).openAppOnStore()
    }

    override fun sendFeedback() {
        (requireActivity() as PdfBaseActivity<*>).sendFeedback()
    }

    override fun shareApp() {
        (requireActivity() as PdfBaseActivity<*>).shareApp()
    }

    override fun showAppRating(isHasShow: Boolean, complete: () -> Unit) {
        (requireActivity() as PdfBaseActivity<*>).showAppRating(isHasShow, complete)
    }
}