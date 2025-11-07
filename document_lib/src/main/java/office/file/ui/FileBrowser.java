package office.file.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.artifex.solib.a;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import office.file.ui.AppFile.CloudPermissionChecked;
import office.file.ui.AppFile.EnumerateListener;
import office.file.ui.editor.BaseActivity;
import office.file.ui.editor.SOEditText;
import office.file.ui.editor.SOTextView;
import office.file.ui.editor.Utilities;
import office.file.ui.extension.FileExtKt;

public class FileBrowser extends RelativeLayout {
    private static AppFile mDirectory;
    List<AppFile> a;
    private f adapter;
    List<AppFile> b = new ArrayList<>();
    private BaseActivity mActivity = null;
    private SOEditText mEditText;
    private AppFile mEnumerating;
    private boolean mFirstUpdate;
    private Handler mHandler;
    private ListView mListView;
    private Button mSaveButton;
    private Runnable mUpdateFiles;

    public FileBrowser(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.b();
    }

    private void a(ListView var1, View var2, int var3, long var4) {
        final g var6 = (g) this.adapter.getItem(var3);
        if (var6 != null) {
            if (var6.a.isCloud() && !AppFile.i()) {
                try {
                    Utilities.showMessage((Activity) this.getContext(), this.getContext().getString(IdController.getIntstring("sodk_editor_connection_error_title")), this.getContext().getString(IdController.getIntstring("sodk_editor_connection_error_body")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            if (var6.a.d()) {
                AppFile.checkCloudPermission(this.mActivity, var6.a, new CloudPermissionChecked() {
                    public void a(boolean var1) {
                        if (var1) {
                            FileBrowser.this.b.add(var6.a);
                            FileBrowser.mDirectory = var6.a;
                            FileBrowser.this.mHandler.post(FileBrowser.this.mUpdateFiles);
                        }

                    }
                });
            } else {
                this.mEditText.setText(Utilities.removeExtension(var6.a.b));
            }
        }

    }

    private void a(AppFile var1, String var2, LinearLayout var3) {
        Button var4 = (Button) LayoutInflater.from(this.getContext()).inflate(IdController.getIntlayout("sodk_breadcrumb_button"), (ViewGroup) null);
        if (var2 == null) {
            var2 = var1.b();
        }

        var4.setText(var2);
        var4.setTag(var1);
        var4.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
                AppFile var5 = (AppFile) var1.getTag();
                if (var5 == null) {
                    FileBrowser.this.b = new ArrayList<>();
                    var5 = null;
                } else {
                    List<AppFile> var2 = new ArrayList<>();

                    for (AppFile var4 : FileBrowser.this.b) {
                        var2.add(var4);
                        if (var4.isSameAs(var5)) {
                            break;
                        }
                    }

                    FileBrowser.this.b = var2;
                }

                FileBrowser.mDirectory = var5;
                FileBrowser.this.mHandler.post(FileBrowser.this.mUpdateFiles);
            }
        });
        var3.addView(var4);
    }

    private void b() {
        LayoutInflater.from(this.getContext()).inflate(IdController.getIntlayout("sodk_file_browser"), this);
        this.mEditText = (SOEditText) this.findViewById(IdController.getIntid("edit_text"));
        this.mSaveButton = (Button) this.findViewById(IdController.getIntid("save_button"));
        ArrayList<AppFile> var1 = new ArrayList<>();
        this.a = var1;
        var1.add(new b(com.artifex.solib.a.b(this.getContext()).getAbsolutePath(), this.getResources().getString(IdController.getIntstring("sodk_editor_my_documents"))));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(Environment.isExternalStorageManager()) {
                this.a.add(new b(Utilities.getDownloadDirectory(this.getContext()).getAbsolutePath(), this.getResources().getString(IdController.getIntstring("sodk_editor_download"))));
                this.a.add(new b(Utilities.getRootDirectory(this.getContext()).getAbsolutePath(), this.getResources().getString(IdController.getIntstring("sodk_editor_all"))));
                String var2 = Utilities.getSDCardPath(this.getContext());
                if (var2 != null) {
                    this.a.add(new b(var2, "SD Card"));
                }
            } else {
                Toast.makeText(this.getContext(), this.getContext().getString(IdController.getIntstring("to_get_access_to_your_documents_please_allow_storage_permission")), Toast.LENGTH_LONG).show();
            }

        } else {
            if (com.artifex.solib.a.d(this.getContext())) {
                this.a.add(new b(Utilities.getDownloadDirectory(this.getContext()).getAbsolutePath(), this.getResources().getString(IdController.getIntstring("sodk_editor_download"))));
                this.a.add(new b(Utilities.getRootDirectory(this.getContext()).getAbsolutePath(), this.getResources().getString(IdController.getIntstring("sodk_editor_all"))));
                String var2 = Utilities.getSDCardPath(this.getContext());
                if (var2 != null) {
                    this.a.add(new b(var2, "SD Card"));
                }
            } else {
                Toast.makeText(this.getContext(), this.getContext().getString(IdController.getIntstring("to_get_access_to_your_documents_please_allow_storage_permission")), Toast.LENGTH_LONG).show();
            }
        }


//        AppFile var3 = c.a("root", "Google Drive", true, true);
//        if (var3 != null) {
//            this.a.add(var3);
//        }
//
//        var3 = c.b("0", "Box", true, true);
//        if (var3 != null) {
//            this.a.add(var3);
//        }
//
//        var3 = c.c("/", "Dropbox", true, true);
//        if (var3 != null) {
//            this.a.add(var3);
//        }
//
//        var3 = c.d("/", "OneDrive", true, true);
//        if (var3 != null) {
//            this.a.add(var3);
//        }

        mDirectory = null;
    }

    private void enableView(View view, boolean enable) {
        if (enable) {
            view.setAlpha(1f);
        } else {
            view.setAlpha(0.5f);
        }
        view.setEnabled(enable);
    }

    private void c() {
        SOEditText var1 = this.mEditText;
        boolean var2;
        var2 = mDirectory != null;

//        var1.setEnabled(var2);
        enableView(var1, var2);

        boolean var3 = this.mEditText.getText().toString().trim().length() > 0;
        enableView(this.mSaveButton, var3 && mDirectory != null);
    }

    private void d() {
        LinearLayout var1 = (LinearLayout) this.findViewById(IdController.getIntid("names_bar"));
        var1.removeAllViews();
        this.a((AppFile) null, this.getResources().getString(IdController.getIntstring("sodk_editor_storage")), var1);

        for (AppFile var3 : this.b) {
            var1.addView((SOTextView) LayoutInflater.from(this.getContext()).inflate(IdController.getIntlayout("sodk_breadcrumb_slash"), (ViewGroup) null));
            this.a(var3, (String) null, var1);
        }

    }

    private void setStatusBarColor(Activity activity, @ColorRes int colorResId) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, colorResId));

    }

    public void a(BaseActivity var1, String var2) {
        LinearLayout lnToolbar = this.findViewById(R.id.ln_save_toolbar);
        if (var2 != null) {
            if (var2.toLowerCase().endsWith(FileExtKt.getDOC()) || var2.toLowerCase().endsWith(FileExtKt.getDOCX())) {
//                lnToolbar.setBackgroundColor(ResourcesCompat.getColor(var1.getResources(), R.color.sodk_editor_header_doc_color, null));
//                setStatusBarColor(var1, R.color.color_doc_toolbar);
//                mSaveButton.setBackgroundColor(getContext().getResources().getColor(R.color.color_doc_toolbar));
            } else if ((var2.toLowerCase().endsWith(FileExtKt.getPPT()) || var2.toLowerCase().endsWith(FileExtKt.getPPTX()))) {
//                lnToolbar.setBackgroundColor(ResourcesCompat.getColor(var1.getResources(), R.color.sodk_editor_header_ppt_color, null));
//                setStatusBarColor(var1, R.color.color_ppt_toolbar);
//                mSaveButton.setBackgroundColor(getContext().getResources().getColor(R.color.color_ppt_toolbar));
            } else if (var2.toLowerCase().endsWith(FileExtKt.getXLS()) || var2.toLowerCase().endsWith(FileExtKt.getXLSX())) {
//                lnToolbar.setBackgroundColor(ResourcesCompat.getColor(var1.getResources(), R.color.sodk_editor_header_xls_color, null));
//                setStatusBarColor(var1, R.color.color_xls_toolbar);
//                mSaveButton.setBackgroundColor(getContext().getResources().getColor(R.color.color_xls_toolbar));
            } else if (var2.toLowerCase().endsWith(FileExtKt.getPDF())) {
//                lnToolbar.setBackgroundColor(ResourcesCompat.getColor(var1.getResources(), R.color.sodk_editor_header_pdf_color, null));
//                setStatusBarColor(var1, R.color.color_pdf_toolbar);
//                mSaveButton.setBackgroundColor(getContext().getResources().getColor(R.color.color_pdf_toolbar));
            } else {
//                lnToolbar.setBackgroundColor(ResourcesCompat.getColor(var1.getResources(), R.color.sodk_editor_header_other_color, null));
//                setStatusBarColor(var1, R.color.color_other_toolbar);
//                mSaveButton.setBackgroundColor(getContext().getResources().getColor(R.color.color_other_toolbar));
            }

            if (!var2.isEmpty()) {
                this.mEditText.setText(Utilities.removeExtension(var2));
            }
        }
        this.mActivity = var1;

        ImageView subAct = findViewById(R.id.img_sub_act);
        subAct.setVisibility(GONE);

        this.mEditText.setSelectAllOnFocus(true);
        this.mEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable var1) {
                FileBrowser.this.c();
            }

            public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4) {
            }

            public void onTextChanged(CharSequence var1, int var2, int var3, int var4) {
            }
        });
        ListView var3 = (ListView) this.findViewById(IdController.getIntid("fileListView"));
        this.mListView = var3;
        var3.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> var1, View var2, int var3, long var4) {
                FileBrowser var6 = FileBrowser.this;
                var6.a(var6.mListView, var2, var3, var4);
            }
        });
        f var4 = new f(((Activity) this.getContext()).getLayoutInflater(), false);
        this.adapter = var4;
        this.mListView.setAdapter(var4);
        this.mHandler = new Handler();
        this.mFirstUpdate = true;
        Runnable var5 = new Runnable() {
            public void run() {
                FileBrowser.this.findViewById(IdController.getIntid("no_documents_found")).setVisibility(View.GONE);
                FileBrowser var1 = FileBrowser.this;
                int var2 = IdController.getIntid("enumerate_progress");
                var1.findViewById(var2).setVisibility(View.GONE);
                FileBrowser.this.mEditText.clearFocus();
                Utilities.hideKeyboard(FileBrowser.this.getContext());
                FileBrowser.this.c();
                if (FileBrowser.mDirectory == null) {
                    FileBrowser.this.adapter.a();

                    for (var2 = 0; var2 < FileBrowser.this.a.size(); ++var2) {
                        FileBrowser.this.adapter.a(new g((AppFile) FileBrowser.this.a.get(var2)));
                    }

                    AppFile.i = null;
                    FileBrowser.this.b.clear();
                    FileBrowser.this.d();
                    if (FileBrowser.this.mFirstUpdate) {
                        FileBrowser.this.mFirstUpdate = false;
                        if (FileBrowser.this.a.size() == 1) {
                            FileBrowser.this.mListView.performItemClick(FileBrowser.this.mListView.getAdapter().getView(0, (View) null, (ViewGroup) null), 0, FileBrowser.this.mListView.getAdapter().getItemId(0));
                        }
                    }
                } else {
                    if (!FileBrowser.mDirectory.serviceAvailable()) {
                        FileBrowser.mDirectory = null;
                        FileBrowser.this.mHandler.post(FileBrowser.this.mUpdateFiles);
                        return;
                    }

                    FileBrowser.this.findViewById(var2).setVisibility(View.VISIBLE);
                    FileBrowser.this.adapter.a();
                    FileBrowser.this.mEnumerating = FileBrowser.mDirectory.enumerateDir(new EnumerateListener() {
                        public void a(ArrayList<AppFile> var1) {
                            if (var1 != null) {
                                ArrayList var2 = new ArrayList();
                                Iterator var3 = var1.iterator();

                                while (true) {
                                    AppFile var6;
                                    do {
                                        if (!var3.hasNext()) {
                                            Collections.sort(var2, new Comparator<AppFile>() {
                                                @Override
                                                public int compare(AppFile var1, AppFile var2) {
                                                    return var1.b().compareToIgnoreCase(var2.b());
                                                }
                                            });
                                            var3 = var2.iterator();

                                            while (var3.hasNext()) {
                                                var6 = (AppFile) var3.next();
                                                FileBrowser.this.adapter.a(new g(var6));
                                            }

                                            int var4 = var2.size();
                                            int var5 = IdController.getIntid("no_documents_found");
                                            if (var4 == 0) {
                                                FileBrowser.this.findViewById(var5).setVisibility(View.VISIBLE);
                                            } else {
                                                FileBrowser.this.findViewById(var5).setVisibility(View.GONE);
                                            }

                                            FileBrowser.this.findViewById(IdController.getIntid("enumerate_progress")).setVisibility(View.GONE);
                                            return;
                                        }

                                        var6 = (AppFile) var3.next();
                                    } while (!var6.d() && !var6.b(FileBrowser.this.getContext()));

                                    var2.add(var6);
                                }
                            } else {
                                FileBrowser.mDirectory = null;
                                FileBrowser.this.mHandler.post(FileBrowser.this.mUpdateFiles);
                            }
                        }
                    });
                }

                FileBrowser.this.d();
            }
        };
        this.mUpdateFiles = var5;
        this.mHandler.post(var5);
    }

    public SOEditText getEditText() {
        return this.mEditText;
    }

    public String getFileName() {
        return this.mEditText.getText().toString().trim();
    }

    public AppFile getFolderAppFile() {
        return mDirectory;
    }
}
