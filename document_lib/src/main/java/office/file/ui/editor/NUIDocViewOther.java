package office.file.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import office.file.ui.editor.R.color;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.R.string;

public class NUIDocViewOther extends NUIDocView {
    public NUIDocViewOther(Context var1) {
        super(var1);
        this.a(var1);
    }

    public NUIDocViewOther(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.a(var1);
    }

    public NUIDocViewOther(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.a(var1);
    }

    private void a(Context var1) {
    }

    private String getFileExtension() {
        if (this.mStartUri != null) {
            return com.artifex.solib.a.a(this.getContext(), this.mStartUri);
        } else {
            String var1;
            if (this.mSession != null) {
                var1 = this.mSession.getUserPath();
            } else if (this.mState != null) {
                var1 = this.mState.getInternalPath();
            } else {
                var1 = "";
            }

            return com.artifex.solib.a.h(var1);
        }
    }

    protected void afterFirstLayoutComplete() {
        super.afterFirstLayoutComplete();
        String var1 = this.getFileExtension();
        if (var1 == null || var1.compareToIgnoreCase("txt") != 0 && var1.compareToIgnoreCase("csv") != 0) {
            this.findViewById(id.first_page_button).setVisibility(View.GONE);
            this.findViewById(id.last_page_button).setVisibility(View.GONE);
            this.findViewById(id.reflow_button).setVisibility(View.GONE);
//            this.findViewById(id.divider_1).setVisibility(View.GONE);
//            this.findViewById(id.divider_2).setVisibility(View.GONE);
        }

    }

    protected PageAdapter createAdapter() {
        return new PageAdapter(this.activity(), this, 1);
    }

    protected void createEditButtons() {
    }

    protected void createEditButtons2() {
    }

    protected void createInputView() {
    }

    protected void createInsertButtons() {
    }

    protected DocView createMainView(Activity var1) {
        return new DocView(var1);
    }

    protected void createReviewButtons() {
    }

    protected void enforceInitialShowUI(View var1) {
        boolean var2 = this.mConfigOptions.a();
        byte var3 = 0;
        byte var4;

        var1 = this.findViewById(id.footer);
        if (var2) {
            var4 = 0;
        } else {
            var4 = 8;
        }

//        var1.setVisibility(var4);
        var1 = this.findViewById(id.header);
        if (var2) {
            var4 = var3;
        } else {
            var4 = 8;
        }

//        var1.setVisibility(var4);
        this.mFullscreen = var2 ^ true;
    }

    public int getBorderColor() {
        return viewx.core.content.a.c(this.getContext(), color.sodk_editor_selected_page_border_color);
    }

    protected int getLayoutId() {
        return layout.sodk_editor_other_document;
    }

    protected TabData[] getTabData() {
        if (this.mTabs == null) {
            this.mTabs = new TabData[2];
            this.mTabs[0] = new TabData( this.getContext().getString(string.sodk_editor_tab_file), id.fileTab, layout.sodk_editor_tab_one, View.VISIBLE);
            this.mTabs[1] = new TabData( this.getContext().getString(string.sodk_editor_tab_pages), id.pagesTab, layout.sodk_editor_tab_right, View.GONE);
        }

        return this.mTabs;
    }

    protected int getTabUnselectedColor() {
        return viewx.core.content.a.c(this.getContext(), color.sodk_editor_header_other_color);
    }

    protected boolean hasRedo() {
        return false;
    }

    protected boolean hasSearch() {
        String var1 = this.getFileExtension();
        return var1 != null && var1.compareToIgnoreCase("txt") == 0;
    }

    protected boolean hasUndo() {
        return false;
    }

    protected void hideUnnecessaryDivider2(int var1) {
        LinearLayout var2 = (LinearLayout)this.findViewById(var1);
        if (var2 != null) {
            int var3 = 0;
            int var4 = 0;

            int var6;
            for(var1 = 0; var3 < var2.getChildCount(); var1 = var6) {
                View var5 = var2.getChildAt(var3);
                var6 = var5.getId();
                int var7;
                if (var6 != id.divider_2) {
                    var7 = var4;
                    var6 = var1;
                    if (var5.getVisibility() == View.VISIBLE) {
                        var7 = var4;
                        var6 = var1;
                        if (var1 == 1) {
                            var7 = var4 + 1;
                            var6 = var1;
                        }
                    }
                } else {
                    var6 = var1 + 1;
                    var7 = var4;
                }

                ++var3;
                var4 = var7;
            }

            if (var4 == 0) {
//                this.findViewById(id.divider_1).setVisibility(View.GONE);
                this.findViewById(id.divider_2).setVisibility(View.GONE);
            }

        }
    }

    protected boolean inputViewHasFocus() {
        return false;
    }

    public void onClick(View var1) {
        super.onClick(var1);
    }

    protected void onFullScreenHide() {
        this.findViewById(id.footer).setVisibility(View.GONE);
        this.findViewById(id.header).setVisibility(View.GONE);
        this.layoutNow();
    }

    public void onPause() {
        this.onPauseCommon();
    }

    public void onRedoButton(View var1) {
        super.onRedoButton(var1);
    }

    public void onReflowButton(View var1) {
        String var2 = this.getFileExtension();
        if (var2 != null && (var2.compareToIgnoreCase("txt") == 0 || var2.compareToIgnoreCase("csv") == 0)) {
            super.onReflowButton(var1);
        }

    }

    public void onResume() {
        this.onResumeCommon();
    }

    public void onUndoButton(View var1) {
        super.onUndoButton(var1);
    }

    protected void scaleHeader() {
        this.scaleToolbar(id.other_toolbar, 0.65F);
//        this.mBackButton.setScaleX(0.65F);
//        this.mBackButton.setScaleY(0.65F);
    }

    protected void setupTabs() {
    }

    public void showUI(boolean var1) {
        this.layoutNow();
        this.afterShowUI(var1);
    }

    protected void updateUIAppearance() {
    }

    protected boolean usePagesView() {
        return false;
    }
}
