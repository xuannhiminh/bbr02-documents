package com.google.android.gms.ads.ez.adparam;


import android.util.Log;

import com.google.android.gms.ads.ez.EzApplication;
import com.google.android.gms.ads.ez.Utils;
import com.google.android.gms.ads.ez.remote.AppConfigs;
import com.google.android.gms.ads.ez.remote.RemoteKey;
import com.google.firebase.BuildConfig;

public class AdsParamUtils extends AdUnitFactory {

    @Override
    public String getAdmobInterId() {
        return AppConfigs.getString(RemoteKey.ADMOB_INTER_ID);
    }

    @Override
    public String getAdmobNativeId() {
        return AppConfigs.getString(RemoteKey.ADMOB_NATIVE_ID);
    }

    @Override
    public boolean getForceWaitApplovin() {
        return AppConfigs.getBoolean(RemoteKey.FORCE_WAIT_APPLOVIN);
    }

    @Override
    public boolean allowShowCMP() {
        return AppConfigs.getBoolean(RemoteKey.ALLOW_SHOW_CMP);
    }

    @Override
    public String getAdmobOpenId() {
        return AppConfigs.getString(RemoteKey.ADMOB_OPEN_ID);
    }

    @Override
    public String getAdmobBannerId() {
        return AppConfigs.getString(RemoteKey.ADMOB_BANNER_ID);
    }

    @Override
    public String getAdmobRewardedId() {
        return AppConfigs.getString(RemoteKey.ADMOB_REWARDED_ID);
    }


    @Override
    public String getFacebookInterId() {
        return AppConfigs.getString(RemoteKey.FB_INTER_ID);
    }

    @Override
    public String getFacebookBannerId() {
        return AppConfigs.getString(RemoteKey.FB_BANNER_ID);
    }

    @Override
    public String getFacebookNativeId() {
        return AppConfigs.getString(RemoteKey.FB_NATIVE_ID);
    }


    @Override
    public String getMasterAdsNetwork() {
        return AppConfigs.getString(RemoteKey.MASTER_ADS_NETWORK);
    }

    @Override
    public String getMasterOpenAdsNetwork() {
        return AppConfigs.getString(RemoteKey.MASTER_OPEN_ADS_NETWORK);
    }

    @Override
    public Long getProjectId() {
        return AppConfigs.getLong(RemoteKey.PROJECT_ID);
    }

    @Override
    public String getResource() {
        return AppConfigs.getString(RemoteKey.RESOURCE);
    }

    @Override
    public String getTokenApi() {
        return AppConfigs.getString(RemoteKey.TOKEN_API);
    }


    @Override
    public String getAdxInterId() {
        return AppConfigs.getString(RemoteKey.ADX_INTER_ID);
    }

    @Override
    public String getAdxBannerId() {
        return AppConfigs.getString(RemoteKey.ADX_BANNER_ID);
    }

    @Override
    public String getAdxNativeId() {
        return AppConfigs.getString(RemoteKey.ADX_NATIVE_ID);
    }

    @Override
    public String getAdxOpenId() {
        return AppConfigs.getString(RemoteKey.ADX_OPEN_ID);
    }

    @Override
    public String getApplovinBannerId() {
        return AppConfigs.getString(RemoteKey.APPLOVIN_BANNER_ID);
    }

    @Override
    public String getApplovinInterId() {
        return AppConfigs.getString(RemoteKey.APPLOVIN_INTER_ID);
    }

    @Override
    public String getApplovinMrecId() {
        return AppConfigs.getString(RemoteKey.APPLOVIN_MREC_ID);
    }

    @Override
    public String getApplovinRewardId() {
        return AppConfigs.getString(RemoteKey.APPLOVIN_REWARDED_ID);
    }

    @Override
    public int getCountShowAds() {
        return 1;
    }

    @Override
    public int getLimitShowAds() {
        return AppConfigs.getInt(RemoteKey.MAX_SHOW_DAY);
    }

    @Override
    public long getTimeLastShowAds() {

        if (AppConfigs.getInt(RemoteKey.APP_UPDATE_VERSION) == Utils.getVersionCode(EzApplication.getInstance().getCurrentActivity())) {
            return 50;
        } else {
            return AppConfigs.getInt(RemoteKey.TIME_SHOW_ADS);
        }

    }


}
