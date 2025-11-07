package office.file.ui.editor;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate;

public class BaseActivity extends AppCompatActivity {
    private static BaseActivity mCurrentActivity;
    private static BaseActivity.PermissionResultHandler mPermissionResultHandler;
    private static BaseActivity.ResultHandler mResultHandler;
    private static BaseActivity.ResumeHandler mResumeHandler;

    private LocalizationActivityDelegate localizationDelegate;

    @Override
    protected void attachBaseContext(Context newBase) {
        localizationDelegate = new LocalizationActivityDelegate(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            applyOverrideConfiguration(localizationDelegate.updateConfigurationLocale(newBase));
            super.attachBaseContext(newBase);
        } else {
            super.attachBaseContext(localizationDelegate.attachBaseContext(newBase));
        }
    }



    public BaseActivity() {
    }

    public static BaseActivity getCurrentActivity() {
        return mCurrentActivity;
    }

//    private void setLocale(String var1) {
//        this.setLocale(new Locale(var1));
//    }

//    private void setLocale(Locale var1) {
//        Locale.setDefault(var1);
//        Configuration var2 = this.getBaseContext().getResources().getConfiguration();
//        var2.locale = var1;
//        this.getBaseContext().getResources().updateConfiguration(var2, this.getBaseContext().getResources().getDisplayMetrics());
//    }

    public static void setPermissionResultHandler(BaseActivity.PermissionResultHandler var0) {
        mPermissionResultHandler = var0;
    }

    public static void setResultHandler(BaseActivity.ResultHandler var0) {
        mResultHandler = var0;
    }

    public static void setResumeHandler(BaseActivity.ResumeHandler var0) {
        mResumeHandler = var0;
    }

    public boolean isSlideShow() {
        return false;
    }

    protected void onActivityResult(int var1, int var2, Intent var3) {
        BaseActivity.ResultHandler var4 = mResultHandler;
        if (var4 == null || !var4.handle(var1, var2, var3)) {
            super.onActivityResult(var1, var2, var3);
        }

    }

    protected void onCreate(Bundle var1) {
        EdgeToEdge.enable(this);
        localizationDelegate.onCreate();
        super.onCreate(var1);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());

        // Set the behavior to allow transient bars to be revealed by swipe
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        View rootView = findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {

            androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    insets.top,
                    v.getPaddingRight(),
                    insets.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });
        /*if (!BuildConfig.DEBUG && !Utils.getPGFromJNI().equals(this.getPackageName())) {
            this.finish();
            this.finishAffinity();
        }*/

    }

    protected void onPause() {
        mCurrentActivity = null;
        super.onPause();
    }

    public void onRequestPermissionsResult(int var1, String[] var2, int[] var3) {
        BaseActivity.PermissionResultHandler var4 = mPermissionResultHandler;
        if (var4 == null || !var4.handle(var1, var2, var3)) {
            super.onRequestPermissionsResult(var1, var2, var3);
        }

    }

    protected void onResume() {
        mCurrentActivity = this;
        super.onResume();
        localizationDelegate.onResume(this);
        BaseActivity.ResumeHandler var1 = mResumeHandler;
        if (var1 != null) {
            var1.handle();
        }
    }

    public interface PermissionResultHandler {
        boolean handle(int var1, String[] var2, int[] var3);
    }

    public interface ResultHandler {
        boolean handle(int var1, int var2, Intent var3);
    }

    public interface ResumeHandler {
        void handle();
    }
}
