package com.ezstudio.pdftoolmodule.activity

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.pdftoolmodule.PdfToolBaseActivity
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.adapter.FileToolItemAdapter
import com.ezstudio.pdftoolmodule.databinding.ActivityMergeFileBinding
import com.ezstudio.pdftoolmodule.model.FileModel
import com.ezteam.baseproject.utils.ViewUtils
//import com.google.android.gms.ads.ez.EzAdControl
import java.util.*

class MergeActivity : PdfToolBaseActivity<ActivityMergeFileBinding>(), View.OnClickListener {

    companion object {
        const val FILE_PATH = "file path"
    }

    private var lstData = mutableListOf<FileModel>()
    private val adapter by lazy {
        FileToolItemAdapter(this, lstData).apply {
            allowMoveFile = true
            allowSwipeRight = true
        }
    }
    var itemTouchHelper: ItemTouchHelper? = null

    override fun initView() {
        binding.rcvListFile.adapter = adapter
    }

    override fun initData() {
        dragItemAdapter()
    }

    override fun initListener() {
        super.initListener()
        binding.ivAddFile.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.ivDone.setOnClickListener(this)
        binding.ivRemoveSuggest.setOnClickListener(this)
        adapter.apply {
            dragClickListener = { holder, _ ->
                itemTouchHelper?.let {
                    it.startDrag(holder)
                }
            }

            itemClickListener = {
                toolViewModel.openFile.value = it.path
            }
        }
    }

    private fun dragItemAdapter() {
        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeFlag(
                    ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.let {
                    Collections.swap(
                        it.list,
                        viewHolder.adapterPosition,
                        target.adapterPosition
                    )
                    adapter.notifyItemMoved(
                        viewHolder.adapterPosition,
                        target.adapterPosition
                    )
                }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

        })
        itemTouchHelper?.attachToRecyclerView(binding.rcvListFile)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_remove_suggest -> {
                ViewUtils.collapse(binding.lnSuggest)
            }
            R.id.iv_add_file -> {
                showBottomAddFile(-1) {
                    lstData.addAll(it)
                    adapter.apply {
                        lstConstant.addAll(it)
                        notifyDataSetChanged()
                    }
                }
            }
            R.id.iv_back -> {
                onBackPressed()
            }
            R.id.iv_done -> {
                if (lstData.isEmpty()) {
                    toast(getString(R.string.choose_at_least))
                } else {
                    callFunctionMerge(listPathSelect())
                }
            }
        }
    }

    override fun onBackPressed() {
        if (lstData.isNotEmpty()) {
            showDialogDiscard {
                if (it) {
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    private fun listPathSelect(): MutableList<String> {
        val lstPath = mutableListOf<String>()
        lstData.forEach {
            lstPath.add(it.path)
        }
        return lstPath
    }

    override fun viewBinding(): ActivityMergeFileBinding {
        return ActivityMergeFileBinding.inflate(LayoutInflater.from(this))
    }

}