package office.pdf.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.UpdateDialogBinding

class UpdateDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: UpdateDialogBinding? = null
    private val binding get() = _binding!!
    private var isViewDestroyed = false
    var listener: Unit.() -> Unit = {}

    // Data from Firebase
    var appVersion: String = "1.0.1"
    var forceUpdate: Boolean = false
    var userCount: Number = 10000
    var updateFeatures: String = "\n"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        // Intercept BACK key
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK // consume the Back event
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = UpdateDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false

        binding.ivClose.isVisible = !forceUpdate

        try {
            val features = updateFeatures.split("\\n")

            binding.feature1.apply {
                val featureText = features.getOrNull(0) ?: ""
                text = featureText
                visibility = if (featureText.isEmpty()) View.GONE else View.VISIBLE
            }

            binding.feature2.apply {
                val featureText = features.getOrNull(1) ?: ""
                text = featureText
                visibility = if (featureText.isEmpty()) View.GONE else View.VISIBLE
            }

            binding.feature3.apply {
                val featureText = features.getOrNull(2) ?: ""
                text = featureText
                visibility = if (featureText.isEmpty()) View.GONE else View.VISIBLE
            }


            binding.tvVersion.text = appVersion
            binding.tvSubtitle.text = getString(R.string.update_title, userCount)

            val userCountStr = userCount.toString()
            val fullText = getString(R.string.update_title, userCountStr)
            val update = getString(R.string.update)
            val desc = getString(R.string.update_desc)

            val styledTextSubtitle = highlightText(
                context = requireContext(),
                fullText = fullText,
                highlightPart = userCountStr
            )
            val styledTextDesc = highlightText(
                context = requireContext(),
                fullText = desc,
                highlightPart = update
            )

            binding.tvSubtitle.text = styledTextSubtitle
            binding.tvDesc.text = styledTextDesc


            val rawText = "* " + getString(R.string.update_note)
            val spannable = SpannableString(rawText)

            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                RelativeSizeSpan(1.4f),
                0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.tvNote.text = spannable

            if(forceUpdate) {
                binding.tvNote.visibility = View.GONE
            } else {
                binding.tvNote.visibility = View.VISIBLE
            }

            binding.btnUpdate.setOnClickListener {
                dismiss()
                listener.invoke(Unit)
            }
            binding.ivClose.setOnClickListener {
                dismiss()
            }
        } catch (e: Exception) {
            Log.e("UpdateDialog", "Error update: ${e.message}")
        }



        // Setup feature adapter
//        val adapter = UpdateFeatureAdapter()
//        binding.rvFeature.adapter = adapter
//        adapter.submitList(featureList)
    }
   private fun highlightText(
        context: Context,
        fullText: String,
        highlightPart: String
    ): SpannableString {
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(highlightPart)
        val endIndex = startIndex + highlightPart.length

        if (startIndex != -1) {
            val color = ContextCompat.getColor(context, R.color.primaryColor)
            spannable.setSpan(
                ForegroundColorSpan(color),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                AbsoluteSizeSpan(24, true),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
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
        isViewDestroyed = true
        _binding = null
    }
}
