//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office.file.ui.editor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import com.artifex.solib.SOBitmap;
import com.artifex.solib.SODoc;
import com.artifex.solib.SOPage;
import com.artifex.solib.SOPageListener;
import com.artifex.solib.SORender;
import com.artifex.solib.k;
import com.artifex.solib.l;
import com.artifex.solib.n;
import com.artifex.solib.o;
import office.file.ui.editor.R.dimen;
import office.file.ui.editor.Utilities.passwordDialogListener;

public class SODocSession {
    private final Activity mActivity;
    private boolean mCancelled = false;
    private boolean mCompleted = false;
    private SODoc mDoc;
    private SOFileState mFileState = null;
    private k mLibrary;
    private SODocSession.SODocSessionLoadListener mListener = null;
    private SODocSession.SODocSessionLoadListener mListener2 = null;
    private SODocSession.SODocSessionLoadListenerCustom mListenerCustom = null;
    private boolean mLoadError = false;
    private boolean mOpen = false;
    private SOPage mPage = null;
    private int mPageCount = 0;
    private SORender mRender = null;
    private String mUserPath = null;
    private String password = null;
    private SharedPreferences sharedPreferences;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SODocSession(Activity var1, k var2) {
        this.mActivity = var1;
        this.mLibrary = var2;
    }

    private void a() {
        SORender var1 = this.mRender;
        if (var1 != null) {
            var1.destroy();
        }

        this.mRender = null;
        SOPage var2 = this.mPage;
        if (var2 != null) {
            var2.m();
        }

        this.mPage = null;
    }

    public void abort() {
        Utilities.dismissCurrentAlert();
        this.mOpen = false;
        this.mListener = null;
        this.mListener2 = null;
        this.mListenerCustom = null;
        SODoc var1 = this.mDoc;
        if (var1 != null) {
            var1.abortLoad();
        }

        this.a();
    }

    public void createThumbnail(SOFileState var1) {
        String var2 = var1.getThumbnail();
        if (var2 == null || var2.isEmpty()) {
            var2 = SOFileDatabase.uniqueThumbFilePath();
        }

        var1.setThumbnail(var2);
        var1.deleteThumbnailFile();
        this.mPage = this.getDoc().getPage(0, new SOPageListener() {
            public void update(RectF var1) {
            }
        });
        int var3 = (int)this.mActivity.getResources().getDimension(dimen.sodk_editor_thumbnail_size);
        PointF var4 = this.mPage.zoomToFitRect(var3, 1);
        double var5 = (double)Math.max(var4.x, var4.y);
        Point var8 = this.mPage.sizeAtZoom(var5);
        final SOBitmap var9 = k.a(var1.getInternalPath(), var8.x, var8.y);
        PointF var7 = new PointF(0.0F, 0.0F);
        String finalVar = var2;
        this.mRender = this.mPage.a(var5, var7, var9, new o() {
            public void a(int var1) {
                Bitmap var2x = var9.a();
                n var3 = new n(finalVar);

                try {
                    var2x.compress(CompressFormat.PNG, 80, var3);
                    var3.close();
                } catch (Exception var4) {
                    var4.printStackTrace();
                }

                SODocSession.this.a();
            }
        }, false);
    }

    public void destroy() {
        this.abort();
        SODoc var1 = this.mDoc;
        if (var1 != null) {
            var1.l();
            this.mDoc = null;
        }

    }

    public void endSession(boolean var1) {
        SODocSession.SODocSessionLoadListenerCustom var2 = this.mListenerCustom;
        if (var2 != null) {
            var2.onSessionComplete(var1);
            this.mListenerCustom = null;
        }

        this.abort();
    }

    public SODoc getDoc() {
        return this.mDoc;
    }

    public SOFileState getFileState() {
        return this.mFileState;
    }

    public String getUserPath() {
        return this.mUserPath;
    }

    public boolean isCancelled() {
        return this.mCancelled;
    }

    public boolean isOpen() {
        return this.mOpen;
    }

    public void open(String var1) {
        this.mUserPath = var1;
        this.mPageCount = 0;
        this.mCompleted = false;
        this.mCancelled = false;
        this.mLoadError = false;
        this.mOpen = true;
        this.mDoc = this.mLibrary.a(var1, new l() {
            public void a() {
                if (SODocSession.this.mOpen && !SODocSession.this.mCancelled && !SODocSession.this.mLoadError) {
                    SODocSession.this.mCompleted = true;
                    if (SODocSession.this.mListener != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener.onDocComplete();
                    }

                    if (SODocSession.this.mListener2 != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener2.onDocComplete();
                    }

                    if (SODocSession.this.mListenerCustom != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListenerCustom.onDocComplete();
                    }
                }

            }

            public void a(int var1) {
                StringBuilder var2 = new StringBuilder();
                var2.append("open_SODocSession: ");
                var2.append(SODocSession.this.mOpen);
                var2.append(" \n");
                var2.append(SODocSession.this.mCancelled);
                var2.append("\n");
                var2.append(SODocSession.this.mLoadError);
                Log.e("SODocSession", var2.toString());
                if (SODocSession.this.mOpen && !SODocSession.this.mCancelled) {
                    SODocSession var3 = SODocSession.this;
                    var3.mPageCount = Math.max(var1, var3.mPageCount);
                    if (SODocSession.this.mListener != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener.onPageLoad(var1);
                    }

                    if (SODocSession.this.mListener2 != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener2.onPageLoad(var1);
                    }

                    if (SODocSession.this.mListenerCustom != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListenerCustom.onPageLoad(var1);
                    }
                }

            }

            public void a(int var1, int var2) {
                if (var1 == 4096) {
                    Utilities.passwordDialog(SODocSession.this.mActivity, new passwordDialogListener() {
                        public void onCancel() {
                            SODocSession.this.mDoc.abortLoad();
                            if (SODocSession.this.mListener != null && SODocSession.this.mOpen) {
                                SODocSession.this.mListener.onCancel();
                            }

                            if (SODocSession.this.mListener2 != null && SODocSession.this.mOpen) {
                                SODocSession.this.mListener2.onCancel();
                            }

                            if (SODocSession.this.mListenerCustom != null && SODocSession.this.mOpen) {
                                SODocSession.this.mListenerCustom.onCancel();
                            }

                            SODocSession.this.mCancelled = true;
                        }

                        public void onOK(String var1) {
                            SODocSession.this.mDoc.providePassword(var1);
                            password = var1;
                        }
                    });
                } else {
                    SODocSession.this.mLoadError = true;
                    if (SODocSession.this.mListener != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener.onError(var1, var2);
                    }

                    if (SODocSession.this.mListener2 != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener2.onError(var1, var2);
                    }

                    if (SODocSession.this.mListenerCustom != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListenerCustom.onError(var1, var2);
                    }

                    SODocSession.this.mOpen = false;
                }

            }

            public void b() {
                if (SODocSession.this.mOpen) {
                    if (SODocSession.this.mListener != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener.onLayoutCompleted();
                    }

                    if (SODocSession.this.mListener2 != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener2.onLayoutCompleted();
                    }

                    if (SODocSession.this.mListenerCustom != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListenerCustom.onLayoutCompleted();
                    }
                }

            }

            public void b(int var1, int var2) {
                if (SODocSession.this.mOpen) {
                    if (SODocSession.this.mListener != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener.onSelectionChanged(var1, var2);
                    }

                    if (SODocSession.this.mListener2 != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListener2.onSelectionChanged(var1, var2);
                    }

                    if (SODocSession.this.mListenerCustom != null && SODocSession.this.mOpen) {
                        SODocSession.this.mListenerCustom.onSelectionChanged(var1, var2);
                    }
                }

            }
        }, this.mActivity);
    }

    public void setFileState(SOFileState var1) {
        this.mFileState = var1;
    }

    public void setSODocSessionLoadListener(SODocSession.SODocSessionLoadListener var1) {
        this.mListener = var1;
        int var2;
        if (var1 != null) {
            var2 = this.mPageCount;
            if (var2 > 0) {
                var1.onPageLoad(var2);
            }

            if (this.mCompleted) {
                this.mListener.onDocComplete();
            }
        }

        SODocSession.SODocSessionLoadListenerCustom var3 = Utilities.getSessionLoadListener();
        this.mListenerCustom = var3;
        if (var3 != null) {
            var2 = this.mPageCount;
            if (var2 > 0) {
                var3.onPageLoad(var2);
            }

            if (this.mCompleted) {
                this.mListenerCustom.onDocComplete();
            }
        }

        StringBuilder var4 = new StringBuilder();
        var4.append("setSODocSessionLoadListener: ");
        var4.append(var1);
        var4.append(" var3: ");
        var4.append(Utilities.getSessionLoadListener());
        Log.e("SODocSession", var4.toString());
    }

    public void setSODocSessionLoadListener2(SODocSession.SODocSessionLoadListener var1) {
        this.mListener2 = var1;
    }

    public interface SODocSessionLoadListener {
        void onCancel();

        void onDocComplete();

        void onError(int var1, int var2);

        void onLayoutCompleted();

        void onPageLoad(int var1);

        void onSelectionChanged(int var1, int var2);
    }

    public interface SODocSessionLoadListenerCustom extends SODocSession.SODocSessionLoadListener {
        void onSessionComplete(boolean var1);

        void onSessionReject();
    }
}
