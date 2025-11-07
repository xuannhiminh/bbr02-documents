package office.file.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.ezteam.baseproject.utils.PreferencesUtils;
import com.ezteam.baseproject.utils.PresKey;

import java.io.File;

import office.file.ui.editor.BaseActivity;
import office.file.ui.editor.SOEditText;
import office.file.ui.editor.SOEditTextOnEditorActionListener;
import office.file.ui.editor.Utilities;

public class ExportFileActivity extends BaseActivity {
    private static final int CANCEL = 1;
    private static final int OK = 2;
    private static FileBrowser mBrowser;
    private static String mFilename;
    private static int mKind;
    private static ExportFileActivity.a mListener;
    private static int mResult;
    private static boolean mUsedButton;
    private boolean mFinishedEarly = false;
    private Configuration mLastConfiguration;

    public ExportFileActivity() {
    }

    public static String fileExt = "";

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        super.onCreate(savedInstanceState, persistentState);
//    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static void a(Activity var0, int var1, boolean var2, ExportFileActivity.a var3, String var4) {
        mListener = var3;
        mKind = var1;
        if (var4 == null) {
            mFilename = "";
        } else {
            mFilename = var4;
        }

        mUsedButton = false;
        var0.startActivity(new Intent(var0, ExportFileActivity.class));
    }


    public void onConfigurationChanged(Configuration var1) {
        super.onConfigurationChanged(var1);
        if (VERSION.SDK_INT >= 28 && var1.uiMode != this.mLastConfiguration.uiMode) {
            ExportFileActivity.a var2 = mListener;
            if (var2 != null) {
                var2.a();
            }

            super.finish();
            this.mFinishedEarly = true;
        }

        this.mLastConfiguration = this.getResources().getConfiguration();
    }

    protected void onCreate(Bundle var1) {
        super.onCreate(var1);
        this.mLastConfiguration = this.getResources().getConfiguration();
        this.setContentView(IdController.getIntlayout("sodk_choose_path"));
        String var3 = mFilename;
        FileBrowser var2 = (FileBrowser) this.findViewById(IdController.getIntid("file_browser"));
        mBrowser = var2;
        var2.a(this, var3);
        Button var4 = (Button) this.findViewById(IdController.getIntid("save_button"));
        if (mKind == 3) {
            var3 = "sodk_editor_copy";
        } else {
            var3 = "sodk_editor_save";
        }

        var4.setText(this.getString(IdController.getIntstring(var3)));
        var4.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                exportFile();
            }
        });
        ((Button) this.findViewById(IdController.getIntid("cancel_button"))).setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                Utilities.hideKeyboard(ExportFileActivity.this);
                ExportFileActivity.mUsedButton = true;
                ExportFileActivity.mResult = 1;
                ExportFileActivity.this.finish();
            }
        });
        SOEditText var5 = mBrowser.getEditText();
        var5.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View var1, int var2, KeyEvent var3) {
                if (var3.getAction() == 0 && var2 == 66) {
                    exportFile();
                    return true;
                } else {
                    return false;
                }
            }
        });
        var5.setOnEditorActionListener(new SOEditTextOnEditorActionListener() {
            public boolean onEditorAction(SOEditText var1, int var2, KeyEvent var3) {
                boolean var4 = true;
                if (var2 == 6) {
                    exportFile();
                } else {
                    var4 = false;
                }

                return var4;
            }
        });
        this.findViewById(R.id.iv_cancel).setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                Utilities.hideKeyboard(ExportFileActivity.this);
                ExportFileActivity.mUsedButton = true;
                ExportFileActivity.mResult = 1;
                ExportFileActivity.this.finish();
            }
        });
    }

    private void exportFile() {
        Utilities.hideKeyboard(ExportFileActivity.this);
        ExportFileActivity.mUsedButton = true;
        ExportFileActivity.mResult = 2;
        ExportFileActivity.this.finish();
        PreferencesUtils.putBoolean(PresKey.SAVED_FILE, true);
        String path = "";
        for (int i = 0; i < mBrowser.b.size(); i++) {
            path = mBrowser.b.get(i).getDisplayPath();

        }
        String finalPath = path;
        Runnable r = () -> {
            File file = (new File(finalPath + File.separator + mBrowser.getFileName().concat(fileExt)));
            MediaScannerConnection.scanFile(getApplicationContext(),
                    new String[]{file.toString()},
                    null, (path1, uri) -> {
                        /*Intent t = new Intent(ActivityExtKt.getRELOAD_CREATE());
                        t.putExtra(Const.path, path1);
                        t.putExtra(Const.ext, fileExt);
                        sendBroadcast(t);*/
                    });
        };
        new Handler(Looper.myLooper()).postDelayed(r, 2500);
    }

    public static void setFileExt(String fileExt) {

        ExportFileActivity.fileExt = fileExt;
    }

    protected void onDestroy() {
        super.onDestroy();
        try {
            if (!this.mFinishedEarly) {
                label18:
                {
                    if (mUsedButton) {
                        int var1 = mResult;
                        if (var1 != 1) {
                            if (var1 == 2) {
                                mListener.a(mBrowser);
                            }
                            break label18;
                        }
                    }

                    mListener.a();
                }

                mListener = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public interface a {
        void a();

        void a(FileBrowser var1);
    }
}
