//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.artifex.solib.SODoc;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelViewLib;
import kankan.wheel.widget.adapters.WheelViewAdapter;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;

public class LineWidthDialogLib {
    private static final int POPUP_OFFSET = 30;
    private static final float[] values = new float[]{0.25F, 0.5F, 1.0F, 1.5F, 3.0F, 4.5F, 6.0F, 8.0F, 12.0F, 18.0F, 24.0F};

    public LineWidthDialogLib() {
    }

    public static void show(Context var0, View var1, final SODoc var2) {
        float var3 = var2.getSelectionLineWidth();
        View var4 = View.inflate(var0, layout.sodk_editor_line_width_dialog, (ViewGroup)null);
        final WheelViewLib var5 = (WheelViewLib)var4.findViewById(id.wheel);
        var5.setViewAdapter(new LineWidthDialogLib.LineWidthAdapter(var0, values));
        var5.setVisibleItems(5);
        int var6 = 0;
        var5.setCurrentItem(0);

        while(true) {
            float[] var7 = values;
            if (var6 >= var7.length) {
                var5.addScrollingListener(new OnWheelScrollListener() {
                    @Override
                    public void onScrollingStarted(WheelViewLib wheel) {

                    }

                    @Override
                    public void onScrollingFinished(WheelViewLib wheel) {
                        var2.setSelectionLineWidth(LineWidthDialogLib.values[wheel.getCurrentItem()]);
                    }
                });
                NUIPopupWindow var8 = new NUIPopupWindow(var4, -2, -2);
                var8.setFocusable(true);
                var8.setOnDismissListener(() -> var5.clean());
                var8.showAsDropDown(var1, 30, 30);
                return;
            }

            if (var7[var6] == var3) {
                var5.setCurrentItem(var6);
            }

            ++var6;
        }
    }

    public static class LineWidthAdapter implements WheelViewAdapter {
        private Context mContext;
        private float[] mValues;

        public LineWidthAdapter(Context var1, float[] var2) {
            this.mValues = var2;
            this.mContext = var1;
        }

        public View getEmptyItem(View var1, ViewGroup var2) {
            return null;
        }

        @SuppressLint("WrongConstant")
        public View getItem(int var1, View var2, ViewGroup var3) {
            View var4 = var2;
            if (var2 == null) {
                var4 = ((LayoutInflater)this.mContext.getSystemService("layout_inflater")).inflate(layout.sodk_editor_line_width_item, var3, false);
            }

            float var5 = LineWidthDialogLib.values[var1];
            SOTextView var7 = (SOTextView)var4.findViewById(id.value);
            StringBuilder var8 = new StringBuilder();
            var8.append(Utilities.formatFloat(var5));
            var8.append(" pt");
            var7.setText(var8.toString());
            int var6 = (int)(var5 * 3.0F / 2.0F);
            var1 = var6;
            if (var6 < 1) {
                var1 = 1;
            }

            var2 = var4.findViewById(id.bar);
            var2.getLayoutParams().height = var1;
            var2.setLayoutParams(var2.getLayoutParams());
            return var4;
        }

        public int getItemsCount() {
            return this.mValues.length;
        }

        public void registerDataSetObserver(DataSetObserver var1) {
        }

        public void unregisterDataSetObserver(DataSetObserver var1) {
        }
    }
}
