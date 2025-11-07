package com.ezteam.ezpdflib.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.activity.Mode
import com.ezteam.ezpdflib.databinding.LibItemPageBinding
import com.ezteam.ezpdflib.extension.reverstBitmap
import com.ezteam.ezpdflib.extension.uriToBitmap
import com.ezteam.ezpdflib.model.PagePdf
import com.ezteam.ezpdflib.model.SingleSize
import com.ezteam.ezpdflib.util.PreferencesKey
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.ezteam.ezpdflib.viewmodel.DetailViewmodel
import com.ezteam.ezpdflib.widget.stickerView.DrawableSticker
import com.ezteam.ezpdflib.widget.stickerView.Sticker
import com.ezteam.ezpdflib.widget.stickerView.StickerView
import com.ezteam.ezpdflib.widget.stickerView.TextSticker
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*


class PdfPageAdapter(
    var context: Context,
    var lstPage: MutableList<PagePdf>,
    var viewmodel: DetailViewmodel
) :
    RecyclerView.Adapter<PdfPageAdapter.ViewHolder>() {

    var canDrawPage = false
    var ratioPDF = "1:1.4142" // Default ratio for A4 paper
    var annotationSelect: ((Int?) -> Unit)? = null
    var mode = Mode.Normal
    var currentIndex = -1
    var isHorizontal = false
    var fileSignature: File? = null
    var cleanSignature: ((Unit) -> Unit)? = null
    var textClickListener: ((Unit) -> Unit)? = null
    var lstPagerAds = mutableListOf<Int>()
    var lstPagerAdsView = mutableMapOf<Int, RelativeLayout?>()

    fun updateItem(index: Int, uri: Uri) {
        if (index < 0 || index >= lstPage.size) {
            return
        }
        lstPage[index].uriImage = uri
        notifyItemChanged(index)
    }

    fun add(index: Int, item: PagePdf) {
        lstPage.add(index, item)
        notifyItemInserted(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lib_item_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(lstPage[position], position)
    }

    override fun getItemCount(): Int {
        return lstPage.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemBinding = LibItemPageBinding.bind(itemView)

        fun bindData(pagePdf: PagePdf, index: Int) {
            val param = itemView.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0, 0, 0, 0)
            if (!isHorizontal && itemCount > 1) {
                if (index == 0) {
                    param.setMargins(
                        0,
                        itemView.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._60sdp),
                        0,
                        0
                    )
                } else if (index == itemCount - 1) {
                    param.setMargins(
                        0,
                        0,
                        0,
                        itemView.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._80sdp),
                    )
                }
            }
            if (pagePdf.uriImage == null && lstPagerAds.contains(index)) {
                itemBinding.imPage.visibility = View.GONE
                itemBinding.lnAds.visibility = View.VISIBLE
                lstPagerAdsView[index]?.parent?.let {
                    (it as ViewGroup).removeView(lstPagerAdsView[index])
                }
                itemBinding.lnAds.removeAllViews()
                itemBinding.lnAds.invalidate()
                lstPagerAdsView[index]?.let {
                    itemBinding.lnAds.addView(it)
                }
                itemBinding.circularProgressBar.visibility = View.INVISIBLE
            } else {
                val card = itemBinding.cardPage
                val pageWidth = SingleSize.getInstance().pageWidth.toFloat()
                val pageHeight = SingleSize.getInstance().pageHeight.toFloat()

                // Safety: avoid zero
//                if (pageWidth > 0f && pageHeight > 0f) {
//                    // Format "W:H" e.g. "595:842" or normalized if you want
//                    ratioPDF = "$pageWidth:$pageHeight"
//                }

                val lp = card.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                lp.dimensionRatio = ratioPDF
                card.layoutParams = lp


                itemBinding.imPage.visibility = View.VISIBLE
                itemBinding.lnAds.visibility = View.GONE

                pagePdf.uriImage?.let {
                    itemBinding.circularProgressBar.visibility = View.INVISIBLE
                } ?: run {
                    itemBinding.circularProgressBar.visibility = View.VISIBLE
                }

                Picasso.get()
                    .load(if (index == viewmodel.lastIndexSearch) viewmodel.uriSearch else pagePdf.uriImage)
                    .into(itemBinding.imPage)

                itemBinding.imPage.apply {
                    if (index == currentIndex) {
                        mode = this@PdfPageAdapter.mode
                        annotationSelect = this@PdfPageAdapter.annotationSelect
                    }
                    clickAble = canDrawPage
                    selectDelete = null
                    drawing = null
                    drawingRedo = arrayListOf()
                    paintDraw = arrayListOf()
                    annotation = null
                    textWord = null
                    selectText = null
                }

                itemBinding.viewSignature.removeAllStickers()
                if (index == currentIndex && fileSignature != null &&
                    (mode == Mode.Signature || mode == Mode.AddImage)
                ) {
                    itemBinding.imPage.clickAble = false
                    itemBinding.viewSignature.apply {
                        layoutParams.width = itemBinding.imPage.width
                        layoutParams.height = itemBinding.imPage.height
                        visibility = View.VISIBLE
                        removeAllStickers()
                        val nightMode = PreferencesUtils.getBoolean(
                            PreferencesKey.KeyPress.PDF_VIEWER_NIGHT_MODE
                        )
                        val sticker = if (nightMode) {
                            DrawableSticker(
                                context,
                                Uri.fromFile(fileSignature).uriToBitmap(context).reverstBitmap()
                            )
                        } else {
                            DrawableSticker(Drawable.createFromPath(fileSignature?.path))
                        }
                        addSticker(sticker)

                        onStickerOperationListener =
                            object : StickerView.OnStickerOperationListener {
                                override fun onStickerAdded(sticker: Sticker) {
                                }

                                override fun onStickerClicked(sticker: Sticker) {
                                }

                                override fun onStickerDeleted(sticker: Sticker) {
                                    cleanSignature?.invoke(Unit)
                                }

                                override fun onStickerDragFinished(sticker: Sticker) {
                                }

                                override fun onStickerTouchedDown(sticker: Sticker) {
                                }

                                override fun onStickerZoomFinished(sticker: Sticker) {
                                }

                                override fun onStickerFlipped(sticker: Sticker) {
                                }

                                override fun onStickerDoubleTapped(sticker: Sticker) {
                                }

                            }

                    }
                }

                if (index == currentIndex && mode == Mode.AddText) {
                    itemBinding.imPage.clickAble = false
                    itemBinding.viewSignature.apply {
                        layoutParams.width = itemBinding.imPage.width
                        layoutParams.height = itemBinding.imPage.height
                        visibility = View.VISIBLE
                        removeAllStickers()
                        val sticker = TextSticker(
                            context,
                            context.resources.getDrawable(R.drawable.bg_color_00)
                        ).apply {
                            setText(context.getString(R.string.add_text))
                            resizeText()
                        }
                        addSticker(sticker)

                        onStickerOperationListener =
                            object : StickerView.OnStickerOperationListener {
                                override fun onStickerAdded(sticker: Sticker) {
                                }

                                override fun onStickerClicked(sticker: Sticker) {
                                    textClickListener?.invoke(Unit)
                                }

                                override fun onStickerDeleted(sticker: Sticker) {
                                    cleanSignature?.invoke(Unit)
                                }

                                override fun onStickerDragFinished(sticker: Sticker) {
                                }

                                override fun onStickerTouchedDown(sticker: Sticker) {
                                }

                                override fun onStickerZoomFinished(sticker: Sticker) {
                                }

                                override fun onStickerFlipped(sticker: Sticker) {
                                }

                                override fun onStickerDoubleTapped(sticker: Sticker) {
                                }

                            }

                    }
                }
            }
        }
    }

}