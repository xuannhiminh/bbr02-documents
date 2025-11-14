package com.brian.base_iap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class PreferencesHelper {

    public static final String SHOW_SELECT_LANGUAGE_FIRST = "show select language first";
    public static final String SHOW_FIRST_PREMIUM = "show first premium";
    public static final String GLOBAL_FIREBASE_TOPIC = "global firebase topic";
    public static final String KEY_LAST_ENGAGE = "key last engage";
    public static final String KEY_FIRST_OPEN = "key first open";
    private static SharedPreferences sharedPreferences;
    private static final String NAME = "MyPref";
    public static final String KEY_LANGUAGE = "language";
    public static final String ENABLE_KIDZONE = "enable kidzone";
    private static final String LIST_APP_KID_ZONE = "list app kid zone";
    private static final String LIST_APP_SCREEN_OFF = "list app screen off";
    private static final String LIST_APP_FIVE_MINUTES = "list app 5 minutes";
    public static final String SETTING_VALUE_SOUND = "setting value sound";
    public static final String SETTING_VALUE_TIMER = "setting value timer";
    public static final String SETTING_VALUE_TIME_LOCK = "setting value time lock";
    public static final String SETTING_VALUE_TONE = "setting value alram tone";
    public static final String SETTING_THEME = "setting value theme";
    public static final String SETTING_APP_MASK = "setting value app mask";
    public static final String SETTING_NEW_APP_LOCK = "setting value new app lock";
    public static final String PATTERN_CODE = "pattern code";
    public static final String FINGERPRINT_UNLOCK = "fingerprint unlock";
    public static final String PIN_CODE = "pin code";
    public static final String QUESTION_ANSER = "question anser";
    public static final String INTRUDER_SELFIE_ENTRIES = "intruder selfie entries";
    public static final String INTRUDER_SELFIE = "intruder selfie ";
    public static final String HIDE_PATTERN = "hide pattern ";
    public static final String FULL_CHARGER_ALARM = "full charger alarm";
    public static final String NOTI_CHARGER_CONNECTED = "notifi charger connected";
    public static final String VIBRATE_DURING_ALARM = "vibrate during alarm";
    public static final String FLASH_DURING_ALARM = "flash during alarm";
    public static final String DETECTION_TYPE = "detection type";

    public static void init(Context mContext) {
        sharedPreferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor editor() {
        return sharedPreferences.edit();
    }

    public static void putBoolean(String key, boolean value) {
        editor().putBoolean(key, value).apply();
    }

    public static void putString(String key, String value) {
        editor().putString(key, value).apply();
    }

    public static void putInt(String key, int value) {
        editor().putInt(key, value).apply();
    }

    public static void putLong(String key, long value) {
        editor().putLong(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defaultvalue) {
        return sharedPreferences.getBoolean(key, defaultvalue);
    }

    public static String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public static String getString(String key, String defaultString) {
        return sharedPreferences.getString(key, defaultString);
    }

    public static int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static void setPatternCode(String patternCode) {
        sharedPreferences.edit().putString(PATTERN_CODE, patternCode).apply();
        // set pin code empty
        sharedPreferences.edit().putString(PIN_CODE, "").apply();
    }

    public static String getPatternCode() {
        return sharedPreferences.getString(PATTERN_CODE, "");
    }

    public static void setPinCode(String pinCode) {
        sharedPreferences.edit().putString(PIN_CODE, pinCode).apply();
        // set pattern code empty
        sharedPreferences.edit().putString(PATTERN_CODE, "").apply();
    }

    public static String getPinCode() {
        return sharedPreferences.getString(PIN_CODE, "");
    }

    public static void setQuestionAnser(String question, String answer) {
        sharedPreferences.edit().putString(QUESTION_ANSER, question + "-" + answer).apply();
    }

    public static String getQuestionAnser() {
        return sharedPreferences.getString(QUESTION_ANSER, "");
    }

    public static Boolean checkQuestionAnser(String question, String anser) {
        String questionAnser = getQuestionAnser();
        String[] split = questionAnser.split("-");
        if (split.length > 0) {
            return split[0].equals(question) && split[1].equals(anser);
        }
        return false;
    }

    public static boolean isPatternCode() {
        if (TextUtils.isEmpty(getPinCode())) {
            return true;
        } else return !TextUtils.isEmpty(getPatternCode());
    }
}
