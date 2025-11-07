package com.google.android.gms.ads.ez;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.ez.adparam.AdUnit;

public class AdxUtils extends AdsFactory2 {

    public static AdxUtils INSTANCE;

    public static AdxUtils getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new AdxUtils(context);
        }
        INSTANCE.mContext = context;
        return INSTANCE;
    }


    public AdxUtils(Activity mContext) {
        super(mContext);
    }

    private AdManagerInterstitialAd mInterstitialAd;

    @Override
    public boolean loadAdNetwork() {

        String id = AdUnit.getAdxInterId();
        LogUtils.logString(this, "LoadAdsNetwork " + getNameAd() + " With Id " + id);

        if (id.equals("")) {
            setAdError();
            return false;
        }


        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();

        AdManagerInterstitialAdLoadCallback adLoadCallback = new AdManagerInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                LogUtils.logString(AdxUtils.class, "Adx Loaded");
                mInterstitialAd = interstitialAd;
                setAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                LogUtils.logString(AdxUtils.class, "Adx Failed " + loadAdError.getMessage() + "  ");
                setAdError();

            }
        };

        AdManagerInterstitialAd.load(
                mContext,
                AdUnit.getAdxInterId(),
                adRequest, adLoadCallback
        );
        return true;
    }


    @Override
    public String getNameAd() {
        return "Adx Inter";
    }

    @Override
    public boolean showAds() {
        if (mInterstitialAd != null && mContext != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    LogUtils.logString(AdxUtils.class, "Adx Closed");
                    mInterstitialAd = null;
                    setAdClosed();


                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when fullscreen content failed to show.
                    LogUtils.logString(AdxUtils.class, "Adx Display Fail");
                    mInterstitialAd = null;
                    setAdDisplayFailed(adError.getMessage());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    LogUtils.logString(AdxUtils.class, "Adx Display Success");
                    mInterstitialAd = null;
                    setAdDisplay();
                }
            });
            mInterstitialAd.show(mContext);
            stateOption.setShowAd();
            return true;
        }
        LogUtils.logString(AdxUtils.class, "Not Accept show ads " + (mInterstitialAd != null) +"   " + (mContext != null));
        return false;
    }
}
