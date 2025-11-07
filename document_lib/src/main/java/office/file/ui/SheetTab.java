package office.file.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import office.file.ui.editor.R.drawable;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.SOTextView;

public class SheetTab extends LinearLayout {
    private static boolean mIsEditingEnabled;
    private boolean mHighlight = false;
    private int mSheetNumber = 0;

    public SheetTab(Context var1) {
        super(var1);
        LayoutInflater.from(var1).inflate(layout.sodk_editor_sheet_tab, this);
    }

    public SheetTab(Context var1, AttributeSet var2) {
        super(var1, var2);
    }

    private View getOuterView() {
        return this.findViewById(id.sheetTab);
    }

    private Space getSpacerView() {
        return (Space)this.findViewById(id.spacer);
    }

    private SOTextView getXView() {
        return (SOTextView)this.findViewById(id.button2);
    }

    public static void setEditingEbabled(boolean var0) {
        mIsEditingEnabled = var0;
    }

    public SOTextView getNameView() {
        return (SOTextView)this.findViewById(id.button1);
    }

    public int getSheetNumber() {
        return this.mSheetNumber;
    }

    public String getText() {
        return this.getNameView().getText().toString();
    }

    public void onDraw(Canvas var1) {
        super.onDraw(var1);
    }

    public boolean performClick() {
        return this.getOuterView().performClick();
    }

    public void setHighlight(boolean var1) {
        this.mHighlight = var1;
        this.invalidate();
    }

    public void setOnClickDelete(final OnClickListener var1) {
        this.getXView().setOnClickListener(new OnClickListener() {
            public void onClick(View var1x) {
                var1.onClick(SheetTab.this);
            }
        });
    }

    public void setOnClickTab(final OnClickListener var1) {
        this.getOuterView().setOnClickListener(new OnClickListener() {
            public void onClick(View var1x) {
                var1.onClick(SheetTab.this);
            }
        });
    }

    public void setOnLongClickTab(final OnLongClickListener var1) {
        this.getOuterView().setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View var1x) {
                var1.onLongClick(SheetTab.this);
                return true;
            }
        });
    }

    public void setSelected(boolean var1) {
        label13: {
            this.getNameView().setSelected(var1);
            this.getXView().setSelected(var1);
            if (var1) {
                this.setBackgroundResource(drawable.sodk_editor_sheet_tab_selected);
                if (mIsEditingEnabled) {
                    var1 = true;
                    break label13;
                }
            } else {
                this.setBackgroundResource(drawable.sodk_editor_sheet_tab);
            }

            var1 = false;
        }

        this.showXView(var1);
    }

    public void setSheetNumber(int var1) {
        this.mSheetNumber = var1;
    }

    public void setText(String var1) {
        this.getNameView().setText(var1);
    }

    public void showXView(boolean var1) {
        SOTextView var2;
        byte var3;
        if (var1) {
            var2 = this.getXView();
            var3 = 0;
        } else {
            var2 = this.getXView();
            var3 = 8;
        }

        var2.setVisibility(var3);
        this.getSpacerView().setVisibility(var3);
    }
}
