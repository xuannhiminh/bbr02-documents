package com.google.android.gms.ads.ez.unity;


import android.app.Activity;
import android.util.Log;

import com.google.android.gms.ads.ez.AdsFactory;
//import com.google.android.gms.ads.ez.EzAdControl;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.core.configuration.IInitializationListener;
import com.google.android.gms.ads.ez.AdsFactory;
//import com.google.android.gms.ads.ez.EzAdControl;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;


public class UnityUtils extends AdsFactory {
    public static UnityUtils INSTANCE;

    public static UnityUtils getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new UnityUtils(context);
        }
        INSTANCE.mContext = context;
        return INSTANCE;
    }


    public UnityUtils(Activity mContext) {
        super(mContext);
    }

    public void init() {


        UnityAds.initialize(mContext, "AdUnit.getUnityAppId()", false, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                LogUtils.logString(UnityUtils.class, "Unity Init Success");
                loadAds();
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                LogUtils.logString(UnityUtils.class, "Unity Init Faild " + message);
            }
        });
    }

    @Override
    public void loadAds() {
        LogUtils.logString(this, "Load Unity");
        String id = AdUnit.getAdxInterId();
        LogUtils.logString(this, "Load Unity With Id " + "AdUnit.getUnityAppId()");


        if (stateOption.isLoading()) {
            // neu dang loading  thi k load nua
        } else if (stateOption.isLoaded()) {
            // neu da loaded thi goi callback luon
            if (mListener != null) {
                mListener.onLoaded();
            }
        } else {
            // neu k loading cung k loaded thi goi ham load ads va dat loading = true

            LogUtils.logString(UnityUtils.class, "Load Unity: Start Loading ");

            UnityAds.load("AdUnit.getUnityAdUnitId()", new IUnityAdsLoadListener() {
                @Override
                public void onUnityAdsAdLoaded(String placementId) {
                    LogUtils.logString(UnityUtils.class, "Unity loaded");
                    stateOption.setOnLoaded();
                    if (mListener != null) {
                        mListener.onLoaded();
                    }
                }

                @Override
                public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                    LogUtils.logString(UnityUtils.class, "Unity Failed " + message + "  " + error);
                    stateOption.setOnFailed();
                    if (mListener != null) {
                        mListener.onError();
                    }
                }
            });


            stateOption.setOnLoading();
        }

    }

    @Override
    public boolean showAds() {
        if (stateOption.isLoaded() && mContext != null) {

            UnityAds.show(mContext, "AdUnit.getUnityAdUnitId()", new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                @Override
                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                    Log.e("UnityUtils", "Unity Ads failed to show ad for " + placementId + " with error: [" + error + "] " + message);
                    LogUtils.logString(UnityUtils.class, "Unity onDisplayFaild");
                    if (mListener != null) {
                        mListener.onDisplayFaild();
                    }
                }

                @Override
                public void onUnityAdsShowStart(String placementId) {
                    stateOption.setShowAd();
                    Log.v("UnityUtils", "onUnityAdsShowStart: " + placementId);
                    LogUtils.logString(UnityUtils.class, "Unity Impression");
                    if (mListener != null) {
                        mListener.onDisplay();
                    }
                }

                @Override
                public void onUnityAdsShowClick(String placementId) {

                }

                @Override
                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                    stateOption.setDismisAd();
                    LogUtils.logString(UnityUtils.class, "Unity Closed");
                    Log.v("UnityUtils", "onUnityAdsShowComplete: " + placementId);
                    if (mListener != null) {
                        mListener.onClosed();
                    }
                    LogUtils.logString(UnityUtils.class, "Call Reload EzAd");
                }
            });


            return true;
        }
        return false;
    }


}
