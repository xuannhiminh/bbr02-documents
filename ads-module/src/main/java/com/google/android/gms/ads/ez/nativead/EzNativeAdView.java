package com.google.android.gms.ads.ez.nativead;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.R;
import com.google.android.gms.ads.ez.listenner.LoadAdCallback;

public class EzNativeAdView extends RelativeLayout {
    private Context mContext;
    private LoadAdCallback loadAdCallback;

    private ShimmerFrameLayout container;

    private View shimmerView;


    public EzNativeAdView(Context context) {
        super(context);

        mContext = context;
        initViews();
        loadAds();
    }

    public EzNativeAdView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        initViews();

        loadAds();
    }

    private void loadAds() {
        LogUtils.logString(EzNativeAdView.class, "Load ad");
        FacebookNativeAds facebookNativeAds = new FacebookNativeAds(mContext);
        facebookNativeAds.setLoadAdCallback(new LoadAdCallback() {
            @Override
            public void onError() {
                LogUtils.logString(EzNativeAdView.class, "Fb Error");
                AdmobNativeNew admobNativeNew = new AdmobNativeNew(mContext);
                admobNativeNew.setLoadAdCallback(new LoadAdCallback() {
                    @Override
                    public void onError() {
                        LogUtils.logString(EzNativeAdView.class, "Admob Error");
                        if (loadAdCallback != null) {
                            loadAdCallback.onError();
                        }
                        setVisibility(GONE);
                        if (container != null) {
                            shimmerView.setVisibility(GONE);
                            container.stopShimmer();
                            container.hideShimmer();
                        }
                    }

                    @Override
                    public void onLoaded() {
                        LogUtils.logString(EzNativeAdView.class, "Admob Loaded");
                        if (loadAdCallback != null) {
                            loadAdCallback.onLoaded();
                        }
                        setVisibility(VISIBLE);
                        addView(admobNativeNew);
                        if (container != null) {
                            shimmerView.setVisibility(GONE);
                            container.stopShimmer();
                            container.hideShimmer();
                        }
                    }
                });
            }

            @Override
            public void onLoaded() {
                LogUtils.logString(EzNativeAdView.class, "Facebook Loaded");
                if (loadAdCallback != null) {
                    loadAdCallback.onLoaded();
                }
                setVisibility(VISIBLE);
                addView(facebookNativeAds);
                if (container != null) {
                    shimmerView.setVisibility(GONE);
                    container.stopShimmer();
                    container.hideShimmer();
                }
            }
        });
    }

    public void setLoadAdCallback(LoadAdCallback loadAdCallback) {
        this.loadAdCallback = loadAdCallback;
    }

    private void initViews() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.native_ez_ad, this);

        container = findViewById(R.id.shimmer_view_container);
        shimmerView = findViewById(R.id.shimmerView);

        if (container != null) {
            shimmerView.setVisibility(VISIBLE);
            container.startShimmer();
        }


    }
}
