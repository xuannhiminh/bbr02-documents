//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import office.file.ui.editor.SOTextView;
import office.file.ui.editor.Utilities;
import office.file.ui.extension.FileExtKt;

public class f extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final LinkedList<g> mItems;
    private boolean mUseControls = true;

    public f(LayoutInflater var1, boolean var2) {
        this.mInflater = var1;
        this.mItems = new LinkedList();
        this.mUseControls = var2;
    }

    private int a(AppFile var1) {
        if(var1.d){
            return  R.drawable.ic_lib_folder;
        } else {
            if(var1.b().toLowerCase().endsWith(FileExtKt.getDOC()) || var1.b().toLowerCase().endsWith(FileExtKt.getDOCX())){
                return R.drawable.png_doc;
            } else if(var1.b().toLowerCase().endsWith(FileExtKt.getPPT()) || var1.b().toLowerCase().endsWith(FileExtKt.getPPTX())){
                return R.drawable.png_ppt;
            } else if(var1.b().toLowerCase().endsWith(FileExtKt.getXLS()) || var1.b().toLowerCase().endsWith(FileExtKt.getXLSX())){
                return R.drawable.png_excel;
            } else if(var1.b().toLowerCase().endsWith(FileExtKt.getPDF())){
                return R.drawable.png_pdf;
            } else {
                return R.drawable.png_txt;
            }
        }
//        return var1.d ? R.drawable.ic_folder_storage : var1.j();
    }

    public void a() {
        this.mItems.clear();
        this.notifyDataSetChanged();
    }

    public void a(g var1) {
        this.mItems.add(var1);
        this.notifyDataSetChanged();
    }

    public int getCount() {
        return this.mItems.size();
    }

    public Object getItem(int var1) {
        try {
            Object var2 = this.mItems.get(var1);
            return var2;
        } catch (Exception var3) {
            return null;
        }
    }

    public long getItemId(int var1) {
        return 0L;
    }

    public View getView(int var1, View var2, ViewGroup var3) {
        View var6 = var2;
        if (var2 == null) {
            var6 = this.mInflater.inflate(IdController.getIntlayout("sodk_picker_entry"), (ViewGroup)null);
        }

        ChooseDocListItemView var4 = (ChooseDocListItemView)var6;
        var4.a();
        var4.setUseControls(this.mUseControls);
        g var5 = (g)this.mItems.get(var1);
        var5.b = var1;
        ((ImageView)var6.findViewById(IdController.getIntid("icon"))).setImageResource(this.a(var5.a));
        SOTextView textView = var6.findViewById(IdController.getIntid("name"));
        textView.setText(var5.a.b);
//        Utilities.setFilenameText((SOTextView)var6.findViewById(IdController.getIntid("name")), var5.a.b);
        var6.setTag(var5);
        var6.findViewById(IdController.getIntid("control_delete")).setTag(var5);
        var6.findViewById(IdController.getIntid("control_rename")).setTag(var5);
        var6.findViewById(IdController.getIntid("control_copy")).setTag(var5);
        var6.findViewById(IdController.getIntid("control_share")).setTag(var5);
        var6.findViewById(IdController.getIntid("control_logout")).setTag(var5);
        return var6;
    }
}
       