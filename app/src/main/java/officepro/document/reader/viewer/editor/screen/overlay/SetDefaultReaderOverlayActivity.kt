package office.pdf.document.reader.viewer.editor.screen.overlay;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import office.pdf.document.reader.viewer.editor.databinding.DefaultReaderUninstallInstructDialogBinding

class SetDefaultReaderOverlayActivity : AppCompatActivity() {

    private lateinit var binding: DefaultReaderUninstallInstructDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind the layout
        binding = DefaultReaderUninstallInstructDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make root clickable so taps outside the dialog dismiss it
        binding.root.setOnClickListener {
            finish()
        }

        // Prevent clicks from dismissing when tapping inside the dialog
        binding.root.getChildAt(0).setOnClickListener {
            // Do nothing — consume the click
        }

        // OK button to close
        binding.btnOk.setOnClickListener {
            finish()
        }
    }
}

