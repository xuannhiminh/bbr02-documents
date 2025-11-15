package office.pdf.document.reader.viewer.editor.screen.start

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import com.brian.base_iap.utils.PreferencesUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.common.PresKey
import office.pdf.document.reader.viewer.editor.databinding.ActivityIntroBinding
import office.pdf.document.reader.viewer.editor.databinding.ItemIntroPageBinding
import office.pdf.document.reader.viewer.editor.screen.base.PdfBaseActivity
import office.pdf.document.reader.viewer.editor.screen.main.MainActivity
import office.pdf.document.reader.viewer.editor.screen.main.MainViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class IntroActivity : PdfBaseActivity<ActivityIntroBinding>() {
//    private val viewModel by inject<MainViewModel>()

    companion object {
        private const val TAG = "IntroActivity"

        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, IntroActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        setClass(activity, IntroActivity::class.java)
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, IntroActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }
    private val ads = listOf(
        R.string.native_intro_1,
        R.string.native_intro_2,
        R.string.native_intro_3,
    )

    private val animations = listOf(
        R.raw.intro1,
        R.raw.intro2,
        R.raw.intro3
    )
    private val titles = listOf(
        R.string.title_intro_1,
        R.string.title_intro_2,
        R.string.title_intro_3
    )

    private fun logEvent(firebaseAnalytic: FirebaseAnalytics, event: String, key: String, value: String) {
        firebaseAnalytic.logEvent(event, Bundle().apply {
            putString(key, value)
            putString("screen", TAG)
        })
    }

    private lateinit var descriptions: List<CharSequence>
    private lateinit var firebaseAnalytic: FirebaseAnalytics
    private var currentPage = 0

    override fun viewBinding(): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(LayoutInflater.from(this))
    }
    private var previousState = ViewPager2.SCROLL_STATE_IDLE

    override fun initView() {
        firebaseAnalytic = FirebaseAnalytics.getInstance(this)
        setupIndicators()
        val open = getString(R.string.open_intro)
        val read = getString(R.string.read_intro)
        val edit = getString(R.string.edit_intro)
        val desc = getString(R.string.title_description_1)
        val keywords = listOf(open, read, edit)
        val styledTextSubtitle = highlightText(
            context = this,
            fullText = desc,
            highlightParts = keywords
        )
        descriptions = listOf(
            styledTextSubtitle,
            getString(R.string.title_description_2),
            getString(R.string.title_description_3)
        )
        val pages = listOf(
            IntroPage(R.raw.intro1, R.string.title_intro_1, styledTextSubtitle), // styledTextSubtitle là SpannableString
            IntroPage(R.raw.intro2, R.string.title_intro_2, getString(R.string.title_description_2)),
            IntroPage(R.raw.intro3, R.string.title_intro_3, getString(R.string.title_description_3)),
        )
        val adapter = IntroPagerAdapter(this, pages)
        adapter.onSkipClick = {
            goToRequestPermissionActivity()
        }
        binding.viewPagerIntro.adapter = adapter
        binding.viewPagerIntro.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                logEvent(firebaseAnalytic, "IntroActivity${position}", "IntroActivity${position}","IntroActivity${position}"  )
                currentPage = position
                setCurrentIndicator(position)
                updateNextButton()
               // loadNativeNomedia(ads[position])
            }
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                // If on the last page and drag → idle without settling → it's an overscroll
                if (currentPage ==  binding.viewPagerIntro.adapter?.itemCount?.minus(1)
                    && previousState == ViewPager2.SCROLL_STATE_DRAGGING
                    && state == ViewPager2.SCROLL_STATE_IDLE
                ) {
                    goToRequestPermissionActivity()
                }
                previousState = state
            }
        })

        updateNextButton()
       // loadNativeNomedia(ads[0])
    }
    private fun highlightText(
        context: Context,
        fullText: String,
        highlightParts: List<String>
    ): SpannableString {
        val spannable = SpannableString(fullText)
        val color = ContextCompat.getColor(context, R.color.primaryColor)

        highlightParts.forEach { part ->
            val regex = Regex(Regex.escape(part), RegexOption.IGNORE_CASE)
            regex.findAll(fullText).forEach { match ->
                val startIndex = match.range.first
                val endIndex = match.range.last + 1

                spannable.setSpan(
                    AbsoluteSizeSpan(20, true),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(color),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return spannable
    }

    override fun onResume() {
        super.onResume()
        // Load quảng cáo nếu layoutNative đang rỗng
//        if (binding.layoutNative.childCount == 0) {
//            loadNativeNomedia(ads[0])
//        }
    }

    private fun loadNativeNomedia(adResId: Int) {


        if (SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.visibility = View.VISIBLE
            val loadingView = LayoutInflater.from(this)
                .inflate(R.layout.ads_native_loading, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    val layoutRes = R.layout.ads_native_bot_no_media
                    val adView = LayoutInflater.from(this@IntroActivity)
                        .inflate(layoutRes, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                }

                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    binding.layoutNative.visibility = View.GONE
                }
            }

            Admob.getInstance().loadNativeAd(
                applicationContext,
                getString(adResId),
                callback
            )
        } else {
            binding.layoutNative.visibility = View.GONE
        }
    }

    override fun initListener() {
        binding.btnNext.setOnClickListener {
            if (currentPage < animations.lastIndex) {
                binding.viewPagerIntro.currentItem = currentPage + 1
            } else {
                goToRequestPermissionActivity()
            }
        }
    }

    override fun initData() {
        // Nếu cần xử lý dữ liệu khi load lần đầu
    }

    private fun setupIndicators() {
        val indicatorLayout = binding.indicatorLayout
        indicatorLayout.removeAllViews()
        for (i in animations.indices) {
            val view = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen._6sdp),
                    resources.getDimensionPixelSize(R.dimen._6sdp)
                ).apply { setMargins(8, 0, 8, 0) }
                setBackgroundResource(R.drawable.indicator_unselected)
            }
            indicatorLayout.addView(view)
        }
        setCurrentIndicator(0)
    }

    private fun setCurrentIndicator(index: Int) {
        val indicatorLayout = binding.indicatorLayout
        for (i in 0 until indicatorLayout.childCount) {
            val view = indicatorLayout.getChildAt(i)
            val isSelected = i == index
            val widthDimen = if (isSelected) R.dimen._15sdp else R.dimen._6sdp
            view.setBackgroundResource(if (isSelected) R.drawable.indicator_selected else R.drawable.indicator_unselected)
            val params = view.layoutParams as LinearLayout.LayoutParams
            params.width = resources.getDimensionPixelSize(widthDimen)
            view.layoutParams = params
        }
    }

    private fun updateNextButton() {
        binding.btnNext.text = getString(
            if (currentPage == animations.lastIndex) R.string.start else R.string.next
        )
    }

    private fun goToRequestPermissionActivity() {
        logEvent(firebaseAnalytic, "intro_next", "",""  )
        if(intent == null) intent = Intent(this, RequestAllFilePermissionActivity::class.java)
        if (PreferencesUtils.getBoolean(PresKey.GET_START, true) || !isAcceptManagerStorage()) {
            intent?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
//        startActivity(intent)
        RequestAllFilePermissionActivity.start(this)
        finish()
    }

    inner class IntroPagerAdapter : RecyclerView.Adapter<IntroPagerAdapter.IntroViewHolder>() {

        inner class IntroViewHolder(val binding: ItemIntroPageBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
            val binding = ItemIntroPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return IntroViewHolder(binding)
        }
        override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
            with(holder.binding) {
                animationView.setAnimation(animations[position])
                animationView.playAnimation()
                textTitle.text = getString(titles[position])
                textContent.text = descriptions[position]
            }
        }

        override fun getItemCount(): Int = animations.size
    }
}
