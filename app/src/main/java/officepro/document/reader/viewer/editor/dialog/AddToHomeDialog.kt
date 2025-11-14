package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.AddToHomeDialogBinding
import officepro.document.reader.viewer.editor.widgets.Widget1
import officepro.document.reader.viewer.editor.widgets.Widget2
import officepro.document.reader.viewer.editor.widgets.Widget3

class AddToHomeDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: AddToHomeDialogBinding? = null
    private val binding get() = _binding!!
    private var isViewDestroyed = false
    private var isAdLoaded = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent) // Translucent background
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AddToHomeDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false
        binding.btnAdd.setOnClickListener {
            dismiss()
            requestAddWidget()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
        } else {
            Log.d("AddToHomeDialog", "Not load Ads")
        }
        val addText = getString(R.string.add)
        val fullText = getString(R.string.tap_add_or_hold_the_widget_to_add_it_to_your_home_screen_instantly_choose_an_option_below, addText)
        val spannable = SpannableString(fullText)

        // Find the position of app name
        val startIndex = fullText.indexOf(addText)
        val endIndex = startIndex + addText.length

        if (startIndex != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Apply bold style
            spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvDescription.text = spannable


        // ViewPager2 Adapter
        val layouts = listOf(
            R.layout.widget_option_1,
            R.layout.widget_option_2,
            R.layout.widget_option_3
        )
        val adapter = ViewPagerAdapter(layouts, requireContext())
        binding.viewPager.adapter = adapter


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicator(position, adapter.itemCount)
            }
        })

        updateIndicator(0, adapter.itemCount)
    }

    fun updateIndicator(position: Int, numPages: Int) {
        binding.indicatorLayout.removeAllViews() // Remove old dots

        for (i in 0 until numPages) {
            val dot = View(requireContext()).apply {
                val s15px = resources.getDimensionPixelSize(R.dimen._15sdp)
                val s6px = resources.getDimensionPixelSize(R.dimen._6sdp)
                val s4px = resources.getDimensionPixelSize(R.dimen._4sdp)
                val size = if (i == position) s15px else s6px
                layoutParams = LinearLayout.LayoutParams(size, s6px).apply {
                    marginStart = s4px
                    marginEnd = s4px
                }

                setBackgroundResource(if (i == position) R.drawable.indicator_selected else R.drawable.indicator_unselected)
            }
            binding.indicatorLayout.addView(dot)
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
                .inflate(R.layout.ads_native_loading_short, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    if (isViewDestroyed || !isAdded || _binding == null) return

                    // Inflate ad view
                    val adView = LayoutInflater.from(safeContext)
                        .inflate(R.layout.ads_native_bot_no_media_short, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)

                    // Cho phép đóng dialog ngoài khi ad đã load
                    isAdLoaded = true
                    dialog?.setCancelable(true)
                    dialog?.setCanceledOnTouchOutside(true)
                }

                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    if (isViewDestroyed || !isAdded || _binding == null) return

                    // Ẩn layout ad, vẫn coi là "đã load" để không block user
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
        } else {
            // Nếu không có internet, hide ad và mở khóa dialog
            binding.layoutNative.visibility = View.GONE
            isAdLoaded = true
            dialog?.setCancelable(true)
            dialog?.setCanceledOnTouchOutside(true)
        }
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
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) // Dynamic height
            setGravity(Gravity.BOTTOM) // Align bottom
            setDimAmount(0.5f) // Dim background
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
        isViewDestroyed = true
    }

    private fun requestAddWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(requireContext())
        val componentName = when (binding.viewPager.currentItem) {
            0 -> ComponentName(requireContext(), Widget1::class.java)
            1 -> ComponentName(requireContext(), Widget2::class.java)
            2 -> ComponentName(requireContext(), Widget3::class.java)
            else -> ComponentName(requireContext(), Widget1::class.java)
        }
        // Check if adding widgets is allowed
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val pinnedWidgetCallbackIntent = when(binding.viewPager.currentItem) {
                0 -> Intent(requireContext(), Widget1::class.java)
                1 -> Intent(requireContext(), Widget2::class.java)
                2 -> Intent(requireContext(), Widget3::class.java)
                else -> Intent(requireContext(), Widget1::class.java)
            }

            val successCallback = PendingIntent.getBroadcast(
                requireContext(),
                0,
                pinnedWidgetCallbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Request the system to show the add widget dialog
            appWidgetManager.requestPinAppWidget(componentName, null, successCallback)
        } else {
            // Show a message if the system doesn't support pinned widgets
            Toast.makeText(requireContext(), requireContext().getString(R.string.cannot_widget), Toast.LENGTH_SHORT).show()
        }
    }


}


class ViewPagerAdapter(private val layouts: List<Int>, private val context: Context) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int = layouts[position]
}

