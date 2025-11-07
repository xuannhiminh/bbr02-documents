//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui.editor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow.OnDismissListener;
import com.artifex.solib.SODoc;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelViewLib;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import office.file.ui.editor.R.color;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;

public class EditNumberFormatPercentageLib {
    private static final int POPUP_OFFSET = 30;
    private static final String[] descriptions;
    private static final String[] formats;

    static {
        StringBuilder var0 = new StringBuilder();
        var0.append(String.format("%d", 0));
        var0.append("%");
        String var11 = var0.toString();
        StringBuilder var1 = new StringBuilder();
        var1.append(String.format("%.1f", 0.1D));
        var1.append("%");
        String var12 = var1.toString();
        StringBuilder var2 = new StringBuilder();
        var2.append(String.format("%.2f", 0.12D));
        var2.append("%");
        String var13 = var2.toString();
        StringBuilder var3 = new StringBuilder();
        var3.append(String.format("%.3f", 0.123D));
        var3.append("%");
        String var14 = var3.toString();
        StringBuilder var4 = new StringBuilder();
        var4.append(String.format("%.4f", 0.1234D));
        var4.append("%");
        String var15 = var4.toString();
        StringBuilder var5 = new StringBuilder();
        var5.append(String.format("%.5f", 0.12345D));
        var5.append("%");
        String var16 = var5.toString();
        StringBuilder var6 = new StringBuilder();
        var6.append(String.format("%.6f", 0.123456D));
        var6.append("%");
        String var17 = var6.toString();
        StringBuilder var7 = new StringBuilder();
        var7.append(String.format("%.7f", 0.1234567D));
        var7.append("%");
        String var18 = var7.toString();
        StringBuilder var8 = new StringBuilder();
        var8.append(String.format("%.8f", 0.12345678D));
        var8.append("%");
        String var9 = var8.toString();
        var8 = new StringBuilder();
        Double var10 = 0.123456789D;
        var8.append(String.format("%.9f", var10));
        var8.append("%");
        descriptions = new String[]{var11, var12, var13, var14, var15, var16, var17, var18, var9, var8.toString(), String.format("%.10f", var10)};
        formats = new String[]{"0%", "0.0%", "0.00%", "0.000%", "0.0000%", "0.00000%", "0.000000%", "0.0000000%", "0.00000000%", "0.000000000%", "0.0000000000%"};
    }

    public EditNumberFormatPercentageLib() {
    }

    public static void show(Context var0, View var1, final SODoc var2) {
        String var3 = var2.getSelectedCellFormat();
        View var4 = View.inflate(var0, layout.sodk_editor_number_format_percentage, (ViewGroup)null);
        final WheelViewLib var5 = (WheelViewLib)var4.findViewById(id.wheel);
        ArrayWheelAdapter var6 = new ArrayWheelAdapter(var0, descriptions);
        var6.setTextSize(18);
        var6.setTextColor(var0.getResources().getColor(color.sodk_editor_wheel_item_text_color));
        var5.setViewAdapter(var6);
        var5.setVisibleItems(5);
        var5.setCurrentItem(0);
        int var7 = 0;

        while(true) {
            String[] var8 = formats;
            if (var7 >= var8.length) {
                var5.addScrollingListener(new OnWheelScrollListener() {
                    @Override
                    public void onScrollingStarted(WheelViewLib wheel) {

                    }

                    @Override
                    public void onScrollingFinished(WheelViewLib wheel) {
                        var2.setSelectedCellFormat(EditNumberFormatPercentageLib.formats[wheel.getCurrentItem()]);
                    }
                });
                NUIPopupWindow var9 = new NUIPopupWindow(var4, -2, -2);
                var9.setFocusable(true);
                var9.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss() {
                        var5.clean();
                    }
                });
                var9.showAsDropDown(var1, 30, 30);
                return;
            }

            if (var8[var7].equals(var3)) {
                var5.setCurrentItem(0);
            }

            ++var7;
        }
    }
}
