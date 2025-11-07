package com.google.android.gms.ads.ez.adparam;


public class AdUnit {
    public static final boolean TEST = false;

    public static boolean isTEST() {
        return TEST;
    }

    public static boolean getForceWaitApplovin() {
        return AdUnitFactory.getInstance(isTEST()).getForceWaitApplovin();
    }
    public static boolean allowShowCMP() {
        return AdUnitFactory.getInstance(isTEST()).allowShowCMP();
    }

    public static String getAdmobInterId() {
        String id = AdUnitFactory.getInstance(isTEST()).getAdmobInterId();
        return id == null ? "" : id;
    }

    public static String getAdmobNativeId() {
        String id = AdUnitFactory.getInstance(isTEST()).getAdmobNativeId();
        return id == null ? "" : id;
    }

    public static String getAdmobBannerId() {
        String id = AdUnitFactory.getInstance(isTEST()).getAdmobBannerId();
        return id == null ? "" : id;
    }

    public static String getAdmobOpenId() {
        String id = AdUnitFactory.getInstance(isTEST()).getAdmobOpenId();
        return id == null ? "" : id;
    }

    public static String getAdmobRewardedId() {
        String id = AdUnitFactory.getInstance(isTEST()).getAdmobRewardedId();
        return id == null ? "" : id;
    }

    public static String getAdxInterId() {
        String id = AdUnitFactory.getInstance(isTEST()).getAdxInterId();
        return id == null ? "" : id;
    }

    public static String getAdxBannerId() {
        String id = AdUnitFactory.getInstance(isTEST()).getAdxBannerId();
        return id == null ? "" : id;
    }


    public static String getAdxNativeId() {
        String id = AdUnitFactory.getInstance(isTEST()).getAdxNativeId();
        return id == null ? "" : id;
    }

    public static String getAdxOpenId() {
        String id = AdUnitFactory.getInstance(isTEST()).getAdxOpenId();
        return id == null ? "" : id;
    }

    public static String getFacebookInterId() {
        String id = AdUnitFactory.getInstance(isTEST()).getFacebookInterId();
        return id == null ? "" : id;
    }

    public static String getFacebookBannerId() {
        String id = AdUnitFactory.getInstance(isTEST()).getFacebookBannerId();
        return id == null ? "" : id;
    }

    public static String getFacebookNativeId() {
        String id = AdUnitFactory.getInstance(isTEST()).getFacebookNativeId();
        return id == null ? "" : id;
    }

    public static int getCountShowAds() {
        return AdUnitFactory.getInstance(isTEST()).getCountShowAds();
    }

    public static int getLimitShowAds() {
        return AdUnitFactory.getInstance(isTEST()).getLimitShowAds();
    }

    public static long getTimeLastShowAds() {
        return AdUnitFactory.getInstance(isTEST()).getTimeLastShowAds();
    }


    public static String getMasterAdsNetwork() {
        String id = AdUnitFactory.getInstance(isTEST()).getMasterAdsNetwork();
        return id == null ? "" : id;
    }

    public static String getMasterOpenAdsNetwork() {
        String id = AdUnitFactory.getInstance(isTEST()).getMasterOpenAdsNetwork();
        return id == null ? "" : id;
    }


    public static String getApplovinBannerId() {
        String id = AdUnitFactory.getInstance(isTEST()).getApplovinBannerId();
        return id == null ? "" : id;
    }

    public static String getApplovinInterId() {
        String id = AdUnitFactory.getInstance(isTEST()).getApplovinInterId();
        return id == null ? "" : id;
    }

    public static String getApplovinRewardId() {
        String id = AdUnitFactory.getInstance(isTEST()).getApplovinRewardId();
        return id == null ? "" : id;
    }
}
