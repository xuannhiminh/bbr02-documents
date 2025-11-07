//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;

import com.artifex.solib.SODoc;

import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.R.string;

public class ColorDialogLib implements OnTouchListener, OnDismissListener {
    public static final int BG_COLORS = 2;
    public static final int FG_COLORS = 1;
    private static ColorDialogLib singleton;
    private final Point down = new Point();
    private final View mAnchor;
    private final Context mContext;
    private final int mDialogType;
    private final SODoc mDoc;
    private final String[] mFgColors = new String[]{
            "#000000", "#FFFFFF", "#FDC685", "#609DFF", "#E55E22", "#A2E192", "#FF5656",
            "#FFEE51", "#75F891", "#FF81DC", "#c43030", "#971670", "#ffb683", "#cc7cff",
            "#2b51bc", "#217ca9", "#2ca9bf", "#59c19c"
    };
    private final ColorChangedListener mListener;
    private boolean mShowTitle = true;
    private NUIPopupWindow popupWindow;
    private int[] start;

    public ColorDialogLib(int var1, Context var2, SODoc var3, View var4, ColorChangedListener var5) {
        this.mContext = var2;
        this.mAnchor = var4;
        this.mListener = var5;
        this.mDialogType = var1;
        this.mDoc = var3;
    }

    private void a() {
        this.popupWindow.dismiss();
        singleton = null;
    }

    public void onDismiss() {
        this.a();
    }

    public boolean onTouch(View var1, MotionEvent var2) {
        int var3 = var2.getAction();
        if (var3 != 0) {
            if (var3 == 2) {
                int var4 = this.down.x;
                int var5 = (int) var2.getRawX();
                int var6 = this.down.y;
                var3 = (int) var2.getRawY();
                NUIPopupWindow var7 = this.popupWindow;
                int[] var8 = this.start;
                var7.update(var8[0] - (var4 - var5), var8[1] - (var6 - var3), -1, -1, true);
            }
        } else {
            this.start = new int[2];
            this.popupWindow.getContentView().getLocationOnScreen(this.start);
            this.down.set((int) var2.getRawX(), (int) var2.getRawY());
        }

        return true;
    }

    public void setShowTitle(boolean var1) {
        this.mShowTitle = var1;
    }

    public void show() {
        singleton = this;
        View var1 = LayoutInflater.from(this.mContext).inflate(layout.sodk_editor_colors, (ViewGroup) null);
        SOTextView var2 = (SOTextView) var1.findViewById(id.color_dialog_title);
        int var4;
        if (this.mShowTitle) {
            Context var3;
            if (this.mDialogType == 2) {
                var3 = this.mContext;
                var4 = string.sodk_editor_background;
            } else {
                var3 = this.mContext;
                var4 = string.sodk_editor_color;
            }

            var2.setText(var3.getString(var4));
        } else {
            var2.setVisibility(View.GONE);
        }

        String[] var14;
        var14 = this.mFgColors;
//        if (this.mDialogType == 2) {
//            var14 = this.mDoc.getBgColorList();
//        } else {
//            var14 = this.mFgColors;
//        }

        LinearLayout var13 = (LinearLayout) var1.findViewById(id.fontcolors_row1);
        LinearLayout var5 = (LinearLayout) var1.findViewById(id.fontcolors_row2);
        LinearLayout var6 = (LinearLayout) var1.findViewById(id.fontcolors_row3);
        var4 = 0;

        for (int var7 = 0; var4 < 3; ++var4) {
            LinearLayout var8 = (new LinearLayout[]{var13, var5, var6})[var4];
            int var9 = var8.getChildCount();

            int var12;
            for (int var10 = 0; var10 < var9; var7 = var12) {
                Button var11 = (Button) var8.getChildAt(var10);
                var12 = var7 + 1;
                if (var12 <= var14.length) {
                    var11.setVisibility(View.VISIBLE);
                    var11.getBackground().setColorFilter(Color.parseColor(var14[var7]), PorterDuff.Mode.SRC);
//                    var11.setBackgroundColor(Color.parseColor(var14[var7]));
                    var11.setTag(var14[var7]);
                    var11.setOnClickListener(new OnClickListener() {
                        public void onClick(View var1) {
                            ColorDialogLib.this.mListener.onColorChanged((String) var1.getTag());
                            ColorDialogLib.this.onDismiss();
                        }
                    });
                } else {
                    var11.setVisibility(View.GONE);
                }

                ++var10;
            }
        }

        Button var15 = (Button) var1.findViewById(id.transparent_color_button);
        if (this.mDialogType == 2) {
            var15.setVisibility(View.VISIBLE);
            var15.setOnClickListener(new OnClickListener() {
                public void onClick(View var1) {
                    ColorDialogLib.this.mListener.onColorChanged((String) var1.getTag());
                    ColorDialogLib.this.onDismiss();
                }
            });
        } else {
            var15.setVisibility(View.GONE);
        }

        NUIPopupWindow var16 = new NUIPopupWindow(this.mContext);
        this.popupWindow = var16;
        var16.setContentView(var1);
        this.popupWindow.setClippingEnabled(false);
        var1.setOnTouchListener(this);
        this.popupWindow.setOnDismissListener(this);
        this.popupWindow.setFocusable(true);
        var1.measure(0, 0);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        this.popupWindow.setWidth(var1.getMeasuredWidth() < width ? var1.getMeasuredWidth() : width);
        this.popupWindow.setHeight(var1.getMeasuredHeight());

        this.popupWindow.showAtLocation(this.mAnchor, 51, 0, height - var1.getMeasuredHeight());
    }
}
