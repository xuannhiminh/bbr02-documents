package office.file.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.GestureDetector.OnGestureListener;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.ImageView.ScaleType;

import com.artifex.solib.ConfigOptions;
import com.artifex.solib.SOBitmap;
import com.artifex.solib.SODoc;
import com.artifex.solib.SOSelectionLimits;
import com.artifex.solib.o;

import java.util.ArrayList;
import java.util.Iterator;

import office.file.ui.editor.DocPageView.ExternalLinkListener;
import office.file.ui.editor.History.HistoryItem;
import office.file.ui.editor.NoteEditor.NoteDataHandler;
import office.file.ui.editor.R.color;
import office.file.ui.editor.R.integer;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.R.string;

public class DocView extends AdapterView<Adapter> implements OnGestureListener, OnScaleGestureListener, DragHandleListener, Runnable {
    private static final int DOUBLE_TAP_TIME = 300;
    private static float MOVE_THRESHOLD;
    private static final int RESIZE_MIN_DIMENSION = 100;
    private static final int SHOW_KEYBOARD_TIME = 500;
    private static final int SMOOTH_SCROLL_TIME = 400;
    private static final String TAG = "DocView";
    protected static final int UNSCALED_GAP = 20;
    private static int ZOOM_DURATION;
    private static float ZOOM_FACTOR;
    private static int ZOOM_PERIOD;
    private int bitmapIndex = 0;
    private SOBitmap[] bitmaps;
    private int dropY = -1;
    private boolean flinging = false;
    private int goToThisPage = -1;
    protected int lastMostVisibleChild = -1;
    private float lastTapX;
    private float lastTapY;
    private PageAdapter mAdapter;
    private boolean mAddComment = false;
    protected final Rect mAllPagesRect = new Rect();
    private final Rect mChildRect = new Rect();
    private final SparseArray<View> mChildViews = new SparseArray(3);
    protected ConfigOptions mConfigOptions = null;
    private boolean mConstrained = true;
    private SODoc mDoc;
    private boolean mDonePressZooming = false;
    private DragHandle mDragHandle = null;
    private Point mDragOrigLocation = new Point();
    private boolean mDragging = false;
    private RectF mDraggingObjectPageBounds;
    private int mDropPageAbove = -1;
    private int mDropPageBelow = -1;
    private boolean mFinished = false;
    protected int mForceColumnCount = -1;
    private boolean mForceLayout = false;
    private GestureDetector mGestureDetector;
    private History mHistory;
    protected DocViewHost mHostActivity = null;
    private final Rect mLastAllPagesRect = new Rect();
    protected int mLastLayoutColumns = 1;
    public float mLastReflowWidth = 0.0F;
    private float mLastScale = 0.0F;
    private int mLastScrollX = 0;
    private int mLastScrollY = 0;
    private long mLastTapTime = 0L;
    private boolean mMoving = false;
    private float mNAdditionalAngle = 0.0F;
    private PointF mNatDim;
    private NoteEditor mNoteEditor = null;
    private boolean mPagesShowing = false;
    private DocPageView mPressPage;
    private float mPressStartScale;
    private int mPressStartViewX;
    private int mPressStartViewY;
    private int mPressStartX;
    private int mPressStartY;
    private boolean mPressZooming = false;
    protected boolean mPressing = false;
    private boolean mPreviousReflowMode = false;
    private boolean mReflowMode = false;
    private int mReflowWidth = -1;
    private float mResizeAspect;
    private DragHandle mResizeHandleBottomLeft = null;
    private DragHandle mResizeHandleBottomRight = null;
    private DragHandle mResizeHandleTopLeft = null;
    private DragHandle mResizeHandleTopRight = null;
    private Point mResizeOrigBottomRight;
    private Rect mResizeOrigRect = new Rect();
    private Point mResizeOrigTopLeft;
    private Rect mResizeRect = new Rect();
    private SOBitmap mResizingBitmap = null;
    private ImageView mResizingView;
    private float mRotateAngle = 0.0F;
    private DragHandle mRotateHandle = null;
    protected float mScale = 1.0F;
    private ScaleGestureDetector mScaleGestureDetector;
    protected boolean mScaling;
    private boolean mScrollRequested = false;
    private Scroller mScroller;
    private int mScrollerLastX;
    private int mScrollerLastY;
    private boolean mScrollingStopped = false;
    protected DocPageView mSelectionEndPage;
    private DragHandle mSelectionHandleBottomRight = null;
    private DragHandle mSelectionHandleTopLeft = null;
    private SOSelectionLimits mSelectionLimits = null;
    protected DocPageView mSelectionStartPage;
    private DocView.Smoother mSmoother;
    private int mStartPage = 0;
    private int mTapStatus = 0;
    private boolean mTouching = false;
    private final Rect mViewport = new Rect();
    private final Point mViewportOrigin = new Point();
    private int mXScroll;
    private int mYScroll;
    private int mostVisibleChild = -1;
    private final Rect mostVisibleRect = new Rect();
    private boolean once = true;
    private int renderCount = 0;
    private boolean renderRequested = false;
    private Point scrollToHere;
    private DocPageView scrollToHerePage;
    private boolean scrollToSelection = false;
    private DocView.a showKeyboardListener = null;
    private int unscaledMaxw = 0;

    public DocView(Context var1) {
        super(var1);
        this.initialize(var1);
    }

    public DocView(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.initialize(var1);
    }

    public DocView(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.initialize(var1);
    }

    private int a(int var1) {
        int var2 = this.mDropPageAbove;
        if (var2 == -1) {
            var2 = this.mDropPageBelow;
            if (var2 == -1) {
                return this.getDoc().r() - 1;
            } else {
                return var1 <= var2 ? var2 : var2 + 1;
            }
        } else {
            return var1 > var2 ? var2 : var2 - 1;
        }
    }

    private Point a(int var1, RectF var2, boolean var3, int var4) {
        Rect var5 = new Rect();
        this.getGlobalVisibleRect(var5);
        int var6 = -var5.top;
        byte var7 = 0;
        var5.offset(0, var6);
        var5.inset(var4, var4);
        DocPageView var8 = (DocPageView) this.getOrCreateChild(var1);
        Point var9 = var8.pageToView((int) var2.left, (int) var2.bottom);
        Rect var10 = var8.getChildRect();
        var9.y += var10.top;
        var9.y -= this.getScrollY();
        var9.x += var10.left;
        var9.x -= this.getScrollX();
        if (var9.y >= var5.top && var9.y <= var5.bottom) {
            var1 = 0;
        } else {
            var1 = (var5.top + var5.bottom) / 2 - var9.y;
        }

        var4 = var7;
        if (var3) {
            if (var9.x >= var5.left) {
                var4 = var7;
                if (var9.x <= var5.right) {
                    return new Point(var4, var1);
                }
            }

            var4 = (var5.left + var5.right) / 2 - var9.x;
        }

        return new Point(var4, var1);
    }

    private Point a(Point var1) {
        var1 = new Point(var1);
        Rect var2 = new Rect();
        this.getGlobalVisibleRect(var2);
        var1.offset(var2.left, var2.top);
        return var1;
    }

    private DragHandle a(RelativeLayout var1, int var2) {
        DragHandle var3;
        if (var2 == 7) {
            var3 = new DragHandle(this.getContext(), layout.sodk_editor_drag_handle, var2);
        } else if (var2 == 8) {
            var3 = new DragHandle(this.getContext(), layout.sodk_editor_rotate_handle, var2);
        } else {
            var3 = new DragHandle(this.getContext(), layout.sodk_editor_resize_handle, var2);
        }

        var1.addView(var3);
        var3.show(false);
        var3.setDragHandleListener(this);
        return var3;
    }

    private void a(float var1) {
        this.mScale *= var1;
        this.scaleChildren();
        int var2 = (int) ((float) this.getScrollY() * var1);
        this.scrollTo(this.getScrollX(), var2);
        this.requestLayout();
    }

    private void a(int var1, int var2, int var3, int var4) {
        if (this.mSelectionStartPage == null) return;
        Rect var5 = this.mSelectionStartPage.screenRect();
        Rect var6 = new Rect(var1, var2, var1 + var3, var2 + var4);
        int[] var7 = new int[2];
        this.getLocationOnScreen(var7);
        var6.offset(var7[0], var7[1]);
        int var8 = var1;
        if (var6.left < var5.left) {
            var8 = var1 + (var5.left - var6.left);
        }

        int var9 = var8;
        if (var6.right > var5.right) {
            var9 = var8 - (var6.right - var5.right);
        }

        var1 = var2;
        if (var6.top < var5.top) {
            var1 = var2 + (var5.top - var6.top);
        }

        var2 = var1;
        if (var6.bottom > var5.bottom) {
            var2 = var1 - (var6.bottom - var5.bottom);
        }

        var5 = this.mResizeRect;
        var8 = var9 + var3;
        var1 = var2 + var4;
        var5.set(var9, var2, var8, var1);
        RelativeLayout.LayoutParams var10 = (RelativeLayout.LayoutParams) this.mResizingView.getLayoutParams();
        var1 -= this.getHeight();
        if (var1 > 0) {
            var1 = -var1;
        } else {
            var1 = 0;
        }

        var8 -= this.getWidth();
        if (var8 > 0) {
            var8 = -var8;
        } else {
            var8 = 0;
        }

        var10.setMargins(var9, var2, var8, var1);
        var10.width = var3;
        var10.height = var4;
        this.mResizingView.setLayoutParams(var10);
        this.mResizingView.invalidate();
        this.mResizingView.setVisibility(View.VISIBLE);
    }

    private void a(View var1) {
        android.view.ViewGroup.LayoutParams var2 = var1.getLayoutParams();
        android.view.ViewGroup.LayoutParams var3 = var2;
        if (var2 == null) {
            var3 = new android.view.ViewGroup.LayoutParams(-2, -2);
        }

        this.addViewInLayout(var1, 0, var3, true);
    }

    private void a(View var1, Float var2) {
        ((DocPageView) var1).setNewScale(var2);
    }

    private void a(DragHandle var1) {
        this.a(this.mResizeOrigTopLeft.x + (var1.getPosition().x - this.mDragOrigLocation.x), this.mResizeOrigTopLeft.y + (var1.getPosition().y - this.mDragOrigLocation.y), this.mResizeOrigBottomRight.x - this.mResizeOrigTopLeft.x, this.mResizeOrigBottomRight.y - this.mResizeOrigTopLeft.y);
    }

    private void a(boolean var1) {
        this.showHandle(this.mSelectionHandleTopLeft, var1);
        this.showHandle(this.mSelectionHandleBottomRight, var1);
    }

    private InputView b() {
        return ((NUIDocView) this.mHostActivity).getInputView();
    }

    private void b(DragHandle var1) {
        int var2;
        int var3;
        int var4;
        int var5;
        label71:
        {
            label70:
            {
                var2 = this.mResizeOrigTopLeft.y;
                var3 = this.mResizeOrigTopLeft.x;
                var4 = this.mResizeOrigBottomRight.y;
                var5 = this.mResizeOrigBottomRight.x;
                int var6 = var1.getKind();
                if (var6 != 3) {
                    if (var6 == 4) {
                        var2 = var1.getPosition().y + var1.getMeasuredHeight() / 2;
                        break label70;
                    }

                    if (var6 != 5) {
                        if (var6 != 6) {
                            break label71;
                        }

                        var4 = var1.getPosition().y + var1.getMeasuredHeight() / 2;
                        break label70;
                    }

                    var4 = var1.getPosition().y;
                    var4 += var1.getMeasuredHeight() / 2;
                } else {
                    var2 = var1.getPosition().y + var1.getMeasuredHeight() / 2;
                }

                var3 = var1.getPosition().x + var1.getMeasuredWidth() / 2;
                break label71;
            }

            var5 = var1.getPosition().x + var1.getMeasuredWidth() / 2;
        }

        var5 -= var3;
        var3 = var4 - var2;
        if (this.mNatDim.x > 0.0F && this.mNatDim.y > 0.0F) {
            var4 = this.mResizeOrigBottomRight.x - this.mResizeOrigTopLeft.x;
            var2 = this.mResizeOrigBottomRight.y - this.mResizeOrigTopLeft.y;
            double var7 = Math.sqrt((double) (var4 * var4 + var2 * var2));
            var7 = Math.sqrt((double) (var5 * var5 + var3 * var3)) / var7;
            double var9 = (double) var4;
            Double.isNaN(var9);
            Double.isNaN(var9);
            Double.isNaN(var9);
            Double.isNaN(var9);
            var5 = (int) (var9 * var7);
            var9 = (double) var2;
            Double.isNaN(var9);
            Double.isNaN(var9);
            Double.isNaN(var9);
            Double.isNaN(var9);
            var4 = (int) (var9 * var7);
            if (var5 < 100) {
                return;
            }

            var2 = var4;
            if (var4 < 100) {
                return;
            }
        } else {
            var2 = var5;
            if (var5 < 100) {
                var2 = 100;
            }

            var4 = var2;
            var5 = var2;
            var2 = var3;
            if (var3 < 100) {
                var2 = 100;
                var5 = var4;
            }
        }

        label48:
        {
            var3 = var1.getKind();
            var4 = 0;
            if (var3 != 3) {
                if (var3 != 4) {
                    if (var3 != 5) {
                        if (var3 != 6) {
                            var3 = 0;
                            break label48;
                        }

                        var4 = this.mResizeOrigTopLeft.x;
                    } else {
                        var4 = this.mResizeOrigBottomRight.x - var5;
                    }

                    var3 = this.mResizeOrigTopLeft.y;
                    break label48;
                }

                var4 = this.mResizeOrigTopLeft.x;
            } else {
                var4 = this.mResizeOrigBottomRight.x - var5;
            }

            var3 = this.mResizeOrigBottomRight.y - var2;
        }

        this.a(var4, var3, var5, var2);
    }

    private void b(boolean var1) {
        if (this.canEditText()) {
            this.showHandle(this.mResizeHandleTopLeft, var1);
            this.showHandle(this.mResizeHandleTopRight, var1);
            this.showHandle(this.mResizeHandleBottomLeft, var1);
            this.showHandle(this.mResizeHandleBottomRight, var1);
        }
    }

    private void c(DragHandle var1) {
        int var10003 = (this.mResizeOrigTopLeft.x + this.mResizeOrigBottomRight.x) / 2;
        int var10004 = this.mResizeOrigTopLeft.y + this.mResizeOrigBottomRight.y;
        float var2 = this.getAngle(var1.getPosition().x, var1.getPosition().y, var10003, var10004 / 2);
        if (Math.abs(var2 - this.mNAdditionalAngle) > 2.0F) {
            this.mNAdditionalAngle = var2;
            this.mResizingView.setRotation(var2);
            ImageView var3 = this.mResizingView;
            var3.setLayoutParams(var3.getLayoutParams());
        }

    }

    private void c(boolean var1) {
        if (this.canEditText()) {
            this.showHandle(this.mDragHandle, var1);
        }
    }

    private boolean c() {
        if (this.mFinished) {
            return false;
        } else if (this.mDoc == null) {
            return false;
        } else if (this.bitmaps == null) {
            return false;
        } else {
            int var1 = 0;

            while (true) {
                SOBitmap[] var2 = this.bitmaps;
                if (var1 >= var2.length) {
                    return true;
                }

                if (var2[var1] == null) {
                    return false;
                }

                if (var2[var1].a() == null) {
                    return false;
                }

                if (this.bitmaps[var1].a().isRecycled()) {
                    return false;
                }

                ++var1;
            }
        }
    }

    private void d() {
        int var1 = 0;
        this.renderRequested = false;
        if (this.c()) {
            int var2 = this.bitmapIndex + 1;
            this.bitmapIndex = var2;
            if (var2 >= this.bitmaps.length) {
                this.bitmapIndex = 0;
            }

            final ArrayList var3;
            for (var3 = new ArrayList(); var1 < this.getPageCount(); ++var1) {
                DocPageView var4 = (DocPageView) this.getOrCreateChild(var1);
                if (var4.getParent() != null && var4.isShown()) {
                    var3.add(var4);
                    var4.startRenderPass();
                }
            }

            final long var5 = System.currentTimeMillis();
            Iterator var8 = var3.iterator();

            while (var8.hasNext()) {
                DocPageView var7 = (DocPageView) var8.next();
                if (!this.c()) {
                    return;
                }

                ++this.renderCount;
                var7.render(this.bitmaps[this.bitmapIndex], new o() {
                    public void a(int var1) {
                        DocView.l(DocView.this);
                        if (DocView.this.renderCount == 0) {
                            Iterator var2 = var3.iterator();

                            while (var2.hasNext()) {
                                DocPageView var3x = (DocPageView) var2.next();
                                var3x.endRenderPass();
                                var3x.invalidate();
                            }

                            if (DocView.this.renderRequested) {
                                DocView.this.d();
                            }
                        }

                    }
                });
            }

        }
    }

    static int l(DocView docView) {
        int i = docView.renderCount;
        docView.renderCount = i - 1;
        return i;
    }

    private void d(boolean var1) {
        if (this.canEditText()) {
            this.showHandle(this.mRotateHandle, var1);
        }
    }

    private void e() {
        this.mSelectionStartPage = null;
        this.mSelectionEndPage = null;
        this.mSelectionLimits = null;
        int var1 = this.getDoc().P();
        int var2 = this.getDoc().Q();
        if (var1 <= var2) {
            if (this.getDoc().r() > 0) {
                this.mSelectionStartPage = (DocPageView) this.getOrCreateChild(var1);

                for (this.mSelectionEndPage = (DocPageView) this.getOrCreateChild(var2); var1 < var2 + 1; ++var1) {
                    SOSelectionLimits var3 = ((DocPageView) this.getOrCreateChild(var1)).getSelectionLimits();
                    if (var3 != null) {
                        SOSelectionLimits var4 = this.mSelectionLimits;
                        if (var4 == null) {
                            this.mSelectionLimits = var3;
                        } else {
                            var4.combineWith(var3);
                        }
                    }
                }

            }
        }
    }

    private View getCached() {
        return null;
    }

    public void addComment() {
        this.mAddComment = true;
        this.getDoc().addHighlightAnnotation();
    }

    protected void addHistory(int var1, int var2, float var3, boolean var4) {
        boolean var5;
        if (var4 && !this.shouldAddHistory(var1, var2, var3)) {
            var5 = false;
        } else {
            var5 = true;
        }

        if (var5) {
            this.getHistory().add(var1, var2, var3);
        }

        this.mHostActivity.selectionupdated();
    }

    protected Point adjustDragHandle(DragHandle var1, Point var2) {
        return new Point(var2);
    }

    protected boolean allowTouchWithoutChildren() {
        return false;
    }

    protected boolean allowXScroll() {
        return !this.mReflowMode;
    }

    protected boolean canEditText() {
        ConfigOptions var1 = this.mConfigOptions;
        return var1 == null || var1.b();
    }

    protected boolean canSelectionSpanPages() {
        return true;
    }

    protected boolean centerPagesHorizontally() {
        return false;
    }

    protected boolean clearAreaSelection() {
        SOSelectionLimits var1 = this.getSelectionLimits();
        boolean var2;
        if (var1 == null || var1.getIsCaret() || !var1.getHasSelectionStart() && !var1.getHasSelectionEnd()) {
            var2 = false;
        } else {
            this.mDoc.clearSelection();
            var2 = true;
        }

        return var2;
    }

    protected void clearChildViews() {
        this.mChildViews.clear();
    }

    protected Point constrainScrollBy(int var1, int var2) {
        Rect var3 = new Rect();
        this.getGlobalVisibleRect(var3);
        int var4 = var3.height();
        int var5 = var3.width();
        int var6 = this.getScrollX();
        int var7 = this.getScrollY();
        int var8;
        if (this.mAllPagesRect.width() <= var5) {
            var8 = var1;
            if (this.mAllPagesRect.width() - var6 - var1 > var5) {
                var8 = 0;
            }

            var1 = var8;
            if (var6 + var8 > 0) {
                var1 = -var6;
            }
        } else {
            var8 = var1;
            if (this.mAllPagesRect.width() < var6 + var5 + var1) {
                var8 = 0;
            }

            var1 = var8;
            if (var6 + var8 < 0) {
                var1 = -var6;
            }

            var8 = this.mAllPagesRect.width() - var6 + var1;
            if (var8 < var5) {
                var1 = var8 - var5;
            }
        }

        if (this.mAllPagesRect.height() <= var4) {
            var8 = var2;
            if (this.mAllPagesRect.height() - var7 - var2 > var4) {
                var8 = 0;
            }

            var2 = var8;
            if (var7 + var8 > 0) {
                var2 = -var7;
            }
        } else {
            var8 = var2;
            if (var7 + var2 < 0) {
                var8 = -var7;
            }

            var7 = -var7;
            var2 = var8;
            if (this.mAllPagesRect.height() + var7 - var8 < var4) {
                var2 = -(var4 - (var7 + this.mAllPagesRect.height()));
            }
        }

        return new Point(var1, var2);
    }

    protected void doDoubleTap(float var1, float var2) {
        if (!((NUIDocView) this.mHostActivity).isFullScreen()) {
            this.doDoubleTap2(var1, var2);
        }
    }

    protected void doDoubleTap2(float var1, float var2) {
        if (!(this.mHostActivity instanceof NUIDocViewOther)) {
            Point var3 = this.eventToScreen(var1, var2);
            DocPageView var4 = this.findPageViewContainingPoint(var3.x, var3.y, false);
            if (var4 != null && var3 != null && var4.canDoubleTap(var3.x, var3.y)) {
                var4.onDoubleTap(var3.x, var3.y);
                if (!this.canEditText()) {
                    return;
                }

                this.focusInputView();
                this.updateInputView();
                this.showKeyboardAfterDoubleTap(var3);
            }

        }
    }

    protected void doPageMenu(int var1) {
    }

    protected void doSingleTap(float var1, float var2) {
        Log.e(TAG, "doSingleTap: " + var1 + "--" + var2);
        if (((NUIDocView) this.mHostActivity).isFullScreen() && !this.handleFullscreenTap(var1, var2)) {
            if (this.mConfigOptions.a()) {
                ((NUIDocView) this.mHostActivity).showUI(true);
            }

        } else if (!(this.mHostActivity instanceof NUIDocViewOther)) {
            if (this.getDoc() != null) {
                this.getDoc().p();
            }
            NoteEditor var3 = this.mNoteEditor;
            if (var3 != null && var3.isVisible()) {
                Utilities.hideKeyboard(this.getContext());
                this.mNoteEditor.hide();
            } else if (this.shouldPreclearSelection() && this.clearAreaSelection()) {
                Utilities.hideKeyboard(this.getContext());
            } else {
                final Point var4 = this.eventToScreen(var1, var2);
                final DocPageView var5 = this.findPageViewContainingPoint(var4.x, var4.y, false);
                Log.e(TAG, "doSingleTap: showKeyboardAndScroll1 " + var5);

                if (var5 != null) {
                    Log.e(TAG, "doSingleTap: showKeyboardAndScroll1.5555 " + this.canEditText());

                    if (!this.onSingleTap((float) var4.x, (float) var4.y, var5)) {
                        Log.e(TAG, "doSingleTap: showKeyboardAndScroll2 " + this.canEditText());

                        if (this.canEditText() && this.tapToFocus()) {
                            this.focusInputView();
                        }

                        if (!var5.onSingleTap(var4.x, var4.y, this.canEditText(), new ExternalLinkListener() {
                            public void handleExternalLink(int var1, Rect var2) {
                                DocView var3 = DocView.this;
                                var3.addHistory(var3.getScrollX(), DocView.this.getScrollY(), DocView.this.mScale, true);
                                int var4 = DocView.this.scrollBoxToTopAmount(var1, new RectF((float) var2.left, (float) var2.top, (float) var2.right, (float) var2.bottom));
                                var3 = DocView.this;
                                var3.addHistory(var3.getScrollX(), DocView.this.getScrollY() - var4, DocView.this.mScale, false);
                                RectF var5 = new RectF((float) var2.left, (float) var2.top, (float) var2.right, (float) var2.bottom);
                                DocView.this.scrollBoxToTop(var1, var5);
                            }
                        })) {
                            Log.e(TAG, "doSingleTap: showKeyboardAndScroll3 ");

                            if (!this.canEditText()) {
                                return;
                            }

                            (new Handler()).postDelayed(new Runnable() {
                                public void run() {
                                    if (!DocView.this.finished()) {
                                        Log.e(TAG, "doSingleTap: showKeyboardAndScroll4 ");
                                        if (DocView.this.mTapStatus == 1 && DocView.this.canEditText()) {
                                            if (var5.selectionLimits().getIsActive()) {
                                                DocView var1 = DocView.this;
                                                var1.scrollToSelection = var1.mHostActivity.showKeyboard();
                                                Log.e(TAG, "doSingleTap: showKeyboardAndScroll5 ");

                                            } else {
                                                DocView.this.showKeyboardAndScroll(var4);
                                                Log.e(TAG, "doSingleTap: showKeyboardAndScroll6 ");


                                            }

                                            DocView.this.updateInputView();
                                        } else if (Utilities.isLandscapePhone(DocView.this.getContext())) {
                                            Utilities.hideKeyboard(DocView.this.getContext());
                                        }

                                        DocView.this.mTapStatus = 0;
                                    }
                                }
                            }, 500L);
                        }

                    }
                }
            }
        }
    }

    protected void dropMovingPage(boolean var1) {
        final int var2 = this.getMovingPageNumber();
        if (this.isValidPage(var2)) {
            if (this.a(var2) == var2) {
                var1 = false;
            }

            if (!var1) {
                if (this.canEditText()) {
                    final ViewTreeObserver var3 = this.getViewTreeObserver();
                    var3.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            var3.removeOnGlobalLayoutListener(this);
                            DocView.this.doPageMenu(var2);
                            DocView.this.mHostActivity.setCurrentPage(var2);
                        }
                    });
                }

                this.finishDrop();
            } else {
                this.movePage(var2, this.a(var2));
                this.finishDrop();
            }
        }
    }

    protected Point eventToScreen(float var1, float var2) {
        int var3 = Math.round(var1);
        int var4 = Math.round(var2);
        Rect var5 = new Rect();
        this.getGlobalVisibleRect(var5);
        return new Point(var3 + var5.left, var4 + var5.top);
    }

    protected DocPageView findPageContainingSelection() {
        for (int var1 = 0; var1 < this.getPageCount(); ++var1) {
            DocPageView var2 = (DocPageView) this.getOrCreateChild(var1);
            if (var2 != null) {
                SOSelectionLimits var3 = var2.selectionLimits();
                if (var3 != null && var3.getIsActive() && var3.getHasSelectionStart()) {
                    return var2;
                }
            }
        }

        return null;
    }

    protected DocPageView findPageViewContainingPoint(int var1, int var2, boolean var3) {
        for (int var4 = 0; var4 < this.getChildCount(); ++var4) {
            View var5 = this.getChildAt(var4);
            Rect var6 = new Rect();
            var5.getGlobalVisibleRect(var6);
            if (var3) {
                var6.left = (int) ((float) var6.left - this.mScale * 20.0F / 2.0F);
                var6.right = (int) ((float) var6.right + this.mScale * 20.0F / 2.0F);
                var6.top = (int) ((float) var6.top - this.mScale * 20.0F / 2.0F);
                var6.bottom = (int) ((float) var6.bottom + this.mScale * 20.0F / 2.0F);
            }

            if (var6.contains(var1, var2)) {
                return (DocPageView) var5;
            }
        }

        return null;
    }

    public void finish() {
        this.mFinished = true;

        for (int var1 = 0; var1 < this.getChildCount(); ++var1) {
            ((DocPageView) this.getChildAt(var1)).finish();
        }

        this.clearChildViews();
        this.mDoc = null;
    }

    protected void finishDrop() {
        this.dropY = -1;
        this.mForceLayout = true;
        this.requestLayout();
    }

    public boolean finished() {
        return this.mFinished;
    }

    protected void focusInputView() {
        Log.e(TAG, "focusInputView: focus");
        if (this.b() != null) {
            Log.e(TAG, "focusInputView: focus ok");

            this.b().setFocus();
        }

    }

    public void forceLayout() {
        if (!this.finished()) {
            this.mForceLayout = true;
            this.requestLayout();
        }
    }

    public Adapter getAdapter() {
        return this.mAdapter;
    }

    public float getAngle(int var1, int var2, int var3, int var4) {
        float var5 = (float) Math.toDegrees(Math.atan2((double) (var4 - var2), (double) (var3 - var1))) - 90.0F;
        float var6 = var5;
        if (var5 < 0.0F) {
            var6 = var5 + 360.0F;
        }

        return var6;
    }

    public int getBorderColor() {
        return this.mHostActivity.getBorderColor();
    }

    protected SODoc getDoc() {
        return this.mDoc;
    }

    public History getHistory() {
        return this.mHistory;
    }

    public int getMostVisiblePage() {
        return this.mostVisibleChild;
    }

    protected int getMovingPageNumber() {
        return -1;
    }

    protected View getOrCreateChild(int var1) {
        View var2 = (View) this.mChildViews.get(var1);
        View var3 = var2;
        if (var2 == null) {
            var3 = this.getViewFromAdapter(var1);
            this.mChildViews.append(var1, var3);
            this.a(var3, this.mScale);
        }

        return var3;
    }

    protected int getPageCount() {
        return this.getAdapter() == null ? 0 : this.getAdapter().getCount();
    }

    public boolean getReflowMode() {
        return this.mReflowMode;
    }

    public int getReflowWidth() {
        return ((DocPageView) this.getOrCreateChild(0)).getReflowWidth();
    }

    public float getScale() {
        return this.mScale;
    }

    public View getSelectedView() {
        return null;
    }

    public SOSelectionLimits getSelectionLimits() {
        return this.mSelectionLimits;
    }

    protected int getStartPage() {
        return this.mStartPage;
    }

    protected View getViewFromAdapter(int var1) {
        return this.getAdapter().getView(var1, this.getCached(), this);
    }

    public void goToFirstPage() {
        this.goToThisPage = 0;
        this.forceLayout();
    }

    public void goToLastPage() {
        this.goToThisPage = this.getPageCount() - 1;
        this.forceLayout();
    }

    protected boolean handleFullscreenTap(float var1, float var2) {
        return false;
    }

    public void handleStartPage() {
        final int var1 = this.getStartPage();
        if (var1 > 0) {
            this.setStartPage(0);
            (new Handler()).post(new Runnable() {
                public void run() {
                    Rect var1x = ((DocPageView) DocView.this.getOrCreateChild(var1 - 1)).getChildRect();
                    DocView.this.scrollBy(0, var1x.top);
                    DocView.this.requestLayout();
                }
            });
        }

    }

    protected void hideHandles() {
        this.a(false);
        this.b(false);
        this.c(false);
        this.d(false);
    }

    protected void initialize(Context var1) {
        this.setBackgroundColor(viewx.core.content.a.c(this.getContext(), color.sodk_editor_doc_background));
        this.mGestureDetector = new GestureDetector(var1, this);
        this.mScaleGestureDetector = new ScaleGestureDetector(var1, this);
        this.mScroller = new Scroller(var1);
        this.mSmoother = new DocView.Smoother(3);
        this.mHistory = new History();
        this.setScrollContainer(false);
    }

    public boolean isAtRest() {
        if (this.mTouching) {
            return false;
        } else if (this.mScaling) {
            return false;
        } else {
            return this.mScroller.isFinished();
        }
    }

    public boolean isDocModified() {
        SODoc var1 = this.mDoc;
        return var1 != null ? var1.getHasBeenModified() : false;
    }

    protected boolean isMovingPage() {
        return false;
    }

    public boolean isValidPage(int var1) {
        int var2 = this.getPageCount();
        return var1 >= 0 && var1 < var2;
    }

    public void layoutNow() {
        this.mForceLayout = true;
        this.requestLayout();
    }

    protected float maxScale() {
        return 5.0F;
    }

    protected float minScale() {
        return 0.15F;
    }

    protected void moveHandlesToCorners() {
        if (!this.finished()) {
            SOSelectionLimits var1 = this.getSelectionLimits();
            if (var1 != null) {
                boolean var2 = var1.getIsActive();
                boolean var3 = true;
                boolean var4;
                if (var2 && !var1.getIsCaret()) {
                    var4 = true;
                } else {
                    var4 = false;
                }

                boolean var5;
                if (var1.getIsActive() && this.getDoc().getSelectionCanBeResized() && !var1.getIsCaret()) {
                    var5 = true;
                } else {
                    var5 = false;
                }

                boolean var6;
                if (var1.getIsActive() && this.getDoc().getSelectionCanBeAbsolutelyPositioned()) {
                    var6 = true;
                } else {
                    var6 = false;
                }

                if (!var1.getIsActive() || !this.getDoc().getSelectionCanBeRotated()) {
                    var3 = false;
                }

                if (var5) {
                    this.positionHandle(this.mResizeHandleTopLeft, this.mSelectionStartPage, (int) var1.getBox().left, (int) var1.getBox().top);
                    this.positionHandle(this.mResizeHandleTopRight, this.mSelectionStartPage, (int) var1.getBox().right, (int) var1.getBox().top);
                    this.positionHandle(this.mResizeHandleBottomLeft, this.mSelectionStartPage, (int) var1.getBox().left, (int) var1.getBox().bottom);
                    this.positionHandle(this.mResizeHandleBottomRight, this.mSelectionStartPage, (int) var1.getBox().right, (int) var1.getBox().bottom);
                }

                if (var4) {
                    this.positionHandle(this.mSelectionHandleTopLeft, this.mSelectionStartPage, (int) var1.getStart().x, (int) var1.getStart().y);
                    this.positionHandle(this.mSelectionHandleBottomRight, this.mSelectionEndPage, (int) var1.getEnd().x, (int) var1.getEnd().y);
                }

                if (var6) {
                    this.positionHandle(this.mDragHandle, this.mSelectionStartPage, (int) (var1.getBox().left + var1.getBox().right) / 2, (int) var1.getBox().bottom);
                }

                if (var3) {
                    this.positionHandle(this.mRotateHandle, this.mSelectionStartPage, (int) (var1.getBox().left + var1.getBox().right) / 2, (int) var1.getBox().top);
                }
            }

        }
    }

    public void movePage(int var1, int var2) {
        if (this.isValidPage(var1)) {
            if (this.isValidPage(var2)) {
                this.getDoc().movePage(var1, var2);
                this.onPageMoved(var2);
            }
        }
    }

    public void onConfigurationChange() {
        for (int var1 = 0; var1 < this.getPageCount(); ++var1) {
            DocPageView var2 = (DocPageView) this.getOrCreateChild(var1);
            if (var2.getParent() != null && var2.isShown()) {
                var2.resetBackground();
            }
        }

        this.mForceColumnCount = this.mLastLayoutColumns;
        ((NUIDocView) this.mHostActivity).triggerOrientationChange();
    }

    public boolean onDown(MotionEvent var1) {
        if (!this.mScroller.isFinished()) {
            this.mScrollingStopped = true;
            this.mScroller.forceFinished(true);
        }

        return true;
    }

    public void onDrag(DragHandle var1) {
        Point var2 = this.a(var1.getPosition());
        DocPageView var3 = this.findPageViewContainingPoint(var2.x, var2.y, false);
        if (var3 != null) {
            if (var1.isSelectionKind()) {
                if (!this.canSelectionSpanPages()) {
                    int var4 = var3.getPageNumber();
                    if (var4 != this.mSelectionStartPage.getPageNumber()) {
                        return;
                    }

                    if (var4 != this.mSelectionEndPage.getPageNumber()) {
                        return;
                    }
                }

                var2 = this.adjustDragHandle(var1, var2);
                if (var1.getKind() == 1) {
                    var2.x += var1.getWidth();
                    var2.y += var1.getHeight();
                    var3.setSelectionStart(var2);
                } else if (var1.getKind() == 2) {
                    var3.setSelectionEnd(var2);
                }
            } else if (var1.isResizeKind()) {
                this.b(var1);
            } else if (var1.isDragKind()) {
                this.a(var1);
            } else if (var1.isRotateKind()) {
                this.c(var1);
            }

        }
    }

    public void onEndDrag(DragHandle var1) {
        try {
            label36:
            {
                int var2;
                int var6;
                int var7;
                int var8;
                Point var12;
                if (var1.isResizeKind() && this.mDraggingObjectPageBounds != null) {
                    var2 = this.mResizeRect.left;
                    int var3 = this.mResizeOrigRect.left;
                    int var4 = this.mResizeRect.top;
                    int var5 = this.mResizeOrigRect.top;
                    var6 = this.mResizeRect.right;
                    var7 = this.mResizeOrigRect.right;
                    var8 = this.mResizeRect.bottom;
                    int var9 = this.mResizeOrigRect.bottom;
                    Point var10 = this.mSelectionStartPage.viewToPage(var2 - var3, var4 - var5);
                    var12 = this.mSelectionStartPage.viewToPage(var6 - var7, var8 - var9);
                    RectF var11 = this.mDraggingObjectPageBounds;
                    var11.left += (float) var10.x;
                    var11 = this.mDraggingObjectPageBounds;
                    var11.top += (float) var10.y;
                    RectF var13 = this.mDraggingObjectPageBounds;
                    var13.right += (float) var12.x;
                    var13 = this.mDraggingObjectPageBounds;
                    var13.bottom += (float) var12.y;
                } else {
                    if (!var1.isDragKind() || this.mDraggingObjectPageBounds == null || !this.mDragging) {
                        if (var1.isRotateKind()) {
                            this.getDoc().setSelectionRotation(this.mRotateAngle + this.mNAdditionalAngle);
                        } else if (var1.isSelectionKind()) {
                            this.moveHandlesToCorners();
                            this.updateInputView();
                        }
                        break label36;
                    }

                    var2 = this.mResizeRect.left;
                    var7 = this.mResizeOrigRect.left;
                    var6 = this.mResizeRect.top;
                    var8 = this.mResizeOrigRect.top;
                    var12 = this.mSelectionStartPage.viewToPage(var2 - var7, var6 - var8);
                    this.mDraggingObjectPageBounds.offset((float) var12.x, (float) var12.y);
                }

                this.mSelectionStartPage.getPage().a(this.mDraggingObjectPageBounds);
            }

            this.mDraggingObjectPageBounds = null;
            this.mDragging = false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void onEndFling() {
        if (!this.finished()) {
            int var1 = this.mostVisibleChild;
            if (var1 >= 0) {
                this.mHostActivity.setCurrentPage(var1);
            }

        }
    }

    public boolean onFling(MotionEvent var1, MotionEvent var2, float var3, float var4) {
        if (this.mScaling) {
            return true;
        } else if (!this.mScroller.isFinished()) {
            return true;
        } else {
            this.flinging = true;
            int var5 = (int) var3;
            int var6 = (int) var4;
            if (!this.allowXScroll()) {
                var5 = 0;
            }

            this.mSmoother.clear();
            this.mScroller.forceFinished(true);
            this.mScrollerLastY = 0;
            this.mScrollerLastX = 0;
            this.mScroller.fling(0, 0, var5, var6, -2147483647, 2147483647, -2147483647, 2147483647);
            this.post(this);
            return true;
        }
    }

    public void onFoundText(int var1, RectF var2) {
        this.scrollBoxIntoView(var1, var2, true);
    }

    public void onFullscreen(boolean var1) {
        if (var1 && this.getDoc() != null) {
            this.getDoc().clearSelection();
        }

        for (int var2 = 0; var2 < this.getPageCount(); ++var2) {
            ((DocPageView) this.getOrCreateChild(var2)).onFullscreen(var1);
        }

    }

    public void onHidePages() {
        int var1 = this.getContext().getResources().getInteger(integer.sodk_editor_page_width_percentage);
        this.a((float) (this.getContext().getResources().getInteger(integer.sodk_editor_pagelist_width_percentage) + var1) / (float) var1);
        this.mPagesShowing = false;
    }

    public void onHistoryItem(HistoryItem var1) {
        this.mXScroll = 0;
        this.mYScroll = 0;
        this.setScrollX(var1.getScrollX());
        this.setScrollY(var1.getScrollY());
        this.mScale = var1.getScale();
        this.forceLayout();
        (new Handler()).post(new Runnable() {
            public void run() {
                if (DocView.this.mostVisibleChild >= 0) {
                    DocView.this.mHostActivity.setCurrentPage(DocView.this.mostVisibleChild);
                }

            }
        });
    }

    protected void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        if (!this.finished()) {
            if (this.isShown()) {
                var4 = this.getPageCount();
                if (this.mReflowMode) {
                    var4 = this.mDoc.r();
                }

                if (this.getPageCount() != 0) {
                    this.scrollBy(-this.mXScroll, -this.mYScroll);
                    this.mYScroll = 0;
                    this.mXScroll = 0;
                    if (this.shouldLayout()) {
                        this.mViewportOrigin.set(this.getScrollX(), this.getScrollY());
                        this.getGlobalVisibleRect(this.mViewport);
                        this.mViewport.offsetTo(this.mViewportOrigin.x, this.mViewportOrigin.y);
                        this.unscaledMaxw = 0;

                        DocPageView var6;
                        for (var2 = 0; var2 < this.getPageCount(); ++var2) {
                            var6 = (DocPageView) this.getOrCreateChild(var2);
                            this.unscaledMaxw = Math.max(this.unscaledMaxw, var6.getUnscaledWidth());
                        }

                        float var7 = (float) this.unscaledMaxw;
                        float var8 = this.mScale;
                        var3 = (int) (var7 * var8);
                        if (this.mPressing) {
                            var2 = this.mLastLayoutColumns;
                        } else {
                            var2 = this.mForceColumnCount;
                            if (var2 == -1) {
                                var2 = (int) (var8 * 20.0F);
                                double var9 = (double) (this.mViewport.width() + var2);
                                double var11 = (double) (var3 + var2);
                                Double.isNaN(var9);
                                Double.isNaN(var11);
                                Double.isNaN(var9);
                                Double.isNaN(var11);
                                Double.isNaN(var9);
                                Double.isNaN(var11);
                                Double.isNaN(var9);
                                Double.isNaN(var11);
                                var2 = (int) (var9 / var11);
                                if (this.mReflowMode) {
                                    var2 = 1;
                                }
                            }
                        }

                        int var13 = var2;
                        if (var2 > var4) {
                            var13 = var4;
                        }

                        this.mostVisibleChild = -1;
                        this.mAllPagesRect.setEmpty();
                        int var14;
                        if (this.isMovingPage()) {
                            var14 = ((DocPageView) this.getOrCreateChild(this.getMovingPageNumber())).getUnscaledHeight();
                            this.mDropPageAbove = -1;
                            this.mDropPageBelow = -1;
                        } else {
                            var14 = 0;
                        }

                        int var15 = Math.max(this.getPageCount(), this.getChildCount());
                        var5 = 0;
                        int var16 = 0;
                        int var17 = 0;
                        var2 = 0;
                        var3 = -1;

                        int var22;
                        for (int var18 = -1; var5 < var15; var18 = var22) {
                            int var19;
                            int var21;
                            int var31;
                            label192:
                            {
                                var6 = (DocPageView) this.getOrCreateChild(var5);
                                if (var6 == null) {
                                    this.removeViewAt(var5);
                                } else {
                                    var6.setDocView(this);
                                    if (!this.isMovingPage() || var6.getPageNumber() != this.getMovingPageNumber()) {
                                        int var23 = var6.getUnscaledWidth();
                                        var19 = var6.getUnscaledHeight();
                                        int var24 = (this.unscaledMaxw + 20) * var17;
                                        var22 = var2 + var19;
                                        var16 = Math.max(var16, var19);
                                        int var25;
                                        int var26;
                                        if (this.isMovingPage()) {
                                            var7 = (float) var2;
                                            var8 = this.mScale;
                                            var25 = (int) (var7 * var8);
                                            var21 = (int) ((float) var22 * var8);
                                            boolean var20;
                                            if (var3 == -1) {
                                                boolean var28;
                                                if (this.dropY <= var25) {
                                                    var28 = true;
                                                } else {
                                                    var28 = false;
                                                }

                                                var20 = var28;
                                                var3 = var5;
                                            } else {
                                                var20 = false;
                                            }

                                            var26 = this.dropY;
                                            boolean var32;
                                            if (var26 >= var25 && var26 <= (var25 + var21) / 2) {
                                                var32 = true;
                                            } else {
                                                var32 = var20;
                                            }

                                            var26 = var22;
                                            var31 = var2;
                                            if (var32) {
                                                this.mDropPageAbove = var5;
                                                var31 = var2 + var14;
                                                var26 = var22 + var14;
                                            }

                                            var22 = var26;
                                        } else {
                                            var31 = var2;
                                        }

                                        var26 = var16;
                                        var7 = (float) var24;
                                        var8 = this.mScale;
                                        var21 = (int) (var7 * var8);
                                        var25 = (int) ((float) var31 * var8);
                                        var16 = (int) ((float) (var23 + var24) * var8);
                                        var24 = (int) ((float) var22 * var8);
                                        this.mChildRect.set(var21, var25, var16, var24);
                                        var6.setChildRect(this.mChildRect);
                                        if (this.mAllPagesRect.isEmpty()) {
                                            this.mAllPagesRect.set(this.mChildRect);
                                        } else {
                                            this.mAllPagesRect.union(this.mChildRect);
                                        }

                                        if (this.mChildRect.intersect(this.mViewport) && var5 < var4) {
                                            if (var6.getParent() == null) {
                                                var6.clearContent();
                                                this.a((View) var6);
                                            }

                                            if (this.centerPagesHorizontally()) {
                                                var2 = (this.getWidth() - this.mChildRect.width()) / 2;
                                            } else {
                                                var2 = 0;
                                            }

                                            var6.layout(var21 + var2, var25, var16 + var2, var24);
                                            var6.invalidate();
                                            var2 = var18;
                                            if (var6.getGlobalVisibleRect(this.mostVisibleRect)) {
                                                var22 = this.mostVisibleRect.height();
                                                var2 = var18;
                                                if (var22 > var18) {
                                                    this.mostVisibleChild = var5;
                                                    var2 = var22;
                                                }
                                            }
                                        } else {
                                            this.removeViewInLayout(var6);
                                            var2 = var18;
                                        }

                                        var22 = var17 + 1;
                                        if (var22 >= var13) {
                                            if (this.getReflowMode()) {
                                                var18 = var31 + var19;
                                            } else {
                                                var18 = var31 + var26;
                                            }

                                            var17 = var18 + 20;
                                            var18 = 0;
                                            var26 = 0;
                                        } else {
                                            var18 = var26;
                                            var17 = var31;
                                            var26 = var22;
                                        }

                                        var16 = var18;
                                        var19 = var26;
                                        var31 = var17;
                                        var21 = var3;
                                        var22 = var2;
                                        if (this.isMovingPage()) {
                                            int var27 = (int) ((float) 20 * this.mScale);
                                            var23 = this.dropY;
                                            var16 = var18;
                                            var19 = var26;
                                            var31 = var17;
                                            var21 = var3;
                                            var22 = var2;
                                            if (var23 >= (var25 + var24) / 2) {
                                                var16 = var18;
                                                var19 = var26;
                                                var31 = var17;
                                                var21 = var3;
                                                var22 = var2;
                                                if (var23 <= var24 + var27) {
                                                    this.mDropPageBelow = var5;
                                                    var31 = var17 + var14;
                                                    var22 = var2;
                                                    var21 = var3;
                                                    var19 = var26;
                                                    var16 = var18;
                                                }
                                            }
                                        }
                                        break label192;
                                    }

                                    this.removeViewInLayout(var6);
                                }

                                var19 = var17;
                                var31 = var2;
                                var21 = var3;
                                var22 = var18;
                            }

                            ++var5;
                            var17 = var19;
                            var2 = var31;
                            var3 = var21;
                        }

                        boolean var30;
                        label145:
                        {
                            this.setMostVisiblePage();
                            if (this.mScaling && var13 >= 1) {
                                var2 = this.mLastLayoutColumns;
                                if (var2 >= 1 && var2 != var13) {
                                    this.scrollBy(this.mAllPagesRect.centerX() - this.mViewport.centerX(), 0);
                                    var2 = this.lastMostVisibleChild;
                                    if (var2 != -1) {
                                        this.scrollToPage(var2, true);
                                    }

                                    var30 = true;
                                    break label145;
                                }
                            }

                            var30 = false;
                        }

                        this.mLastLayoutColumns = var13;
                        this.mLastAllPagesRect.set(this.mAllPagesRect);
                        this.lastMostVisibleChild = this.mostVisibleChild;
                        this.moveHandlesToCorners();
                        NoteEditor var29 = this.mNoteEditor;
                        if (var29 != null) {
                            var29.move();
                        }

                        if (this.once) {
                            var6 = (DocPageView) this.getOrCreateChild(0);
                            this.once = false;
                        }

                        this.handleStartPage();
                        this.triggerRender();
                        if (var30) {
                            (new Handler()).post(new Runnable() {
                                public void run() {
                                    DocView.this.layoutNow();
                                }
                            });
                        }

                        var2 = this.goToThisPage;
                        if (var2 != -1) {
                            this.goToThisPage = -1;
                            int finalVar = var2;
                            (new Handler()).post(new Runnable() {
                                public void run() {
                                    DocView var1 = DocView.this;
                                    int var3 = var1.mDoc.r();
                                    boolean var4 = true;
                                    if (finalVar != var3 - 1) {
                                        var4 = false;
                                    }

                                    var1.scrollToPage(finalVar, var4, false);
                                }
                            });
                        }

                    }
                }
            }
        }
    }

    public void onLayoutChanged() {
    }

    public void onLongPress(MotionEvent var1) {
        this.mTapStatus = 2;
        if (!this.mScrollingStopped) {
            this.doDoubleTap(var1.getX(), var1.getY());
        }

        this.mLastTapTime = 0L;
//        if (canEditText()) {
//            Point var2 = this.a(new Point((int) var1.getX(), (int) var1.getY()));
//            if (this.findPageViewContainingPoint(var2.x, var2.y, false) != null) {
//                Utilities.hideKeyboard(this.getContext());
//                this.mPressing = true;
//                this.mPressStartScale = this.mScale;
//                this.mPressStartX = (int) var1.getX();
//                this.mPressStartY = (int) var1.getY();
//                this.mPressStartViewX = this.mPressStartX + this.getScrollX();
//                this.mPressStartViewY = this.mPressStartY + this.getScrollY();
//                float var3 = this.mScale;
//                this.zoomWhilePressing(var3, ZOOM_FACTOR * var3, false);
//            }
//        }

    }

    public void onLongPressMoving(MotionEvent var1) {
        if (this.canEditText()) {
            if (!this.mPressZooming) {
                Point var2 = this.eventToScreen((float) ((int) var1.getX()), (float) ((int) var1.getY()));
                DocPageView var3 = this.findPageViewContainingPoint(var2.x, var2.y, false);
                this.mPressPage = var3;
                if (var3 != null) {
                    if (!this.mMoving) {
                        float var4 = (float) Utilities.convertDpToPixel(MOVE_THRESHOLD);
                        if (Math.abs(var1.getX() - (float) this.mPressStartX) >= var4 || Math.abs(var1.getY() - (float) this.mPressStartY) >= var4) {
                            this.mMoving = true;
                        }
                    }

                    if (this.mMoving) {
                        int var5 = Utilities.convertDpToPixel(35.0F);
                        this.mPressPage.setCaret(var2.x, var2.y - var5);
                        this.forceLayout();
                        this.focusInputView();
                        this.updateInputView();
                    }

                }
            }
        }
    }

    public void onLongPressRelease() {
        this.zoomWhilePressing(this.mScale, this.mPressStartScale, true);
    }

    public void onLongPressReleaseDone() {
        SOSelectionLimits var1 = this.getSelectionLimits();
        if (var1 == null) {
        } else if (var1.getIsActive() && this.mPressPage != null) {
            this.scrollToSelection = this.mHostActivity.showKeyboard();
        }

    }

    protected void onMovePage(int var1, int var2) {
        if (this.isMovingPage()) {
            this.dropY = var2 + this.getScrollY();
            this.mForceLayout = true;
            this.requestLayout();
        }
    }

    public void onNextPrevTrackedChange() {
        this.mScrollRequested = true;
        Utilities.hideKeyboard(this.getContext());
    }

    public void onOrientationChange() {
        Rect var1 = new Rect();
        this.getGlobalVisibleRect(var1);
        int var2 = var1.width();
        int var3 = this.mAllPagesRect.width();
        final ViewTreeObserver var5;
        if (var3 > 0 && var2 > 0) {
            if (!this.mReflowMode && this.mLastLayoutColumns == 0 && var3 >= var2) {
                this.mForceColumnCount = -1;
            } else {
                final float var4 = (float) var2 / (float) var3;
                var3 = this.getScrollY();
                this.mScale *= var4;
                this.scaleChildren();
                this.requestLayout();
                var5 = this.getViewTreeObserver();
                int finalVar = var3;
                var5.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        var5.removeOnGlobalLayoutListener(this);
                        int var1 = finalVar;
                        int var2 = (int) (var4 * (float) var1);
                        DocView.this.scrollBy(0, -(var1 - var2));
                        DocView var3x = DocView.this;
                        var3x.mForceColumnCount = -1;
                        var3x.layoutNow();
                    }
                });
            }
        } else {
            this.requestLayout();
            var5 = this.getViewTreeObserver();
            var5.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    var5.removeOnGlobalLayoutListener(this);
                    DocView.this.onOrientationChange();
                }
            });
        }
    }

    protected void onPageMoved(int var1) {
    }

    protected void onPreLayout() {
    }

    public void onReflowScale() {
        if (this.mReflowMode) {
            DocPageView var1 = (DocPageView) this.getOrCreateChild(0);
            int var2 = this.mDoc.r();

            for (int var3 = 1; var3 < var2; ++var3) {
                ((DocPageView) this.getOrCreateChild(var3)).onReflowScale(var1);
            }
        }

    }

    public void onReloadFile() {
    }

    public boolean onScale(ScaleGestureDetector var1) {
        if (this.mPressing) {
            return true;
        } else {
            float var2 = this.mScale;
            float var3 = Math.min(Math.max(var1.getScaleFactor() * var2, this.minScale()), this.maxScale());
            this.mScale = var3;
            if (var3 == var2) {
                return true;
            } else {
                this.scaleChildren();
                var3 = var1.getFocusX();
                var2 = var1.getFocusY();
                int var4 = (int) var3;
                int var5 = this.getScrollX();
                int var6 = (int) var2;
                int var7 = this.getScrollY();
                var3 = (float) this.mXScroll;
                var2 = (float) (var4 + var5);
                this.mXScroll = (int) (var3 + (var2 - var1.getScaleFactor() * var2));
                var2 = (float) this.mYScroll;
                var3 = (float) (var6 + var7);
                this.mYScroll = (int) (var2 + (var3 - var1.getScaleFactor() * var3));
                this.requestLayout();
                return true;
            }
        }
    }

    public boolean onScaleBegin(ScaleGestureDetector var1) {
        this.mScaling = true;
        this.hideHandles();
        if (this.mReflowWidth == -1) {
            this.mReflowWidth = this.getReflowWidth();
        }

        this.mYScroll = 0;
        this.mXScroll = 0;
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector var1) {
        this.showHandles();
        Rect var7 = this.mAllPagesRect;
        if (var7 != null && var7.width() != 0 && this.mAllPagesRect.height() != 0) {
            var7 = new Rect();
            this.getGlobalVisibleRect(var7);
            if (!this.mReflowMode && this.mLastLayoutColumns == 0 && this.mAllPagesRect.width() >= var7.width()) {
                this.mScaling = false;
                return;
            }

            SODoc var2 = this.getDoc();
            this.getPageCount();
            if (this.mReflowMode) {
                this.mDoc.r();
            }

            float var3 = (float) var7.width() / (float) this.mAllPagesRect.width();
            float var4 = (float) var7.height() / (float) this.mAllPagesRect.height();
            int var5;
            final int var6;
            final ViewTreeObserver var8;
            if (this.mReflowMode) {
                NUIDocView.currentNUIDocView().onReflowScale();
                var5 = this.getScrollX();
                var6 = this.getScrollY();
                var3 = 1.0F;
                if (NUIDocView.currentNUIDocView().isPageListVisible()) {
                    var3 = (float) this.getContext().getResources().getInteger(integer.sodk_editor_page_width_percentage) / 100.0F;
                }

                var3 = var3 * (float) this.mReflowWidth / this.mScale;
                var2.a(2, var3);
                var8 = this.getViewTreeObserver();
                int finalVar1 = var5;
                float finalVar2 = var3;
                var8.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        var8.removeOnGlobalLayoutListener(this);
                        float var1 = DocView.this.mLastReflowWidth / finalVar2;
                        int var2 = var6;
                        int var3x = (int) (var1 * (float) var2);
                        DocView.this.scrollBy(-finalVar1, -(var2 - var3x));
                        DocView.this.mLastReflowWidth = finalVar2;
                    }
                });
            } else {
                var4 = Math.min(var3, var4);
                var6 = this.unscaledMaxw;
                var5 = this.mLastLayoutColumns;
                this.mScale = (float) var7.width() / (float) (var6 * var5 + (var5 - 1) * 20);
                this.scaleChildren();
                this.mXScroll = 0;
                this.mYScroll = 0;
                var5 = (int) ((float) (this.getScrollY() + var7.height() / 2) * var3 - (float) (var7.height() / 2));
                if ((int) (var4 * (float) this.mLastAllPagesRect.height()) < var7.height()) {
                    var5 = 0;
                }

                var8 = this.getViewTreeObserver();
                int finalVar = var5;
                var8.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        var8.removeOnGlobalLayoutListener(this);
                        DocView.this.scrollAfterScaleEnd(0, finalVar);
                        DocView.this.requestLayout();
                    }
                });
                this.requestLayout();
            }

            this.mScaling = false;
        }

    }

    public boolean onScroll(MotionEvent var1, MotionEvent var2, float var3, float var4) {
        if (this.mScaling) {
            return true;
        } else if (this.mPressing) {
            return true;
        } else if (this.mDragging) {
            return true;
        } else if (!this.mScroller.isFinished()) {
            return true;
        } else {
            if (this.mReflowMode) {
                var3 = 0.0F;
            }

            this.mXScroll = (int) ((float) this.mXScroll - var3);
            this.mYScroll = (int) ((float) this.mYScroll - var4);
            this.requestLayout();
            return true;
        }
    }

    public void onSelectionChanged() {
        if (this.getDoc() != null) {
            this.e();
            NoteEditor var1 = this.mNoteEditor;
            byte var2 = 0;
            boolean var3;
            if (var1 != null && var1.isVisible()) {
                var3 = false;
            } else {
                var3 = true;
            }

            if (var3) {
                this.scrollSelectionIntoView();
            }

            boolean var4 = false;
            int var7 = var2;

            boolean var6;
            for (var6 = var4; var7 < this.getPageCount(); ++var7) {
                DocPageView var5 = (DocPageView) this.getOrCreateChild(var7);
                if (var5.sizeViewToPage()) {
                    var6 = true;
                }

                var5.setNewScale(this.mScale);
            }

            if (var6) {
                this.forceLayout();
            }

            this.updateDragHandles();
            this.updateReview();
        }
    }

    public void onShowKeyboard(boolean var1) {
        if (var1) {
            Point var2 = this.scrollToHere;
            if (var2 != null && this.scrollToHerePage != null) {
                RectF var3 = new RectF((float) var2.x, (float) this.scrollToHere.y, (float) (this.scrollToHere.x + 1), (float) (this.scrollToHere.y + 1));
                this.scrollBoxIntoView(this.scrollToHerePage.getPageNumber(), var3);
                this.scrollToHere = null;
                this.scrollToHerePage = null;
            }

            if (this.scrollToSelection) {
                this.scrollSelectionIntoView();
                this.scrollToSelection = false;
            }
        } else {
            NoteEditor var4 = this.mNoteEditor;
            if (var4 != null) {
                var4.preMoving();
            }

            this.mForceLayout = true;
            this.requestLayout();
        }

        DocView.a var5 = this.showKeyboardListener;
        if (var5 != null) {
            var5.a(var1);
        }

    }

    public void onShowPages() {
        int var1 = this.getContext().getResources().getInteger(integer.sodk_editor_page_width_percentage);
        int var2 = this.getContext().getResources().getInteger(integer.sodk_editor_pagelist_width_percentage);
        this.a((float) var1 / (float) (var1 + var2));
        this.mPagesShowing = true;
    }

    public void onShowPress(MotionEvent var1) {
    }

    protected boolean onSingleTap(float var1, float var2, DocPageView var3) {
        return false;
    }

    public boolean onSingleTapUp(MotionEvent var1) {
        Log.e(TAG, "onSingleTapUp: " + var1.getDownTime() + " " + var1.getEventTime() + " " + var1.getAction() + " " + var1.getX() + " " + var1.getY() + " " + var1.getMetaState());

        long var2 = System.currentTimeMillis();
        long var4 = this.mLastTapTime;
        if (var4 != 0L && var2 - var4 < 300L) {
            this.mTapStatus = 2;
            if (!this.mScrollingStopped) {
                this.doDoubleTap(this.lastTapX, this.lastTapY);
            }

            this.mLastTapTime = 0L;
        } else {
            this.mLastTapTime = var2;
            this.lastTapX = var1.getX();
            float var6 = var1.getY();
            this.lastTapY = var6;
            if (!this.mScrollingStopped) {
                this.doSingleTap(this.lastTapX, var6);
            }

            this.mTapStatus = 1;
        }

        this.mScrollingStopped = false;
        return false;
    }

    public void onStartDrag(DragHandle var1) {
        this.mDragging = true;
        if (!var1.isSelectionKind()) {
            DocPageView var2 = this.mSelectionStartPage;
            if (var2 == null || var2.getSelectionLimits() == null) {
                this.mDragging = false;
                return;
            }

            this.mDraggingObjectPageBounds = this.mSelectionStartPage.getSelectionLimits().getBox();
            SOBitmap var4 = this.getDoc().getSelectionAsBitmap();
            this.mResizingBitmap = var4;
            ImageView var3 = this.mResizingView;
            Bitmap var5;
            if (var4 != null) {
                var5 = var4.a();
            } else {
                var5 = null;
            }

            var3.setImageBitmap(var5);
            SOSelectionLimits var7 = this.getSelectionLimits();
            Point var6 = this.mSelectionStartPage.pageToView((int) var7.getBox().left, (int) var7.getBox().top);
            this.mResizeOrigTopLeft = var6;
            var6.offset(this.mSelectionStartPage.getLeft(), this.mSelectionStartPage.getTop());
            this.mResizeOrigTopLeft.offset(-this.getScrollX(), -this.getScrollY());
            Point var8 = this.mSelectionStartPage.pageToView((int) var7.getBox().right, (int) var7.getBox().bottom);
            this.mResizeOrigBottomRight = var8;
            var8.offset(this.mSelectionStartPage.getLeft(), this.mSelectionStartPage.getTop());
            this.mResizeOrigBottomRight.offset(-this.getScrollX(), -this.getScrollY());
            this.mNatDim = this.getDoc().M();
            this.mResizeAspect = (float) (this.mResizeOrigBottomRight.y - this.mResizeOrigTopLeft.y) / (float) (this.mResizeOrigBottomRight.x - this.mResizeOrigTopLeft.x);
            this.mDragOrigLocation.set(var1.getPosition().x, var1.getPosition().y);
            this.mRotateAngle = this.getDoc().getSelectionRotation();
            this.mNAdditionalAngle = 0.0F;
            this.mResizingView.setRotation(0.0F);
            this.a(this.mResizeOrigTopLeft.x, this.mResizeOrigTopLeft.y, this.mResizeOrigBottomRight.x - this.mResizeOrigTopLeft.x, this.mResizeOrigBottomRight.y - this.mResizeOrigTopLeft.y);
            this.mResizeOrigRect.set(this.mResizeRect);
            this.hideHandles();
        }

    }

    public boolean onTouchEvent(MotionEvent var1) {
        if (this.finished()) {
            return true;
        } else if (!this.allowTouchWithoutChildren() && this.getChildCount() <= 0) {
            return true;
        } else {
            this.mConstrained = true;
            if ((var1.getAction() & 255) == 0) {
                this.mTouching = true;
            }

            if ((var1.getAction() & 255) == 1) {
                if (this.mPressing) {
//                    this.onSingleTapUp(var1);

                    this.onLongPressRelease();
                } else {
                    this.mTouching = false;
                }
            }

            if ((var1.getAction() & 255) == 2 && this.mPressing) {
                this.onLongPressMoving(var1);
            }

            this.mScaleGestureDetector.onTouchEvent(var1);
            this.mGestureDetector.onTouchEvent(var1);
            return true;
        }
    }

    public boolean pagesShowing() {
        return this.mPagesShowing;
    }

    protected void positionHandle(DragHandle var1, DocPageView var2, int var3, int var4) {
        if (var1 != null) {
            Point var5 = var2.pageToView(var3, var4);
            var5.offset(var2.getChildRect().left, var2.getChildRect().top);
            var5.offset(-this.getScrollX(), -this.getScrollY());
            var1.measure(0, 0);
            var5.offset(-var1.getMeasuredWidth() / 2, -var1.getMeasuredHeight() / 2);
            Point var6 = var1.offsetCircleToEdge();
            var5.offset(var6.x, var6.y);
            var1.moveTo(var5.x, var5.y);
        }

    }

    public void preNextPrevTrackedChange() {
        NoteEditor var1 = this.mNoteEditor;
        if (var1 != null) {
            var1.preMoving();
        }

    }

    public void releaseBitmaps() {
        this.bitmaps = null;
    }

    public void requestLayout() {
        if (!this.finished()) {
            this.onPreLayout();
            super.requestLayout();
        }
    }

    protected void resetInputView() {
        if (this.b() != null) {
            this.b().resetEditable();
        }

    }

    public void run() {
        if (!this.mScroller.isFinished()) {
            this.mScroller.computeScrollOffset();
            int var1 = this.mScroller.getCurrX();
            int var2 = this.mScroller.getCurrY();
            int var3 = var1 - this.mScrollerLastX;
            var1 = var2 - this.mScrollerLastY;
            if (this.flinging) {
                this.mSmoother.addValue(var1);
                var2 = this.mSmoother.getAverage();
            } else {
                var2 = var1;
            }

            this.mXScroll += var3;
            this.mYScroll += var2;
            this.requestLayout();
            this.mScrollerLastX += var3;
            this.mScrollerLastY += var1;
            this.post(this);
        } else {
            this.requestLayout();
            this.onEndFling();
            this.flinging = false;
        }

    }

    public void saveComment() {
        NoteEditor var1 = this.mNoteEditor;
        if (var1 != null) {
            var1.saveComment();
        }

    }

    protected void scaleChildren() {
        for (int var1 = 0; var1 < this.getPageCount(); ++var1) {
            ((DocPageView) this.getOrCreateChild(var1)).setNewScale(this.mScale);
        }

    }

    protected void scrollAfterScaleEnd(int var1, int var2) {
        this.scrollTo(var1, var2);
    }

    public void scrollBoxIntoView(int var1, RectF var2) {
        this.scrollBoxIntoView(var1, var2, false);
    }

    public void scrollBoxIntoView(int var1, RectF var2, boolean var3) {
        this.scrollBoxIntoView(var1, var2, var3, 0);
    }

    public void scrollBoxIntoView(int var1, RectF var2, boolean var3, int var4) {
        Point var5 = this.a(var1, var2, var3, var4);
        this.smoothScrollBy(var5.x, var5.y);
    }

    public void scrollBoxToTop(int var1, RectF var2) {
        this.smoothScrollBy(0, this.scrollBoxToTopAmount(var1, var2));
    }

    public int scrollBoxToTopAmount(int var1, RectF var2) {
        Rect var3 = new Rect();
        this.getGlobalVisibleRect(var3);
        var3.offset(0, -var3.top);
        DocPageView var4 = (DocPageView) this.getOrCreateChild(var1);
        Point var5 = var4.pageToView((int) var2.left, (int) var2.top);
        Rect var6 = var4.getChildRect();
        var5.y += var6.top;
        var5.y -= this.getScrollY();
        return var3.top - var5.y;
    }

    public void scrollBy(int var1, int var2) {
        Point var3;
        if (this.mConstrained) {
            var3 = this.constrainScrollBy(var1, var2);
        } else {
            var3 = new Point(var1, var2);
        }

        super.scrollBy(var3.x, var3.y);
    }

    public void scrollEditorIntoView() {
        NoteEditor var1 = this.mNoteEditor;
        if (var1 != null && var1.isVisible()) {
            Rect var6 = new Rect();
            this.getGlobalVisibleRect(var6);
            var6.offset(-var6.left, -var6.top);
            Rect var2 = this.mNoteEditor.getRect();
            int var3 = var2.top;
            int var4 = var6.top;
            byte var5 = 0;
            if (var3 < var4) {
                var4 = var6.top - var2.top;
            } else {
                var4 = 0;
            }

            var3 = var4;
            if (var2.bottom > var6.bottom) {
                var3 = var6.bottom - var2.bottom;
            }

            var4 = var5;
            if (var2.left < var6.left) {
                var4 = var6.left - var2.left;
            }

            if (var2.right > var6.right) {
                var4 = var6.right - var2.right;
            }

            this.smoothScrollBy(var4, var3);
        }

    }

    public void scrollPointVisible(Point var1) {
        Rect var2 = new Rect();
        this.getGlobalVisibleRect(var2);
        if (var1.y < var2.top || var1.y > (var2.top + var2.bottom) / 2) {
            this.smoothScrollBy(0, (var2.top + var2.bottom) / 2 - var1.y);
        }

    }

    public void scrollSelectionIntoView() {
        if (this.mSelectionLimits != null) {
            DocPageView var1 = this.mSelectionStartPage;
            if (var1 != null) {
                SOSelectionLimits var3 = var1.getSelectionLimits();
                if (var3 != null) {
                    RectF var4 = var3.getBox();
                    if (var4 != null) {
                        int var2 = this.mSelectionStartPage.getPageNumber();
                        if (((NUIDocView) this.mHostActivity).wasTyping()) {
                            if (Utilities.isRTL(this.getContext())) {
                                var4 = new RectF(var4.left, var4.top, var4.left + 1.0F, var4.bottom);
                            } else {
                                var4 = new RectF(var4.right - 1.0F, var4.top, var4.right, var4.bottom);
                            }

                            this.scrollBoxIntoView(var2, var4, true);
                        } else {
                            this.scrollBoxIntoView(var2, var4);
                        }

                    }
                }
            }
        }
    }

    public void scrollToPage(int var1, boolean var2) {
        if (this.isValidPage(var1)) {
            int var3 = this.getPageCount();
            boolean var4 = true;
            if (var1 != var3 - 1) {
                var4 = false;
            }

            this.scrollToPage(var1, var4, var2);
        }

    }

    public void scrollToPage(int var1, boolean var2, boolean var3) {
        var1 = this.scrollToPageAmounts(var1, var2).y;
        if (var1 != 0) {
            if (var3) {
                this.scrollBy(0, var1);
            } else {
                this.smoothScrollBy(0, var1);
            }
        } else {
            this.forceLayout();
        }

    }

    public Point scrollToPageAmounts(int var1) {
        int var2 = this.getPageCount();
        boolean var3 = true;
        if (var1 != var2 - 1) {
            var3 = false;
        }

        return this.scrollToPageAmounts(var1, var3);
    }

    public Point scrollToPageAmounts(int var1, boolean var2) {
        Rect var3 = new Rect();
        this.getGlobalVisibleRect(var3);
        Point var4 = new Point();
        var4.set(this.getScrollX(), this.getScrollY());
        var3.offsetTo(var4.x, var4.y);
        Rect var6 = ((DocPageView) this.getOrCreateChild(var1)).getChildRect();
        if (var6.height() > var3.height()) {
            int var5;
            if (var2) {
                var1 = this.getScrollY() - var6.top;
                var5 = var6.height() - var3.height();
            } else {
                var1 = this.getScrollY();
                var5 = var6.top;
            }

            var1 -= var5;
        } else if (var6.top >= var3.top && var6.bottom <= var3.bottom) {
            var1 = 0;
        } else if (var6.top == 0) {
            var1 = this.getScrollY();
        } else {
            var1 = this.getScrollY() + var3.height() / 2 - (var6.bottom + var6.top) / 2;
        }

        return new Point(0, var1);
    }

    public void selectTopLeft() {
        ((DocPageView) this.getChildAt(0)).selectTopLeft();
    }

    public void setAdapter(Adapter var1) {
        this.mAdapter = (PageAdapter) var1;
        this.requestLayout();
    }

    public void setBitmaps(SOBitmap[] var1) {
        boolean var2 = false;
        if (var1[0] != null) {
            var2 = true;
        }

        this.setValid(var2);
        this.bitmaps = var1;
    }

    public void setConfigOptions(ConfigOptions var1) {
        this.mConfigOptions = var1;
    }

    public void setDoc(SODoc var1) {
        this.mDoc = var1;
    }

    public void setHost(DocViewHost var1) {
        this.mHostActivity = var1;
    }

    protected void setMostVisiblePage() {
        if (this.mTouching) {
            int var1 = this.mostVisibleChild;
            if (var1 >= 0 && !this.mScaling) {
                this.mHostActivity.setCurrentPage(var1);
            }
        }

    }

    public void setReflowMode(boolean var1) {
        this.mPreviousReflowMode = this.mReflowMode;
        this.mReflowMode = var1;
        this.mReflowWidth = -1;
    }

    public void setReflowWidth() {
        this.mReflowWidth = this.getReflowWidth();
    }

    public void setScale(float var1) {
        this.mScale = var1;
    }

    public void setSelection(int var1) {
        throw new UnsupportedOperationException(this.getContext().getString(string.sodk_editor_not_supported));
    }

    public void setShowKeyboardListener(DocView.a var1) {
        this.showKeyboardListener = var1;
    }

    public void setStartPage(int var1) {
        this.mStartPage = var1;
    }

    public void setValid(boolean var1) {
        for (int var2 = 0; var2 < this.getPageCount(); ++var2) {
            ((DocPageView) this.getOrCreateChild(var2)).setValid(var1);
        }

    }

    public void setup(RelativeLayout var1) {
        this.setupHandles(var1);
        this.setupNoteEditor();
    }

    public void setupHandles(RelativeLayout var1) {
        this.mSelectionHandleTopLeft = this.a((RelativeLayout) var1, 1);
        this.mSelectionHandleBottomRight = this.a((RelativeLayout) var1, 2);
        ImageView var2 = new ImageView(this.getContext());
        this.mResizingView = var2;
        var2.setAlpha(0.5F);
        var1.addView(this.mResizingView);
        this.mResizingView.setVisibility(View.GONE);
        this.mResizingView.setAdjustViewBounds(false);
        this.mResizingView.setScaleType(ScaleType.FIT_XY);
        this.mResizeHandleTopLeft = this.a((RelativeLayout) var1, 3);
        this.mResizeHandleTopRight = this.a((RelativeLayout) var1, 4);
        this.mResizeHandleBottomLeft = this.a((RelativeLayout) var1, 5);
        this.mResizeHandleBottomRight = this.a((RelativeLayout) var1, 6);
        this.mDragHandle = this.a((RelativeLayout) var1, 7);
        this.mRotateHandle = this.a((RelativeLayout) var1, 8);
    }

    protected void setupNoteEditor() {
        this.mNoteEditor = new NoteEditor((Activity) this.getContext(), this, this.mHostActivity, new NoteDataHandler() {
            public String getAuthor() {
                return DocView.this.getDoc().getSelectedTrackedChangeAuthor();
            }

            public String getComment() {
                String var1;
                boolean var3;
                label34:
                {
                    var1 = DocView.this.getDoc().getSelectedTrackedChangeComment();
                    int var2 = DocView.this.getDoc().getSelectedTrackedChangeType();
                    Context var4;
                    if (var2 != 5) {
                        if (var2 != 24) {
                            if (var2 != 26) {
                                if (var2 != 28) {
                                    if (var2 != 36) {
                                        switch (var2) {
                                            case 15:
                                                var4 = DocView.this.getContext();
                                                var2 = string.sodk_editor_inserted_text;
                                                break;
                                            case 16:
                                                var4 = DocView.this.getContext();
                                                var2 = string.sodk_editor_inserted_paragraph;
                                                break;
                                            case 17:
                                                var4 = DocView.this.getContext();
                                                var2 = string.sodk_editor_inserted_table_cell;
                                                break;
                                            case 18:
                                                var4 = DocView.this.getContext();
                                                var2 = string.sodk_editor_inserted_table_row;
                                                break;
                                            default:
                                                switch (var2) {
                                                    case 31:
                                                        var4 = DocView.this.getContext();
                                                        var2 = string.sodk_editor_changed_table_cell_properties;
                                                        break;
                                                    case 32:
                                                        var4 = DocView.this.getContext();
                                                        var2 = string.sodk_editor_changed_table_grid;
                                                        break;
                                                    case 33:
                                                        var4 = DocView.this.getContext();
                                                        var2 = string.sodk_editor_changed_table_properties;
                                                        break;
                                                    default:
                                                        var3 = DocView.this.mConfigOptions.b();
                                                        break label34;
                                                }
                                        }
                                    } else {
                                        var4 = DocView.this.getContext();
                                        var2 = string.sodk_editor_changed_table_row_properties;
                                    }
                                } else {
                                    var4 = DocView.this.getContext();
                                    var2 = string.sodk_editor_changed_section_properties;
                                }
                            } else {
                                var4 = DocView.this.getContext();
                                var2 = string.sodk_editor_changed_run_properties;
                            }
                        } else {
                            var4 = DocView.this.getContext();
                            var2 = string.sodk_editor_changed_paragraph_properties;
                        }
                    } else {
                        var4 = DocView.this.getContext();
                        var2 = string.sodk_editor_deleted_text;
                    }

                    var1 = var4.getString(var2);
                    var3 = false;
                }

                DocView.this.mNoteEditor.setCommentEditable(var3);
                return var1;
            }

            public String getDate() {
                return DocView.this.getDoc().getSelectedTrackedChangeDate();
            }

            public void setComment(String var1) {
                SODoc var2 = DocView.this.getDoc();
                if (var2 != null) {
                    var2.setSelectionAnnotationComment(var1);
                }

            }
        });
    }

    protected boolean shouldAddHistory(int var1, int var2, float var3) {
        HistoryItem var4 = this.getHistory().current();
        if (var4 == null) {
            return true;
        } else {
            Rect var5 = new Rect();
            this.getLocalVisibleRect(var5);
            var5.offset(var4.getScrollX(), var4.getScrollY());
            Rect var6 = new Rect();
            this.getLocalVisibleRect(var6);
            var6.offset(var1, var2);
            return !(new Rect(var5)).intersect(var6);
        }
    }

    protected boolean shouldLayout() {
        boolean var1 = this.mPreviousReflowMode;
        boolean var2 = this.mReflowMode;
        boolean var3 = true;
        if (var1 != var2) {
            var1 = true;
        } else {
            var1 = false;
        }

        if (this.mScale != this.mLastScale || this.mLastScrollX != this.getScrollX() || this.mLastScrollY != this.getScrollY()) {
            var1 = true;
        }

        if (this.mForceLayout) {
            this.mForceLayout = false;
            var1 = var3;
        }

        if (var1) {
            this.mLastScale = this.mScale;
            this.mLastScrollX = this.getScrollX();
            this.mLastScrollY = this.getScrollY();
        }

        return var1;
    }

    protected boolean shouldPreclearSelection() {
        return true;
    }

    protected void showHandle(DragHandle var1, boolean var2) {
        if (var1 != null) {
            var1.show(var2);
        }

    }

    protected void showHandles() {
        this.hideHandles();
        SOSelectionLimits var1 = this.getSelectionLimits();
        if (var1 != null) {
            boolean var2 = var1.getIsActive();
            boolean var3 = true;
            if (var2 && !var1.getIsCaret() && !NUIDocView.currentNUIDocView().getIsComposing()) {
                var2 = true;
            } else {
                var2 = false;
            }

            boolean var4;
            if (var1.getIsActive() && this.getDoc().getSelectionCanBeResized() && !var1.getIsCaret()) {
                var4 = true;
            } else {
                var4 = false;
            }

            boolean var5;
            if (var1.getIsActive() && this.getDoc().getSelectionCanBeAbsolutelyPositioned()) {
                var5 = true;
            } else {
                var5 = false;
            }

            if (!var1.getIsActive() || !this.getDoc().getSelectionCanBeRotated()) {
                var3 = false;
            }

            this.a(var2);
            this.b(var4);
            this.c(var5);
            this.d(var3);
        }

    }

    protected void showKeyboardAfterDoubleTap(Point var1) {
        if (!Utilities.isLandscapePhone(this.getContext())) {
            this.showKeyboardAndScroll(var1);
        }

    }

    protected void showKeyboardAndScroll(Point var1) {
        DocPageView var2 = this.findPageViewContainingPoint(var1.x, var1.y, false);
        if (var2 != null) {
            this.scrollToHerePage = null;
            this.scrollToHere = null;
            if (this.mHostActivity.showKeyboard()) {
                this.scrollToHerePage = var2;
                this.scrollToHere = var2.screenToPage(var1);
            }

        }
    }

    protected void smoothScrollBy(int var1, int var2) {
        this.smoothScrollBy(var1, var2, 400);
    }

    protected void smoothScrollBy(int var1, int var2, int var3) {
        if (var1 != 0 || var2 != 0) {
            this.mScrollerLastY = 0;
            this.mScrollerLastX = 0;
            this.mScroller.startScroll(0, 0, var1, var2, var3);
            this.post(this);
        }
    }

    protected void startMovingPage(int var1) {
    }

    protected boolean tapToFocus() {
        return true;
    }

    public void triggerRender() {
        if (this.bitmaps[0] != null) {
            this.renderRequested = true;
            if (this.renderCount == 0) {
                this.d();
            }

        }
    }

    protected void updateDragHandles() {
        this.mResizingView.setImageBitmap((Bitmap) null);
        this.mResizingView.setVisibility(View.GONE);
        SOBitmap var1 = this.mResizingBitmap;
        if (var1 != null && var1.a() != null) {
            this.mResizingBitmap.a().recycle();
            this.mResizingBitmap = null;
        }

        this.showHandles();
        this.moveHandlesToCorners();
    }

    protected void updateInputView() {
        if (this.b() != null) {
            this.b().updateEditable();
        }

    }

    protected void updateReview() {
        boolean var1 = this.mScrollRequested;
        this.mScrollRequested = false;
        boolean var2 = this.mAddComment;
        this.mAddComment = false;
        if (!this.getDoc().getSelectionHasAssociatedPopup() && !this.getDoc().selectionIsReviewable()) {
            NoteEditor var4 = this.mNoteEditor;
            if (var4 != null && var4.isVisible()) {
                Utilities.hideKeyboard(this.getContext());
                this.mNoteEditor.hide();
                if (var1) {
                    this.scrollSelectionIntoView();
                }
            }
        } else {
            this.mTapStatus = 0;
            SOSelectionLimits var3 = this.getSelectionLimits();
            this.mNoteEditor.show(var3, this.mSelectionStartPage);
            this.mNoteEditor.move();
            if (var1) {
                this.scrollEditorIntoView();
            }

            if (var2) {
                this.mNoteEditor.focus();
                this.mHostActivity.showKeyboard();
            }
        }

    }

    public void waitForRest(final Runnable var1) {
        if (this.isAtRest()) {
            var1.run();
        } else {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    DocView.this.waitForRest(var1);
                }
            }, 100L);
        }

    }

    protected void zoomWhilePressing(final float var1, final float var2, final boolean var3) {
        this.mPressZooming = true;
        this.mDonePressZooming = false;
        final long var4 = System.currentTimeMillis();
        final Handler var6 = new Handler();
        var6.postDelayed(new Runnable() {
            public void run() {
                long var1x = System.currentTimeMillis();
                long var3x = var1x;
                if (!DocView.this.mDonePressZooming) {
                    var3x = var1x;
                    if (var1x > var4 + (long) DocView.ZOOM_DURATION) {
                        var3x = var4 + (long) DocView.ZOOM_DURATION;
                        DocView.this.mDonePressZooming = true;
                    }
                }

                DocView var6x;
                if (var3x <= var4 + (long) DocView.ZOOM_DURATION) {
                    float var5 = (float) (var3x - var4) / (float) DocView.ZOOM_DURATION;
                    var6x = DocView.this;
                    float var7 = var1;
                    var6x.mScale = var7 + (var2 - var7) * var5;
                    var6x.scaleChildren();
                    int var8 = (int) ((float) DocView.this.mPressStartViewX * DocView.this.mScale / DocView.this.mPressStartScale);
                    int var9 = (int) ((float) DocView.this.mPressStartViewY * DocView.this.mScale / DocView.this.mPressStartScale);
                    int var10 = DocView.this.mPressStartX;
                    int var11 = DocView.this.mPressStartY;
                    int var12 = DocView.this.getScrollX();
                    int var13 = DocView.this.getScrollY();
                    var6x = DocView.this;
                    var6x.mXScroll = var6x.mXScroll - (var8 - var10 - var12);
                    var6x = DocView.this;
                    var6x.mYScroll = var6x.mYScroll - (var9 - var11 - var13);
                    DocView.this.requestLayout();
                    final ViewTreeObserver var14 = DocView.this.getViewTreeObserver();
                    var14.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            var14.removeOnGlobalLayoutListener(this);
                            var6.post(this::onGlobalLayout);
                        }
                    });
                } else {
                    DocView.this.mPressZooming = false;
                    if (var3) {
                        var6x = DocView.this;
                        var6x.mPressing = false;
                        var6x.mTouching = false;
                        DocView.this.onLongPressReleaseDone();
                    }
                }

            }
        }, (long) ZOOM_PERIOD);
    }

    public class Smoother {
        private int MAX;
        ArrayList<Integer> a = new ArrayList();

        Smoother(int var2) {
            this.MAX = var2;
        }

        public void addValue(int var1) {
            if (this.a.size() == this.MAX) {
                this.a.remove(0);
            }

            this.a.add(new Integer(var1));
        }

        public void clear() {
            this.a.clear();
        }

        public int getAverage() {
            int var1 = this.a.size();
            int var2 = 0;
            if (var1 == 0) {
                return 0;
            } else {
                for (var1 = 0; var2 < this.a.size(); ++var2) {
                    var1 += (Integer) this.a.get(var2);
                }

                return var1 / this.a.size();
            }
        }
    }

    interface a {
        void a(boolean var1);
    }
}
