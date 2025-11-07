package com.google.android.gms.ads.ez.admob;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.ez.AdsFactory2;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.analytics.FirebaseAnalTool;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdmobUtils extends AdsFactory2 {

    public static AdmobUtils INSTANCE;

    public static AdmobUtils getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new AdmobUtils(context);
        }
        INSTANCE.mContext = context;
        return INSTANCE;
    }

    public void init() {
        MobileAds.initialize(mContext, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
    }


    public AdmobUtils(Activity mContext) {
        super(mContext);
    }

    private InterstitialAd admobInterstitialAd;

    @Override
    public boolean loadAdNetwork() {

        String id = AdUnit.getAdmobInterId();
        LogUtils.logString(this, "LoadAdsNetwork " + getNameAd() + " With Id " + id);

        if (id.equals("")) {
            setAdError();
            return false;
        }


        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(mContext, id, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {


                LogUtils.logString(AdmobUtils.class, "Admob Loaded");
                admobInterstitialAd = interstitialAd;
                setAdLoaded();


            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                admobInterstitialAd = null;
                LogUtils.logString(AdmobUtils.class, "Admob Failed " + loadAdError.getMessage() + "  ");
                setAdError();
            }
        });
        return true;
    }


    @Override
    public String getNameAd() {
        return "Admob Inter";
    }

    @Override
    public boolean showAds() {
        if (admobInterstitialAd != null && mContext != null) {


            admobInterstitialAd.setOnPaidEventListener(new OnPaidEventListener() {
                @Override
                public void onPaidEvent(AdValue adValue) {
                    FirebaseAnalTool.getInstance(mContext).loadDailyAdsRevenue(adValue.getValueMicros() / 1000000f, "Inter");
                }
            });

            admobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    setAdClick();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    LogUtils.logString(AdmobUtils.class, "Admob Closed");
                    admobInterstitialAd = null;
                    setAdClosed();


                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {

                    // Called when fullscreen content failed to show.
                    LogUtils.logString(AdmobUtils.class, "Admob Display Fail");
                    admobInterstitialAd = null;
                    setAdDisplayFailed(adError.getMessage());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    LogUtils.logString(AdmobUtils.class, "Admob Display Success");
                    admobInterstitialAd = null;
                    setAdDisplay();


                }
            });
            admobInterstitialAd.show(mContext);
            stateOption.setShowAd();
            return true;
        }
        LogUtils.logString(AdmobUtils.class, "Not Accept show ads");
        return false;
    }
}
