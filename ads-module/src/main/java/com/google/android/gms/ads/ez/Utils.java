package com.google.android.gms.ads.ez;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.EventLogTags;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.listenner.LoadAdCallback;
import com.google.android.gms.ads.ez.listenner.ShowAdCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static String getTopActivity(Context context) {


        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);


        return EzApplication.getInstance().getCurrentActivity().getClass().getName();
    }

    public static boolean checkTopActivityIsAd(Context context) {
        String topActivity = Utils.getTopActivity(context);
        if (topActivity.contains("com.google.android.gms.ads.AdActivity")
                || topActivity.contains("com.applovin.adview.AppLovinFullscreenActivity")
                || topActivity.contains("StartActivity")
                || topActivity.contains("GetStartActivity")
                || topActivity.contains("SplashActivity")
                || topActivity.contains("PrivacyPolicy")
        ) {
            return true;
        }
        return false;
    }

    public static boolean checkTopActivityIsEzTeam(Context context) {
        if (Utils.getTopActivity(context).indexOf("ezteam") != -1) {
            return true;
        }
        return false;
    }

    public static int getVersionCode(Context context) {
        int version = 0;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static boolean checkNetworkEnable(Context activity) {
        ConnectivityManager conMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean CheckInstallerId(Context context) {
        if (AdUnit.isTEST()) {
            return true;
        }
        // A list with valid installers package name
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));
        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

        // true if your app has been downloaded from Play Store
        Log.e("Utils", "CheckInstallerId: " + (installer != null && validInstallers.contains(installer)));
        return installer != null && validInstallers.contains(installer);
    }

    public static void RemoveViewInParent(ViewGroup view) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
    }
}
