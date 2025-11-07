package com.google.android.gms.ads.ez.admob;

import android.app.Activity;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.ez.AdsFactory;
import com.google.android.gms.ads.ez.AdsFactory2;
//import com.google.android.gms.ads.ez.EzAdControl;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.Utils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.analytics.FirebaseAnalTool;
import com.google.android.gms.ads.ez.listenner.LoadAdCallback;
import com.google.android.gms.ads.ez.listenner.ShowAdCallback;
import com.google.android.gms.ads.ez.utils.StateOption;

import java.util.Date;

public class AdmobOpenUtils extends AdsFactory2 {
    private static AdmobOpenUtils INSTANCE;

    public static AdmobOpenUtils getInstance(Activity activity) {
        if (INSTANCE == null) {
            INSTANCE = new AdmobOpenUtils(activity);
            return INSTANCE;
        }
        INSTANCE.mContext = activity;
        return INSTANCE;
    }

    private AppOpenAd admobOpenAd = null;
    private long loadTime = 0;


    public AdmobOpenUtils(Activity mContext) {
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

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                mContext, AdUnit.getAdmobOpenId(), request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        super.onAdLoaded(appOpenAd);

                        LogUtils.logString(AdmobOpenUtils.class, "Admob Loaded ");
                        admobOpenAd = appOpenAd;
                        loadTime = (new Date()).getTime();
                        setAdLoaded();

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        LogUtils.logString(AdmobOpenUtils.class, "Admob Failed " + loadAdError);
                        admobOpenAd = null;

                        setAdError();

                    }
                });
        return true;
    }


    @Override
    public String getNameAd() {
        return "Admob Open";
    }

    @Override
    public boolean showAds() {
        LogUtils.logString(AdmobOpenUtils.class, "Show Admob Open Ads " + admobOpenAd + "  " + wasLoadTimeLessThanNHoursAgo(4));
        if (admobOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)) {
            LogUtils.logString(AdmobOpenUtils.class, "Accept show ads");


            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            admobOpenAd = null;
                            setAdClosed();
                            loadAds();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            admobOpenAd = null;
                            setAdDisplayFailed(adError.getMessage());
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            admobOpenAd.setOnPaidEventListener(new OnPaidEventListener() {
                                @Override
                                public void onPaidEvent(AdValue adValue) {
                                    FirebaseAnalTool.getInstance(mContext).loadDailyAdsRevenue(adValue.getValueMicros() / 1000000f, "Open");
                                }
                            });
                            admobOpenAd = null;
                            setAdDisplay();

                        }
                    };

            admobOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            admobOpenAd.show(mContext);
            return true;
        }
        LogUtils.logString(AdmobOpenUtils.class, "Not Accept show ads");
        return false;
    }


    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }


}
