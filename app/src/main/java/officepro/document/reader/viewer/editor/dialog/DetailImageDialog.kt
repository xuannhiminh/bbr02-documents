package officepro.document.reader.viewer.editor.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.DetailPageDialogBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailImageDialog( var imagePath: String?) : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: DetailPageDialogBinding? = null
    private val binding get() = _binding!!
    private var onConfirm: (() -> Unit)? = null
    private var isViewDestroyed = false
    private var isAdLoaded = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DetailPageDialogBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun getImageInfo(imagePath: String): Triple<String, String, String>? {
        try {
            val file = File(imagePath)
            if (!file.exists()) return null

            // ✅ Tên file
            val name = file.name

            // ✅ Dung lượng (làm tròn, không có số thập phân)
            val sizeInBytes = file.length()
            val sizeText = when {
                sizeInBytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.0f MB", sizeInBytes / (1024.0 * 1024))
                else -> String.format(Locale.getDefault(), "%.0f KB", sizeInBytes / 1024.0)
            }

            // ✅ Ngày tạo (dùng metadata hệ thống)
            val path = file.toPath()
            val attr = Files.readAttributes(path, BasicFileAttributes::class.java)
            val creationTime = Date(attr.creationTime().toMillis())
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val createdDate = dateFormat.format(creationTime)

            return Triple(name, sizeText, createdDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        isViewDestroyed = false
        imagePath?.let { path ->
            val info = getImageInfo(path)
            if (info != null) {
                val (name, size, date) = info

                binding.title.text = name
                binding.title.isSelected = true
                binding.tvFilename.text = name
                binding.tvFilename.isSelected = true
                binding.tvFileInfo.text = "$size | $date"
                binding.tvPath.text = path
                binding.tvPath.isSelected = true
                //binding.tvViewed.text = fileModel.timeRecent ?: "-"
                binding.tvSize.text = size
                binding.tvModified.text = date
            } else {
                binding.tvTitle.text = getString(R.string.image)
                binding.tvFileInfo.text = ""
            }
        } ?: run {
            binding.tvTitle.text = getString(R.string.image)
            binding.tvFileInfo.text = ""
        }

        if (Locale.getDefault().language == "ar") {
            binding.tvTitle.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        } else {
            binding.tvTitle.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        binding.starIcon.visibility = View.GONE
        binding.tvModified.visibility = View.GONE
        binding.tvModifiedLabel.visibility = View.GONE
    }
    override fun onCancel(dialog: DialogInterface) {
        // Chỉ cho cancel khi quảng cáo đã load
        if (!isAdLoaded) {

        } else {
            super.onCancel(dialog)
        }
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
        isViewDestroyed = true
        _binding = null
    }
    private fun initListener() {

    }
}
