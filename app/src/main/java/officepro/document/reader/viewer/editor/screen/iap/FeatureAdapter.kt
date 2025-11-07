import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import officepro.document.reader.viewer.editor.R

class FeatureAdapter(private val items: List<FeatureItem>) : RecyclerView.Adapter<FeatureAdapter.FeatureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feature, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class FeatureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvFeatureName: TextView = view.findViewById(R.id.tvFeatureName)
        private val icPro: ImageView = view.findViewById(R.id.icPro)
        private val icBasic: ImageView = view.findViewById(R.id.icBasic)

        fun bind(item: FeatureItem) {
            tvFeatureName.text = item.name

            if (item.isProAvailable) {
                icPro.setImageResource(R.drawable.icon_check_pro)
                applyGradientToImageView(icPro)
            } else {
                icPro.setImageDrawable(null)
            }

            icBasic.setImageResource(
                if (item.isBasicAvailable) R.drawable.ic_check else R.drawable.ic_base_close
            )
        }

        private fun applyGradientToImageView(imageView: ImageView) {
            val drawable = ContextCompat.getDrawable(imageView.context, R.drawable.ic_check) ?: return
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            val paint = Paint()
            val shader = LinearGradient(
                0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(),
                intArrayOf(Color.parseColor("#FEB743"), Color.parseColor("#FFB681")),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = shader
            canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)

            imageView.setImageDrawable(BitmapDrawable(imageView.resources, bitmap))
        }
    }
}