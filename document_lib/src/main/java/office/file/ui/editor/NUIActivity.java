package office.file.ui.editor;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.artifex.solib.ConfigOptions;
import com.artifex.solib.k;
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil;
import com.brian.base_iap.utils.IAPUtils;
import com.brian.base_iap.utils.PreferencesUtils;
import com.ezteam.baseproject.utils.PresKey;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.nlbn.ads.callback.NativeCallback;
import com.nlbn.ads.util.Admob;

import office.file.ui.IdController;
import office.file.ui.editor.NUIView.OnDoneListener;
import office.file.ui.editor.R.id;
import office.file.ui.editor.R.layout;
import office.file.ui.editor.R.string;
import office.file.ui.editor.SODocSession.SODocSessionLoadListenerCustom;

public class NUIActivity extends BaseActivity {
    private static SODocSession useSession;
    private Intent mChildIntent = null;
    private Configuration mLastConfiguration;
    private int mLastKeyCode = -1;
    private long mLastKeyTime = 0L;
    protected NUIView mNUIView;
    String filePath = "";

    public NUIActivity() {
    }

    private void initializeFromIntent() {
        try {
            Intent incomingIntent = this.getIntent();
            boolean hasSessionFlag ;
            Bundle extras = incomingIntent != null ? incomingIntent.getExtras() : null;
            if (extras != null) {
                hasSessionFlag  = extras.getBoolean("SESSION", false);
                this.setCustomConfiguration(incomingIntent);
            } else {
                hasSessionFlag  = false;
            }

            if (hasSessionFlag  && useSession == null) {
                super.finish();
            } else {
                this.processOpenIntent(incomingIntent, false);
            }
        } catch (Exception ex) {
            finish();
        }
    }

    private void processOpenIntent(Intent intent, boolean isNewIntent) {
        Bundle extras = intent.getExtras();
        if (k.c().B()) {
            this.mNUIView = null;
            try {
                Utilities.showMessageAndFinish(this, this.getString(string.sodk_editor_error), this.getString(string.sodk_editor_has_no_permission_to_open));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            boolean falsePlaceholder  = false;
            boolean isSessionOpening;
            if (extras != null) {
                isSessionOpening = extras.getBoolean("SESSION", false);
//                isSessionOpening = false;
            } else {
                isSessionOpening = false;
            }

            SODocSession existingSession = useSession;
            this.setContentView(layout.sodk_editor_doc_view_activity);
            NUIView nuiView = (NUIView) this.findViewById(id.doc_view);
            this.mNUIView = nuiView;
            nuiView.setOnDoneListener(new OnDoneListener() {
                public void done() {
//                    if(!Objects.requireNonNull(MainActivity.Companion.getInstance()).isMainRunning()){
                    /*if (MainActivity.Companion.getInstance() == null) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }*/
                    /*if (AppConfigs.getBoolean(RemoteKey.CAN_BACK_HOME)) {
                        String activityToStart = "com.ezstudio.pdfreaderver4.activity.MainActivity";
                        try {
                            Class<?> c = Class.forName(activityToStart);
                            Intent intent = new Intent(NUIActivity.this, c);
                            intent.putExtra("FROM_DETAIL", true);
                            startActivity(intent);
                        } catch (ClassNotFoundException ignored) {
                        }
                    }*/
                    NUIActivity.super.finish();
                }
            });
            int startPage;
            String foreignData;
            String customDocData;
            SOFileState fileState;
            String resolvedCustomDocData;
            if (extras != null) {
                boolean isTemplateFlag;
                label35:
                {
                    startPage = extras.getInt("START_PAGE");
                    SOFileState autoOpenState = SOFileState.fromString(extras.getString("STATE"), SOFileDatabase.getDatabase());
                    foreignData = extras.getString("FOREIGN_DATA", (String) null);
                    isTemplateFlag = extras.getBoolean("IS_TEMPLATE", true);
                    customDocData = extras.getString("CUSTOM_DOC_DATA");
                    fileState = autoOpenState;
                    if (autoOpenState == null) {
                        fileState = null;
                        if (!isNewIntent) {
                            autoOpenState = SOFileState.getAutoOpen(this);
                            fileState = autoOpenState;
                            if (autoOpenState != null) {
                                isNewIntent = falsePlaceholder ;
                                fileState = autoOpenState;
                                break label35;
                            }
                        }
                    }

                    isNewIntent = isSessionOpening;
                }

                resolvedCustomDocData = customDocData;
                isSessionOpening = isNewIntent;
                isNewIntent = isTemplateFlag;
            } else {
                customDocData = null;
                resolvedCustomDocData = null;
                isNewIntent = true;
                startPage = 1;
                foreignData = null;
                fileState = null;
            }

            if (resolvedCustomDocData == null) {
                Utilities.setSessionLoadListener((SODocSessionLoadListenerCustom) null);
            }

            filePath = getIntent().getData().getPath();
            if (isSessionOpening) {
                this.mNUIView.start(existingSession, startPage, foreignData, filePath);
            } else if (false) { // temporary set as false to fix bug open a file from another app when already opened a file
                this.mNUIView.start(fileState, startPage, filePath);
            } else {
                Uri uri = intent.getData();
                String type = intent.getType();
                this.mNUIView.start(uri, isNewIntent, startPage, resolvedCustomDocData, type, filePath);
            }

            this.checkIAP();
        }
    }

    private void maybeRecreateOnUiModeChange(Configuration var1) {
        if (VERSION.SDK_INT >= 28 && var1.uiMode != this.mLastConfiguration.uiMode) {
            this.onPause();
            super.finish();
            this.startActivity(this.getIntent());
        }

        this.mLastConfiguration = this.getResources().getConfiguration();
    }

    public static void setSession(SODocSession var0) {
        useSession = var0;
    }

    protected void checkIAP() {
    }

    public Intent childIntent() {
        return this.mChildIntent;
    }

    protected void doResumeActions() {
        NUIView var1 = this.mNUIView;
        if (var1 != null) {
            var1.onResume();
        }

        this.maybeRecreateOnUiModeChange(this.getResources().getConfiguration());
    }

    public void finish() {
        if (this.mNUIView == null) {
            super.finish();
        } else {
            Utilities.dismissCurrentAlert();
            this.onBackPressed();
        }

    }

    protected void initialise() {
        this.initializeFromIntent();
    }

    public boolean isDocModified() {
        NUIView var1 = this.mNUIView;
        return var1 != null && var1.isDocModified();
    }

    protected void onActivityResult(int var1, int var2, Intent var3) {
        NUIView var4 = this.mNUIView;
        if (var4 != null) {
            var4.onActivityResult(var1, var2, var3);
        }
    }

    public void onBackPressed() {
        NUIView var1 = this.mNUIView;
        if (var1 != null) {
            var1.onBackPressed(false);
        }
    }

    public void onConfigurationChanged(Configuration var1) {
        super.onConfigurationChanged(var1);
        this.maybeRecreateOnUiModeChange(var1);
        this.mNUIView.onConfigurationChange(var1);
    }

    protected void onCreate(Bundle var1) {
        super.onCreate(var1);
        this.mLastConfiguration = this.getResources().getConfiguration();
        this.initialise();
        if (getIntent().getData() != null) {
            filePath = getIntent().getData().getPath();
        }
        loadNativeNomedia();
        Log.d(this.getClass().getSimpleName(), "onCreate: 1");
    }
    private void loadNativeNomedia() {
        if (IAPUtils.INSTANCE.isPremium()) {
            FrameLayout layoutNative = findViewById(IdController.getIntid("layout_native"));
            if( layoutNative != null ) {
                layoutNative.setVisibility(View.GONE);
            }
            return;
        }

        Log.d(this.getClass().getSimpleName(), "loadNativeNomedia: 1");

        FrameLayout layoutNative = findViewById(IdController.getIntid("layout_native"));
        if( layoutNative != null ) {
            layoutNative.setVisibility(View.VISIBLE);
            View loadingView = LayoutInflater.from(this).inflate(com.ezteam.baseproject.R.layout.ads_native_loading_short, null);
            layoutNative.removeAllViews();
            layoutNative.addView(loadingView);
            NativeCallback callback = new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    Log.d(this.getClass().getSimpleName(), "loadNativeNomedia: 4");

                    // Inflate the native ad layout
                    View adView = LayoutInflater.from(NUIActivity.this)
                            .inflate(com.ezteam.baseproject.R.layout.ads_native_bot_no_media_short, null);
                    NativeAdView nativeAdView = (NativeAdView) adView;

                    // Remove loading view and add the native ad view
                    layoutNative.removeAllViews();
                    layoutNative.addView(nativeAdView);

                    // Populate the native ad view with the ad assets
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, nativeAdView);
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    Log.d(this.getClass().getSimpleName(), "loadNativeNomedia: 5");
                    layoutNative.setVisibility(View.GONE);
                }
            };

            // Load the native ad
            Admob.getInstance().loadNativeAd(
                    getApplicationContext(),
                    FirebaseRemoteConfigUtil.Companion.getInstance().getAdsConfigValue("native_filedetail"),
                    callback
            );
        }
    }



    protected void onDestroy() {
        //sendBroadcast(new Intent(ActivityExtKt.getSHOW_RATE()));
        NUIView var1 = this.mNUIView;
        if (var1 != null) {
            var1.onDestroy();
        }
        if (var1 != null && var1.mDocView != null) {
            PreferencesUtils.putInteger(PresKey.LAST_PAGE, var1.mDocView.mCurrentPageNum);
        }
        super.onDestroy();
    }

    public boolean onKeyDown(int var1, KeyEvent var2) {
        long var3 = var2.getEventTime();
        if (this.mLastKeyCode == var1 && var3 - this.mLastKeyTime <= 100L) {
            return true;
        } else {
            this.mLastKeyTime = var3;
            this.mLastKeyCode = var1;
            return this.mNUIView.doKeyDown(var1, var2);
        }
    }

    public void onNewIntent(final Intent newIntent) {
        k.c();
        if (this.isDocModified()) {
            Utilities.yesNoMessage(this, this.getString(string.sodk_editor_new_intent_title), this.getString(string.sodk_editor_new_intent_body), this.getString(string.sodk_editor_new_intent_yes_button), this.getString(string.sodk_editor_new_intent_no_button), new Runnable() {
                public void run() {
                    if (NUIActivity.this.mNUIView != null) {
                        NUIActivity.this.mNUIView.endDocSession(true);
                    }

                    NUIActivity.this.setCustomConfiguration(newIntent);
                    NUIActivity.this.processOpenIntent(newIntent, true);
                }
            }, new Runnable() {
                public void run() {
                    SODocSessionLoadListenerCustom intentToProcess = Utilities.getSessionLoadListener();
                    if (intentToProcess != null) {
                        intentToProcess.onSessionReject();
                    }

                    Utilities.setSessionLoadListener((SODocSessionLoadListenerCustom) null);
                }
            });
        } else {
            NUIView var2 = this.mNUIView;
            if (var2 != null) {
                var2.endDocSession(true);
            }

            this.setCustomConfiguration(newIntent);
            this.processOpenIntent(newIntent, true);
        }

    }

    public void onPause() {
        NUIView var1 = this.mNUIView;
        if (var1 != null) {
            var1.onPause();
            this.mNUIView.releaseBitmaps();
        }

        if (Utilities.isChromebook(this)) {
            PrintHelperPdf.setPrinting(false);
        }

        super.onPause();
    }

    protected void onResume() {
        this.mChildIntent = null;
        super.onResume();
        this.doResumeActions();
    }


    protected void setConfigurableButtons() {
        NUIView var1 = this.mNUIView;
        if (var1 != null) {
            var1.setConfigurableButtons();
        }

    }

    protected void setCustomConfiguration(Intent var1) {
        try {
            ConfigOptions var2 = k.c();
            Log.e("TAGGGGGGGGGGGGGG", "setCustomConfiguration: " + var2);

            Bundle var3 = var1.getExtras();
            if (var1.hasExtra("ENABLE_SAVE")) {
                var2.o(var3.getBoolean("ENABLE_SAVE", true));
            }

            if (var1.hasExtra("ENABLE_SAVEAS")) {
                var2.b(var3.getBoolean("ENABLE_SAVEAS", true));
            }

            if (var1.hasExtra("ENABLE_SAVEAS_PDF")) {
                var2.c(var3.getBoolean("ENABLE_SAVEAS_PDF", true));
            }

            if (var1.hasExtra("ENABLE_CUSTOM_SAVE")) {
                var2.p(var3.getBoolean("ENABLE_CUSTOM_SAVE", true));
            }

            if (var1.hasExtra("ALLOW_AUTO_OPEN")) {
                var2.r(var3.getBoolean("ALLOW_AUTO_OPEN", true));
            }

        } catch (Exception ex) {

        }
    }

    public void startActivity(Intent var1) {
        this.mChildIntent = var1;
        super.startActivity(var1, (Bundle) null);
    }

    public void startActivityForResult(Intent var1, int var2) {
        this.mChildIntent = var1;
        super.startActivityForResult(var1, var2);
    }

    public void startActivityForResult(Intent var1, int var2, Bundle var3) {
        this.mChildIntent = var1;
        super.startActivityForResult(var1, var2, var3);
    }
}
