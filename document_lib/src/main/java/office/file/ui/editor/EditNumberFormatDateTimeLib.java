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
import com.artifex.solib.a;
import java.util.HashMap;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelViewLib;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import office.file.ui.editor.R.color;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EditNumberFormatDateTimeLib {
    private static final int POPUP_OFFSET = 30;
    private static String[] countries;
    private static String[] countries_localized;
    private static HashMap<String, String[]> countryFormatMap;
    private static String[] formats;

    public EditNumberFormatDateTimeLib() {
    }

    private static String a(Context var0, String var1) {
        int var2 = var0.getResources().getIdentifier(var1, "string", var0.getPackageName());
        if (var2 != 0) {
            String var3 = var0.getString(var2);
            if (var3 != null && !var3.isEmpty()) {
                return var3;
            }
        }

        return var1;
    }

    private static void a(Context var0) {
        String var1 = a.c(var0, "date_time.json");
        if (var1 != null) {
            JSONException var10000;
            label82: {
                JSONObject var2;
                JSONArray var16;
                boolean var10001;
                try {
                    var2 = new JSONObject(var1);
                    var16 = var2.getJSONArray("countries");
                    countries = new String[var16.length()];
                    countries_localized = new String[var16.length()];
                } catch (JSONException var12) {
                    var10000 = var12;
                    var10001 = false;
                    break label82;
                }

                int var3 = 0;

                while(true) {
                    try {
                        if (var3 >= var16.length()) {
                            break;
                        }

                        String var4 = var16.getString(var3);
                        countries[var3] = var4;
                        countries_localized[var3] = a(var0, var4.replace(" ", "_"));
                    } catch (JSONException var11) {
                        var10000 = var11;
                        var10001 = false;
                        break label82;
                    }

                    ++var3;
                }

                JSONObject var17;
                try {
                    HashMap var13 = new HashMap();
                    countryFormatMap = var13;
                    var17 = var2.getJSONObject("formats");
                } catch (JSONException var10) {
                    var10000 = var10;
                    var10001 = false;
                    break label82;
                }

                var3 = 0;

                while(true) {
                    String[] var14;
                    JSONArray var18;
                    try {
                        if (var3 >= countries.length) {
                            break;
                        }

                        var18 = var17.getJSONArray(countries[var3]);
                        var14 = new String[var18.length()];
                    } catch (JSONException var8) {
                        var10000 = var8;
                        var10001 = false;
                        break label82;
                    }

                    int var5 = 0;

                    while(true) {
                        try {
                            if (var5 >= var18.length()) {
                                break;
                            }

                            var14[var5] = var18.getString(var5);
                        } catch (JSONException var9) {
                            var10000 = var9;
                            var10001 = false;
                            break label82;
                        }

                        ++var5;
                    }

                    countryFormatMap.put(countries[var3], c(var14));

                    ++var3;
                }

                formats = (String[])countryFormatMap.get(countries[0]);
                return;
            }

            JSONException var15 = var10000;
            var15.printStackTrace();
        }
    }

    private static void a(SODoc var0, WheelViewLib var1, WheelViewLib wheelViewLib) {
        String var6 = var0.getSelectedCellFormat();
        int var3 = 0;

        while(true) {
            String[] var4 = countries;
            if (var3 >= var4.length) {
                var1.setCurrentItem(0);
                wheelViewLib.setCurrentItem(0);
                return;
            }

            String var7 = var4[var3];
            var4 = (String[])countryFormatMap.get(var7);

            for(int var5 = 0; var5 < var4.length; ++var5) {
                if (var4[var5].equals(var6)) {
                    var1.setCurrentItem(var3);
                    ((ArrayWheelAdapter) wheelViewLib.getViewAdapter()).setItems(var4);
                    wheelViewLib.invalidateWheel(true);
                    wheelViewLib.setCurrentItem(var5);
                    return;
                }
            }

            ++var3;
        }
    }

    private static String[] c(String[] var0) {
        String[] var1 = new String[var0.length];

        for(int var2 = 0; var2 < var0.length; ++var2) {
            String var3 = var0[var2];
            int var4 = var3.lastIndexOf("]");
            String var5 = var3;
            if (var4 >= 0) {
                var5 = var3;
                if (var3.indexOf("[h]") != 0) {
                    var5 = var3.substring(var4 + 1);
                }
            }

            var4 = var5.indexOf(";@");
            var3 = var5;
            if (var4 >= 0) {
                var3 = var5.substring(0, var4);
            }

            var1[var2] = var3;
        }

        return var1;
    }

    public static void show(Context var0, View var1, final SODoc var2) {
        a(var0);
        View var3 = View.inflate(var0, layout.sodk_editor_number_format_datetime, (ViewGroup)null);
        final WheelViewLib var4 = (WheelViewLib)var3.findViewById(id.country_wheel);
        ArrayWheelAdapter var5 = new ArrayWheelAdapter(var0, countries_localized);
        var5.setTextSize(18);
        var5.setTextColor(var0.getResources().getColor(color.sodk_editor_wheel_item_text_color));
        var4.setViewAdapter(var5);
        var4.setVisibleItems(5);
        final WheelViewLib var8 = (WheelViewLib)var3.findViewById(id.format_wheel);
        final ArrayWheelAdapter var6 = new ArrayWheelAdapter(var0, formats);
        var6.setTextSize(18);
        var6.setTextColor(var0.getResources().getColor(color.sodk_editor_wheel_item_text_color));
        var8.setViewAdapter(var6);
        var8.setVisibleItems(5);
        a(var2, var4, var8);
        var4.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelViewLib wheel) {

            }

            @Override
            public void onScrollingFinished(WheelViewLib wheel) {
                String var2x = EditNumberFormatDateTimeLib.countries[var4.getCurrentItem()];
                EditNumberFormatDateTimeLib.formats = (String[]) EditNumberFormatDateTimeLib.countryFormatMap.get(var2x);
                var6.setItems(EditNumberFormatDateTimeLib.formats);
                var8.invalidateWheel(true);
                var8.setCurrentItem(0);
                var2.setSelectedCellFormat(EditNumberFormatDateTimeLib.formats[0]);
            }
        });

        var8.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelViewLib wheel) {

            }

            @Override
            public void onScrollingFinished(WheelViewLib wheel) {
                String var2x = EditNumberFormatDateTimeLib.formats[var8.getCurrentItem()];
                var2.setSelectedCellFormat(var2x);
            }
        });

        NUIPopupWindow var7 = new NUIPopupWindow(var3, -2, -2);
        var7.setFocusable(true);
        var7.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                var4.clean();
                var8.clean();
                EditNumberFormatDateTimeLib.countries = null;
                EditNumberFormatDateTimeLib.formats = null;
                EditNumberFormatDateTimeLib.countryFormatMap = null;
            }
        });
        var7.showAsDropDown(var1, 30, 30);
    }
}
