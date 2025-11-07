package com.google.android.gms.ads.ez.nativead;


import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.ez.EzApplication;
import com.google.android.gms.ads.ez.IAPUtils;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.R;
import com.google.android.gms.ads.ez.SharedPreferencesUtils;
import com.google.android.gms.ads.ez.Utils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.listenner.LoadAdCallback;
import com.google.android.gms.ads.ez.observer.MyObserver;
import com.google.android.gms.ads.ez.observer.MySubject;
import com.google.android.gms.ads.ez.remote.AppConfigs;
import com.google.android.gms.ads.ez.remote.RemoteKey;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

public class AdmobNativeLanguage extends RelativeLayout {

    public static final String NATIVE_SIZE = "native_size";
    protected Context mContext;
    private LoadAdCallback loadAdCallback;
    private NativeAd nativeAd;
    private NativeAdView adView;
    private String TAG = "AdmobNativeAdView";
    private long lastTimeLoadAds = 0;
    private OnTouchListener onTouch;

    private ShimmerFrameLayout container;

    private View shimmerView;
    private View rootView;
    private MyObserver mObserver = new MyObserver() {
        @Override
        public void update(String message) {
            if (message.equals(IAPUtils.KEY_PURCHASE_SUCCESS)) {
                LogUtils.logString(AdmobNativeAdView.class, "AdmobNativeAdView user purchase observer -> remove ads");
                setVisibility(GONE);
                if (loadAdCallback != null) {
                    loadAdCallback.onError();
                }
            }
        }
    };


    public AdmobNativeLanguage(Context context) {
        super(context);


        mContext = context;
        initViews();
    }

    public AdmobNativeLanguage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        initViews();
    }

    public void setLoadAdCallback(LoadAdCallback loadAdCallback) {
        this.loadAdCallback = loadAdCallback;
    }

    private void initViews() {


        MySubject.getInstance().attach(mObserver);
        if (IAPUtils.getInstance().isPremium()) {
            LogUtils.logString(AdmobNativeAdView.class, "AdmobNativeAdView user purchase init -> remove ads");
            return;
        }


        View view = LayoutInflater.from(mContext).inflate(R.layout.native_admod_language, this);

        loadAd();
    }

    public void loadAd() {

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);

        if (AppConfigs.getInt(RemoteKey.APP_UPDATE_VERSION) == Utils.getVersionCode(EzApplication.getInstance().getCurrentActivity())) {
            if (loadAdCallback != null) {
                loadAdCallback.onError();
            }
            setVisibility(GONE);
           return;
        }


        container = findViewById(R.id.shimmer_view_container);
        rootView = findViewById(R.id.root_view);
        shimmerView = findViewById(R.id.shimmerView);

        if (container != null) {
            shimmerView.setVisibility(VISIBLE);
            rootView.setVisibility(GONE);
            container.startShimmer();
        }


        int nativeSize = SharedPreferencesUtils.getTagInt(mContext, NATIVE_SIZE, 0);
        LogUtils.logString(AdmobNativeAdView.class, "Native Size " + nativeSize);
        if (nativeSize != 0) {
            DisplayMetrics outMetrics = mContext.getResources().getDisplayMetrics();
            if (outMetrics.heightPixels / nativeSize < 2) {
                LogUtils.logString(AdmobNativeAdView.class, "Not show height");
                setVisibility(GONE);
                return;
            }
        }

        lastTimeLoadAds = System.currentTimeMillis();
        LogUtils.logString(AdmobNativeAdView.class, "Ad Unit " + AdUnit.getAdmobNativeId());
        nativeAd = null;
        AdLoader.Builder builder = new AdLoader.Builder(mContext,AdUnit.getAdmobNativeId())
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nati) {
                        LogUtils.logString(AdmobNativeAdView.class, "Ad Loaded");


                        nativeAd = nati;

                        setAdToLayout();
                        if (loadAdCallback != null) {
                            loadAdCallback.onLoaded();
                        }
                    }
                });


        builder.withNativeAdOptions(new NativeAdOptions.Builder().setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build()).build());
        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                LogUtils.logString(AdmobNativeAdView.class, "Ad Load Fail");
                setVisibility(GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }


        }).build().loadAd(new AdRequest.Builder().build());
    }

    public void setAdToLayout() {
        adView = (NativeAdView) findViewById(R.id.ad_view);

//        int[] location = new int[2];
//        adView.getLocationOnScreen(location);
//        int y = location[1];
//
//        DisplayMetrics outMetrics = mContext.getResources().getDisplayMetrics();
//
//
//        Log.e(TAG, "setAdToLayout: "+outMetrics.heightPixels +"  " + y );
//        if (outMetrics.heightPixels / (outMetrics.heightPixels - y) < 2.5) {
//            LogUtils.logString(AdmobNativeAdView.class, "Not show height");
//            setVisibility(GONE);
//            return;
//        }
//        SharedPreferencesUtils.setTagInt(mContext, NATIVE_SIZE, outMetrics.heightPixels - y);


        if (container != null) {
            shimmerView.setVisibility(GONE);
            rootView.setVisibility(VISIBLE);
            container.stopShimmer();
            container.hideShimmer();
        }


        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (adView.getStoreView() != null) {
            if (nativeAd.getStore() == null) {
                adView.getStoreView().setVisibility(View.GONE);
            } else {
                adView.getStoreView().setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }
        }


        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }


        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.

//

//        if (adView.getMediaView().getVisibility() == VISIBLE) {
//            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
//            LayoutParams layoutParams = new LayoutParams(
//                    LayoutParams.MATCH_PARENT,
//                    (metrics.widthPixels * 2));
//            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//            adView.setLayoutParams(layoutParams);
//        }


        adView.setNativeAd(nativeAd);


    }
}