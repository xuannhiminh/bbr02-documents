package office.file.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;
import com.artifex.solib.SODoc;
import com.artifex.solib.SOLinkData;
import com.artifex.solib.k;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import office.file.ui.editor.R.dimen;
import office.file.ui.editor.R.drawable;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;

class a implements OnDismissListener {
    private static office.file.ui.editor.a singleton;
    private final View mAnchor;
    private Button mCancelButton;
    private final Context mContext;
    private final SODoc mDoc;
    private final office.file.ui.editor.a.b mListener;
    private NUIPopupWindow popupWindow;

    a(Context var1, SODoc var2, View var3, office.file.ui.editor.a.b var4) {
        this.mContext = var1;
        this.mAnchor = var3;
        this.mDoc = var2;
        this.mListener = var4;
    }

    public static void b() {
        try {
            if (singleton != null) {
                singleton.c();
            }
        } catch (Exception var1) {
            singleton = null;
        }

    }

    private void d() {
        this.popupWindow.dismiss();
        singleton = null;
    }

    public void a() {
        office.file.ui.editor.a var1 = singleton;
        if (var1 != null) {
            var1.d();
        }

        singleton = this;
        View var4 = LayoutInflater.from(this.mContext).inflate(layout.sodk_editor_table_of_contents, (ViewGroup)null);
        ListView var2 = (ListView)var4.findViewById(id.List);
        final office.file.ui.editor.a.c var3 = new office.file.ui.editor.a.c(this.mContext);
        var2.setAdapter(var3);
        k.a(this.mDoc, new com.artifex.solib.k.a() {
            public void a(int var1, int var2, int var3x, String var4, String var5, float var6, float var7) {
                var3.a(new aa(var1, var2, var3x, var4, var5, var6, var7));
            }
        });
        var2.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> var1, View var2, int var3x, long var4) {
                aa var8 = (aa)var3.getItem(var3x);
                if (var8.h >= 0) {
                    RectF var6 = new RectF(var8.f, var8.g, var8.f + 1.0F, var8.g + 1.0F);
                    SOLinkData var7 = new SOLinkData(var8.h, var6);
                    if (Utilities.isPhoneDevice(mContext)) {
                        d();
                    }

                    mListener.a(var7);
                }

            }
        });
        Button var6 = (Button)var4.findViewById(id.cancel_button);
        this.mCancelButton = var6;
        var6.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                d();
            }
        });
        NUIPopupWindow var5 = new NUIPopupWindow(var4);
        this.popupWindow = var5;
        var5.setFocusable(true);
        this.popupWindow.setClippingEnabled(true);
        this.popupWindow.setOnDismissListener(this);
        this.c();
    }

    void c() {
        Point var1 = Utilities.getScreenSize(this.mContext);
        int var2;
        int var3;
        int var4;
        int var5;
        if (Utilities.isPhoneDevice(this.mContext)) {
            var2 = var1.x;
            var3 = var1.y;
            this.mCancelButton.setVisibility(View.VISIBLE);
            this.popupWindow.setBackgroundDrawable((Drawable)null);
            var4 = 0;
            var5 = 0;
        } else {
            var4 = (int)this.mContext.getResources().getDimension(dimen.sodk_editor_toc_offsetx);
            var5 = (int)this.mContext.getResources().getDimension(dimen.sodk_editor_toc_offsety);
            var2 = var1.x / 2;
            int var6 = var1.y / 2;
            this.mCancelButton.setVisibility(View.GONE);
            this.popupWindow.setBackgroundDrawable(viewx.core.content.a.a(this.mContext, drawable.sodk_editor_table_of_contents));
            var2 -= var4;
            var4 = var4;
            var3 = var6;
        }

        if (this.popupWindow.isShowing()) {
            this.popupWindow.update(var4, var5, var2, var3);
        } else {
            this.popupWindow.setWidth(var2);
            this.popupWindow.setHeight(var3);
            this.popupWindow.showAtLocation(this.mAnchor, 0, var4, var5);
        }

    }

    public void onDismiss() {
        this.d();
    }

    private static class aa {
        int a;
        int b;
        String c;
        String d;
        int e;
        float f;
        float g;
        int h;

        private aa(int var2, int var3, int var4, String var5, String var6, float var7, float var8) {
            this.a = var2;
            this.b = var3;
            this.c = var5;
            this.d = var6;
            this.f = var7;
            this.g = var8;
            this.h = var4;
        }
    }

    interface b {
        void a(SOLinkData var1);
    }

    private class c extends BaseAdapter {
        private ArrayList<aa> listEntries = new ArrayList();
        private Context mContext;
        private Map<Integer, aa> mapEntries = new HashMap();

        c(Context var2) {
            this.mContext = var2;
        }

        void a(aa var1) {
            this.mapEntries.put(var1.a, var1);
            int var2 = 0;

            for(aa var3 = var1; var3 != null && var3.b != 0; var3 = (aa)this.mapEntries.get(var3.b)) {
                ++var2;
            }

            var1.e = var2;
            this.listEntries.add(var1);
        }

        public int getCount() {
            return this.listEntries.size();
        }

        public Object getItem(int var1) {
            return this.listEntries.get(var1);
        }

        public long getItemId(int var1) {
            return 0L;
        }

        public View getView(int var1, View var2, ViewGroup var3) {
            aa var6 = (aa)this.listEntries.get(var1);
            View var4 = ((Activity)this.mContext).getLayoutInflater().inflate(layout.sodk_editor_toc_list_item, var3, false);
            SOTextView var7 = (SOTextView)var4.findViewById(id.toc_item);
            var7.setText(var6.c);
            int var5 = Utilities.convertDpToPixel(40.0F);
            var1 = var6.e;
            var7.setPadding(var7.getPaddingLeft() + var5 * var1, var7.getPaddingTop(), var7.getPaddingRight(), var7.getPaddingBottom());
            return var4;
        }
    }
}
