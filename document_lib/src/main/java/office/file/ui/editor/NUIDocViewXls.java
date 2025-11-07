package office.file.ui.editor;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;

import com.artifex.solib.SODoc;
import com.artifex.solib.SOSelectionLimits;
import com.artifex.solib.SOSelectionTableRange;
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil;

import office.file.ui.editor.R.color;
import office.file.ui.editor.R.drawable;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.integer;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.R.string;
import office.file.ui.SheetTab;

public class NUIDocViewXls extends NUIDocView {
    private static final float CELL_SIZE_INCREMENT = 0.5F;
    private static final int POPUP_OFFSET = 30;
    private static final String TAG = "NUIDocViewXls";
    private static final float cmPerInch = 2.54F;
    private static final float maxCellDimension = 30.0F;
    private static final float minCellDimension = 0.15F;
    private boolean addingPage = false;
    private String currentFormulaCategory = null;
    private ListPopupWindow currentFormulaPopup = null;
    private boolean deletingPage = false;
    private int draggingIndex = -1;
    private LinearLayout mAlignOptionsButton;
    private SOEditText mCellHeightBox;
    private SOTextView mCellHeightLabel;
    private SOEditText mCellWidthBox;
    private SOTextView mCellWidthLabel;
    private LinearLayout mDecreaseCellHeightButton;
    private LinearLayout mDecreaseCellWidthButton;
    private LinearLayout mDeleteColumnButton;
    private LinearLayout mDeleteRowButton;
    private SOTextView mFXButton;
    private LinearLayout mFormulaDateTimeButton;
    private LinearLayout mFormulaEngineeringButton;
    private LinearLayout mFormulaFinancialButton;
    private LinearLayout mFormulaInformationButton;
    private LinearLayout mFormulaLogicalButton;
    private LinearLayout mFormulaLookupButton;
    private LinearLayout mFormulaMathsButton;
    private LinearLayout mFormulaStatisticalButton;
    private LinearLayout mFormulaSumButton;
    private LinearLayout mFormulaTextButton;
    private LinearLayout mIncreaseCellHeightButton;
    private LinearLayout mIncreaseCellWidthButton;
    private LinearLayout mInsertColumnLeftButton;
    private LinearLayout mInsertColumnRightButton;
    private LinearLayout mInsertRowAboveButton;
    private LinearLayout mInsertRowBelowButton;
    private LinearLayout mMergeCellsButton;
    private LinearLayout mNumberFormatButton;
    private boolean undoredo = false;

    public NUIDocViewXls(Context var1) {
        super(var1);
        this.a(var1);
    }

    public NUIDocViewXls(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.a(var1);
    }

    public NUIDocViewXls(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.a(var1);
    }

    private int a(ListAdapter var1) {
        int var2 = 0;
        int var3 = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int var4 = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int var5 = var1.getCount();
        View var6 = null;
        Object var7 = var6;
        int var8 = 0;

        Object var12;
        for (int var9 = 0; var2 < var5; var7 = var12) {
            int var10 = var1.getItemViewType(var2);
            int var11 = var9;
            if (var10 != var9) {
                var6 = null;
                var11 = var10;
            }

            var12 = var7;
            if (var7 == null) {
                var12 = new FrameLayout(this.getContext());
            }

            var6 = var1.getView(var2, var6, (ViewGroup) var12);
            var6.measure(var3, var4);
            var8 += var6.getMeasuredHeight();
            ++var2;
            var9 = var11;
        }

        return var8;
    }

    private void a() {
        SOEditText var1 = (SOEditText) this.findViewById(id.cell_width_box);
        this.mCellWidthBox = var1;
        var1.setImeActionLabel(this.getContext().getString(string.sodk_editor_ime_action_label_done), 66);
        this.mCellWidthBox.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                SOEditText var2 = (SOEditText) var1;
                var2.selectAll();
                String var3 = var2.getText().toString();
                var1.setTag(string.sodk_editor_cellbox_last_value, var3);
            }
        });
        this.mCellWidthBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View var1, boolean var2) {
                if (var2) {
                    var1.callOnClick();
                } else {
                    String var3 = (String) var1.getTag(string.sodk_editor_cellbox_last_value);
                    ((SOEditText) var1).setText(var3);
                }

            }
        });
        this.mCellWidthBox.setOnEditorActionListener(new SOEditTextOnEditorActionListener() {
            public boolean onEditorAction(SOEditText var1, int var2, KeyEvent var3) {
                String var4 = var1.getText().toString();
                if (!var4.isEmpty()) {
                    float var5 = 0.0F;

                    boolean var8;
                    label25:
                    {
                        float var6;
                        try {
                            var6 = Float.parseFloat(var4);
                        } catch (NumberFormatException var7) {
                            var8 = false;
                            break label25;
                        }

                        var8 = true;
                        var5 = var6;
                    }

                    if (var8 && var5 >= 0.15F && var5 <= 30.0F) {
                        var1.setTag(string.sodk_editor_cellbox_last_value, var4);
                        NUIDocViewXls.this.getDoc().setSelectedColumnWidth(var5 / 2.54F);
                    } else {
                        var1.setText((String) var1.getTag(string.sodk_editor_cellbox_last_value));
                    }
                }

                NUIDocViewXls.this.b();
                return true;
            }
        });
        this.mIncreaseCellWidthButton.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                NUIDocViewXls.this.a(0.5F);
            }
        });
        this.mDecreaseCellWidthButton.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                NUIDocViewXls.this.a(-0.5F);
            }
        });
        var1 = (SOEditText) this.findViewById(id.cell_height_box);
        this.mCellHeightBox = var1;
        var1.setImeActionLabel(this.getContext().getString(string.sodk_editor_ime_action_label_done), 66);
        this.mCellHeightBox.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                SOEditText var2 = (SOEditText) var1;
                var2.selectAll();
                String var3 = var2.getText().toString();
                var1.setTag(string.sodk_editor_cellbox_last_value, var3);
            }
        });
        this.mCellHeightBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View var1, boolean var2) {
                if (var2) {
                    var1.callOnClick();
                } else {
                    String var3 = (String) var1.getTag(string.sodk_editor_cellbox_last_value);
                    ((SOEditText) var1).setText(var3);
                }

            }
        });
        this.mCellHeightBox.setOnEditorActionListener(new SOEditTextOnEditorActionListener() {
            public boolean onEditorAction(SOEditText var1, int var2, KeyEvent var3) {
                String var5 = var1.getText().toString();
                if (!var5.isEmpty()) {
                    float var4 = Float.parseFloat(var5);
                    if (var4 >= 0.15F && var4 <= 30.0F) {
                        var1.setTag(string.sodk_editor_cellbox_last_value, var5);
                        NUIDocViewXls.this.getDoc().setSelectedRowHeight(var4 / 2.54F);
                    } else {
                        var1.setText((String) var1.getTag(string.sodk_editor_cellbox_last_value));
                    }
                }

                NUIDocViewXls.this.b();
                return true;
            }
        });
        this.mIncreaseCellHeightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                NUIDocViewXls.this.b(0.5F);
            }
        });
        this.mDecreaseCellHeightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                NUIDocViewXls.this.b(-0.5F);
            }
        });
    }

    private void a(float var1) {
        var1 = Math.min(Math.max((float) Math.round(this.getDoc().getSelectedColumnWidth() * 2.54F * 2.0F) / 2.0F + var1, 0.15F), 30.0F);
        this.getDoc().setSelectedColumnWidth(var1 / 2.54F);
        this.a(this.mCellWidthBox, var1);
    }

    private void a(int var1) {
        this.setCurrentSheet(var1);
    }

    private void a(Context var1) {
    }

    private void a(final String var1) {
        ListPopupWindow var2 = this.currentFormulaPopup;
        if (var2 != null) {
            var2.dismiss();
        }

        if (this.getKeyboardHeight() > 0) {
            Utilities.hideKeyboard(this.getContext());
            final ViewTreeObserver var3 = this.getViewTreeObserver();
            var3.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    var3.removeOnGlobalLayoutListener(this);
                    NUIDocViewXls.this.b(var1);
                }
            });
        } else {
            this.b(var1);
        }

    }

    private void a(SOEditText var1, float var2) {
        var1.setText(String.format("%.2f", var2));
    }

    private void a(SOTextView var1, boolean var2) {
        int var3;
        if (var2) {
            var3 = viewx.core.content.a.c(this.getContext(), color.sodk_editor_button_text);
        } else {
            var3 = -4473925;
        }

        var1.setTextColor(var3);
    }

    private void b() {
        Utilities.hideKeyboard(this.activity());
        this.mCellWidthBox.clearFocus();
        this.mCellHeightBox.clearFocus();
    }

    private void b(float var1) {
        var1 = Math.min(Math.max((float) Math.round(this.getDoc().getSelectedRowHeight() * 2.54F * 2.0F) / 2.0F + var1, 0.15F), 30.0F);
        this.getDoc().setSelectedRowHeight(var1 / 2.54F);
        this.a(this.mCellHeightBox, var1);
    }

    private void b(String var1) {
        final ListPopupWindow var2 = new ListPopupWindow(this.activity());
        var2.setBackgroundDrawable(viewx.core.content.a.a(this.activity(), drawable.sodk_editor_formula_popup));
        var2.setModal(true);
        var2.setAnchorView(this.mFXButton);
        var2.setHorizontalOffset(30);
        var2.setVerticalOffset(30);
        final ChooseFormulaAdapter var3 = new ChooseFormulaAdapter(this.activity(), var1);
        var2.setAdapter(var3);
        Point var4 = Utilities.getScreenSize(this.getContext());
        var2.setContentWidth(var4.x / 2);
        int[] var5 = new int[2];
        this.mFXButton.getLocationOnScreen(var5);
        var5 = Utilities.screenToWindow(var5, this.getContext());
        var2.setHeight(Math.min(var4.y - var5[1] - this.mFXButton.getHeight() - 60, this.a((ListAdapter) var3)));
        var2.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> var1, View var2x, int var3x, long var4) {
                DocExcelView var6;
                String var8;
                label11:
                {
                    var2.dismiss();
                    var6 = (DocExcelView) NUIDocViewXls.this.getDocView();
                    String var7 = var6.getEditText();
                    String var10 = var3.getItem(var3x);
                    if (var7 != null) {
                        var8 = var10;
                        if (!var7.isEmpty()) {
                            break label11;
                        }
                    }

                    StringBuilder var9 = new StringBuilder();
                    var9.append("=");
                    var9.append(var10);
                    var8 = var9.toString();
                }

                var6.insertEditText(var8);
                var6.copyEditTextToCell();
            }
        });
        var2.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                NUIDocViewXls.this.currentFormulaPopup = null;
                NUIDocViewXls.this.currentFormulaCategory = null;
            }
        });
        var2.show();
        this.currentFormulaPopup = var2;
        this.currentFormulaCategory = var1;
        var2.getListView().setDivider((Drawable) null);
        var2.getListView().setVerticalScrollBarEnabled(true);
    }

    private void c() {
        final LinearLayout lnSheetExcelBar = (LinearLayout) this.findViewById(id.excel_sheets_bar);
        lnSheetExcelBar.removeAllViews();
        SheetTab.setEditingEbabled(this.mConfigOptions.b());
        int var2 = this.getDoc().r();
        final Activity var3 = this.activity();
        int var4 = 1;

        while (true) {
            boolean var5 = false;
            if (var4 > var2) {
//                if (this.mConfigOptions.b()) {
                Button btnPlusSheet = (Button) this.activity().getLayoutInflater().inflate(layout.sodk_editor_sheet_tab_plus, lnSheetExcelBar, false);
                btnPlusSheet.setText("+");
                btnPlusSheet.setOnClickListener(new OnClickListener() {
                    public void onClick(View var1) {
                        if (mConfigOptions.b()) {
                            NUIDocViewXls.this.addingPage = true;
                            int var2 = NUIDocViewXls.this.getDoc().r();
                            NUIDocViewXls.this.getDoc().addBlankPage(var2);
                        }
                    }
                });
                lnSheetExcelBar.addView(btnPlusSheet);

                return;
            }

            DocExcelView var6 = (DocExcelView) this.getDocView();
            int var7 = var4 - 1;
            final String var10 = var6.getPageTitle(var7);
            SheetTab var8 = new SheetTab(var3);
            var8.setText(var10);
            var8.setSheetNumber(var7);
            var8.setOnClickTab(new OnClickListener() {
                public void onClick(View var1) {
                    int var2 = ((SheetTab) var1).getSheetNumber();
                    NUIDocViewXls.this.a(var2);
                }
            });

            if (this.mConfigOptions.b()) {
                var8.setOnLongClickTab(new OnLongClickListener() {
                    public boolean onLongClick(View var1x) {
                        int var2 = ((SheetTab) var1x).getSheetNumber();
                        View var3 = lnSheetExcelBar.getChildAt(var2);
                        NUIDocViewXls.this.draggingIndex = var2;
                        String var4 = var10;
                        ClipData var6 = ClipData.newPlainText(var4, var4);
                        DragShadowBuilder var5 = new DragShadowBuilder(var3);
                        if (VERSION.SDK_INT >= 24) {
                            var1x.startDragAndDrop(var6, var5, (Object) null, 0);
                        } else {
                            var1x.startDrag(var6, var5, (Object) null, 0);
                        }

                        return true;
                    }
                });

                lnSheetExcelBar.setOnDragListener(new OnDragListener() {
                    private int dropIndex = -1;

                    private void a(int var1x) {
                        this.dropIndex = var1x;
                        var1x = 0;

                        while (true) {
                            int var2 = lnSheetExcelBar.getChildCount();
                            boolean var3 = true;
                            if (var1x >= var2 - 1) {
                                return;
                            }

                            SheetTab var4 = (SheetTab) lnSheetExcelBar.getChildAt(var1x);
                            if (var1x != this.dropIndex || var1x == NUIDocViewXls.this.draggingIndex) {
                                var3 = false;
                            }

                            var4.setHighlight(var3);
                            ++var1x;
                        }
                    }

                    public boolean onDrag(View var1x, DragEvent var2) {
                        int var3 = var2.getAction();
                        byte var4 = -1;
                        int var5;
                        if (var3 != 2) {
                            label38:
                            {
                                if (var3 != 3) {
                                    if (var3 != 4) {
                                        var5 = var4;
                                        if (var3 != 5) {
                                            var5 = var4;
                                            if (var3 != 6) {
                                                return true;
                                            }
                                        }
                                        break label38;
                                    }
                                } else {
                                    var3 = this.dropIndex;
                                    if (var3 != -1 && var3 != NUIDocViewXls.this.draggingIndex) {
                                        NUIDocViewXls.this.getDoc().movePage(NUIDocViewXls.this.draggingIndex, this.dropIndex);
                                        NUIDocViewXls.this.c();
                                        NUIDocViewXls.this.setCurrentSheet(this.dropIndex);
                                        NUIDocViewXls.this.setSearchStart();
                                    }
                                }

                                NUIDocViewXls.this.draggingIndex = -1;
                                var5 = var4;
                            }
                        } else {
                            var3 = 0;
                            while (true) {
                                var5 = var4;
                                if (var3 >= lnSheetExcelBar.getChildCount() - 1) {
                                    break;
                                }
                                SheetTab var6 = (SheetTab) lnSheetExcelBar.getChildAt(var3);
                                if (var2.getX() > (float) var6.getLeft() && var2.getX() < (float) var6.getRight()) {
                                    var5 = var3;
                                    break;
                                }
                                ++var3;
                            }
                        }

                        this.a(var5);
                        return true;
                    }
                });

                var8.setOnClickDelete(new OnClickListener() {
                    public void onClick(View var1) {
                        SheetTab var2 = (SheetTab) var1;
                        String var6 = var2.getText();
                        final int var3x = var2.getSheetNumber();
                        Activity var4 = var3;
                        String var5 = NUIDocViewXls.this.getContext().getString(string.sodk_editor_delete_worksheet_q);
                        StringBuilder var7 = new StringBuilder();
                        var7.append(NUIDocViewXls.this.getContext().getString(string.sodk_editor_do_you_want_to_delete_the_sheet));
                        var7.append(var6);
                        var7.append("\" ?");
                        Utilities.yesNoMessage(var4, var5, var7.toString(), NUIDocViewXls.this.getContext().getString(string.sodk_editor_yes), NUIDocViewXls.this.getContext().getString(string.sodk_editor_no), new Runnable() {
                            public void run() {
                                NUIDocViewXls.this.deletingPage = true;
                                NUIDocViewXls.this.getDoc().clearSelection();
                                NUIDocViewXls.this.getDoc().deletePage(var3x);
                            }
                        }, (Runnable) null);
                    }
                });
            }

            lnSheetExcelBar.addView(var8);
            if (this.getCurrentSheet() == var7) {
                var5 = true;
            }

            var8.setSelected(var5);
            ++var4;
        }
    }

    private void d() {
        ListPopupWindow var1 = this.currentFormulaPopup;
        if (var1 != null) {
            String var2 = this.currentFormulaCategory;
            var1.dismiss();
            this.a(var2);
        }

    }

    private void e() {
        SOSelectionTableRange var1 = this.getDoc().selectionTableRange();
        if (var1 != null && var1.rowCount() == 1 && var1.columnCount() == 1) {
            String var2 = this.getDoc().getSelectionAsText();
            ((DocExcelView) this.getDocView()).setEditText(var2);
        }

    }

    private int getCurrentSheet() {
        DocExcelView var1 = (DocExcelView) this.getDocView();
        return var1 != null ? var1.getCurrentSheet() : 0;
    }

    private void setCurrentSheet(int var1) {
        if (var1 != this.getCurrentSheet()) {
            DocExcelView var2 = (DocExcelView) this.getDocView();
            var2.copyEditTextToCell();
            this.getDoc().clearSelection();
            var2.setEditText("");
        }

        LinearLayout var7 = (LinearLayout) this.findViewById(id.excel_sheets_bar);
        int var3 = this.getDoc().r();

        for (int var4 = 0; var4 < var3; ++var4) {
            View var5 = var7.getChildAt(var4);
            if (var5 != null) {
                boolean var6;
                if (var4 == var1) {
                    var6 = true;
                } else {
                    var6 = false;
                }

                var5.setSelected(var6);
                if (var3 == 1) {
                    ((SheetTab) var5).showXView(false);
                }
            }
        }

        ((DocExcelView) this.getDocView()).setCurrentSheet(var1);
        this.onSelectionChanged();
        this.setPageNumberText();
    }

    protected void afterFirstLayoutComplete() {
        super.afterFirstLayoutComplete();
        this.mInsertRowAboveButton = (LinearLayout) this.createToolbarButton(id.insert_row_above_button);
        this.mInsertRowBelowButton = (LinearLayout) this.createToolbarButton(id.insert_row_below_button);
        this.mInsertColumnLeftButton = (LinearLayout) this.createToolbarButton(id.insert_column_left_button);
        this.mInsertColumnRightButton = (LinearLayout) this.createToolbarButton(id.insert_column_right_button);
        this.mDeleteRowButton = (LinearLayout) this.createToolbarButton(id.delete_row_button);
        this.mDeleteColumnButton = (LinearLayout) this.createToolbarButton(id.delete_column_button);
        this.mMergeCellsButton = (LinearLayout) this.createToolbarButton(id.merge_cells_button);
        this.mNumberFormatButton = (LinearLayout) this.createToolbarButton(id.number_format_button);
        this.mIncreaseCellWidthButton = (LinearLayout) this.createToolbarButton(id.cell_width_up_button);
        this.mDecreaseCellWidthButton = (LinearLayout) this.createToolbarButton(id.cell_width_down_button);
        this.mIncreaseCellHeightButton = (LinearLayout) this.createToolbarButton(id.cell_height_up_button);
        this.mDecreaseCellHeightButton = (LinearLayout) this.createToolbarButton(id.cell_height_down_button);
        this.mCellWidthLabel = this.findViewById(id.cell_width_label);
        this.mCellHeightLabel = this.findViewById(id.cell_height_label);
//        setAllSameSize(new LinearLayout[]{this.mMergeCellsButton, this.mNumberFormatButton});
        this.mFormulaSumButton = (LinearLayout) this.createToolbarButton(id.formula_sum);
        this.mFormulaDateTimeButton = (LinearLayout) this.createToolbarButton(id.formula_datetime);
        this.mFormulaEngineeringButton = (LinearLayout) this.createToolbarButton(id.formula_engineering);
        this.mFormulaFinancialButton = (LinearLayout) this.createToolbarButton(id.formula_financial);
        this.mFormulaInformationButton = (LinearLayout) this.createToolbarButton(id.formula_information);
        this.mFormulaLogicalButton = (LinearLayout) this.createToolbarButton(id.formula_logical);
        this.mFormulaLookupButton = (LinearLayout) this.createToolbarButton(id.formula_lookup);
        this.mFormulaMathsButton = (LinearLayout) this.createToolbarButton(id.formula_maths);
        this.mFormulaStatisticalButton = (LinearLayout) this.createToolbarButton(id.formula_statistical);
        this.mFormulaTextButton = (LinearLayout) this.createToolbarButton(id.formula_text);
//        setAllSameSize(new LinearLayout[]{this.mFormulaSumButton, this.mFormulaDateTimeButton, this.mFormulaEngineeringButton, this.mFormulaFinancialButton, this.mFormulaInformationButton, this.mFormulaLogicalButton, this.mFormulaLookupButton, this.mFormulaMathsButton, this.mFormulaStatisticalButton, var1});
        this.mFXButton = (SOTextView) this.createToolbarButton(id.fx_button);
        this.a();
    }

    public void clickSheetButton(int var1, boolean var2) {
        LinearLayout var3 = (LinearLayout) this.findViewById(id.excel_sheets_bar);
        if (var3 != null) {
            SheetTab var4 = (SheetTab) var3.getChildAt(var1);
            if (var4 != null) {
                var4.performClick();
            }
        }

        if (!var2) {
            this.setSearchStart();
        }
    }

    protected void createEditButtons2() {
        this.mAlignOptionsButton = (LinearLayout) this.createToolbarButton(id.align_options_button);
    }

    protected void createInputView() {
        Log.d("AAAA", "AAAA");
    }

    protected void createInsertButtons() {
    }

    protected DocView createMainView(Activity var1) {
        DocExcelView var2 = new DocExcelView(var1);
        if (Utilities.isPhoneDevice(var1)) {
            var2.setScale(1.5F);
        }

        return var2;
    }

    protected void createPagesButtons() {
    }

    protected void createReviewButtons() {
    }

    public void doCut() {
        super.doCut();
        ((DocExcelView) this.getDocView()).setEditText("");
    }

    public void doPaste() {
        super.doPaste();
        String var1 = this.getDoc().getSelectionAsText();
        ((DocExcelView) this.getDocView()).setEditText(var1);
    }

    public void doRedo() {
        super.doRedo();
        this.e();
        this.undoredo = true;
    }

    public void doUndo() {
        super.doUndo();
        this.e();
        this.undoredo = true;
    }

    protected void focusInputView() {
    }

    public int getBorderColor() {
        return viewx.core.content.a.c(this.getContext(), color.sodk_editor_header_xls_color);
    }

    protected int getLayoutId() {
        return layout.sodk_editor_excel_document;
    }

    protected String getPageNumberText() {
        return String.format(this.getContext().getString(string.sodk_editor_sheet_d_of_d), this.getCurrentSheet() + 1, this.getPageCount());
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
                NUIDocView.TabData tabData5 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_formulas), R.id.formulasTab, R.layout.sodk_editor_tab_right, 0);
                tabDataArr5[4] = tabData5;
            } else {
                NUIDocView.TabData[] tabDataArr6 = this.mTabs;
                NUIDocView.TabData tabData6 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_file), R.id.fileTab, R.layout.sodk_editor_tab_one, 0);
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
                NUIDocView.TabData tabData10 = new NUIDocView.TabData(getContext().getString(R.string.sodk_editor_tab_formulas), R.id.formulasTab, R.layout.sodk_editor_tab_right, 8);
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
            var2 = color.sodk_editor_header_xls_color;
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
            var2 = color.sodk_editor_header_xls_color;
        }

        return viewx.core.content.a.c(var1, var2);
    }

    protected void handleStartPage() {
        if (this.getStartPage() > 0 && this.getPageCount() >= this.getStartPage()) {
            this.setCurrentSheet(this.getStartPage() - 1);
            this.setStartPage(0);
        }

    }

    public void onAlignOptionsButton(View var1) {
        (new AlignmentDialog(this.getContext(), this.getDoc(), var1)).show();
    }

    public void onClick(View var1) {
        super.onClick(var1);
        if (var1 == this.mAlignOptionsButton) {
            this.onAlignOptionsButton(var1);
        }

        if (var1 == this.mInsertRowAboveButton) {
            this.onInsertRowAbove(var1);
        }

        if (var1 == this.mInsertRowBelowButton) {
            this.onInsertRowBelow(var1);
        }

        if (var1 == this.mInsertColumnLeftButton) {
            this.onInsertColumnLeft(var1);
        }

        if (var1 == this.mInsertColumnRightButton) {
            this.onInsertColumnRight(var1);
        }

        if (var1 == this.mDeleteRowButton) {
            this.onDeleteRow(var1);
        }

        if (var1 == this.mDeleteColumnButton) {
            this.onDeleteColumn(var1);
        }

        if (var1 == this.mMergeCellsButton) {
            this.onMergeCellsButton(var1);
        }

        if (var1 == this.mNumberFormatButton) {
            this.onNumberFormatButton(var1);
        }

        if (var1 == this.mFormulaSumButton) {
            this.onFormulaSumButton(var1);
        }

        if (var1 == this.mFormulaDateTimeButton) {
            this.onFormulaDateTimeButton(var1);
        }

        if (var1 == this.mFormulaEngineeringButton) {
            this.onFormulaEngineeringButton(var1);
        }

        if (var1 == this.mFormulaFinancialButton) {
            this.onFormulaFinancialButton(var1);
        }

        if (var1 == this.mFormulaInformationButton) {
            this.onFormulaInformationButton(var1);
        }

        if (var1 == this.mFormulaLogicalButton) {
            this.onFormulaLogicalButton(var1);
        }

        if (var1 == this.mFormulaLookupButton) {
            this.onFormulaLookupButton(var1);
        }

        if (var1 == this.mFormulaMathsButton) {
            this.onFormulaMathsButton(var1);
        }

        if (var1 == this.mFormulaStatisticalButton) {
            this.onFormulaStatisticalButton(var1);
        }

        if (var1 == this.mFormulaTextButton) {
            this.onFormulaTextButton(var1);
        }

        if (var1 == this.mFXButton) {
            this.onClickFunctionButton(var1);
        }

    }

    public void onClickFunctionButton(View var1) {
        Utilities.hideKeyboard(this.getContext());
        View var2 = inflate(this.getContext(), layout.sodk_editor_formula_categories, (ViewGroup) null);
        GridView var3 = (GridView) var2.findViewById(id.grid);
        final ChooseFormulaCategoryAdapter var4 = new ChooseFormulaCategoryAdapter(this.activity());
        var3.setAdapter(var4);
        final NUIPopupWindow var5 = new NUIPopupWindow(var2, -2, -2);
        var5.setFocusable(true);
        var3.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> var1, View var2, int var3, long var4x) {
                var5.dismiss();
                String var6 = var4.getItem(var3);
                NUIDocViewXls.this.a(var6);
            }
        });
        var5.showAsDropDown(var1, 30, 30);
    }

    public void onDeleteColumn(View var1) {
        ((DocExcelView) this.getDocView()).deleteSelectedColumns();
    }

    public void onDeleteRow(View var1) {
        ((DocExcelView) this.getDocView()).deleteSelectedRows();
    }

    protected void onDocCompleted() {
        label30:
        {
            int var1;
            label35:
            {
                super.onDocCompleted();
                this.setPageCount(this.getPageCount());
                this.c();
                if (this.addingPage) {
                    var1 = this.getDoc().r();
                } else if (this.deletingPage) {
                    if (this.getCurrentSheet() == 0) {
                        this.setCurrentSheet(0);
                        break label30;
                    }

                    var1 = this.getCurrentSheet();
                } else {
                    var1 = this.getPageCount();
                    if (var1 <= 0 || this.getCurrentSheet() < var1) {
                        var1 = this.getCurrentSheet();
                        break label35;
                    }
                }

                --var1;
            }

            this.setCurrentSheet(var1);
        }

        this.addingPage = false;
        this.deletingPage = false;
    }

    public void onFormulaDateTimeButton(View var1) {
        this.a("Date and Time");
    }

    public void onFormulaEngineeringButton(View var1) {
        this.a("Engineering");
    }

    public void onFormulaFinancialButton(View var1) {
        this.a("Financial");
    }

    public void onFormulaInformationButton(View var1) {
        this.a("Information");
    }

    public void onFormulaLogicalButton(View var1) {
        this.a("Logical");
    }

    public void onFormulaLookupButton(View var1) {
        this.a("Lookup");
    }

    public void onFormulaMathsButton(View var1) {
        this.a("Maths");
    }

    public void onFormulaStatisticalButton(View var1) {
        this.a("Statistical");
    }

    public void onFormulaSumButton(View var1) {
        DocExcelView var2;
        String var5;
        label11:
        {
            var2 = (DocExcelView) this.getDocView();
            String var3 = this.getContext().getString(string.sodk_editor_autosum_text);
            String var4 = var2.getEditText();
            if (var4 != null) {
                var5 = var3;
                if (!var4.isEmpty()) {
                    break label11;
                }
            }

            StringBuilder var6 = new StringBuilder();
            var6.append("=");
            var6.append(var3);
            var5 = var6.toString();
        }

        var2.insertEditText(var5);
        this.getDoc().setSelectionText(var2.getEditText());
    }

    public void onFormulaTextButton(View var1) {
        this.a("Text");
    }

    protected void onFullScreenHide() {
        this.findViewById(id.fx_bar).setVisibility(GONE);
        super.onFullScreenHide();
    }

    public void onInsertColumnLeft(View var1) {
        this.getDoc().addColumnsLeft();
    }

    public void onInsertColumnRight(View var1) {
        this.getDoc().addColumnsRight();
    }

    public void onInsertRowAbove(View var1) {
        this.getDoc().addRowsAbove();
    }

    public void onInsertRowBelow(View var1) {
        this.getDoc().addRowsBelow();
    }

    public void onMergeCellsButton(View var1) {
        boolean var2 = this.getDoc().getTableCellsMerged();
        this.getDoc().setTableCellsMerged(var2 ^ true);
        if (!var2) {
            String var3 = this.getDoc().getSelectionAsText();
            ((DocExcelView) this.getDocView()).setEditText(var3);
        }

    }

    public void onNumberFormatButton(View var1) {
        Utilities.hideKeyboard(this.getContext());
        View var2 = inflate(this.getContext(), layout.sodk_editor_number_formats, (ViewGroup) null);
        GridView var3 = (GridView) var2.findViewById(id.grid);
        byte var4;
        if (Utilities.isPhoneDevice(this.getContext()) && !Utilities.isLandscapePhone(this.getContext())) {
            var4 = 1;
        } else {
            var4 = 2;
        }

        var3.setNumColumns(var4);
        var3.setAdapter(new ChooseNumberFormatAdapter(this.activity(), var4));
        final NUIPopupWindow var5 = new NUIPopupWindow(var2, -2, -2);
        var5.setFocusable(true);
        var3.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> var1, View var2, int var3, long var4) {
                var5.dismiss();
                Utilities.hideKeyboard(NUIDocViewXls.this.getContext());
                switch (var3) {
                    case 0:
                        NUIDocViewXls.this.getDoc().setSelectedCellFormat("General");
                        break;
                    case 1:
                        EditNumberFormatDateTimeLib.show(NUIDocViewXls.this.activity(), NUIDocViewXls.this.mNumberFormatButton, NUIDocViewXls.this.getDoc());
                        break;
                    case 2:
                        EditNumberFormatNumberLib.show(NUIDocViewXls.this.activity(), NUIDocViewXls.this.mNumberFormatButton, NUIDocViewXls.this.getDoc());
                        break;
                    case 3:
                        EditNumberFormatFractionLib.show(NUIDocViewXls.this.activity(), NUIDocViewXls.this.mNumberFormatButton, NUIDocViewXls.this.getDoc());
                        break;
                    case 4:
                        EditNumberFormatCurrencyLib.show(NUIDocViewXls.this.activity(), NUIDocViewXls.this.mNumberFormatButton, NUIDocViewXls.this.getDoc());
                        break;
                    case 5:
                        EditNumberFormatPercentageLib.show(NUIDocViewXls.this.activity(), NUIDocViewXls.this.mNumberFormatButton, NUIDocViewXls.this.getDoc());
                        break;
                    case 6:
                        EditNumberFormatAccountingLib.show(NUIDocViewXls.this.activity(), NUIDocViewXls.this.mNumberFormatButton, NUIDocViewXls.this.getDoc());
                        break;
                    case 7:
                        EditNumberFormatCustomLib.show(NUIDocViewXls.this.activity(), NUIDocViewXls.this.mNumberFormatButton, NUIDocViewXls.this.getDoc());
                }

            }
        });
        var5.showAsDropDown(var1, 30, 30);
    }

    protected void onOrientationChange() {
        super.onOrientationChange();
        this.d();
    }

    protected void onPageLoaded(int var1) {
        this.c();
        super.onPageLoaded(var1);
    }

    public void onSelectionChanged() {
        super.onSelectionChanged();
        this.setPageCount(this.getPageCount());
        if (this.undoredo) {
            this.c();
            this.undoredo = false;
        }

    }

    public void onShowKeyboard(final boolean z) {
        if (isActivityActive() && getPageCount() > 0) {
            this.keyboardShown = z;
            onShowKeyboardPreventPush(z);
            DocView docView = getDocView();
            if (docView != null) {
                docView.onShowKeyboard(z);
            }
            if (isLandscapePhone()) {
                showUI(!z);
                if (z) {
                    requestLayout();
                    final ViewTreeObserver viewTreeObserver = getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            viewTreeObserver.removeOnGlobalLayoutListener(this);
                            ((DocExcelView) NUIDocViewXls.this.getDocView()).scrollSelectedCellAboveKeyboard();
                        }
                    });
                }
            } else if (z) {
                ((DocExcelView) getDocView()).scrollSelectedCellAboveKeyboard();
            }
            d();
            final ViewTreeObserver viewTreeObserver2 = getViewTreeObserver();
            viewTreeObserver2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    viewTreeObserver2.removeOnGlobalLayoutListener(this);
                    DocView docView = NUIDocViewXls.this.getDocView();
                    if (docView != null) {
                        docView.onShowKeyboard(z);
                    }
                    if (NUIDocViewXls.this.mShowHideKeyboardRunnable != null) {
                        NUIDocViewXls.this.mShowHideKeyboardRunnable.run();
                        NUIDocViewXls.this.mShowHideKeyboardRunnable = null;
                    }
                    NUIDocViewXls.this.layoutNow();
                }
            });
        }
    }

    protected boolean shouldConfigureSaveAsPDFButton() {
        return FirebaseRemoteConfigUtil.Companion.getInstance().isAllowSaveExcelToPDF();
        // turn off Excel to PDF
    }

    public void preSave() {
        ((DocExcelView) this.getDocView()).copyEditTextToCell();
    }

    protected void prepareToGoBack() {
        DocView var1 = this.getDocView();
        if (var1 != null && this.getDoc() != null && ((DocExcelView) var1).copyEditTextToCell()) {
            SODocSession var2 = this.getSession();
            if (var2 != null) {
                SOFileState var3 = var2.getFileState();
                if (var3 != null) {
                    var3.setHasChanges(true);
                }
            }
        }

    }

    protected void resetInputView() {
    }

    public void setSearchStart() {
        int var1 = this.getCurrentSheet();
        if (var1 >= 0) {
            this.getDoc().a(var1, 0.0F, 0.0F);
        }

    }

    public boolean showKeyboard() {
        return false;
    }

    public void showUI(boolean var1) {
        super.showUI(var1);
        if (this.mConfigOptions.z() && var1 && this.mConfigOptions.b()) {
            this.findViewById(id.fx_bar).setVisibility(VISIBLE);
        }
    }

    protected void updateEditUIAppearance() {
        SOSelectionLimits var1 = this.getDocView().getSelectionLimits();
        boolean var2 = true;
        boolean var4;
        boolean var5;
        boolean var6;
        if (var1 != null) {
            boolean var3 = var1.getIsActive();
            if (var3 && !var1.getIsCaret()) {
                var4 = true;
            } else {
                var4 = false;
            }

            var5 = var3;
            var6 = var4;
            if (var3) {
                var1.getIsCaret();
                var5 = var3;
                var6 = var4;
            }
        } else {
            var5 = false;
            var6 = false;
        }

        SODoc var8 = this.mSession.getDoc();
//        this.mStyleBoldButton.setEnabled(var6);
        enableView(this.mStyleBoldButton, var6);
        LinearLayout var7 = this.mStyleBoldButton;
        if (var6 && var8.getSelectionIsBold()) {
            var4 = true;
        } else {
            var4 = false;
        }

        var7.setSelected(var4);
        enableView(this.mStyleItalicButton, var6);
//        this.mStyleItalicButton.setEnabled(var6);
        var7 = this.mStyleItalicButton;
        if (var6 && var8.getSelectionIsItalic()) {
            var4 = true;
        } else {
            var4 = false;
        }

        var7.setSelected(var4);
//        this.mStyleUnderlineButton.setEnabled(var6);
        enableView(this.mStyleUnderlineButton, var6);
        var7 = this.mStyleUnderlineButton;
        if (var6 && var8.getSelectionIsUnderlined()) {
            var4 = true;
        } else {
            var4 = false;
        }

        var7.setSelected(var4);
//        this.mStyleLinethroughButton.setEnabled(var6);
        enableView(this.mStyleLinethroughButton, var6);
        var7 = this.mStyleLinethroughButton;
        if (var6 && var8.getSelectionIsLinethrough()) {
            var4 = var2;
        } else {
            var4 = false;
        }

        var7.setSelected(var4);
        enableView(this.mAlignOptionsButton, (var5));
    }

    private void enableView(View view, boolean enable) {
        if (enable) {
            view.setAlpha(1f);
        } else {
            view.setAlpha(0.5f);
        }
        view.setEnabled(enable);
    }

    protected void updateReviewUIAppearance() {
    }

    protected void updateUIAppearance() {
        super.updateUIAppearance();
        SOSelectionLimits var1 = this.getDocView().getSelectionLimits();
        boolean var2;
        boolean var3;
        boolean var4;
        if (var1 != null) {
            var2 = var1.getIsActive();
            if (var2 && !var1.getIsCaret()) {
                var3 = true;
            } else {
                var3 = false;
            }

            if (var2 && var1.getIsCaret()) {
                var4 = true;
            } else {
                var4 = false;
            }
        } else {
            var4 = false;
            var3 = false;
        }

        LinearLayout var6 = this.mInsertRowAboveButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mInsertRowBelowButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

        enableView(var6, var2);
//        var6.setEnabled(var2);
        var6 = this.mInsertColumnLeftButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mInsertColumnRightButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mDeleteRowButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mDeleteColumnButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mAlignOptionsButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

        enableView(var6, var2);
//        var6.setEnabled(var2);
        var6 = this.mNumberFormatButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }
        enableView(var6, var2);
//        var6.setEnabled(var2);
        var6 = this.mFormulaSumButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mFormulaDateTimeButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mFormulaEngineeringButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mFormulaFinancialButton;
        if (!var4 && !var3) {
            var2 = false;
        } else {
            var2 = true;
        }

        enableView(var6, var2);
//        var6.setEnabled(var2);
        var6 = this.mFormulaInformationButton;
        var2 = var4 || var3;

        enableView(var6, var2);
//        var6.setEnabled(var2);
        var6 = this.mFormulaLogicalButton;
        var2 = var4 || var3;

        enableView(var6, var2);
//        var6.setEnabled(var2);
        var6 = this.mFormulaLookupButton;
        var2 = var4 || var3;

        enableView(var6, var2);
//        var6.setEnabled(var2);
        var6 = this.mFormulaMathsButton;
        var2 = var4 || var3;

        enableView(var6, var2);
//        var6.setEnabled(var2);
        var6 = this.mFormulaStatisticalButton;
        var2 = var4 || var3;

        enableView(var6, var2);
//        var6.setEnabled(var2);
        var6 = this.mFormulaTextButton;
        var2 = var4 || var3;

        enableView(var6, var2);
//        var6.setEnabled(var2);
        ((DocExcelView) this.getDocView()).onSelectionChanged();
        float var5 = this.getDoc().getSelectedColumnWidth();
        if (var5 > 0.0F) {
            this.a(this.mCellWidthBox, var5 * 2.54F);
        }

        SOEditText var7 = this.mCellWidthBox;
        var2 = var4 || var3;

//        var7.setEnabled(var2);
        enableView(var7, var2);
        var6 = this.mIncreaseCellWidthButton;
        var2 = var4 || var3;

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mDecreaseCellWidthButton;
        var2 = var4 || var3;

//        var6.setEnabled(var2);
        enableView(var6, var2);
        SOTextView var8 = this.mCellWidthLabel;
        var2 = var4 || var3;

        this.a(var8, var2);
        var5 = this.getDoc().getSelectedRowHeight();
        if (var5 > 0.0F) {
            this.a(this.mCellHeightBox, var5 * 2.54F);
        }

        var7 = this.mCellHeightBox;
        var2 = var4 || var3;

//        var7.setEnabled(var2);
        enableView(var7, var2);
        var6 = this.mIncreaseCellHeightButton;
        var2 = var4 || var3;

//        var6.setEnabled(var2);
        enableView(var6, var2);
        var6 = this.mDecreaseCellHeightButton;
        var2 = var4 || var3;

        enableView(var6, var2);
//        var6.setEnabled(var2);
        var8 = this.mCellHeightLabel;
        var2 = var4 || var3;

        this.a(var8, var2);
        var2 = this.getDoc().getTableCellsMerged();
        SOSelectionTableRange var9 = this.getDoc().selectionTableRange();

        enableView(this.mMergeCellsButton, var3 && (var2 || (var9 != null && (var9.columnCount() >= 2 || var9.rowCount() >= 2))));

//        this.mMergeCellsButton.setEnabled(var3 && (var2 || (var9 != null && (var9.columnCount() >= 2 || var9.rowCount() >= 2))));

    }

    protected boolean usePagesView() {
        return false;
    }
}
