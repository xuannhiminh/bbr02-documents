package com.ezteam.baseproject.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.nlbn.ads.util.ConsentHelper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.ezteam.baseproject.R
import com.ezteam.baseproject.dialog.DialogLoading
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.ezteam.baseproject.utils.permisson.PermissionUtils
import com.google.android.gms.ads.LoadAdError
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open abstract class BaseActivity<B : ViewBinding> : LocalizationActivity() {
    protected lateinit var binding: B
    private var permissionComplete: ((Boolean) -> Unit)? = null
    private var lastClickTime: Long = 0
    private var progressDialog: DialogLoading? = null
    companion object {
        private const val TIME_DELAY_CHECKING_MANAGE_ALL_FILE_PERMISSION = 50L
        private const val TIME_OUT_CHECKING_MANAGE_ALL_FILE_PERMISSION = 60_000L
    }

    /**
     * Returns true if any DialogFragment is currently showing in this activity.
     */
    fun isAnyDialogFragmentShowing(): Boolean {
        try {
            return supportFragmentManager.fragments.any { fragment ->
                fragment is DialogFragment && fragment.dialog?.isShowing == true
            }
        } catch (e: Exception) {
            Log.e("BaseActivity", "Error checking for showing DialogFragments", e)
        }
        return false
    }

    fun handleAppNameSpannable(showIcon: Boolean = false): SpannableString {
        return try {
            val appName = getString(R.string.app_name)
           // val spannable = if (showIcon) SpannableString("$appName\u00A0") else SpannableString(appName)
            val spannable = SpannableString(appName)

            val startIndex = 0
            val endIndex = appName.indexOf(' ').takeIf { it >= 0 } ?: appName.length

            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannable.setSpan(
                ForegroundColorSpan(redColor),
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
//            if (showIcon) {
//                val drawable = ContextCompat.getDrawable(this, R.drawable.ic_premium_small)
//                val iconSizeInDp = 16f
//                val iconSizeInPx = TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    iconSizeInDp,
//                    resources.displayMetrics
//                ).toInt()
//
//                drawable?.setBounds(0, 0, iconSizeInPx, iconSizeInPx)
//
//                drawable?.let {
//                    val imageSpan = TopAlignImageSpan(it)
//                    spannable.setSpan(
//                        imageSpan,
//                        spannable.length - 1,
//                        spannable.length,
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
//                }
//            }


            return spannable
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error creating spannable app name: ${e.message}")
            SpannableString(getString(R.string.app_name))
        }
    }

    class TopAlignImageSpan(drawable: Drawable) : ImageSpan(drawable) {
        override fun draw(
            canvas: Canvas, text: CharSequence,
            start: Int, end: Int, x: Float,
            top: Int, y: Int, bottom: Int, paint: Paint
        ) {
            val drawable = drawable
            canvas.save()
            val transY = top.toFloat()
            canvas.translate(x, transY)
            drawable.draw(canvas)
            canvas.restore()
        }
    }

    protected fun showAdsInterstitial(idAds: String,complete: () -> Unit) {
        if (IAPUtils.isPremium() || !Admob.getInstance().isLoadFullAds || !ConsentHelper.getInstance(this.applicationContext).canRequestAds()) {
            return complete()
        }
//        if (IAPUtils.isPremium()) {
//            return complete()
//        }

        val interCallback: AdCallback = object : AdCallback() {
            override fun onNextAction() {
                return complete()
            }
            override fun onAdFailedToLoad(var1: LoadAdError?) {
                Log.e("TAG", "onAdFailedToLoad: ${var1?.message}")
                return complete()
            }
        }
        Admob.getInstance().loadAndShowInter(this,
            idAds,
            100, 10000, interCallback)
    }

    val activityLauncher: BetterActivityResult<Intent, ActivityResult> =
        BetterActivityResult.registerActivityForResult(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(null)
        binding = viewBinding()
        setContentView(binding.root)
        initView()
        initData()
        initListener()
        configSystemUI()
    }

    private fun configSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = insets.top,
                bottom = insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    protected val viewException: Array<View>?
        protected get() = null

    protected abstract fun initView()
    protected abstract fun initData()
    protected abstract fun initListener()
    protected abstract fun viewBinding(): B

    private fun showLoading() {
        progressDialog ?: let {
            progressDialog = DialogLoading.ExtendBuilder(this)
                .setCancelable(false)
                .setCanOnTouchOutside(false)
                .build() as DialogLoading
            progressDialog?.show()
        }
    }

    fun requestPermissionStorage(result: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
//                val handler = Handler(Looper.getMainLooper())
//                val startTime = System.currentTimeMillis()
//                val checkRunnable = object : Runnable {
//                    override fun run() {
//                        //Log.d("BaseActivity", "Checking manage all file permission " + (System.currentTimeMillis() - startTime))
//                        if (Environment.isExternalStorageManager()) {
//                            Log.d("BaseActivity", "Checking manage all file permission success")
//                            result(true)
//                        } else if ((System.currentTimeMillis() - startTime) < TIME_OUT_CHECKING_MANAGE_ALL_FILE_PERMISSION) {
//                            // Log.d("BaseActivity", "Checking manage all file permission again")
//                            handler.postDelayed(this, TIME_DELAY_CHECKING_MANAGE_ALL_FILE_PERMISSION)
//                        } else {
//                            Log.d("BaseActivity", "Checking manage all file permission timeout")
//                            handler.removeCallbacks(this)
//                        }
//                    }
//                }
//                handler.postDelayed(checkRunnable, TIME_DELAY_CHECKING_MANAGE_ALL_FILE_PERMISSION)
                val job = lifecycleScope.launch(Dispatchers.IO) {
                    val startTime = System.currentTimeMillis()
                    while (true) {
                        if (Environment.isExternalStorageManager()) {
                            withContext(Dispatchers.Main) {
                                Log.d("BaseActivity", "Checking manage all file permission success")
                                result(true)
                            }
                            break
                        } else if ((System.currentTimeMillis() - startTime) < TIME_OUT_CHECKING_MANAGE_ALL_FILE_PERMISSION) {
                            // Check again after delay
                            delay(TIME_DELAY_CHECKING_MANAGE_ALL_FILE_PERMISSION)
                        } else {
                            Log.d("BaseActivity", "Checking manage all file permission timeout")
                            break
                        }
                    }
                }
                job.start()

// To cancel the job if needed (for example in onDestroy)
// job.cancel()
                var intent: Intent
                try {
                    intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data =
                        Uri.parse(String.format("package:%s", applicationContext.packageName))
                     AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                    activityLauncher.launch(
                        intent
                    ) {
                        Log.d("BaseActivity", "Checking manage all file permission result: $it")
                    //    handler.removeCallbacks(checkRunnable)
                        job.cancel()
                        if (Environment.isExternalStorageManager()) {
                            result(true)
                        } else {
                            result(false)
                        }
                    }
                } catch (e: Exception) {
                    try {
                        intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data =
                            Uri.parse(String.format("package:%s", applicationContext.packageName))
                        activityLauncher.launch(
                            intent
                        ) {
                            Log.d("BaseActivity", "Checking manage all file permission result: $it")
//                        handler.removeCallbacks(checkRunnable)
                            job.cancel()
                            if (Environment.isExternalStorageManager()) {
                                result(true)
                            } else {
                                result(false)
                            }
                        }
                    } catch (e: Exception) {
                        result(false)
                        Log.e("BaseActivity", "Error launching manage all files permission intent", e)
                    }

                }
            } else {
                result(true)
            }
        } else {
            requestPermission(
                {
                    result(it)
                },
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    fun requestPermissionNotificationGoToSetting(result: (Boolean) -> Unit) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                val job = lifecycleScope.launch(Dispatchers.IO) {
                    val startTime = System.currentTimeMillis()
                    while (true) {
                        if (ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                         {
                            withContext(Dispatchers.Main) {
                                Log.d("BaseActivity", "Checking notification permission success")
                                result(true)
                            }
                            break
                        } else if ((System.currentTimeMillis() - startTime) < TIME_OUT_CHECKING_MANAGE_ALL_FILE_PERMISSION) {
                            // Check again after delay
                            delay(TIME_DELAY_CHECKING_MANAGE_ALL_FILE_PERMISSION)
                        } else {
                            Log.d("BaseActivity", "Checking notification permission timeout")
                            break
                        }
                    }
                }
                job.start()

// To cancel the job if needed (for example in onDestroy)
// job.cancel()
                try {
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        }
                    } else {
                        TODO("VERSION.SDK_INT < O")
                    }

                    activityLauncher.launch(intent) {
                        Log.d("BaseActivity", "Checking notification permission result: $it")
                        job.cancel()
                        if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            result(true)
                        } else {
                            result(false)
                        }
                    }
                } catch (e: Exception) {
                    val fallbackIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    activityLauncher.launch(fallbackIntent) {
                        Log.d("BaseActivity", "Checking notification permission result: $it")
                        job.cancel()
                        if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            result(true)
                        } else {
                            result(false)
                        }
                    }
                }

            } else {
                result(true)
            }
    }

    private fun hideLoading() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            progressDialog = null
        }
    }

    fun showHideLoading(isShow: Boolean) {
        try {
            if (isFinishing) return
            if (isShow) {
                showLoading()
            } else {
                hideLoading()
            }
        } catch (e: WindowManager.BadTokenException) {

        }
    }

    fun aVoidDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return true
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return false
    }

    fun toast(content: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!TextUtils.isEmpty(content)) Toast.makeText(
                this@BaseActivity,
                content,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    open fun requestPermission(
        complete: (Boolean) -> Unit,
        vararg permissions: String?
    ) {
        this.permissionComplete = complete
        if (!PermissionUtils.checkPermissonAccept(
                this,
                *permissions
            )
        ) {
            PermissionUtils.requestRuntimePermission(this, *permissions)
        } else {
            complete.invoke(true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionUtils.MY_PERMISSIONS_REQUEST -> if (PermissionUtils.checkPermissonAccept(
                    this,
                    *permissions
                )
            ) {
                permissionComplete?.invoke(true)
            } else {
                permissionComplete?.invoke(false)
            }
        }
    }

    protected open fun isAcceptManagerStorage(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            PermissionUtils.checkPermissonAccept(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }


    protected fun showDialogDiscard(result: (Boolean) -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.discard_edit))
            .setPositiveButton(
                getString(R.string.cancel)
            ) { dialog, which ->
                result(false)
                dialog?.dismiss()
            }
            .setNegativeButton(
                getString(R.string.discard)
            ) { dialog, which ->
                result(true)
                dialog?.dismiss()
            }
            .show()
    }


    open fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    protected fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.checkPermissonAccept(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            true
        }
    }
}