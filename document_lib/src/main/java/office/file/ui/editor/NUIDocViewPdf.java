package office.file.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.artifex.solib.SOLinkData;
import com.artifex.solib.SOSelectionLimits;
import com.artifex.solib.c;
import com.artifex.solib.e;
import com.artifex.solib.k;

import office.file.ui.editor.History.HistoryItem;
import office.file.ui.editor.InkColorDialogLib.ColorChangedListener;
import office.file.ui.editor.InkLineWidthDialogLib.WidthChangedListener;
import office.file.ui.editor.R.color;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.integer;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.R.string;
import office.file.ui.editor.a.b;

public class NUIDocViewPdf extends NUIDocView {
    private boolean firstSelectionCleared = false;
    private LinearLayout mAuthorButton;
    private LinearLayout mDeleteButton;
    private LinearLayout mDrawButton;
    private LinearLayout mHighlightButton;
    private LinearLayout mLineColorButton;
    private LinearLayout mLineThicknessButton;
    private LinearLayout mNextLinkButton;
    private LinearLayout mNoteButton;
    private LinearLayout mPreviousLinkButton;
    private ToolbarButton mRedactApplyButton;
    private ToolbarButton mRedactMarkAreaButton;
    private ToolbarButton mRedactMarkButton;
    private ToolbarButton mRedactRemoveButton;
    private LinearLayout mTocButton;
    private ToolbarButton mToggleAnnotButton;
    private ImageView mLineColorView;

    public NUIDocViewPdf(Context var1) {
        super(var1);
        this.a(var1);
    }

    public NUIDocViewPdf(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.a(var1);
    }

    public NUIDocViewPdf(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.a(var1);
    }

    private void a() {
        LinearLayout var1 = (LinearLayout) this.createToolbarButton(id.toc_button);
        this.mTocButton = var1;

        setEnable(var1, false);
    }

    private void setEnable(View view, boolean enable) {
        if (enable) {
            view.setAlpha(1f);
        } else {
            view.setAlpha(0.5f);
        }
        view.setEnabled(enable);
    }

    private void a(Context var1) {
    }

    private void a(View var1) {
        (new a(this.getContext(), this.getDoc(), this, new b() {
            public void a(SOLinkData var1) {
                DocView var2 = NUIDocViewPdf.this.getDocView();
                var2.addHistory(var2.getScrollX(), var2.getScrollY(), var2.getScale(), true);
                int var3 = var2.scrollBoxToTopAmount(var1.a, var1.b);
                var2.addHistory(var2.getScrollX(), var2.getScrollY() - var3, var2.getScale(), false);
                var2.scrollBoxToTop(var1.a, var1.b);
                NUIDocViewPdf.this.updateUIAppearance();
            }
        })).a();
    }

    private void a(HistoryItem var1) {
        this.getDocView().onHistoryItem(var1);
        this.updateUIAppearance();
    }

    private void b() {
        if (this.mSavePdfButton != null) {
            this.mSavePdfButton.setVisibility(GONE);
        }

        if (this.mOpenPdfInButton != null) {
            this.mOpenPdfInButton.setVisibility(GONE);
        }

    }

    private void b(View var1) {
        HistoryItem var2 = this.getDocView().getHistory().previous();
        if (var2 != null) {
            this.a(var2);
        }

    }

    private void c(View var1) {
        HistoryItem var2 = this.getDocView().getHistory().next();
        if (var2 != null) {
            this.a(var2);
        }

    }

    protected void afterFirstLayoutComplete() {
        super.afterFirstLayoutComplete();
        this.mToggleAnnotButton = (ToolbarButton) this.createToolbarButton(id.show_annot_button);
        this.mHighlightButton = (LinearLayout) this.createToolbarButton(id.highlight_button);
        this.mNoteButton = (LinearLayout) this.createToolbarButton(id.note_button);
        this.mAuthorButton = (LinearLayout) this.createToolbarButton(id.author_button);
        this.mDrawButton = (LinearLayout) this.createToolbarButton(id.draw_button);
        this.mLineColorButton = (LinearLayout) this.createToolbarButton(id.line_color_button);
        this.mLineThicknessButton = (LinearLayout) this.createToolbarButton(id.line_thickness_button);
        this.mDeleteButton = (LinearLayout) this.createToolbarButton(id.delete_button);
        this.mRedactMarkButton = (ToolbarButton) this.createToolbarButton(id.redact_button_mark);
        this.mRedactMarkAreaButton = (ToolbarButton) this.createToolbarButton(id.redact_button_mark_area);
        this.mRedactRemoveButton = (ToolbarButton) this.createToolbarButton(id.redact_button_remove);
        this.mRedactApplyButton = (ToolbarButton) this.createToolbarButton(id.redact_button_apply);
        this.mLineColorView = findViewById(id.line_color);
        if (this.mConfigOptions.a != null && !this.mConfigOptions.y()) {
            this.mConfigOptions.a.a(this.mRedactMarkButton);
            this.mConfigOptions.a.a(this.mRedactMarkAreaButton);
            this.mConfigOptions.a.a(this.mRedactRemoveButton);
            this.mConfigOptions.a.a(this.mRedactApplyButton);
        }

        this.a();
        this.b();
        this.mPreviousLinkButton = (LinearLayout) this.createToolbarButton(id.previous_link_button);
        this.mNextLinkButton = (LinearLayout) this.createToolbarButton(id.next_link_button);
    }

    protected void checkXFA() {
        if (k.c().w()) {
            boolean var1;
            if (this.mPageCount == 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            if (var1) {
                boolean var2 = this.getDoc().x();
                boolean var3 = this.getDoc().y();
                if (var2 && !var3) {
                    try {
                        Utilities.showMessage((Activity) this.getContext(), this.getContext().getString(string.sodk_editor_xfa_title), this.getContext().getString(string.sodk_editor_xfa_body));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                if (var2 && var3) {
                    ((c) this.getDoc()).f(true);
                }
            }
        }

    }

    protected PageAdapter createAdapter() {
        return new PageAdapter(this.activity(), this, 2);
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
        return new DocPdfView(var1);
    }

    protected void createReviewButtons() {
    }

    public int getBorderColor() {
        return viewx.core.content.a.c(this.getContext(), color.sodk_editor_header_pdf_color);
    }

    public InputView getInputView() {
        return null;
    }

    protected int getLayoutId() {
        return layout.sodk_editor_pdf_document;
    }

    public DocPdfView getPdfDocView() {
        return (DocPdfView) this.getDocView();
    }

    public NUIDocView.TabData[] getTabData() {
        if (this.mTabs == null) {
            this.mTabs = new NUIDocView.TabData[4];
            int i = this.mConfigOptions.y() ? 0 : 8;
            if (this.mConfigOptions.b()) {
                NUIDocView.TabData[] tabDataArr = this.mTabs;
                NUIDocView.TabData tabData = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_file), R.id.fileTab, R.layout.sodk_editor_tab_left, 0);
                tabDataArr[0] = tabData;
                NUIDocView.TabData[] tabDataArr2 = this.mTabs;
                NUIDocView.TabData tabData2 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_annotate), R.id.annotateTab, R.layout.sodk_editor_tab, 0);
                tabDataArr2[1] = tabData2;
                NUIDocView.TabData[] tabDataArr3 = this.mTabs;
                NUIDocView.TabData tabData3 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_redact), R.id.redactTab, R.layout.sodk_editor_tab, 0);
                tabDataArr3[2] = tabData3;
                NUIDocView.TabData[] tabDataArr4 = this.mTabs;
                NUIDocView.TabData tabData4 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_pages), R.id.pagesTab, R.layout.sodk_editor_tab_right, 0);
                tabDataArr4[3] = tabData4;
            } else {
                NUIDocView.TabData[] tabDataArr5 = this.mTabs;
                NUIDocView.TabData tabData5 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_file), R.id.fileTab, R.layout.sodk_editor_tab_left, 0);
                tabDataArr5[0] = tabData5;
                NUIDocView.TabData[] tabDataArr6 = this.mTabs;
                NUIDocView.TabData tabData6 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_annotate), R.id.annotateTab, R.layout.sodk_editor_tab, 8);
                tabDataArr6[1] = tabData6;
                NUIDocView.TabData[] tabDataArr7 = this.mTabs;
                NUIDocView.TabData tabData7 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_redact), R.id.redactTab, R.layout.sodk_editor_tab, i);
                tabDataArr7[2] = tabData7;
                NUIDocView.TabData[] tabDataArr8 = this.mTabs;
                NUIDocView.TabData tabData8 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_pages), R.id.pagesTab, R.layout.sodk_editor_tab_right, 0);
                tabDataArr8[3] = tabData8;
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
            var2 = color.sodk_editor_header_pdf_color;
        }

        return viewx.core.content.a.c(var1, var2);
    }

    protected int getTabUnselectedColor() {
        int var1;
        if (this.getResources().getInteger(integer.sodk_editor_ui_doc_tabbar_color_from_doctype) == 0) {
            var1 = viewx.core.content.a.c(this.activity(), color.sodk_editor_header_color);
        } else {
            var1 = Utilities.colorForDocExt(this.getContext(), this.getDocFileExtension());
        }

        return var1;
    }

    protected boolean inputViewHasFocus() {
        return false;
    }

    protected boolean isRedactionMode() {
        String var1 = this.getCurrentTab();
        return var1 != null && var1.equals("REDACT");
    }

    protected void layoutAfterPageLoad() {
    }

    public void onClick(View var1) {
        super.onClick(var1);
        if (var1 == this.mToggleAnnotButton) {
            this.onToggleAnnotationsButton(var1);
        }

        if (var1 == this.mHighlightButton) {
            this.onHighlightButton(var1);
        }

        if (var1 == this.mDeleteButton) {
            this.onDeleteButton(var1);
        }

        if (var1 == this.mNoteButton) {
            this.onNoteButton(var1);
        }

        if (var1 == this.mAuthorButton) {
            this.onAuthorButton(var1);
        }

        if (var1 == this.mDrawButton) {
            this.onDrawButton(var1);
        }

        if (var1 == this.mLineColorButton) {
            this.onLineColorButton(var1);
        }

        if (var1 == this.mLineThicknessButton) {
            this.onLineThicknessButton(var1);
        }

        if (var1 == this.mTocButton) {
            if (var1.getAlpha() != 1f) { // check by dev
                return;
            }
            this.a(var1);
        }

        if (var1 == this.mRedactMarkButton) {
            this.onRedactMark(var1);
        }

        if (var1 == this.mRedactMarkAreaButton) {
            this.onRedactMarkArea(var1);
        }

        if (var1 == this.mRedactRemoveButton) {
            this.onRedactRemove(var1);
        }

        if (var1 == this.mRedactApplyButton) {
            this.onRedactApply(var1);
        }

        if (var1 == this.mPreviousLinkButton) {
            this.b(var1);
        }

        if (var1 == this.mNextLinkButton) {
            this.c(var1);
        }

    }

    public void onDeleteButton(View var1) {
        DocPdfView var2 = this.getPdfDocView();
        if (this.getDoc().getSelectionCanBeDeleted()) {
            this.getDoc().selectionDelete();
        } else {
            if (!var2.getDrawMode()) {
                return;
            }

            var2.clearInk();
        }

        this.updateUIAppearance();
    }

    protected void onDeviceSizeChange() {
        super.onDeviceSizeChange();
        office.file.ui.editor.a.b();
    }

    protected void onDocCompleted() {
        if (!this.mFinished) {
            if (!this.firstSelectionCleared) {
                this.mSession.getDoc().clearSelection();
                this.firstSelectionCleared = true;
            }

            this.mPageCount = this.mSession.getDoc().r();
            String var1;
            if (this.mPageCount <= 0) {
                var1 = Utilities.getOpenErrorDescription(this.getContext(), 17);
                try {
                    Utilities.showMessage((Activity) this.getContext(), this.getContext().getString(string.sodk_editor_error), var1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                this.mAdapter.setCount(this.mPageCount);
                this.layoutNow();
//                this.mTocButton.setEnabled(false);
                setEnable(this.mTocButton, false);

                //this.setButtonColor(this.mTocButton, this.getResources().getInteger(color.sodk_editor_button_disabled));
                k.a(this.getDoc(), new com.artifex.solib.k.a() {
                    public void a(int var1, int var2, int var3, String var4, String var5, float var6, float var7) {
//                        NUIDocViewPdf.this.mTocButton.setEnabled(true);
                        setEnable(NUIDocViewPdf.this.mTocButton, true);
                        NUIDocViewPdf var8 = NUIDocViewPdf.this;
                        //var8.setButtonColor(var8.mTocButton, NUIDocViewPdf.this.getResources().getInteger(color.sodk_editor_header_button_enabled_tint));
                    }
                });
                if (this.mSession.getDoc().getAuthor() == null) {
                    var1 = Utilities.getApplicationName(this.activity());
                    var1 = Utilities.getStringPreference(Utilities.getPreferencesObject(this.activity(), "general"), "DocAuthKey", var1);
                    this.mSession.getDoc().setAuthor(var1);
                }

            }
        }
    }

    public void onDrawButton(View var1) {
        this.getPdfDocView().onDrawMode();
        this.updateUIAppearance();
    }

    protected void onFullScreen(View var1) {
        this.getPdfDocView().resetModes();
        this.updateUIAppearance();
        super.onFullScreen(var1);
    }

    public void onHighlightButton(View var1) {
        if (this.getDoc() != null) {
            this.getDoc().addHighlightAnnotation();
            this.getDoc().clearSelection();
        }
    }

    public void onLineColorButton(View var1) {
        final DocPdfView var2 = this.getPdfDocView();
        if (var2.getDrawMode()) {
            InkColorDialogLib var3 = new InkColorDialogLib(1, this.activity(), this.mLineColorButton, new ColorChangedListener() {
                public void onColorChanged(String var1) {
                    int var2x = Color.parseColor(var1);
                    var2.setInkLineColor(var2x);
                    NUIDocViewPdf.this.mLineColorView.setColorFilter(var2x);
//                    NUIDocViewPdf.this.mLineColorButton.setDrawableColor(var2x);
                }
            }, true);
            var3.setShowTitle(false);
            var3.show();
        }

    }

    public void onLineThicknessButton(View var1) {
        final DocPdfView var3 = this.getPdfDocView();
        if (var3.getDrawMode()) {
            float var2 = var3.getInkLineThickness();
            InkLineWidthDialogLib.show(this.activity(), this.mLineThicknessButton, var2, new WidthChangedListener() {
                public void onWidthChanged(float var1) {
                    var3.setInkLineThickness(var1);
                }
            });
        }

    }

    public void onNoteButton(View var1) {
        this.getPdfDocView().onNoteMode();
        this.updateUIAppearance();
    }

    protected void onPageLoaded(int var1) {
        this.checkXFA();
        super.onPageLoaded(var1);
    }

    public void onRedactApply(View var1) {
        Utilities.yesNoMessage((Activity) this.getContext(), "", this.getContext().getString(string.sodk_editor_redact_confirm_apply_body), this.getContext().getString(string.sodk_editor_yes), this.getContext().getString(string.sodk_editor_no), new Runnable() {
            public void run() {
                c var1 = (c) NUIDocViewPdf.this.getDoc();
                var1.u();
                var1.clearSelection();
                NUIDocViewPdf.this.updateUIAppearance();
            }
        }, new Runnable() {
            public void run() {
            }
        });
    }

    public void onRedactMark(View var1) {
        if (e.k() == -1) {
            try {
                Utilities.showMessage((Activity) this.getContext(), this.getContext().getString(string.sodk_editor_marknosel_title), this.getContext().getString(string.sodk_editor_marknosel_body));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            c var2 = (c) this.getDoc();
            var2.s();
            var2.clearSelection();
            this.updateUIAppearance();
        }
    }

    public void onRedactMarkArea(View var1) {
        this.getPdfDocView().toggleMarkAreaMode();
        this.updateUIAppearance();
    }

    public void onRedactRemove(View var1) {
        if (this.getDoc().getSelectionCanBeDeleted()) {
            this.getDoc().selectionDelete();
            this.updateUIAppearance();
        }

    }

    public void onRedoButton(View var1) {
        super.onRedoButton(var1);
    }

    public void onReflowButton(View var1) {
    }

    protected void onSearch() {
        super.onSearch();
    }

    public void onTabChanged(String var1) {
        super.onTabChanged(var1);
    }

    protected void onTabChanging(String var1, String var2) {
        this.getPdfDocView().saveNoteData();
        if (var1.equals(this.getContext().getString(string.sodk_editor_tab_redact))) {
            this.getDoc().clearSelection();
        }

        if (var1.equals(this.getContext().getString(string.sodk_editor_tab_annotate))) {
            if (this.getPdfDocView().getDrawMode()) {
                this.getPdfDocView().onDrawMode();
            }

            this.getDoc().clearSelection();
        }

    }

    public void onToggleAnnotationsButton(View var1) {
    }

    public void onUndoButton(View var1) {
        super.onUndoButton(var1);
    }

    protected void preSaveQuestion(final Runnable var1, final Runnable var2) {
        if (this.getDoc()!=null && !((c) this.getDoc()).t()) {
            if (var1 != null) {
                var1.run();
            }

        } else {
            Utilities.yesNoMessage((Activity) this.getContext(), "", this.getContext().getString(string.sodk_editor_redact_confirm_save), this.getContext().getString(string.sodk_editor_yes), this.getContext().getString(string.sodk_editor_no), new Runnable() {
                public void run() {
                    Runnable var1x = var1;
                    if (var1x != null) {
                        var1x.run();
                    }

                }
            }, new Runnable() {
                public void run() {
                    Runnable var1 = var2;
                    if (var1 != null) {
                        var1.run();
                    }

                }
            });
        }
    }

    protected void prepareToGoBack() {
        if (this.mSession == null || this.mSession.getDoc() != null) {
            if (this.getPdfDocView() != null) {
                this.getPdfDocView().resetModes();
            }

        }
    }

    public void reloadFile() {
        c var1 = (c) this.getDoc();
        if (var1 == null)
            return;
        boolean var2 = var1.i();
        boolean var3 = var1.h();
        boolean var4 = false;
        var1.b(false);
        var1.a(false);
        SOFileState var5 = this.mSession.getFileState();
        String var11;
        if (var2) {
            var11 = var5.getInternalPath();
        } else if (var3) {
            String var6 = var5.getUserPath();
            var11 = var6;
            if (var6 == null) {
                var11 = this.mSession.getFileState().getOpenedPath();
            }
        } else {
            var11 = var5.getOpenedPath();
            if (var1.f()) {
                return;
            }

            long var7 = var1.a();
            long var9 = com.artifex.solib.a.b(var11);
            if (var9 == 0L) {
                return;
            }

            if (var9 < var7) {
                return;
            }
        }

        if (!var2) {
            var1.a(var11);
        }

        com.artifex.solib.c.c var12 = new com.artifex.solib.c.c() {
            public void a() {
                if (NUIDocViewPdf.this.getDocView() != null) {
                    NUIDocViewPdf.this.getDocView().onReloadFile();
                }

                if (NUIDocViewPdf.this.usePagesView() && NUIDocViewPdf.this.getDocListPagesView() != null) {
                    NUIDocViewPdf.this.getDocListPagesView().onReloadFile();
                }

                Utilities.createAndShowWaitSpinner(getContext()).dismiss();
            }
        };
        if (var3 || var2) {
            var4 = true;
        }

        var1.a(var11, var12, var4);
    }

    public void setConfigurableButtons() {
        super.setConfigurableButtons();
        this.b();
    }

    protected void setupTabs() {
//        super.setupTabs();
//        if (this.mConfigOptions.a != null && !this.mConfigOptions.y()) {
//            View var1 = (View) this.tabMap.get(this.getContext().getString(string.sodk_editor_tab_redact));
//            if (var1 != null) {
//                this.mConfigOptions.a.a(var1);
//            }
//        }

    }

    protected boolean shouldConfigureSaveAsPDFButton() {
        return false;
    }

    protected void updateUIAppearance() {
        DocPdfView var1 = this.getPdfDocView();
        this.updateSaveUIAppearance();
        this.updateUndoUIAppearance();
        SOSelectionLimits var2 = this.getDocView().getSelectionLimits();
        if (var2 != null && var2.getIsActive()) {
            var2.getIsCaret();
        }

        if (this.getDoc() == null)
            return;
        boolean canBeDeleted = this.getDoc().getSelectionCanBeDeleted();
        boolean isAlterable = this.getDoc().getSelectionIsAlterableTextSelection();
        setEnable(this.mHighlightButton, isAlterable);
        isAlterable = var1.getNoteMode();
        setEnable(this.mNoteButton, isAlterable);
        this.findViewById(id.note_button).setSelected(isAlterable);
        boolean var5 = var1.getDrawMode();
        isAlterable = ((DocPdfView) this.getDocView()).hasNotSavedInk();
        LinearLayout var10 = this.mDeleteButton;
        boolean var6 = false;
        if ((!var5 || !isAlterable) && !canBeDeleted) {
            isAlterable = false;
        } else {
            isAlterable = true;
        }

        setEnable(var10, isAlterable);

        var10 = this.mNoteButton;
        isAlterable = !var5;
        setEnable(var10, isAlterable);
        setEnable(this.mAuthorButton, isAlterable);

        setEnable(this.mHighlightButton, isAlterable);

        setEnable(this.mLineColorButton, !isAlterable);
        setEnable(this.mLineThicknessButton, !isAlterable);

        int var7 = ((DocPdfView) this.getDocView()).getInkLineColor();
        this.mLineColorView.setColorFilter(var7);
        this.findViewById(id.line_color_button).setSelected(var5);
        c var11 = (c) this.getDoc();
        var5 = var1.getMarkAreaMode();
        setEnable(this.mRedactMarkButton, !var5);

        this.mRedactMarkAreaButton.setSelected(var5);
        ToolbarButton var8 = this.mRedactRemoveButton;
        isAlterable = !var5 && canBeDeleted && var11.o();

        setEnable(var8, isAlterable);
        var8 = this.mRedactApplyButton;
        isAlterable = var6;
        if (!var5) {
            if (var11.t()) {
                isAlterable = true;
            }
        }

        setEnable(var8, isAlterable);

        History var9 = var1.getHistory();
        setEnable(this.mPreviousLinkButton, var9.canPrevious());

        this.mNextLinkButton.setEnabled(var9.canNext());
        setEnable(this.mPreviousLinkButton, var9.canNext());

        this.getPdfDocView().onSelectionChanged();
    }


    protected void updateUndoUIAppearance() {
        this.mUndoButton.setVisibility(VISIBLE);
        this.mRedoButton.setVisibility(VISIBLE);
    }
}
