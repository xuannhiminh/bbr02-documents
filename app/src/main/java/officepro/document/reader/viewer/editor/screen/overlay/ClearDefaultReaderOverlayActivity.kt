package office.pdf.document.reader.viewer.editor.screen.overlay;

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.DefaultReaderUninstallInstructDialogBinding

class ClearDefaultReaderOverlayActivity : AppCompatActivity() {

    private lateinit var binding: DefaultReaderUninstallInstructDialogBinding

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind the layout
        binding = DefaultReaderUninstallInstructDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // OK button to close
        binding.btnOk.setOnClickListener {
            finish()
        }

        val highlightStep1 = getString(R.string.set_default)
        val fullTextStep1 = getString(R.string.select_set_as_default)
        val spannableStep1 = SpannableString(fullTextStep1)

        // Find the position of app name
        val startIndexStep1 = fullTextStep1.indexOf(highlightStep1)
        val endIndexStep1 = startIndexStep1 + highlightStep1.length

        if (startIndexStep1 != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannableStep1.setSpan(ForegroundColorSpan(redColor), startIndexStep1, endIndexStep1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.step1.text = spannableStep1

        val highlightStep2 = getString(R.string.clear_default)
        val fullTextStep2 = getString(R.string.click_clear_default)
        val spannableStep2 = SpannableString(fullTextStep2)

        // Find the position of app name
        val startIndexStep2 = fullTextStep2.indexOf(highlightStep2)
        val endIndexStep2 = startIndexStep2 + highlightStep2.length

        if (startIndexStep2 != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannableStep2.setSpan(ForegroundColorSpan(redColor), startIndexStep2, endIndexStep2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.step2.text = spannableStep2

        val highlightStep3 = "<"
        val fullTextStep3 = getString(R.string.click_back_to_app)
        val spannableStep3 = SpannableString(fullTextStep3)

        // Find the position of app name
        val startIndexStep3 = fullTextStep3.indexOf(highlightStep3)
        val endIndexStep3= startIndexStep3 + highlightStep3.length

        if (startIndexStep3 != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannableStep3.setSpan(ForegroundColorSpan(redColor), startIndexStep3, endIndexStep3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.step3.text = spannableStep3
    }
}

