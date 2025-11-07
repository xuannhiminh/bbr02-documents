package com.google.android.gms.ads.ez.nativead;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.ez.R;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.listenner.LoadAdCallback;

import java.util.ArrayList;
import java.util.List;

public class FacebookNativeAds extends RelativeLayout {
    private final String TAG = "NativeAdmContext".getClass().getSimpleName();
    private NativeAdLayout nativeAdLayout;
    private NativeAd nativeAd;
    private Context mContext;

    private LoadAdCallback loadAdCallback;

    public FacebookNativeAds(Context context) {
        super(context);
        mContext = context;
        loadNativeAd();
    }

    public FacebookNativeAds(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        loadNativeAd();
    }

    public LoadAdCallback getLoadAdCallback() {
        return loadAdCallback;
    }

    public void setLoadAdCallback(LoadAdCallback loadAdCallback) {
        this.loadAdCallback = loadAdCallback;
    }

    private void loadNativeAd() {
        // Instantiate a NativeAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        nativeAd = new NativeAd(mContext, AdUnit.getFacebookNativeId());

        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());

                if (loadAdCallback != null) {
                    loadAdCallback.onError();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }

                if (loadAdCallback != null) {
                    loadAdCallback.onLoaded();
                }
                // Inflate Native Ad into Container
                inflateAd(nativeAd);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        };

        // Request an ad
        nativeAd.loadAd(
                nativeAd.buildLoadAdConfig()
                        .withAdListener(nativeAdListener)
                        .build());
    }

    @SuppressLint("MissingInflatedId")
    private void inflateAd(NativeAd nativeAd) {

        nativeAd.unregisterView();
        View view = LayoutInflater.from(mContext).inflate(R.layout.native_facebook_item, this);
        // Add the Ad view into the ad container.
        nativeAdLayout = view.findViewById(R.id.native_ad_container);

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(mContext, nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        MediaView nativeAdIcon = view.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = view.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = view.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = view.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = view.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = view.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = view.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                view, nativeAdMedia, nativeAdIcon, clickableViews);
    }
}
