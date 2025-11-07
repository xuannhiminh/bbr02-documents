package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.ads.ez.remote.AppConfigs;
import com.google.android.gms.ads.ez.remote.RemoteKey;

public class OverlayView extends LinearLayout {
    private Context mContext;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    public OverlayView(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    private void initViews() {
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(localDisplayMetrics);
        params = getLayoutParam();
        setBackgroundColor(Color.TRANSPARENT);
    }


    public void showOverlay() {
        try {
            Log.e("OverlayView", "showOverlay: " + ((Activity) mContext).getClass().toString());
            windowManager.addView(this, params);

            new CountDownTimer(2000, 2000) {

                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    try {
                        Log.e("OverlayView", "remove: ");
                        windowManager.removeView(OverlayView.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private WindowManager.LayoutParams getLayoutParam() {
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(localDisplayMetrics);
        localLayoutParams.y = 0;
        localLayoutParams.width = Math.min(localDisplayMetrics.widthPixels, localDisplayMetrics.heightPixels);
        localLayoutParams.height = (Math.max(localDisplayMetrics.widthPixels, localDisplayMetrics.heightPixels));
        localLayoutParams.flags = 155714048;
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        localLayoutParams.format = PixelFormat.TRANSLUCENT;
        return localLayoutParams;
    }


    public static void showOverlay(Context context) {
        Log.e("TAG", "showOverlay: " + AppConfigs.getBoolean(RemoteKey.ENABLE_OVERLAY));
        if (AppConfigs.getBoolean(RemoteKey.ENABLE_OVERLAY)) {
            Log.e("TAG", "showOverlay: " + AppConfigs.getInt(RemoteKey.APP_UPDATE_VERSION));
            if (AppConfigs.getInt(RemoteKey.APP_UPDATE_VERSION) != Utils.getVersionCode(EzApplication.getInstance().getCurrentActivity())) {
                OverlayView overlayView = new OverlayView(context);
                overlayView.showOverlay();
            }


        }

    }
}
