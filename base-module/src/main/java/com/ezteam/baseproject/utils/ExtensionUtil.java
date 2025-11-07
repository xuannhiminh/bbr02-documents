package com.ezteam.baseproject.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ExtensionUtil {

    public static void SendFeedBack(Context mContext, String mEmail, String mEmailTitle) {
        String[] TO = {mEmail};
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.setData(Uri.parse("mailto:"));
        intentEmail.setType("message/rfc822");

        intentEmail.putExtra(Intent.EXTRA_EMAIL, TO);
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, mEmailTitle);
        intentEmail.putExtra(Intent.EXTRA_TEXT, "Enter your FeedBack");

        try {
            mContext.startActivity(Intent.createChooser(intentEmail, "Send FeedBack..."));
        } catch (ActivityNotFoundException e) {

        }
    }

    public static void shareApp(Context context) {
        final String appPackageName = context.getPackageName();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out the App at: https://play.google.com/store/apps/details?id=" + appPackageName);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }
    public static void shareAppUrl(Context context ,String packageUrl) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out the App at: https://play.google.com/store/apps/details?id=" + packageUrl);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public static void OpenMoreApp(Context mContext, String mStore) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://search?q=pub:" + mStore));
        mContext.startActivity(intent);
    }

    public static void OpenBrower(Context mContext, String mLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(mLink));
            mContext.startActivity(intent);
        } catch (Exception e) {
        }
    }

    public static void shareTxt(Context context, String data) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        /*This will be the actual content you wish you share.*/
        /*The type of the content is text, obviously.*/
        intent.setType("text/plain");
        /*Applying information Subject and Body.*/
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, data);
        /*Fire!*/
        context.startActivity(Intent.createChooser(intent, ""));
    }

}
