package com.google.android.gms.ads.ez.analytics;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.applovin.mediation.MaxAd;
import com.google.android.gms.ads.ez.EzApplication;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.SharedPreferencesUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalTool {
    private static FirebaseAnalTool mFirebaseAnalTool;
    private FirebaseAnalytics mFirebaseAnalytics;
    public static final String SPACE = " <-> ";
    private final String KEY_SCREEN = "SCREEN";
    private final String KEY_EVENT_NAME = "EVENT_NAME";

    public static class Param {
        public static final String OPEN_APP = "Open app";
        public static final String OPEN_SCREEN = "Open screen";
        public static final String APP_OPEN_WITH_MAIN = "Open app by main";
        public static final String APP_OPEN_WITH_FILE = "Open app by file";
        public static final String CLICK_BUTTON = "Click to button";
        public static final String ADS_LOAD_ERROR = "Ads load error";
        public static final String ADS_LOAD_SUCCESS = "Ads load success";

        public static final String AD_DISPLAY = "Ad display";
        public static final String AD_CLICK = "Ad Click";
    }

    public static class Event {
        public static final String ACTION_IN_APP = "action_in_app";
        public static final String ACTION_PURCHASE = "action_purchase";
        public static final String ACTION_SHOW_ADS = "action_show_ads";
    }

    public static class Screen {
        public static final String SPLASH_ACTIVITY = "SplashAct";
        public static final String UNKNOW_ACTIVITY = "UnknowAct";
        public static final String MAIN_ACTIVITY = "MainAct";
        public static final String PDF_READER_ACTIVITY = "PDFReaderAct";
    }

    private static Context mContext;

    public static FirebaseAnalTool getInstance(Context context) {
        if (mFirebaseAnalTool == null || (mContext != null && !mContext.equals(context))) {
            mContext = context;
            mFirebaseAnalTool = new FirebaseAnalTool(context);
        }
        return mFirebaseAnalTool;
    }

    public FirebaseAnalTool(Context context) {
        if (context != null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
    }

    public void trackEvent(String eventNames, String screen, String param) {
        Log.e("TAG", "trackEvent: " + screen + "  " + param);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screen);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, param);
        bundle.putString(FirebaseAnalytics.Param.CONTENT, screen + SPACE + param);
        LogUtils.logString(FirebaseAnalTool.class, eventNames + "  " + screen + "  " + param);
        if (mFirebaseAnalytics != null)
            mFirebaseAnalytics.logEvent(eventNames, bundle);
    }

    public void trackEventAds(String eventNames) {
        LogUtils.logString(FirebaseAnalTool.class, eventNames);
        if (mFirebaseAnalytics != null)
            mFirebaseAnalytics.logEvent("Ads_" + eventNames, new Bundle());
    }

    public void trackEvent(String eventNames, String param) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, param);
        bundle.putString(FirebaseAnalytics.Param.CONTENT, param);
        LogUtils.logString(FirebaseAnalTool.class, eventNames + "  " + param);
        if (mFirebaseAnalytics != null)
            mFirebaseAnalytics.logEvent(eventNames, bundle);
    }

    public void trackReferrer(String eventName, String referrer) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT, referrer);

        params.putString(FirebaseAnalytics.Param.ITEM_ID, "ITEM_ID");
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, "ITEM_NAME");
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CONTENT_TYPE");
        params.putString("Tesstt", "Tesstt");


        mFirebaseAnalytics.logEvent(eventName, params);
    }


    public void logApplovinAdImpresstion(MaxAd impressionData) {


        double revenue = impressionData.getRevenue(); // In USD

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.AD_PLATFORM, "appLovin");
        params.putString(FirebaseAnalytics.Param.AD_SOURCE, impressionData.getNetworkName());
        params.putString(FirebaseAnalytics.Param.AD_FORMAT, impressionData.getFormat().getLabel());
        params.putString(FirebaseAnalytics.Param.AD_UNIT_NAME, impressionData.getAdUnitId());
        params.putDouble(FirebaseAnalytics.Param.VALUE, revenue);
        params.putString(FirebaseAnalytics.Param.CURRENCY, "USD"); // All Applovin revenue is sent in USD
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params);


    }

    public void loadDailyAdsRevenue(double adValue, String format) {
        LogUtils.logString(FirebaseAnalTool.class, "Value " + String.valueOf(adValue) + "   " + adValue * 1000000 + "  Format " + format);
        Context context = EzApplication.getInstance().getCurrentActivity();
        double currentImpressionRevenue = adValue; //LTV pingback provides value in micros, so if you are using that directly, make sure to divide by 10^6
        float previousTroasCache = SharedPreferencesUtils.getTagFloat(context, "TroasCache", 0); //Use App Local storage to store cache of tROAS
        float currentTroasCache = (float) (previousTroasCache + currentImpressionRevenue);
        if (currentTroasCache >= 0.001) {
            LogTroasFirebaseAdRevenueEvent(currentTroasCache, format, "Admob", "Admob");
            SharedPreferencesUtils.setTagFloat(context, "TroasCache", 0);
        } else {
            SharedPreferencesUtils.setTagFloat(context, "TroasCache", currentTroasCache);
        }
    }

    private void LogTroasFirebaseAdRevenueEvent(double TroasCache, String format, String platform, String source) {
        LogUtils.logString(FirebaseAnalTool.class, "Value " + String.valueOf(TroasCache) + "  Format " + format);
        Bundle bundle = new Bundle();
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, TroasCache);//(Required)tROAS event must include Double Value
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");//put in the correct currency
        bundle.putString(FirebaseAnalytics.Param.AD_FORMAT, format);
        bundle.putString(FirebaseAnalytics.Param.AD_PLATFORM, platform);
        bundle.putString(FirebaseAnalytics.Param.AD_SOURCE, source);
        if (mFirebaseAnalytics != null)
            mFirebaseAnalytics.logEvent("ad_impression_ez", bundle);
    }
}
