//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui;

import android.app.Application;
import android.content.Context;


import com.nlbn.ads.util.AdsApplication;

import viewx.j.b;

public abstract class MyLibApplication extends AdsApplication {
    private static Context context;

    public MyLibApplication() {
    }

    private void ab() {
        try {
            BaseOpenFileActivity.b();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BaseOpenFileActivity.c();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Context getAppContext() {
        return context;
    }

    public void onCreate() {
        this.ab();
        super.onCreate();
        context = this;
    }
}
