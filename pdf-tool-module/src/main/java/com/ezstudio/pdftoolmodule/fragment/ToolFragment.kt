package com.ezstudio.pdftoolmodule.fragment

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ezstudio.pdftoolmodule.PdfToolBaseFragment
import com.ezstudio.pdftoolmodule.activity.*
import com.ezstudio.pdftoolmodule.adapter.FunctionAdapter
import com.ezstudio.pdftoolmodule.databinding.FragmentToolBinding
import com.ezstudio.pdftoolmodule.utils.Config

class ToolFragment : PdfToolBaseFragment<FragmentToolBinding>() {

    private val functionAdapter by lazy {
        FunctionAdapter(requireActivity(), Config.lstFunction())
    }

    companion object {

        fun getInstance(): ToolFragment {
            return ToolFragment()
        }
    }

    override fun initView() {
        toolViewModel.getAllFileInDevice()
        binding.rcvFunction.adapter = functionAdapter
    }

    override fun initData() {
    }

    override fun initListener() {
        functionAdapter.itemClickListener = {
            when (it.id) {
                Config.FUCNID.MERGE.id -> {
                    activityLauncher.launch(
                        Intent(
                            requireActivity(),
                            MergeActivity::class.java
                        )
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            result.data?.getStringExtra(MergeActivity.FILE_PATH)?.let {
                                toolViewModel.pdfCreated.postValue(mutableListOf(it))
                                openSuccessScreen(mutableListOf(it))
                            }
                        }
                    }
                }
                Config.FUCNID.SPLIT.id -> {
                    callFunctionSplit()
                }
                Config.FUCNID.INVERT.id -> {
                    callFunctionInvert()
                }
                Config.FUCNID.REMOVE_DUPLICATE.id -> {
                    callFunctionRemoveDuplicate()
                }
                Config.FUCNID.ADD_PASSWORD.id -> {
                    callFunctionAddPassword()
                }
                Config.FUCNID.REMOVE_PASSWORD.id -> {
                    callFunctionRemovePassword()
                }
                Config.FUCNID.ROTATE_PAGE.id -> {
                    callFunctionRotatePage()
                }
                Config.FUCNID.ADD_WATERMARK.id -> {
                    callFunctionAddWatermark()
                }
                Config.FUCNID.ADD_IMAGE.id -> {
                    activityLauncher.launch(
                        Intent(
                            requireActivity(),
                            AddImageActivity::class.java
                        )
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            result.data?.getStringExtra(AddImageActivity.FILE_PATH)?.let {
                                toolViewModel.pdfCreated.postValue(mutableListOf(it))
                                openSuccessScreen(mutableListOf(it))
                            }
                        }
                    }
                }
                Config.FUCNID.IMAGE_TO_PDF.id -> {
                    activityLauncher.launch(
                        Intent(
                            requireActivity(),
                            ImageToPdfActivity::class.java
                        )
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            result.data?.getStringExtra(ImageToPdfActivity.FILE_PATH)?.let {
                                toolViewModel.pdfCreated.postValue(mutableListOf(it))
                                openSuccessScreen(mutableListOf(it))
                            }
                        }
                    }
                }
                Config.FUCNID.ORGANIZE_PAGES.id -> {
                    showBottomAddFile(1) {
                        if (it.isNotEmpty()) {
                            if (checkFileProtected(it[0])) {
                                return@showBottomAddFile
                            }
                            activityLauncher.launch(
                                Intent(
                                    requireActivity(),
                                    OrganizeActivity::class.java
                                ).apply {
                                    putExtra(OrganizeActivity.FILE_PATH, it[0].path)
                                }
                            ) { result ->
                                if (result.resultCode == Activity.RESULT_OK) {
                                    result.data?.getStringExtra(OrganizeActivity.FILE_PATH)?.let {
                                        toolViewModel.pdfCreated.postValue(mutableListOf(it))
                                        openSuccessScreen(mutableListOf(it))
                                    }
                                }
                            }
                        }
                    }
                }
                Config.FUCNID.EXTRACT_IMAGE.id -> {
                    showBottomAddFile(1) {
                        if (it.isNotEmpty()) {
                            if (checkFileProtected(it[0])) {
                                return@showBottomAddFile
                            }
                            activityLauncher.launch(
                                Intent(
                                    requireActivity(),
                                    ExtractActivity::class.java
                                ).apply {
                                    putExtra(ExtractActivity.FILE_PATH, it[0].path)
                                }
                            ) {

                            }
                        }
                    }
                }
                else -> {

                }
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentToolBinding {
        return FragmentToolBinding.inflate(inflater, container, false)
    }
}