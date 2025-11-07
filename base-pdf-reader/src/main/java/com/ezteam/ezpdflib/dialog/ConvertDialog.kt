package com.ezteam.ezpdflib.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.ezteam.ezpdflib.databinding.ConvertDialogBinding

class ConvertDialog : DialogFragment() {
    private var _binding: ConvertDialogBinding? = null
    private val binding get() = _binding!!

    private var title: String = ""
    private var message: String = ""
    private var onConfirm: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ConvertDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = title

        binding.btnOk.setOnClickListener {
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setDimAmount(0.5f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setTitle(title: String): ConvertDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String): ConvertDialog {
        this.message = message
        return this
    }

    fun setOnConfirmListener(callback: () -> Unit): ConvertDialog {
        this.onConfirm = callback
        return this
    }
}
