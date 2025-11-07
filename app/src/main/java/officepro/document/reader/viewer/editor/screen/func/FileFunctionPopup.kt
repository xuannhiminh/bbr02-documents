package officepro.document.reader.viewer.editor.screen.func

import android.graphics.Color
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.FunctionState
import officepro.document.reader.viewer.editor.model.FileModel
import androidx.core.graphics.drawable.toDrawable
import com.ezteam.baseproject.listener.EzItemListener

class FileFunctionPopup(
    private val context: Context,
    private val fileModel: FileModel,
    public var functionListener: ((FunctionState) -> Unit)? = null
) {
    private var popupWindow: PopupWindow? = null

    fun show(anchor: View) {
        val popupView = LayoutInflater.from(context)
            .inflate(R.layout.select_file_dialog_2, null)

        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 16f // bóng đổ
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            isOutsideTouchable = true
        }

        // --- Bind view ---
        val funcDetail = popupView.findViewById<View>(R.id.func_detail_file)
        val funcRename = popupView.findViewById<View>(R.id.func_rename)
        val funcShare = popupView.findViewById<View>(R.id.func_share)
        val funcDelete = popupView.findViewById<View>(R.id.func_delete)

        funcDetail?.setOnClickListener {
            functionListener?.invoke(FunctionState.DETAIL)
            dismiss()
        }
        funcRename?.setOnClickListener {
            functionListener?.invoke(FunctionState.RENAME)
            dismiss()
        }
        funcShare?.setOnClickListener {
            functionListener?.invoke(FunctionState.SHARE)
            dismiss()
        }
        funcDelete?.setOnClickListener {
            functionListener?.invoke(FunctionState.DELETE)
            dismiss()
        }

        // --- Tính toán vị trí hiển thị ---
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        popupView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        val popupWidth = popupView.measuredWidth
        val popupHeight = popupView.measuredHeight

        var offsetX = -80
        var offsetY = 0

        if (anchorX + popupWidth > screenWidth) {
            offsetX = -popupWidth + anchor.width - 60
        }
        if (anchorY + anchor.height + popupHeight > screenHeight) {
            offsetY = -popupHeight - anchor.height
        }

        popupWindow?.showAsDropDown(anchor, offsetX, offsetY)
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }
}