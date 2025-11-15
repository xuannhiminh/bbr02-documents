package office.pdf.document.reader.viewer.editor.screen.start

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import office.pdf.document.reader.viewer.editor.databinding.ItemIntroPageBinding

// Data class chứa dữ liệu cho từng trang
data class IntroPage(
    val animationRes: Int,
    val titleRes: Int,
    val description: CharSequence
)

class IntroPagerAdapter(
    private val context: Context,
    private val pages: List<IntroPage>
) : RecyclerView.Adapter<IntroPagerAdapter.IntroViewHolder>() {

    inner class IntroViewHolder(val binding: ItemIntroPageBinding) : RecyclerView.ViewHolder(binding.root)
    var onSkipClick: (() -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        val binding = ItemIntroPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IntroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        val page = pages[position]
        with(holder.binding) {
            animationView.setAnimation(page.animationRes)
            animationView.playAnimation()
            btnSkip.setOnClickListener {
                onSkipClick?.invoke()
            }
            textTitle.text = context.getString(page.titleRes)
            textContent.text = page.description
        }
    }

    override fun getItemCount(): Int = pages.size
}
