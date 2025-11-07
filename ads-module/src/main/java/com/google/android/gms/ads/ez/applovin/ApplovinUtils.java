package com.google.android.gms.ads.ez.applovin;


import android.app.Activity;
import android.util.Log;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.google.android.gms.ads.ez.AdsFactory;
import com.google.android.gms.ads.ez.AdsFactory2;
//import com.google.android.gms.ads.ez.EzAdControl;
import com.google.android.gms.ads.ez.EzApplication;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.analytics.FirebaseAnalTool;

public class ApplovinUtils extends AdsFactory2 {

    public static ApplovinUtils INSTANCE;
    private final String TAG = "ApplovinUtils";


    public static ApplovinUtils getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new ApplovinUtils(context);
        }
        INSTANCE.mContext = context;
        return INSTANCE;
    }


    public void init() {
        LogUtils.logString(ApplovinUtils.class, "init");
        // Please make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(EzApplication.getInstance()).setMediationProvider("max");

        AppLovinSdk.initializeSdk(mContext.getApplicationContext(), new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                // AppLovin SDK is initialized, start loading ads
                LogUtils.logString(ApplovinUtils.class, "init Success");

                loadAds();
            }
        });
    }


    private MaxInterstitialAd interstitialAd;

    public ApplovinUtils(Activity mContext) {
        super(mContext);
    }

    @Override
    public boolean loadAdNetwork() {
        if (!AppLovinSdk.getInstance(mContext).isInitialized()) {
            LogUtils.logString(this, "LoadAdsNetwork Chua init");
            return false;
        }

        String id = AdUnit.getApplovinInterId();
        LogUtils.logString(this, "LoadAdsNetwork " + getNameAd() + " With Id " + id);

        if (id.equals("")) {
            setAdError();
            return false;
        }

        interstitialAd = new MaxInterstitialAd(id, mContext);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils Loaded");
                setAdLoaded();
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdDisplayed");
                setAdDisplay();

            }

            @Override
            public void onAdHidden(MaxAd ad) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdHidden");
                setAdClosed();
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdClicked");
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdLoadFailed " + error);
                setAdError();
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdDisplayFailed");
                setAdDisplayFailed(error.getMessage());
            }
        });

        interstitialAd.loadAd();

        return true;
    }

    @Override
    public String getNameAd() {
        return "Inter Applovin";
    }

    @Override
    public boolean showAds() {
        if (stateOption.isLoaded() && mContext != null && interstitialAd.isReady()) {

            interstitialAd.setRevenueListener(new MaxAdRevenueListener() {
                @Override
                public void onAdRevenuePaid(MaxAd ad) {
                    LogUtils.logString(ApplovinUtils.class, "ApplovinUtils2222 onAdRevenuePaid " + ad.getRevenuePrecision());
                    FirebaseAnalTool.getInstance(mContext).logApplovinAdImpresstion(ad);
                }
            });


            interstitialAd.showAd();
            stateOption.setShowAd();
            return true;
        }
        LogUtils.logString(ApplovinUtils.class, "Not Accept show ads");
        return false;
    }
}
