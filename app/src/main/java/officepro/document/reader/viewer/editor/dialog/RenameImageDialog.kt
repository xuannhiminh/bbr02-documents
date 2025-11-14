package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.InputNameDialogBinding
import officepro.document.reader.viewer.editor.screen.setting.FeedBackSucessDialog
import java.io.File

class RenameImageDialog(private val imagePath: String?) : DialogFragment() {

    override fun getTheme(): Int = R.style.DialogStyle

    private var _binding: InputNameDialogBinding? = null
    private val binding get() = _binding!!

    private var isViewDestroyed = false
    private var isAdLoaded = false

    private var onRenamed: (() -> Unit)? = null

    fun setOnRenamedListener(listener: () -> Unit) {
        onRenamed = listener
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
        _binding = InputNameDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false

        imagePath?.let { path ->
            val file = File(path)
            if (!file.exists()) {
                Toast.makeText(requireContext(), getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
                dismiss()
                return
            }

            // Gợi ý tên cũ (không có đuôi)
            val oldName = file.nameWithoutExtension
            val ext = file.extension

            binding.editInputName.setText(oldName)
            binding.editInputName.selectAll()

            binding.btnOk.setOnClickListener {
                val newName = binding.editInputName.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.enter_file_name), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newFile = File(file.parent, "$newName.$ext")

                if (newFile.exists()) {
                    Toast.makeText(requireContext(), getString(R.string.file_already_exists), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val success = file.renameTo(newFile)
                if (success) {
                    Toast.makeText(requireContext(), getString(R.string.rename_success), Toast.LENGTH_SHORT).show()
                    onRenamed?.invoke()
                    dismiss()
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.rename_unsuccessful), Toast.LENGTH_SHORT).show()
                }
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }

        } ?: run {
            Toast.makeText(requireContext(), getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
            dismiss()
        }

        // Nếu có quảng cáo thì load
        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
        } else {
            binding.layoutNative.visibility = View.GONE
        }
    }

    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }

        val safeContext = context ?: return
        if (!SystemUtils.isInternetAvailable(safeContext)) {
            binding.layoutNative.visibility = View.GONE
            isAdLoaded = true
            dialog?.setCancelable(true)
            dialog?.setCanceledOnTouchOutside(true)
            return
        }

        isAdLoaded = false
        binding.layoutNative.visibility = View.VISIBLE
        val loadingView = LayoutInflater.from(safeContext)
            .inflate(R.layout.ads_native_loading_short, null)
        binding.layoutNative.removeAllViews()
        binding.layoutNative.addView(loadingView)

        val callback = object : NativeCallback() {
            override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                super.onNativeAdLoaded(nativeAd)
                if (isViewDestroyed || !isAdded || _binding == null) return
                val adView = LayoutInflater.from(safeContext)
                    .inflate(R.layout.ads_native_bot_no_media_short, null) as NativeAdView
                binding.layoutNative.removeAllViews()
                binding.layoutNative.addView(adView)
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)

                isAdLoaded = true
                dialog?.setCancelable(true)
                dialog?.setCanceledOnTouchOutside(true)
            }

            override fun onAdFailedToLoad() {
                super.onAdFailedToLoad()
                if (isViewDestroyed || !isAdded || _binding == null) return
                binding.layoutNative.visibility = View.GONE
                isAdLoaded = true
                dialog?.setCancelable(true)
                dialog?.setCanceledOnTouchOutside(true)
            }
        }

        Admob.getInstance().loadNativeAd(
            safeContext.applicationContext,
            getString(R.string.native_popup_all),
            callback
        )
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
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewDestroyed = true
        _binding = null
    }
}

