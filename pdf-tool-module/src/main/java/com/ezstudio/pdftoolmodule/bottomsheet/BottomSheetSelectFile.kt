package com.ezstudio.pdftoolmodule.bottomsheet

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.adapter.FileToolItemAdapter
import com.ezstudio.pdftoolmodule.databinding.BottomSelectFileBinding
import com.ezstudio.pdftoolmodule.model.FileModel
import com.ezstudio.pdftoolmodule.viewmodel.PdfToolViewModel
import com.ezteam.baseproject.utils.KeyboardUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import java.io.Serializable

class BottomSheetSelectFile : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSelectFileBinding
    private lateinit var adapter: FileToolItemAdapter
    private val toolViewModel by inject<PdfToolViewModel>()
    var listFilter = Filter.ALL

    enum class Filter : Serializable {
        ENCRYPTION, DECRYPTION, ALL
    }

    var doneListener: ((MutableList<FileModel>) -> Unit)? = null

    companion object {

        private const val MAX_ITEM_SELECT = "max item select"

        fun getInstance(
            maxItemSelect: Int = 1
        ): BottomSheetSelectFile {
            return BottomSheetSelectFile().also {
                it.arguments = bundleOf(
                    MAX_ITEM_SELECT to maxItemSelect,
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSelectFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
    }

    private fun initViews() {
        context?.let {
            KeyboardUtils.autoHideClickView(binding.root, it as Activity)
            adapter = FileToolItemAdapter(it, mutableListOf()).apply {
                allowSelect = true
            }
            arguments?.let {
                adapter.maxSelectItem = it.getInt(MAX_ITEM_SELECT, 1)
            }
            binding.rcvListFile.adapter = adapter
        }

    }

    private fun initListener() {
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
//                val lstData = when (listFilter) {
//                    Filter.ALL -> {
//                        it
//                    }
//                    Filter.ENCRYPTION -> {
//                        it.filter { !it.hasPassword }
//                    }
//                    Filter.DECRYPTION -> {
//                        it.filter { it.hasPassword }
//                    }
//                }
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
            dismiss()
        }
        binding.tvDone.setOnClickListener {
            if (adapter.lstSelected.isEmpty()) {
                Toast.makeText(context, getString(R.string.choose_at_least), Toast.LENGTH_SHORT)
                    .show()
            } else {
                doneListener?.invoke(adapter.lstSelected)
                dismiss()
            }
        }
    }

}