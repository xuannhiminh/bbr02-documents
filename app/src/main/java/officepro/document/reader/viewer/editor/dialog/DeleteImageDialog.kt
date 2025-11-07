package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.DeleteDialogBinding
import java.io.File

class DeleteImageDialog( val imagePath: String?) : DialogFragment() {

    override fun getTheme(): Int = R.style.DialogStyle

    private var _binding: DeleteDialogBinding? = null
    private val binding get() = _binding!!

    private var onDeleted: (() -> Unit)? = null

    fun setOnDeletedListener(listener: () -> Unit) {
        onDeleted = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                requestFeature(Window.FEATURE_NO_TITLE)
                setBackgroundDrawableResource(android.R.color.transparent)
            }
            setCancelable(true)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DeleteDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOk.setOnClickListener {
            deleteFile()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun deleteFile() {
        imagePath?.let { path ->
            val file = File(path)
            if (!file.exists()) {
                Toast.makeText(requireContext(), getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
                dismiss()
                return
            }

            val deleted = try {
                file.delete()
            } catch (e: Exception) {
                false
            }

            if (deleted) {
                Toast.makeText(requireContext(), getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show()
                onDeleted?.invoke()
                dismiss()
            } else {
                Toast.makeText(requireContext(), getString(R.string.app_error), Toast.LENGTH_SHORT).show()
            }
        } ?: dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
            setDimAmount(0.5f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

