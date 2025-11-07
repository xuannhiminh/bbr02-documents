//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui.editor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupWindow.OnDismissListener;
import com.artifex.solib.SODoc;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelViewLib;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import office.file.ui.editor.R.color;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;

class EditNumberFormatNumberLib {
    private static final int POPUP_OFFSET = 30;
    private static SODoc doc;
    private static WheelViewLib leftWheel;
    private static final String[] left_descriptions;
    private static final String[] left_formats;
    private static WheelViewLib rightWheel;
    private static final String[] right_descriptions;
    private static final String[] right_formats;
    private static CheckBox scientificCheck;
    private static CheckBox thousandsSepCheck;

    static {
        Double var0 = -1234.1D;
        String var1 = String.format("%.2f", var0);
        Double var2 = 1234.1D;
        left_descriptions = new String[]{var1, String.format("%.2f (red)", var2), String.format("%.2f (red)", var0), String.format("(%.2f)", var2), String.format("(%.2f) (red)", var2)};
        left_formats = new String[]{"DEC", "DEC;[Red]DEC", "DEC;[Red]\\-DEC", "DEC_);(DEC)", "DEC_);[Red](DEC)"};
        String var11 = String.format("%d", 0);
        String var3 = String.format("%.1f", 0.1D);
        String var10 = String.format("%.2f", 0.12D);
        String var4 = String.format("%.3f", 0.123D);
        String var5 = String.format("%.4f", 0.1234D);
        var1 = String.format("%.5f", 0.12345D);
        String var6 = String.format("%.6f", 0.123456D);
        String var7 = String.format("%.7f", 0.1234567D);
        String var8 = String.format("%.8f", 0.12345678D);
        Double var9 = 0.123456789D;
        right_descriptions = new String[]{var11, var3, var10, var4, var5, var1, var6, var7, var8, String.format("%.9f", var9), String.format("%.10f", var9)};
        right_formats = new String[]{"0", "0.0", "0.00", "0.000", "0.0000", "0.00000", "0.000000", "0.0000000", "0.00000000", "0.000000000", "0.0000000000"};
    }

    public EditNumberFormatNumberLib() {
    }

    private static void c() {
        String var0 = doc.getSelectedCellFormat();
        thousandsSepCheck.setChecked(var0.contains("#,##"));
        var0 = var0.replace("#,##", "");
        scientificCheck.setChecked(var0.contains("E+00"));
        var0 = var0.replace("E+00", "");

        for(int var1 = 0; var1 < right_formats.length; ++var1) {
            int var2 = 0;

            while(true) {
                String[] var3 = left_formats;
                if (var2 >= var3.length) {
                    break;
                }

                if (var3[var2].replace("DEC", right_formats[var1]).equals(var0)) {
                    leftWheel.setCurrentItem(var2);
                    rightWheel.setCurrentItem(var1);
                    return;
                }

                ++var2;
            }
        }

    }

    private static void d() {
        String var0 = right_formats[rightWheel.getCurrentItem()];
        String var1 = var0;
        if (thousandsSepCheck.isChecked()) {
            StringBuilder var3 = new StringBuilder();
            var3.append(var0);
            var3.append("#,##");
            var1 = var3.toString();
        }

        var0 = var1;
        if (scientificCheck.isChecked()) {
            StringBuilder var2 = new StringBuilder();
            var2.append(var1);
            var2.append("E+00");
            var0 = var2.toString();
        }

        var1 = left_formats[leftWheel.getCurrentItem()].replace("DEC", var0);
        doc.setSelectedCellFormat(var1);
    }

    private static void e() {
        leftWheel.clean();
        rightWheel.clean();
        leftWheel = null;
        rightWheel = null;
        thousandsSepCheck = null;
        scientificCheck = null;
        doc = null;
    }

    public static void show(Context var0, View var1, SODoc var2) {
        View var3 = View.inflate(var0, layout.sodk_editor_number_format_number, (ViewGroup)null);
        doc = var2;
        leftWheel = (WheelViewLib)var3.findViewById(id.left_wheel);
        ArrayWheelAdapter var5 = new ArrayWheelAdapter(var0, left_descriptions);
        var5.setTextSize(18);
        var5.setTextColor(var0.getResources().getColor(color.sodk_editor_wheel_item_text_color));
        leftWheel.setViewAdapter(var5);
        leftWheel.setVisibleItems(5);
        rightWheel = (WheelViewLib)var3.findViewById(id.right_wheel);
        ArrayWheelAdapter var6 = new ArrayWheelAdapter(var0, right_descriptions);
        var6.setTextSize(18);
        var6.setTextColor(var0.getResources().getColor(color.sodk_editor_wheel_item_text_color));
        rightWheel.setViewAdapter(var6);
        rightWheel.setVisibleItems(5);
        thousandsSepCheck = (CheckBox)var3.findViewById(id.thousand_sep_checkbox);
        scientificCheck = (CheckBox)var3.findViewById(id.scientific_checkbox);
        c();
        rightWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelViewLib wheel) {

            }

            @Override
            public void onScrollingFinished(WheelViewLib wheel) {
                EditNumberFormatNumberLib.d();
            }
        });

        leftWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelViewLib wheel) {

            }

            @Override
            public void onScrollingFinished(WheelViewLib wheel) {
                EditNumberFormatNumberLib.d();
            }
        });

        scientificCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton var1, boolean var2) {
                EditNumberFormatNumberLib.d();
            }
        });
        thousandsSepCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton var1, boolean var2) {
                EditNumberFormatNumberLib.d();
            }
        });
        NUIPopupWindow var4 = new NUIPopupWindow(var3, -2, -2);
        var4.setFocusable(true);
        var4.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                EditNumberFormatNumberLib.e();
            }
        });
        var4.showAsDropDown(var1, 30, 30);
    }
}
