package com.google.android.gms.ads.ez.applovin;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.applovin.sdk.AppLovinSdk;

public class ExampleAppOpenManager
        implements LifecycleObserver, MaxAdListener
{
    private  MaxAppOpenAd appOpenAd;
    private  Context context;

    public ExampleAppOpenManager( Context context)
    {
        ProcessLifecycleOwner.get().getLifecycle().addObserver( this );

        this.context = context;

        appOpenAd = new MaxAppOpenAd( "YOUR_AD_UNIT_ID", context);
        appOpenAd.setListener( this );
        appOpenAd.loadAd();
    }

    private void showAdIfReady()
    {
        if ( appOpenAd == null || !AppLovinSdk.getInstance( context ).isInitialized() ) return;

        if ( appOpenAd.isReady() )
        {
            appOpenAd.showAd( "YOUR_TEST_PLACEMENT_HERE" );
        }
        else
        {
            appOpenAd.loadAd();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart()
    {
        showAdIfReady();
    }

    @Override
    public void onAdLoaded(final MaxAd ad) {}
    @Override
    public void onAdLoadFailed(final String adUnitId, final MaxError error) {}
    @Override
    public void onAdDisplayed(final MaxAd ad) {}
    @Override
    public void onAdClicked(final MaxAd ad) {}

    @Override
    public void onAdHidden(final MaxAd ad)
    {
        appOpenAd.loadAd();
    }

    @Override
    public void onAdDisplayFailed(final MaxAd ad, final MaxError error)
    {
        appOpenAd.loadAd();
    }
}