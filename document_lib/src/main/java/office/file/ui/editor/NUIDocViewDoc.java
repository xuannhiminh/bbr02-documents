
package office.file.ui.editor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.artifex.solib.SODoc;
import com.artifex.solib.SOSelectionLimits;
import com.artifex.solib.k;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import office.file.ui.R;
import office.file.ui.R.color;
import office.file.ui.R.id;
import office.file.ui.R.layout;
import office.file.ui.R.string;

public class NUIDocViewDoc extends NUIDocView {
    private LinearLayout mReviewAcceptButton;
    private LinearLayout mReviewAuthorButton;
    private LinearLayout mReviewCommentButton;
    private LinearLayout mReviewDeleteCommentButton;
    private LinearLayout mReviewNextButton;
    private LinearLayout mReviewPreviousButton;
    private LinearLayout mReviewRejectButton;
    private LinearLayout mReviewShowChangesButton;
    private LinearLayout mReviewTrackChangesButton;

    public NUIDocViewDoc(Context var1) {
        super(var1);
        this.a(var1);
    }

    public NUIDocViewDoc(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.a(var1);
    }

    public NUIDocViewDoc(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.a(var1);
    }

    private void a(Context var1) {
    }

    private void a(boolean var1) {
        DocView var2 = this.getDocView();
        SODoc var3 = var2.getDoc();
        var2.preNextPrevTrackedChange();
        SOSelectionLimits var4 = var2.getSelectionLimits();
        boolean var5;
        if (var4 != null) {
            var5 = var4.getIsActive();
        } else {
            var5 = false;
        }

        if (var1 != var5) {
            if (var1) {
                var2.selectTopLeft();
            } else {
                var3.clearSelection();
            }
        }

        if (var3.nextTrackedChange()) {
            var2.onNextPrevTrackedChange();
        } else {
            if (!var5) {
                var3.clearSelection();
            }

            Utilities.yesNoMessage(this.activity(), this.activity().getString(string.sodk_editor_no_more_found), this.activity().getString(string.sodk_editor_keep_searching), this.activity().getString(string.sodk_editor_str_continue), this.activity().getString(string.sodk_editor_stop), new Runnable() {
                public void run() {
                    NUIDocViewDoc.this.a(false);
                }
            }, (Runnable) null);
        }

    }

    private void b(boolean var1) {
        DocView var2 = this.getDocView();
        SODoc var3 = var2.getDoc();
        var2.preNextPrevTrackedChange();
        SOSelectionLimits var4 = var2.getSelectionLimits();
        boolean var5;
        if (var4 != null) {
            var5 = var4.getIsActive();
        } else {
            var5 = false;
        }

        if (var1 != var5) {
            if (var1) {
                var2.selectTopLeft();
            } else {
                var3.clearSelection();
            }
        }

        if (var3.previousTrackedChange()) {
            var2.onNextPrevTrackedChange();
        } else {
            if (!var5) {
                var3.clearSelection();
            }

            Utilities.yesNoMessage(this.activity(), this.activity().getString(string.sodk_editor_no_more_found), this.activity().getString(string.sodk_editor_keep_searching), this.activity().getString(string.sodk_editor_str_continue), this.activity().getString(string.sodk_editor_stop), new Runnable() {
                public void run() {
                    NUIDocViewDoc.this.b(false);
                }
            }, (Runnable) null);
        }

    }

    protected void createReviewButtons() {
        if (this.mConfigOptions.b()) {
            this.mReviewShowChangesButton = (LinearLayout) this.createToolbarButton(id.show_changes_button);
            this.mReviewTrackChangesButton = (LinearLayout) this.createToolbarButton(id.track_changes_button);
            this.mReviewCommentButton = (LinearLayout) this.createToolbarButton(id.comment_button);
            this.mReviewDeleteCommentButton = (LinearLayout) this.createToolbarButton(id.delete_comment_button);
            this.mReviewAcceptButton = (LinearLayout) this.createToolbarButton(id.accept_button);
            this.mReviewRejectButton = (LinearLayout) this.createToolbarButton(id.reject_button);
            this.mReviewNextButton = (LinearLayout) this.createToolbarButton(id.next_button);
            this.mReviewPreviousButton = (LinearLayout) this.createToolbarButton(id.previous_button);
            this.mReviewAuthorButton = (LinearLayout) this.createToolbarButton(id.author_button);
//            ArrayList var1 = new ArrayList();
//            var1.add(this.mReviewShowChangesButton);
//            var1.add(this.mReviewTrackChangesButton);
//            var1.add(this.mReviewCommentButton);
//            var1.add(this.mReviewDeleteCommentButton);
//            var1.add(this.mReviewAcceptButton);
//            var1.add(this.mReviewRejectButton);
//            var1.add(this.mReviewNextButton);
//            var1.add(this.mReviewPreviousButton);
            if (this.mConfigOptions.a != null && !this.mConfigOptions.v()) {
                this.mConfigOptions.a.a(this.mReviewShowChangesButton);
                this.mConfigOptions.a.a(this.mReviewTrackChangesButton);
                this.mConfigOptions.a.a(this.mReviewAuthorButton);
            }

            if (this.mConfigOptions.s()) {
//                var1.add(this.mReviewAuthorButton);
            } else {
                this.findViewById(id.author_button_divider).setVisibility(View.GONE);
                this.mReviewAuthorButton.setVisibility(View.GONE);
            }

//            ToolbarButton.setAllSameSize((ToolbarButton[]) var1.toArray(new ToolbarButton[var1.size()]));
        }
    }

    public int getBorderColor() {
        return viewx.core.content.a.c(this.getContext(), color.sodk_editor_header_doc_color);
    }

    protected int getLayoutId() {
        return layout.sodk_editor_document;
    }

    public void onAcceptButton(View var1) {
        this.getDocView().getDoc().acceptTrackedChange();
    }

    public void onClick(View var1) {
        super.onClick(var1);
        if (var1 == this.mReviewShowChangesButton) {
            this.onShowChangesButton(var1);
        }

        if (var1 == this.mReviewTrackChangesButton) {
            this.onTrackChangesButton(var1);
        }

        if (var1 == this.mReviewCommentButton) {
            this.onCommentButton(var1);
        }

        if (var1 == this.mReviewDeleteCommentButton) {
            this.onDeleteCommentButton(var1);
        }

        if (var1 == this.mReviewAcceptButton) {
            this.onAcceptButton(var1);
        }

        if (var1 == this.mReviewRejectButton) {
            this.onRejectButton(var1);
        }

        if (var1 == this.mReviewNextButton) {
            this.onNextButton(var1);
        }

        if (var1 == this.mReviewPreviousButton) {
            this.onPreviousButton(var1);
        }

        if (var1 == this.mReviewAuthorButton) {
            this.onAuthorButton(var1);
        }

    }

    public void onCommentButton(View var1) {
        this.getDocView().addComment();
    }

    public void onDeleteCommentButton(View var1) {
        this.getDocView().getDoc().deleteHighlightAnnotation();
    }

    protected void onFullScreen(View var1) {
        this.getDocView().saveComment();
        super.onFullScreen(var1);
    }

    public void onNextButton(View var1) {
        this.a(true);
    }

    public void onPreviousButton(View var1) {
        this.b(true);
    }

    public void onRejectButton(View var1) {
        this.getDocView().getDoc().rejectTrackedChange();
    }

    public void onShowChangesButton(View var1) {
        this.getDocView().saveComment();
        SODoc var2 = this.getDocView().getDoc();
        var2.setShowingTrackedChanges(!var2.getShowingTrackedChanges());
        this.onSelectionChanged();
    }

    public void onTrackChangesButton(View var1) {
        this.getDocView().saveComment();
        SODoc var2 = this.getDocView().getDoc();
        var2.setTrackingChanges(!var2.getTrackingChanges());
        this.onSelectionChanged();
    }

    protected void prepareToGoBack() {
        DocView var1 = this.getDocView();
        if (var1 != null) {
            var1.preNextPrevTrackedChange();
        }

    }

    protected void setupTabs() {
//        super.setupTabs();
        if (this.mConfigOptions.a != null && !k.e(this.activity())) {
            View var1 = (View) this.tabMap.get(this.getContext().getString(string.sodk_editor_tab_review));
            if (var1 != null) {
                this.mConfigOptions.a.a(var1);
            }
        }

    }

    protected void updateReviewUIAppearance() {
        try {
            SODoc var1 = this.getDocView().getDoc();
            boolean var2 = var1.getShowingTrackedChanges();
            boolean var3 = var1.getTrackingChanges();
            LinearLayout var4 = this.mReviewShowChangesButton;
            int var5;
            if (var2) {
                var5 = office.file.ui.R.drawable.ic_checked;
            } else {
                var5 = office.file.ui.R.drawable.ic_unchecked;
            }

            ((ImageView) var4.getChildAt(0)).setImageResource(var5);
            var4 = this.mReviewTrackChangesButton;
            if (var3) {
                var5 = office.file.ui.R.drawable.ic_checked;
            } else {
                var5 = office.file.ui.R.drawable.ic_unchecked;
            }
            ((ImageView) var4.getChildAt(0)).setImageResource(var5);

            SOSelectionLimits var9 = this.getDocView().getSelectionLimits();
            boolean var6 = false;
            boolean var7;
            if (var9 != null) {
                var7 = var9.getIsActive();
            } else {
                var7 = false;
            }

            boolean var10;
            if (var7 && var1.getSelectionHasAssociatedPopup()) {
                var10 = true;
            } else {
                var10 = false;
            }

            if (var7 && !var10 && var2) {
                var7 = true;
            } else {
                var7 = false;
            }

            if (var10) {
                this.mReviewDeleteCommentButton.setVisibility(View.VISIBLE);
                this.mReviewCommentButton.setVisibility(View.GONE);
                this.mReviewDeleteCommentButton.setEnabled(true);
            } else {
                this.mReviewDeleteCommentButton.setVisibility(View.GONE);
                this.mReviewCommentButton.setVisibility(View.VISIBLE);
                this.mReviewCommentButton.setEnabled(var7);
            }

            this.mReviewPreviousButton.setEnabled(var2);
            this.mReviewNextButton.setEnabled(var2);
            boolean var8 = var1.selectionIsReviewable();
            var7 = var6;
            if (var2) {
                var7 = var6;
                if (var3) {
                    var7 = var6;
                    if (var8) {
                        var7 = true;
                    }
                }
            }

            this.mReviewAcceptButton.setEnabled(var7);
            this.mReviewRejectButton.setEnabled(var7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
