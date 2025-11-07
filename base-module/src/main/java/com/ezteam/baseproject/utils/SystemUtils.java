package com.ezteam.baseproject.utils;

import android.app.AppOpsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SystemUtils {

    public static String getThemeColorInHex(@NonNull Context context, @NonNull String colorName, @AttrRes int attribute) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(attribute, outValue, true);
        return String.format("#%06X", (0xFFFFFF & outValue.data));
    }

    public static String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault()); // Use device local timezone
        return sdf.format(date);
    }
    public static void copyToClipboard(Context context, CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }

    public static int getHeightStatusBar(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return StringUtils.capitalize(model);
        } else {
            return StringUtils.capitalize(manufacturer);
        }
    }

    public static String getVersionName(Context mContext) {
        try {
            return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }

    public static void openNewURL(Context mContext, String url) {
        if (!TextUtils.isEmpty(url)) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    public static final boolean isUsageAccessAllowed(Context mContext) {
        boolean granted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                AppOpsManager appOps = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
                int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), mContext.getPackageName());
                if (mode == AppOpsManager.MODE_DEFAULT) {
                    String permissionUsage = "android.permission.PACKAGE_USAGE_STATS";
                    granted = (mContext.checkCallingOrSelfPermission(permissionUsage) == PackageManager.PERMISSION_GRANTED);
                } else {
                    granted = (mode == AppOpsManager.MODE_ALLOWED);
                }
            } catch (Throwable e) {
            }
        } else {
            return true;
        }
        return granted;
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return false;
        }

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
    public static Long getInstallTime(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            return info.firstInstallTime;
        } catch (Exception e) {
            return -1L;
        }
    }

}
