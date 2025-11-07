package com.ezteam.ezpdflib.activity


import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.print.PrintManager
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate
import com.ezteam.baseproject.utils.permisson.PermissionUtils
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.dialog.AlertDialog
import com.ezteam.ezpdflib.dialog.DetailFileInPDFDetailDialog
import com.ezteam.ezpdflib.dialog.DialogInputName
import com.ezteam.ezpdflib.dialog.DialogLoading
import com.ezteam.ezpdflib.dialog.InputPasswordDialog
import com.ezteam.ezpdflib.model.SingleSize
import com.ezteam.ezpdflib.util.FileSaveManager
import com.ezteam.ezpdflib.util.PathUtils
import com.ezteam.ezpdflib.util.PdfUtils
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.ezteam.ezpdflib.util.Utils
import com.ezteam.ezpdflib.util.print.PdfDocumentAdapter
import com.ezteam.ezpdflib.viewmodel.DetailViewmodel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import java.io.File


open class BasePdfViewerActivity : AppCompatActivity() {

    private var lastClickTime: Long = 0
    private var progressDialog: DialogLoading? = null

    val viewmodel: DetailViewmodel by lazy {
        ViewModelProvider(this).get(DetailViewmodel::class.java)
    }
    private val localizationDelegate: LocalizationActivityDelegate by lazy {
        LocalizationActivityDelegate(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge();
        localizationDelegate.onCreate()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        // Set the behavior to allow transient bars to be revealed by swipe
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        val rootView: View = findViewById(android.R.id.content)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = insets.top,
                bottom = insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun attachBaseContext(newBase: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            applyOverrideConfiguration(localizationDelegate.updateConfigurationLocale(newBase))
            super.attachBaseContext(newBase)
        } else {
            super.attachBaseContext(localizationDelegate.attachBaseContext(newBase))
        }
    }

    override fun onResume() {
        super.onResume()
        localizationDelegate.onResume(this)
    }

    protected open fun isAcceptManagerStorage(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            PermissionUtils.checkPermissonAccept(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun showDialogPassword(message: String?, responseListener: (String?) -> Unit) {
        InputPasswordDialog.ExtendBuilder(this)
            .setCancelable(false)
            .setTitle(message)
            .onSetPositiveButton(
                resources.getString(R.string.ok)
            ) { it, data ->
                data[InputPasswordDialog.INPUT_PASSWORD]?.let {
                    responseListener(it.toString())
                } ?: run {
                    responseListener(null)
                }
                it.dismiss()
            }
            .onSetNegativeButton(
                resources.getString(R.string.cancel)
            ) {
                it.dismiss()
                responseListener(null)
            }
            .build()
            .show()
    }

    fun showDialogEditPassword(
        file: File,
        isRemovePass: Boolean,
        responseListener: (String?) -> Unit
    ) {
        showDialogPassword(if (isRemovePass) {
            getString(R.string.remove_password)
        } else {
            getString(R.string.add_password)
        }, responseListener = { password ->
            if (password == null)
                return@showDialogPassword
            showProgressDialog()
            GlobalScope.launch(Dispatchers.IO) {
                if (!password.isNullOrEmpty()) {
                    val pathNew: String = if (isRemovePass) {
                        PdfUtils.removePassword(
                            this@BasePdfViewerActivity,
                            file.path,
                            password
                        )
                    } else {
                        PdfUtils.doEncryption(this@BasePdfViewerActivity, file.path, password)
                    }
                    if (pathNew.isEmpty()) {
                        if (isRemovePass) {
                            toast(getString(R.string.remove_password_error))
                        } else {
                            toast(getString(R.string.add_password_error))
                        }
                    } else {
                        FileSaveManager.deleteFile(this@BasePdfViewerActivity, file.path) {
                            val uri: Uri? = FileSaveManager.saveFileStorage(
                                this@BasePdfViewerActivity,
                                pathNew,
                                file.parent,
                                FilenameUtils.getBaseName(file.path)
                            )
                            val newRealPath: String? = uri?.let {
                                PathUtils.getPath(this@BasePdfViewerActivity, uri)
                            }
                            GlobalScope.launch(Dispatchers.Main) {
                                responseListener(newRealPath)
                            }
                        }
                        GlobalScope.launch(Dispatchers.Main) {
                            if (isRemovePass) {
                                toast(getString(R.string.remove_password_success))
                            } else {
                                toast(getString(R.string.add_password_success))
                            }
                        }
                    }
                    hideProgressDialog()
                }
            }
        })
    }

    fun addPasswordBackground(file: File, inputPass: String, responseListener: (String?) -> Unit) {
        val pathNew = PdfUtils.doEncryption(this, file.path, inputPass)
        if (!TextUtils.isEmpty(pathNew)) {
            FileSaveManager.deleteFile(this, file.path) {
                val uri: Uri? = FileSaveManager.saveFileStorage(
                    this,
                    pathNew,
                    file.parent,
                    FilenameUtils.getBaseName(file.path)
                )
                val newRealPath: String? = uri?.let {
                    PathUtils.getPath(this, uri)
                }
                responseListener(newRealPath)
            }
        } else {
            responseListener(null)
        }
    }

    fun showDialogConfirm(title: String, message: String, onConfirm: (Boolean) -> Unit) {
        AlertDialog.ExtendBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .onSetPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                onConfirm(true)
            }
            .onSetNegativeButton(resources.getString(R.string.no)) {
                onConfirm(false)
            }
            .build()
            .show()
    }

    open fun aVoidDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return true
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return false
    }

    fun toast(content: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            if (!TextUtils.isEmpty(content)) Toast.makeText(this@BasePdfViewerActivity, content, Toast.LENGTH_SHORT).show()
        }
    }

    open fun initView() {
        PreferencesUtils.init(this)

        viewmodel.isLoading.observe(this) {
            if (it == true) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        }
        SingleSize.getInstance().screenW = Utils.getWidthScreen(this)
        SingleSize.getInstance().screenH = (Utils.getWidthScreen(this) * 1.414f).toInt()
    }

    private fun showProgressDialog() {
        progressDialog ?: let {
            progressDialog = DialogLoading.ExtendBuilder(this)
                .setCancelable(false)
                .setCanOntouchOutside(false)
                .build() as DialogLoading
            progressDialog?.show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            progressDialog = null
        }
    }

    fun printDoc(filePath: String, password: String?) {
        if (password == null || TextUtils.isEmpty(password)) {
            callPrint(filePath)
        } else {
            CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
                val filePrint = PdfUtils.removePasswordForPrint(
                    this@BasePdfViewerActivity,
                    filePath,
                    password
                )
                callPrint(filePrint)
            }
        }
    }

    private fun callPrint(filePrint: String) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = getString(R.string.app_name)
        try {
            printManager.print(
                jobName, PdfDocumentAdapter(this, File(filePrint)), null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        var context: Context? = applicationContext
//        while (context is ContextWrapper && context !is Activity) {
//            context = context.baseContext
//        }
//        if (context is Activity) {
//            val activity = context
//            val printManager = activity.getSystemService(PRINT_SERVICE) as PrintManager
//            val jobName = getString(com.ezteam.ezpdflib.R.string.app_name) + " Document"
//            try{
//                printManager.print(
//                    jobName,
//                    PdfDocumentAdapter(activity,  File(filePrint)),
//                    PrintAttributes.Builder().build()
//                )
//            }catch (e: Exception) {
////            e.printStackTrace()
//        }
//        } else {
//            // Handle the case where the context is not an Activity
//            Log.e("PrintError", "Unable to retrieve Activity context for printing.")
//        }
    }

    protected open fun showDetail(pathFile: String) {
//        val message = File(pathFile).infoDetail
//        DetailDialog.ExtendBuilder(this)
//            .setPathFile(pathFile)
//            .setTitle(resources.getString(R.string.properties))
//            .setMessage(
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT)
//                } else {
//                    Html.fromHtml(message)
//                }.toString()
//            )
//            .setCanOntouchOutside(true)
//            .onSetPositiveButton(
//                resources.getString(R.string.ok)
//            ) { baseDialog, _ ->
//                baseDialog.dismiss()
//            }
//            .build()
//            .show()

        DetailFileInPDFDetailDialog(file = File(pathFile)).show(
            supportFragmentManager,
            "DetailFileDialog"
        )
    }

    protected open fun showPopupEdit(
        fileInfo: String,
        responseListener: (String?) -> Unit
    ) {
        DialogInputName.ExtendBuilder(this)
            .setInputValue(FilenameUtils.getBaseName(fileInfo))
            .setLabel(getString(R.string.enter_file_name))
            .setTitle(getString(R.string.rename))
            .setCanOntouchOutside(false)
            .onSetPositiveButton(getString(R.string.ok)) { baseDialog, datas ->
                val newName =
                    datas[DialogInputName.DATA_INPUT] as String?
                responseListener(newName)
                baseDialog.dismiss()
            }
            .onSetNegativeButton(getString(R.string.cancel)) {
                it.dismiss()
            }
            .onDismissListener {}
            .build()
            .show()
    }
}