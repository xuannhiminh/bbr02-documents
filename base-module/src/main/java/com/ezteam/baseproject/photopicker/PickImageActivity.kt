package com.ezteam.baseproject.photopicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.ezteam.baseproject.R
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.databinding.ActivityPickImageBinding
import com.ezteam.baseproject.utils.StringUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import org.koin.android.ext.android.inject
import java.io.File
import java.text.MessageFormat
import java.util.*

class PickImageActivity : BaseActivity<ActivityPickImageBinding>() {
    companion object {
        private const val TAG = "PICK_IMAGE"
        private const val KEY_LIMIT_MAX_IMAGE = "KEY_LIMIT_MAX_IMAGE"
        private const val KEY_LIMIT_MIN_IMAGE = "KEY_LIMIT_MIN_IMAGE"
        const val KEY_PICK_ONE = "KEY_PICK_ONE"
        private const val COUNT_MAX = 5
        const val REQUEST_CODE = 0x00111
        const val URI_IMAGES_RESULT = "URI_IMAGES_RESULT"

        fun start(
            activity: BaseActivity<*>,
            minImage: Int,
            maxImage: Int,
            result: (MutableList<Uri>) -> Unit
        ) {
            val intent = Intent(activity, PickImageActivity::class.java).apply {
                putExtra(KEY_LIMIT_MIN_IMAGE, minImage)
                putExtra(KEY_LIMIT_MAX_IMAGE, maxImage)
                putExtra(KEY_PICK_ONE, false)
            }
            activity.activityLauncher.launch(intent) {
                it.data?.let {
                    val lstData = ArrayList<Uri>()
                    it.getParcelableArrayListExtra<Uri>(URI_IMAGES_RESULT)?.let {
                        lstData.addAll(it)
                    }
                    result(lstData)
                }
            }
        }

        fun start(activity: BaseActivity<*>, result: (MutableList<Uri>) -> Unit) {
            val intent = Intent(activity, PickImageActivity::class.java).apply {
                putExtra(KEY_PICK_ONE, true)
            }
            activity.activityLauncher.launch(intent) {
                it.data?.let {
                    val lstData = ArrayList<Uri>()
                    it.getParcelableExtra<Uri>(URI_IMAGES_RESULT)?.let {
                        lstData.add(it)
                    }
                    result(lstData)
                }
            }
        }
    }

    private val pickerViewModel by inject<PickerViewModel>()

    private lateinit var listPopupWindow: ListPopupWindow

    private lateinit var adapter: ImageAlbumAdapter
    private lateinit var selectedAdapter: ImageAlbumAdapter
    private lateinit var directoryAdapter: PopupDirectoryListAdapter
    private val directories: MutableList<PhotoDirectory> = mutableListOf()

    private val maxPhoto by lazy {
        intent?.getIntExtra(KEY_LIMIT_MAX_IMAGE, 9)
    }

    private val minPhoto by lazy {
        intent?.getIntExtra(KEY_LIMIT_MIN_IMAGE, 2) ?: 2
    }

    private val isSingleSelect by lazy {
        intent?.getBooleanExtra(KEY_PICK_ONE, false) ?: false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }
    override fun initView() {
//        setWindowFlag(
//            this,
//            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//            false
//        )
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
//        }
//        window.statusBarColor = Color.parseColor(
//            SystemUtils.getThemeColorInHex(
//                this,
//                "colorPrimaryDark",
//                androidx.appcompat.R.attr.colorPrimaryDark
//            )
//        )
        adapter = ImageAlbumAdapter(this, arrayListOf(), 3) {
            if (selectedAdapter.list.size == maxPhoto) {
                toast(MessageFormat.format(resources.getString(R.string.limit_image), maxPhoto))
                return@ImageAlbumAdapter
            }

            if (isSingleSelect) {
                val intent = Intent()
                intent.putExtra(URI_IMAGES_RESULT, Uri.fromFile(File(it.path)))
                setResult(RESULT_OK, intent)
                finish()

            } else {
                pickerViewModel.addPhotoSelected(it)
            }
        }

        binding.rcvImage.adapter = adapter

        selectedAdapter = ImageAlbumAdapter(this, arrayListOf(), 5, true) {
            pickerViewModel.removePhoto(it)
        }
        binding.rcvSelected.adapter = selectedAdapter

        initPopupDirs()

        if (isSingleSelect) {
            binding.selectedLayout.isVisible = false
        }
    }

    private fun initPopupDirs() {
        directoryAdapter = PopupDirectoryListAdapter(
            Glide.with(this),
            directories
        )
        listPopupWindow = ListPopupWindow(this)
        listPopupWindow.width = ListPopupWindow.MATCH_PARENT
        listPopupWindow.anchorView = binding.toolBar
        listPopupWindow.setAdapter(directoryAdapter)
        listPopupWindow.isModal = true
        listPopupWindow.setDropDownGravity(Gravity.BOTTOM)
    }

    override fun initData() {
        pickerViewModel.isLoading.observe(this) {
            showHideLoading(it)
        }

        requestPermissionStorage {
            if (it) {
                pickerViewModel.loadImages()
            } else {
                finish()
            }
        }

        pickerViewModel.dirsLiveData.observe(this) { dirs ->
            try {
                if (dirs.isEmpty()) return@observe
                directories.clear()
                directories.addAll(dirs.sortedBy {
                    it.name.firstOrNull()?.let { char ->
                        StringUtils.capitalize(it.name)
                    } ?: it.name
                })
                directoryAdapter.notifyDataSetChanged()
                adjustHeight()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        pickerViewModel.dirCurrentLiveData.observe(this) {
            it?.let {
                binding.tvDirectory.text = it.name
            }
        }

        pickerViewModel.getPhotosLiveData().observe(this) { lstPhoto ->
            lstPhoto?.let {
                adapter.setList(it.sortedByDescending { it.dateAdded })
                adapter.setList(it)
                adapter.notifyDataSetChanged()
                if (it.isNotEmpty()) {
                    pickerViewModel.isLoading.postValue(false)
                }
            }

        }

        pickerViewModel.photosSelectedLiveData.observe(this) {
            selectedAdapter.setList(it)
            selectedAdapter.notifyDataSetChanged()

            if (it.size > 0) {
                binding.rcvSelected.smoothScrollToPosition(it.size - 1)
            }
            binding.selectedLayout.isVisible = it.size > 0
        }
    }

    override fun initListener() {
        binding.btnDone.setOnClickListener {
            if (selectedAdapter.list.size < minPhoto) {
                toast(MessageFormat.format(resources.getString(R.string.min_photo), minPhoto))
                return@setOnClickListener
            }

            val uris = pickerViewModel.photosSelectedLiveData.value?.map {
                return@map Uri.fromFile(File(it.path))
            }?.toMutableList() ?: mutableListOf()
            val intent = Intent()
            intent.putParcelableArrayListExtra(URI_IMAGES_RESULT, uris as ArrayList<Uri>)
            setResult(Activity.RESULT_OK, intent)
            finish()

        }

        binding.ivBack.setOnClickListener { finish() }

        binding.dropdownView.setOnClickListener {
            listPopupWindow.show()
        }

        listPopupWindow.setOnItemClickListener(::onDirectoryItemClick)
    }

    private fun onDirectoryItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        pickerViewModel.dirCurrentLiveData.postValue(directories[position])
        listPopupWindow.dismiss()
    }

    private fun adjustHeight() {
        var count: Int = directoryAdapter.count
        count =
            if (count < COUNT_MAX) count else COUNT_MAX
        if (listPopupWindow != null) {
            listPopupWindow.height =
                count * resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._61sdp)
        }
    }

    override fun viewBinding(): ActivityPickImageBinding {
        return ActivityPickImageBinding.inflate(LayoutInflater.from(this))
    }
}