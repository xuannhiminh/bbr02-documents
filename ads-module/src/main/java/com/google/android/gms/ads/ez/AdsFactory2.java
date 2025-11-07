package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.os.CountDownTimer;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.ez.admob.AdmobOpenUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.listenner.LoadAdCallback;
import com.google.android.gms.ads.ez.listenner.ShowAdCallback;
import com.google.android.gms.ads.ez.utils.StateOption;


public abstract class AdsFactory2 {
    protected Activity mContext;
    protected AdFactoryListener mListener;
    protected StateOption stateOption;


    protected LoadAdCallback loadAdCallback;
    protected ShowAdCallback showAdCallback;

    public AdsFactory2(Activity mContext) {
        this.mContext = mContext;
        stateOption = new StateOption();
    }


    public AdsFactory2 setLoadAdCallback(LoadAdCallback loadAdCallback) {
        this.loadAdCallback = loadAdCallback;
        return this;
    }

    public AdsFactory2 setShowAdCallback(ShowAdCallback showAdCallback) {
        this.showAdCallback = showAdCallback;
        return this;
    }

    public void loadAds() {
        LogUtils.logString(this.getClass(), "Load " + getNameAd());
        if (!stateOption.isLoading()) {
            if (stateOption.isLoaded()) {

                LogUtils.logString(this.getClass(), "Load " + getNameAd() + ": da loaded -> tra ve onloaded luon");
                new CountDownTimer(200, 200) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        setAdLoaded();
                    }
                }.start();

            } else {


                LogUtils.logString(this.getClass(), "Load " + getNameAd() + ": chua loaded -> start loading");
                // start load ad

                if (loadAdNetwork()) {
                    stateOption.setOnLoading();
                }
            }
        } else {
            LogUtils.logString(this.getClass(), "Load " + getNameAd() + ": dang loading -> k load nua");
        }
    }

    public abstract boolean loadAdNetwork();


    public abstract String getNameAd();

    public abstract boolean showAds();

    public boolean isLoading() {
        return stateOption.isLoading();
    }

    public boolean isLoaded() {
        return stateOption.isLoaded();
    }

    protected void setAdLoaded() {
        stateOption.setOnLoaded();
        if (loadAdCallback != null) {
            loadAdCallback.onLoaded();
        }
        loadAdCallback = null;
    }

    protected void setAdError() {
        stateOption.setOnFailed();
        if (loadAdCallback != null) {
            loadAdCallback.onError();
        }
        loadAdCallback = null;
    }

    // show ad thi k set null vi con phai nghe dismis
    protected void setAdDisplay() {
        LogUtils.logString(this.getClass(), "Show " + getNameAd() + " Success ");
        stateOption.setShowAd();
        if (showAdCallback != null) {
            showAdCallback.onDisplay();
        }
    }

    protected void setAdDisplayFailed(String error) {
        LogUtils.logString(this.getClass(), "Show " + getNameAd() + " failed mess: " + error);
        if (showAdCallback != null) {
            showAdCallback.onDisplayFaild();
        }
        showAdCallback = null;
    }

    protected void setAdClosed() {
        LogUtils.logString(this.getClass(), getNameAd() + " closed");
        stateOption.setDismisAd();
        if (showAdCallback != null) {
            showAdCallback.onClosed();
        }
        showAdCallback = null;
    }

    protected void setAdClick() {
        LogUtils.logString(this.getClass(), getNameAd() + " click");
        if (showAdCallback != null) {
            showAdCallback.onClickAd();
        }
    }
}
