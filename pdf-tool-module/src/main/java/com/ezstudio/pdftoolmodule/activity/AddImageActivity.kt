package com.ezstudio.pdftoolmodule.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.pdftoolmodule.PdfToolBaseActivity
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.adapter.ImageAdapter
import com.ezstudio.pdftoolmodule.databinding.ActivityAddImageBinding
import com.ezstudio.pdftoolmodule.model.FileModel
import com.ezteam.baseproject.photopicker.PickImageActivity
import com.ezteam.baseproject.utils.DateUtils
import com.ezteam.baseproject.utils.ViewUtils
import java.util.*

class AddImageActivity : PdfToolBaseActivity<ActivityAddImageBinding>(), View.OnClickListener {

    private var fileSelected: FileModel? = null
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
        binding.ivAddFile.setOnClickListener(this)
        binding.ivRemoveSuggest.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.cardFileInfo.setOnClickListener(this)
        binding.ivAddImage.setOnClickListener(this)
        binding.ivDone.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun fillDataPdfFile(fileModel: FileModel) {
        this.fileSelected = fileModel
        binding.tvFileTitle.text = fileModel.name
        binding.tvFileCreateDate.text =
            "${
                DateUtils.longToDateString(
                    fileModel.date,
                    DateUtils.DATE_FORMAT_7
                )
            } | ${fileModel.sizeString}"
        binding.cardFileInfo.visibility = View.VISIBLE
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
            R.id.iv_add_file -> {
                showBottomAddFile(1) {
                    if (checkFileProtected(it[0])) {
                        return@showBottomAddFile
                    }
                    if (it.isNotEmpty()) {
                        fillDataPdfFile(it[0])
                    }
                }
            }
            R.id.iv_back -> {
                onBackPressed()
            }
            R.id.card_file_info -> {
                toolViewModel.openFile.postValue(fileSelected?.path)
            }
            R.id.iv_add_image -> {
                PickImageActivity.start(this, 1, 1000) {
                    imageAdapter.addAll(it)
                }
            }
            R.id.iv_done -> {
                when {
                    fileSelected == null -> {
                        toast(getString(R.string.please_select_pdf_file))
                    }
                    imageAdapter.list.isNullOrEmpty() -> {
                        toast(getString(R.string.please_select_image))
                    }
                    else -> {
                        callFunctionAddImage(fileSelected!!.path, imageAdapter.list)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (imageAdapter.list.isNotEmpty() || fileSelected != null) {
            showDialogDiscard {
                if (it) {
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    override fun viewBinding(): ActivityAddImageBinding {
        return ActivityAddImageBinding.inflate(LayoutInflater.from(this))
    }


}