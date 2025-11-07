package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.ez.listenner.LoadAdCallback;
import com.google.android.gms.ads.ez.listenner.ShowAdCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class InterstitialUtils {
//    private final String HIGH_ID = "ca-app-pub-5025628276811480/7043791352";
//    private final String MEDIUM_ID = "ca-app-pub-5025628276811480/8438131053";
//    private final String ALL_ID = "ca-app-pub-5025628276811480/5347566302";

    private final String HIGH_ID = "";
    private final String MEDIUM_ID = "";
    private final String ALL_ID = "";


    private Activity mContext;
    private InterstitialAd admobInterstitialAd;
    protected LoadAdCallback loadAdCallback;
    protected ShowAdCallback showAdCallback;
    private DialogLoading dialogLoading;
    private AdChecker adChecker;
    private boolean isLoading;
    private boolean isCountDownFinish;
    private boolean isRequestTimeout;

    private AdRequest adRequest;
    private int countLoadAds;

    public static InterstitialUtils getInstance() {
        return new InterstitialUtils();
    }

    public InterstitialUtils() {
        this.mContext = EzApplication.getInstance().getCurrentActivity();
        adChecker = new AdChecker(mContext);
        dialogLoading = new DialogLoading(mContext);
        adRequest = new AdRequest.Builder().build();
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                isCountDownFinish = true;
                showAds();
            }
        }.start();

        new CountDownTimer(5000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                isRequestTimeout = true;
                if (isLoading) {
                    // thoat
                    dismisDialogLoading();
                    if (loadAdCallback != null) {
                        loadAdCallback.onError();
                        loadAdCallback = null;
                    }
                }
            }
        }.start();
    }

    public void loadAndShowAds() {
        if (!adChecker.checkShowAds()) {
            LogUtils.logString(InterstitialUtils.class, "Ad Checker false");
            if (loadAdCallback != null) {
                loadAdCallback.onError();
                loadAdCallback = null;
            }
            return;
        }
        loadAds(HIGH_ID);
    }

    private void loadAds(String id) {
        LogUtils.logString(InterstitialUtils.class, "loadAds");
        countLoadAds++;
        isLoading = true;

        dialogLoading.show();

        InterstitialAd.load(mContext, id, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                LogUtils.logString(InterstitialUtils.class, "Admob Loaded");
                admobInterstitialAd = interstitialAd;
                isLoading = false;
                showAds();
                if (loadAdCallback != null) {
                    loadAdCallback.onLoaded();
                    loadAdCallback = null;
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                LogUtils.logString(InterstitialUtils.class, "Admob Fail " + countLoadAds);
                if (countLoadAds == 1) {
                    loadAds(MEDIUM_ID);
                } else if (countLoadAds == 2) {
                    loadAds(ALL_ID);
                } else {
                    LogUtils.logString(InterstitialUtils.class, "Admob Failed " + loadAdError.getMessage() + "  ");
                    admobInterstitialAd = null;
                    isLoading = false;
                    dismisDialogLoading();
                    if (loadAdCallback != null) {
                        loadAdCallback.onError();
                        loadAdCallback = null;
                    }
                }
            }
        });
    }

    private void showAds() {
        if (admobInterstitialAd != null && mContext != null && !isLoading && isCountDownFinish && !isRequestTimeout) {
            dismisDialogLoading();
            admobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    LogUtils.logString(InterstitialUtils.class, "Admob Closed");
                    admobInterstitialAd = null;
                    adChecker.setShowAds();
                    if (showAdCallback != null) {
                        showAdCallback.onClosed();
                        showAdCallback = null;
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when fullscreen content failed to show.
                    LogUtils.logString(InterstitialUtils.class, "Admob Display Fail");
                    admobInterstitialAd = null;
                    if (showAdCallback != null) {
                        showAdCallback.onDisplayFaild();
                        showAdCallback = null;
                    }
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    LogUtils.logString(InterstitialUtils.class, "Admob Display Success");
                    admobInterstitialAd = null;
                    if (showAdCallback != null) {
                        showAdCallback.onDisplay();
                    }
                }
            });
            admobInterstitialAd.show(mContext);
        }
        LogUtils.logString(InterstitialUtils.class, "Not Accept show ads");
    }

    private void dismisDialogLoading() {
        new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                dialogLoading.dismiss();
            }
        }.start();
    }

    public InterstitialUtils setLoadAdCallback(LoadAdCallback loadAdCallback) {
        this.loadAdCallback = loadAdCallback;
        return this;
    }

    public InterstitialUtils setShowAdCallback(ShowAdCallback showAdCallback) {
        this.showAdCallback = showAdCallback;
        return this;
    }
}
