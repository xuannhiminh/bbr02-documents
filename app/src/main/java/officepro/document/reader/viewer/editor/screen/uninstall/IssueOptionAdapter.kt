import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import officepro.document.reader.viewer.editor.R
import android.graphics.Typeface
import android.graphics.Color
import android.widget.LinearLayout

class IssueOptionAdapter(
    private val options: List<Int>,  // 🔄 đổi sang List<Int>
    private var selectedIndex: Int = -1,
    private val onSelected: (Int) -> Unit
) : RecyclerView.Adapter<IssueOptionAdapter.IssueViewHolder>() {

    inner class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_language)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val rdSelect: RadioButton = itemView.findViewById(R.id.rd_select)
        val tvItem: LinearLayout = itemView.findViewById(R.id.tv_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_issue, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val context = holder.itemView.context
        val redColor = ContextCompat.getColor(context, R.color.issue_background_color)


        holder.tvTitle.text = context.getString(options[position])
        val isSelected = position == selectedIndex
        holder.rdSelect.isChecked = isSelected

        holder.cardView.strokeColor = if (isSelected)
            ContextCompat.getColor(context, R.color.primaryColor)
        else
            ContextCompat.getColor(context, android.R.color.transparent)

        holder.tvTitle.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)

//        holder.tvItem.setBackgroundColor(if (isSelected) redColor else Color.TRANSPARENT)

        holder.cardView.setOnClickListener {
            val oldIndex = selectedIndex
            selectedIndex = position
            notifyItemChanged(oldIndex)
            notifyItemChanged(selectedIndex)
            onSelected(position)
        }
    }


    override fun getItemCount(): Int = options.size
}

