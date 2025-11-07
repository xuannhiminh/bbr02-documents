package com.ezstudio.pdftoolmodule.activity

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.pdftoolmodule.PdfToolBaseActivity
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.adapter.PdfPageAdapter
import com.ezstudio.pdftoolmodule.databinding.ActivityOrganizeBinding
import com.ezstudio.pdftoolmodule.model.PdfPageModel
import com.ezstudio.pdftoolmodule.utils.pdftool.Thumbnail
import com.ezteam.baseproject.utils.ViewUtils
//import com.google.android.gms.ads.ez.EzAdControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class OrganizeActivity : PdfToolBaseActivity<ActivityOrganizeBinding>(), View.OnClickListener {

    companion object {
        const val FILE_PATH = "file path"
    }

    private val filePath by lazy {
        intent?.getStringExtra(FILE_PATH) ?: ""
    }
    private var itemTouchHelper: ItemTouchHelper? = null
    private val pageAdapter by lazy {
        PdfPageAdapter(this, mutableListOf())
    }

    override fun initView() {
        binding.rcvPage.adapter = pageAdapter
        dragItemAdapter()
    }

    override fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            Thumbnail.start(filePath, 0.5f, result = { bitmap, index ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (!isDestroyed) {
                        val page = PdfPageModel(bitmap, index)
                        pageAdapter.apply {
                            list.add(page)
                            notifyItemInserted(index)
                        }
                    }
                }
            })
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
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or
                            ItemTouchHelper.UP or ItemTouchHelper.DOWN
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                Collections.swap(
                    pageAdapter.list,
                    viewHolder.adapterPosition,
                    target.adapterPosition
                )
                pageAdapter.notifyItemMoved(
                    viewHolder.adapterPosition,
                    target.adapterPosition
                )
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

        })
        itemTouchHelper?.attachToRecyclerView(binding.rcvPage)
    }

    override fun initListener() {
        super.initListener()
        binding.ivBack.setOnClickListener(this)
        binding.ivRemoveSuggest.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.iv_back -> {
                onBackPressed()
            }
            R.id.iv_remove_suggest -> {
                ViewUtils.collapse(binding.lnSuggest)
            }
        }
    }

    override fun viewBinding(): ActivityOrganizeBinding {
        return ActivityOrganizeBinding.inflate(LayoutInflater.from(this))
    }

}