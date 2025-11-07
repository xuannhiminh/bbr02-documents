//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui.editor;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow.OnDismissListener;

import com.artifex.solib.SODoc;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelViewLib;
import kankan.wheel.widget.adapters.WheelViewAdapter;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;

public class LineTypeDialogLib {
    private static final int POPUP_OFFSET = 30;
    private static final int SOLineStyle_DashDotDotSys = 4;
    private static final int SOLineStyle_DashDotGEL = 8;
    private static final int SOLineStyle_DashDotSys = 3;
    private static final int SOLineStyle_DashGEL = 6;
    private static final int SOLineStyle_DashSys = 1;
    private static final int SOLineStyle_DotGEL = 5;
    private static final int SOLineStyle_DotSys = 2;
    private static final int SOLineStyle_LongDashDotDotGEL = 10;
    private static final int SOLineStyle_LongDashDotGEL = 9;
    private static final int SOLineStyle_LongDashGEL = 7;
    private static final int SOLineStyle_Solid = 0;
    private static final float[][] patterns;
    private static final int[] styles = new int[]{0, 2, 5, 1, 6, 7, 3, 8, 9, 4, 10};

    static {
        float[] var0 = new float[]{1.0F, 1.0F};
        float[] var1 = new float[]{1.0F, 3.0F};
        float[] var2 = new float[]{3.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F};
        patterns = new float[][]{{1.0F, 0.0F}, var0, var1, {3.0F, 1.0F}, {4.0F, 3.0F}, {8.0F, 3.0F}, {3.0F, 1.0F, 1.0F, 1.0F}, {4.0F, 3.0F, 1.0F, 3.0F}, {8.0F, 3.0F, 1.0F, 3.0F}, var2, {8.0F, 3.0F, 1.0F, 3.0F, 1.0F, 3.0F}};
    }

    public LineTypeDialogLib() {
    }

    public static void show(Context var0, View var1, final SODoc var2) {
        int var3 = var2.getSelectionLineType();
        View var4 = View.inflate(var0, layout.sodk_editor_line_width_dialog, (ViewGroup) null);
        final WheelViewLib var5 = (WheelViewLib) var4.findViewById(id.wheel);
        var5.setViewAdapter(new LineTypeDialogLib.LineTypeAdapter(var0, styles));
        var5.setVisibleItems(5);
        int var6 = 0;
        var5.setCurrentItem(0);

        while (true) {
            int[] var7 = styles;
            if (var6 >= var7.length) {
                var5.addScrollingListener(new OnWheelScrollListener() {
                    @Override
                    public void onScrollingStarted(WheelViewLib wheel) {

                    }

                    @Override
                    public void onScrollingFinished(WheelViewLib wheel) {
                        var2.setSelectionLineType(LineTypeDialogLib.styles[wheel.getCurrentItem()]);
                    }
                });
                NUIPopupWindow var8 = new NUIPopupWindow(var4, -2, -2);
                var8.setFocusable(true);
                var8.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss() {
                        var5.clean();
                    }
                });
                var8.showAsDropDown(var1, 30, 30);
                return;
            }

            if (var7[var6] == var3) {
                var5.setCurrentItem(var6);
            }

            ++var6;
        }
    }

    public static class LineTypeAdapter implements WheelViewAdapter {
        private Context mContext;
        private int[] mStyles;

        public LineTypeAdapter(Context var1, int[] var2) {
            this.mStyles = var2;
            this.mContext = var1;
        }

        public View getEmptyItem(View var1, ViewGroup var2) {
            return null;
        }

        public View getItem(int var1, View var2, ViewGroup var3) {
            View var4 = var2;
            if (var2 == null) {
                var4 = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(layout.sodk_editor_line_type_item, var3, false);
            }

            int var10000 = this.mStyles[var1];
            ((DottedLineView) var4.findViewById(id.bar)).setPattern(LineTypeDialogLib.patterns[var1]);
            return var4;
        }

        public int getItemsCount() {
            return this.mStyles.length;
        }

        public void registerDataSetObserver(DataSetObserver var1) {
        }

        public void unregisterDataSetObserver(DataSetObserver var1) {
        }
    }
}
