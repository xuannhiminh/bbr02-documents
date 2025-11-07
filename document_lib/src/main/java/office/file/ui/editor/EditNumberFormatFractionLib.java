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
import office.file.ui.editor.R.string;

public class EditNumberFormatFractionLib {
    private static final int POPUP_OFFSET = 30;
    private static String[] descriptions;
    private static final String[] formats = new String[]{"# ?/?", "# ??/??", "#\\ ???/???", "#\\ ?/2", "#\\ ?/4", "#\\ ?/8", "#\\ ?/16", "#\\ ?/10", "#\\ ?/100"};

    public EditNumberFormatFractionLib() {
    }

    public static void show(Context var0, View var1, final SODoc var2) {
        descriptions = new String[]{var0.getString(string.sodk_editor_up_to_one_digit), var0.getString(string.sodk_editor_up_to_two_digits), var0.getString(string.sodk_editor_up_to_three_digits), var0.getString(string.sodk_editor_as_halves), var0.getString(string.sodk_editor_as_quarters), var0.getString(string.sodk_editor_as_eighths), var0.getString(string.sodk_editor_as_sixteenths), var0.getString(string.sodk_editor_as_tenths), var0.getString(string.sodk_editor_as_hundredths)};
        String var3 = var2.getSelectedCellFormat();
        View var4 = View.inflate(var0, layout.sodk_editor_number_format_fractions, (ViewGroup)null);
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
                        var2.setSelectedCellFormat(EditNumberFormatFractionLib.formats[wheel.getCurrentItem()]);
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
