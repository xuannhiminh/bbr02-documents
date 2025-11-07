package officepro.document.reader.viewer.editor.screen.create

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetDialog
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.BottomSheetCreatePdfBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob

class BottomSheetCreatePdf(
    var complete: (fileName: String, password: String, size: String) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetCreatePdfBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetCreatePdfBinding.inflate(inflater, container, false)
        return binding.root
    }
    private var isAdLoaded = false
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
        } else {
            Log.d("BottomSheetCreatePdf", "Not load Ads")
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
            onAdLoadFinished()
            return
        }

        binding.layoutNative.visibility = View.VISIBLE
        val loadingView = LayoutInflater.from(safeContext)
            .inflate(com.ezteam.ezpdflib.R.layout.ads_native_loading_short, null)
        binding.layoutNative.removeAllViews()
        binding.layoutNative.addView(loadingView)

        val callback = object : NativeCallback() {
            override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                super.onNativeAdLoaded(nativeAd)
                if (!isAdded || view == null || isRemoving) return

                val adView = LayoutInflater.from(safeContext)
                    .inflate(com.ezteam.ezpdflib.R.layout.ads_native_bot_no_media_short, null) as NativeAdView
                binding.layoutNative.removeAllViews()
                binding.layoutNative.addView(adView)
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)

                onAdLoadFinished()
            }

            override fun onAdFailedToLoad() {
                super.onAdFailedToLoad()
                if (!isAdded || view == null || isRemoving) return
                binding.layoutNative.visibility = View.GONE

                onAdLoadFinished()
            }
        }

        Admob.getInstance().loadNativeAd(
            safeContext.applicationContext,
            getString(com.ezteam.ezpdflib.R.string.native_popup_all),
            callback
        )
    }

    private fun onAdLoadFinished() {
        isAdLoaded = true
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
    }
    private fun initListener() {
        binding.cbPassword.setOnCheckedChangeListener { _, isCheck ->
            binding.edtPasswordLayout.isVisible = isCheck
            if (!isCheck) {
                binding.edtPassword.setText("")
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonConvert.setOnClickListener {
            val fileName = binding.edtFileName.text.toString()
            val password = binding.edtPassword.text.toString()
            val size = binding.spinnerPageSize.selectedItem.toString()

            if (fileName.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.file_name_invalid),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                complete.invoke(fileName, password,size)
                dismiss()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.edtFileName.setText(
            "PdfCreate_${System.currentTimeMillis()}"
        )
    }
}