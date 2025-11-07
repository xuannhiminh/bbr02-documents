//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui.editor;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import com.artifex.solib.SODoc;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelViewLib;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import office.file.ui.editor.R.color;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.R.string;
import office.file.ui.editor.R.style;
import viewx.core.content.a;

public class EditNumberFormatCustomLib {
    private static final int POPUP_OFFSET = 30;
    private static String[] descriptions;
    private static LinearLayout editButton;
    private static SOTextView editIndicator;
    private static String[] fixed_descriptions;
    private static final String[] fixed_formats;
    private static WheelViewLib formatWheel;
    private static String[] formats;
    private static SODoc theDocument;
    private static ArrayWheelAdapter<String> wheelAdapter;

    static {
        String[] var0 = new String[]{"General", "0", "[$£-809]#,##0", "yyyy-mm-dd;@", "# ?/?"};
        fixed_formats = var0;
        formats = (String[])var0.clone();
        editButton = null;
        editIndicator = null;
        wheelAdapter = null;
        formatWheel = null;
    }

    public EditNumberFormatCustomLib() {
    }

    private static void a(Context var0, SODoc var1, WheelViewLib var2) {
        String var3 = var1.getSelectedCellFormat();
        int var4 = 0;

        while(true) {
            String[] var5 = formats;
            if (var4 >= var5.length) {
                descriptions = b(descriptions, var3);
                formats = b(formats, var3);
                wheelAdapter.setItems(descriptions);
                formatWheel.invalidateWheel(true);
                formatWheel.setCurrentItem(formats.length - 1);
                b(var0, true, editButton, editIndicator);
                b(theDocument, formatWheel);
                return;
            }

            if (var3.equals(var5[var4])) {
                var2.setCurrentItem(var4);
                if (var4 + 1 > fixed_formats.length) {
                    b(var0, true, editButton, editIndicator);
                }

                return;
            }

            ++var4;
        }
    }

    private static int b(String var0) {
        int var1 = 0;

        while(true) {
            String[] var2 = formats;
            if (var1 >= var2.length) {
                return -1;
            }

            if (var0.equals(var2[var1])) {
                return var1;
            }

            ++var1;
        }
    }

    private static void b(final Context var0, String var1, String var2, final EditNumberFormatCustomLib.TextListener var3) {
        Builder var4 = new Builder(var0, style.sodk_editor_alert_dialog_style);
        var4.setTitle(var1);
        View var5 = LayoutInflater.from(var0).inflate(layout.sodk_editor_number_format_prompt, (ViewGroup)null);
        final SOEditText var6 = (SOEditText)var5.findViewById(id.editTextDialogUserInput);
        var6.setText(var2);
        var4.setView(var5);
        var4.setPositiveButton(string.sodk_editor_OK, new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {
                Utilities.hideKeyboard(var0, var6);
                var3.setText(var6.getText().toString());
            }
        });
        var4.setNegativeButton(string.sodk_editor_cancel, new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {
                Utilities.hideKeyboard(var0, var6);
                var1.cancel();
            }
        });
        var4.show();
    }

    private static void b(Context var0, boolean var1, LinearLayout var2, SOTextView var3) {
        var2.setEnabled(var1);
        int var4;
        if (var1) {
            var4 = color.sodk_editor_xls_custom_num_edit_enabled_color;
        } else {
            var4 = color.sodk_editor_xls_custom_num_edit_disabled_color;
        }

        var3.setTextColor(a.c(var0, var4));
    }

    private static void b(SODoc var0, WheelViewLib var1) {
        var0.setSelectedCellFormat(formats[var1.getCurrentItem()]);
    }

    private static String[] b(String[] var0, String var1) {
        String[] var2 = new String[var0.length + 1];
        System.arraycopy(var0, 0, var2, 0, var0.length);
        var2[var0.length] = var1;
        return var2;
    }

    public static void show(final Context var0, View var1, final SODoc var2) {
        String[] var3 = new String[]{var0.getString(string.sodk_editor_format_category_general), var0.getString(string.sodk_editor_format_category_number), var0.getString(string.sodk_editor_format_category_currency), var0.getString(string.sodk_editor_format_category_date_and_time), var0.getString(string.sodk_editor_format_category_fraction)};
        fixed_descriptions = var3;
        descriptions = (String[])var3.clone();
        theDocument = var2;
        View var4 = View.inflate(var0, layout.sodk_editor_number_format_custom, (ViewGroup)null);
        formatWheel = (WheelViewLib)var4.findViewById(id.custom_wheel);
        ArrayWheelAdapter var6 = new ArrayWheelAdapter(var0, descriptions);
        wheelAdapter = var6;
        var6.setTextSize(18);
        wheelAdapter.setTextColor(var0.getResources().getColor(color.sodk_editor_wheel_item_text_color));
        formatWheel.setViewAdapter(wheelAdapter);
        formatWheel.setVisibleItems(5);
        editButton = (LinearLayout)var4.findViewById(id.edit_function_wrapper);
        editIndicator = (SOTextView)var4.findViewById(id.edit_function_indicator);
        ((LinearLayout)var4.findViewById(id.create_new_wrapper)).setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View var1) {
                Context var2x = var0;
                EditNumberFormatCustomLib.b(var2x, var2x.getString(string.sodk_editor_create_new), "", new EditNumberFormatCustomLib.TextListener() {
                    public void setText(String var1) {
                        if (var1 != null && !var1.isEmpty()) {
                            int var2x = EditNumberFormatCustomLib.b(var1);
                            if (var2x >= 0) {
                                EditNumberFormatCustomLib.formatWheel.setCurrentItem(var2x);
                            } else {
                                EditNumberFormatCustomLib.descriptions = EditNumberFormatCustomLib.b(EditNumberFormatCustomLib.descriptions, var1);
                                EditNumberFormatCustomLib.formats = EditNumberFormatCustomLib.b(EditNumberFormatCustomLib.formats, var1);
                                EditNumberFormatCustomLib.wheelAdapter.setItems(EditNumberFormatCustomLib.descriptions);
                                EditNumberFormatCustomLib.formatWheel.invalidateWheel(true);
                                EditNumberFormatCustomLib.formatWheel.setCurrentItem(EditNumberFormatCustomLib.formats.length - 1);
                                EditNumberFormatCustomLib.b(var0, true, EditNumberFormatCustomLib.editButton, EditNumberFormatCustomLib.editIndicator);
                            }

                            EditNumberFormatCustomLib.b(var2, EditNumberFormatCustomLib.formatWheel);
                        }

                    }
                });
            }
        });
        editButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View var1) {
                final int var2x = EditNumberFormatCustomLib.formatWheel.getCurrentItem();
                if (var2x >= EditNumberFormatCustomLib.fixed_descriptions.length) {
                    String var3 = EditNumberFormatCustomLib.descriptions[var2x];
                    Context var4 = var0;
                    EditNumberFormatCustomLib.b(var4, var4.getString(string.sodk_editor_edit_function), var3, new EditNumberFormatCustomLib.TextListener() {
                        public void setText(String var1) {
                            if (var1 != null && !var1.isEmpty()) {
                                int var2xx = EditNumberFormatCustomLib.b(var1);
                                WheelViewLib var3;
                                if (var2xx >= 0) {
                                    var3 = EditNumberFormatCustomLib.formatWheel;
                                } else {
                                    EditNumberFormatCustomLib.descriptions[var2x] = var1;
                                    EditNumberFormatCustomLib.formats[var2x] = var1;
                                    EditNumberFormatCustomLib.wheelAdapter.setItems(EditNumberFormatCustomLib.descriptions);
                                    EditNumberFormatCustomLib.formatWheel.invalidateWheel(true);
                                    var3 = EditNumberFormatCustomLib.formatWheel;
                                    var2xx = var2x;
                                }

                                var3.setCurrentItem(var2xx);
                                EditNumberFormatCustomLib.b(var2, EditNumberFormatCustomLib.formatWheel);
                            }

                        }
                    });
                }

            }
        });
        b(var0, false, editButton, editIndicator);
        a(var0, var2, formatWheel);
        formatWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelViewLib wheel) {

            }

            @Override
            public void onScrollingFinished(WheelViewLib wheel) {
                Context var3 = var0;
                boolean var2x;
                if (EditNumberFormatCustomLib.formatWheel.getCurrentItem() >= EditNumberFormatCustomLib.fixed_descriptions.length) {
                    var2x = true;
                } else {
                    var2x = false;
                }

                EditNumberFormatCustomLib.b(var3, var2x, EditNumberFormatCustomLib.editButton, EditNumberFormatCustomLib.editIndicator);
                EditNumberFormatCustomLib.b(var2, EditNumberFormatCustomLib.formatWheel);
            }
        });

        NUIPopupWindow var5 = new NUIPopupWindow(var4, -2, -2);
        var5.setFocusable(true);
        var5.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                EditNumberFormatCustomLib.formatWheel.clean();
            }
        });
        var5.showAsDropDown(var1, 30, 30);
    }

    public interface TextListener {
        void setText(String var1);
    }
}
