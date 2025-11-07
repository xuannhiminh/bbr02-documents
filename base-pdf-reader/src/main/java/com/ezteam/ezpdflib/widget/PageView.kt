package com.ezteam.ezpdflib.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GestureDetectorCompat
import com.ezteam.nativepdf.Annotation
import com.ezteam.nativepdf.TextSelector
import com.ezteam.nativepdf.TextWord
import com.ezteam.ezpdflib.activity.Mode
import com.ezteam.ezpdflib.activity.PdfDetailActivity
import com.ezteam.ezpdflib.model.AnnotationValue
import com.ezteam.ezpdflib.model.SingleAnnotation
import com.ezteam.ezpdflib.model.SingleSize
import com.ezteam.ezpdflib.model.SingleTextword
import com.ezteam.ezpdflib.util.Config
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.ezteam.ezpdflib.widget.zoomview.ZoomLayout
import java.util.*
import kotlin.collections.ArrayList

class PageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs),
    GestureDetector.OnGestureListener {

    private fun getZoomLayout(): ZoomLayout? =
        (context as? PdfDetailActivity)?.binding?.zoomlayout

    private fun toDocumentCoords(x: Float, y: Float): Pair<Float, Float> {
        val zoomLayout = getZoomLayout() ?: return Pair(x / scaleWidth, y / scaleHeight)

        // Compose full transform: document -> view internal (page) scale, then zoom, then pan.
        val full = Matrix().apply {
            // 1. page scaling (doc coordinate to PageView coordinate)
            setScale(scaleWidth * zoomLayout.engine.realZoom, scaleHeight * zoomLayout.engine.realZoom)
            // 2. pan (translation) applied after zoom
            postTranslate(zoomLayout.engine.scaledPanX, zoomLayout.engine.scaledPanY)
        }

        // Invert to go screen (touch) -> document
        val inverse = Matrix()
        return if (full.invert(inverse)) {
            val pts = floatArrayOf(x, y)
            inverse.mapPoints(pts)
            Pair(pts[0], pts[1])
        } else {
            // Fallback: naive (may miss pan)
            Pair(x / scaleWidth, y / scaleHeight)
        }
    }


    var clickAble = false
    private lateinit var detector: GestureDetectorCompat
    var annotation: Array<Annotation>? = null
    var textWord: Array<Array<TextWord>>? = null
    var selectDelete: RectF? = null
    var selectText: RectF? = null
    var mode = Mode.Normal
    var drawing: ArrayList<ArrayList<PointF>>? = null
    var drawingRedo = arrayListOf<ArrayList<PointF>>()
    var paintDraw = arrayListOf<AnnotationValue>()
    private val scaleHeight by lazy {
        height.toFloat() / SingleSize.getInstance().pointY
    }
    private val scaleWidth by lazy {
        width.toFloat() / SingleSize.getInstance().pointX
    }

    private val marginLeft by lazy {
        (SingleSize.getInstance().screenW - SingleSize.getInstance().pageWidth) / 2
    }

    private val marginTop by lazy {
        (SingleSize.getInstance().screenH - SingleSize.getInstance().pageHeight) / 2
    }
    var annotationSelect: ((Int?) -> Unit)? = null

    init {
        initView()
    }

    private fun initView() {
        detector = GestureDetectorCompat(context, this)
    }

    fun undoListener(isUndo: Boolean) {
        if (isUndo) {
            drawing?.let {
                if (it.isNotEmpty()) {
                    val item = it.last()
                    drawingRedo.add(item)
                    it.remove(item)
                    invalidate()
                }
            }
        } else {
            if (drawingRedo.isNotEmpty()) {
                val item = drawingRedo.last()
                drawing?.add(item)
                drawingRedo.remove(item)
                invalidate()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (clickAble) {
            when (mode) {
                Mode.Ink -> {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> touchStart(event.x, event.y)
                        MotionEvent.ACTION_MOVE -> touchMove(event.x, event.y)
                        MotionEvent.ACTION_UP -> touchUp()
                    }
                }
                Mode.Delete, Mode.CopyText, Mode.Strikeout, Mode.Unline, Mode.HighLight -> {
                    return detector.onTouchEvent(event)
                }

                else -> {}
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun touchUp() {
    }

    private fun touchMove(x: Float, y: Float) {
        val docRelXx: Float = (x) / scaleWidth
        val docRelYy: Float = (y - top + marginTop) / scaleHeight
        val (docRelX, docRelY) = toDocumentCoords(x, y)

        drawing = drawing ?: ArrayList()
        drawing?.let {
            if (it.isNotEmpty()) {
                val arc: ArrayList<PointF> = it[it.size - 1]
                arc.add(PointF(docRelX, docRelY))
                invalidate()
            }
        }
    }

    private fun touchStart(x: Float, y: Float) {
        Log.d("PageView", "touchStart: $x, $y")
        val docRelXx: Float = (x) / (scaleWidth)
        val docRelYy: Float = (y - top + marginTop) / (scaleHeight)
        val (docRelX, docRelY) = toDocumentCoords(x, y)
        Log.d("PageView", "touchStart: docRelX=$docRelX, docRelY=$docRelY")
        drawing = drawing ?: ArrayList()
        val arc = ArrayList<PointF>()
        arc.add(PointF(docRelX, docRelY))
        drawing?.add(arc)
        paintDraw.add(PreferencesUtils.getAnnotation(Config.PreferencesKey.inkValue))
        invalidate()
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent) {

    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        if (mode == Mode.Delete) {
            if (annotation == null)
                annotation = SingleAnnotation.getInstance().annotation
//            val docRelX: Float = (event.x - left) / scaleWidth
//            val docRelY: Float = (event.y - top) / scaleHeight
            val (docRelX, docRelY) = toDocumentCoords(event.x, event.y)

            selectDelete = null
            annotation?.let {
                for (item in it) {
                    if (item.contains(docRelX, docRelY)) {
                        when (item.type) {
                            Annotation.Type.HIGHLIGHT, Annotation.Type.UNDERLINE,
                            Annotation.Type.SQUIGGLY,
                            Annotation.Type.STRIKEOUT, Annotation.Type.INK -> {
                                selectDelete = item
                            }

                            else -> {}
                        }
                    }
                }
            }
            val indexAnnotation = annotation?.indexOf(selectDelete)
            annotationSelect?.invoke(if (indexAnnotation == -1) null else indexAnnotation)
        }
        invalidate()
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (mode == Mode.CopyText || mode == Mode.HighLight
            || mode == Mode.Unline || mode == Mode.Strikeout
        ) {
            if (textWord == null)
                textWord = SingleTextword.getInstance().textword

            // Kiểm tra nếu `e1` không null mới gọi `selectText()`
            e1?.let { event1 ->
                selectText(event1.x, event1.y, e2.x, e2.y)
            }
        }
        return false
    }

    //    override fun onScroll(
//        e1: MotionEvent,
//        e2: MotionEvent,
//        distanceX: Float,
//        distanceY: Float
//    ): Boolean {
//        if (mode == Mode.CopyText || mode == Mode.HighLight
//            || mode == Mode.Unline || mode == Mode.Strikeout
//        ) {
//            if (textWord == null)
//                textWord = SingleTextword.getInstance().textword
//            selectText(e1.x, e1.y, e2.x, e2.y)
//        }
//        return false
//    }
    override fun onDraw(canvas: Canvas) {
        try {
            super.onDraw(canvas)
        /*Delete*/
        selectDelete?.let {
            Paint().apply {
                style = Paint.Style.STROKE
                color = Config.BOX_COLOR
                strokeWidth = 3f
                canvas.drawRect(
                    it.left * scaleWidth,
                    it.top * scaleHeight,
                    it.right * scaleWidth,
                    it.bottom * scaleHeight,
                    this
                )
            }
        }

        /*Ink*/
        drawing?.let {
            val paint = Paint().apply {
                isAntiAlias = true
                isDither = true
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                style = Paint.Style.STROKE
            }

            for (index in 0 until it.size) {
                val arc = it[index]
                paint.apply {
                    color = paintDraw[index].getColorSetting()
                    strokeWidth = paintDraw[index].thickness.toFloat()
                }
                val path = Path()
                if (arc.size >= 2) {
                    val iit: Iterator<PointF> = arc.iterator()
                    var p = iit.next()
                    var mX: Float = p.x * scaleWidth
                    var mY: Float = p.y * scaleHeight
                    path.moveTo(mX, mY)
                    while (iit.hasNext()) {
                        p = iit.next()
                        val x: Float = p.x * scaleWidth
                        val y: Float = p.y * scaleHeight
                        path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                        mX = x
                        mY = y
                    }
                    path.lineTo(mX, mY)
                    canvas.drawPath(path, paint)
                } else {
                    val p = arc[0]
                    canvas.drawCircle(
                        p.x * scaleWidth,
                        p.y * scaleHeight,
                        paintDraw[index].thickness.toFloat(),
                        paint
                    )
                    canvas.drawPath(path, paint)
                }
            }
        }

        /*Copy, HighLight, Unline, Stroker*/
        selectText?.let { selectText ->
            textWord?.let { text ->
                Paint().apply {
                    color = Config.HIGHLIGHT_COLOR
                    strokeWidth = 3f
                    TextSelector(text, selectText).select(object : TextSelector.TextProcessor {

                        var rect: RectF? = null

                        override fun onStartLine() {
                            rect = RectF()
                        }

                        override fun onWord(word: TextWord?) {
                            word?.let {
                                rect?.union(it)
                            }
                        }

                        override fun onEndLine() {
                            rect?.let {
                                if (!it.isEmpty) {
                                    canvas.drawRect(
                                        it.left * scaleWidth,
                                        it.top * scaleHeight,
                                        it.right * scaleWidth,
                                        it.bottom * scaleHeight,
                                        this@apply
                                    )
                                }
                            }
                        }

                    })
                }
            }
        }
        } catch (e: Exception) {
            Log.d("PageView", "onDraw: ", e)
        }
    }



    private fun selectText(x0: Float, y0: Float, x1: Float, y1: Float) {
        val docRelX0: Float = (x0 - left) / scaleWidth
        val docRelY0: Float = (y0 - top) / scaleHeight
        val docRelX1: Float = (x1 - left) / scaleWidth
        val docRelY1: Float = (y1 - top) / scaleHeight
        selectText =
            if (docRelY0 <= docRelY1) RectF(docRelX0, docRelY0, docRelX1, docRelY1)
            else RectF(docRelX1, docRelY1, docRelX0, docRelY0)
        invalidate()
    }

    fun copyText(): String {
        val text = StringBuilder()
        TextSelector(SingleTextword.getInstance().textword, selectText)
            .select(object : TextSelector.TextProcessor {
                var line: StringBuilder? = null

                override fun onStartLine() {
                    line = StringBuilder()
                }

                override fun onWord(word: TextWord?) {
                    line?.let {
                        if (it.isNotEmpty()) {
                            it.append(' ')
                        }
                        it.append(word!!.w)
                    }
                }

                override fun onEndLine() {
                    if (text.isNotEmpty()) text.append('\n')
                    text.append(line)
                }
            })
        return text.toString()
    }

    fun getMarkupSelectionPoint(): ArrayList<PointF> {
        val quadPoints = ArrayList<PointF>()
        TextSelector(SingleTextword.getInstance().textword, selectText)
            .select(object : TextSelector.TextProcessor {
                var rect: RectF? = null
                override fun onStartLine() {
                    rect = RectF()
                }

                override fun onWord(word: TextWord?) {
                    rect?.union(word!!)
                }

                override fun onEndLine() {
                    rect?.let {
                        if (!it.isEmpty) {
                            quadPoints.apply {
                                add(PointF(it.left, it.bottom))
                                add(PointF(it.right, it.bottom))
                                add(PointF(it.right, it.top))
                                add(PointF(it.left, it.top))
                            }
                        }
                    }
                }
            })
        return quadPoints
    }

    override fun onLongPress(e: MotionEvent) {

    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return true
    }

//    override fun onFling(
//        e1: MotionEvent,
//        e2: MotionEvent,
//        velocityX: Float,
//        velocityY: Float
//    ): Boolean {
//        return true
//    }
}