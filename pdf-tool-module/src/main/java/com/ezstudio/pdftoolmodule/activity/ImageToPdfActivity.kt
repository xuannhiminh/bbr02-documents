package com.ezstudio.pdftoolmodule.activity

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.pdftoolmodule.PdfToolBaseActivity
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.adapter.ImageAdapter
import com.ezstudio.pdftoolmodule.databinding.ActivityImageToPdfBinding
import com.ezteam.baseproject.photopicker.PickImageActivity
import com.ezteam.baseproject.utils.ViewUtils
//import com.google.android.gms.ads.ez.EzAdControl
import java.util.*
import kotlin.collections.ArrayList

class ImageToPdfActivity : PdfToolBaseActivity<ActivityImageToPdfBinding>(), View.OnClickListener {

    private var itemTouchHelper: ItemTouchHelper? = null

    private val imageAdapter by lazy {
        ImageAdapter(this, mutableListOf())
    }

    companion object {
        const val FILE_PATH = "file path"
    }

    override fun initView() {
        binding.rcvPhoto.adapter = imageAdapter
        dragItemAdapter()
    }

    override fun initData() {
    }

    override fun initListener() {
        super.initListener()
        binding.ivAddImage.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.ivDone.setOnClickListener(this)
        binding.ivRemoveSuggest.setOnClickListener(this)
    }

    private fun dragItemAdapter() {
        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeFlag(
                    ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                imageAdapter.let {
                    Collections.swap(
                        it.list,
                        viewHolder.adapterPosition,
                        target.adapterPosition
                    )
                    imageAdapter.notifyItemMoved(
                        viewHolder.adapterPosition,
                        target.adapterPosition
                    )
                }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

        })
        itemTouchHelper?.attachToRecyclerView(binding.rcvPhoto)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.iv_remove_suggest -> {
                ViewUtils.collapse(binding.lnSuggest)
            }
            R.id.iv_back -> {
                onBackPressed()
            }
            R.id.iv_add_image -> {
                PickImageActivity.start(this, 1, 1000) {
                    imageAdapter.addAll(it)
                }
            }
            R.id.iv_done -> {
                when {
                    imageAdapter.list.isNullOrEmpty() -> {
                        toast(getString(R.string.please_select_image))
                    }
                    else -> {
                        val lstPath = mutableListOf<String>()
                        imageAdapter.list.forEachIndexed { _, uri ->
                            uri.path?.let { lstPath.add(it) }
                        }
                        callFunctionImageToPdf(lstPath as ArrayList<String>)
                    }
                }
            }
        }
    }

    override fun viewBinding(): ActivityImageToPdfBinding {
        return ActivityImageToPdfBinding.inflate(LayoutInflater.from(this))
    }

}