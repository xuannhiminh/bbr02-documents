package com.ezteam.ezpdflib.activity.signature

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.activity.BasePdfViewerActivity
import com.ezteam.ezpdflib.adapter.ColorAdapter
import com.ezteam.ezpdflib.databinding.LibActivityCreateSignatureBinding
import com.ezteam.ezpdflib.util.Config
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.ezteam.ezpdflib.widget.singature.UI.SilkySignaturePad
//import com.google.android.gms.ads.ez.EzAdControl
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import java.io.File
import java.io.FileOutputStream

class CreateSignatureActivity : BasePdfViewerActivity(), ColorPickerDialogListener {

    private val KEY_COLOR = "KEY_COLOR"
    private val binding: LibActivityCreateSignatureBinding by lazy {
        LibActivityCreateSignatureBinding.inflate(LayoutInflater.from(this))
    }

    private val colorAdapter by lazy {
        val dataColor = arrayListOf("")
        dataColor.addAll(Config.colorHexString)
        ColorAdapter(dataColor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initListener()
//        EzAdControl.getInstance(this).showAds()
    }

    override fun initView() {
        super.initView()
        binding.rcvColor.adapter = colorAdapter
        colorAdapter.colorSelect = PreferencesUtils.getString(KEY_COLOR, "#000000")
        val indexSelect = Config.colorHexString.indexOfFirst {
            it == colorAdapter.colorSelect
        }
        binding.rcvColor.smoothScrollToPosition(if (indexSelect == -1) 0 else indexSelect)

        binding.silkySignaturePad.setPenColor(Color.parseColor(colorAdapter.colorSelect))
    }

    private fun initListener() {

        binding.silkySignaturePad.setOnSignedListener(object : SilkySignaturePad.OnSignedListener {
            override fun onStartSigning() {
            }

            override fun onSigned() {
            }

            override fun onClear() {
            }

        })

        colorAdapter.itemSelectListener = {
            if (TextUtils.isEmpty(it)) {
                try {
                    ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(0)
                        .setColor(Color.parseColor(colorAdapter.colorSelect))
                        .setShowAlphaSlider(false)
                        .show(this)
                } catch (ex: Exception) {
                    ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(0)
                        .setColor(Color.parseColor("#FF0000"))
                        .setShowAlphaSlider(false)
                        .show(this)
                }
            } else {
                PreferencesUtils.putString(KEY_COLOR, it)
                binding.silkySignaturePad.setPenColor(Color.parseColor(it))
            }
        }

        binding.imBack.setOnClickListener {
            finish()
        }

        binding.ivSave.setOnClickListener {
            if (!binding.silkySignaturePad.isEmpty) {
                val myDir = File("${this.filesDir}/signature")
                if (!myDir.exists()) {
                    myDir.mkdirs()
                }
                val saveFile = File(myDir.absolutePath, "${System.currentTimeMillis()}.png")
                val fos = FileOutputStream(saveFile)
                binding.silkySignaturePad.getTransparentSignatureBitmap(true)
                    ?.compress(Bitmap.CompressFormat.PNG, 100, fos)
                setResult(Activity.RESULT_OK)
                fos.flush()
                fos.close()
                finish()
            } else {
                toast(getString(R.string.signature_empty))
            }

        }

        binding.ivClear.setOnClickListener {
            binding.silkySignaturePad.clear()
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        val hexColor = String.format("#%06X", (0xFFFFFF and color))
        PreferencesUtils.putString(KEY_COLOR, hexColor)
        binding.silkySignaturePad.setPenColor(color)
    }

    override fun onDialogDismissed(dialogId: Int) {

    }

}