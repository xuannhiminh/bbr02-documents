package com.ezteam.baseproject.dialog

import android.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<BD : ViewBinding, B : BuilderDialog>(var builder: B, context: Context) :
    Dialog(context) {

    protected abstract val viewBinding: BD
    lateinit var binding: BD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = viewBinding
        setContentView(binding.root)
        window?.apply {
            val windowParams = attributes
            setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            windowParams.dimAmount = 0.7f
            attributes = windowParams
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        initView()
        initListener()
    }

    override fun show() {
        super.show()
        setOnDismissListener {
            builder.dismissDialogListener?.invoke()
        }
        applyImmersiveMode()
    }

    private fun applyImmersiveMode() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window?.insetsController?.let { controller ->
                controller.hide(
                    android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window?.decorView?.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    open fun initView() {
        title?.let {
            it.text = builder.title.orEmpty()
        }

        positiveButton?.let {
            if (!builder.positiveButton.isNullOrEmpty()) {
                it.text = builder.positiveButton
                it.setOnClickListener {
                    handleClickPositiveButton(HashMap())
                }
            }
        }

        negativeButton?.let {
            if (!builder.negativeButton.isNullOrEmpty()) {
                it.text = builder.negativeButton
                it.setOnClickListener(::handleClickNegativeButton)
            }
        }

        message?.let {
            it.text = builder.message.orEmpty()
        }

        container?.let {
            it.setOnClickListener {
                dismiss()
            }
        }
    }

    protected abstract fun initListener()
    protected open val positiveButton: TextView?
        get() = null
    protected open val negativeButton: TextView?
        get() = null
    protected open val title: TextView?
        get() = null
    protected open val message: TextView?
        get() = null
    protected open val container: View?
        get() = null

    protected open fun handleClickNegativeButton(view: View) {
        builder.negativeButtonListener?.let {
            it(this)
        }
        dismiss()
    }

    protected open fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        builder.positiveButtonListener?.let {
            it(this, data)
        }
        dismiss()
    }
}