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
import com.artifex.solib.a;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelViewLib;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import office.file.ui.editor.R.color;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EditNumberFormatCurrencyLib {
    private static final int POPUP_OFFSET = 30;
    private static WheelViewLib curWheel;
    private static String[] cur_descriptions;
    private static String[] cur_formats;
    private static SODoc doc;
    private static WheelViewLib negWheel;
    private static final String[] neg_descriptions;
    private static final String[] neg_formats;
    private static CheckBox twoPlacesCheck;

    static {
        Double var0 = -1234.1D;
        String var1 = String.format("%.2f", var0);
        Double var2 = 1234.1D;
        neg_descriptions = new String[]{var1, String.format("%.2f (red)", var2), String.format("%.2f (red)", var0), String.format("(%.2f)", var2), String.format("(%.2f) (red)", var2)};
        neg_formats = new String[]{"DEC", "DEC;[Red]DEC", "DEC;[Red]\\-DEC", "DEC_);(DEC)", "DEC_);[Red](DEC)"};
    }

    public EditNumberFormatCurrencyLib() {
    }

    private static void a(Context var0) {
        String var1 = a.c(var0, "currencies.json");
        if (var1 != null) {
            JSONException var10000;
            label59: {
                JSONArray var3;
                boolean var10001;
                try {
                    JSONObject var2 = new JSONObject(var1);
                    var3 = var2.getJSONArray("currencies");
                    cur_descriptions = new String[var3.length()];
                    cur_formats = new String[var3.length()];
                } catch (JSONException var12) {
                    var10000 = var12;
                    var10001 = false;
                    break label59;
                }

                int var4 = 0;

                while(true) {
                    String var5;
                    String var6;
                    String var15;
                    try {
                        if (var4 >= var3.length()) {
                            return;
                        }

                        JSONObject var14 = var3.getJSONObject(var4);
                        var15 = var14.getString("description");
                        var5 = var14.getString("format");
                        var6 = var14.getString("token");
                    } catch (JSONException var10) {
                        var10000 = var10;
                        var10001 = false;
                        break;
                    }

                    var1 = var15;
                    if (var6 != null) {
                        label61: {
                            var1 = var15;

                            int var7;
                            if (var6.isEmpty()) {
                                break label61;
                            }

                            var1 = var0.getPackageName();
                            var7 = var0.getResources().getIdentifier(var6, "string", var1);

                            var1 = var15;
                            if (var7 != 0) {
                                var1 = var0.getString(var7);
                            }
                        }
                    }

                    cur_descriptions[var4] = var1;
                    cur_formats[var4] = var5;

                    ++var4;
                }
            }

            JSONException var13 = var10000;
            var13.printStackTrace();
        }
    }

    private static void c() {
        String var0 = doc.getSelectedCellFormat();
        twoPlacesCheck.setChecked(var0.contains("#,##0.00"));
        var0 = var0.replace("#,##0.00", "#,##0");

        for(int var1 = 0; var1 < cur_formats.length; ++var1) {
            int var2 = 0;

            while(true) {
                String[] var3 = neg_formats;
                if (var2 >= var3.length) {
                    break;
                }

                if (var3[var2].replace("DEC", cur_formats[var1]).equals(var0)) {
                    curWheel.setCurrentItem(var1);
                    negWheel.setCurrentItem(var2);
                    return;
                }

                ++var2;
            }
        }

    }

    private static void d() {
        String var0 = cur_formats[curWheel.getCurrentItem()];
        String var1 = var0;
        if (twoPlacesCheck.isChecked()) {
            var1 = var0.replace("#,##0", "#,##0.00");
        }

        var1 = neg_formats[negWheel.getCurrentItem()].replace("DEC", var1);
        doc.setSelectedCellFormat(var1);
    }

    private static void e() {
        curWheel.clean();
        negWheel.clean();
        curWheel = null;
        negWheel = null;
        twoPlacesCheck = null;
        doc = null;
        cur_descriptions = null;
        cur_formats = null;
    }

    public static void show(Context var0, View var1, SODoc var2) {
        a(var0);
        View var3 = View.inflate(var0, layout.sodk_editor_number_format_currency, (ViewGroup)null);
        doc = var2;
        curWheel = (WheelViewLib)var3.findViewById(id.left_wheel);
        ArrayWheelAdapter var5 = new ArrayWheelAdapter(var0, cur_descriptions);
        var5.setTextSize(18);
        var5.setTextColor(var0.getResources().getColor(color.sodk_editor_wheel_item_text_color));
        curWheel.setViewAdapter(var5);
        curWheel.setVisibleItems(5);
        negWheel = (WheelViewLib)var3.findViewById(id.right_wheel);
        ArrayWheelAdapter var6 = new ArrayWheelAdapter(var0, neg_descriptions);
        var6.setTextSize(18);
        var6.setTextColor(var0.getResources().getColor(color.sodk_editor_wheel_item_text_color));
        negWheel.setViewAdapter(var6);
        negWheel.setVisibleItems(5);
        twoPlacesCheck = (CheckBox)var3.findViewById(id.decimal_places_checkbox);
        c();
        negWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelViewLib wheel) {

            }

            @Override
            public void onScrollingFinished(WheelViewLib wheel) {
                EditNumberFormatCurrencyLib.d();
            }
        });

        curWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelViewLib wheel) {

            }

            @Override
            public void onScrollingFinished(WheelViewLib wheel) {
                EditNumberFormatCurrencyLib.d();
            }
        });

        twoPlacesCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton var1, boolean var2) {
                EditNumberFormatCurrencyLib.d();
            }
        });
        NUIPopupWindow var4 = new NUIPopupWindow(var3, -2, -2);
        var4.setFocusable(true);
        var4.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                EditNumberFormatCurrencyLib.e();
            }
        });
        var4.showAsDropDown(var1, 30, 30);
    }
}
