package com.google.android.gms.ads.ez;


import static androidx.lifecycle.Lifecycle.Event.ON_START;


import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDexApplication;

import com.facebook.ads.AdSettings;
import com.google.android.gms.ads.ez.admob.AdmobOpenUtils;
import com.google.android.gms.ads.ez.banner.BannerAd;
import com.google.android.gms.ads.ez.consent.ConsentUtils;
import com.google.android.gms.ads.ez.remote.AppConfigs;
import com.google.android.gms.ads.ez.remote.RemoteKey;

public class EzApplication extends MultiDexApplication implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static EzApplication INSTANCE;

    public static EzApplication getInstance() {
        return INSTANCE;
    }

    private Activity currentActivity, oldActivity;
    private boolean isDestroy = false;
    private boolean isSkipNextAds = false;

    private boolean isPause = false;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        AdSettings.addTestDevice("f92cf18d-fd97-459e-8dbb-24954d726d82");
//        AdSettings.addTestDevice("6c01a236-be2c-4d7f-9f26-7a0d0d378c43");
        registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        AppConfigs.getInstance(this);
        InstallReferrerReceiver.receiverInstall(this);
        IAPUtils.getInstance().init(this);

    }

    public void getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(currentActivity,
                        new String[]{"android.permission.POST_NOTIFICATIONS"},
                        1228);
            }
        } catch (Exception e) {

        }
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public Activity getOldActivity() {
        return oldActivity;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (SharedPreferencesUtils.getTagLong(activity, SharedPreferencesUtils.FIRST_OPEN) == 0) {
            SharedPreferencesUtils.setTagLong(activity, SharedPreferencesUtils.FIRST_OPEN, System.currentTimeMillis());
        }

        if (currentActivity == null) {
            currentActivity = activity;
            oldActivity = activity;
//            LogUtils.logString(EzApplication.class, "Consent " + ConsentUtils.getInstance(activity).canRequestAds());

        }
        Log.e("TAG", "gatherConsent: " + EzAdControl.getInstance(activity).isAdInitialized() + "  " + activity.getClass() + "   " + currentActivity.getClass());
        if (!EzAdControl.getInstance(activity).isAdInitialized()) {
            EzAdControl.initAd(activity);
        }
        getNotificationPermission();
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        LogUtils.logString(EzApplication.class, "ON_START " + Utils.getTopActivity(currentActivity)+"   "
                +Utils.checkTopActivityIsAd(currentActivity) + "  "
                + AdChecker.getInstance(currentActivity).checkShowAds() + "   " + isDestroy);
        isPause = true;
        if (isSkipNextAds) {
            isSkipNextAds = false;
            LogUtils.logString(EzApplication.class, "Skip next ads");
        } else {
            if (!Utils.checkTopActivityIsAd(currentActivity) && AdChecker.getInstance(currentActivity).checkShowAds() ) {
                LogUtils.logString(EzApplication.class, "Show open ads");
                if (AdmobOpenUtils.getInstance(currentActivity).showAds()) {
                    AdChecker.getInstance(currentActivity).setShowAds();
                }

            }
        }
        isDestroy = false;

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        LogUtils.logString(EzApplication.class, "onActivityStarted " + activity.getClass().getSimpleName());
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        oldActivity = currentActivity;

        currentActivity = activity;
        LogUtils.logString(EzApplication.class, "onActivityResumed: " + activity.getClass().getSimpleName());

//        IronSource.onResume(activity);

        updateBanner(activity);


    }

    private void updateBanner(Activity activity) {
        int id = activity.getResources().getIdentifier("bannerAd", "id", activity.getPackageName());
        BannerAd bannerAd = activity.findViewById(id);
        if (!isPause) {
            try {
                bannerAd.initViews();
            } catch (NullPointerException e) {

            }
        }
        isPause = false;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
//        IronSource.onPause(activity);
        LogUtils.logString(EzApplication.class, "onActivityPaused " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        LogUtils.logString(EzApplication.class, "onActivityStopped " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        LogUtils.logString(EzApplication.class, "onActivityDestroyed " + activity.getClass());
//        if (!activity.getClass().toString().contains("AdActivity")) {
//            isDestroy = true;
//        } else {
//            isDestroy = false;
//        }
    }


    public void setSkipNextAds() {
        if (AppConfigs.getBoolean(RemoteKey.ALLOW_SKIP_OPEN)) {
            isSkipNextAds = true;
        }
    }


}
