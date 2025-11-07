package com.google.android.gms.ads.ez.banner;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdView;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.ez.EzApplication;
import com.google.android.gms.ads.ez.IAPUtils;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.R;
import com.google.android.gms.ads.ez.SharedPreferencesUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.analytics.FirebaseAnalTool;
import com.google.android.gms.ads.ez.observer.MyObserver;
import com.google.android.gms.ads.ez.observer.MySubject;
import com.google.android.gms.ads.ez.utils.StateOption;

public class BannerAd extends RelativeLayout {
    private final String TAG = "BannerAd";
    private AdView fbBanner;
    private com.google.android.gms.ads.AdView admobBanner;
    private Context mContext;
    private StateOption stateOption;
    private AdManagerAdView adxBanner;
    private MaxAdView applovinBanner;
    private MyObserver mObserver = new MyObserver() {
        @Override
        public void update(String message) {
            if (message.equals(IAPUtils.KEY_PURCHASE_SUCCESS)) {
                LogUtils.logString(IAPUtils.class, "BannerAd user purchase observer -> remove ads");
                setVisibility(INVISIBLE);
            }
        }
    };


    public BannerAd(Context context) {
        super(context);

        stateOption = new StateOption();

        mContext = context;
        if (!stateOption.isLoading()) {
            initViews();
        }
    }

    public BannerAd(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        stateOption = new StateOption();

        mContext = context;
        initViews();
    }

    public static void setAllowShowBanner(String activity, boolean allow) {
        Log.e("BannerAd", "setAllowShowBanner: " + activity + "_banner");
        SharedPreferencesUtils.setTagBoolean(EzApplication.getInstance().getCurrentActivity(), activity + "_banner", allow);
    }

    private boolean isAllowShowBanner() {
        Log.e("BannerAd", "isAllowShowBanner: " + EzApplication.getInstance().getCurrentActivity().getClass().getSimpleName() + "_banner");
        return SharedPreferencesUtils.getTagBoolean(mContext, mContext .getClass().getSimpleName() + "_banner", true);
    }

    public void initViews() {
        Log.e(TAG, "initViews: " + isAllowShowBanner());

        if (!isAllowShowBanner()) {
            Log.e(TAG, "Khong cho show ");
            return;
        }
        if (stateOption.isLoading()) {
            Log.e(TAG, "Dang loading ");
            return;
        }


        MySubject.getInstance().attach(mObserver);
        if (IAPUtils.getInstance().isPremium()) {
            LogUtils.logString(IAPUtils.class, "BannerAd user purchase init -> remove ads");
            setVisibility(INVISIBLE);
            return;
        }


        loadAdmob();
//        getViewTreeObserver().addOnGlobalLayoutListener(
//                new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        if (!initialLayoutComplete) {
//                            initialLayoutComplete = true;
//                            loadAdmob();
//                        }
//                    }
//                });


//        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.gravity = Gravity.CENTER;
//        setLayoutParams(layoutParams);
    }

    private void loadFacebookBanner() {


        fbBanner = new AdView(mContext, AdUnit.getFacebookBannerId(), com.facebook.ads.AdSize.BANNER_HEIGHT_50);


        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                setVisibility(INVISIBLE);
                stateOption.setOnFailed();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                removeAllViews();
                addView(fbBanner);
            }

            @Override
            public void onAdClicked(Ad ad) {
                EzApplication.getInstance().setSkipNextAds();
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };

        fbBanner.loadAd(fbBanner.buildLoadAdConfig().withAdListener(adListener).build());
    }

    private boolean initialLayoutComplete = false;

    private void loadAdmob() {

        if (admobBanner != null) {
            admobBanner.destroy();
        }
        admobBanner = new com.google.android.gms.ads.AdView(mContext);
        admobBanner.setAdUnitId(AdUnit.getAdmobBannerId());
        admobBanner.setAdSize(getAdSize());

        Bundle extras = new Bundle();
        extras.putString("collapsible", "bottom");
//        extras.putString("collapsible_request_id", UUID.randomUUID().toString());
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();


        admobBanner.loadAd(adRequest);
        stateOption.setOnLoading();
        admobBanner.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.e(TAG, "onAdFailedToLoad: ");
                loadAdx();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.e(TAG, "onAdLoaded: ");
                stateOption.setOnLoaded();
                admobBanner.setOnPaidEventListener(new OnPaidEventListener() {
                    @Override
                    public void onPaidEvent(AdValue adValue) {
                        FirebaseAnalTool.getInstance(mContext).loadDailyAdsRevenue(adValue.getValueMicros() / 1000000f, "Banner");
                    }
                });


                removeAllViews();
                setVisibility(VISIBLE);
                addView(admobBanner);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();

            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                EzApplication.getInstance().setSkipNextAds();
            }
        });


    }


    private void loadAdx() {

        adxBanner = new AdManagerAdView(mContext);

        adxBanner.setAdSizes(getAdSize());

        adxBanner.setAdUnitId(AdUnit.getAdxBannerId());


        Bundle extras = new Bundle();
        extras.putString("collapsible", "bottom");


        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();

        adxBanner.loadAd(adRequest);

        adxBanner.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                Log.e(TAG, "Adx onAdLoaded: ");
                removeAllViews();
                addView(adxBanner);
                stateOption.setOnLoaded();
                setVisibility(View.VISIBLE);
            }


            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.e(TAG, "Adx onAdFailedToLoad: ");
                loadFacebookBanner();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }


            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();

                EzApplication.getInstance().setSkipNextAds();
            }
        });

    }

    private void loadApplovin() {
        if (AdUnit.getApplovinBannerId().equals("")) {
            setVisibility(INVISIBLE);
            return;
        }
        applovinBanner = new MaxAdView(AdUnit.getApplovinBannerId(), (Activity) mContext);
        applovinBanner.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {

            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.e(TAG, "onAdLoaded: applovin");
                removeAllViews();
                addView(applovinBanner);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: applovin " + error);
                setVisibility(INVISIBLE);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });

        // Stretch to the width of the screen for banners to be fully functional
        int width = ViewGroup.LayoutParams.MATCH_PARENT;

        // Banner height on phones and tablets is 50 and 90, respectively
        int heightPx = getResources().getDimensionPixelSize(R.dimen._45sdp);

        applovinBanner.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));

        // Set background or background color for banners to be fully functional
        applovinBanner.setBackgroundColor(Color.RED);


        // Load the ad
        applovinBanner.loadAd();

    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        try {
//            Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = mContext.getResources().getDisplayMetrics();
//            display.getMetrics(outMetrics);

            float widthPixels = outMetrics.widthPixels;
            float density = outMetrics.density;

            int adWidth = (int) (widthPixels / density);
            Log.e(TAG, "getAdSize: " + adWidth + "  " + outMetrics.widthPixels);
            // Step 3 - Get adaptive ad size and return for setting on the ad view.
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mContext, adWidth);
        } catch (Exception e) {
            Log.e(TAG, "getAdSize: ", e);
            e.printStackTrace();
        }
        return AdSize.BANNER;
    }

}