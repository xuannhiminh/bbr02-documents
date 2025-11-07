package office.file.ui.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.io.File;
import java.util.UUID;

import office.file.ui.OpenFileActivity;
import office.file.ui.R;
import office.file.ui.editor.BuildConfig;
import office.file.ui.extension.FileExtKt;

public class Utility {
    public static void setUpDialog(Dialog dialog, Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();

        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (displayMetrics.widthPixels * .95);
        lp.height = (int) (displayMetrics.heightPixels * .9);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout((int) (displayMetrics.widthPixels * .95), (int) (displayMetrics.heightPixels * .9));
    }

    public static int getVersionApp(Activity activity) {
        PackageInfo pInfo = null;
        try {
            pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int currentAppVersionCode = 0;
        if (pInfo != null) {
            currentAppVersionCode = pInfo.versionCode;
        }
        return currentAppVersionCode;
    }

    public static void changeColorStatusBar(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(color);
        }
    }

    public static void funcRepeatAnim(View viewAnim) {
        viewAnim.clearAnimation();
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(viewAnim, "scaleX", new float[]{1.0f, 1.05f, 1.0f});
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(viewAnim, "scaleY", new float[]{1.0f, 1.05f, 1.0f});
        animatorSet.setDuration(1800);
        scaleX.setRepeatCount(1000);
        scaleY.setRepeatCount(1000);
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.start();
    }

    public static void funcGotoGP(Context mContext, String packageName) {
        try {
            mContext.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + packageName)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "Not Show!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static void funcPolicy(Context mContext, String linkPolicy) {
        try {
            mContext.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(linkPolicy)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "No Open Policy", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



    public static void funcSendEmail(Context ctx) {
        try {
            String nameDevice = Build.MODEL + " API " + Build.VERSION.SDK_INT;
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            int versionCode = pInfo.versionCode;

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            String[] recipients = new String[]{"idomobier@gmail.com"};
            emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback From Device: " + nameDevice + " version " + versionCode);
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
            emailIntent.setType("***text/plain***");
            ctx.startActivity(Intent.createChooser(emailIntent, "Send E-mail..."));
        } catch (Exception e) {
            Toast.makeText(ctx, "No Send Feedback!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static void shareFile(Context context, File fileWithinMyDir) {
        if (fileWithinMyDir.exists()) {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            intentShareFile.setType("application/" + fileWithinMyDir.getName().substring(fileWithinMyDir.getName().indexOf(".")+1));
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentShareFile.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", fileWithinMyDir));
            context.startActivity(Intent.createChooser(intentShareFile, "Share File"));
        }
    }

    public static void createShortCut(Context context, File mFile) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Intent intent = new Intent(new Intent(context, OpenFileActivity.class));
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("fileName", mFile.getAbsolutePath());
            intent.putExtra("pageNum", 0);

            if (mFile.isFile()) {
                if (mFile.getName().toLowerCase().endsWith(FileExtKt.getPDF())) {
                    ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
                            .setIntent(intent) // !!! intent's action must be set on oreo
                            .setShortLabel(mFile.getName())
                            .setIcon(IconCompat.createWithResource(context, R.drawable.png_pdf))
                            .build();
                    ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);


                    Toast.makeText(context, "Create shortcut success!", Toast.LENGTH_SHORT).show();
                } else if (mFile.getName().toLowerCase().endsWith(FileExtKt.getXLS()) || mFile.getName().toLowerCase().endsWith(FileExtKt.getXLSX())) {
                    ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
                            .setIntent(intent) // !!! intent's action must be set on oreo
                            .setShortLabel(mFile.getName())
                            .setIcon(IconCompat.createWithResource(context, R.drawable.png_excel))
                            .build();
                    ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
                    Toast.makeText(context, "Create shortcut success!", Toast.LENGTH_SHORT).show();
                } else if (mFile.getName().toLowerCase().endsWith(FileExtKt.getPPT()) || mFile.getName().toLowerCase().endsWith(FileExtKt.getPPTX())) {
                    ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
                            .setIntent(intent) // !!! intent's action must be set on oreo
                            .setShortLabel(mFile.getName())
                            .setIcon(IconCompat.createWithResource(context, R.drawable.png_ppt))
                            .build();
                    ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
                    Toast.makeText(context, "Create shortcut success!", Toast.LENGTH_SHORT).show();

                } else if (mFile.getName().toLowerCase().endsWith(FileExtKt.getDOC()) || mFile.getName().toLowerCase().endsWith(FileExtKt.getDOCX())) {
                    ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
                            .setIntent(intent) // !!! intent's action must be set on oreo
                            .setShortLabel(mFile.getName())
                            .setIcon(IconCompat.createWithResource(context, R.drawable.png_doc))
                            .build();
                    ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
                    Toast.makeText(context, "Create shortcut success!", Toast.LENGTH_SHORT).show();
                } else {
                    ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
                            .setIntent(intent) // !!! intent's action must be set on oreo
                            .setShortLabel(mFile.getName())
                            .setIcon(IconCompat.createWithResource(context, R.drawable.png_txt ))
                            .build();
                    Toast.makeText(context, "Create shortcut success!", Toast.LENGTH_SHORT).show();
                    ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
                }
            }
        }
    }

    public static String getRealPathFromURI(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }
}
       