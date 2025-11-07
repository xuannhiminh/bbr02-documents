package office.file.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.artifex.solib.SOBitmap;
import com.artifex.solib.SODoc;
import com.artifex.solib.SOHyperlink;
import com.artifex.solib.SOPage;
import com.artifex.solib.SOPageListener;
import com.artifex.solib.SORender;
import com.artifex.solib.SOSelectionLimits;
import com.artifex.solib.k;
import com.artifex.solib.o;

import office.file.ui.editor.R.color;
import office.file.ui.editor.R.integer;

public class DocPageView extends View implements SOPageListener, AnimatableView {
    private static final boolean DEBUG_PAGE_RENDERING = false;
    private static final String TAG = "DocPageView";
    private static Paint lowResPainter;
    Path a;
    private final Rect drawRect;
    private final Rect drawRectHold = new Rect();
    private float drawScale;
    private float drawScaleHold;
    private boolean isCurrent;
    private Bitmap lowResBitmap;
    private Point lowResScreenSize;
    private int mBackgroundColor;
    private int mBackgroundColorHold;
    private SOBitmap mBitmapDraw;
    private SOBitmap mBitmapDrawHold = null;
    private SOBitmap mBitmapRender = null;
    private final Paint mBlankPainter;
    private final Paint mBorderPainter;
    private final Rect mBorderRect;
    private final Rect mChildRect;
    private SODataLeakHandlers mDataLeakHandlers;
    private SODoc mDoc;
    private DocView mDocView;
    private Rect mDrawToRect = new Rect();
    private final Rect mDstRect;
    private boolean mFinished = false;
    protected int mLayer = -2;
    protected SOPage mPage;
    private int mPageNum = -1;
    protected Rect mPageRect = new Rect();
    private final Paint mPainter;
    private SORender mRender = null;
    protected PointF mRenderOrigin = new PointF();
    private Rect mRenderToRect = new Rect();
    protected float mScale = 1.0F;
    private final Paint mSelectedBorderPainter;
    protected Point mSize;
    private final Rect mSrcRect;
    protected double mZoom = 1.0D;
    private final Rect renderRect = new Rect();
    private float renderScale;
    private int[] screenLoc = new int[2];
    private boolean valid;

    public DocPageView(Context var1, SODoc var2) {
        super(var1);
        this.mBackgroundColorHold = viewx.core.content.a.c(this.getContext(), color.sodk_editor_page_default_bg_color);
        this.mBitmapDraw = null;
        this.drawRect = new Rect();
        this.mBackgroundColor = viewx.core.content.a.c(this.getContext(), color.sodk_editor_page_default_bg_color);
        this.mSrcRect = new Rect();
        this.mDstRect = new Rect();
        this.mBorderRect = new Rect();
        this.isCurrent = false;
        this.valid = true;
        this.lowResBitmap = null;
        this.lowResScreenSize = null;
        this.mDocView = null;
        this.a = null;
        this.mChildRect = new Rect();
        this.setLayoutParams(new LayoutParams(-2, -2));
        this.resetBackground();
        this.mDoc = var2;
        this.mPainter = new Paint();
        Paint var4 = new Paint();
        this.mBlankPainter = var4;
        var4.setStyle(Style.FILL);
        this.mBlankPainter.setColor(this.mBackgroundColor);
        var4 = new Paint();
        this.mBorderPainter = var4;
        var4.setColor(viewx.core.content.a.c(this.getContext(), color.sodk_editor_page_border_color));
        this.mBorderPainter.setStyle(Style.STROKE);
        this.mBorderPainter.setStrokeWidth((float) Utilities.convertDpToPixel(2.0F));
        this.mSelectedBorderPainter = new Paint();
        this.setSelectedBorderColor(viewx.core.content.a.c(this.getContext(), color.sodk_editor_selected_page_border_color));
        this.mSelectedBorderPainter.setStyle(Style.STROKE);
        this.mSelectedBorderPainter.setStrokeWidth((float) Utilities.convertDpToPixel((float) var1.getResources().getInteger(integer.sodk_editor_selected_page_border_width)));
        if (lowResPainter == null) {
            Paint var3 = new Paint();
            lowResPainter = var3;
            var3.setAntiAlias(true);
            lowResPainter.setFilterBitmap(true);
            lowResPainter.setDither(true);
        }

        this.getDataLeakHandlers();
    }

    private int a(SOBitmap var1) {
        if (!this.valid) {
            return 0;
        } else if (var1 == null) {
            return -1;
        } else if (var1.b() != null && var1.a() != null) {
            int var2 = var1.b().left + 5;
            int var3 = var1.b().top + 5;
            int var4 = var1.b().right - 5;
            int var5 = var1.b().bottom - 5;
            return this.a(new int[]{var1.a().getPixel(var2, var3), var1.a().getPixel(var4, var3), var1.a().getPixel(var2, var5), var1.a().getPixel(var4, var5)});
        } else {
            return -1;
        }
    }

    private int a(int[] var1) {
        int var2 = 0;
        int var3 = 0;
        int var4 = 0;
        int var5 = 0;

        int var6;
        for (var6 = 0; var2 < var1.length; ++var2) {
            int var7 = var1[var2];
            var4 += var7 >> 16 & 255;
            var5 += var7 >> 8 & 255;
            var6 += var7 & 255;
            var3 += var7 >>> 24;
        }

        return Color.argb(var3 / var1.length, var4 / var1.length, var5 / var1.length, var6 / var1.length);
    }

    private Point a(float var1, float var2) {
        return this.screenToPage((int) var1, (int) var2);
    }

    private void a(SOBitmap var1, final o var2, Rect var3, Rect var4) {
        this.mRenderToRect.set(var4);
        this.mRenderToRect.offset(NUIDocView.OVERSIZE_MARGIN, NUIDocView.OVERSIZE_MARGIN);
        this.mDrawToRect.set(var3);
        this.setPageRect();
        int var5 = Math.min(Math.max(this.mRenderToRect.top - this.mPageRect.top, 0), NUIDocView.OVERSIZE_MARGIN);
        int var6 = Math.min(Math.max(this.mPageRect.bottom - this.mRenderToRect.bottom, 0), NUIDocView.OVERSIZE_MARGIN);
        int var7 = Math.min(Math.max(this.mRenderToRect.left - this.mPageRect.left, 0), NUIDocView.OVERSIZE_MARGIN);
        int var8 = Math.min(Math.max(this.mPageRect.right - this.mRenderToRect.right, 0), NUIDocView.OVERSIZE_MARGIN);
        int var9 = var7;
        int var10 = var8;
        if (this.b()) {
            if (this.getParent() instanceof DocListPagesView) {
                var9 = Math.min(Math.max(this.mRenderToRect.left - this.mPageRect.left, 0), 0);
                var10 = var8;
            } else {
                var10 = Math.min(Math.max(this.mPageRect.right - this.mRenderToRect.right, 0), 0);
                var9 = var7;
            }
        }

        var3 = this.mRenderToRect;
        var3.top -= var5;
        var3 = this.mRenderToRect;
        var3.bottom += var6;
        var3 = this.mRenderToRect;
        var3.left -= var9;
        var3 = this.mRenderToRect;
        var3.right += var10;
        var3 = this.mDrawToRect;
        var3.top -= var5;
        var3 = this.mDrawToRect;
        var3.bottom += var6;
        var3 = this.mDrawToRect;
        var3.left -= var9;
        var3 = this.mDrawToRect;
        var3.right += var10;
        if (this.mRenderToRect.left < 0) {
            var10 = -this.mRenderToRect.left;
            var3 = this.mRenderToRect;
            var3.left += var10;
            var3 = this.mDrawToRect;
            var3.left += var10;
        }

        if (this.mRenderToRect.right > var1.c()) {
            var10 = this.mRenderToRect.right - var1.c();
            var3 = this.mRenderToRect;
            var3.right -= var10;
            var3 = this.mDrawToRect;
            var3.right -= var10;
        }

        if (this.mRenderToRect.top < 0) {
            var10 = -this.mRenderToRect.top;
            var3 = this.mRenderToRect;
            var3.top += var10;
            var3 = this.mDrawToRect;
            var3.top += var10;
        }

        if (this.mRenderToRect.bottom > var1.d()) {
            var10 = this.mRenderToRect.bottom - var1.d();
            var3 = this.mRenderToRect;
            var3.bottom -= var10;
            var3 = this.mDrawToRect;
            var3.bottom -= var10;
        }

        this.renderRect.set(this.mDrawToRect);
        this.renderScale = this.mScale;
        this.mBitmapRender = new SOBitmap(var1, this.mRenderToRect.left, this.mRenderToRect.top, this.mRenderToRect.right, this.mRenderToRect.bottom);
        this.setOrigin();
        SOPage var15 = this.mPage;
        var10 = this.mLayer;
        double var11 = (double) this.mScale;
        double var13 = this.mZoom;
//        Double.isNaN(var11);
//        Double.isNaN(var11);
//        Double.isNaN(var11);
//        Double.isNaN(var11);
        if (mRenderOrigin != null && mBitmapRender != null && var15 != null) {

            this.mRender = var15.a(var10, var11 * var13, (double) this.mRenderOrigin.x, (double) this.mRenderOrigin.y, this.mBitmapRender, (SOBitmap) null, new o() {
                public void a(int var1) {
                    if (!DocPageView.this.mFinished) {
                        DocPageView.this.stopRender();
                        if (var1 == 0) {
                            DocPageView var2x = DocPageView.this;
                            var2x.mBitmapDrawHold = var2x.mBitmapRender;
                            DocPageView.this.drawRectHold.set(DocPageView.this.renderRect);
                            var2x = DocPageView.this;
                            var2x.drawScaleHold = var2x.renderScale;
                            var2x = DocPageView.this;
                            var2x.mBackgroundColorHold = var2x.a(var2x.mBitmapDrawHold);
                        } else {
                            System.out.printf("render error %d for page %d  %n", var1, DocPageView.this.mPageNum);
                        }

                        var2.a(var1);
                    }
                }
            }, true, k.c(this.getContext()));
        }
    }


    private boolean a() {
        DocView var1 = this.getDocView();
        return var1 != null && var1.getReflowMode();
    }

    private boolean b() {
        DocView var1 = this.getDocView();
        return var1 != null && var1.pagesShowing();
    }

    private void getDataLeakHandlers() {
        // $FF: Couldn't be decompiled
    }

    protected boolean canDoubleTap(int var1, int var2) {
        return true;
    }

    protected void changePage(int var1) {
        if (!this.isFinished()) {
            if (this.valid) {
                if (this.mDoc != null) {
                    if (var1 != this.mPageNum || this.mPage == null) {
                        this.mPageNum = var1;
                        this.dropPage();
                        SOPage var2 = this.mDoc.getPage(this.mPageNum, this);
                        this.mPage = var2;
                        this.mDoc.a(var2);
                    }

                }
            }
        }
    }

    public void clearContent() {
        this.mBitmapDraw = null;
        this.invalidate();
    }

    protected void dropPage() {
        SOPage var1 = this.mPage;
        if (var1 != null) {
            this.mDoc.b(var1);
            this.mPage.m();
        }

//        this.mPage = null;
    }

    public void endRenderPass() {
        this.mBitmapDraw = this.mBitmapDrawHold;
        this.drawRect.set(this.drawRectHold);
        this.drawScale = this.drawScaleHold;
        this.mBackgroundColor = this.mBackgroundColorHold;
    }

    public void finish() {

        this.mFinished = true;
        this.stopRender();
        SOPage var1 = this.mPage;
        if (var1 != null) {
            var1.n();
            this.mPage = null;
        }

        this.mBitmapDraw = null;
        this.mBitmapDrawHold = null;
        this.mBitmapRender = null;
        this.mDoc = null;
    }

    public Rect getChildRect() {
        return this.mChildRect;
    }

    public Path getClipPath() {
        return this.a;
    }

    protected SODoc getDoc() {
        return this.mDoc;
    }

    protected DocView getDocView() {
        return this.mDocView;
    }

    public double getFactor() {
        double var1 = this.mZoom;
        double var3 = (double) this.mScale;
        Double.isNaN(var3);
        Double.isNaN(var3);
        Double.isNaN(var3);
        Double.isNaN(var3);
        return var1 * var3;
    }

    public float[] getHorizontalRuler() {
        return this.mFinished ? null : this.mPage.getHorizontalRuler();
    }

    public SOPage getPage() {
        return this.mPage;
    }

    public int getPageNumber() {
        return this.mPageNum;
    }

    public int getReflowWidth() {
        return this.mPage.sizeAtZoom(1.0D).x;
    }

    SOSelectionLimits getSelectionLimits() {
        SOPage var1 = this.mPage;
        return var1 == null ? null : var1.selectionLimits();
    }

    public Point getSize() {
        return this.mSize;
    }

    public int getUnscaledHeight() {
        Point var1;
        if (this.a()) {
            var1 = this.mPage.sizeAtZoom(this.mZoom);
        } else {
            var1 = this.mSize;
        }

        return var1.y;
    }

    public int getUnscaledWidth() {
        Point var1;
        if (this.a()) {
            var1 = this.mPage.sizeAtZoom(this.mZoom);
        } else {
            var1 = this.mSize;
        }

        return var1.x;
    }

    public float[] getVerticalRuler() {
        return this.mFinished ? null : this.mPage.getVerticalRuler();
    }

    public double getZoomScale() {
        double var1 = this.mZoom;
        double var3 = (double) this.mScale;
        Double.isNaN(var3);
        Double.isNaN(var3);
        Double.isNaN(var3);
        Double.isNaN(var3);
        return var1 * var3;
    }

    protected boolean handleFullscreenTap(int var1, int var2) {
        return this.tryHyperlink(new Point(var1, var2), (DocPageView.ExternalLinkListener) null);
    }

    public boolean isCurrent() {
        return this.isCurrent;
    }

    protected boolean isFinished() {
        return this.mFinished;
    }

    protected boolean isValid() {
        return this.valid;
    }

    protected void launchHyperLink(String var1) {
        SODataLeakHandlers var2 = this.mDataLeakHandlers;
        if (var2 != null) {
            try {
                var2.launchUrlHandler(var1);
            } catch (UnsupportedOperationException var3) {
                var3.printStackTrace();
            }

        }
    }

    public void onDoubleTap(int var1, int var2) {
        Point var3 = this.screenToPage(var1, var2);
        this.mPage.select(2, (double) var3.x, (double) var3.y);
        NUIDocView.currentNUIDocView().showUI(true);
    }

    public void onDraw(Canvas var1) {
        if (!this.mFinished) {
            if (this.isShown()) {
                if (this.mPage != null) {
                    Bitmap var2 = this.lowResBitmap;
                    Rect var3;
                    Rect var6;
                    if (var2 == null) {
                        if (this.valid) {
                            this.mBlankPainter.setColor(this.mBackgroundColor);
                            var6 = new Rect();
                            this.getLocalVisibleRect(var6);
                            var1.drawRect(var6, this.mBlankPainter);
                            if (this.a != null) {
                                var1.save();
                            }

                            SOBitmap var7 = this.mBitmapDraw;
                            if (var7 != null && !var7.a().isRecycled()) {
                                this.mSrcRect.set(var7.b());
                                this.mDstRect.set(this.drawRect);
                                if (this.drawScale != this.mScale) {
                                    var3 = this.mDstRect;
                                    var3.left = (int) ((float) var3.left * (this.mScale / this.drawScale));
                                    var3 = this.mDstRect;
                                    var3.top = (int) ((float) var3.top * (this.mScale / this.drawScale));
                                    var3 = this.mDstRect;
                                    var3.right = (int) ((float) var3.right * (this.mScale / this.drawScale));
                                    var3 = this.mDstRect;
                                    var3.bottom = (int) ((float) var3.bottom * (this.mScale / this.drawScale));
                                }

                                Path var9 = this.a;
                                if (var9 != null) {
                                    var1.clipPath(var9);
                                }

                                var1.drawBitmap(var7.a(), this.mSrcRect, this.mDstRect, this.mPainter);
                                this.mBorderRect.set(0, 0, this.getWidth(), this.getHeight());
                                Paint var8;
                                if (this.isCurrent) {
                                    var3 = this.mBorderRect;
                                    var8 = this.mSelectedBorderPainter;
                                } else {
                                    var3 = this.mBorderRect;
                                    var8 = this.mBorderPainter;
                                }

                                var1.drawRect(var3, var8);
                                if (this.a != null) {
                                    var1.restore();
                                }
                            }

                        }
                    } else {
                        var3 = new Rect(0, 0, var2.getWidth(), this.lowResBitmap.getHeight());
                        Point var4 = Utilities.getScreenSize(this.getContext());
                        Point var5 = this.lowResScreenSize;
                        if (var5 == null || var4 == null || var5.x == var4.x && this.lowResScreenSize.y == var4.y) {
                            var6 = this.drawRect;
                        } else {
                            var6 = new Rect();
                            this.getLocalVisibleRect(var6);
                        }

                        var1.drawBitmap(this.lowResBitmap, var3, var6, this.mPainter);
                    }
                }
            }
        }
    }

    public void onFullscreen(boolean var1) {
    }

    public void onReflowScale(DocPageView var1) {
        this.mZoom = var1.mZoom;
        this.mScale = var1.mScale;
        this.mSize.x = var1.mSize.x;
        this.mSize.y = var1.mSize.y;
        this.requestLayout();
    }

    public boolean onSingleTap(int var1, int var2, boolean var3, DocPageView.ExternalLinkListener var4) {
        Point var5 = this.screenToPage(var1, var2);
        if (this.tryHyperlink(var5, var4)) {
            return true;
        } else {
            if (var3) {
                this.getDoc().clearSelection();
                this.mPage.select(3, (double) var5.x, (double) var5.y);
            }

            return false;
        }
    }

    public Point pageToScreen(Point var1) {
        double var2 = this.getFactor();
        double var4 = (double) var1.x;
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        int var6 = (int) (var4 * var2);
        var4 = (double) var1.y;
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        int var7 = (int) (var4 * var2);
        int[] var8 = new int[2];
        this.getLocationOnScreen(var8);
        return new Point(var6 + var8[0], var7 + var8[1]);
    }

    public Rect pageToScreen(RectF var1) {
        double var2 = this.getFactor();
        double var4 = (double) var1.left;
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        int var6 = (int) (var4 * var2);
        var4 = (double) var1.top;
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        int var7 = (int) (var4 * var2);
        var4 = (double) var1.right;
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        int var8 = (int) (var4 * var2);
        var4 = (double) var1.bottom;
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        int var9 = (int) (var4 * var2);
        int[] var10 = new int[2];
        this.getLocationOnScreen(var10);
        return new Rect(var6 + var10[0], var7 + var10[1], var8 + var10[0], var9 + var10[1]);
    }

    public int pageToView(int var1) {
        double var2 = this.getFactor();
        double var4 = (double) var1;
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        return (int) (var4 * var2);
    }

    public Point pageToView(int var1, int var2) {
        return new Point(this.pageToView(var1), this.pageToView(var2));
    }

    protected Rect pageToView(RectF var1) {
        double var2 = this.getFactor();
        Rect var4 = new Rect();
        double var5 = (double) var1.left;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        var4.left = (int) Math.round(var5 * var2);
        var5 = (double) var1.top;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        var4.top = (int) Math.round(var5 * var2);
        var5 = (double) var1.right;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        var4.right = (int) Math.round(var5 * var2);
        var5 = (double) var1.bottom;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        var4.bottom = (int) Math.round(var5 * var2);
        return var4;
    }

    protected void pageToView(PointF var1, PointF var2) {
        double var3 = this.getFactor();
        float var5 = var1.x;
        float var6 = (float) var3;
        var2.set(var5 * var6, var1.y * var6);
    }

    public void pageToView(Rect var1, Rect var2) {
        double var3 = this.getFactor();
        double var5 = (double) var1.left;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        int var7 = (int) (var5 * var3);
        var5 = (double) var1.top;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        int var8 = (int) (var5 * var3);
        var5 = (double) var1.right;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        int var9 = (int) (var5 * var3);
        var5 = (double) var1.bottom;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        var2.set(var7, var8, var9, (int) (var5 * var3));
    }

    public void render(SOBitmap var1, o var2) {
        if (!this.mFinished) {
            Rect var3 = new Rect();
            if (!this.getLocalVisibleRect(var3)) {
                var2.a(0);
            } else {
                Rect var4 = new Rect();
                if (!this.getGlobalVisibleRect(var4)) {
                    var2.a(0);
                } else {
                    this.a(var1, var2, var3, var4);
                }
            }
        }
    }

    public void resetBackground() {
        this.mBackgroundColor = viewx.core.content.a.c(this.getContext(), color.sodk_editor_page_default_bg_color);
        this.mBackgroundColorHold = viewx.core.content.a.c(this.getContext(), color.sodk_editor_page_default_bg_color);
    }

    public void resize(int var1, int var2) {
        SOPage var3 = this.mPage;
        if (var3 != null) {
            PointF var6 = var3.zoomToFitRect(var1, var2);
            double var4 = (double) Math.max(var6.x, var6.y);
            this.mZoom = var4;
            this.mSize = this.mPage.sizeAtZoom(var4);
        }
    }

    public Rect screenRect() {
        int[] var1 = new int[2];
        this.getLocationOnScreen(var1);
        Rect var2 = new Rect();
        var2.set(var1[0], var1[1], var1[0] + this.getChildRect().width(), var1[1] + this.getChildRect().height());
        return var2;
    }

    protected Point screenToPage(int var1, int var2) {
        int[] var3 = new int[2];
        this.getLocationOnScreen(var3);
        var3 = Utilities.screenToWindow(var3, this.getContext());
        int var4 = var3[0];
        int var5 = var3[1];
        double var6 = this.getFactor();
        double var8 = (double) (var1 - var4);
        Double.isNaN(var8);
        Double.isNaN(var8);
        Double.isNaN(var8);
        Double.isNaN(var8);
        var1 = (int) (var8 / var6);
        var8 = (double) (var2 - var5);
        Double.isNaN(var8);
        Double.isNaN(var8);
        Double.isNaN(var8);
        Double.isNaN(var8);
        return new Point(var1, (int) (var8 / var6));
    }

    protected Point screenToPage(Point var1) {
        return this.screenToPage(var1.x, var1.y);
    }

    protected PointF screenToPage(PointF var1) {
        return new PointF(this.a(var1.x, var1.y));
    }

    protected Rect screenToPage(Rect var1) {
        Point var2 = this.screenToPage(var1.left, var1.top);
        Point var3 = this.screenToPage(var1.right, var1.bottom);
        return new Rect(var2.x, var2.y, var3.x, var3.y);
    }

    public void selectTopLeft() {
        this.mPage.select(2, 0.0D, 0.0D);
    }

    public SOSelectionLimits selectionLimits() {
        return this.mFinished ? null : this.mPage.selectionLimits();
    }

    public void setCaret(int var1, int var2) {
        Point var3 = this.screenToPage(var1, var2);
        SOHyperlink var4 = this.mPage.objectAtPoint((float) var3.x, (float) var3.y);
        if ((var4 == null || var4.url == null) && var4.pageNum == -1) {
            this.mPage.select(3, (double) var3.x, (double) var3.y);
        }
    }

    public void setChildRect(Rect var1) {
        this.mChildRect.set(var1);
    }

    public void setClipPath(Path var1) {
        this.a = var1;
    }

    public void setCurrent(boolean var1) {
        if (var1 != this.isCurrent) {
            this.isCurrent = var1;
            this.invalidate();
        }

    }

    public void setDocView(DocView var1) {
        this.mDocView = var1;
    }

    public void setLayer(int var1) {
        this.mLayer = var1;
    }

    public void setNewScale(float var1) {
        this.mScale = var1;
    }

    protected void setOrigin() {
        this.mRenderOrigin.set((float) (-this.mDrawToRect.left), (float) (-this.mDrawToRect.top));
    }

    protected void setPageRect() {
        this.getLocationOnScreen(this.screenLoc);
        Rect var1 = this.mPageRect;
        int[] var2 = this.screenLoc;
        var1.set(var2[0], var2[1], var2[0] + this.getChildRect().width(), this.screenLoc[1] + this.getChildRect().height());
        this.mPageRect.offset(NUIDocView.OVERSIZE_MARGIN, NUIDocView.OVERSIZE_MARGIN);
    }

    public void setSelectedBorderColor(int var1) {
        this.mSelectedBorderPainter.setColor(var1);
    }

    public void setSelectionEnd(Point var1) {
        var1 = this.screenToPage(var1);
        PointF var2 = new PointF((float) var1.x, (float) var1.y);
        this.mPage.a(1, var2);
    }

    public void setSelectionStart(Point var1) {
        var1 = this.screenToPage(var1);
        PointF var2 = new PointF((float) var1.x, (float) var1.y);
        this.mPage.a(0, var2);
    }

    public void setValid(boolean var1) {
        if (var1 != this.valid) {
            this.valid = var1;
            if (!var1) {
                if (this.isShown()) {
                    SOBitmap var2 = this.mBitmapDraw;
                    if (var2 != null && !var2.a().isRecycled()) {
                        this.lowResScreenSize = Utilities.getScreenSize(this.getContext());
                        int var3 = this.mBitmapDraw.c() / 2;
                        int var4 = this.mBitmapDraw.d() / 2;
                        Rect var5 = new Rect(0, 0, var3, var4);
                        this.lowResBitmap = Bitmap.createBitmap(var3, var4, Config.ARGB_8888);
                        (new Canvas(this.lowResBitmap)).drawBitmap(this.mBitmapDraw.a(), this.mBitmapDraw.b(), var5, lowResPainter);
                    }
                }

                this.mBitmapDraw = null;
                this.mBitmapDrawHold = null;
                this.mBitmapRender = null;
            } else {
                Bitmap var6 = this.lowResBitmap;
                if (var6 != null) {
                    var6.recycle();
                }

                this.lowResBitmap = null;
            }

            this.invalidate();
        }
    }

    public void setupPage(int var1, int var2, int var3) {
        if (!this.isFinished()) {
            if (this.valid) {
                this.changePage(var1);
                this.resize(var2, 1);
            }
        }
    }

    public boolean sizeViewToPage() {
        SOPage var1 = this.mPage;
        boolean var2 = false;
        if (var1 == null) {
            return false;
        } else {
            Point var5 = this.mSize;
            if (var5 == null) {
                return false;
            } else {
                int var3 = var5.x;
                int var4 = this.mSize.y;
                var5 = this.mPage.sizeAtZoom(this.mZoom);
                this.mSize = var5;
                if (var5 == null) {
                    return false;
                } else {
                    if (var5.x != var3 || this.mSize.y != var4) {
                        var2 = true;
                    }

                    return var2;
                }
            }
        }
    }

    public void startRenderPass() {
    }

    public void stopRender() {
        SORender var1 = this.mRender;
        if (var1 != null) {
            var1.abort();
            this.mRender.destroy();
            this.mRender = null;
        }

    }

    protected boolean tryHyperlink(Point var1, DocPageView.ExternalLinkListener var2) {
        SOHyperlink var3 = this.mPage.objectAtPoint((float) var1.x, (float) var1.y);
        if (var3 != null) {
            if (var3.url != null) {
                if (k.c().n()) {
                    this.launchHyperLink(var3.url);
                }

                return true;
            }

            if (var3.pageNum != -1) {
                if (var2 != null) {
                    var2.handleExternalLink(var3.pageNum, var3.bbox);
                }

                return true;
            }
        }

        return false;
    }

    public void update(RectF var1) {
        if (!this.mFinished) {
            if (this.isShown()) {
                ((Activity) this.getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        NUIDocView var1 = NUIDocView.currentNUIDocView();
                        if (var1 != null) {
                            var1.triggerRender();
                        }

                    }
                });
            }
        }
    }

    public int viewToPage(int var1) {
        double var2 = this.getFactor();
        double var4 = (double) var1;
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        Double.isNaN(var4);
        return (int) (var4 / var2);
    }

    public Point viewToPage(int var1, int var2) {
        double var3 = this.getFactor();
        double var5 = (double) var1;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        var1 = (int) (var5 / var3);
        var5 = (double) var2;
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        Double.isNaN(var5);
        return new Point(var1, (int) (var5 / var3));
    }

    public interface ExternalLinkListener {
        void handleExternalLink(int var1, Rect var2);
    }
}
