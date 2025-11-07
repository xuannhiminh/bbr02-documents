//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
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
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.R.string;

public class InkColorDialogLib implements OnTouchListener, OnDismissListener {
    public static final int BG_COLORS = 2;
    public static final int FG_COLORS = 1;
    private static InkColorDialogLib singleton;
    private final Point down = new Point();
    private final View mAnchor;
    private boolean mAutoDismiss = false;
    private final Context mContext;
    private final int mDialogType;
    private final String[] mFgColors = new String[]{"#000000", "#FFFFFF", "#D8D8D8", "#808080", "#EEECE1", "#1F497D", "#0070C0", "#C0504D", "#9BBB59", "#8064A2", "#4BACC6", "#F79646", "#FF0000", "#FFFF00", "#DBE5F1", "#F2DCDB", "#EBF1DD", "#00B050"};
    private final InkColorDialogLib.ColorChangedListener mListener;
    private boolean mShowTitle = true;
    private NUIPopupWindow popupWindow;
    private int[] start;

    public InkColorDialogLib(int var1, Context var2, View var3, InkColorDialogLib.ColorChangedListener var4, boolean var5) {
        this.mContext = var2;
        this.mAnchor = var3;
        this.mListener = var4;
        this.mDialogType = var1;
        this.mAutoDismiss = var5;
    }

    public void dismiss() {
        this.popupWindow.dismiss();
        singleton = null;
    }

    public void onDismiss() {
        this.dismiss();
    }

    public boolean onTouch(View var1, MotionEvent var2) {
        int var3 = var2.getAction();
        if (var3 != 0) {
            if (var3 == 2) {
                int var4 = this.down.x;
                int var5 = (int)var2.getRawX();
                int var6 = this.down.y;
                var3 = (int)var2.getRawY();
                NUIPopupWindow var7 = this.popupWindow;
                int[] var8 = this.start;
                var7.update(var8[0] - (var4 - var5), var8[1] - (var6 - var3), -1, -1, true);
            }
        } else {
            this.start = new int[2];
            this.popupWindow.getContentView().getLocationOnScreen(this.start);
            this.down.set((int)var2.getRawX(), (int)var2.getRawY());
        }

        return true;
    }

    public void setShowTitle(boolean var1) {
        this.mShowTitle = var1;
    }

    public void show() {
        singleton = this;
        View var1 = LayoutInflater.from(this.mContext).inflate(layout.sodk_editor_colors, (ViewGroup)null);
        SOTextView var2 = (SOTextView)var1.findViewById(id.color_dialog_title);
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
            var2.setVisibility(8);
        }

        String[] var13 = this.mFgColors;
        LinearLayout var5 = (LinearLayout)var1.findViewById(id.fontcolors_row1);
        LinearLayout var6 = (LinearLayout)var1.findViewById(id.fontcolors_row2);
        LinearLayout var7 = (LinearLayout)var1.findViewById(id.fontcolors_row3);
        var4 = 0;

        Button var14;
        for(int var8 = 0; var4 < 3; ++var4) {
            LinearLayout var9 = (new LinearLayout[]{var5, var6, var7})[var4];
            int var10 = var9.getChildCount();

            int var12;
            for(int var11 = 0; var11 < var10; var8 = var12) {
                var14 = (Button)var9.getChildAt(var11);
                var12 = var8 + 1;
                if (var12 <= var13.length) {
                    var14.setVisibility(0);
                    var14.setBackgroundColor(Color.parseColor(var13[var8]));
                    var14.setTag(var13[var8]);
                    var14.setOnClickListener(new OnClickListener() {
                        public void onClick(View var1) {
                            InkColorDialogLib.this.mListener.onColorChanged((String)var1.getTag());
                            if (InkColorDialogLib.this.mAutoDismiss) {
                                InkColorDialogLib.this.dismiss();
                            }

                        }
                    });
                } else {
                    var14.setVisibility(8);
                }

                ++var11;
            }
        }

        var14 = (Button)var1.findViewById(id.transparent_color_button);
        if (this.mDialogType == 2) {
            var14.setVisibility(0);
            var14.setOnClickListener(new OnClickListener() {
                public void onClick(View var1) {
                    InkColorDialogLib.this.mListener.onColorChanged((String)var1.getTag());
                }
            });
        } else {
            var14.setVisibility(8);
        }

        var4 = Utilities.getScreenSize(this.mContext).x;
        NUIPopupWindow var15 = new NUIPopupWindow(var1, -2, -2);
        this.popupWindow = var15;
        var15.setFocusable(true);
        var1.measure(0, 0);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        this.popupWindow.setWidth(var1.getMeasuredWidth() < width ? var1.getMeasuredWidth() : width);
        this.popupWindow.showAtLocation(this.mAnchor, 51, var4 - var1.getMeasuredWidth() - 15, height - var1.getMeasuredHeight());
        this.popupWindow.setClippingEnabled(false);
        var1.setOnTouchListener(this);
        this.popupWindow.setOnDismissListener(this);
    }

    public interface ColorChangedListener {
        void onColorChanged(String var1);
    }
}
