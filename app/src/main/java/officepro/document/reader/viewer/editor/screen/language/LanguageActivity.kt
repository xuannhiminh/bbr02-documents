package officepro.document.reader.viewer.editor.screen.language

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.brian.base_iap.utils.PreferencesHelper
import officepro.document.reader.viewer.editor.databinding.ActivityLanguageBinding
import officepro.document.reader.viewer.editor.screen.main.MainActivity
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.animation.AnimationUtils
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import com.brian.base_iap.utils.IAPUtils
import com.brian.base_iap.utils.PreferencesUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import officepro.document.reader.viewer.editor.common.PresKey
import officepro.document.reader.viewer.editor.screen.start.IntroActivity
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.screen.iap.IapActivityV2
import officepro.document.reader.viewer.editor.screen.start.RequestAllFilePermissionActivity

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {

    private var adapter: RecyclerView.Adapter<*>? = null
    private var baseAdapter: BaseLanguageAdapter? = null

    private val datas = listOf(*Config.itemsLanguage)

    companion object {
        private const val TAG = "LanguageActivity"

        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, LanguageActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        setClass(activity, LanguageActivity::class.java)
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, LanguageActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private fun logEvent(firebaseAnalytic: FirebaseAnalytics, event: String, key: String, value: String) {
        firebaseAnalytic.logEvent(event, Bundle().apply {
            putString(key, value)
        })
    }

    override fun initView() {
        Log.i(TAG, "initView")

        if (TemporaryStorage.shouldLoadAdsLanguageScreen) {
            startDoneCountdown()
            loadNativeNomedia()
        } else {
            showIvDoneChecked()
        }

        // Get the saved language from preferences — used only to show iv_hand initially
        val cached = PreferencesHelper.getString(
            PreferencesHelper.KEY_LANGUAGE, Config.itemsLanguage[0].value
        )

        if (FirebaseRemoteConfigUtil.getInstance().getLanguageAdapterType() == 0) {
            val a = LanguageAdapter(datas, this, cached, object : LanguageAdapter.OnLanguageSelectedListener {
                override fun onLanguageSelected(item: ItemSelected) {
                    // Do something when selected
                }
            })
            adapter = a
            baseAdapter = a
        } else {
            val a = Language2Adapter(datas, this, cached, object : Language2Adapter.OnLanguageSelectedListener {
                override fun onLanguageSelected(item: ItemSelected) {
                    binding.ivDone.isEnabled = true
                    binding.ivDone.alpha = 1f
                }
            })
            adapter = a
            baseAdapter = a
        }
        binding.rcvData.adapter = adapter
        
        // Tối ưu hiệu suất RecyclerView
        binding.rcvData.setHasFixedSize(true)
        binding.rcvData.setItemViewCacheSize(20)
        binding.rcvData.isDrawingCacheEnabled = true
        binding.rcvData.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

        binding.rcvData.isNestedScrollingEnabled = false
        binding.rcvData.setRecycledViewPool(androidx.recyclerview.widget.RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(0, 20)
            setMaxRecycledViews(1, 10)
            setMaxRecycledViews(2, 10)
        })
        
        scrollToSelectedLanguageSmooth(cached)
        
        binding.edtSearchLanguage.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_search, 0, 0, 0
        )
        binding.edtSearchLanguage.addTextChangedListener(object : android.text.TextWatcher {
            private var searchJob: Handler? = null
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.removeCallbacksAndMessages(null)

                searchJob = Handler().apply {
                    postDelayed({
                        baseAdapter?.filter(s?.toString() ?: "")
                        if (baseAdapter?.getDisplayList()?.isEmpty() == true) {
                            binding.layoutEmpty.visibility = View.VISIBLE
                            binding.rcvData.visibility = View.GONE
                            binding.animationView.playAnimation()
                        } else {
                            binding.layoutEmpty.visibility = View.GONE
                            binding.rcvData.visibility = View.VISIBLE
                            binding.animationView.cancelAnimation()
                        }
                        val show = !s.isNullOrEmpty()
                        val endDrawable = if (show) getDrawable(R.drawable.ic_tool_close_round) else null
                        binding.edtSearchLanguage.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_search, 0,
                            if (show) R.drawable.ic_tool_close_round else 0, 0
                        )
                    }, 300)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.edtSearchLanguage.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val editText = binding.edtSearchLanguage
                val drawableEnd = editText.compoundDrawables[2]
                if (drawableEnd != null) {
                    val bounds = drawableEnd.bounds
                    val x = event.x.toInt()
                    val width = editText.width
                    val paddingEnd = editText.paddingEnd
                    if (x >= width - paddingEnd - bounds.width() && x <= width - editText.paddingEnd) {
                        editText.setText("")
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    override fun initListener() {
//        loadNative()
    }

    override fun viewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this))
    }

    override fun initData() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (FirebaseRemoteConfigUtil.getInstance().getLanguageAdapterType() == 0) {
            // do nothing
        } else {
            binding.ivDone.isEnabled = false
            binding.ivDone.alpha = 0.5f
        }
        binding.ivDone.setOnClickListener { v: View? ->
            TemporaryStorage.shouldLoadAdsLanguageScreen = false
            PreferencesHelper.putString(PreferencesHelper.KEY_LANGUAGE, baseAdapter!!.selected)
            if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
                logEvent(firebaseAnalytics, "language_first_time_done", "button_action", "done_click")
               // startIntro()
                startRequestAllFilePermission()
//                if (!IAPUtils.isPremium() && BillingProcessor.isIabServiceAvailable(this)) {
//                    startIAP()
//                } else {
//                    startRequestAllFilePermission()
//                }
            } else {
                logEvent(firebaseAnalytics, "language_from_main_done", "button_action", "done_click")
                setLanguageUpDate(baseAdapter!!.selected)
                onBackPressed()
            }
        }
    }

    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            showIvDoneChecked()
            return
        }

        if (!SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.visibility = View.GONE
            showIvDoneChecked()
            return
        }
        binding.layoutNative.visibility = View.VISIBLE
        Log.d(TAG, "loadNativeNomedia: TemporaryStorage.nativeAdPreload " + TemporaryStorage.nativeAdPreload)
        if (TemporaryStorage.nativeAdPreload != null) {
            if (isFinishing || isDestroyed) {
                TemporaryStorage.nativeAdPreload = null
                TemporaryStorage.isLoadingNativeAdsLanguage = false
                TemporaryStorage.callbackNativeAdsLanguage = {  }
                return
            }
            showIvDoneChecked()

            val layoutRes = if (FirebaseRemoteConfigUtil.getInstance().isBigAds()) {
                R.layout.ads_native_bot_2
            } else {
                R.layout.ads_native_bot_no_media_short
            }
            val adView = LayoutInflater.from(this@LanguageActivity).inflate(layoutRes, null) as NativeAdView
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(adView)
            Admob.getInstance().pushAdsToViewCustom(TemporaryStorage.nativeAdPreload, adView)
            TemporaryStorage.nativeAdPreload = null
            return
        } else if (TemporaryStorage.isLoadingNativeAdsLanguage) {
            Log.d(TAG, "loadNativeNomedia: TemporaryStorage.isLoadingNativeAdsLanguage " + TemporaryStorage.isLoadingNativeAdsLanguage)
            val layoutRes = if (FirebaseRemoteConfigUtil.getInstance().isBigAds()) {
                R.layout.ads_native_bot_loading_2
            } else {
                R.layout.ads_native_loading_short
            }
            val loadingView = LayoutInflater.from(this).inflate(layoutRes, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            TemporaryStorage.callbackNativeAdsLanguage = fun(nativeAd: NativeAd?) {
                // This return only exits the function literal, not the enclosing Activity
                if (isFinishing || isDestroyed) return

                if (nativeAd != null) {
                    Log.i("LanguageActivity", "Native ad loaded while waiting")
                    TemporaryStorage.isLoadingNativeAdsLanguage = false

                    // Show a loading-complete indicator
                    showIvDoneChecked()

                    // Inflate and bind your NativeAdView
                    val layoutRes = if (FirebaseRemoteConfigUtil.getInstance().isBigAds()) {
                        R.layout.ads_native_bot_2
                    } else {
                        R.layout.ads_native_bot_no_media_short
                    }
                    val adView = LayoutInflater.from(this@LanguageActivity).inflate(layoutRes, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                } else {
                    Log.e("LanguageActivity", "Native ad failed to load while waiting")
                    TemporaryStorage.isLoadingNativeAdsLanguage = false

                    // Clear the callback so it won't be invoked again
                    TemporaryStorage.callbackNativeAdsLanguage = { }

                    // Hide the ad container
                    binding.layoutNative.visibility = View.GONE
                    showIvDoneChecked()
                }
            }

            return
        } else {
            val layoutRes = if (FirebaseRemoteConfigUtil.getInstance().isBigAds()) {
                R.layout.ads_native_bot_loading_2
            } else {
                R.layout.ads_native_loading_short
            }
            val loadingView = LayoutInflater.from(this).inflate(layoutRes, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    if (isFinishing || isDestroyed) return
                    showIvDoneChecked()
                    val layoutRes = if (FirebaseRemoteConfigUtil.getInstance().isBigAds()) {
                        R.layout.ads_native_bot_2
                    } else {
                        R.layout.ads_native_bot_no_media_short
                    }
                    val adView = LayoutInflater.from(this@LanguageActivity).inflate(layoutRes, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                }

                override fun onAdFailedToLoad() {
                    if (isFinishing || isDestroyed) return
                    binding.layoutNative.visibility = View.GONE
                    showIvDoneChecked()
                }
            }

            Admob.getInstance().loadNativeAd(
                applicationContext,
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_language"),
                callback
            )
        }

    }
    private fun showIvDoneLoading() {
        binding.ivDone.visibility = View.INVISIBLE
        binding.ivLoading.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_loading)
            val rotate = AnimationUtils.loadAnimation(this@LanguageActivity, R.anim.rotate_loading)
            startAnimation(rotate)
            isClickable = false
        }
    }

    private fun showIvDoneChecked() {
        doneCountDown?.cancel()
        binding.ivLoading.clearAnimation()
        binding.ivLoading.visibility = View.INVISIBLE
        binding.ivDone.apply {
            visibility = View.VISIBLE
            isClickable = true
        }
    }


    private var doneCountDown: CountDownTimer? = null

    private fun startDoneCountdown() {
        showIvDoneLoading()

        doneCountDown?.cancel()
        doneCountDown = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                showIvDoneChecked()
            }
        }
        doneCountDown?.start()
    }

    private fun setLanguageUpDate(language: String) {
        if(baseAdapter!!.selected.contains("-")) {
            val language = baseAdapter!!.selected.split("-")[0]
            val country = baseAdapter!!.selected.split("-")[1]
            setLanguage(language, country)
        } else {
            setLanguage(baseAdapter!!.selected)
        }
    }

//    override fun onResume() {
//        super.onResume()
//        if (TemporaryStorage.shouldLoadAdsLanguageScreen) {
//            startDoneCountdown()
//            loadNativeNomedia()
//        } else {
//            showIvDoneChecked()
//        }
//    }

    private fun startRequestAllFilePermission() {
        if(intent == null) intent = Intent(this, RequestAllFilePermissionActivity::class.java)
        if (PreferencesUtils.getBoolean(PresKey.GET_START, true) || !isAcceptManagerStorage()) {
            intent?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        RequestAllFilePermissionActivity.start(this)
        finish()
        setLanguageUpDate(baseAdapter!!.selected)
    }

    override fun onStop() {
        super.onStop()
        TemporaryStorage.nativeAdPreload = null
        PreferencesUtils.putBoolean(PresKey.FIRST_TIME_OPEN_APP, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        TemporaryStorage.callbackNativeAdsLanguage = {}
        TemporaryStorage.isLoadingNativeAdsLanguage = false
    }


    private fun scrollToSelectedLanguageSmooth(selected: String) {
        binding.rcvData.post {
            val adapter = binding.rcvData.adapter as? BaseLanguageAdapter ?: return@post
            val displayList = adapter.getDisplayList()
            val selectedPosition = displayList.indexOfFirst { it.value == selected }

            if (selectedPosition != -1) {
                val layoutManager = binding.rcvData.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
                layoutManager?.scrollToPositionWithOffset(selectedPosition, 0)
            }
        }
    }


}