package com.ezstudio.pdftoolmodule.activity

import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import com.ezstudio.pdftoolmodule.PdfToolBaseActivity
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.adapter.FileToolItemAdapter
import com.ezstudio.pdftoolmodule.bottomsheet.BottomSheetSelectFile
import com.ezstudio.pdftoolmodule.databinding.ActivitySelectFileBinding
import com.ezstudio.pdftoolmodule.model.FileModel
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.utils.KeyboardUtils

class SelectFileActivity : PdfToolBaseActivity<ActivitySelectFileBinding>() {

    private lateinit var adapter: FileToolItemAdapter
    private val maxItem by lazy {
        intent.getIntExtra(MAX_ITEM_SELECT, 1)
    }

    companion object {
        private const val MAX_ITEM_SELECT = "max item select"
        var doneListener: ((MutableList<FileModel>) -> Unit)? = null
        var listFilter = BottomSheetSelectFile.Filter.ALL
        fun start(
            activity: BaseActivity<*>,
            maxItem: Int
        ) {
            val intent = Intent(activity, SelectFileActivity::class.java).apply {
                putExtra(MAX_ITEM_SELECT, maxItem)
            }
            activity.activityLauncher.launch(intent) {

            }
            activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        }
    }

    override fun initView() {
        KeyboardUtils.autoHideClickView(binding.root, this)
        adapter = FileToolItemAdapter(this, mutableListOf()).apply {
            allowSelect = true
        }
        adapter.maxSelectItem = maxItem
        binding.rcvListFile.adapter = adapter
    }

    override fun initData() {
    }

    override fun initListener() {
        super.initListener()
        adapter.itemClickListener = {
            binding.tvDone.text = if (adapter.lstSelected.isNotEmpty()) {
                "${getString(R.string.tool_done)} (${adapter.lstSelected.size})"
            } else {
                getString(R.string.tool_done)
            }
        }
        toolViewModel.lstPdfFile.observe(this) {
            binding.rcvListFile.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            adapter.apply {
                val lstData = it
                lstConstant.clear()
                lstConstant.addAll(lstData)
                setList(lstData)
                notifyDataSetChanged()
            }
        }
        binding.edtSearch.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    binding.ivClearSearch.visibility =
                        if (TextUtils.isEmpty(s)) View.INVISIBLE else View.VISIBLE
                    adapter.filter.filter(s)
                }

                override fun afterTextChanged(s: Editable) {}
            })

        binding.ivClearSearch.setOnClickListener {
            binding.edtSearch.setText("")
        }
        binding.ivBack.setOnClickListener {
            finish()
//            overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
        }
        binding.tvDone.setOnClickListener {
            if (adapter.lstSelected.isEmpty()) {
                toast(getString(R.string.choose_at_least))
            } else {
                doneListener?.invoke(adapter.lstSelected)
                val intent = Intent()
                setResult(RESULT_OK, intent)
                finish()
//                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
            }
        }
        binding.container.setOnClickListener {
            finish()
//            overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        doneListener = null
    }

    override fun viewBinding(): ActivitySelectFileBinding {
        return ActivitySelectFileBinding.inflate(LayoutInflater.from(this))
    }
}