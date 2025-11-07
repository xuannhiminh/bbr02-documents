package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.ez.admob.AdmobOpenUtils;
import com.google.android.gms.ads.ez.admob.AdmobUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.analytics.FirebaseAnalTool;
import com.google.android.gms.ads.ez.applovin.ApplovinUtils;
import com.google.android.gms.ads.ez.consent.ConsentUtils;
import com.google.android.gms.ads.ez.consent.OnConsentListener;
import com.google.android.gms.ads.ez.listenner.LoadAdCallback;
import com.google.android.gms.ads.ez.listenner.ShowAdCallback;
import com.google.android.gms.ads.ez.observer.MySubject;
import com.google.android.gms.ads.ez.utils.StateOption;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.ump.FormError;

import java.util.ArrayList;
import java.util.List;

public class EzAdControl {

    private static EzAdControl INSTANCE;
    public static final String KEY_CLOSE_ADS = "close_ads";
    private StateOption stateOption = StateOption.getInstance();
    private DialogLoading dialogLoading;

    public static EzAdControl getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new EzAdControl(context);
        } else {
            INSTANCE.mContext = context;
        }
        return INSTANCE;
    }

    private Activity mContext;
    //    private AdFactoryListener adListener;
    private LoadAdCallback loadAdCallback;
    private ShowAdCallback showAdCallback;
    private AdChecker adChecker;
    private List<String> listAds;
    private boolean isTimeOut = false;
    private boolean isCapping = true;

    private boolean adInitialized = false;

    public String errorCode;

    public EzAdControl(Activity context) {
        mContext = context;
        adChecker = new AdChecker(mContext);
    }

    public static void initAd(Activity context) {
        LogUtils.logString(EzAdControl.class, "Init Ad " + AdUnit.allowShowCMP());
        getInstance(context);
        FirebaseAnalTool.getInstance(context).trackEventAds("init");
        if (AdUnit.allowShowCMP() || !ConsentUtils.getInstance(context).canRequestAds()) {

            if (ConsentUtils.getInstance(context).canRequestAds()) {
                LogUtils.logString(EzAdControl.class, "Consent_Ad: init admob 1 ");
                getInstance(context).initAdmob();
                return;
            }


            LogUtils.logString(EzAdControl.class, "Call show CMP");
            ConsentUtils.getInstance(context).gatherConsent(context, new OnConsentListener() {
                @Override
                public void onComplete(FormError error) {
                    LogUtils.logString(EzAdControl.class, "Consent_Ad: dismis");
                    if (error != null) {
                        // Consent not obtained in current session.
                    }
                    if (ConsentUtils.getInstance(context).canRequestAds()) {
                        LogUtils.logString(EzAdControl.class, "Consent_Ad: init admob 2 ");
                        getInstance(context).initAdmob();
                    }
                }
            });



        } else {
            getInstance(context).initAdmob();
        }
        ApplovinUtils.getInstance(context).init();
        FacebookUtils.getInstance(context).init();
        getInstance(context).loadAd();
    }

    public boolean isAdInitialized() {
        return adInitialized;
    }


    private void initAdmob() {
        LogUtils.logString(EzAdControl.class, "Init Admob ");
        MobileAds.initialize(mContext, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                LogUtils.logString(EzAdControl.class, "Init Success ");
                adInitialized = true;
                if (loadAdCallback != null) {
                    setLoadAdCallback(loadAdCallback);
                }
                FirebaseAnalTool.getInstance(mContext).trackEventAds("init_success");
                loadAd();
            }
        });
    }

    public EzAdControl setLoadAdCallback(LoadAdCallback callback) {
        LogUtils.logString(EzAdControl.class, "setLoadAdCallback");
        loadAdCallback = callback;
        if (!adInitialized) {
            return this;
        }
        new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                setLoadAdSuccess(true);
            }
        }.start();

        new CountDownTimer(10000, 10000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                isTimeOut = true;
                if (!adInitialized) {
                    LogUtils.logString(EzAdControl.class, "Request time out chua init ad xong");
                    return;
                }
                if (stateOption.isLoaded()) {
                    LogUtils.logString(EzAdControl.class, "Request time out da call onLoaded roi");
                    return;
                }

                if (stateOption.isShowed()) {
                    LogUtils.logString(EzAdControl.class, "Request time out dang show ads");
                    return;
                }
                // neu den day ma 1 trong 4 net loaded thi tra ve onloaded - neu ca 4 deu chua thi onerror - k can quan tam co dang loading hay k
                if (AdmobOpenUtils.getInstance(mContext).isLoaded()
                        || AdmobUtils.getInstance(mContext).isLoaded()
                        || ApplovinUtils.getInstance(mContext).isLoaded()
                        || AdxUtils.getInstance(mContext).isLoaded()) {
                    LogUtils.logString(EzAdControl.class, "Request time out but a network loaded");
                    stateOption.setOnLoaded();
                    if (loadAdCallback != null) {
                        loadAdCallback.onLoaded();
                        LogUtils.logString(EzAdControl.class, "loadAdCallback4__________null ");
                        loadAdCallback = null;
                    }
                } else {
                    LogUtils.logString(EzAdControl.class, "Request time out onError");
                    stateOption.setOnFailed();
                    if (loadAdCallback != null) {
                        errorCode = "Request Timeout";
                        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_load_ad__error_timeout");
                        loadAdCallback.onError();
                        LogUtils.logString(EzAdControl.class, "loadAdCallback5__________null ");
                        loadAdCallback = null;
                    }
                }
            }
        }.start();
        return this;
    }

    public EzAdControl setShowAdCallback(ShowAdCallback showAdCallback) {
        this.showAdCallback = showAdCallback;


        return this;
    }

    public void loadAd() {

        if (!adInitialized) {
            return;
        }
        LogUtils.logString(EzAdControl.class, "Call load ad");
        FirebaseAnalTool.getInstance(mContext).trackEventAds("start_load_ads");
        loadAdmob();
        loadAdx();
        loadAdmobOpen();
        loadApplovin();

        stateOption.setOnLoading();


    }

    private void loadFacebook() {
        LogUtils.logString(EzAdControl.class, "Load Facebook");
        FacebookUtils.getInstance(mContext)
                .setLoadAdCallback(new LoadAdCallback() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzAdControl.class, "Facebook onError");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, "Facebook Loaded");
                        setLoadAdSuccess();
                    }


                }).loadAds();
    }

    private void loadApplovin() {
        LogUtils.logString(EzAdControl.class, "Load  Applovin");
        ApplovinUtils.getInstance(mContext)
                .setLoadAdCallback(new LoadAdCallback() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzAdControl.class, "Applovin onError");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, "Applovin Loaded");
                        setLoadAdSuccess();
                    }


                }).loadAds();
    }

    private void loadAdmob() {
        LogUtils.logString(EzAdControl.class, "Load Admob ");
        AdmobUtils.getInstance(mContext)
                .setLoadAdCallback(new LoadAdCallback() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzAdControl.class, "Admob onError");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, "Admob Loaded");
                        setLoadAdSuccess();
                    }


                }).loadAds();
    }

    private void loadAdmobOpen() {

        LogUtils.logString(EzAdControl.class, "Load Admob Open");
        AdmobOpenUtils.getInstance(mContext)
                .setLoadAdCallback(new LoadAdCallback() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzAdControl.class, "Admob Open onError");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, "Admob Open Loaded");

                        setLoadAdSuccess();
                    }


                }).loadAds();
    }

    private void loadAdx() {
        LogUtils.logString(EzAdControl.class, "Load Adx");
        AdxUtils.getInstance(mContext)
                .setLoadAdCallback(new LoadAdCallback() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzAdControl.class, "Adx onError");
                        setLoadAdSuccess();
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzAdControl.class, "Adx Loaded");
                        setLoadAdSuccess();
                    }


                }).loadAds();
    }


    public void showAds() {
        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads_1");

        isOpen = false;
        LogUtils.logString(EzAdControl.class, "showAds");
        listAds = new ArrayList<>();

        String[] array = AdUnit.getMasterAdsNetwork().split(",", -1);
        LogUtils.logString(EzAdControl.class, AdUnit.getMasterAdsNetwork());
        for (String s : array) {

            listAds.add(s);
        }


        Message message = mHandler1.obtainMessage();
        message.arg1 = 1;
        message.sendToTarget();
    }

    private boolean isOpen = false;

    public void showOpenAds() {
        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads_1");

        isOpen = true;
        LogUtils.logString(EzAdControl.class, "showOpenAds");
        this.isCapping = false;
        listAds = new ArrayList<>();

        String[] array = AdUnit.getMasterOpenAdsNetwork().split(",", -1);
        for (String s : array) {
            listAds.add(s);
        }


        Message message = mHandler1.obtainMessage();
        message.arg1 = 1;
        message.sendToTarget();
    }

    public void showAdsWithoutCapping() {
        this.isCapping = false;
        showAds();
    }


    private boolean isloading() {
        return AdmobUtils.getInstance(mContext).isLoading();
    }

    private boolean isLoaded() {
        if (AdmobUtils.getInstance(mContext).isLoaded()) {
            return true;
        } else {
            // dang check tai sao lai request time out ow day
            // tim cac de isloaded vs adx
        }
        return AdmobUtils.getInstance(mContext).isLoaded();
    }


    final Handler mHandler1 = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            showAdss();
        }
    };

    private boolean showAdss() {

        LogUtils.logString(EzAdControl.class, "Call Show Ad " + isCapping);
        if (isCapping) {
            if (!adChecker.checkShowAds()) {
                LogUtils.logString(EzAdControl.class, "Ad Checker false");
                FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__display_adchecker");
                setOnAdDisplayFaild();
                return false;
            }
        } else {
            if (IAPUtils.getInstance().isPremium()) {
                LogUtils.logString(EzAdControl.class, "Da mua khong show ads");
                FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__display_fail_premium");
                setOnAdDisplayFaild();
                return false;
            }
            isCapping = true;
        }


//        if (isOpen) {
        if (true) {
            dialogLoading = new DialogLoading(mContext);

            if (!mContext.isDestroyed() && !mContext.isFinishing() && !dialogLoading.isShowing()) {
                dialogLoading.show();
            }
            new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    showAds2();
                    new CountDownTimer(200, 200) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            if (!mContext.isDestroyed() && !mContext.isFinishing()) {
                                hideProgress();
                            }
                        }
                    }.start();
                }
            }.start();
        } else {
            showAds2();
        }
//        loadAd();
        return false;

    }

    public void hideProgress() {
        if (dialogLoading != null) {
            if (dialogLoading.isShowing()) { //check if dialog is showing.

                //get the Context object that was used to great the dialog
                Context context = ((ContextWrapper) dialogLoading.getContext()).getBaseContext();

                //if the Context used here was an activity AND it hasn't been finished or destroyed
                //then dismiss it
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed())
                        dialogLoading.dismiss();
                } else //if the Context used wasnt an Activity, then dismiss it too
                    dialogLoading.dismiss();
            }
            dialogLoading = null;
        }
    }

    private boolean showAds2() {
        // neu het phan tu thi return ve 0, luc nay da load qua 4 mang nhung k show dc
        if (listAds == null || listAds.size() == 0) {
            FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__display_fail_list_null");
            setOnAdDisplayFaild();
            return false;
        }
        String network = listAds.get(0);

        listAds.remove(0);


        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads_2");
        LogUtils.logString(EzAdControl.class, network);
        switch (network) {
            case "facebook":
                if (FacebookUtils.getInstance(mContext).setShowAdCallback(new ShowAdCallback() {
                    @Override
                    public void onDisplay() {

                        new CountDownTimer(100, 100) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                OverlayView.showOverlay(EzApplication.getInstance().getCurrentActivity());
                            }
                        }.start();


                        setOnAdDisplayed();
                    }

                    @Override
                    public void onDisplayFaild() {
                    }

                    @Override
                    public void onClosed() {
                        LogUtils.logString(EzAdControl.class, "Facebook  onClosed");
                        setOnAdClosed();
                    }
                }).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show Facebook Success");
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;
            case "admob":
                if (AdmobUtils.getInstance(mContext).setShowAdCallback(new ShowAdCallback() {
                    @Override
                    public void onDisplay() {
                        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__admob_display");
                        LogUtils.logString(EzAdControl.class, "Admob onDisplay");
                        setOnAdDisplayed();
                    }

                    @Override
                    public void onDisplayFaild() {
                        LogUtils.logString(EzAdControl.class, "Admob onDisplayFaild");
                    }

                    @Override
                    public void onClosed() {
                        LogUtils.logString(EzAdControl.class, "Admob onClosed");
                        setOnAdClosed();
                    }

                    @Override
                    public void onClickAd() {
                        super.onClickAd();
                        if (showAdCallback != null) {
                            showAdCallback.onClickAd();
                        }
//                        FirebaseAnalTool.getInstance(mContext).trackEvent(ACTION_SHOW_ADS,
//                                EzApplication.getInstance().getOldActivity().getClass().getSimpleName(), FirebaseAnalTool.Param.AD_CLICK);
                    }
                }).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show Admob Success");
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;
            case "max":
                if (ApplovinUtils.getInstance(mContext).setShowAdCallback(new ShowAdCallback() {
                    @Override
                    public void onDisplay() {
                        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__max_display");
                        setOnAdDisplayed();
                    }

                    @Override
                    public void onDisplayFaild() {
                    }

                    @Override
                    public void onClosed() {
                        LogUtils.logString(EzAdControl.class, "Admob Open onClosed");
                        setOnAdClosed();
                    }
                }).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show applovin Success");
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;

            case "open":
                if (AdmobOpenUtils.getInstance(mContext).setShowAdCallback(new ShowAdCallback() {
                    @Override
                    public void onDisplay() {
                        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__open_display");
                        setOnAdDisplayed();
                    }

                    @Override
                    public void onDisplayFaild() {
                    }

                    @Override
                    public void onClosed() {
                        LogUtils.logString(EzAdControl.class, "Admob Open onClosed");
                        setOnAdClosed();
                    }
                }).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show Admob Open Success");
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;
            case "adx":

                if (AdxUtils.getInstance(mContext).setShowAdCallback(new ShowAdCallback() {
                    @Override
                    public void onDisplay() {
                        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__adx_display");
                        setOnAdDisplayed();
                    }

                    @Override
                    public void onDisplayFaild() {
                    }

                    @Override
                    public void onClosed() {
                        LogUtils.logString(EzAdControl.class, "Adx Open onClosed");
                        setOnAdClosed();
                    }
                }).showAds()) {
                    LogUtils.logString(EzAdControl.class, "Show Adx Success ");
                    return true;
                }
                if (showAds2()) {
                    return true;
                }
                break;
            default:
                if (showAds2()) {
                    return true;
                }
                break;
        }

        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__display_fail_no_ad");
        setOnAdDisplayFaild();

        loadAd();
        return false;
    }


    private void setLoadAdSuccess(boolean... acceptRecallListenners) {
        // chi nghe theo admob inter
        // neu admob inter dang loading thi bo qua k can lam gi -> cho admob xong hoac requet time out ( tuc la k lam gi)
        // neu admob inter da load xong
        // admob inter loaded thi tra ve luon
        // neu admob inter error thi k lam gi -> cho tat ca cac net xong hoac requet time out ( tuc la k lam gi)

        if (!adInitialized) {
            LogUtils.logString(EzAdControl.class, "Chua init ad xong");
            return;
        }

        boolean acceptRecallListenner = false;
        if (acceptRecallListenners.length != 0) {
            acceptRecallListenner = acceptRecallListenners[0];
        }

        if (!acceptRecallListenner) {
            if (stateOption.isLoaded()) {
                return;
            }
        }
        LogUtils.logString(EzAdControl.class, "ads__________1");
        if (AdmobUtils.getInstance(mContext).isLoading() || AdmobOpenUtils.getInstance(mContext).isLoading()
                | AdxUtils.getInstance(mContext).isLoading()
                || (AdUnit.getForceWaitApplovin() ? ApplovinUtils.getInstance(mContext).isLoading() : false)) {
            // bo qua
        } else {

            LogUtils.logString(EzAdControl.class, "ads__________2 " + isTimeOut);


            // admob da load xong
            if (AdmobUtils.getInstance(mContext).isLoaded() || AdmobOpenUtils.getInstance(mContext).isLoaded()
                    || ApplovinUtils.getInstance(mContext).isLoaded()) {
                LogUtils.logString(EzAdControl.class, "ads__________3");
                // tra ve onloaded
                LogUtils.logString(EzAdControl.class, "LoadAdSuccess Admob onLoaded");
                stateOption.setOnLoaded();
                if (loadAdCallback != null) {
                    loadAdCallback.onLoaded();
                    LogUtils.logString(EzAdControl.class, "loadAdCallback6__________null ");
                    loadAdCallback = null;
                }
            } else if (!AdmobOpenUtils.getInstance(mContext).isLoading()
                    && !AdmobUtils.getInstance(mContext).isLoading()
                    && !ApplovinUtils.getInstance(mContext).isLoading()
                    && !AdxUtils.getInstance(mContext).isLoading()) {
                // neu tat ca cac net deu da load xong thi chi can 1 net da loaded la dc
                LogUtils.logString(EzAdControl.class, "ads__________4");
                if (AdmobOpenUtils.getInstance(mContext).isLoaded()
                        || AdmobUtils.getInstance(mContext).isLoaded()
                        || ApplovinUtils.getInstance(mContext).isLoaded()
                        || AdxUtils.getInstance(mContext).isLoaded()) {
                    LogUtils.logString(EzAdControl.class, "LoadAdSuccess All success onLoaded");
                    stateOption.setOnLoaded();
                    if (loadAdCallback != null) {
                        loadAdCallback.onLoaded();
                        LogUtils.logString(EzAdControl.class, "loadAdCallback7__________null ");
                        loadAdCallback = null;


                    }
                } else {
                    LogUtils.logString(EzAdControl.class, "LoadAdSuccess All success onError");
                    stateOption.setOnFailed();
                    if (loadAdCallback != null) {
                        errorCode = "All net error";
                        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_load_ad__error_all_net_error");
                        loadAdCallback.onError();

                        LogUtils.logString(EzAdControl.class, "loadAdCallback1__________null ");
                        loadAdCallback = null;
                    }
                }


            } else if (isTimeOut) {
                LogUtils.logString(EzAdControl.class, "ads__________5_timeout " + loadAdCallback);
                // neu van dang con nhieu hon 1 thang loading thi ma da timeout roi thi chi can 1 thang loaded la dc
                if (AdmobOpenUtils.getInstance(mContext).isLoaded()
                        || AdmobUtils.getInstance(mContext).isLoaded()
                        || ApplovinUtils.getInstance(mContext).isLoaded()
                        || AdxUtils.getInstance(mContext).isLoaded()) {
                    LogUtils.logString(EzAdControl.class, "Request time out but a network loaded " + loadAdCallback);
                    stateOption.setOnLoaded();
                    if (loadAdCallback != null) {
                        LogUtils.logString(EzAdControl.class, "ads__________7_call_loaded");
                        loadAdCallback.onLoaded();
                        LogUtils.logString(EzAdControl.class, "loadAdCallback2__________null ");
                        loadAdCallback = null;
                    }
                } else {
                    LogUtils.logString(EzAdControl.class, "Request time out onError");
                    stateOption.setOnFailed();
                    if (loadAdCallback != null) {
                        errorCode = "Request Timeout 2";
                        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_load_ad__error_timeout_2");
                        loadAdCallback.onError();
                        LogUtils.logString(EzAdControl.class, "loadAdCallback3__________null ");
                        loadAdCallback = null;
                    }
                }
            } else {
                LogUtils.logString(EzAdControl.class, "ads__________6");
            }
        }


    }


    private void setOnAdDisplayed() {
        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__display_success");
        LogUtils.logString(EzAdControl.class, "setOnAdDisplayed ");


        if (showAdCallback != null) {
            showAdCallback.onDisplay();
        }
        stateOption.setShowAd();
    }

    private void setOnAdClosed() {
        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__close");
        loadAd();
        if (showAdCallback != null) {
            showAdCallback.onClosed();
            Log.e("TAG", "onFinishxx: 4");
            showAdCallback = null;
        }
        stateOption.setDismisAd();
        adChecker.setShowAds();
        MySubject.getInstance().notifyChange(KEY_CLOSE_ADS);
    }

    private void setOnAdDisplayFaild() {
        FirebaseAnalTool.getInstance(mContext).trackEventAds("call_show_ads__display_fail");
        if (showAdCallback != null) {
            showAdCallback.onDisplayFaild();
            Log.e("TAG", "onFinishxx: 5");
            showAdCallback = null;
        }
    }
}