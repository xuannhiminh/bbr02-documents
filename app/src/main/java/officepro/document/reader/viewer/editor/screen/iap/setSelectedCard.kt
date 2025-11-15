package officepro.document.reader.viewer.editor.screen.iap

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView

fun setSelectedCard(
    selectedCard: CardView,
    endSection: LinearLayout,
    otherCard: CardView,
    otherEndSection: LinearLayout,
    otherEndSection2: LinearLayout? = null,
    otherCard2: CardView? = null ) {
    Log.d("setSelectedCard", "Setting selected card with endSection: ${endSection.id}")

    val borderWidth = 1f.dpToPx(selectedCard.context).toInt()
    val cornerRadius = 12f.dpToPx(selectedCard.context)

    // Create gradient border drawable (red -> yellow)
    val borderDrawable = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(
            Color.parseColor("#F8C83C"),
            Color.parseColor("#FE2160")
        )
    ).apply {
        this.cornerRadius = cornerRadius
    }

    // Inner drawable (background with #33FFFFFF)
    val innerDrawable = GradientDrawable().apply {
        this.setColor(Color.parseColor("#4D3946"))
        this.cornerRadius = cornerRadius - borderWidth
    }

    val layerDrawable = LayerDrawable(arrayOf(borderDrawable, innerDrawable)).apply {
        // Inset innerDrawable to create visible border
        setLayerInset(1, borderWidth, borderWidth, borderWidth, borderWidth)
    }

    val unselectedBackground = GradientDrawable().apply {
        this.cornerRadius = cornerRadius
        setColor(Color.parseColor("#4D3946"))
    }

    // Apply backgrounds
    selectedCard.background = layerDrawable
    otherCard.background = unselectedBackground
    otherCard2?.background = unselectedBackground

    // Apply gradient shader to all TextView in selected end section
    Handler(Looper.getMainLooper()).postDelayed({
        endSection.post {
            val width = endSection.width
            if (width > 0) {
                val gradientShader = LinearGradient(
                    0f, 0f, width.toFloat(), 0f, // horizontal gradient
                    intArrayOf(
                        Color.parseColor("#F8C83C"),
                        Color.parseColor("#FE2160")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                for (i in 0 until endSection.childCount) {
                    val child = endSection.getChildAt(i)
                    if (child is TextView) {
                        child.paint.shader = gradientShader
                        child.invalidate()
                    }
                }
            }
        }
    }, 50)

    // Reset other end section text color
    Handler(Looper.getMainLooper()).postDelayed({
        otherEndSection.post {
            for (i in 0 until otherEndSection.childCount) {
                val child = otherEndSection.getChildAt(i)
                if (child is TextView) {
                    child.paint.shader = null
                    child.setTextColor(Color.WHITE)
                    child.invalidate()
                }
            }
        }
        otherEndSection2?.post {
            for (i in 0 until otherEndSection.childCount) {
                val child = otherEndSection.getChildAt(i)
                if (child is TextView) {
                    child.paint.shader = null
                    child.setTextColor(Color.WHITE)
                    child.invalidate()
                }
            }
        }
    }, 50)
}

// Extension function: convert dp to px
fun Float.dpToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}