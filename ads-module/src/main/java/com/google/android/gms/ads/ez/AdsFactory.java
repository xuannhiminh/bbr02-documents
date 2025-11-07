package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.ads.ez.listenner.LoadAdCallback;
import com.google.android.gms.ads.ez.listenner.ShowAdCallback;
import com.google.android.gms.ads.ez.utils.StateOption;


public abstract class AdsFactory {
    protected Activity mContext;
    protected AdFactoryListener mListener;
    protected StateOption stateOption ;


    protected LoadAdCallback loadAdCallback;
    protected ShowAdCallback showAdCallback;

    public AdsFactory(Activity mContext) {
        this.mContext = mContext;
        stateOption = new StateOption();
    }

    public AdsFactory setListener(AdFactoryListener mListener) {
        this.mListener = mListener;
        return this;
    }

    public void setLoadAdCallback(LoadAdCallback loadAdCallback) {
        this.loadAdCallback = loadAdCallback;
    }

    public void setShowAdCallback(ShowAdCallback showAdCallback) {
        this.showAdCallback = showAdCallback;
    }

    public abstract void loadAds();

    public abstract boolean showAds();

    public boolean isLoading() {
        return stateOption.isLoading();
    }

    public boolean isLoaded() {
        return stateOption.isLoaded();
    }
}
