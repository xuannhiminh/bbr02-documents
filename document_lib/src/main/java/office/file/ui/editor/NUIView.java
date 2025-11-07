package office.file.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.artifex.solib.a;
import com.artifex.solib.k;

public class NUIView extends FrameLayout {
    public NUIDocView mDocView;
    private NUIView.OnDoneListener mDoneListener = null;
    private String filePath;

    public NUIView(Context var1) {
        super(var1);
        this.a(var1);
    }

    public NUIView(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.a(var1);
    }

    public NUIView(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.a(var1);
    }

    private void a(Context var1) {
    }

    private void a(Uri var1, String var2) {
        var2 = a.a(this.getContext(), var1, var2);
        StringBuilder var3 = new StringBuilder();
        var3.append("SomeFileName.");
        var3.append(var2);
        this.a(var3.toString());
    }

    private void a(String var1) {
        Object var2;
        if (a.c(var1, "pdf")) {
            var2 = new NUIDocViewPdf(this.getContext());
        } else if (a.c(var1, "svg")) {
            var2 = new NUIDocViewMuPdf(this.getContext());
        } else if (a.c(var1, "epub")) {
            var2 = new NUIDocViewMuPdf(this.getContext());
        } else if (a.c(var1, "xps")) {
            var2 = new NUIDocViewMuPdf(this.getContext());
        } else if (a.c(var1, "fb2")) {
            var2 = new NUIDocViewMuPdf(this.getContext());
        } else if (a.c(var1, "xhtml")) {
            var2 = new NUIDocViewMuPdf(this.getContext());
        } else if (a.c(var1, "cbz")) {
            var2 = new NUIDocViewMuPdf(this.getContext());
        } else if (k.c((Activity) this.getContext(), var1)) {
            var2 = new NUIDocViewXls(this.getContext());
        } else if (k.d((Activity) this.getContext(), var1)) {
            var2 = new NUIDocViewPpt(this.getContext());
        } else if (k.e((Activity) this.getContext(), var1)) {
            var2 = new NUIDocViewPdf(this.getContext());
        } else if (k.f((Activity) this.getContext(), var1)) {
            var2 = new NUIDocViewDoc(this.getContext());
        } else {
            var2 = new NUIDocViewOther(this.getContext());
        }

        this.mDocView = (NUIDocView) var2;
        this.mDocView.setFilePath(filePath);
    }

    public boolean doKeyDown(int var1, KeyEvent var2) {
        NUIDocView var3 = this.mDocView;
        return var3 != null ? var3.doKeyDown(var1, var2) : false;
    }

    public void endDocSession(boolean var1) {
        NUIDocView var2 = this.mDocView;
        if (var2 != null) {
            var2.endDocSession(var1);
        }

    }

    public boolean isDocModified() {
        NUIDocView var1 = this.mDocView;
        return var1 != null && var1.documentHasBeenModified();
    }

    public void onActivityResult(int var1, int var2, Intent var3) {
        NUIDocView var4 = this.mDocView;
        if (var4 != null) {
            var4.onActivityResult(var1, var2, var3);
        }

    }

    public void onBackPressed(Boolean isConfirmBack) {
        NUIDocView var1 = this.mDocView;
        if (var1 != null) {
            var1.onBackPressed(isConfirmBack);
        }

    }

    public void onConfigurationChange(Configuration var1) {
        this.mDocView.onConfigurationChange(var1);
    }

    public void onDestroy() {
        if(this.mDocView != null) {
            this.mDocView.onDestroy();
        }
    }

    public void onPause() {
        NUIDocView var1 = this.mDocView;
        if (var1 != null) {
            var1.onPause();
        }

        Utilities.hideKeyboard(this.getContext());
    }

    public void onResume() {
        NUIDocView var1 = this.mDocView;
        if (var1 != null) {
            var1.onResume();
        }

    }

    public void releaseBitmaps() {
        NUIDocView var1 = this.mDocView;
        if (var1 != null) {
            var1.releaseBitmaps();
        }

    }

    public void setConfigurableButtons() {
        NUIDocView var1 = this.mDocView;
        if (var1 != null) {
            var1.setConfigurableButtons();
        }

    }

    public void setOnDoneListener(NUIView.OnDoneListener var1) {
        this.mDoneListener = var1;
    }

    public void start(Uri var1, boolean var2, int var3, String var4, String var5,String filePath) {
        this.filePath = filePath;
        this.a(var1, var5);
        this.addView(this.mDocView);
        this.mDocView.start(var1, var2, var3, var4, this.mDoneListener);
    }

    public void start(SODocSession var1, int var2, String var3,String filePath) {
        this.filePath = filePath;
        this.a(var1.getUserPath());
        this.addView(this.mDocView);
        this.mDocView.start(var1, var2, var3, this.mDoneListener);
    }

    public void start(SOFileState var1, int var2,String filePath) {
        this.filePath = filePath;
        this.a(var1.getOpenedPath());
        this.addView(this.mDocView);
        this.mDocView.start(var1, var2, this.mDoneListener);
    }


    public interface OnDoneListener {
        void done();
    }
}
