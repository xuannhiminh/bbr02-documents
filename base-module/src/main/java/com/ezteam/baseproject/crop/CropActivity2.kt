package com.ezteam.baseproject.crop

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.ezteam.baseproject.R
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.crop.cropper.CropImage
import com.ezteam.baseproject.crop.cropper.CropImageOptions
import com.ezteam.baseproject.crop.cropper.CropImageView
import com.ezteam.baseproject.databinding.ActivityCrop2Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception

class CropActivity2 : BaseActivity<ActivityCrop2Binding>(),
    View.OnClickListener,
    CropImageView.OnSetImageUriCompleteListener,
    CropImageView.OnCropImageCompleteListener {

    private lateinit var itemCropAdapter: ItemCropAdapter
    private var onChanged = false
    private var mOptions: CropImageOptions? = null
    val uriImage: Uri? by lazy {
        intent?.getParcelableExtra(DATA_INTENT_CROP) as Uri?
    }

    val dataCrop = fun(context: Context): MutableList<ItemCrop> {
        return mutableListOf(
            ItemCrop(0, context.getString(R.string.custom), R.drawable.ic_base_crop_free, 0, 0),
            ItemCrop(1, context.getString(R.string.crop_11), R.drawable.ic_base_crop_11, 1, 1),
            ItemCrop(2, context.getString(R.string.crop_45), R.drawable.ic_base_crop_45, 4, 5),
            ItemCrop(3, context.getString(R.string.crop_916), R.drawable.ic_base_crop_916, 9, 16),
            ItemCrop(4, context.getString(R.string.crop_43), R.drawable.ic_base_crop_43, 4, 3),
            ItemCrop(5, context.getString(R.string.crop_54), R.drawable.ic_base_crop_54, 5, 4),
            ItemCrop(6, context.getString(R.string.crop_74), R.drawable.ic_base_crop_74, 7, 4),
            ItemCrop(7, context.getString(R.string.crop_31), R.drawable.ic_base_crop_31, 3, 1),
            ItemCrop(8, context.getString(R.string.crop_21), R.drawable.ic_base_crop_21, 2, 1),
            ItemCrop(9, context.getString(R.string.crop_32), R.drawable.ic_base_crop_32, 3, 2),
            ItemCrop(10, context.getString(R.string.crop_23), R.drawable.ic_base_crop_23, 2, 3),
            ItemCrop(11, context.getString(R.string.crop_a4), R.drawable.ic_base_crop_a4, 8, 11),
            ItemCrop(12, context.getString(R.string.crop_34), R.drawable.ic_base_crop_34, 3, 4),
        )
    }

    companion object {
        private const val DATA_INTENT_CROP = "data intent crop"

        fun start(activity: BaseActivity<*>, uri: Uri, result: (Uri?) -> Unit) {
            val intent = CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(activity, CropActivity2::class.java)
            intent.putExtra(DATA_INTENT_CROP, uri)

            activity.activityLauncher.launch(intent) {
                if (it.resultCode == RESULT_OK) {
                    result(it.data?.data)
                }
            }
        }
    }

    override fun initView() {
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        window.statusBarColor = resources.getColor(R.color.color_252525)
//        window.navigationBarColor = resources.getColor(R.color.tokenBlack100)
        intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)?.let {
            mOptions = it.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS)
        }
        binding.tvTitle.text = resources.getText(R.string.crop_image_activity_title)
        binding.cropImageView.isAutoZoomEnabled = false

        uriImage?.let {
            binding.cropImageView.setImageUriAsync(it)
        } ?: run {
            toast(getString(R.string.app_error))
            finish()
        }

        itemCropAdapter = ItemCropAdapter(this, dataCrop(this))
        binding.rcvCrop.adapter = itemCropAdapter
    }

    override fun onStart() {
        super.onStart()
        binding.cropImageView.setOnSetImageUriCompleteListener(this)
        binding.cropImageView.setOnCropImageCompleteListener(this)
    }

    override fun onStop() {
        super.onStop()
        binding.cropImageView.setOnSetImageUriCompleteListener(null)
        binding.cropImageView.setOnCropImageCompleteListener(null)
    }

    override fun onSetImageUriComplete(view: CropImageView?, uri: Uri?, error: Exception?) {
        if (error == null) {
            mOptions?.let {
                binding.cropImageView.cropRect = it.initialCropWindowRectangle
                if (it.initialRotation > -1) {
                    binding.cropImageView.rotatedDegrees = it.initialRotation
                }
            }
        } else {
            finish()
            toast(getString(R.string.app_error))
        }
    }

    override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult) {
        showHideLoading(false)
        val intent = Intent().apply {
            data = result.uri
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun initListener() {
        itemCropAdapter.itemSelectListener = { item ->
            onChanged = true
            if (item.width == 0 && item.height == 0) {
                binding.cropImageView.setFixedAspectRatio(false)
            } else {
                binding.cropImageView.setAspectRatio(item.width, item.height)
            }
        }

        binding.imClose.setOnClickListener(this)
        binding.imCheck.setOnClickListener(this)
        binding.tvReset.setOnClickListener(this)
    }

    private fun resetAll() {
        onChanged = false
        itemCropAdapter.apply {
            itemSelected = list[0]
            notifyDataSetChanged()
        }
        binding.cropImageView.apply {
            setAspectRatio(1, 1)
            setFixedAspectRatio(false)
        }

    }

    override fun onBackPressed() {
        if (onChanged) {
            showDialogDiscard {
                if (it) {
                    super.onBackPressed()
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.im_close -> {
                if (onChanged) {
                    showDialogDiscard {
                        if (it) {
                            finish()
                        }
                    }
                } else {
                    finish()
                }
            }
            R.id.im_check -> {
                cropImage()
            }
            R.id.tv_reset -> {
                resetAll()
            }
        }
    }

    private fun cropImage() {
        showHideLoading(true)
        val fileOutput = "IMG_" + System.currentTimeMillis() + ".png"
        val outputUri = Uri.fromFile(File.createTempFile("cropped", fileOutput, cacheDir))
        mOptions?.let {
            binding.cropImageView.saveCroppedImageAsync(
                outputUri,
                it.outputCompressFormat,
                it.outputCompressQuality,
                it.outputRequestWidth,
                it.outputRequestHeight,
                it.outputRequestSizeOptions
            )
        }
    }

    override fun initData() {

    }

    override fun viewBinding(): ActivityCrop2Binding {
        return ActivityCrop2Binding.inflate(LayoutInflater.from(this))
    }

}