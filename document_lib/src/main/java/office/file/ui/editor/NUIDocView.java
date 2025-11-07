package office.file.ui.editor;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.print.PrintManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.aigestudio.wheelpicker.WheelPicker;
import com.artifex.solib.ConfigOptions;
import com.artifex.solib.SOBitmap;
import com.artifex.solib.SODoc;
import com.artifex.solib.SODocSaveListener;
import com.artifex.solib.SOLib;
import com.artifex.solib.SOSelectionLimits;
import com.artifex.solib.j;
import com.artifex.solib.k;
import com.artifex.solib.p;
import com.ezteam.baseproject.dialog.GuideEditDialog;
import com.ezteam.baseproject.dialog.GuideStep;
import com.ezteam.baseproject.print.PdfDocumentAdapter;
import com.ezteam.baseproject.utils.FileUtil;
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil;
import com.ezteam.baseproject.utils.IAPUtils;
import com.ezteam.baseproject.utils.PreferencesUtils;
import com.ezteam.baseproject.utils.PresKey;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.tabs.TabLayout;
import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.util.Admob;
import com.nlbn.ads.util.AppOpenManager;
import com.nlbn.ads.util.ConsentHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import office.file.ui.ExportFileActivity;
import office.file.ui.IdController;
import office.file.ui.OnChangeScreenReward;
import office.file.ui.R;
import office.file.ui.editor.AuthorDialog.AuthorDialogListener;
import office.file.ui.editor.NUIView.OnDoneListener;
import office.file.ui.editor.SODocSession.SODocSessionLoadListener;
import office.file.ui.extension.ViewUtilsKt;
import office.file.ui.utils.MyUtils;
import office.file.ui.utils.PdfUtils;

public class NUIDocView extends FrameLayout implements OnClickListener, OnTabChangeListener, DocViewHost, OnChangeScreenReward {
    private static final int MAX_FONT_SIZE = 72;
    private static final int MIN_FONT_SIZE = 6;
    private static final int ORIENTATION_LANDSCAPE = 2;
    private static final int ORIENTATION_PORTAIT = 1;
    public static int OVERSIZE_MARGIN;
    public static final int OVERSIZE_PERCENT = 20;
    private static final String STORE_NAME = "general";
    private static final int VERSION_TAP_INTERVAL = 500;
    private static final String WAS_ANIM_KEY = "scroll_was_animated";
    private static NUIDocView mCurrentNUIDocView;
    OnDoneListener a = null;
    private SOBitmap[] bitmaps = new SOBitmap[]{null, null};
    private int keyboardHeight = 0;
    protected boolean keyboardShown = false;
    private long lastTypingTime = 0L;
    protected PageAdapter mAdapter;
    private LinearLayout mAlignCenterButton;
    private LinearLayout mAlignJustifyButton;
    private LinearLayout mAlignLeftButton;
    private LinearLayout mAlignRightButton;
    private ArrayList<String> mAllTabHostTabs = new ArrayList();
    protected ImageView mBackButton;
    protected ConfigOptions mConfigOptions = null;
    private LinearLayout mCopyButton;
    private LinearLayout mCopyButton2;
    private View mCopyDivider;
    public int mCurrentPageNum = 0;
    private String mCurrentTab = "";
    private String mCustomDocdata;
    private ToolbarButton mCustomSaveButton;
    private LinearLayout mCutButton;
    private SODataLeakHandlers mDataLeakHandlers;
    private final String mDebugTag = "NUIDocView";
    private View mDecorView = null;
    protected LinearLayout mDecreaseIndentButton;
    private ArrayList<String> mDeleteOnClose = new ArrayList();
    private DocListPagesView mDocPageListView;
    private String mDocUserPath;
    private DocView mDocView;
    private k mDocumentLib;
    private Boolean mEndSessionSilent;
    private SOFileDatabase mFileDatabase;
    private SOFileState mFileState;
    protected boolean mFinished = false;
    private LinearLayout mFirstPageButton;
    private LinearLayout mFontBackgroundButton;
    private LinearLayout mFontColorButton;
    private LinearLayout mFontDownButton;
    private LinearLayout mLayoutFont;
    private TextView mFontNameText;
    private SOTextView mFontSizeText;
    private LinearLayout mFontUpButton;
    private SOTextView mFooter;
    private View mFooterLead;
    private SOTextView mFooterText;
    private boolean mForceOrientationChange = false;
    private boolean mForceReload = false;
    private boolean mForceReloadAtResume = false;
    private String mForeignData = null;
    protected boolean mFullscreen = false;
    private ImageView mFullscreenButton;
    protected Toast mFullscreenToast;
    protected LinearLayout mIncreaseIndentButton;
    private InputView mInputView = null;
    protected LinearLayout mInsertImageButton;
    protected LinearLayout mInsertPhotoButton;
    private boolean mIsActivityActive = false;
    private boolean mIsComposing = false;
    private boolean mIsSearching = false;
    protected boolean mIsSession = false;
    private boolean mIsTemplate = false;
    private boolean mIsWaiting = false;
    private int mLastOrientation = 0;
    private LinearLayout mLastPageButton;
    protected LinearLayout mListBulletsButton;
    protected LinearLayout mListNumbersButton;
    private ListPopupWindow mListPopupWindow;
    protected LinearLayout mOpenInButton;
    protected LinearLayout mOpenPdfInButton;
    protected int mPageCount;
    private LinearLayout mPasteButton;
    private int mPrevKeyboard = -1;
    protected LinearLayout mPrintButton;
    private ProgressDialog mProgressDialog;
    private Handler mProgressHandler = null;
    private boolean mProgressIsScheduled = false;
    protected LinearLayout mProtectButton;
    protected ImageView mRedoButton;
    private LinearLayout mReflowButton;
    protected LinearLayout mSaveAsButton;
    protected LinearLayout mSaveButton;
    protected LinearLayout mSavePdfButton;
    private ImageView mSearchButton;
    private ImageView mSearchClear;
    private int mSearchCounter = 0;
    private Handler mSearchHandler = null;
    private p mSearchListener = null;
    private LinearLayout mSearchNextButton;
    private LinearLayout mSearchPreviousButton;
    private LinearLayout mSearchCloseButton;
    private ProgressDialog mSearchProgressDialog = null;
    private SOEditText mSearchText;
    protected SODocSession mSession;
    private LinearLayout mShareButton;
    protected Runnable mShowHideKeyboardRunnable = null;
    private boolean mShowUI = true;
    private int mStartPage = 0;
    protected Uri mStartUri = null;
    private boolean mStarted = false;
    protected SOFileState mState = null;
    protected LinearLayout mStyleBoldButton;
    protected LinearLayout mStyleItalicButton;
    protected LinearLayout mStyleLinethroughButton;
    protected LinearLayout mStyleUnderlineButton;
    protected LinearLayout mStyleTextColorButton;
    protected LinearLayout mStyleTextHighlightButton;

    protected NUIDocView.TabData[] mTabs = null;
    private final int mTabsInsertTabIdx = 2;
    protected ImageView mUndoButton;
    private long mVersionLastTapTime = 0L;
    private int mVersionTapCount = 0;
    private TabHost tabHost = null;
    protected Map<String, View> tabMap = new HashMap();
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private TextView lnMenuFile;
    private TextView lnMenuTypo;
    private TextView lnMenuEdit;
    private TextView lnMenuInsert;
    private TextView lnMenuPage;
    private TextView lnMenuAnnotate;
    private TextView lnMenuSlide;
    private TextView lnMenuFormat;
    private TextView lnMenuFormulas;

    private LinearLayout lnHeader;
    private LinearLayout lnEditor;
    private LinearLayout lnRollback;
    protected LinearLayout mFileTab;
    protected LinearLayout mEditTab;
    protected LinearLayout mHomeTab;
    protected LinearLayout mInsertTab;
    protected LinearLayout mFormatTab;
    protected LinearLayout mFormulasTab;
    protected LinearLayout mSlideTab;
    protected LinearLayout mPagesTab;
    protected LinearLayout mAnnotateTab;
    protected LinearLayout mSearchTab;
    protected LinearLayout mReviewTab;
    protected LinearLayout mRedactTab;

    private LinearLayout lnBottom;
    private LinearLayout lnMenuTextColor;
    private LinearLayout lnMenuTextHighlight;
    private ImageView mColorRed;
    private ImageView mColorOrange;
    private ImageView mColorYellow;
    private ImageView mColorGreen;
    private ImageView mColorBlue;
    private ImageView mColorPurple;
    private ImageView mColorBlack;
    private ImageView mColorWhite;

    private ImageView mHighlightRed;
    private ImageView mHighlightOrange;
    private ImageView mHighlightYellow;
    private ImageView mHighlightGreen;
    private ImageView mHighlightBlue;
    private ImageView mHighlightPurple;
    private ImageView mHighlightBlack;
    private ImageView mHighlightWhite;


    private ImageView mPremiumSmallIcon;
    private ImageView mPremiumSmallIcon2;
    private ImageView mPremiumSmallIcon3;

    private ImageView mButtonKeyboard;
    private ImageView mButtonOutFullScreen;

    private LinearLayout mFormulasButton;
    private LinearLayout lnFormulas;

    private int currentTabId = 0;

    public NUIDocView(Context var1) {
        super(var1);
        this.initView(var1);
    }

    public NUIDocView(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.initView(var1);
    }

    public NUIDocView(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.initView(var1);
    }

    private void A() {
        DocView var1 = this.getDocView();
        var1.smoothScrollBy(0, var1.getHeight() * 9 / 10, 400);
    }

    private void B() {
        DocView var1 = this.getDocView();
        var1.smoothScrollBy(0, -var1.getHeight() * 9 / 10, 400);
    }

    private void C() {
        DocView var1 = this.getDocView();
        var1.smoothScrollBy(0, var1.getHeight() / 20, 100);
    }

    private void D() {
        DocView var1 = this.getDocView();
        var1.smoothScrollBy(0, -var1.getHeight() / 20, 100);
    }

    private boolean E() {
        Object var1 = Utilities.getPreferencesObject(this.getContext(), "general");
        if (var1 != null) {
            String var2 = Utilities.getStringPreference(var1, WAS_ANIM_KEY, "TRUE");
            if (var2 != null && !var2.equals("TRUE")) {
                return false;
            }
        }

        return true;
    }

    private void F() {
        Object var1 = Utilities.getPreferencesObject(this.getContext(), "general");
        if (var1 != null) {
            Utilities.setStringPreference(var1, WAS_ANIM_KEY, "TRUE");
        }

    }

    private int highlightView(ListAdapter var1) {
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
            var10 = var6.getMeasuredWidth();
            var9 = var8;
            if (var10 > var8) {
                var9 = var10;
            }

            ++var2;
            var8 = var9;
            var9 = var11;
        }

        return var8;
    }

    private NUIDocView.TabData highlightView(String var1) {
        if (this.mTabs != null) {
            int var2 = 0;

            while (true) {
                NUIDocView.TabData[] var3 = this.mTabs;
                if (var2 >= var3.length) {
                    break;
                }

                NUIDocView.TabData var4 = var3[var2];
                if (var1.equals(var4.name)) {
                    return var4;
                }

                ++var2;
            }
        }

        return null;
    }

    private void highlightView() {
        this.b();
        this.setDocViewBitmap();
    }

    private void initView(Context context) {
        this.mDecorView = ((Activity) this.getContext()).getWindow().getDecorView();
        com.artifex.solib.a.a(context);
        this.mPrevKeyboard = context.getResources().getConfiguration().keyboard;
        this.mConfigOptions = k.c();


    }

    private List<String> imgList = new ArrayList<>();
    private int currentPos = 0;

    public void doInsert(String path) {
        doInsertImage(path);
        if (currentPos < imgList.size() - 1) {
            currentPos++;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doInsert(imgList.get(currentPos));
                }
            }, 1000);
        }
    }

    private void highlightView(View view, int viewId, float scale) {
        view = view.findViewById(viewId);
        view.setScaleX(scale);
        view.setScaleY(scale);
    }

    private void highlightView(View view, boolean isEnable) {
        if (view == null) return;
        if (isEnable) {
            view.setAlpha(1f);
        } else {
            view.setAlpha(0.5f);
        }
        view.setEnabled(isEnable);

    }

    private void highlightView(Button var1, boolean var2) {
        /*var1.setEnabled(var2);
        int var3 = viewx.core.content.a.c(this.activity(), color.sodk_editor_header_button_enabled_tint);
        if (!var2) {
            var3 = viewx.core.content.a.c(this.activity(), color.sodk_editor_header_button_disabled_tint);
        }

        this.setButtonColor(var1, var3);*/
    }

    private void saveTemplate(final boolean isFinish) {
        if (this.mDataLeakHandlers != null) {
            this.preSaveQuestion(new Runnable() {
                public void run() {
                    String var1x;
                    boolean var10001;
                    try {
                        var1x = NUIDocView.this.mFileState.getUserPath();
                    } catch (Exception var7) {
                        var10001 = false;
                        return;
                    }

                    String var2 = var1x;
                    if (var1x == null) {
                        try {
                            var2 = NUIDocView.this.mFileState.getOpenedPath();
                        } catch (UnsupportedOperationException var6) {
                            var10001 = false;
                            return;
                        }
                    }

                    try {
                        File var8 = new File(var2);
                        SODataLeakHandlers var9 = NUIDocView.this.mDataLeakHandlers;
                        var1x = var8.getName();
                        SODoc var3 = NUIDocView.this.mSession.getDoc();
                        SOSaveAsComplete var4 = new SOSaveAsComplete() {
                            public void onComplete(int var1x, String var2) {
                                if (var1x == 0) {
                                    NUIDocView.this.setFooterText(var2);
                                    NUIDocView.this.mFileState.setUserPath(var2);
                                    if (isFinish) {
                                        NUIDocView.this.prefinish();
                                    }

                                    if (NUIDocView.this.mFinished) {
                                        return;
                                    }

                                    NUIDocView.this.mFileState.setHasChanges(false);
                                    NUIDocView.this.onSelectionChanged();
                                    NUIDocView.this.reloadFile();
                                } else {
                                    NUIDocView.this.mFileState.setUserPath((String) null);
                                }

                                NUIDocView.this.mIsTemplate = NUIDocView.this.mFileState.isTemplate();
                            }
                        };
                        var9.saveAsHandler(var1x, var3, var4);
                    } catch (UnsupportedOperationException var5) {
                        var10001 = false;
                    }
                }
            }, new Runnable() {
                public void run() {
                }
            });
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void b() {
        Point var1 = Utilities.getRealScreenSize(this.activity());
        int var2 = Math.max(var1.x, var1.y);
        int var3 = var2 * 120 / 100;
        OVERSIZE_MARGIN = (var3 - var2) / 2;
        var2 = 0;

        while (true) {
            SOBitmap[] var4 = this.bitmaps;
            if (var2 >= var4.length) {
                return;
            }

            var4[var2] = this.mDocumentLib.a(var3, var3);
            ++var2;
        }
    }

    private void b(View var1, boolean var2) {

    }

    private void b(String var1) {
        NUIDocView.TabData var2 = this.highlightView(var1);
        if (var2 != null) {
            LinearLayout var3 = (LinearLayout) this.findViewById(var2.contentId);
            if (var3 != null && var3.getChildCount() != 0) {
                if (this.E()) {
                    return;
                }

                this.F();
            }

        }
    }

    private void b(final boolean var1) {
        if (this.mSearchHandler == null) {
            this.mSearchHandler = new Handler();
        }

        this.mSearchHandler.post(new Runnable() {
            public void run() {
                SODoc var1x = NUIDocView.this.getDoc();
                if (var1x != null) {
                    if (!NUIDocView.this.mIsSearching) {
                        NUIDocView.this.mIsSearching = true;
                        NUIDocView.this.setSearchCounter();
                        var1x.e(var1);
                        NUIDocView.this.x();
                    }
                }
            }
        });
    }

    private void c() {
        int var1 = this.mDecorView.getHeight();
        int var2 = var1 * 15 / 100;
        Rect var3 = new Rect();
        this.mDecorView.getWindowVisibleDisplayFrame(var3);
        this.keyboardHeight = var1 - var3.bottom;
        Resources var4 = this.getContext().getResources();
        var1 = var4.getIdentifier("config_showNavigationBar", "bool", "android");
        if (var1 > 0 && var4.getBoolean(var1) || Utilities.isEmulator()) {
            var1 = var4.getIdentifier("navigation_bar_height", "dimen", "android");
            if (var1 > 0) {
                var1 = var4.getDimensionPixelSize(var1);
            } else {
                var1 = 0;
            }

            this.keyboardHeight -= var1;
        }

        if (this.keyboardHeight >= var2) {
            if (!this.keyboardShown) {
                if (this.mButtonKeyboard != null) {
                    this.mButtonKeyboard.setVisibility(INVISIBLE);
                }
                this.onShowKeyboard(true);
            }
        } else {
            this.keyboardHeight = 0;
            if (this.keyboardShown) {
                if (this.mButtonKeyboard != null) {
                    this.mButtonKeyboard.setVisibility(VISIBLE);
                }
                this.onShowKeyboard(false);
            }
        }

    }

    public static NUIDocView currentNUIDocView() {
        return mCurrentNUIDocView;
    }

    private void d() {
        this.highlightView((View) this, false);
    }

    private void e() {
        this.mStarted = false;
        final ViewGroup var1 = (ViewGroup) ((LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(this.getLayoutId(), (ViewGroup) null);
        this.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                NUIDocView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (!NUIDocView.this.mStarted) {
                    if (NUIDocView.this.mDocumentLib instanceof SOLib) {
                        ConfigOptions var1x = k.c();
                        ((SOLib) NUIDocView.this.mDocumentLib).setTrackChangesEnabled(var1x.v());
                        ((SOLib) NUIDocView.this.mDocumentLib).setAnimationEnabled(var1x.A());
                    }

                    NUIDocView.this.enforceInitialShowUI(var1);
                    NUIDocView.this.afterFirstLayoutComplete();
                    NUIDocView.this.mStarted = true;


                }

            }
        });
        this.addView(var1);
    }

    private void f() {
        label66:
        {
            String var14;
            label55:
            {
                label54:
                {
                    label53:
                    {
                        label52:
                        {
                            j var1;
                            boolean var10001;
                            try {
                                k.b(this.activity());
                                var1 = SOLib.g();
                            } catch (ExceptionInInitializerError var10) {
                                var10001 = false;
                                break label54;
                            } catch (LinkageError var11) {
                                var10001 = false;
                                break label53;
                            } catch (SecurityException var12) {
                                var10001 = false;
                                break label52;
                            }

                            if (var1 != null) {
                                try {
                                    var1.a(this.activity());
                                    return;
                                } catch (ExceptionInInitializerError var2) {
                                    var10001 = false;
                                    break label54;
                                } catch (LinkageError var3) {
                                    var10001 = false;
                                    break label53;
                                } catch (SecurityException var4) {
                                    var10001 = false;
                                }
                            } else {
                                try {
                                    ClassNotFoundException var15 = new ClassNotFoundException();
                                    throw var15;
                                } catch (ExceptionInInitializerError var6) {
                                    var10001 = false;
                                    break label54;
                                } catch (LinkageError var7) {
                                    var10001 = false;
                                    break label53;
                                } catch (SecurityException var8) {
                                    var10001 = false;
                                } catch (ClassNotFoundException var9) {
                                    var10001 = false;
                                    break label66;
                                }
                            }
                        }

                        var14 = String.format("initClipboardHandler() experienced unexpected exception [%s]", "SecurityException");
                        break label55;
                    }

                    var14 = String.format("initClipboardHandler() experienced unexpected exception [%s]", "LinkageError");
                    break label55;
                }

                var14 = String.format("initClipboardHandler() experienced unexpected exception [%s]", "ExceptionInInitializerError");
            }

            return;
        }
    }

    private void g() {
        String var17;
        label78:
        {
            label67:
            {
                label66:
                {
                    label65:
                    {
                        label64:
                        {
                            label63:
                            {
                                label62:
                                {
                                    SODataLeakHandlers var1;
                                    boolean var10001;
                                    try {
                                        var1 = Utilities.getDataLeakHandlers();
                                        this.mDataLeakHandlers = var1;
                                    } catch (ExceptionInInitializerError var12) {
                                        var10001 = false;
                                        break label66;
                                    } catch (LinkageError var13) {
                                        var10001 = false;
                                        break label65;
                                    } catch (SecurityException var14) {
                                        var10001 = false;
                                        break label64;
                                    } catch (Exception var16) {
                                        var10001 = false;
                                        break label62;
                                    }

                                    if (var1 != null) {
                                        try {
                                            var1.initDataLeakHandlers(this.activity());
                                            return;
                                        } catch (ExceptionInInitializerError var2) {
                                            var10001 = false;
                                            break label66;
                                        } catch (LinkageError var3) {
                                            var10001 = false;
                                            break label65;
                                        } catch (SecurityException var4) {
                                            var10001 = false;
                                            break label64;
                                        } catch (Exception var5) {
                                            var10001 = false;
                                            break label63;
                                        }
                                    } else {
                                        try {
                                            ClassNotFoundException var18 = new ClassNotFoundException();
                                            throw var18;
                                        } catch (ExceptionInInitializerError var7) {
                                            var10001 = false;
                                            break label66;
                                        } catch (LinkageError var8) {
                                            var10001 = false;
                                            break label65;
                                        } catch (SecurityException var9) {
                                            var10001 = false;
                                            break label64;
                                        } catch (ClassNotFoundException var10) {
                                            var10001 = false;
                                            break label63;
                                        }
                                    }
                                }

                                var17 = "DataLeakHandlers IOException";
                                break label78;
                            }

                            var17 = "DataLeakHandlers implementation unavailable";
                            break label78;
                        }

                        var17 = String.format("setDataLeakHandlers() experienced unexpected exception [%s]", "SecurityException");
                        break label67;
                    }

                    var17 = String.format("setDataLeakHandlers() experienced unexpected exception [%s]", "LinkageError");
                    break label67;
                }

                var17 = String.format("setDataLeakHandlers() experienced unexpected exception [%s]", "ExceptionInInitializerError");
            }

            return;
        }

        Log.i("NUIDocView", var17);
    }

    private View getSingleTabView() {
        int var1 = this.tabHost.getTabWidget().getTabCount();
        return this.tabHost.getTabWidget().getChildTabViewAt(var1 - 1);

    }

    private void h() {
        Point var1 = Utilities.getRealScreenSize(this.activity());
        byte var2;
        if (var1.x > var1.y) {
            var2 = 2;
        } else {
            var2 = 1;
        }

        label18:
        {
            if (!this.mForceOrientationChange) {
                int var3 = this.mLastOrientation;
                if (var2 == var3 || var3 == 0) {
                    break label18;
                }
            }

            this.onOrientationChange();
        }

        this.mForceOrientationChange = false;
        this.mLastOrientation = var2;
    }

    private void i() {
        this.mDocView.requestLayout();
        if (this.usePagesView() && this.isPageListVisible()) {
            this.mDocPageListView.requestLayout();
        }

    }

    private void j() {
        SODocSession var1 = this.mSession;
        if (var1 != null && this.mPrintButton != null) {
            byte var2;
            LinearLayout var3;
            if (!var1.getDoc().k() || !this.mConfigOptions.l() && !this.mConfigOptions.m()) {
                var3 = this.mPrintButton;
                var2 = 8;
            } else {
                var3 = this.mPrintButton;
                var2 = 0;
            }

            var3.setVisibility(var2);
        }

    }

    private void k() {
        Utilities.hideKeyboard(this.getContext());
    }

    private void l() {
        ProgressDialog var1 = new ProgressDialog(this.getContext(), R.style.sodk_editor_alert_dialog_style);
        this.mProgressDialog = var1;
        var1.setMessage(this.getContext().getString(R.string.sodk_editor_loading_please_wait));
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setIndeterminate(true);
        Window var2 = this.mProgressDialog.getWindow();
        var2.setFlags(8, 8);
        var2.clearFlags(2);
        try {
            this.mProgressDialog.show();
        } catch (Exception e) {
            // Handle the exception if the dialog cannot be shown
            Log.e("NUIDocView", "Failed to show progress dialog: " + e.getMessage());
        }
    }

    private void m() {
        ProgressDialog var1 = this.mProgressDialog;
        try {
            if (var1 != null) {
                var1.dismiss();
                this.mProgressDialog = null;
            }
        } catch (IllegalArgumentException e) {

        }
    }

    private void setSearchListener() {

        if (this.mSearchListener == null) {
            this.mSearchListener = new p() {
                public void a() {
                    NUIDocView.this.mIsSearching = false;
                    NUIDocView.this.dismissSearchDialog();
                    Utilities.yesNoMessage((Activity) NUIDocView.this.getContext(), NUIDocView.this.getResources().getString(R.string.sodk_editor_no_more_found), NUIDocView.this.getResources().getString(R.string.sodk_editor_keep_searching), NUIDocView.this.getResources().getString(R.string.sodk_editor_str_continue), NUIDocView.this.getResources().getString(R.string.sodk_editor_stop), new Runnable() {
                        public void run() {
                            (new Handler()).post(new Runnable() {
                                public void run() {
                                    NUIDocView.this.mIsSearching = true;
                                    NUIDocView.this.x();
                                }
                            });
                        }
                    }, new Runnable() {
                        public void run() {
                            NUIDocView.this.mIsSearching = false;
                        }
                    });
                }

                public void a(int var1) {
                }

                public void a(int var1, RectF var2) {
                    NUIDocView.this.dismissSearchDialog();
                    NUIDocView.this.getDocView().onFoundText(var1, var2);
                    NUIDocView.this.getDocView().waitForRest(new Runnable() {
                        public void run() {
                            NUIDocView.this.mIsSearching = false;
                        }
                    });
                }

                public boolean b() {
                    NUIDocView.this.x();
                    return true;
                }

                public boolean c() {
                    NUIDocView.this.x();
                    return true;
                }

                public void d() {
                    NUIDocView.this.dismissSearchDialog();
                    NUIDocView.this.mIsSearching = false;
                }

                public void e() {
                    NUIDocView.this.dismissSearchDialog();
                    NUIDocView.this.mIsSearching = false;
                }
            };
            this.mSession.getDoc().a(this.mSearchListener);
        }

        this.mSession.getDoc().d(false);
    }

    private void o() {

        int var1 = this.tabHost.getTabWidget().getTabCount();

        for (int var2 = 1; var2 < var1 - 1; ++var2) {
            this.tabHost.getTabWidget().getChildAt(var2).setVisibility(GONE);
        }

    }

    private void p() {
        int var1 = this.tabHost.getTabWidget().getTabCount();

        for (int var2 = 1; var2 < var1 - 1; ++var2) {
            this.tabHost.getTabWidget().getChildAt(var2).setVisibility(VISIBLE);
        }

    }

    private void q() {

        if (this.mListNumbersButton.isSelected()) {
            this.mSession.getDoc().C();
        } else if (this.mListBulletsButton.isSelected()) {
            this.mSession.getDoc().D();
        } else {
            this.mSession.getDoc().B();
        }

    }

    private boolean r() {

        int[] var1 = this.mSession.getDoc().getIndentationLevel();
        return var1 != null && var1[0] < var1[1];
    }

    private boolean s() {

        int[] var1 = this.mSession.getDoc().getIndentationLevel();
        return var1 != null && var1[0] > 0;
    }

    private void setFooterText(String var1) {

        if (var1 != null && !var1.isEmpty()) {
            String var2 = (new File(var1)).getName();
            if (!var2.isEmpty()) {
                this.mFooter.setText(var2);
            } else {
                this.mFooter.setText(var1);
            }
        }

    }

    private void setSingleTabTitle(String var1) {

        if (!var1.equalsIgnoreCase(this.getContext().getString(R.string.sodk_editor_tab_hidden))) {
            int var2 = this.tabHost.getTabWidget().getTabCount();
            ((SOTextView) this.tabHost.getTabWidget().getChildTabViewAt(var2 - 1).findViewById(R.id.tabText)).setText(var1);
        }
    }

    private void setTab(String var1) {

        this.mCurrentTab = var1;
        ((TabHost) this.findViewById(R.id.tabhost)).setCurrentTabByTag(this.mCurrentTab);
        this.setSingleTabTitle(var1);
        if (Utilities.isPhoneDevice(this.activity())) {
            this.scaleHeader();
        }

    }

    private void setValid(boolean var1) {

        DocView var2 = this.mDocView;
        if (var2 != null) {
            var2.setValid(var1);
        }

        if (this.usePagesView()) {
            DocListPagesView var3 = this.mDocPageListView;
            if (var3 != null) {
                var3.setValid(var1);
            }
        }

    }

    private void releaseDocViewBitmap() {

        DocView var1 = this.mDocView;
        if (var1 != null) {
            var1.releaseBitmaps();
        }

        if (this.usePagesView()) {
            DocListPagesView var3 = this.mDocPageListView;
            if (var3 != null) {
                var3.releaseBitmaps();
            }
        }

        int var2 = 0;

        while (true) {
            SOBitmap[] var4 = this.bitmaps;
            if (var2 >= var4.length) {
                return;
            }

            if (var4[var2] != null) {
                var4[var2].a().recycle();
                this.bitmaps[var2] = null;
            }

            ++var2;
        }
    }

    private void u() {


        if (this.mTabs != null) {
            if (this.mConfigOptions.b()) {
                NUIDocView.TabData[] var1 = this.getTabData();
                ListPopupWindow var2;
                if (!this.mConfigOptions.j() && !this.mConfigOptions.k()) {
                    if (this.mCurrentTab.equals(this.mTabs[2].name)) {
                        String var3 = this.mTabs[this.getInitialTab()].name;
                        this.changeTab(var3);
                        this.setSingleTabTitle(var3);
                        this.tabHost.setCurrentTabByTag(var3);
                        this.b(this.getSingleTabView(), true);
                    }

                    var2 = this.mListPopupWindow;
                    if (var2 != null && var2.isShowing() && var1[2].visibility != 8) {
                        this.mListPopupWindow.dismiss();
                    }

                    var1[2].visibility = 8;
                } else {
                    var2 = this.mListPopupWindow;
                    if (var2 != null && var2.isShowing() && var1[2].visibility != 0) {
                        this.mListPopupWindow.dismiss();
                    }

                    var1[2].visibility = 0;
                }

            }
        }
    }

    private void setDocViewBitmap() {

        DocView var1 = this.mDocView;
        if (var1 != null) {
            var1.setBitmaps(this.bitmaps);
        }

        if (this.usePagesView()) {
            DocListPagesView var2 = this.mDocPageListView;
            if (var2 != null) {
                var2.setBitmaps(this.bitmaps);
            }
        }

    }

    private void setSearchCounter() {

        if (this.mSearchCounter > 1000) {
            this.mSearchCounter = 0;
        }

        ++this.mSearchCounter;
    }

    private void x() {


        Utilities.hideKeyboard(this.getContext());
        this.y();
        String searchText = this.mSearchText.getText().toString();
        SODoc var2 = this.getDoc();
        var2.b(searchText);
        var2.q();
    }

    private void y() {

        if (!this.mProgressIsScheduled) {
            this.mProgressIsScheduled = true;
            final int var1 = this.mSearchCounter;
            if (this.mProgressHandler == null) {
                this.mProgressHandler = new Handler();
            }

            this.mProgressHandler.postDelayed(new Runnable() {
                public void run() {
                    NUIDocView.this.mProgressIsScheduled = false;
                    if (NUIDocView.this.mIsSearching) {
                        if (var1 == NUIDocView.this.mSearchCounter) {
                            if (NUIDocView.this.getDoc() != null) {
                                if (NUIDocView.this.mSearchProgressDialog == null) {
                                    NUIDocView var1x = NUIDocView.this;
                                    var1x.mSearchProgressDialog = new ProgressDialog(var1x.getContext(), R.style.sodk_editor_alert_dialog_style);
                                }

                                ProgressDialog var2 = NUIDocView.this.mSearchProgressDialog;
                                StringBuilder var3 = new StringBuilder();
                                var3.append(NUIDocView.this.getResources().getString(R.string.sodk_editor_searching));
                                var3.append("...");
                                var2.setMessage(var3.toString());
                                NUIDocView.this.mSearchProgressDialog.setCancelable(false);
                                NUIDocView.this.mSearchProgressDialog.setButton(-2, NUIDocView.this.getResources().getString(R.string.sodk_editor_cancel), new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface var1x, int var2) {
                                        NUIDocView.this.getDoc().cancelSearch();
                                    }
                                });
                                NUIDocView.this.mSearchProgressDialog.show();
                            }
                        }
                    }
                }
            }, 1000L);
        }
    }

    private void dismissSearchDialog() {
        ProgressDialog var1 = this.mSearchProgressDialog;
        if (var1 != null) {
            var1.dismiss();
        }

    }

    protected Activity activity() {
        return (Activity) this.getContext();
    }

    public void addDeleteOnClose(String var1) {
        this.mDeleteOnClose.add(var1);
    }


    private final String paramSaveAs = "save_as";
    private final String paramEdit = "edit";
    private final String paramSavePDF = "save_as_pdf";

    @Override
    public void onDismissReward(String nameParam) {
        mConfigOptions.a(true); // editable
        isNewCreate = true;
        if (nameParam.equals(paramEdit)) {
            onBottomMenuButton(mEditTab, getResources().getString(R.string.sodk_editor_tab_edit));
        }
        if (nameParam.equals(paramSaveAs)) {
            onSaveAsButton(mSaveButton);
        }
        if (nameParam.equals(paramSavePDF)) {
            onSavePDFButton(mSavePdfButton);
        }
    }

    private boolean isNewCreate = false;

    protected void afterFirstLayoutComplete() {
        isNewCreate = activity().getIntent().getBooleanExtra("ALLOW_EDIT", false);
        mConfigOptions.a(true);

        this.mFinished = false;
        if (this.mConfigOptions.q()) {
            SOFileDatabase.init(this.activity());
        }

        this.createEditButtons();
        this.createEditButtons2();
        this.createReviewButtons();
        this.createPagesButtons();
        this.createInsertButtons();
        this.mBackButton = (ImageView) this.createToolbarButton(R.id.back_button);
        this.mUndoButton = (ImageView) this.createToolbarButton(R.id.undo_button);
        this.mRedoButton = (ImageView) this.createToolbarButton(R.id.redo_button);

        this.lnMenuFile = (TextView) this.createToolbarButton(R.id.ln_menu_file);
        this.lnMenuFile.setOnClickListener(this);

        this.lnMenuTypo = (TextView) this.createToolbarButton(R.id.ln_menu_typo);

        this.lnMenuEdit = (TextView) this.createToolbarButton(R.id.ln_menu_edit);
        this.lnMenuEdit.setOnClickListener(this);

        this.lnMenuInsert = (TextView) this.createToolbarButton(R.id.ln_menu_insert);
        this.lnMenuInsert.setOnClickListener(this);

        this.lnMenuPage = (TextView) this.createToolbarButton(R.id.ln_menu_page);
        this.lnMenuPage.setOnClickListener(this);

        this.lnMenuAnnotate = (TextView) this.createToolbarButton(R.id.ln_menu_annotate);
        this.lnMenuAnnotate.setOnClickListener(this);

        this.lnMenuSlide = (TextView) this.createToolbarButton(R.id.ln_menu_slide);
        this.lnMenuSlide.setOnClickListener(this);

        this.lnMenuFormat = (TextView) this.createToolbarButton(R.id.ln_menu_format);
        this.lnMenuFormat.setOnClickListener(this);

        this.lnMenuFormulas = (TextView) this.createToolbarButton(R.id.ln_menu_formulas);
        this.lnMenuFormulas.setOnClickListener(this);

        this.lnMenuFormulas = (TextView) this.createToolbarButton(R.id.ln_menu_formulas);
        this.lnMenuFormulas.setOnClickListener(this);

        this.mSearchButton = (ImageView) this.createToolbarButton(R.id.search_button);
        this.mFullscreenButton = (ImageView) this.createToolbarButton(R.id.fullscreen_button);
        this.mSearchNextButton = (LinearLayout) this.createToolbarButton(R.id.search_next);
        this.mSearchPreviousButton = (LinearLayout) this.createToolbarButton(R.id.search_previous);
        this.mSearchCloseButton = (LinearLayout) this.createToolbarButton(R.id.search_close);

        lnHeader = (LinearLayout) this.createToolbarButton(R.id.header);
        lnEditor = (LinearLayout) this.createToolbarButton(R.id.ln_menu_editor);
        lnRollback = (LinearLayout) this.createToolbarButton(R.id.rollback_view);
        mFileTab = (LinearLayout) this.createToolbarButton(R.id.fileTab);
        mEditTab = (LinearLayout) this.createToolbarButton(R.id.editTab);
        mHomeTab = (LinearLayout) this.createToolbarButton(R.id.typoTab);
        mInsertTab = (LinearLayout) this.createToolbarButton(R.id.insertTab);
        mFormatTab = (LinearLayout) this.createToolbarButton(R.id.formatTab);
        mFormulasTab = (LinearLayout) this.createToolbarButton(R.id.formulasTab);
        mSlideTab = (LinearLayout) this.createToolbarButton(R.id.slidesTab);
        mPagesTab = (LinearLayout) this.createToolbarButton(R.id.pagesTab);
        mAnnotateTab = (LinearLayout) this.createToolbarButton(R.id.annotateTab);
        mSearchTab = (LinearLayout) this.createToolbarButton(R.id.searchTab);
        mReviewTab = (LinearLayout) this.createToolbarButton(R.id.reviewTab);
        mRedactTab = (LinearLayout) this.createToolbarButton(R.id.redactTab);
        mFormulasButton = (LinearLayout) this.createToolbarButton(R.id.formulas_button);

        lnBottom = (LinearLayout) this.createToolbarButton(R.id.ln_bottom);

        lnFormulas = (LinearLayout) this.createToolbarButton(R.id.ln_formulas);

        lnMenuTextColor = (LinearLayout) this.createToolbarButton(R.id.ln_menu_text_color);

        lnMenuTextHighlight = (LinearLayout) this.createToolbarButton(R.id.ln_menu_text_highlight);

        mColorRed = (ImageView) this.createToolbarButton(R.id.iv_color_red);

        mColorOrange = (ImageView) this.createToolbarButton(R.id.iv_color_orange);

        mColorYellow = (ImageView) this.createToolbarButton(R.id.iv_color_yellow);

        mColorGreen = (ImageView) this.createToolbarButton(R.id.iv_color_green);

        mColorBlue = (ImageView) this.createToolbarButton(R.id.iv_color_blue);

        mColorPurple = (ImageView) this.createToolbarButton(R.id.iv_color_purple);

        mColorBlack = (ImageView) this.createToolbarButton(R.id.iv_color_black);

        mColorWhite = (ImageView) this.createToolbarButton(R.id.iv_color_white);


        mHighlightRed = (ImageView) this.createToolbarButton(R.id.iv_highlight_red);

        mHighlightOrange = (ImageView) this.createToolbarButton(R.id.iv_highlight_orange);

        mHighlightYellow = (ImageView) this.createToolbarButton(R.id.iv_highlight_yellow);

        mHighlightGreen = (ImageView) this.createToolbarButton(R.id.iv_highlight_green);

        mHighlightBlue = (ImageView) this.createToolbarButton(R.id.iv_highlight_blue);

        mHighlightPurple = (ImageView) this.createToolbarButton(R.id.iv_highlight_purple);

        mHighlightBlack = (ImageView) this.createToolbarButton(R.id.iv_highlight_black);

        mHighlightWhite = (ImageView) this.createToolbarButton(R.id.iv_highlight_white);

        mButtonKeyboard = (ImageView) this.createToolbarButton(R.id.button_keyboard);
        mButtonOutFullScreen = (ImageView) this.createToolbarButton(R.id.button_out_full_screen);

        try {
            mColorRed.setOnClickListener(this);

            mColorOrange.setOnClickListener(this);

            mColorYellow.setOnClickListener(this);

            mColorGreen.setOnClickListener(this);

            mColorBlue.setOnClickListener(this);

            mColorPurple.setOnClickListener(this);

            mColorBlack.setOnClickListener(this);

            mColorWhite.setOnClickListener(this);

            mHighlightRed.setOnClickListener(this);

            mHighlightOrange.setOnClickListener(this);

            mHighlightYellow.setOnClickListener(this);

            mHighlightGreen.setOnClickListener(this);

            mHighlightBlue.setOnClickListener(this);

            mHighlightPurple.setOnClickListener(this);

            mHighlightBlack.setOnClickListener(this);

            mHighlightWhite.setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageView var1;

        this.mBackButton.setOnClickListener(this);

        if (!this.hasSearch()) {
            var1 = this.mSearchButton;
            if (var1 != null) {
                var1.setVisibility(INVISIBLE);
            }
        }

        if (!this.hasUndo()) {
            var1 = this.mUndoButton;
            if (var1 != null) {
                var1.setVisibility(GONE);
            }
        }

        if (!this.hasRedo()) {
            var1 = this.mRedoButton;
            if (var1 != null) {
                var1.setVisibility(GONE);
            }
        }

        if (!this.mConfigOptions.z()) {
            var1 = this.mFullscreenButton;
            if (var1 != null) {
                var1.setVisibility(GONE);
            }
        }

        if (!this.mConfigOptions.b()) {
            var1 = this.mUndoButton;
            if (var1 != null) {
                var1.setVisibility(GONE);
            }

            var1 = this.mRedoButton;
            if (var1 != null) {
                var1.setVisibility(GONE);
            }
        }

        this.showSearchSelected(false);
        this.mSearchText = (SOEditText) this.findViewById(R.id.search_text_input);
        this.mFooterText = (SOTextView) this.findViewById(R.id.footer_page_text);
        this.mFooterText.setVisibility(View.GONE);

//        this.mFooterLead = this.findViewById(R.id.footer_lead);
        this.mSearchText.setOnEditorActionListener(new SOEditTextOnEditorActionListener() {
            public boolean onEditorAction(SOEditText var1, int var2, KeyEvent var3) {
                if (var2 == 5) {
                    NUIDocView.this.onSearchNext((View) null);
                    return true;
                } else {
                    return false;
                }
            }
        });
        this.mSearchText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View var1, int var2, KeyEvent var3) {
                return var2 == 67 && NUIDocView.this.mSearchText.getSelectionStart() == 0 && NUIDocView.this.mSearchText.getSelectionEnd() == 0;
            }
        });

        this.mSearchText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable var1) {
            }

            public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4) {
            }

            public void onTextChanged(CharSequence var1, int var2, int var3, int var4) {
                NUIDocView.this.setSearchStart();
                LinearLayout var5 = NUIDocView.this.mSearchNextButton;
                var2 = var1.toString().length();
                boolean var6 = false;
                boolean var7;

                if (var2 > 0) {
                    var7 = true;
                } else {
                    var7 = false;
                }

                highlightView(var5, var7);
                var5 = NUIDocView.this.mSearchPreviousButton;
                var7 = var6;
                if (var1.toString().length() > 0) {
                    var7 = true;
                }

                highlightView(var5, var7);
            }
        });
        this.mSearchText.setCustomSelectionActionModeCallback(Utilities.editFieldDlpHandler);
        ImageView var3 = (ImageView) this.findViewById(R.id.search_text_clear);
        this.mSearchClear = var3;
        var3.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                NUIDocView.this.mSearchText.setText("");
            }
        });
        this.mSaveButton = (LinearLayout) this.createToolbarButton(R.id.save_button);
        this.mSaveAsButton = (LinearLayout) this.createToolbarButton(R.id.save_as_button);
        this.mSavePdfButton = (LinearLayout) this.createToolbarButton(R.id.save_pdf_button);

        // Icon premium

        boolean isPremium = IAPUtils.INSTANCE.isPremium();

        View view = findViewById(R.id.ic_save_premium);
        if (view != null) {
            view.setVisibility(isPremium ? GONE : VISIBLE);
        }
        view = findViewById(R.id.ic_save_as_premium);
        if (view != null) {
            view.setVisibility(isPremium ? GONE : VISIBLE);
        }
        view = findViewById(R.id.ic_save_pdf_premium);
        if (view != null) {
            view.setVisibility(isPremium ? GONE : VISIBLE);
        }

        this.mPrintButton = (LinearLayout) this.createToolbarButton(R.id.print_button);
        this.mShareButton = (LinearLayout) this.createToolbarButton(R.id.share_button);
        this.mOpenInButton = (LinearLayout) this.createToolbarButton(R.id.open_in_button);
        this.mOpenPdfInButton = (LinearLayout) this.createToolbarButton(R.id.open_pdf_in_button);
        this.mProtectButton = (LinearLayout) this.createToolbarButton(R.id.protect_button);
        this.mCopyButton2 = (LinearLayout) this.createToolbarButton(R.id.copy_button2);
        this.mCopyDivider = (View) findViewById(R.id.divider_copy);
        int var2 = this.getContext().getResources().getIdentifier("custom_save_button", "id", this.getContext().getPackageName());
        if (var2 != 0) {
            this.mCustomSaveButton = (ToolbarButton) this.createToolbarButton(var2);
        }

        this.onDeviceSizeChange();
        this.setConfigurableButtons();
        this.setConfigToolbar();
        this.fixFileToolbar(R.id.file_toolbar);
        this.mAdapter = this.createAdapter();
        DocView var4 = this.createMainView(this.activity());
        this.mDocView = var4;
        var4.setHost(this);
        this.mDocView.setAdapter(this.mAdapter);


        this.mDocView.setConfigOptions(this.mConfigOptions);
        if (this.usePagesView()) {
            DocListPagesView var5 = new DocListPagesView(this.activity());
            this.mDocPageListView = var5;
            var5.setHost(this);
            this.mDocPageListView.setAdapter(this.mAdapter);
            this.mDocPageListView.setMainView(this.mDocView);
            this.mDocPageListView.setBorderColor(this.mDocView.getBorderColor());
        }

        RelativeLayout var6 = (RelativeLayout) this.findViewById(R.id.doc_inner_container);
        var6.addView(this.mDocView, 0);
        this.mDocView.setup(var6);
        if (this.usePagesView()) {
            var6 = (RelativeLayout) this.findViewById(R.id.pages_container);
            var6.addView(this.mDocPageListView);
            this.mDocPageListView.setup(var6);
            this.mDocPageListView.setCanManipulatePages(this.canCanManipulatePages());
        }

        this.mFooter = (SOTextView) this.findViewById(R.id.footer_text);
        LinearLayout var7 = (LinearLayout) this.findViewById(R.id.header_top);
        if (var7 != null) {
            //var7.setBackgroundColor(this.getTabUnselectedColor());
        }

//        View var8 = this.findViewById(id.header_top_spacer);
//        if (var8 != null) {
        //var8.setBackgroundColor(this.getTabUnselectedColor());
//        }

        this.findViewById(R.id.footer).setOnClickListener(new OnClickListener() {
            @SuppressLint("StringFormatInvalid")
            public void onClick(View var1) {
                long var2 = System.currentTimeMillis();
                long var4 = NUIDocView.this.mVersionLastTapTime;
                long currentTimeMillis = System.currentTimeMillis();
                int i = ((currentTimeMillis - NUIDocView.this.mVersionLastTapTime) > VERSION_TAP_INTERVAL ? 1 : ((currentTimeMillis - NUIDocView.this.mVersionLastTapTime) == VERSION_TAP_INTERVAL ? 0 : -1));
                NUIDocView nUIDocView = NUIDocView.this;
                if (i > 0) {
                    int unused = nUIDocView.mVersionTapCount = 1;
                } else {
                    int unused2 = nUIDocView.mVersionTapCount = nUIDocView.mVersionTapCount + 1;
                }

                if (NUIDocView.this.mVersionTapCount == 5) {
                    String[] var12 = k.d((Activity) NUIDocView.this.getContext());
                    String var6 = "";
                    String var7;
                    String var8;
                    String var9;
                    String var13;
                    if (var12 != null) {
                        var7 = var12[0];
                        var8 = var12[1];
                        var13 = var12[3];
                    } else {
                        var9 = "";
                        var8 = var9;
                        var7 = var9;
                        var13 = var9;
                    }

                    ApplicationInfo var14 = NUIDocView.this.getContext().getApplicationInfo();

                    try {
                        var9 = NUIDocView.this.getContext().getPackageManager().getPackageInfo(var14.packageName, 0).versionName;
                    } catch (NameNotFoundException var10) {
                        var10.printStackTrace();
                        var9 = var6;
                    }
                    try {
                        Utilities.showMessage((Activity) NUIDocView.this.getContext(),
                                NUIDocView.this.getContext().getString(R.string.sodk_editor_version_title),
                                String.format(NUIDocView.this.getContext().getString(R.string.sodk_editor_version_format), var7, var8, var9, var13));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    NUIDocView.this.mVersionTapCount = 0;
                }

                NUIDocView.this.mVersionLastTapTime = var2;
            }
        });

        if (this.mConfigOptions.q()) {
            this.mFileDatabase = SOFileDatabase.getDatabase();
        }

        final Activity var9 = this.activity();
        this.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (NUIDocView.this.mFinished) {
                    NUIDocView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    NUIDocView.this.h();
                }
            }
        });
        if (mDocView != null) {
            this.mDocView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (NUIDocView.this.mDocView != null) {
                        NUIDocView.this.mDocView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    NUIDocView var1 = NUIDocView.this;
                    var1.b(var1.mCurrentTab);
                    NUIDocView.this.highlightView();
                    if (NUIDocView.this.mIsSession) {
                        var1 = NUIDocView.this;
                        var1.mDocUserPath = var1.mSession.getUserPath();
                        if (!NUIDocView.this.mConfigOptions.q()) {
                            throw new UnsupportedOperationException();
                        }

                        NUIDocView.this.l();
                        var1 = NUIDocView.this;
                        var1.mFileState = var1.mFileDatabase.stateForPath(NUIDocView.this.mDocUserPath, NUIDocView.this.mIsTemplate);
                        NUIDocView.this.mFileState.setForeignData(NUIDocView.this.mForeignData);
                        NUIDocView.this.mSession.setFileState(NUIDocView.this.mFileState);
                        NUIDocView.this.mFileState.openFile(NUIDocView.this.mIsTemplate);
                        NUIDocView.this.mFileState.setHasChanges(false);
                        var1 = NUIDocView.this;
                        var1.setFooterText(var1.mFileState.getUserPath());
                        NUIDocView.this.mDocView.setDoc(NUIDocView.this.mSession.getDoc());
                        if (NUIDocView.this.usePagesView()) {
                            if (NUIDocView.this.mDocPageListView != null) {
                                NUIDocView.this.mDocPageListView.setDoc(NUIDocView.this.mSession.getDoc());
                            }
                        }

                        if (NUIDocView.this.mAdapter != null) {
                            NUIDocView.this.mAdapter.setDoc(NUIDocView.this.mSession.getDoc());
                        }
                        NUIDocView.this.mSession.setSODocSessionLoadListener(new SODocSessionLoadListener() {
                            public void onCancel() {
                                NUIDocView.this.m();
                            }

                            public void onDocComplete() {
                                NUIDocView.this.m();
                                NUIDocView.this.onDocCompleted();
                                NUIDocView.this.setPageNumberText();
                            }

                            public void onError(int var1, int var2) {
                                if (NUIDocView.this.mSession.isOpen()) {
                                    NUIDocView.this.d();
                                    NUIDocView.this.m();
                                    if (!NUIDocView.this.mSession.isCancelled() || var1 != 6) {
                                        String var3 = Utilities.getOpenErrorDescription(NUIDocView.this.getContext(), var1);
                                        try {
                                            Utilities.showMessage(var9, NUIDocView.this.getContext().getString(R.string.sodk_editor_error), var3);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }

                            public void onLayoutCompleted() {
                                NUIDocView.this.onLayoutChanged();

                            }

                            public void onPageLoad(int var1) {
                                NUIDocView.this.m();
                                NUIDocView.this.onPageLoaded(var1);
                            }

                            public void onSelectionChanged(int var1, int var2) {
                                NUIDocView.this.onSelectionMonitor(var1, var2);
                            }
                        });
                        if (NUIDocView.this.usePagesView()) {
                            float var2 = (float) NUIDocView.this.getResources().getInteger(R.integer.sodk_editor_pagelist_width_percentage) / 100.0F;
                            if (NUIDocView.this.mDocPageListView != null) {
                                NUIDocView.this.mDocPageListView.setScale(var2);
                            }
                        }
                    } else {
                        label74:
                        {
                            if (NUIDocView.this.mState != null) {
                                if (!NUIDocView.this.mConfigOptions.q()) {
                                    throw new UnsupportedOperationException();
                                }

                                var1 = NUIDocView.this;
                                var1.mDocUserPath = var1.mState.getOpenedPath();
                                var1 = NUIDocView.this;
                                var1.setFooterText(var1.mState.getUserPath());
                                NUIDocView.this.l();
                                var1 = NUIDocView.this;
                                var1.mFileState = var1.mState;
                                NUIDocView.this.mFileState.openFile(NUIDocView.this.mIsTemplate);
                                var1 = NUIDocView.this;
                                var1.mSession = new SODocSession(var9, var1.mDocumentLib);
                                NUIDocView.this.mSession.setFileState(NUIDocView.this.mFileState);
                                NUIDocView.this.mSession.setSODocSessionLoadListener(new SODocSessionLoadListener() {
                                    public void onCancel() {
                                        NUIDocView.this.m();
                                    }

                                    public void onDocComplete() {
                                        if (!NUIDocView.this.mFinished) {
                                            NUIDocView.this.m();
                                            NUIDocView.this.onDocCompleted();
                                        }
                                    }

                                    public void onError(int var1, int var2) {
                                        if (!NUIDocView.this.mFinished) {
                                            NUIDocView.this.d();
                                            NUIDocView.this.m();
                                            if (!NUIDocView.this.mSession.isCancelled() || var1 != 6) {
                                                String var3 = Utilities.getOpenErrorDescription(NUIDocView.this.getContext(), var1);
                                                try {
                                                    Utilities.showMessage(var9, NUIDocView.this.getContext().getString(R.string.sodk_editor_error), var3);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }

                                            }

                                        }
                                    }

                                    public void onLayoutCompleted() {
                                        NUIDocView.this.onLayoutChanged();


                                    }

                                    public void onPageLoad(int var1) {
                                        if (!NUIDocView.this.mFinished) {
                                            NUIDocView.this.m();
                                            NUIDocView.this.onPageLoaded(var1);
                                        }
                                    }

                                    public void onSelectionChanged(int var1, int var2) {
                                        NUIDocView.this.onSelectionMonitor(var1, var2);
                                    }
                                });
                                NUIDocView.this.mSession.open(NUIDocView.this.mFileState.getInternalPath());
                                if (NUIDocView.this.mSession.getDoc() != null && mDocView != null) {
                                    NUIDocView.this.mDocView.setDoc(NUIDocView.this.mSession.getDoc());
                                } else {
                                    if (mDocPageListView != null) {
                                        mDocPageListView.finish();
                                    }
                                }
                                if (NUIDocView.this.usePagesView()) {
                                    if (NUIDocView.this.mDocPageListView != null) {
                                        NUIDocView.this.mDocPageListView.setDoc(NUIDocView.this.mSession.getDoc());
                                    }
                                }

                                if (NUIDocView.this.mAdapter != null) {
                                    NUIDocView.this.mAdapter.setDoc(NUIDocView.this.mSession.getDoc());
                                }
                                if (!NUIDocView.this.usePagesView()) {
                                    break label74;
                                }
                            } else {
                                Uri var6 = NUIDocView.this.mStartUri;
                                String var3 = var6.getScheme();
                                if (var3 != null && var3.equalsIgnoreCase("content")) {
                                    String var7 = com.artifex.solib.a.b(NUIDocView.this.getContext(), var6);
                                    if (var7.equals("---fileOpen")) {
                                        try {
                                            Utilities.showMessage(var9, NUIDocView.this.getContext().getString(R.string.sodk_editor_content_error), NUIDocView.this.getContext().getString(R.string.sodk_editor_error_opening_from_other_app));
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                        return;
                                    }

                                    if (var7.startsWith("---")) {
                                        var3 = NUIDocView.this.getResources().getString(R.string.sodk_editor_cant_create_temp_file);
                                        Activity var11 = var9;
                                        String var4 = NUIDocView.this.getContext().getString(R.string.sodk_editor_content_error);
                                        StringBuilder var5 = new StringBuilder();
                                        var5.append(NUIDocView.this.getContext().getString(R.string.sodk_editor_error_opening_from_other_app));
                                        var5.append(": \n\n");
                                        var5.append(var3);
                                        try {
                                            Utilities.showMessage(var11, var4, var5.toString());
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                        return;
                                    }

                                    NUIDocView.this.mDocUserPath = var7;
                                    if (NUIDocView.this.mIsTemplate) {
                                        NUIDocView.this.addDeleteOnClose(var7);
                                    }
                                } else {
                                    NUIDocView.this.mDocUserPath = var6.getPath();
                                    if (NUIDocView.this.mDocUserPath == null) {
                                        try {
                                            Utilities.showMessage(var9, NUIDocView.this.getContext().getString(R.string.sodk_editor_invalid_file_name), NUIDocView.this.getContext().getString(R.string.sodk_editor_error_opening_from_other_app));
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                        StringBuilder var8 = new StringBuilder();
                                        var8.append(" Uri has no path: ");
                                        var8.append(var6.toString());
                                        return;
                                    }
                                }

                                var1 = NUIDocView.this;
                                var1.setFooterText(var1.mDocUserPath);
                                NUIDocView.this.l();
                                NUIDocView var9x;
                                Object var10;
                                if (NUIDocView.this.mConfigOptions.q()) {
                                    var9x = NUIDocView.this;
                                    var10 = var9x.mFileDatabase.stateForPath(NUIDocView.this.mDocUserPath, NUIDocView.this.mIsTemplate);
                                } else {
                                    var9x = NUIDocView.this;
                                    var10 = new SOFileStateDummy(var9x.mDocUserPath);
                                }

                                var9x.mFileState = (SOFileState) var10;
                                NUIDocView.this.mFileState.openFile(NUIDocView.this.mIsTemplate);
                                NUIDocView.this.mFileState.setHasChanges(false);
                                var1 = NUIDocView.this;
                                var1.mSession = new SODocSession(var9, var1.mDocumentLib);
                                NUIDocView.this.mSession.setFileState(NUIDocView.this.mFileState);
                                NUIDocView.this.mSession.setSODocSessionLoadListener(new SODocSessionLoadListener() {
                                    public void onCancel() {
                                        NUIDocView.this.d();
                                        NUIDocView.this.m();
                                    }

                                    public void onDocComplete() {
                                        if (!NUIDocView.this.mFinished) {
                                            NUIDocView.this.m();
                                            NUIDocView.this.onDocCompleted();
                                        }
                                    }

                                    public void onError(int var1, int var2) {
                                        if (!NUIDocView.this.mFinished) {
                                            NUIDocView.this.d();
                                            NUIDocView.this.m();
                                            if (!NUIDocView.this.mSession.isCancelled() || var1 != 6) {
                                                String var3 = Utilities.getOpenErrorDescription(NUIDocView.this.getContext(), var1);
                                                try {
                                                    Utilities.showMessage(var9, NUIDocView.this.getContext().getString(R.string.sodk_editor_error), var3);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }

                                        }
                                    }

                                    public void onLayoutCompleted() {
                                        NUIDocView.this.onLayoutChanged();


                                    }

                                    public void onPageLoad(int var1) {
                                        if (!NUIDocView.this.mFinished) {
                                            NUIDocView.this.m();
                                            NUIDocView.this.onPageLoaded(var1);


                                        }
                                    }

                                    public void onSelectionChanged(int var1, int var2) {
                                        NUIDocView.this.onSelectionMonitor(var1, var2);
                                    }
                                });
                                NUIDocView.this.mSession.open(NUIDocView.this.mFileState.getInternalPath());
                                if (NUIDocView.this.mSession.getDoc() != null && mDocView != null) {
                                    NUIDocView.this.mDocView.setDoc(NUIDocView.this.mSession.getDoc());
                                } else {
                                    if (mDocPageListView != null) {
                                        mDocPageListView.finish();
                                    }
                                }
                                if (NUIDocView.this.usePagesView()) {
                                    if (NUIDocView.this.mDocPageListView != null) {
                                        NUIDocView.this.mDocPageListView.setDoc(NUIDocView.this.mSession.getDoc());
                                    }
                                }

                                if (NUIDocView.this.mAdapter != null) {
                                    NUIDocView.this.mAdapter.setDoc(NUIDocView.this.mSession.getDoc());
                                }
                                if (!NUIDocView.this.usePagesView()) {
                                    break label74;
                                }
                            }

                            if (NUIDocView.this.mDocPageListView != null) {
                                NUIDocView.this.mDocPageListView.setScale(0.2F);
                            }

                        }
                    }

                    NUIDocView.this.createInputView();
                }
            });
        }
        if (Utilities.isPhoneDevice(this.activity())) {
            this.scaleHeader();
        }
        setupTabLayout();
        setupSettings();
        if (PreferencesUtils.getBoolean(PresKey.SETTING_LAST_PAGE)) {
            int lastPage = activity().getIntent().getIntExtra("LAST_PAGE", 0);
            if (lastPage > 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        goToPage(lastPage, false);
                    }
                }, 500);
            }
        }
    }

    private void setConfigToolbar() {

    }

    protected void afterShowUI(boolean var1) {

        if (this.mConfigOptions.z() && var1) {
            Toast var2 = this.mFullscreenToast;
            if (var2 != null) {
                var2.cancel();
            }
            if (!isFullScreen()) {
                this.findViewById(R.id.footer).setVisibility(VISIBLE);
            }

            // Toggle the out-of-fullscreen button visibility based on fullscreen state
            if (mButtonOutFullScreen != null) {
                mButtonOutFullScreen.setVisibility(isFullScreen() ? VISIBLE : GONE);
            }

            if (this.isPagesTab()) {
                this.showPages();
            }

//            this.mFullscreen = false;
            if (this.getDocView() != null) {
                this.getDocView().onFullscreen(false);
            }

            this.layoutNow();
        }
    }

    private void setupSettings() {
        boolean isFullScreen = PreferencesUtils.getBoolean("auto_full_settings_key", false);
        if (isFullScreen) {
            onClick(mFullscreenButton);
            if (mButtonOutFullScreen != null) {
                mButtonOutFullScreen.setVisibility(GONE);
            }
        }
    }

    public boolean canCanManipulatePages() {
        return false;
    }

    protected void changeTab(String var1) {
        this.mCurrentTab = var1;
        this.setSingleTabTitle(var1);
        this.onSelectionChanged();
        if (!this.mCurrentTab.equals(this.getContext().getString(R.string.sodk_editor_tab_find)) && !this.mCurrentTab.equals(this.getContext().getString(R.string.sodk_editor_tab_hidden))) {
            this.findViewById(R.id.searchTab).setVisibility(GONE);
            this.showSearchSelected(false);
        }

        this.handlePagesTab(var1);
        this.setTabColors(var1);
        this.mDocView.layoutNow();
    }

    public void clickSheetButton(int var1, boolean var2) {
    }

    protected PageAdapter createAdapter() {
        return new PageAdapter(this.getContext(), this, 1);
    }

    protected void createEditButtons() {

        this.mFontSizeText = (SOTextView) this.createToolbarButton(R.id.font_size_text);
        this.mFontUpButton = (LinearLayout) this.createToolbarButton(R.id.fontup_button);
        this.mFontDownButton = (LinearLayout) this.createToolbarButton(R.id.fontdown_button);
        this.mFontNameText = (TextView) this.createToolbarButton(R.id.font_name_text);
        this.mLayoutFont = (LinearLayout) this.createToolbarButton(R.id.ln_menu_font);
        this.mFontColorButton = (LinearLayout) this.createToolbarButton(R.id.font_color_button);
        this.mFontBackgroundButton = (LinearLayout) this.createToolbarButton(R.id.font_background_button);
        this.mCutButton = (LinearLayout) this.createToolbarButton(R.id.cut_button);
        this.mCopyButton = (LinearLayout) this.createToolbarButton(R.id.copy_button);
        this.mPasteButton = (LinearLayout) this.createToolbarButton(R.id.paste_button);
        this.mStyleBoldButton = (LinearLayout) this.createToolbarButton(R.id.bold_button);
        this.mStyleItalicButton = (LinearLayout) this.createToolbarButton(R.id.italic_button);
        this.mStyleUnderlineButton = (LinearLayout) this.createToolbarButton(R.id.underline_button);
        this.mStyleLinethroughButton = (LinearLayout) this.createToolbarButton(R.id.striketrough_button);
        this.mStyleTextColorButton = (LinearLayout) this.createToolbarButton(R.id.text_color_button);
        this.mStyleTextHighlightButton = (LinearLayout) this.createToolbarButton(R.id.text_highlight_button);
    }

    protected void createEditButtons2() {

        this.mListBulletsButton = (LinearLayout) this.createToolbarButton(R.id.list_bullets_button);
        this.mListNumbersButton = (LinearLayout) this.createToolbarButton(R.id.list_numbers_button);
        this.mAlignLeftButton = (LinearLayout) this.createToolbarButton(R.id.align_left_button);
        this.mAlignCenterButton = (LinearLayout) this.createToolbarButton(R.id.align_center_button);
        this.mAlignRightButton = (LinearLayout) this.createToolbarButton(R.id.align_right_button);
        this.mAlignJustifyButton = (LinearLayout) this.createToolbarButton(R.id.align_justify_button);
        this.mIncreaseIndentButton = (LinearLayout) this.createToolbarButton(R.id.indent_increase_button);
        this.mDecreaseIndentButton = (LinearLayout) this.createToolbarButton(R.id.indent_decrease_button);
    }

    protected void createInputView() {

        RelativeLayout var1 = (RelativeLayout) this.findViewById(R.id.doc_inner_container);
        InputView var2 = new InputView(this.getContext(), this.mSession.getDoc(), this);
        this.mInputView = var2;
        var1.addView(var2);
    }


    protected void createInsertButtons() {
        this.mInsertImageButton = (LinearLayout) this.createToolbarButton(R.id.insert_image_button);
        this.mInsertPhotoButton = (LinearLayout) this.createToolbarButton(R.id.insert_photo_button);
    }

    protected DocView createMainView(Activity var1) {
        return new DocView(var1);
    }

    protected void createPagesButtons() {
        this.mFirstPageButton = (LinearLayout) this.createToolbarButton(R.id.first_page_button);
        this.mLastPageButton = (LinearLayout) this.createToolbarButton(R.id.last_page_button);
        LinearLayout var1 = (LinearLayout) this.createToolbarButton(R.id.reflow_button);
        this.mReflowButton = var1;
        if (var1 != null) {
//            var1.setEnabled(false);
            highlightView(var1, false);
        }

//        setAllSameSize(new LinearLayout[]{this.mFirstPageButton, this.mLastPageButton, this.mReflowButton});
    }

    protected void createReviewButtons() {
    }

    protected View createToolbarButton(int var1) {
        View var2 = this.findViewById(var1);
        if (var2 != null) {
            var2.setOnClickListener(this);
        }

        return var2;
    }

    protected void defocusInputView() {
        InputView var1 = this.mInputView;
        if (var1 != null) {
            var1.clearFocus();
        }

    }

    public void doArrowKey(KeyEvent var1) {
        boolean var2 = var1.isShiftPressed();
        boolean var3 = var1.isAltPressed();
        switch (var1.getKeyCode()) {
            case 19:
                if (!var2 && !var3) {
                    this.mSession.getDoc().processKeyCommand(2);
                }

                if (var2 && !var3) {
                    this.mSession.getDoc().processKeyCommand(12);
                }

                if (!var2 && var3) {
                    this.mSession.getDoc().processKeyCommand(6);
                }

                if (var2 && var3) {
                    this.mSession.getDoc().processKeyCommand(14);
                }

                this.onTyping();
                return;
            case 20:
                if (!var2 && !var3) {
                    this.mSession.getDoc().processKeyCommand(3);
                }

                if (var2 && !var3) {
                    this.mSession.getDoc().processKeyCommand(13);
                }

                if (!var2 && var3) {
                    this.mSession.getDoc().processKeyCommand(7);
                }

                if (var2 && var3) {
                    this.mSession.getDoc().processKeyCommand(15);
                }

                this.onTyping();
                return;
            case 21:
                if (!var2 && !var3) {
                    this.mSession.getDoc().processKeyCommand(0);
                }

                if (var2 && !var3) {
                    this.mSession.getDoc().processKeyCommand(8);
                }

                if (!var2 && var3) {
                    this.mSession.getDoc().processKeyCommand(4);
                }

                if (var2 && var3) {
                    this.mSession.getDoc().processKeyCommand(10);
                }

                this.onTyping();
                return;
            case 22:
                if (!var2 && !var3) {
                    this.mSession.getDoc().processKeyCommand(1);
                }

                if (var2 && !var3) {
                    this.mSession.getDoc().processKeyCommand(9);
                }

                if (!var2 && var3) {
                    this.mSession.getDoc().processKeyCommand(5);
                }

                if (var2 && var3) {
                    this.mSession.getDoc().processKeyCommand(11);
                }

                this.onTyping();
                return;
            default:
        }
    }

    public void doBold() {
        if (this.mSession != null) {
            SODoc var1 = this.mSession.getDoc();
            boolean var2 = var1.getSelectionIsBold() ^ true;
            this.mStyleBoldButton.setSelected(var2);
            var1.setSelectionIsBold(var2);
        }
    }

    public void doCopy() {
        this.mSession.getDoc().J();
    }

    public void doCut() {
        this.mSession.getDoc().I();
        this.updateInputView();
    }

    public void doInsertImage(String var1) {
        if (!com.artifex.solib.a.c(var1)) {
            try {
                Utilities.showMessage((Activity) this.getContext(), this.getContext().getString(R.string.sodk_editor_insert_image_gone_title), this.getContext().getString(R.string.sodk_editor_insert_image_gone_body));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                String var2 = Utilities.preInsertImage(this.getContext(), var1);
                this.getDoc().d(var2);
                if (!var1.equalsIgnoreCase(var2)) {
                    this.addDeleteOnClose(var2);
                }
            } catch (Exception var3) {
                Utilities.showMessage((Activity) this.getContext(), this.getContext().getString(R.string.sodk_editor_insert_image_gone_title), this.getContext().getString(R.string.sodk_editor_insert_image_gone_title));
            }
        }
    }

    public void doItalic() {
        SODoc var1 = this.mSession.getDoc();
        boolean var2 = var1.getSelectionIsItalic() ^ true;
        this.mStyleItalicButton.setSelected(var2);
        var1.setSelectionIsItalic(var2);
    }

    public boolean doKeyDown(int var1, KeyEvent var2) {
        boolean var3 = var2.isAltPressed();
        boolean var4 = var2.isCtrlPressed();
        boolean var5 = var2.isShiftPressed();
        BaseActivity var6 = (BaseActivity) this.getContext();
        var1 = var2.getKeyCode();
        if (var1 == 4) {
            if (var6.isSlideShow()) {
                var6.finish();
            } else {
                this.onBackPressed(false);
            }

            return true;
        } else {
            if (var1 == 37) {
                if (this.inputViewHasFocus() && (var4 || var3)) {
                    this.onTyping();
                    this.doItalic();
                    return true;
                }
            } else if (var1 != 47) {
                if (var1 == 52) {
                    if (this.inputViewHasFocus() && (var4 || var3)) {
                        this.onTyping();
                        this.doCut();
                        return true;
                    }
                } else if (var1 != 54) {
                    SOSelectionLimits var10;
                    if (var1 == 62) {
                        var10 = this.getDocView().getSelectionLimits();
                        if (var10 == null || !var10.getIsActive()) {
                            if (var5) {
                                this.A();
                            } else {
                                this.B();
                            }

                            return true;
                        }
                    } else if (var1 != 30) {
                        if (var1 == 31) {
                            if (this.inputViewHasFocus() && (var4 || var3)) {
                                this.doCopy();
                                return true;
                            }
                        } else if (var1 != 49) {
                            if (var1 == 50) {
                                if (this.inputViewHasFocus() && (var4 || var3)) {
                                    this.onTyping();
                                    this.doPaste();
                                    return true;
                                }
                            } else if (var1 != 66) {
                                if (var1 == 67) {
                                    if (this.inputViewHasFocus()) {
                                        if ((var2.getFlags() & 2) != 2) {
                                            this.onTyping();
                                            this.getDoc().O();
                                        }

                                        return true;
                                    }

                                    return false;
                                }

                                switch (var1) {
                                    case 19:
                                        var10 = this.getDocView().getSelectionLimits();
                                        if (var10 == null || !var10.getIsActive()) {
                                            if (!var3 && !var4) {
                                                this.C();
                                            } else {
                                                this.A();
                                            }

                                            return true;
                                        }
                                    case 20:
                                        var10 = this.getDocView().getSelectionLimits();
                                        if (var10 == null || !var10.getIsActive()) {
                                            if (!var3 && !var4) {
                                                this.D();
                                            } else {
                                                this.B();
                                            }

                                            return true;
                                        }
                                    case 21:
                                    case 22:
                                        if (this.inputViewHasFocus() && (var2.getFlags() & 2) != 2) {
                                            this.onTyping();
                                            this.doArrowKey(var2);
                                            return true;
                                        }
                                }
                            } else if (this.inputViewHasFocus()) {
                                this.mIsComposing = false;
                                if ((var2.getFlags() & 2) != 0) {
                                    this.onTyping();
                                    return true;
                                }
                            }
                        } else if (this.inputViewHasFocus() && (var4 || var3)) {
                            this.onTyping();
                            this.doUnderline();
                            return true;
                        }
                    } else if (this.inputViewHasFocus() && (var4 || var3)) {
                        this.onTyping();
                        this.doBold();
                        return true;
                    }
                } else if (var4 || var3) {
                    this.onTyping();
                    if (var5) {
                        this.doRedo();
                    } else {
                        this.doUndo();
                    }

                    return true;
                }
            } else if (var4 || var3) {
                this.doSave();
                return true;
            }

            if (this.inputViewHasFocus()) {
                char var7 = (char) var2.getUnicodeChar();
                if (var7 != 0) {
                    this.onTyping();
                    String var9 = "" +
                            var7;
                    this.getDoc().setSelectionText(var9);
                }
            }

            return true;
        }
    }

    public void doPaste() {
        this.mSession.getDoc().a(this.getContext(), this.getTargetPageNumber());
        this.updateInputView();
    }

    public void doRedo() {
        int var1 = this.mSession.getDoc().getCurrentEdit();
        if (var1 < this.mSession.getDoc().getNumEdits()) {
            this.getDoc().clearSelection();
            this.mSession.getDoc().setCurrentEdit(var1 + 1);
            this.updateInputView();
        }
    }

    public void doSave() {
        if (this.mIsTemplate) {
            this.saveTemplate(false);
        } else {
            this.preSaveQuestion(new Runnable() {
                public void run() {
                    final ProgressDialog var1 = Utilities.createAndShowWaitSpinner(NUIDocView.this.getContext());
                    NUIDocView.this.mSession.getDoc().a(NUIDocView.this.mFileState.getInternalPath(), new SODocSaveListener() {
                        public void onComplete(int var1x, int var2) {
                            var1.dismiss();
                            if (var1x == 0) {
                                NUIDocView.this.mFileState.saveFile();
                                NUIDocView.this.updateUIAppearance();
                                if (NUIDocView.this.mDataLeakHandlers != null) {
                                    NUIDocView.this.mDataLeakHandlers.postSaveHandler(new SOSaveAsComplete() {
                                        public void onComplete(int var1x, String var2) {
                                            NUIDocView.this.reloadFile();
                                        }
                                    });
                                }
                            } else {
                                String var3 = String.format(NUIDocView.this.activity().getString(R.string.sodk_editor_error_saving_document_code), var2);
                                try {
                                    Utilities.showMessage(NUIDocView.this.activity(), NUIDocView.this.activity().getString(R.string.sodk_editor_error), var3);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }
                    });
                }
            }, new Runnable() {
                public void run() {
                }
            });
        }
    }

    public void doSelectAll() {
        this.getDocView().selectTopLeft();
        this.mSession.getDoc().processKeyCommand(6);
        this.mSession.getDoc().processKeyCommand(15);
    }

    public void doStrikethrough() {
        SODoc var1 = this.mSession.getDoc();
        boolean var2 = var1.getSelectionIsLinethrough() ^ true;
        this.mStyleLinethroughButton.setSelected(var2);
        var1.setSelectionIsLinethrough(var2);
    }

    public void doUnderline() {
        SODoc var1 = this.mSession.getDoc();
        boolean var2 = var1.getSelectionIsUnderlined() ^ true;
        this.mStyleUnderlineButton.setSelected(var2);
        var1.setSelectionIsUnderlined(var2);
    }

    public void doUndo() {
        if (this.mSession != null) {
            int var1 = this.mSession.getDoc().getCurrentEdit();
            if (var1 > 0) {
                this.getDoc().clearSelection();
                this.mSession.getDoc().setCurrentEdit(var1 - 1);
                this.updateInputView();
            }
        }
    }

    public boolean documentHasBeenModified() {
        SODocSession var1 = this.mSession;
        boolean var2;
        if (var1 == null || var1.getDoc() == null || this.mFileState == null || !this.mSession.getDoc().getHasBeenModified() && !this.mFileState.hasChanges()) {
            var2 = false;
        } else {
            var2 = true;
        }

        return var2;
    }

    public void endDocSession(boolean var1) {
        DocView var2 = this.mDocView;
        if (var2 != null) {
            var2.finish();
        }

        if (this.usePagesView()) {
            DocListPagesView var3 = this.mDocPageListView;
            if (var3 != null) {
                var3.finish();
            }
        }

        SODocSession var4 = this.mSession;
        if (var4 != null) {
            var4.endSession(var1);
        }

        this.m();
    }

    protected void enforceInitialShowUI(View var1) {

        boolean var2 = this.mConfigOptions.a();
        View var3 = var1.findViewById(R.id.tabhost);
        byte var4 = 8;
        byte var5;
        if (var3 != null) {
            if (var2) {
                var5 = 0;
            } else {
                var5 = 8;
            }

            var3.setVisibility(var5);
        }

        var1 = var1.findViewById(R.id.footer);
        var5 = var4;
        if (var2) {
            var5 = 0;
        }

        var1.setVisibility(var5);
        this.mFullscreen = !var2;
    }

    protected void fixFileToolbar(int var1) {
        LinearLayout var2 = (LinearLayout) this.findViewById(var1);
        if (var2 != null) {
            View var3 = null;
            int var4 = 0;
            boolean var8 = false;

            boolean var5;
            View var7;
            for (var5 = false; var4 < var2.getChildCount(); var3 = var7) {
                View var6 = var2.getChildAt(var4);
                if (var6 instanceof LinearLayout) {
                    var7 = var3;
                    if (var6.getVisibility() == VISIBLE) {
                        var8 = true;
                        var5 = true;
                        var7 = var3;
                    }
                } else {
                    if (!var5) {
                        var6.setVisibility(GONE);
                    } else if (!var8 && var3 != null) {
                        var3.setVisibility(GONE);
                    }

                    var7 = var6;
                    var8 = false;
                }

                ++var4;
            }

            if (var3 != null && !var8) {
                var3.setVisibility(GONE);
            }

            if (!var5) {
                var2.setVisibility(GONE);
                ((View) var2.getParent()).setVisibility(GONE);
            }

        }
    }

    protected void focusInputView() {

        if (!k.c().b()) {
            this.defocusInputView();
        } else {
            InputView var1 = this.mInputView;
            if (var1 != null) {
                var1.setFocus();
            }

        }
    }

    public void forceReload() {
        this.mForceReload = true;
    }

    public void forceReloadAtResume() {
        this.mForceReloadAtResume = true;
    }

    public int getBorderColor() {
        return viewx.core.content.a.c(this.getContext(), R.color.sodk_editor_selected_page_border_color);
    }

    protected String getCurrentTab() {
        return this.mCurrentTab;
    }

    public SODoc getDoc() {
        SODocSession var1 = this.mSession;
        return var1 == null ? null : var1.getDoc();
    }

    public String getDocFileExtension() {
        SOFileState var1 = this.mState;
        String var2;
        if (var1 != null) {
            var2 = var1.getUserPath();
        } else {
            SODocSession var3 = this.mSession;
            if (var3 == null) {
                var2 = com.artifex.solib.a.a(this.getContext(), this.mStartUri);
                return var2;
            }

            var2 = var3.getUserPath();
        }

        var2 = com.artifex.solib.a.h(var2);
        return var2;
    }

    public DocListPagesView getDocListPagesView() {
        return this.mDocPageListView;
    }

    public DocView getDocView() {
        return this.mDocView;
    }

    protected int getInitialTab() {
        return 0;
    }

    public InputView getInputView() {
        return this.mInputView;
    }

    public boolean getIsComposing() {
        return this.mIsComposing;
    }

    public int getKeyboardHeight() {
        return this.keyboardHeight;
    }

    protected int getLayoutId() {
        return 0;
    }

    protected int getPageCount() {
        return this.mPageCount;
    }

    protected String getPageNumberText() {
        return String.format(this.getContext().getString(R.string.sodk_editor_page_d_of_d), this.mCurrentPageNum + 1, this.getPageCount());
    }

    public SODocSession getSession() {
        return this.mSession;
    }

    protected int getStartPage() {
        return this.mStartPage;
    }

    protected NUIDocView.TabData[] getTabData() {
        if (this.mTabs == null) {
            this.mTabs = new NUIDocView.TabData[5];
            if (this.mConfigOptions.b()) {
                this.mTabs[0] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_file), R.id.fileTab, R.layout.sodk_editor_tab_left, 0);
                this.mTabs[1] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_edit), R.id.editTab, R.layout.sodk_editor_tab, 0);
                this.mTabs[2] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_insert), R.id.insertTab, R.layout.sodk_editor_tab, 0);
                this.mTabs[3] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_pages), R.id.pagesTab, R.layout.sodk_editor_tab, 0);
                this.mTabs[4] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_review), R.id.reviewTab, R.layout.sodk_editor_tab_right, 0);
            } else {
                this.mTabs[0] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_file), R.id.fileTab, R.layout.sodk_editor_tab_left, 0);
                this.mTabs[1] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_edit), R.id.editTab, R.layout.sodk_editor_tab, 8);
                this.mTabs[2] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_insert), R.id.insertTab, R.layout.sodk_editor_tab, 8);
                this.mTabs[3] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_pages), R.id.pagesTab, R.layout.sodk_editor_tab_right, 0);
                this.mTabs[4] = new NUIDocView.TabData(this.getContext().getString(R.string.sodk_editor_tab_review), R.id.reviewTab, R.layout.sodk_editor_tab_right, 8);

            }
        }

        if (!k.e(this.activity()) && this.mConfigOptions.a == null) {
            NUIDocView.TabData[] var1 = this.mTabs;
            var1[4].visibility = 8;
            var1[3].layoutId = R.layout.sodk_editor_tab_right;
        }

        return this.mTabs;
    }

    protected int getTabSelectedColor() {
        Activity var1;
        int var2;
        if (this.getResources().getInteger(R.integer.sodk_editor_ui_doc_tab_color_from_doctype) == 0) {
            var1 = this.activity();
            var2 = R.color.sodk_editor_header_color_selected;
        } else {
            var1 = this.activity();
            var2 = R.color.sodk_editor_header_doc_color;
        }

        return viewx.core.content.a.c(var1, var2);
    }

    protected int getTabSelectedTextColor() {
        return viewx.core.content.a.c(this.activity(), R.color.sodk_editor_header_text_color_selected);
    }

    protected int getTabUnselectedColor() {
        Activity var1;
        int var2;
        if (this.getResources().getInteger(R.integer.sodk_editor_ui_doc_tabbar_color_from_doctype) == 0) {
            var1 = this.activity();
            var2 = R.color.sodk_editor_header_color;
        } else {
            var1 = this.activity();
            var2 = R.color.sodk_editor_header_doc_color;
        }

        return viewx.core.content.a.c(var1, var2);
    }

    protected int getTabUnselectedTextColor() {
        return viewx.core.content.a.c(this.activity(), R.color.sodk_editor_header_text_color);
    }

    protected int getTargetPageNumber() {
        DocPageView var1 = this.getDocView().findPageContainingSelection();
        if (var1 != null) {
            return var1.getPageNumber();
        } else {
            Rect var3 = new Rect();
            this.getDocView().getGlobalVisibleRect(var3);
            var1 = this.getDocView().findPageViewContainingPoint((var3.left + var3.right) / 2, (var3.top + var3.bottom) / 2, true);
            int var2 = 0;
            if (var1 != null) {
                var2 = var1.getPageNumber();
            }

            return var2;
        }
    }

    protected void goBack(Boolean isConfirmBack) {
        if (isFullScreen()) {
            onFullScreenBack();
            mFullscreen = false;
            return;
        }

        if (isMenuOpen) {
            ViewUtilsKt.collapse(lnHeader);
            isMenuOpen = false;
            return;
        }

        this.prepareToGoBack();
        if (this.documentHasBeenModified()) {
            this.activity().runOnUiThread(new Runnable() {
                public void run() {
                    int var1 = R.string.sodk_editor_save;
                    int var2 = var1;
                    if (NUIDocView.this.mCustomDocdata != null) {
                        int var3 = NUIDocView.this.getContext().getResources().getIdentifier("secure_save_upper", "string", NUIDocView.this.getContext().getPackageName());
                        var2 = var1;
                        if (var3 != 0) {
                            var2 = var3;
                        }
                    }

                    (new Builder(NUIDocView.this.activity(), R.style.sodk_editor_alert_dialog_style)).setTitle(R.string.sodk_editor_document_has_been_modified).setMessage(R.string.sodk_editor_would_you_like_to_save_your_changes).setCancelable(false).setPositiveButton(var2, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface var1, int var2) {

                            var1.dismiss();
                            NUIDocView.this.preSaveQuestion(new Runnable() {
                                public void run() {
                                    if (NUIDocView.this.mCustomDocdata != null) {
                                        NUIDocView.this.onCustomSaveButton((View) null);
                                    } else if (NUIDocView.this.mIsTemplate) {
                                        NUIDocView.this.saveTemplate(true);
                                    } else {
                                        NUIDocView.this.mSession.getDoc().a(NUIDocView.this.mFileState.getInternalPath(), new SODocSaveListener() {
                                            public void onComplete(int var1, int var2) {
                                                if (var1 == 0) {
                                                    NUIDocView.this.mFileState.saveFile();
                                                    if (NUIDocView.this.mDataLeakHandlers != null) {
                                                        NUIDocView.this.mDataLeakHandlers.postSaveHandler(new SOSaveAsComplete() {
                                                            public void onComplete(int var1, String var2) {
                                                                if (var1 == 0) {
                                                                    NUIDocView.this.mFileState.closeFile();
                                                                    NUIDocView.this.prefinish();
                                                                }

                                                            }
                                                        });
                                                    } else {
                                                        NUIDocView.this.mFileState.closeFile();
                                                        NUIDocView.this.prefinish();
                                                    }
                                                } else {
                                                    NUIDocView.this.mFileState.closeFile();
                                                    String var3 = String.format(NUIDocView.this.activity().getString(R.string.sodk_editor_error_saving_document_code), var2);
                                                    try {
                                                        Utilities.showMessage(NUIDocView.this.activity(), NUIDocView.this.activity().getString(R.string.sodk_editor_error), var3);
                                                    } catch (Exception e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }

                                            }
                                        });
                                    }

                                }
                            }, new Runnable() {
                                public void run() {
                                }
                            });
                        }
                    }).setNegativeButton(R.string.sodk_editor_discard, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface var1, int var2) {
                            try {
                                var1.dismiss();
                                NUIDocView.this.mFileState.closeFile();
                                NUIDocView.this.mEndSessionSilent = Boolean.FALSE;
                                NUIDocView.this.prefinish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).setNeutralButton(R.string.sodk_editor_continue_editing, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface var1, int var2) {
                            var1.dismiss();
                        }
                    }).create().show();
                }
            });

        } else if (isConfirmBack) {

            new Builder(getContext()).setTitle(R.string.exit).setMessage(R.string.do_you_close_file)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            NUIDocView.this.mFileState.closeFile();
                            NUIDocView.this.mEndSessionSilent = Boolean.FALSE;
                            NUIDocView.this.prefinish();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

        } else {
            if (NUIDocView.this.mFileState != null) {
                NUIDocView.this.mFileState.closeFile();
            }
            NUIDocView.this.mEndSessionSilent = Boolean.FALSE;
            NUIDocView.this.prefinish();
        }

    }

    public void goToPage(int var1) {
        this.goToPage(var1, false);
    }

    public void goToPage(int var1, boolean var2) {
        this.mDocView.scrollToPage(var1, var2);
        if (this.usePagesView()) {
            this.mDocPageListView.scrollToPage(var1, var2);
        }
    }

    protected void handlePagesTab(String var1) {
        if (var1.equals(this.activity().getString(R.string.sodk_editor_tab_pages))) {
            this.showPages();
        } else {
            this.hidePages();
        }

    }

    protected void handleStartPage() {
        int var1 = this.getStartPage();
        if (this.getStartPage() > 0 && this.getPageCount() >= this.getStartPage()) {
            this.setStartPage(0);
            this.mDocView.setStartPage(var1);
            this.mCurrentPageNum = var1 - 1;
            this.setPageNumberText();
            this.mDocView.requestLayout();
            this.j();
        }

    }

    protected boolean hasRedo() {
        return true;
    }

    protected boolean hasSearch() {
        return true;
    }

    protected boolean hasUndo() {
        return true;
    }

    protected void hidePages() {
        RelativeLayout var1 = (RelativeLayout) this.findViewById(R.id.pages_container);
        if (var1 != null && var1.getVisibility() != GONE) {
            this.mDocView.onHidePages();
            var1.setVisibility(GONE);
        }

    }

    protected boolean inputViewHasFocus() {

        InputView var1 = this.mInputView;
        return var1 != null && var1.hasFocus();
    }

    protected boolean isActivityActive() {
        return this.mIsActivityActive;
    }

    public boolean isFullScreen() {
        return this.mConfigOptions.z() && this.mFullscreen;
    }

    public boolean isKeyboardVisible() {
        return this.keyboardShown;
    }

    public boolean isLandscapePhone() {
        boolean var1;
        if (this.mLastOrientation == 2 && Utilities.isPhoneDevice(this.getContext())) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    public boolean isPageListVisible() {
        RelativeLayout var1 = (RelativeLayout) this.findViewById(R.id.pages_container);
        return var1 != null && var1.getVisibility() == VISIBLE;
    }

    protected boolean isPagesTab() {
        return this.getCurrentTab().equals(this.activity().getString(R.string.sodk_editor_tab_pages));
    }

    protected boolean isRedactionMode() {
        return false;
    }

    protected boolean isSearchVisible() {
        View var1 = this.findViewById(R.id.search_text_input);
        return var1 != null && var1.getVisibility() == VISIBLE && var1.isShown();
    }

    protected void layoutAfterPageLoad() {

        this.layoutNow();
    }

    public void layoutNow() {
        DocView var1 = this.mDocView;
        if (var1 != null) {
            var1.layoutNow();
        }

        if (this.mDocPageListView != null && this.usePagesView() && this.isPageListVisible()) {
            this.mDocPageListView.layoutNow();
        }

    }

    public void onActivityResult(int var1, int var2, Intent var3) {
        SODataLeakHandlers var4 = this.mDataLeakHandlers;

        if (var4 != null) {
            var4.onActivityResult(var1, var2, var3);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void onAlignCenterButton(View var1) {
        this.mSession.getDoc().F();
    }

    public void onAlignJustifyButton(View var1) {
        this.mSession.getDoc().H();
    }

    public void onAlignLeftButton(View var1) {
        this.mSession.getDoc().E();
    }

    public void onAlignRightButton(View var1) {
        this.mSession.getDoc().G();
    }

    public void onAuthorButton(View var1) {
        final SODoc var2 = this.getDocView().getDoc();
        AuthorDialog.show(this.activity(), new AuthorDialogListener() {
            public void onCancel() {
            }

            public void onOK(String var1) {
                var2.setAuthor(var1);
                Utilities.setStringPreference(Utilities.getPreferencesObject(NUIDocView.this.activity(), "general"), "DocAuthKey", var1);
            }
        }, var2.getAuthor());
    }

    public void onBackPressed(Boolean isConfirmBack) {
        // interstitial
        goBack(isConfirmBack);
    }

    public void onClick(View var1) {
        if (fontWindow != null) {
            fontWindow.dismiss();
        }
        if (fontSizeWindow != null) {
            fontSizeWindow.dismiss();
        }

        if (var1 != null) {
            if (var1 == this.mStyleTextColorButton) {
                onColorButton(true);
            }

            if (var1 == this.mStyleTextHighlightButton) {
                onColorButton(false);
            }

            if (var1 == this.mColorRed) {
                onFontColorButton(R.color.color_text_red);
            }

            if (var1 == this.mColorYellow) {
                onFontColorButton(R.color.color_text_yellow);
            }

            if (var1 == this.mColorOrange) {
                onFontColorButton(R.color.color_text_orange);
            }

            if (var1 == this.mColorGreen) {

                onFontColorButton(R.color.color_text_green);
            }

            if (var1 == this.mColorBlue) {
                onFontColorButton(R.color.color_text_blue);
            }

            if (var1 == this.mColorPurple) {
                onFontColorButton(R.color.color_text_purple);
            }

            if (var1 == this.mColorBlack) {
                onFontColorButton(R.color.color_text_black);
            }

            if (var1 == this.mColorWhite) {
                onFontColorButton(R.color.color_text_white);

            }

            if (var1 == this.mHighlightRed) {
                onFontBackgroundButton(R.color.color_text_red);

            }

            if (var1 == this.mHighlightYellow) {
                onFontBackgroundButton(R.color.color_text_yellow);

            }

            if (var1 == this.mHighlightOrange) {
                onFontBackgroundButton(R.color.color_text_orange);

            }

            if (var1 == this.mHighlightGreen) {

                onFontBackgroundButton(R.color.color_text_green);
            }

            if (var1 == this.mHighlightBlue) {

                onFontBackgroundButton(R.color.color_text_blue);
            }

            if (var1 == this.mHighlightPurple) {
                onFontBackgroundButton(R.color.color_text_purple);
            }

            if (var1 == this.mHighlightBlack) {
                onFontBackgroundButton(R.color.color_text_black);
            }

            if (var1 == this.mHighlightWhite) {
                onFontBackgroundClear();
//                onFontBackgroundButton(R.color.color_text_white);
            }

            if (var1 == this.lnMenuFile) {
                onBottomMenuButton(mFileTab, getResources().getString(R.string.sodk_editor_tab_file));
            }

            if (var1 == this.lnMenuTypo) {
                onBottomMenuButton(mHomeTab, getResources().getString(R.string.sodk_editor_tab_typo));
            }

            if (var1 == this.lnMenuEdit) {
                onBottomMenuButton(mEditTab, getResources().getString(R.string.sodk_editor_tab_edit));
            }

            if (var1 == this.lnMenuInsert) {
                onBottomMenuButton(mInsertTab, getResources().getString(R.string.sodk_editor_tab_insert));
            }

            if (var1 == this.lnMenuFormat) {
                onBottomMenuButton(mFormatTab, getResources().getString(R.string.sodk_editor_tab_format));
            }

            if (var1 == this.lnMenuFormulas) {
                onBottomMenuButton(mFormulasTab, getResources().getString(R.string.sodk_editor_tab_formulas));
            }

            if (var1 == this.lnMenuSlide) {
                onBottomMenuButton(mSlideTab, getResources().getString(R.string.sodk_editor_tab_slides));
            }

            if (var1 == this.lnMenuPage) {
                onBottomMenuButton(mPagesTab, getResources().getString(R.string.sodk_editor_tab_pages));
            }

            if (var1 == this.lnMenuAnnotate) {
                onBottomMenuButton(mAnnotateTab, getResources().getString(R.string.sodk_editor_tab_annotate));
            }

            if (var1 == this.mSaveAsButton) {
                this.onSaveAsButton(var1);
            }

            if (var1 == this.mSaveButton) {
                try {
                    this.onSaveButton(var1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (var1 == this.mCustomSaveButton) {
                this.onCustomSaveButton(var1);
            }

            if (var1 == this.mSavePdfButton) {
                this.onSavePDFButton(var1);
            }

            if (var1 == this.mPrintButton) {
                try {
                    this.onPrintButton(var1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                this.onPrintButton(var1);
            }

            if (var1 == this.mShareButton) {
                this.onShareButton(var1);
            }

            if (var1 == this.mOpenInButton) {
                this.onOpenInButton(var1);
            }

            if (var1 == this.mOpenPdfInButton) {
                this.onOpenPDFInButton(var1);
            }

            if (var1 == this.mProtectButton) {
                this.onProtectButton(var1);
            }

            if (var1 == this.mFontUpButton) {
                this.onFontUpButton(var1);
            }

            if (var1 == this.mFontDownButton) {
                this.onFontDownButton(var1);
            }

            if (var1 == this.mFontSizeText) {
                this.onTapFontSize(var1);
            }

            if (var1 == this.mFontNameText || var1 == this.mLayoutFont) {
                this.onTapFontName(var1);
            }

//            if (var1 == this.mFontColorButton) {
//                this.onFontColorButton(var1);
//            }

//            if (var1 == this.mFontBackgroundButton) {
//                this.onFontBackgroundButton(var1);
//            }

            if (var1 == this.mCutButton) {
                this.onCutButton(var1);
            }

            if (var1 == this.mCopyButton) {
                this.onCopyButton(var1);
            }

            if (var1 == this.mCopyButton2) {
                this.onCopyButton(var1);
            }

            if (var1 == this.mPasteButton) {
                this.onPasteButton(var1);
            }

            if (var1 == this.mStyleBoldButton) {
                this.doBold();
            }

            if (var1 == this.mStyleItalicButton) {
                this.doItalic();
            }

            if (var1 == this.mStyleUnderlineButton) {
                this.doUnderline();
            }

            if (var1 == this.mStyleLinethroughButton) {
                this.doStrikethrough();
            }

            if (var1 == this.mListBulletsButton) {
                this.onListBulletsButton(var1);
            }

            if (var1 == this.mListNumbersButton) {
                this.onListNumbersButton(var1);
            }

            if (var1 == this.mAlignLeftButton) {
                this.onAlignLeftButton(var1);
            }

            if (var1 == this.mAlignCenterButton) {
                this.onAlignCenterButton(var1);
            }

            if (var1 == this.mAlignRightButton) {
                this.onAlignRightButton(var1);
            }

            if (var1 == this.mAlignJustifyButton) {
                this.onAlignJustifyButton(var1);
            }

            if (var1 == this.mIncreaseIndentButton) {
                this.onIndentIncreaseButton(var1);
            }

            if (var1 == this.mDecreaseIndentButton) {
                this.onIndentDecreaseButton(var1);
            }

            if (var1 == this.mFirstPageButton) {
                this.onFirstPageButton(var1);
            }

            if (var1 == this.mLastPageButton) {
                this.onLastPageButton(var1);
            }

            if (var1 == this.mReflowButton) {
                this.onReflowButton(var1);
            }

            if (var1 == this.mUndoButton) {
                this.onUndoButton(var1);
            }

            if (var1 == this.mRedoButton) {
                this.onRedoButton(var1);
            }

            if (var1 == this.mSearchButton) {
                this.onSearchButton(var1);
            }

            if (var1 == this.mSearchNextButton) {
                this.onSearchNext(var1);
            }

            if (var1 == this.mSearchPreviousButton) {
                this.onSearchPrevious(var1);
            }

            if (var1 == this.mSearchCloseButton) {
                this.onSearchClose(var1);
            }

            if (var1 == this.mBackButton) {
                onBackPressed(false);
            }


            if (var1 == this.mInsertImageButton) {
                this.onInsertImageButton(var1);
            }

            if (var1 == this.mInsertPhotoButton) {
//                this.onInsertPhotoButton(var1);

                checkPermissions(!MyUtils.Companion.shouldShowRequestPermissionRationale(
                        activity(),
                        cmsPms
                ));
            }

            if (this.mConfigOptions.z()) {
                ImageView var2 = this.mFullscreenButton;
                if (var2 != null && var1 == var2) {
                    this.onFullScreen(var1);
                }
            }

            if (var1 == this.mFormulasButton) {
                onFormulasButton();
            }

            if (var1 == this.mButtonKeyboard) {
                if (keyboardShown) {
                    Utilities.hideKeyboard(getContext());
                } else {
                    Utilities.showKeyboard(getContext());
                }
            }

            if (var1 == this.mButtonOutFullScreen) {
                onFullScreenBack();
                mFullscreen = false;
                return;
            }
        }
    }

    String[] cmsPms = new String[]{Manifest.permission.CAMERA};
    int CODE_PMS = 1009;
    private boolean isListenCameraPms = false;

    private void checkPermissions(boolean showRepeat) {
        if (MyUtils.Companion.hasPermission(activity(), cmsPms)) {
            AppOpenManager.getInstance().disableAppResume();
            startItemFlow();
        } else if (showRepeat) {
            isListenCameraPms = true;
            ActivityCompat.requestPermissions(activity(), cmsPms, CODE_PMS);
        } else {
        }
    }


    public void startItemFlow() {
        this.onInsertPhotoButton(this.mInsertPhotoButton);
    }

    private boolean isMenuOpen = false;

    protected void onBottomMenuButton(View var1, String titleText) {
        if (lnBottom != null) {
            lnBottom.setVisibility(GONE);
        }
        currentTabId = var1.getId();
        if (mFileTab != null) {
            mFileTab.setVisibility(View.GONE);
        }
        if (mEditTab != null) {
            mEditTab.setVisibility(View.GONE);
        }

        if (mHomeTab != null) {
            mHomeTab.setVisibility(View.GONE);
        }

        if (mAnnotateTab != null) {
            mAnnotateTab.setVisibility(View.GONE);
        }
        if (mInsertTab != null) {
            mInsertTab.setVisibility(View.GONE);
        }
        if (mFormatTab != null) {
            mFormatTab.setVisibility(View.GONE);
        }
        if (mFormulasTab != null) {
            mFormulasTab.setVisibility(View.GONE);
        }
        if (mSlideTab != null) {
            mSlideTab.setVisibility(View.GONE);
        }
        if (mPagesTab != null) {
            mPagesTab.setVisibility(View.GONE);
        }
        if (mSearchTab != null) {
            mSearchTab.setVisibility(View.GONE);
        }

        if (mReviewTab != null) {
            mReviewTab.setVisibility(View.GONE);
        }

        if (mRedactTab != null) {
            mRedactTab.setVisibility(View.GONE);
        }

        if (lnEditor != null) {
            if (var1 != mFileTab) {
                lnEditor.setVisibility(VISIBLE);
            } else {
                lnEditor.setVisibility(GONE);
            }
        }

        if (lnRollback != null) {
            if (var1 == mPagesTab || var1 == mSearchTab) {
                lnRollback.setVisibility(GONE);
            } else {
                lnRollback.setVisibility(VISIBLE);
            }
        }

        var1.setVisibility(View.VISIBLE);
        isMenuOpen = false;
        Utilities.hideKeyboard(getContext(), mSearchTab);
    }

    public void onConfigurationChange(Configuration var1) {
        if (var1 != null && var1.keyboard != this.mPrevKeyboard) {
            this.resetInputView();
            this.mPrevKeyboard = var1.keyboard;
        }

        DocView var2 = this.mDocView;
        if (var2 != null) {
            var2.onConfigurationChange();
        }

        if (this.usePagesView()) {
            DocListPagesView var3 = this.mDocPageListView;
            if (var3 != null) {
                var3.onConfigurationChange();
            }
        }

    }

    public void onCopyButton(View var1) {
        this.doCopy();
    }

    public void onCustomSaveButton(View var1) {
        if (this.mDataLeakHandlers != null) {
            if (!this.mConfigOptions.C()) {
                try {
                    Utilities.showMessage(this.activity(), this.getContext().getString(R.string.sodk_editor_error), this.getContext().getString(R.string.sodk_editor_has_no_permission_to_save));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            this.preSaveQuestion(new Runnable() {
                public void run() {
                    String var1 = NUIDocView.this.mFileState.getUserPath();
                    String var2 = var1;
                    if (var1 == null) {
                        var2 = NUIDocView.this.mFileState.getOpenedPath();
                    }

                    File var8 = new File(var2);
                    NUIDocView.this.preSave();

                    try {
                        SODataLeakHandlers var9 = NUIDocView.this.mDataLeakHandlers;
                        String var3 = var8.getName();
                        SODoc var4 = NUIDocView.this.mSession.getDoc();
                        var1 = NUIDocView.this.mCustomDocdata;
                        SOCustomSaveComplete var5 = new SOCustomSaveComplete() {
                            public void onComplete(int var1, String var2, boolean var3) {
                                NUIDocView.this.mFileState.setHasChanges(false);
                                if (var1 == 0) {
                                    NUIDocView.this.mFileState.setHasChanges(false);
                                }

                                if (var3) {
                                    NUIDocView.this.prefinish();
                                }

                            }
                        };
                        var9.customSaveHandler(var3, var4, var1, var5);
                    } catch (UnsupportedOperationException var6) {
                    } catch (IOException var7) {
                    }
                }
            }, new Runnable() {
                public void run() {
                }
            });
        }

    }

    public void onCutButton(View var1) {
        this.doCut();
    }

    public void onDestroy() {
//

        this.releaseDocViewBitmap();
        if (this.mDeleteOnClose != null) {
            for (int var1 = 0; var1 < this.mDeleteOnClose.size(); ++var1) {
                com.artifex.solib.a.f((String) this.mDeleteOnClose.get(var1));
            }

            this.mDeleteOnClose.clear();
        }

        SODataLeakHandlers var2 = this.mDataLeakHandlers;
        if (var2 != null) {
            var2.finaliseDataLeakHandlers();
        }

    }

    protected void onDeviceSizeChange() {
//        View var1 = this.findViewById(id.back_button_after);
//        ImageView var2;
//        int var3;
//        TabHost var4;
//        Resources var5;
//        if (Utilities.isPhoneDevice(this.activity())) {
//            this.scaleHeader();
//            var2 = this.mSearchButton;
//            if (var2 != null) {
//                var2.setVisibility(INVISIBLE);
//            }
//
//            var4 = this.tabHost;
//            if (var4 != null) {
//                var4.getTabWidget().getLayoutParams().width = Utilities.convertDpToPixel(150.0F);
//                this.o();
//                this.getSingleTabView().setVisibility(VISIBLE);
//            }
//
//            var5 = this.getContext().getResources();
//            var3 = dimen.sodk_editor_after_back_button_phone;
//        } else {
//            if (this.hasSearch()) {
//                var2 = this.mSearchButton;
//                if (var2 != null) {
//                    var2.setVisibility(VISIBLE);
//                }
//            }
//
//            var4 = this.tabHost;
//            if (var4 != null) {
//                var4.getTabWidget().getLayoutParams().width = Utilities.convertDpToPixel(550.0F);
//                this.p();
//                this.getSingleTabView().setVisibility(GONE);
//            }
//
//            var5 = this.getContext().getResources();
//            var3 = dimen.sodk_editor_after_back_button;
//        }
//
//        var3 = (int) var5.getDimension(var3);
//        var1.getLayoutParams().width = Utilities.convertDpToPixel((float) var3);
    }

    protected void onDocCompleted() {

        if (!this.mFinished) {
            int var1 = this.mSession.getDoc().r();
            this.mPageCount = var1;
            this.mAdapter.setCount(var1);
            this.layoutNow();
            if (k.e(this.activity())) {
                String var2 = Utilities.getStringPreference(Utilities.getPreferencesObject(this.activity(), "general"), "DocAuthKey", Utilities.getApplicationName(this.activity()));
                this.mSession.getDoc().setAuthor(var2);
            }

        }
    }

    public void onFirstPageButton(View var1) {
        this.mDocView.goToFirstPage();
        if (this.usePagesView()) {
            this.mDocPageListView.goToFirstPage();
        }

    }

    public void onFontBackgroundClear() {
        NUIDocView.this.mSession.getDoc().setSelectionBackgroundTransparent();
    }

    public void onFontBackgroundButton(@ColorRes int colorId) {
        String color = "#" + Integer.toHexString(ContextCompat.getColor(getContext(), colorId))
                .replace("ff", "");
        NUIDocView.this.mSession.getDoc().setSelectionBackgroundColor(color);

//        (new ColorDialogLib(2, this.getContext(), this.mSession.getDoc(), this.mDocView, new ColorChangedListener() {
//            public void onColorChanged(String var1) {
//                if (var1.equals("transparent")) {
//                    NUIDocView.this.mSession.getDoc().setSelectionBackgroundTransparent();
//                } else {
//                    NUIDocView.this.mSession.getDoc().setSelectionBackgroundColor(var1);
//                }
//
//            }
//        })).show();
    }

    public void onFontColorButton(@ColorRes int colorId) {
        String color = "#" + Integer.toHexString(ContextCompat.getColor(getContext(), colorId))
                .replace("ff", "");
        NUIDocView.this.mSession.getDoc().setSelectionFontColor(color);

//        (new ColorDialogLib(1, this.getContext(), this.mSession.getDoc(), this.mDocView, new ColorChangedListener() {
//            public void onColorChanged(String var1) {
//                NUIDocView.this.mSession.getDoc().setSelectionFontColor("#ffffff");
//            }
//        })).show();
    }

    public void onColorButton(boolean textColor) {
        Utilities.hideKeyboard(this.getContext());
        if (lnFormulas != null) {
            lnFormulas.setVisibility(GONE);
        }
        if (this.lnBottom.getVisibility() == GONE) {
            this.lnMenuTextColor.setVisibility(textColor ? VISIBLE : GONE);
            this.lnMenuTextHighlight.setVisibility(textColor ? GONE : VISIBLE);
            ViewUtilsKt.showUp(lnBottom);
        } else {
            if (textColor && lnMenuTextColor.getVisibility() == VISIBLE ||
                    !textColor && lnMenuTextHighlight.getVisibility() == VISIBLE) {
                ViewUtilsKt.hideDown(lnBottom);
            } else {
                this.lnMenuTextColor.setVisibility(textColor ? VISIBLE : GONE);
                this.lnMenuTextHighlight.setVisibility(textColor ? GONE : VISIBLE);
            }
        }
    }

    public void onFormulasButton() {
        Utilities.hideKeyboard(this.getContext());
        if (this.lnBottom.getVisibility() == GONE) {
            this.lnMenuTextColor.setVisibility(GONE);
            this.lnMenuTextHighlight.setVisibility(GONE);
            this.lnFormulas.setVisibility(VISIBLE);
            ViewUtilsKt.showUp(lnBottom);
        } else {
            ViewUtilsKt.hideDown(lnBottom);
        }
    }

    public void onFontDownButton(View var1) {
        if (this.mSession.getDoc() != null) {
            double var2 = this.mSession.getDoc().getSelectionFontSize();
            if ((int) var2 > MIN_FONT_SIZE) {
                this.mSession.getDoc().setSelectionFontSize(var2 - 1.0D);
            }
        }
    }

    public void onFontUpButton(View var1) {
        if (mSession != null) {
            double var2 = this.mSession.getDoc().getSelectionFontSize();
            if ((int) var2 < MAX_FONT_SIZE) {
                this.mSession.getDoc().setSelectionFontSize(var2 + 1.0D);
            }
        }
    }

    protected void onFullScreen(View var1) {
        if (!this.mFinished) {
            if (this.getDocView() != null) {
                if (!this.isFullScreen()) {
                    if (this.mConfigOptions.z()) {
                        this.mFullscreen = true;
                        Utilities.hideKeyboard(this.getContext());
                        this.getDocView().onFullscreen(true);
                        this.onFullScreenHide();
//                        if (this.mFullscreenToast == null) {
//                            String var2 = this.getContext().getString(string.sodk_editor_fullscreen_warning);
//                            Toast var3 = Toast.makeText(this.getContext(), var2, Toast.LENGTH_SHORT);
//                            this.mFullscreenToast = var3;
//                            var3.setGravity(53, 0, 0);
//                            ((TextView) ((ViewGroup) this.mFullscreenToast.getView()).getChildAt(0)).setTextSize(16.0F);
//                        }

//                        this.mFullscreenToast.show();
                    }
                }
            }
        }
    }

    protected void onFullScreenHide() {
        this.findViewById(R.id.ln_top_tool).setVisibility(GONE);
//        this.findViewById(id.tabhost).setVisibility(GONE);
        this.findViewById(R.id.header).setVisibility(GONE);
        this.findViewById(R.id.footer).setVisibility(GONE);
        this.hidePages();
        if (mButtonOutFullScreen != null) {
            mButtonOutFullScreen.setVisibility(VISIBLE);
        }
        this.layoutNow();
    }

    protected void onFullScreenBack() {
        this.findViewById(R.id.ln_top_tool).setVisibility(VISIBLE);
//        this.findViewById(id.tabhost).setVisibility(GONE);
        this.findViewById(R.id.header).setVisibility(VISIBLE);
        this.findViewById(R.id.footer).setVisibility(VISIBLE);
//        this.showPages();
        if (mButtonOutFullScreen != null) {
            mButtonOutFullScreen.setVisibility(GONE);
        }
        this.layoutNow();
    }

    public void onIndentDecreaseButton(View var1) {
        int[] var2 = this.mSession.getDoc().getIndentationLevel();
        if (var2 != null && var2[0] > 0) {
            this.mSession.getDoc().setIndentationLevel(var2[0] - 1);
        }

    }

    public void onIndentIncreaseButton(View var1) {
        int[] var2 = this.mSession.getDoc().getIndentationLevel();
        if (var2 != null && var2[0] < var2[1]) {
            this.mSession.getDoc().setIndentationLevel(var2[0] + 1);
        }

    }

    public void onInsertImageButton(View var1) {
        if (!checkStoragePermission(this.getContext())) {
            Utilities.showMessage((Activity) NUIDocView.this.getContext(),
                    NUIDocView.this.getContext().getString(R.string.edit),
                    NUIDocView.this.getContext().getString(R.string.accept_all_file_permission_edit2));
            return;
        }
        this.showKeyboard(false, new Runnable() {
            public void run() {
                if (NUIDocView.this.mDataLeakHandlers != null) {
                    try {
                        NUIDocView.this.mDataLeakHandlers.insertImageHandler(NUIDocView.this);
                    } catch (UnsupportedOperationException var2) {
                        var2.printStackTrace();
                    }

                } else {
                    throw new UnsupportedOperationException();
                }
            }
        });
    }

    public void onInsertPhotoButton(View var1) {
        this.showKeyboard(false, new Runnable() {
            public void run() {
                if (NUIDocView.this.mDataLeakHandlers != null) {
                    try {
                        NUIDocView.this.mDataLeakHandlers.insertPhotoHandler(NUIDocView.this);
                    } catch (UnsupportedOperationException var2) {
                    }
                    catch (Exception ignore){
                    }

                } else {
                    throw new UnsupportedOperationException();
                }
            }
        });
    }

    public boolean onKeyPreIme(int var1, KeyEvent var2) {
        BaseActivity var3 = (BaseActivity) this.getContext();
        if (!var3.isSlideShow()) {
            View var4 = var3.getCurrentFocus();
            if (var4 != null && var2.getKeyCode() == 4) {
                var4.clearFocus();
                Utilities.hideKeyboard(this.getContext());
                return true;
            }
        }

        return super.onKeyPreIme(var1, var2);
    }

    public void onLastPageButton(View var1) {
        this.mDocView.goToLastPage();
        if (this.usePagesView()) {
            this.mDocPageListView.goToLastPage();
        }

    }

    public void onLayoutChanged() {
        SODocSession var1 = this.mSession;
        if (var1 != null && var1.getDoc() != null && !this.mFinished) {
            this.mDocView.onLayoutChanged();
        }

    }

    public void onListBulletsButton(View var1) {
        LinearLayout var2;
        if (this.mListBulletsButton.isSelected()) {
            var2 = this.mListBulletsButton;
        } else {
            this.mListBulletsButton.setSelected(true);
            var2 = this.mListNumbersButton;
        }

        var2.setSelected(false);
        this.q();
    }

    public void onListNumbersButton(View var1) {
        LinearLayout var2;
        if (this.mListNumbersButton.isSelected()) {
            var2 = this.mListNumbersButton;
        } else {
            this.mListNumbersButton.setSelected(true);
            var2 = this.mListBulletsButton;
        }

        var2.setSelected(false);
        this.q();
    }

    protected void onMeasure(int var1, int var2) {
        this.c();
        super.onMeasure(var1, var2);
    }

    public void onOpenInButton(View var1) {
        this.preSave();
        if (this.mDataLeakHandlers != null) {
            try {
                File var4 = new File(this.mFileState.getOpenedPath());
                this.mDataLeakHandlers.openInHandler(var4.getName(), this.mSession.getDoc());
            } catch (NullPointerException var2) {
            } catch (UnsupportedOperationException var3) {
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void onOpenPDFInButton(View var1) {
        this.preSave();
        if (this.mDataLeakHandlers != null) {
            try {
                File var4 = new File(this.mFileState.getOpenedPath());
                this.mDataLeakHandlers.openPdfInHandler(var4.getName(), this.mSession.getDoc());
            } catch (NullPointerException var2) {
            } catch (UnsupportedOperationException var3) {
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected void onOrientationChange() {
        this.mDocView.onOrientationChange();
        if (this.usePagesView()) {
            this.mDocPageListView.onOrientationChange();
        }

        if (!this.isFullScreen()) {
            this.showUI(this.keyboardShown ^ true);
        }

        this.onDeviceSizeChange();
    }

    protected void onPageLoaded(int var1) {
        int var2 = this.mPageCount;
        boolean var3 = false;
        boolean var6;
        if (var2 == 0) {
            var6 = true;
        } else {
            var6 = false;
        }

        this.mPageCount = var1;
        if (var6) {
            this.setSearchListener();
            this.updateUIAppearance();
            LinearLayout var4 = this.mReflowButton;
            if (var4 != null) {
                highlightView(var4, true);
            }
        }

        var2 = this.mAdapter.getCount();
        boolean var5 = var3;
        if (this.mPageCount != var2) {
            var5 = true;
        }

        if (this.mPageCount < var2) {
            this.mDocView.removeAllViewsInLayout();
            if (this.usePagesView()) {
                this.mDocPageListView.removeAllViewsInLayout();
            }
        }

        this.mAdapter.setCount(this.mPageCount);
        if (var5) {
            final ViewTreeObserver var7 = this.getViewTreeObserver();
            var7.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    var7.removeOnGlobalLayoutListener(this);
                    if (!NUIDocView.this.mFinished) {
                        if (NUIDocView.this.mDocView.getReflowMode()) {
                            NUIDocView.this.onReflowScale();
                        } else {
                            NUIDocView.this.mDocView.scrollSelectionIntoView();
                        }

                    }
                }
            });
            this.layoutAfterPageLoad();
        } else {
            this.i();
        }

        this.handleStartPage();
        if (!this.mIsWaiting) {
            this.mIsWaiting = true;
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    NUIDocView.this.setPageNumberText();
                    NUIDocView.this.mIsWaiting = false;
                }
            }, 1000L);
        }


    }

    public void onPasteButton(View var1) {
        this.doPaste();
    }

    public void onPause() {
        this.onPauseCommon();
        DocView var1 = this.mDocView;
        if (var1 == null || !var1.finished()) {
            if (this.mFileState != null) {
                var1 = this.mDocView;
                if (var1 != null && var1.getDoc() != null && this.mDataLeakHandlers != null) {
                    SODoc var2 = this.mDocView.getDoc();
                    this.mDataLeakHandlers.pauseHandler(var2, var2.getHasBeenModified());
                }
            }

        }
    }

    protected void onPauseCommon() {
        this.mIsActivityActive = false;
        this.resetInputView();
    }

    public void onPrintButton(View var1) {
        if (this.mConfigOptions.B()) {
            try {
                Utilities.showMessage(this.activity(), this.getContext().getString(R.string.sodk_editor_error), this.getContext().getString(R.string.sodk_editor_has_no_permission_to_print));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            SODataLeakHandlers var3 = this.mDataLeakHandlers;
            if (var3 != null) {
                try {
                    if (filePath.endsWith(".pdf") && !TextUtils.isEmpty(mSession.getPassword())) {
                        RemovePasswordAsyncTask removePasswordAsyncTask = new RemovePasswordAsyncTask();
                        removePasswordAsyncTask.setOnResult(filePath -> {
                            try {
                                PrintManager printManager = (PrintManager) getContext().getSystemService(Context.PRINT_SERVICE);
                                String jobName = "print document";
                                printManager.print(jobName, new PdfDocumentAdapter(getContext(), new File(filePath)), null);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        removePasswordAsyncTask.execute();
                    } else {
                        var3.printHandler(this.mSession.getDoc());
                    }
                } catch (UnsupportedOperationException var2) {
                } catch (NullPointerException e) {
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    public interface OnResult {
        void onSuccessListener(String filePath);
    }

    public class RemovePasswordAsyncTask extends AsyncTask<Void, Void, String> {
        ProgressDialog dialog;
        OnResult onResult;

        public void setOnResult(OnResult onResult) {
            this.onResult = onResult;
        }

        @Override
        protected void onPreExecute() {
            dialog = Utilities.createAndShowWaitSpinner(NUIDocView.this.getContext());
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            return PdfUtils.INSTANCE.removePassword(getContext(), filePath, mSession.getPassword());
        }

        @Override
        protected void onPostExecute(String fileCache) {
            dialog.dismiss();
            if (onResult != null) {
                onResult.onSuccessListener(fileCache);
            }
            super.onPostExecute(fileCache);
        }
    }

    public void onProtectButton(View var1) {
    }

    public void onRedoButton(View var1) {
        this.doRedo();
    }


    public void onReflowButton(View var1) {
        this.mSession.setSODocSessionLoadListener2(new SODocSessionLoadListener() {
            public void onCancel() {
            }

            public void onDocComplete() {
            }

            public void onError(int var1, int var2) {
            }

            public void onLayoutCompleted() {
                NUIDocView.this.onLayoutChanged();
            }

            public void onPageLoad(int var1) {
            }

            public void onSelectionChanged(int var1, int var2) {
                NUIDocView.this.mSession.setSODocSessionLoadListener2((SODocSessionLoadListener) null);
                NUIDocView.this.mDocView.scrollTo(NUIDocView.this.mDocView.getScrollX(), 0);
                if (NUIDocView.this.usePagesView()) {
                    if (NUIDocView.this.mDocPageListView != null) {
                        NUIDocView.this.mDocPageListView.scrollTo(NUIDocView.this.mDocPageListView.getScrollX(), 0);
                    }
                }

                NUIDocView.this.setCurrentPage(0);
                if (NUIDocView.this.mDocView.getReflowMode()) {
                    NUIDocView.this.mDocView.setReflowWidth();
                    NUIDocView.this.mDocView.onScaleEnd((ScaleGestureDetector) null);
                } else {
                    float var3 = 1.0F;
                    if (NUIDocView.this.usePagesView()) {
                        var3 = (float) NUIDocView.this.getResources().getInteger(R.integer.sodk_editor_page_width_percentage) / 100.0F;
                    }

                    NUIDocView.this.mDocView.setScale(var3);
                    NUIDocView.this.mDocView.scaleChildren();
                }

                if (NUIDocView.this.usePagesView()) {
                    if (NUIDocView.this.mDocPageListView != null) {
                        NUIDocView.this.mDocPageListView.fitToColumns();
                    }
                }

                NUIDocView.this.layoutNow();
            }
        });
        if (this.getDoc().L() == 1) {
            if (this.usePagesView()) {
                this.mDocPageListView.setReflowMode(true);
            }

            this.mDocView.setReflowMode(true);
            this.getDoc().a(2, (float) this.getDocView().getReflowWidth());
            this.mDocView.mLastReflowWidth = (float) this.getDocView().getReflowWidth();
        } else {
            this.mDocView.setReflowMode(false);
            if (this.usePagesView()) {
                this.mDocPageListView.setReflowMode(false);
            }

            this.getDoc().a(1, (float) this.getDocView().getReflowWidth());
        }

    }

    public void onReflowScale() {
        this.mDocView.onReflowScale();
        if (this.usePagesView()) {
            this.mDocPageListView.onReflowScale();
        }

    }

    public void onResume() {
        this.onResumeCommon();
        this.keyboardHeight = 0;
        this.onShowKeyboard(false);
        SOFileState var1 = SOFileState.getAutoOpen(this.getContext());
        if (var1 != null && this.mFileState != null && var1.getLastAccess() > this.mFileState.getLastAccess()) {
            this.mFileState.setHasChanges(var1.hasChanges());
        }

        SOFileState.clearAutoOpen(this.getContext());
        SODataLeakHandlers var2 = this.mDataLeakHandlers;
        if (var2 != null) {
            var2.doInsert();
        }
        AppOpenManager.getInstance().enableAppResume();
    }

    protected void onResumeCommon() {
        mCurrentNUIDocView = this;
        if (this.mDocUserPath != null) {
            this.b();
            this.setDocViewBitmap();
        }

        label27:
        {
            if (this.mForceReloadAtResume) {
                this.mForceReloadAtResume = false;
                if (this.getDoc() != null)
                    this.getDoc().b(true);
            } else {
                if (!this.mForceReload) {
                    break label27;
                }

                this.mForceReload = false;
                this.getDoc().a(true);
            }

            this.reloadFile();
        }

        this.mIsActivityActive = true;
        this.focusInputView();
        DocView var1 = this.getDocView();
        if (var1 != null) {
            var1.forceLayout();
        }

        if (this.usePagesView()) {
            DocListPagesView var2 = this.getDocListPagesView();
            if (var2 != null) {
                var2.forceLayout();
            }
        }

    }

    public void onSaveAsButton(View var1) {
        if (!checkStoragePermission(this.getContext())) {
            try {
                Utilities.showMessage((Activity) NUIDocView.this.getContext(),
                        NUIDocView.this.getContext().getString(R.string.edit),
                        NUIDocView.this.getContext().getString(R.string.accept_all_file_permission_edit2));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }
//        if (blockFunction(IAPHelper.KEY_SAVE)) return;
        if (this.mFileState != null) {
            ExportFileActivity.setFileExt(this.mFileState.getOpenedPath().substring(this.mFileState.getOpenedPath().lastIndexOf(".")));
        }
        this.preSave();
        this.saveTemplate(false);
    }

    public void onSaveButton(View var1) {
        if (!checkStoragePermission(this.getContext())) {
            try {
                Utilities.showMessage((Activity) NUIDocView.this.getContext(),
                        NUIDocView.this.getContext().getString(R.string.edit),
                        NUIDocView.this.getContext().getString(R.string.accept_all_file_permission_edit2));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }
//        if (blockFunction(IAPHelper.KEY_SAVE)) return;
        if (this.mFileState != null) {
            ExportFileActivity.setFileExt(this.mFileState.getOpenedPath().substring(this.mFileState.getOpenedPath().lastIndexOf(".")));
        }
        this.preSave();
        this.doSave();
    }

    public void onSavePDFButton(View var1) {
        if (!checkStoragePermission(this.getContext())) {
            try {
                Utilities.showMessage((Activity) NUIDocView.this.getContext(),
                        NUIDocView.this.getContext().getString(R.string.edit),
                        NUIDocView.this.getContext().getString(R.string.accept_all_file_permission_edit2));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }
//        if (blockFunction(IAPHelper.KEY_SAVE)) return;
        if (this.mDataLeakHandlers != null) {
            try {
                ExportFileActivity.setFileExt(".pdf");
                File var3 = new File(this.mFileState.getOpenedPath());
                this.mDataLeakHandlers.saveAsPdfHandler(var3.getName(), this.mSession.getDoc());
            } catch (UnsupportedOperationException var2) {
            } catch (NullPointerException e) {
            }
        } else {
            // throw new UnsupportedOperationException();
        }
    }

    private boolean blockFunction(String func) {
//        if (!IAPHelper.isUnlockFunction(func)) {
//            String activityToStart = "com.ezstudio.pdfreaderver4.activity.PremiumActivityJava";
//            try {
//                Class<?> c = Class.forName(activityToStart);
//                Intent intent = new Intent(getContext(), c);
//                intent.putExtra("FROM_DETAIL", true);
//                getContext().startActivity(intent);
//            } catch (ClassNotFoundException ignored) {
//            }
//            return true;
//        }
        return false;
    }

    protected void onSearch() {
//        this.setTab(this.getContext().getString(R.string.sodk_editor_tab_hidden));
//        if (Utilities.isPhoneDevice(this.getContext())) {
//            this.o();
//        }

//        this.findViewById(R.id.searchTab).setVisibility(VISIBLE);
        onBottomMenuButton(mSearchTab, getResources().getString(R.string.sodk_editor_search));
        this.showSearchSelected(true);
        this.mSearchText.getText().clear();
        this.mSearchText.requestFocus();
        Utilities.showKeyboard(this.getContext());
    }

    public void onSearchButton(View var1) {
        this.onSearch();
    }

    public void onSearchNext(View var1) {
        this.b(false);
    }

    public void onSearchPrevious(View var1) {
        this.b(true);
    }

    public void onSearchClose(View var1) {
        currentTabId = 0;
        isMenuOpen = false;
        mSearchTab.setVisibility(GONE);
        Utilities.hideKeyboard(getContext(), mSearchTab);
        if (mTabLayout != null) {
            mTabLayout.selectTab(mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()));
        }
    }

    public void onSelectionChanged() {
        SODocSession var1 = this.mSession;
        if (var1 != null && var1.getDoc() != null && !this.mFinished) {
            this.mDocView.onSelectionChanged();
            if (this.usePagesView() && this.isPageListVisible()) {
                this.mDocPageListView.onSelectionChanged();
            }

            this.updateUIAppearance();
        }

    }

    public void onSelectionMonitor(int var1, int var2) {
        this.onSelectionChanged();
    }

    public void onShareButton(View var1) {
        this.preSave();
        if (this.mDataLeakHandlers != null) {
            try {
                File var4 = new File(this.mFileState.getOpenedPath());
                if (var4 != null && var4.exists()) {
                    Context context = getContext();
                    Uri uri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".provider",
                            var4
                    );

                    String name = var4.getName().toLowerCase();
                    String mimeType;
                    if (name.endsWith(".pdf")) {
                        mimeType = "application/pdf";
                    } else if (name.endsWith(".doc") || name.endsWith(".dot")) {
                        mimeType = "application/msword";
                    } else if (name.endsWith(".docx")) {
                        mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    } else if (name.endsWith(".xls")) {
                        mimeType = "application/vnd.ms-excel";
                    } else if (name.endsWith(".xlsx")) {
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    } else if (name.endsWith(".ppt")) {
                        mimeType = "application/vnd.ms-powerpoint";
                    } else if (name.endsWith(".pptx")) {
                        mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                    } else if (name.endsWith(".txt")) {
                        mimeType = "text/plain";
                    } else {
                        mimeType = "application/octet-stream";
                    }

                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType(mimeType);
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    activity().startActivity(Intent.createChooser(share, context.getString(R.string.share)));
                }
            } catch (Exception ignore) {
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void onShowKeyboard(final boolean var1) {
        if (this.isActivityActive()) {
            if (this.getPageCount() > 0) {
                this.keyboardShown = var1;
                this.onShowKeyboardPreventPush(var1);
                if (!this.isFullScreen()) {
                    this.showUI(!var1);
                }

                if (this.usePagesView()) {
                    DocListPagesView var2 = this.getDocListPagesView();
                    if (var2 != null) {
                        var2.onShowKeyboard(var1);
                    }
                }

                final ViewTreeObserver var3 = this.getViewTreeObserver();
                var3.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        var3.removeOnGlobalLayoutListener(this);
                        DocView var1x = NUIDocView.this.getDocView();
                        if (var1x != null) {
                            var1x.onShowKeyboard(var1);
                        }

                        if (NUIDocView.this.mShowHideKeyboardRunnable != null) {
                            NUIDocView.this.mShowHideKeyboardRunnable.run();
                            NUIDocView.this.mShowHideKeyboardRunnable = null;
                        }

                    }
                });
            }
        }
    }

    protected void onShowKeyboardPreventPush(boolean var1) {
        boolean var2;
        if ((this.activity().getWindow().getAttributes().flags & 1024) != 0) {
            var2 = true;
        } else {
            var2 = false;
        }

        if (var2) {
            View var3 = ((BaseActivity) this.getContext()).findViewById(16908290);
            if (var1) {
                var3.setPadding(0, 0, 0, this.getKeyboardHeight());
                this.findViewById(R.id.footer).setVisibility(GONE);
            } else {
                var3.setPadding(0, 0, 0, 0);
                this.findViewById(R.id.footer).setVisibility(VISIBLE);
            }
        }

    }

    public void onTabChanged(String var1) {
        this.onTabChanging(this.mCurrentTab, var1);
        this.getDocView().saveComment();
        if (var1.equals(this.activity().getString(R.string.sodk_editor_tab_review)) && !this.getDocView().getDoc().docSupportsReview()) {
            try {
                Utilities.showMessage(this.activity(), this.activity().getString(R.string.sodk_editor_not_supported), this.activity().getString(R.string.sodk_editor_cant_review_doc_body));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.setTab(this.mCurrentTab);
            if (this.mCurrentTab.equals(this.activity().getString(R.string.sodk_editor_tab_hidden))) {
                this.onSearchButton(this.mSearchButton);
            }
            this.onSelectionChanged();

        } else if (var1.equals(this.activity().getString(R.string.sodk_editor_tab_single))) {
            this.setTab(this.mCurrentTab);
            this.onSelectionChanged();
            this.activity();
            ListPopupWindow var6 = new ListPopupWindow(this.activity());
            this.mListPopupWindow = var6;
            var6.setBackgroundDrawable(viewx.core.content.a.a(this.activity(), R.drawable.sodk_editor_menu_popup));
            this.mListPopupWindow.setModal(true);
            this.mListPopupWindow.setAnchorView(this.getSingleTabView());
            final ArrayAdapter var7 = new ArrayAdapter(this.activity(), R.layout.sodk_editor_menu_popup_item);
            this.mListPopupWindow.setAdapter(var7);
            NUIDocView.TabData[] var2 = this.getTabData();
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                if (var2[var4].visibility == 0) {
                    String var5 = var2[var4].name;
                    var7.add(var5);
                    ((SOTextView) this.activity().getLayoutInflater().inflate(R.layout.sodk_editor_menu_popup_item, (ViewGroup) null)).setText(var5);
                }
            }

            if (this.hasSearch()) {
                var7.add(this.activity().getString(R.string.sodk_editor_tab_find));
                ((SOTextView) this.activity().getLayoutInflater().inflate(R.layout.sodk_editor_menu_popup_item, (ViewGroup) null)).setText(this.activity().getString(R.string.sodk_editor_tab_find));
            }

            this.mListPopupWindow.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> var1, View var2, int var3, long var4) {
                    if (NUIDocView.this.mListPopupWindow != null) {
                        NUIDocView.this.mListPopupWindow.dismiss();
                    }

                    NUIDocView.this.mListPopupWindow = null;
                    NUIDocView.this.k();
                    String var6 = (String) var7.getItem(var3);
                    if (var6.equals(NUIDocView.this.activity().getString(R.string.sodk_editor_tab_find))) {
                        NUIDocView.this.onSearch();
                        NUIDocView.this.setSingleTabTitle(var6);
                    } else if (var6.equals(NUIDocView.this.activity().getString(R.string.sodk_editor_tab_review)) && !NUIDocView.this.getDocView().getDoc().docSupportsReview()) {
                        try {
                            Utilities.showMessage(NUIDocView.this.activity(), NUIDocView.this.activity().getString(R.string.sodk_editor_not_supported), NUIDocView.this.activity().getString(R.string.sodk_editor_cant_review_doc_body));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        NUIDocView.this.changeTab(var6);
                        NUIDocView.this.setSingleTabTitle(var6);
                        NUIDocView.this.tabHost.setCurrentTabByTag(var6);
                        NUIDocView.this.b(var6);
                    }

                    NUIDocView var7x = NUIDocView.this;
//                    var7x.b(var7x.getSingleTabView(), true);
                }
            });
            this.mListPopupWindow.setContentWidth(this.highlightView((ListAdapter) var7));
            this.showKeyboard(false, new Runnable() {
                public void run() {
                    NUIDocView.this.mListPopupWindow.show();
                }
            });
        } else {
            this.changeTab(var1);
            if (!Utilities.isPhoneDevice(this.getContext())) {
                this.b(var1);
            }

        }
    }

    protected void onTabChanging(String var1, String var2) {
    }

    PopupWindow fontWindow;

    public void onTapFontName(View var1) {
        fontWindow = new PopupWindow(getContext());
        fontWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        View view = LayoutInflater.from(getContext()).inflate(R.layout.sodk_editor_edit_font, null);
        fontWindow.setContentView(view);
        fontWindow.setOutsideTouchable(true);
        fontWindow.setFocusable(true);
        WheelPicker fontWheel = view.findViewById(R.id.left_wheel);

        if (this.getDoc() == null) return;
        String xx = this.getDoc().getFontList();
        List<String> newData = Arrays.asList(xx.split(",").clone());
        fontWheel.setData(newData);
        fontWheel.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolled(int offset) {

            }

            @Override
            public void onWheelSelected(int position) {
                getDoc().setSelectionFontName(newData.get(position));

            }

            @Override
            public void onWheelScrollStateChanged(int state) {

            }
        });
        fontWindow.showAsDropDown(var1);


//        (new EditFont(this.getContext(), var1, this.getDoc())).show();
    }

    PopupWindow fontSizeWindow;

    public void onTapFontSize(View var1) {
        fontSizeWindow = new PopupWindow(getContext());
        fontSizeWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        View view = LayoutInflater.from(getContext()).inflate(R.layout.sodk_editor_edit_font, null);
        fontSizeWindow.setContentView(view);
        fontSizeWindow.setOutsideTouchable(true);
        fontSizeWindow.setFocusable(true);
        WheelPicker fontWheel = view.findViewById(R.id.left_wheel);

        String[] abc = {"6 pt", "8 pt", "9 pt", "10 pt", "12 pt", "14 pt", "16 pt", "18 pt", "20 pt", "24 pt", "30 pt", "36 pt", "48 pt", "60 pt", "72 pt"};

        List<String> newData = Arrays.asList(abc);
        fontWheel.setData(newData);
        fontWheel.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolled(int offset) {

            }

            @Override
            public void onWheelSelected(int position) {
                String s = newData.get(position).substring(0, newData.get(position).length() - 3);
                double d = 14.0;
                try {
                    d = Double.parseDouble(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getDoc().setSelectionFontSize(d);

            }

            @Override
            public void onWheelScrollStateChanged(int state) {

            }
        });

        fontSizeWindow.showAsDropDown(var1);


//        (new EditFont(this.getContext(), var1, this.getDoc())).show();
    }

    public void onTyping() {
        this.lastTypingTime = System.currentTimeMillis();
    }

    public void onUndoButton(View var1) {
        this.doUndo();
    }

    public void preSave() {
    }

    protected void preSaveQuestion(Runnable var1, Runnable var2) {
        if (var1 != null) {
            var1.run();
        }
    }

    public interface CompleteListener {
        void onComplete();
    }

    public void showAdsInterstitial(final CompleteListener complete) {
        if (IAPUtils.INSTANCE.isPremium()
                || !Admob.getInstance().isLoadFullAds()
                || !ConsentHelper.getInstance(this.activity().getApplicationContext()).canRequestAds()) {
            complete.onComplete();
            return;
        }

        AdCallback interCallback = new AdCallback() {
            @Override
            public void onNextAction() {
                complete.onComplete();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError error) {
                Log.e("TAG", "onAdFailedToLoad: " + (error != null ? error.getMessage() : "Unknown error"));
                complete.onComplete();
            }
        };

        Admob.getInstance().loadAndShowInter(
                NUIDocView.this.activity(),
                FirebaseRemoteConfigUtil.Companion.getInstance().getAdsConfigValue("inter_filedetail"),
                100,
                100,
                interCallback
        );
    }




    public void prefinish() {
        showAdsInterstitial(new CompleteListener() {
            @Override
            public void onComplete() {
                prepareToFinish();
            }
        });
    }

    private void prepareToFinish() {
        if (!this.mFinished) {
            this.mFinished = true;
            SODocSession var1 = this.mSession;
            if (var1 != null && var1.getDoc() != null) {
                this.mSession.getDoc().a((p) null);
            }

            this.mSearchListener = null;
            this.m();
            SOFileState var2 = this.mFileState;
            if (var2 != null) {
                var2.closeFile();
            }

            Utilities.hideKeyboard(this.getContext());
            DocView var3 = this.mDocView;
            if (var3 != null) {
                var3.finish();
                this.mDocView = null;
            }

            if (this.usePagesView()) {
                DocListPagesView var4 = this.mDocPageListView;
                if (var4 != null) {
                    var4.finish();
                    this.mDocPageListView = null;
                }
            }

            var1 = this.mSession;
            if (var1 != null) {
                var1.abort();
            }

            SODoc var5 = this.getDoc();
            if (var5 != null) {
                var5.N();
            }

            PageAdapter var6 = this.mAdapter;
            if (var6 != null) {
                var6.setDoc((SODoc) null);
            }

            this.mAdapter = null;
            Boolean var7 = this.mEndSessionSilent;
            if (var7 != null) {
                this.endDocSession(var7);
                this.mEndSessionSilent = null;
            }

            if (this.mSession != null) {
                final ProgressDialog var8 = new ProgressDialog(this.getContext(), R.style.sodk_editor_alert_dialog_style);
                var8.setMessage(this.getContext().getString(R.string.sodk_editor_wait));
                var8.setCancelable(false);
                var8.setIndeterminate(true);
                var8.getWindow().clearFlags(2);
                var8.setOnShowListener(new OnShowListener() {
                    public void onShow(DialogInterface var1) {
                        (new Handler()).post(new Runnable() {
                            public void run() {
                                if (NUIDocView.this.mSession != null) {
                                    NUIDocView.this.mSession.destroy();
                                }
                                try {
                                    if(!NUIDocView.this.activity().isFinishing() || !NUIDocView.this.activity().isDestroyed()) {
                                        var8.dismiss();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
//
//                                if (NUIDocView.this.onDoneListener != null) {
//                                    NUIDocView.this.onDoneListener.done();
//                                }

                            }
                        });
                    }
                });
                var8.show();
            } else {
                OnDoneListener var9 = this.a;
                if (var9 != null) {
                    var9.done();
                }
            }

            NUIView.OnDoneListener var10 = this.a;
            if (var10 != null) {
                var10.done();
            }

        }
    }

    protected void prepareToGoBack() {
    }

    public void releaseBitmaps() {
        this.setValid(false);
        this.releaseDocViewBitmap();
        this.setDocViewBitmap();
    }

    public void reloadFile() {
    }

    protected void resetInputView() {
        InputView var1 = this.mInputView;
        if (var1 != null) {
            var1.resetEditable();
        }
    }

    protected void scaleHeader() {
//        this.scaleToolbar(id.annotate_toolbar, 0.65F);
//        this.scaleToolbar(id.doc_pages_toolbar, 0.65F);
//        this.scaleToolbar(id.edit_toolbar, 0.65F);
//        this.scaleToolbar(id.excel_edit_toolbar, 0.65F);
//        this.scaleToolbar(id.file_toolbar, 0.65F);
//        this.scaleToolbar(id.format_toolbar, 0.65F);
//        this.scaleToolbar(id.formulas_toolbar, 0.65F);
//        this.scaleToolbar(id.insert_toolbar, 0.65F);
//        this.scaleToolbar(id.pdf_pages_toolbar, 0.65F);
//        this.scaleToolbar(id.ppt_format_toolbar, 0.65F);
//        this.scaleToolbar(id.ppt_insert_toolbar, 0.65F);
//        this.scaleToolbar(id.ppt_slides_toolbar, 0.65F);
//        this.scaleToolbar(id.review_toolbar, 0.65F);
//        this.scaleSearchToolbar(0.65F);

//        this.scaleTabArea(1.3F);
//        int var1 = this.tabHost.getTabWidget().getTabCount();
//        ((SOTextView) this.tabHost.getTabWidget().getChildTabViewAt(var1 - 1).findViewById(id.tabText)).setTextSize((float) this.getContext().getResources().getDimension(com.intuit.ssp.R.dimen._7ssp));
//        this.mBackButton.setScaleX(0.65F);
//        this.mBackButton.setScaleY(0.65F);
//        this.mUndoButton.setScaleX(0.5F);
//        this.mUndoButton.setScaleY(0.5F);
//        this.mRedoButton.setScaleX(0.5F);
//        this.mRedoButton.setScaleY(0.5F);
//        this.mSearchButton.setScaleX(0.65F);
//        this.mSearchButton.setScaleY(0.65F);
    }

    protected void scaleSearchToolbar(float var1) {
        LinearLayout var2 = (LinearLayout) this.findViewById(R.id.search_toolbar);
        if (var2 != null) {
            this.highlightView(var2, R.id.search_icon, var1);
            this.highlightView(var2, R.id.search_text_clear, var1);
            this.highlightView(var2, R.id.search_next, var1);
            this.highlightView(var2, R.id.search_previous, var1);
            LinearLayout var3 = (LinearLayout) var2.findViewById(R.id.search_input_wrapper);
            Context var4;
            int var5;
            if (Utilities.isPhoneDevice(this.getContext())) {
                var4 = this.getContext();
                var5 = R.drawable.sodk_editor_search_input_wrapper_phone;
            } else {
                var4 = this.getContext();
                var5 = R.drawable.sodk_editor_search_input_wrapper;
            }

            var3.setBackground(viewx.core.content.a.a(var4, var5));
            this.mSearchText.setTextSize(2, var1 * 20.0F);
            var3.measure(0, 0);
            var5 = var3.getMeasuredHeight();
            var3.getLayoutParams().height = (int) ((float) var5 * 0.85F);
            var2.setPadding(0, -15, 0, -15);
        }
    }

    protected void scaleTabArea(float var1) {
        LinearLayout var2 = (LinearLayout) this.findViewById(R.id.header_top);
        if (var2 != null) {
            var2.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int var3 = var2.getMeasuredHeight();
            var2.getLayoutParams().height = (int) ((float) var3 * var1);
            var2.requestLayout();
            var2.invalidate();
        }
    }

    protected void scaleToolbar(int var1, float var2) {
    }

    public void selectionupdated() {
        this.onSelectionChanged();
    }

    protected void setButtonColor(Button var1, int var2) {
        Drawable[] var4 = var1.getCompoundDrawables();

        for (int var3 = 0; var3 < var4.length; ++var3) {
            if (var4[var3] != null) {
                //viewx.core.graphics.drawable.a.a(var4[var3], var2);
            }
        }

    }

    public void setConfigurableButtons() {
//        ArrayList var1 = new ArrayList();
        if (this.mSaveButton != null) {
//        highlightView(this.mSaveButton, this.mConfigOptions.o());
            highlightView(this.mSaveButton, true);

//            if (this.mConfigOptions.o()) {
//                this.mSaveButton.setVisibility(VISIBLE);
//                var1.add(this.mSaveButton);
//            } else {
//                this.mSaveButton.setVisibility(GONE);
//            }
        }

        if (this.mCustomSaveButton != null) {
            if (this.mConfigOptions.p()) {
                this.mCustomSaveButton.setVisibility(VISIBLE);
//                var1.add(this.mCustomSaveButton);
            } else {
                this.mCustomSaveButton.setVisibility(GONE);
            }
        }

        if (this.mSaveAsButton != null) {
            if (this.mConfigOptions.c()) {
                this.mSaveAsButton.setVisibility(VISIBLE);
//                var1.add(this.mSaveAsButton);
            } else {
                this.mSaveAsButton.setVisibility(GONE);
            }
        }

        // only for txt
        LinearLayout lnSaveAsPdf = findViewById(R.id.save_as_button);

        if (this.mSavePdfButton != null) {
            if (this.mConfigOptions.d()) {
                lnSaveAsPdf.setVisibility(VISIBLE);
                this.mSavePdfButton.setVisibility(VISIBLE);
//                var1.add(this.mSavePdfButton);

            } else {
                lnSaveAsPdf.setVisibility(GONE);
                this.mSavePdfButton.setVisibility(GONE);
            }
        } else {
            if (lnSaveAsPdf != null) {
                lnSaveAsPdf.setVisibility(GONE);
            }
        }
        if (this.mSavePdfButton != null && !this.shouldConfigureSaveAsPDFButton()) {
            this.mSavePdfButton.setVisibility(GONE);
        }

        if (this.mShareButton != null) {
            if (this.mConfigOptions.g()) {
                this.mShareButton.setVisibility(VISIBLE);
//                var1.add(this.mShareButton);
            } else {
                this.mShareButton.setVisibility(GONE);
            }
        }

        if (this.mOpenInButton != null) {
            if (this.mConfigOptions.e()) {
                this.mOpenInButton.setVisibility(VISIBLE);
//                var1.add(this.mOpenInButton);
            } else {
                this.mOpenInButton.setVisibility(GONE);
            }
        }

        if (this.mOpenPdfInButton != null) {
            if (this.mConfigOptions.f()) {
                this.mOpenPdfInButton.setVisibility(VISIBLE);
//                var1.add(this.mOpenPdfInButton);
            } else {
                this.mOpenPdfInButton.setVisibility(GONE);
            }
        }

        if (this.mPrintButton != null) {
            if (!this.mConfigOptions.l() && !this.mConfigOptions.m()) {
                this.mPrintButton.setVisibility(GONE);
            } else {
                this.mPrintButton.setVisibility(VISIBLE);
//                var1.add(this.mPrintButton);
            }
            mPrintButton.setVisibility(GONE);
            // remove print because of crash
        }

        LinearLayout var2 = this.mProtectButton;
        if (var2 != null) {
//            var1.add(var2);
            this.mProtectButton.setVisibility(GONE);
        }

        if (this.mCopyButton2 != null) {
            if (!this.mConfigOptions.b() && !this.getDocFileExtension().equalsIgnoreCase("pdf")) {
                this.mCopyButton2.setVisibility(VISIBLE);
                this.mCopyDivider.setVisibility(VISIBLE);
//                var1.add(this.mCopyButton2);
            } else {
                this.mCopyButton2.setVisibility(GONE);
                this.mCopyDivider.setVisibility(GONE);

            }
        }

//        setAllSameSize((LinearLayout[]) var1.toArray(new LinearLayout[var1.size()]));

        if (!this.mConfigOptions.b()) {
            ImageView var3 = this.mUndoButton;
            if (var3 != null) {
                var3.setVisibility(GONE);
            }

            var3 = this.mRedoButton;
            if (var3 != null) {
                var3.setVisibility(GONE);
            }
        }

        if (this.mInsertImageButton != null) {
            if (this.mConfigOptions.j() && this.mConfigOptions.b()) {
                highlightView(this.mInsertImageButton, true);
            } else {
                highlightView(this.mInsertImageButton, false);
            }
        }

        if (this.mInsertPhotoButton != null) {
            if (this.mConfigOptions.k() && this.mConfigOptions.b()) {
                highlightView(this.mInsertPhotoButton, true);
            } else {
                highlightView(this.mInsertPhotoButton, false);
            }
        }

        this.setInsertTabVisibility();
    }

    public void setCurrentPage(int var1) {
        if (this.usePagesView()) {
            this.mDocPageListView.setCurrentPage(var1);
            this.mDocPageListView.scrollToPage(var1, false);
        }

        this.mCurrentPageNum = var1;
        this.setPageNumberText();
        this.mSession.getFileState().setPageNumber(this.mCurrentPageNum);
    }

    protected void setInsertTabVisibility() {
        if (this.tabHost != null) {
            if (Utilities.isPhoneDevice(this.getContext())) {
                this.u();
            } else {
                TabWidget var1 = this.tabHost.getTabWidget();
                if (var1 != null) {
                    int var2 = this.mAllTabHostTabs.indexOf(this.getContext().getString(R.string.sodk_editor_tab_insert));
                    if (var2 != -1) {
                        NUIDocView.TabData[] var3 = this.getTabData();
                        byte var5;
                        View var6;
                        if (!this.mConfigOptions.j() && !this.mConfigOptions.k()) {
                            if (this.tabHost.getCurrentTab() == var2) {
                                int var4 = this.mAllTabHostTabs.indexOf(var3[this.getInitialTab()].name);
                                this.tabHost.setCurrentTab(var4);
                            }

                            var6 = var1.getChildTabViewAt(var2);
                            var5 = 8;
                        } else {
                            var6 = var1.getChildTabViewAt(var2);
                            var5 = 0;
                        }

                        var6.setVisibility(var5);
                    }
                }
            }
        }
    }

    public void setIsComposing(boolean var1) {
        this.mIsComposing = var1;
    }

    protected void setPageCount(int var1) {

        if (this.mAdapter != null) {
            this.mPageCount = var1;
            this.mAdapter.setCount(var1);
            this.i();
        }

    }

    protected void setPageNumberText() {
        (new Handler()).post(new Runnable() {
            public void run() {
                NUIDocView.this.mFooterText.setVisibility(View.VISIBLE);
                NUIDocView.this.mFooterText.setText(NUIDocView.this.getPageNumberText());
                NUIDocView.this.mFooterText.measure(0, 0);
//                NUIDocView.this.mFooterLead.getLayoutParams().width = NUIDocView.this.mFooterText.getMeasuredWidth();
//                NUIDocView.this.mFooterLead.getLayoutParams().height = NUIDocView.this.mFooterText.getMeasuredHeight();
            }
        });
    }

    public void setSearchStart() {
    }

    protected void setStartPage(int var1) {
        this.mStartPage = var1;
    }

    protected void setTabColors(String var1) {
        Iterator var2 = this.tabMap.entrySet().iterator();

        while (var2.hasNext()) {
            Entry var3 = (Entry) var2.next();
            String var4 = (String) var3.getKey();
            this.b((View) var3.getValue(), var1.equals(var4));
        }

//        this.b(this.getSingleTabView(), true);
    }


    protected boolean shouldConfigureSaveAsPDFButton() {
        return true;
    }

    protected void showKeyboard(boolean var1, Runnable var2) {
        boolean var3;
        if (this.getKeyboardHeight() > 0) {
            var3 = true;
        } else {
            var3 = false;
        }

        label29:
        {
            if (var1) {
                if (!isMenuOpen) {
                    Utilities.showKeyboard(this.getContext());
                }
                if (var3) {
                    break label29;
                }
            } else {
                Utilities.hideKeyboard(this.getContext());
                if (!var3) {
                    break label29;
                }
            }

            this.mShowHideKeyboardRunnable = var2;
            return;
        }

        var2.run();
    }

    public boolean showKeyboard() {
        /*if (!isMenuOpen) {
            Utilities.showKeyboard(this.getContext());
        }
        return !isMenuOpen;*/
//        Utilities.showKeyboard(this.getContext());

        Log.e("Keyboard", keyboardShown + "");
        if (mButtonKeyboard != null) {
            mButtonKeyboard.setVisibility(keyboardShown ? INVISIBLE : VISIBLE);
        }
        return true;
    }

    protected void showPages() {
        RelativeLayout var1 = (RelativeLayout) this.findViewById(R.id.pages_container);
        if (var1 != null) {
            if (var1.getVisibility() != VISIBLE) {
                var1.setVisibility(VISIBLE);
                this.mDocPageListView.setVisibility(VISIBLE);
                this.mDocView.onShowPages();
            }

            final ViewTreeObserver var2 = this.mDocView.getViewTreeObserver();
            var2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    var2.removeOnGlobalLayoutListener(this);
                    int var1 = NUIDocView.this.mDocView.getMostVisiblePage();
                    if (NUIDocView.this.mDocPageListView != null) {
                        NUIDocView.this.mDocPageListView.setCurrentPage(var1);
                        NUIDocView.this.mDocPageListView.scrollToPage(var1, false);
                        NUIDocView.this.mDocPageListView.fitToColumns();
                        NUIDocView.this.mDocPageListView.layoutNow();
                    }
                }
            });
        }

    }

    protected void showSearchSelected(boolean var1) {
        ImageView var2 = this.mSearchButton;
        if (var2 != null) {
            var2.setSelected(var1);
            ImageView var3;
            int var4;
            Activity var5;
            if (var1) {
                var3 = this.mSearchButton;
                var5 = this.activity();
                var4 = R.color.sodk_editor_button_tint;
            } else {
                var3 = this.mSearchButton;
                var5 = this.activity();
                var4 = R.color.sodk_editor_header_button_enabled_tint;
            }

//            this.setButtonColor(var3, viewx.core.content.a.c(var5, var4));
        }

    }

    public void showUI(final boolean var1) {

        label28:
        {
            label27:
            {
                View var3 = this.findViewById(R.id.header);
                if (this.isLandscapePhone()) {
                    if (!var1 && !this.isSearchVisible()) {
                        var3.setVisibility(GONE);
                        break label27;
                    }

                    if (!var1) {
                        break label28;
                    }
                } else {
                    break label28;
                }

                var3.setVisibility(VISIBLE);
            }

            this.layoutNow();
        }

        final ViewTreeObserver var4 = this.getViewTreeObserver();
        var4.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                var4.removeOnGlobalLayoutListener(this);
                NUIDocView.this.afterShowUI(var1);
            }
        });
    }

    public void start(Uri var1, boolean var2, int var3, String var4, OnDoneListener var5) {
        this.mIsTemplate = var2;
        this.mStartUri = var1;
        this.mCustomDocdata = var4;
        this.a = var5;
        var4 = com.artifex.solib.a.a(this.getContext(), var1);
        Activity var6 = this.activity();
        StringBuilder var7 = new StringBuilder();
        var7.append("filename.");
        var7.append(var4);
        this.mDocumentLib = k.a(var6, var7.toString());
        this.e();
        this.g();
        this.f();
    }

    public void start(SODocSession var1, int var2, String var3, OnDoneListener var4) {
        this.mIsSession = true;
        this.mSession = var1;
        this.mIsTemplate = false;
        this.setStartPage(var2);
        this.a = var4;
        this.mForeignData = var3;
        this.mDocumentLib = k.a(this.activity(), var1.getUserPath());
        this.e();
        this.g();
        this.f();
    }

    public void start(SOFileState var1, int var2, OnDoneListener var3) {
        this.mIsSession = false;
        this.mIsTemplate = var1.isTemplate();
        this.mState = var1;
        this.setStartPage(var2);
        this.a = var3;
        this.mDocumentLib = k.a(this.activity(), var1.getOpenedPath());
        this.e();
        this.g();
        this.f();
    }

    public void triggerOrientationChange() {
        this.mForceOrientationChange = true;
    }

    public void triggerRender() {
        DocView var1 = this.mDocView;
        if (var1 != null) {
            var1.triggerRender();
        }

        if (this.mDocPageListView != null && this.usePagesView() && this.isPageListVisible()) {
            this.mDocPageListView.triggerRender();
        }

    }

    protected void updateEditUIAppearance() {
        if (this.getDocView() == null) {
            return;
        }
        SOSelectionLimits var1 = this.getDocView().getSelectionLimits();
        boolean var2 = true;
        boolean var3;
        boolean var5;
        boolean var6;
        if (var1 != null) {
            var3 = var1.getIsActive();
            boolean var4;
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

        SODoc var9 = this.mSession.getDoc();
        if (var6 && !var9.selectionIsAutoshapeOrImage()) {
            var3 = true;
        } else {
            var3 = false;
        }

//        this.mStyleBoldButton.setEnabled(var3);
        highlightView(this.mStyleBoldButton, var3);
        LinearLayout var7 = this.mStyleBoldButton;
        boolean var8;
        if (var3 && var9.getSelectionIsBold()) {
            var8 = true;
        } else {
            var8 = false;
        }

        var7.setSelected(var8);
//        this.mStyleItalicButton.setEnabled(var3);
        highlightView(this.mStyleItalicButton, var3);

        var7 = this.mStyleItalicButton;
        if (var3 && var9.getSelectionIsItalic()) {
            var8 = true;
        } else {
            var8 = false;
        }

        var7.setSelected(var8);
//        this.mStyleUnderlineButton.setEnabled(var3);
        highlightView(this.mStyleUnderlineButton, var3);
        var7 = this.mStyleUnderlineButton;
        if (var3 && var9.getSelectionIsUnderlined()) {
            var8 = true;
        } else {
            var8 = false;
        }

        var7.setSelected(var8);
//        this.mStyleLinethroughButton.setEnabled(var3);
//        highlightView(this.mStyleLinethroughButton, var3);
//
//        var7 = this.mStyleLinethroughButton;
//        if (var3 && var9.getSelectionIsLinethrough()) {
//            var3 = true;
//        } else {
//            var3 = false;
//        }
//
//        var7.setSelected(var3);
//        this.mAlignLeftButton.setEnabled(var5);
        highlightView(this.mStyleTextColorButton, var3);

        highlightView(this.mStyleTextHighlightButton, var3);

        if (lnBottom != null) {
            if (this.lnBottom.getVisibility() == VISIBLE) {
                this.lnBottom.setVisibility(var3 ? VISIBLE : GONE);
            }
        }

        highlightView(this.mAlignLeftButton, var5);
        var7 = this.mAlignLeftButton;
        if (var5 && var9.getSelectionIsAlignLeft()) {
            var3 = true;
        } else {
            var3 = false;
        }

        var7.setSelected(var3);
//        this.mAlignCenterButton.setEnabled(var5);
        highlightView(this.mAlignCenterButton, var5);
        var7 = this.mAlignCenterButton;
        if (var5 && var9.getSelectionIsAlignCenter()) {
            var3 = true;
        } else {
            var3 = false;
        }

        var7.setSelected(var3);
//        this.mAlignRightButton.setEnabled(var5);
        highlightView(this.mAlignRightButton, var5);
        var7 = this.mAlignRightButton;
        if (var5 && var9.getSelectionIsAlignRight()) {
            var3 = true;
        } else {
            var3 = false;
        }

        var7.setSelected(var3);
//        this.mAlignJustifyButton.setEnabled(var5);
        highlightView(this.mAlignJustifyButton, var5);
        var7 = this.mAlignJustifyButton;
        if (var5 && var9.getSelectionIsAlignJustify()) {
            var3 = true;
        } else {
            var3 = false;
        }

        var7.setSelected(var3);
//        this.mListBulletsButton.setEnabled(var5);
        highlightView(this.mListBulletsButton, var5);
        var7 = this.mListBulletsButton;
        if (var5 && var9.getSelectionListStyleIsDisc()) {
            var3 = true;
        } else {
            var3 = false;
        }

        var7.setSelected(var3);
//        this.mListNumbersButton.setEnabled(var5);
        highlightView(this.mListNumbersButton, var5);
        var7 = this.mListNumbersButton;
        if (var5 && var9.getSelectionListStyleIsDecimal()) {
            var3 = true;
        } else {
            var3 = false;
        }

        var7.setSelected(var3);
        LinearLayout var10 = this.mIncreaseIndentButton;
        if (var5 && this.r()) {
            var3 = true;
        } else {
            var3 = false;
        }

//        var10.setEnabled(var3);
        highlightView(var10, var3);
        var10 = this.mDecreaseIndentButton;
        if (var5 && this.s()) {
            var5 = var2;
        } else {
            var5 = false;
        }

//        var10.setEnabled(var5);
        highlightView(var10, var5);
    }

    protected void updateInputView() {
        InputView var1 = this.mInputView;
        if (var1 != null) {
            var1.updateEditable();
        }

    }

    protected void updateInsertUIAppearance() {
        if (this.getDocView() == null) {
            return;
        }
        SOSelectionLimits var1 = this.getDocView().getSelectionLimits();
        boolean var2;
        if (var1 != null && var1.getIsActive() && var1.getIsCaret()) {
            var2 = true;
        } else {
            var2 = false;
        }

        if (this.mInsertImageButton != null && this.mConfigOptions.j()) {
            highlightView(this.mInsertImageButton, (var2));
        }

        if (this.mInsertPhotoButton != null && this.mConfigOptions.k()) {
            highlightView(this.mInsertPhotoButton, (var2));
        }

//        doPaste();

    }

    private boolean doPasted = false;

    protected void updateReviewUIAppearance() {
    }

    protected void updateSaveUIAppearance() {
        if (this.mSaveButton != null) {
            boolean var1 = this.documentHasBeenModified();
            boolean var2 = this.mConfigOptions.b();
            if (!var2) {
                var1 = false;
            }

            if (this.mIsTemplate) {
                var1 = false;
            }
//            this.mSaveButton.setEnabled(var1);
//            highlightView(this.mSaveButton, var1);
            highlightView(this.mSaveButton, true);
        }

    }

    protected void updateUIAppearance() {
        if (this.getDocView() == null) {
            return;
        }
        this.updateSaveUIAppearance();
        SOSelectionLimits var1 = this.getDocView().getSelectionLimits();
        boolean var2 = true;
        boolean var3 = true;
        boolean var4;
        boolean var5;
        boolean var6;
        if (var1 != null) {
            var4 = var1.getIsActive();
            if (var4 && !var1.getIsCaret()) {
                var5 = true;
            } else {
                var5 = false;
            }

            if (var4 && var1.getIsCaret()) {
                var6 = true;
            } else {
                var6 = false;
            }
        } else {
            var6 = false;
            var5 = false;
        }

        LinearLayout var12;
        if (!this.mConfigOptions.b()) {
            var12 = this.mCopyButton2;
            if (var12 != null) {
                if (!var5 || !this.mSession.getDoc().getSelectionCanBeCopied()) {
                    var3 = false;
                }

//                var12.setEnabled(var3);
                highlightView(var12, var3);
            }

        } else {
            this.updateEditUIAppearance();
            this.updateUndoUIAppearance();
            this.updateReviewUIAppearance();
            boolean var7 = this.getDoc().selectionIsAutoshapeOrImage();
            var3 = var5 && !var7;

            highlightView(mFontNameText, var3);

            highlightView(mFontSizeText, var3);

            long var8 = Math.round(this.mSession.getDoc().getSelectionFontSize());
            SOTextView var10 = this.mFontSizeText;
            String var11;
            if (var8 > 0L) {
                var11 = String.format("%d", (int) var8);
            } else {
                var11 = "11";
            }

            var10.setText(var11);
            if (var3) {
                var12 = this.mFontUpButton;
                var4 = var8 < MAX_FONT_SIZE;

                highlightView(var12, var4);
                var12 = this.mFontDownButton;
                var4 = var8 > MIN_FONT_SIZE;

                highlightView(var12, var4);
            } else {
                highlightView(mFontUpButton, false);
                highlightView(mFontDownButton, false);
            }

            var11 = Utilities.getSelectionFontName(this.mSession.getDoc());
            this.mFontNameText.setText((var11 == null || var11.isEmpty()) ? "Arial" : var11);
            highlightView(mFontColorButton, var3);
            highlightView(mFontBackgroundButton, var3);
            var12 = this.mCutButton;
            var3 = var5 && this.mSession.getDoc().getSelectionCanBeDeleted();

            highlightView(var12, var3);
            var12 = this.mCopyButton;
            var3 = var5 && this.mSession.getDoc().getSelectionCanBeCopied();

            highlightView(var12, var3);
            var12 = this.mPasteButton;
            var3 = !var7 && (var6 || var5) && this.mSession.getDoc().K();

            highlightView(var12, var3);
            this.updateInsertUIAppearance();
        }
    }

    protected void updateUndoUIAppearance() {
        SODocSession var1 = this.mSession;
        if (var1 != null && var1.getDoc() != null) {
            int var2 = this.mSession.getDoc().getCurrentEdit();
            int var3 = this.mSession.getDoc().getNumEdits();
            ImageView var6 = this.mUndoButton;
            boolean var4 = false;
            boolean var5;
            if (var2 > 0) {
                var5 = true;
            } else {
                var5 = false;
            }

            highlightView(var6, var5);

            var6 = this.mRedoButton;
            var5 = var4;
            if (var2 < var3) {
                var5 = true;
            }

            highlightView(var6, var5);
        }
    }

    protected boolean usePagesView() {
        return true;
    }

    public boolean wasTyping() {
        boolean var1;
        if (System.currentTimeMillis() - this.lastTypingTime < VERSION_TAP_INTERVAL) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }


    protected class TabData {
        public int contentId;
        public int layoutId;
        public String name;
        public int visibility;

        public TabData(String var2, int var3, int var4, int var5) {
            this.name = var2;
            this.contentId = var3;
            this.layoutId = var4;
            this.visibility = var5;
        }
    }

    private TabLayout mTabLayout;

    private void setupTabLayout() {
        mTabLayout = findViewById(R.id.doc_tab_layout);
        if (mTabLayout == null) return;
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                selectedTab(tab);
            }
        });
        mTabLayout.selectTab(mTabLayout.getTabAt(1));
    }

    private static boolean checkStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    protected void selectedTab(TabLayout.Tab tab) {
        if (tab.getText() == getResources().getString(R.string.file)) {
            onBottomMenuButton(mFileTab, getResources().getString(R.string.file));
        } else if (tab.getText() == getResources().getString(R.string.home)) {
            onBottomMenuButton(mHomeTab, getResources().getString(R.string.home));
            if (!checkStoragePermission(this.getContext())) {
                try {
                    Utilities.showMessage((Activity) NUIDocView.this.getContext(),
                            NUIDocView.this.getContext().getString(R.string.edit),
                            NUIDocView.this.getContext().getString(R.string.accept_all_file_permission_edit2));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        } else if (tab.getText() == getResources().getString(R.string.edit)) {
            onBottomMenuButton(mEditTab, getResources().getString(R.string.edit));
        } else if (tab.getText() == getResources().getString(R.string.insert)) {
            onBottomMenuButton(mInsertTab, getResources().getString(R.string.insert));
        } else if (tab.getText() == getResources().getString(R.string.lib_page)) {
            onBottomMenuButton(mPagesTab, getResources().getString(R.string.lib_page));
        } else if (tab.getText() == getResources().getString(R.string.format)) {
            onBottomMenuButton(mFormatTab, getResources().getString(R.string.format));
        } else if (tab.getText() == getResources().getString(R.string.formulas)) {
            onBottomMenuButton(mFormulasTab, getResources().getString(R.string.formulas));
        } else if (tab.getText() == getResources().getString(R.string.annotate)) {
            onBottomMenuButton(mAnnotateTab, getResources().getString(R.string.annotate));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Hiển thị hướng dẫn khi vào màn hình đọc file docx
        final View buttonKeyboard = findViewById(R.id.button_keyboard);
        final View lnTopTool = findViewById(R.id.ln_top_tool);
        final View docInnerContainer = findViewById(R.id.doc_inner_container);
        final View highlightButton = findViewById(R.id.highlight_button);
        if (buttonKeyboard != null) buttonKeyboard.setVisibility(View.GONE);
        String stepDocument = getResources().getString(R.string.guide_step_document);
        String stepKeyboard = getResources().getString(R.string.guide_step_keyboard);
        String stepToolbar = getResources().getString(R.string.guide_step_toolbar);
        String stepHighlight = getResources().getString(R.string.guide_step_highlight);
        String stepHighlight2 = getResources().getString(R.string.guide_step_highlight_2);

        List<GuideStep> steps = new ArrayList<>();

        if (this instanceof NUIDocViewDoc || this instanceof NUIDocViewXls || this instanceof NUIDocViewPpt) {
            steps.add(new GuideStep(docInnerContainer, Arrays.asList(stepDocument),200f, 0));
            steps.add(new GuideStep(buttonKeyboard, Arrays.asList(stepKeyboard),0f,1));
            steps.add(new GuideStep(docInnerContainer, Arrays.asList(stepToolbar),100f, 2));
        } else if (this instanceof NUIDocViewPdf ){
            steps.add(new GuideStep(docInnerContainer, Arrays.asList(stepHighlight),200f, 0));
            steps.add(new GuideStep(highlightButton, Arrays.asList(stepHighlight2),200f, 2));
        }


        int borderColor = Color.RED;
        String filePath = getFilePath();
        String guideKey = null;

        if (filePath != null) {
            PreferencesUtils.putInteger(PresKey.TIME_ENTER_FILES, PreferencesUtils.getInteger(PresKey.TIME_ENTER_FILES, 0) + 1);
            if (this instanceof NUIDocViewDoc) {
                borderColor = ContextCompat.getColor(getContext(), R.color.blue);
                guideKey = PresKey.FIRST_TIME_OPEN_DOC;
                PreferencesUtils.putInteger(PresKey.TIME_ENTER_FILE_DOC, PreferencesUtils.getInteger(PresKey.TIME_ENTER_FILE_DOC, 0) + 1);
            } else if (this instanceof NUIDocViewXls) {
                borderColor = ContextCompat.getColor(getContext(), R.color.green);
                guideKey = PresKey.FIRST_TIME_OPEN_EXCEL;
                PreferencesUtils.putInteger(PresKey.TIME_ENTER_FILE_EXCEL, PreferencesUtils.getInteger(PresKey.TIME_ENTER_FILE_EXCEL, 0) + 1);
            } else if (this instanceof NUIDocViewPpt) {
                borderColor = ContextCompat.getColor(getContext(), R.color.orange);
                guideKey = PresKey.FIRST_TIME_OPEN_PPT;
                PreferencesUtils.putInteger(PresKey.TIME_ENTER_FILE_PPT, PreferencesUtils.getInteger(PresKey.TIME_ENTER_FILE_PPT, 0) + 1);
            } else if (this instanceof NUIDocViewPdf) {
                borderColor = ContextCompat.getColor(getContext(), R.color.primaryColor);
                guideKey = PresKey.FIRST_TIME_OPEN_PDF;
                PreferencesUtils.putInteger(PresKey.TIME_ENTER_FILE_PDF, PreferencesUtils.getInteger(PresKey.TIME_ENTER_FILE_PDF, 0) + 1);
            }
        }

        if (guideKey != null && !PreferencesUtils.getBoolean(guideKey, false)) {
            GuideEditDialog dialog = new GuideEditDialog(getContext(), steps, borderColor);

            dialog.setOnStepChangedListener(new GuideEditDialog.OnStepChangedListener() {
                @Override
                public void onStepChanged(int stepIndex) {
                    if (stepIndex == 1 && buttonKeyboard != null) {
                        buttonKeyboard.setVisibility(View.VISIBLE);
                    }
                }
            });

            final String finalGuideKey = guideKey;
            dialog.setOnDismissListener(dialogInterface -> {
                PreferencesUtils.putBoolean(finalGuideKey, true);
            });

            dialog.show();
        }
    }

}


