package com.google.android.gms.ads.ez;

import org.jetbrains.annotations.Nullable;

public class IAPHelper {
    public static final String KEY_CREATE_FILE = "create_file";
    public static final String KEY_SAVE = "save_file";


    public static final int ACTIVITY_PREMIUM = 0;
    public static final int DIALOG_REMOVE_ONE_MONTH = 1;
    public static final int DIALOG_GIFT = 2;
    public static final String KEY_CAST_WEB = "cast_web";


    public static boolean isUnlockFunction(String function) {
//        if(function.equals(KEY_CREATE_FILE)){
//            return false;
//        }
        return true;
    }

    public static int getScreenPromoSub() {
        return DIALOG_REMOVE_ONE_MONTH;
    }


    public static String getOneMonthKey() {
        return IAPUtils.KEY_PREMIUM_2;
    }


    public static String getOneMonthSaleKey() {
        return IAPUtils.KEY_PREMIUM_2;
    }

    public static String getGiftKey() {
        return IAPUtils.KEY_PREMIUM_2;
    }

    public static String getOneYearKey() {
        return IAPUtils.KEY_PREMIUM_3;
    }

    public static String getFreeTrialKey() {
        return IAPUtils.KEY_PREMIUM_1;
    }


}
