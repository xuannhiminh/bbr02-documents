package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.ezteam.baseproject.animation.AnimationUtils
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.ExitAppDialogBinding

class ExitAppDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: ExitAppDialogBinding? = null
    private val binding get() = _binding!!

    private var title: String = ""
    private var message: String = ""
    private var onConfirm: (() -> Unit)? = null
    private var isViewDestroyed = false
    private var isAdLoaded = false

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private fun logEvent(event: String) {
        firebaseAnalytics.logEvent(event, Bundle())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ExitAppDialogBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun disabledButton() {
        _binding?.let {
            it.buttonContainer.visibility = View.GONE
            it.ivLoading.apply {
                visibility = View.VISIBLE
                setImageResource(R.drawable.ic_loading)
                val rotate = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_loading)
                startAnimation(rotate)
                isClickable = false
            }
            it.tvMessage.text = getString(R.string.saving_content)
        }
    }
    private fun enabledButton() {
        doneCountDown?.cancel()
        _binding?.let {
            it.buttonContainer.visibility = View.VISIBLE
            it.ivLoading.visibility = View.GONE
            it.ivLoading.clearAnimation()
            it.tvMessage.text = getString(R.string.exit_content)
        }
    }
    private var doneCountDown: CountDownTimer? = null
    private fun startDoneCountdown() {
        disabledButton()

        doneCountDown?.cancel()
        doneCountDown = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (isViewDestroyed || _binding == null) return
                if (!isAdLoaded) {
                    enabledButton()
                }
            }
        }
        doneCountDown?.start()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startDoneCountdown()
        loadNativeNomedia()
        isViewDestroyed = false
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

            binding.btnOk.setOnClickListener {
                logEvent("out_app")
                requireActivity().finishAffinity()
                dismiss()
            }

            binding.btnCancel.setOnClickListener {
                logEvent("cancel_exit_app")
                dismiss()
            }

        } catch (e: Exception) {

        }
    }
    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }

        val safeContext = context ?: return
        if (SystemUtils.isInternetAvailable(safeContext)) {
            isAdLoaded = false // reset trạng thái

            binding.layoutNative.visibility = View.VISIBLE
            val loadingView = LayoutInflater.from(safeContext)
                .inflate(R.layout.ads_native_bot_loading_2, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    if (isViewDestroyed || !isAdded || _binding == null) return

                    // Inflate ad view
                    val adView = LayoutInflater.from(safeContext)
                        .inflate(R.layout.ads_native_bot_2, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)

                    // Cho phép đóng dialog ngoài khi ad đã load
                    isAdLoaded = true

                    doneCountDown?.cancel()
                    enabledButton()
                    dialog?.setCancelable(true)
                    dialog?.setCanceledOnTouchOutside(true)
                }

                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    if (isViewDestroyed || !isAdded || _binding == null) return

                    // Ẩn layout ad, vẫn coi là "đã load" để không block user
                    binding.layoutNative.visibility = View.GONE

                    isAdLoaded = true

                    doneCountDown?.cancel()
                    enabledButton()
                    dialog?.setCancelable(true)
                    dialog?.setCanceledOnTouchOutside(true)
                }
            }

            Admob.getInstance().loadNativeAd(
                safeContext.applicationContext,
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_exit_app"),
                callback
            )
        } else {
            // Nếu không có internet, hide ad và mở khóa dialog
            binding.layoutNative.visibility = View.GONE
            isAdLoaded = true
            dialog?.setCancelable(true)
            dialog?.setCanceledOnTouchOutside(true)
        }
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setDimAmount(0.5f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewDestroyed = true
        doneCountDown?.cancel()
        _binding = null
    }

    fun setTitle(title: String): ExitAppDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String): ExitAppDialog {
        this.message = message
        return this
    }

    fun setOnConfirmListener(callback: () -> Unit): ExitAppDialog {
        this.onConfirm = callback
        return this
    }
}
