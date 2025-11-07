package office.file.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.artifex.solib.SOSelectionLimits;

import office.file.ui.editor.R.color;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.integer;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.R.string;
import office.file.ui.editor.ShapeDialog.Shape;
import office.file.ui.editor.ShapeDialog.onSelectShapeListener;

public class NUIDocViewPpt extends NUIDocView {
    private boolean arranging = false;
    boolean b = false;
    private LinearLayout mArrangeBackButton;
    private LinearLayout mArrangeBackwardButton;
    private LinearLayout mArrangeForwardButton;
    private LinearLayout mArrangeFrontButton;
    private LinearLayout mInsertShapeButton;
    private LinearLayout mLineColorButton;
    private LinearLayout mLineTypeButton;
    private LinearLayout mLineWidthButton;
    private LinearLayout mShapeColorButton;
    private ImageView mSlideshowButton;

    public NUIDocViewPpt(Context var1) {
        super(var1);
        this.a(var1);
    }

    public NUIDocViewPpt(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.a(var1);
    }

    public NUIDocViewPpt(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.a(var1);
    }

    private void a(Context var1) {
    }

    protected void afterFirstLayoutComplete() {
        super.afterFirstLayoutComplete();
        this.mShapeColorButton = (LinearLayout) this.createToolbarButton(id.shape_color);
        this.mLineColorButton = (LinearLayout) this.createToolbarButton(id.line_color);
        this.mLineWidthButton = (LinearLayout) this.createToolbarButton(id.line_width);
        this.mLineTypeButton = (LinearLayout) this.createToolbarButton(id.line_type);
        this.mArrangeBackButton = (LinearLayout) this.createToolbarButton(id.arrange_back);
        this.mArrangeBackwardButton = (LinearLayout) this.createToolbarButton(id.arrange_backwards);
        this.mArrangeForwardButton = (LinearLayout) this.createToolbarButton(id.arrange_forward);
        this.mArrangeFrontButton = (LinearLayout) this.createToolbarButton(id.arrange_front);
        this.mInsertShapeButton = (LinearLayout) this.createToolbarButton(id.insert_shape_button);
        this.mSlideshowButton = (ImageView) this.createToolbarButton(id.slideshow_button);
        this.mSlideshowButton.setVisibility(VISIBLE);
    }

    public boolean canCanManipulatePages() {
        return this.mConfigOptions.b();
    }

    protected void createEditButtons() {
        super.createEditButtons();
    }

    protected DocView createMainView(Activity var1) {
        DocPowerPointView var2 = new DocPowerPointView(var1);
        return var2;
    }

    protected void createPagesButtons() {
    }

    protected void createReviewButtons() {
    }

    public void doInsertImage(String var1) {
        var1 = Utilities.preInsertImage(this.getContext(), var1);
        int var2 = this.getTargetPageNumber();
        this.getDoc().clearSelection();
        this.getDoc().a(var2, var1);
        this.getDocView().scrollToPage(var2, false);
    }

    public int getBorderColor() {
        return viewx.core.content.a.c(this.getContext(), color.sodk_editor_header_ppt_color);
    }

    protected int getLayoutId() {
        return layout.sodk_editor_powerpoint_document;
    }

    public NUIDocView.TabData[] getTabData() {
        if (this.mTabs == null) {
            this.mTabs = new NUIDocView.TabData[5];
            if (this.mConfigOptions.b()) {
                NUIDocView.TabData[] tabDataArr = this.mTabs;
                NUIDocView.TabData tabData = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_file), R.id.fileTab, R.layout.sodk_editor_tab_left, 0);
                tabDataArr[0] = tabData;
                NUIDocView.TabData[] tabDataArr2 = this.mTabs;
                NUIDocView.TabData tabData2 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_edit), R.id.editTab, R.layout.sodk_editor_tab, 0);
                tabDataArr2[1] = tabData2;
                NUIDocView.TabData[] tabDataArr3 = this.mTabs;
                NUIDocView.TabData tabData3 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_insert), R.id.insertTab, R.layout.sodk_editor_tab, 0);
                tabDataArr3[2] = tabData3;
                NUIDocView.TabData[] tabDataArr4 = this.mTabs;
                NUIDocView.TabData tabData4 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_format), R.id.formatTab, R.layout.sodk_editor_tab, 0);
                tabDataArr4[3] = tabData4;
                NUIDocView.TabData[] tabDataArr5 = this.mTabs;
                NUIDocView.TabData tabData5 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_slides), R.id.slidesTab, R.layout.sodk_editor_tab_right, 0);
                tabDataArr5[4] = tabData5;
            } else {
                NUIDocView.TabData[] tabDataArr6 = this.mTabs;
                NUIDocView.TabData tabData6 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_file), R.id.fileTab, R.layout.sodk_editor_tab_left, 0);
                tabDataArr6[0] = tabData6;
                NUIDocView.TabData[] tabDataArr7 = this.mTabs;
                NUIDocView.TabData tabData7 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_edit), R.id.editTab, R.layout.sodk_editor_tab, 8);
                tabDataArr7[1] = tabData7;
                NUIDocView.TabData[] tabDataArr8 = this.mTabs;
                NUIDocView.TabData tabData8 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_insert), R.id.insertTab, R.layout.sodk_editor_tab, 8);
                tabDataArr8[2] = tabData8;
                NUIDocView.TabData[] tabDataArr9 = this.mTabs;
                NUIDocView.TabData tabData9 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_format), R.id.formatTab, R.layout.sodk_editor_tab, 8);
                tabDataArr9[3] = tabData9;
                NUIDocView.TabData[] tabDataArr10 = this.mTabs;
                NUIDocView.TabData tabData10 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_slides), R.id.slidesTab, R.layout.sodk_editor_tab_right, 0);
                tabDataArr10[4] = tabData10;
            }
        }
        return this.mTabs;
    }


    protected int getTabSelectedColor() {
        Activity var1;
        int var2;
        if (this.getResources().getInteger(integer.sodk_editor_ui_doc_tab_color_from_doctype) == 0) {
            var1 = this.activity();
            var2 = color.sodk_editor_header_color_selected;
        } else {
            var1 = this.activity();
            var2 = color.sodk_editor_header_ppt_color;
        }

        return viewx.core.content.a.c(var1, var2);
    }

    protected int getTabUnselectedColor() {
        Activity var1;
        int var2;
        if (this.getResources().getInteger(integer.sodk_editor_ui_doc_tabbar_color_from_doctype) == 0) {
            var1 = this.activity();
            var2 = color.sodk_editor_header_color;
        } else {
            var1 = this.activity();
            var2 = color.sodk_editor_header_ppt_color;
        }

        return viewx.core.content.a.c(var1, var2);
    }

    protected void handlePagesTab(String var1) {
        if (var1.equals(this.getResources().getString(string.sodk_editor_tab_slides))) {
            this.showPages();
        } else {
            this.hidePages();
        }

    }

    protected boolean isPagesTab() {
        return this.getCurrentTab().equals(this.activity().getString(string.sodk_editor_tab_slides));
    }

    public void onClick(View var1) {
        super.onClick(var1);
        if (var1 == this.mShapeColorButton) {
            this.onClickFillColor(var1);
        }

        if (var1 == this.mLineColorButton) {
            this.onClickLineColor(var1);
        }

        if (var1 == this.mLineWidthButton) {
            this.onClickLineWidth(var1);
        }

        if (var1 == this.mLineTypeButton) {
            this.onClickLineType(var1);
        }

        if (var1 == this.mArrangeBackButton) {
            this.onClickArrangeBack(var1);
        }

        if (var1 == this.mArrangeBackwardButton) {
            this.onClickArrangeBackwards(var1);
        }

        if (var1 == this.mArrangeForwardButton) {
            this.onClickArrangeForwards(var1);
        }

        if (var1 == this.mArrangeFrontButton) {
            this.onClickArrangeFront(var1);
        }

        if (var1 == this.mInsertShapeButton) {
            this.onInsertShapeButton(var1);
        }

        if (var1 == this.mSlideshowButton) {
            this.onClickSlideshow(var1);
        }

    }

    public void onClickArrangeBack(View var1) {
        if (!this.arranging) {
            this.arranging = true;
            this.updateUIAppearance();
            this.getDoc().setSelectionArrangeBack();
        }
    }

    public void onClickArrangeBackwards(View var1) {
        if (!this.arranging) {
            this.arranging = true;
            this.updateUIAppearance();
            this.getDoc().setSelectionArrangeBackwards();
        }
    }

    public void onClickArrangeForwards(View var1) {
        if (!this.arranging) {
            this.arranging = true;
            this.updateUIAppearance();
            this.getDoc().setSelectionArrangeForwards();
        }
    }

    public void onClickArrangeFront(View var1) {
        if (!this.arranging) {
            this.arranging = true;
            this.updateUIAppearance();
            this.getDoc().setSelectionArrangeFront();
        }
    }

    public void onClickFillColor(View var1) {
        ColorDialogLib var2 = new ColorDialogLib(2, this.getContext(), this.getDoc(), var1, new ColorChangedListener() {
            public void onColorChanged(String var1) {
                NUIDocViewPpt.this.getDoc().setSelectionFillColor(var1);
            }
        });
        var2.setShowTitle(false);
        var2.show();
    }

    public void onClickLineColor(View view) {
        ColorDialogLib dialog = new ColorDialogLib(2, this.getContext(), this.getDoc(), view,
                color -> NUIDocViewPpt.this.getDoc().setSelectionLineColor(color));
        dialog.setShowTitle(false);
        dialog.show();
    }

    public void onClickLineType(View var1) {
        LineTypeDialogLib.show(this.activity(), var1, this.getDoc());
    }

    public void onClickLineWidth(View var1) {
        LineWidthDialogLib.show(this.activity(), var1, this.getDoc());
    }

    public void onClickSlideshow(View var1) {
        try {
            this.getDoc().clearSelection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.getDoc().p();
        ShowSlideActivity.setSession(this.mSession);
        Intent var2 = new Intent(this.getContext(), ShowSlideActivity.class);
        var2.setAction("android.intent.action.VIEW");
        this.activity().startActivity(var2);
    }

    protected void onDocCompleted() {
        super.onDocCompleted();
    }

    public void onInsertShapeButton(View var1) {
        (new ShapeDialog(this.getContext(), var1, new onSelectShapeListener() {
            public void onSelectShape(Shape var1) {
                int var2 = NUIDocViewPpt.this.getTargetPageNumber();
                NUIDocViewPpt var3 = NUIDocViewPpt.this;
                var3.b = true;
                var3.getDoc().clearSelection();
                NUIDocViewPpt.this.getDoc().a(var2, var1.shape, var1.properties);
                NUIDocViewPpt.this.getDocView().scrollToPage(var2, false);
            }
        })).show();
    }

    public void onSelectionChanged() {
        super.onSelectionChanged();
        this.arranging = false;
        this.updateUIAppearance();
        if (this.b) {
            this.getDocView().scrollSelectionIntoView();
        }

        this.b = false;
    }

    protected void setInsertTabVisibility() {
    }

    public boolean showKeyboard() {
        SOSelectionLimits var1 = this.getDocView().getSelectionLimits();
        if (var1 != null && var1.getIsActive() && !this.getDoc().getSelectionCanBeAbsolutelyPositioned()) {
            super.showKeyboard();
            return true;
        } else {
            return false;
        }
    }

    protected void updateEditUIAppearance() {
        super.updateEditUIAppearance();
    }

    protected void updateInsertUIAppearance() {
        if (this.mInsertImageButton != null && this.mConfigOptions.j()) {
            this.mInsertImageButton.setEnabled(true);
        }

        if (this.mInsertPhotoButton != null && this.mConfigOptions.k()) {
            this.mInsertPhotoButton.setEnabled(true);
        }

    }

    protected void updateReviewUIAppearance() {
    }

    private void setCustomEnable(View view, Boolean enable) {
        if (enable) {
            view.setAlpha(1f);
        } else {
            view.setAlpha(0.5f);

        }
    }

    protected void updateUIAppearance() {
        super.updateUIAppearance();
        boolean var1 = this.getDoc().selectionIsAutoshapeOrImage();
        this.mShapeColorButton.setEnabled(var1);
        setCustomEnable(this.mShapeColorButton, var1);

        this.mLineColorButton.setEnabled(var1);
        setCustomEnable(this.mLineColorButton, var1);

        this.mLineWidthButton.setEnabled(var1);
        setCustomEnable(this.mLineWidthButton, var1);

        this.mLineTypeButton.setEnabled(var1);
        setCustomEnable(this.mLineTypeButton, var1);


        LinearLayout var2 = this.mArrangeBackButton;
        boolean var3 = false;
        boolean var4;
        if (var1 && !this.arranging) {
            var4 = true;
        } else {
            var4 = false;
        }

        var2.setEnabled(var4);
        setCustomEnable(var2, var4);

        var2 = this.mArrangeBackwardButton;
        if (var1 && !this.arranging) {
            var4 = true;
        } else {
            var4 = false;
        }

        var2.setEnabled(var4);
        setCustomEnable(var2, var4);

        var2 = this.mArrangeForwardButton;
        if (var1 && !this.arranging) {
            var4 = true;
        } else {
            var4 = false;
        }

        var2.setEnabled(var4);
        setCustomEnable(var2, var4);

        var2 = this.mArrangeFrontButton;
        var4 = var3;
        if (var1) {
            var4 = var3;
            if (!this.arranging) {
                var4 = true;
            }
        }

        var2.setEnabled(var4);
        setCustomEnable(var2, var4);

    }
}
