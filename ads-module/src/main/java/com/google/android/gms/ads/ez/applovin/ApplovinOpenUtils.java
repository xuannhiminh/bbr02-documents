package com.google.android.gms.ads.ez.applovin;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.ez.AdsFactory2;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.analytics.FirebaseAnalTool;

import java.util.Date;

public class ApplovinOpenUtils extends AdsFactory2 {
    private static ApplovinOpenUtils INSTANCE;

    public static ApplovinOpenUtils getInstance(Activity activity) {
        if (INSTANCE == null) {
            INSTANCE = new ApplovinOpenUtils(activity);
            return INSTANCE;
        }
        INSTANCE.mContext = activity;
        return INSTANCE;
    }

    private MaxAppOpenAd appOpenAd = null;


    public ApplovinOpenUtils(Activity mContext) {
        super(mContext);
    }


    @Override
    public boolean loadAdNetwork() {
        String id = AdUnit.getAdmobOpenId();
        LogUtils.logString(this, "LoadAdsNetwork " + getNameAd() + " With Id " + id);

        if (id.equals("")) {
            setAdError();
            return false;
        }

        appOpenAd = new MaxAppOpenAd("5c5250bb52b6252b", mContext);
        appOpenAd.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd maxAd) {

            }

            @Override
            public void onAdCollapsed(MaxAd maxAd) {

            }

            @Override
            public void onAdLoaded(MaxAd maxAd) {
                LogUtils.logString(ApplovinOpenUtils.class, "Open Applovin Loaded ");
                setAdLoaded();
            }

            @Override
            public void onAdDisplayed(MaxAd maxAd) {
                appOpenAd = null;
                setAdDisplay();
            }

            @Override
            public void onAdHidden(MaxAd maxAd) {
                appOpenAd = null;
                setAdClosed();
                loadAds();
            }

            @Override
            public void onAdClicked(MaxAd maxAd) {

            }

            @Override
            public void onAdLoadFailed(String s, MaxError maxError) {
                LogUtils.logString(ApplovinOpenUtils.class, "Admob Failed " + maxError.getMessage());
                appOpenAd = null;
                setAdError();
            }

            @Override
            public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                appOpenAd = null;
                setAdDisplayFailed(maxError.getMessage());
            }
        });
        appOpenAd.loadAd();


        return true;
    }


    @Override
    public String getNameAd() {
        return "Applovin Open";
    }

    @Override
    public boolean showAds() {
        if (appOpenAd == null || !AppLovinSdk.getInstance(mContext).isInitialized())
            return false;

        if (appOpenAd.isReady()) {
            appOpenAd.showAd();
            return true;
        } else {
            appOpenAd.loadAd();
            return false;
        }
    }


}
