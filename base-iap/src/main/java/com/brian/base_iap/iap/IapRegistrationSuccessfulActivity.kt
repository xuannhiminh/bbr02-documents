package com.brian.base_iap.iap

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.brian.base_iap.databinding.ActivityIap3Binding
import com.brian.base_iap.databinding.ActivityIapRegistrationSuccessfulBinding
import com.google.firebase.analytics.FirebaseAnalytics

class IapRegistrationSuccessfulActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIapRegistrationSuccessfulBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    protected var isFromSplash = false

    companion object {
        fun hasExtraKeyContaining(intent: Intent, substring: String): Boolean {
            val extras = intent.extras ?: return false
            return extras.keySet().any { key -> substring in key }
        }
        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, IapActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: hasExtraKeyContaining(activity.intent, pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        setClass(activity, IapActivity::class.java)
                        flags = 0
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, IapActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIapRegistrationSuccessfulBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        isFromSplash = intent.getBooleanExtra("${packageName}.isFromSplash", false)

        initView()
        initData()
        initListener()
    }
    fun initView() {
//        window.statusBarColor = Color.parseColor("#1F0718")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

    }

    fun initListener() {

        binding.btnOk.setOnClickListener {
            navigateNext()
            this@IapRegistrationSuccessfulActivity.finish()
        }
    }
    private fun navigateNext() {
        if (!isFromSplash) {
            finish()    // vào từ main
            return
        }
        IapRouter.navigationHandler?.goToNextScreen(this as AppCompatActivity)
        finish()
    }

    fun initData() {

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}