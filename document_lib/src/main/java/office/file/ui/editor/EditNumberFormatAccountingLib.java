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

public class EditNumberFormatAccountingLib {
    private static final int POPUP_OFFSET = 30;
    private static String[] cur_descriptions;
    private static String[] cur_formats;

    public EditNumberFormatAccountingLib() {
    }

    private static void a(Context var0) {
        String var1 = a.c(var0, "currencies.json");
        if (var1 != null) {
            JSONException var10000;
            label59: {
                JSONObject var2;
                JSONArray var3;
                boolean var10001;
                try {
                    var2 = new JSONObject(var1);
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
                    try {
                        if (var4 >= var3.length()) {
                            return;
                        }

                        var2 = var3.getJSONObject(var4);
                        var1 = var2.getString("description");
                        var5 = var2.getString("format");
                        var6 = var2.getString("token");
                    } catch (JSONException var10) {
                        var10000 = var10;
                        var10001 = false;
                        break;
                    }

                    String var14 = var1;
                    if (var6 != null) {
                        label61: {
                            var14 = var1;

                            int var7;
                            if (var6.isEmpty()) {
                                break label61;
                            }

                            var14 = var0.getPackageName();
                            var7 = var0.getResources().getIdentifier(var6, "string", var14);

                            var14 = var1;
                            if (var7 != 0) {
                                var14 = var0.getString(var7);
                            }
                        }
                    }

                    cur_descriptions[var4] = var14;
                    cur_formats[var4] = var5;

                    ++var4;
                }
            }

            JSONException var13 = var10000;
            var13.printStackTrace();
        }
    }

    private static void b(SODoc var0, WheelViewLib var1, CheckBox var2) {
        String var4 = var0.getSelectedCellFormat();
        var2.setChecked(var4.contains("#,##0.00"));
        String var6 = var4.replace("#,##0.00", "#,##0");
        int var3 = 0;

        while(true) {
            String[] var5 = cur_formats;
            if (var3 >= var5.length) {
                return;
            }

            if (var6.equals(var5[var3])) {
                var1.setCurrentItem(var3);
                return;
            }

            ++var3;
        }
    }

    private static void c(SODoc var0, WheelViewLib var1, CheckBox var2) {
        String var3 = cur_formats[var1.getCurrentItem()];
        String var4 = var3;
        if (var2.isChecked()) {
            var4 = var3.replace("#,##0", "#,##0.00");
        }

        var0.setSelectedCellFormat(var4);
    }

    public static void show(Context var0, View var1, final SODoc var2) {
        a(var0);
        View var3 = View.inflate(var0, layout.sodk_editor_number_format_accounting, (ViewGroup)null);
        final WheelViewLib var4 = (WheelViewLib)var3.findViewById(id.cur_wheel);
        ArrayWheelAdapter var5 = new ArrayWheelAdapter(var0, cur_descriptions);
        var5.setTextSize(18);
        var5.setTextColor(var0.getResources().getColor(color.sodk_editor_wheel_item_text_color));
        var4.setViewAdapter(var5);
        var4.setVisibleItems(5);
        final CheckBox var6 = (CheckBox)var3.findViewById(id.decimal_places_checkbox);
        b(var2, var4, var6);
        var4.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelViewLib wheel) {

            }

            @Override
            public void onScrollingFinished(WheelViewLib wheel) {
                EditNumberFormatAccountingLib.c(var2, var4, var6);
            }
        });

        var6.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton var1, boolean var2x) {
                EditNumberFormatAccountingLib.c(var2, var4, var6);
            }
        });
        NUIPopupWindow var7 = new NUIPopupWindow(var3, -2, -2);
        var7.setFocusable(true);
        var7.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                var4.clean();
                EditNumberFormatAccountingLib.cur_descriptions = null;
                EditNumberFormatAccountingLib.cur_formats = null;
            }
        });
        var7.showAsDropDown(var1, 30, 30);
    }
}
