package com.google.android.gms.ads.ez;

import android.app.Activity;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.listenner.ShowAdCallback;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class FacebookUtils extends AdsFactory2 {
    public static FacebookUtils INSTANCE;
    private static final String TAG_LAST_SHOW_FB = "last_show_fb";

    public static FacebookUtils getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new FacebookUtils(context);
        }
        INSTANCE.mContext = context;
        return INSTANCE;
    }

    public void init() {
        AdSettings.addTestDevice("1685560b-98a1-4cbb-863d-1290f86e110d");
//        AdSettings.addTestDevice("1685560b-98a1-4cbb-863d-1290f86e110d");
//        AdSettings.addTestDevice("3be1f8c6-9083-4853-9ab7-3c93a5a6371f");
        AudienceNetworkAds.initialize(mContext);
    }

    private InterstitialAd fbInterstitialAd;

    public FacebookUtils(Activity mContext) {
        super(mContext);
    }

    @Override
    public boolean loadAdNetwork() {
//        AdSettings.addTestDevice("eef23637-87bf-4465-ac7c-f6b3f30347fd");
        String id = AdUnit.getFacebookInterId();
        LogUtils.logString(this, "LoadAdsNetwork " + getNameAd() + " With Id " + id);
        if (id.equals("")) {
            setAdError();
            return false;
        }
        fbInterstitialAd = new InterstitialAd(mContext, id);

        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                SharedPreferencesUtils.setTagLong(mContext, TAG_LAST_SHOW_FB, System.currentTimeMillis());
                LogUtils.logString(FacebookUtils.class, "Facebook Impression");
                fbInterstitialAd = null;
                setAdDisplay();
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                LogUtils.logString(FacebookUtils.class, "Facebook Closed");
                fbInterstitialAd = null;
                setAdClosed();
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                LogUtils.logString(FacebookUtils.class, "Facebook Failed " + adError.getErrorMessage());
                fbInterstitialAd = null;
                setAdError();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                LogUtils.logString(FacebookUtils.class, "Facebook Loaded");
                setAdLoaded();
            }

            @Override
            public void onAdClicked(Ad ad) {
                LogUtils.logString(FacebookUtils.class, "Facebook Ad Click");
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };

        fbInterstitialAd.loadAd(
                fbInterstitialAd.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build());

        return true;
    }

    @Override
    public String getNameAd() {
        return "Facebook Inter";
    }

    @Override
    public boolean showAds() {
        if (System.currentTimeMillis() - SharedPreferencesUtils.getTagLong(mContext, TAG_LAST_SHOW_FB) < 60000) {
            return false;
        }
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
            return true;
        }
        return false;
    }


}

