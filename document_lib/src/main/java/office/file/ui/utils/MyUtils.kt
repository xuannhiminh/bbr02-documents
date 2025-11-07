package office.file.ui.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import office.file.ui.R
import java.util.*

class MyUtils {
    companion object {
        fun shareText(context: Context, text: String){
            try {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                val shareBody = text
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Office Reader")
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                context.startActivity(Intent.createChooser(sharingIntent, "Share to"))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        fun openStorageAccess(activity: Activity, CODE_PMS: Int) {
            try {

                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", activity.packageName))
                activity.startActivityForResult(intent, CODE_PMS)
            } catch (e: Exception) {

                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                activity.startActivityForResult(intent, CODE_PMS)
            }
        }

        fun feedback(context: Context, content: String) {
            context.startActivity(
                createEmailIntent(
                    context,
                    content
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }


        private fun capitalize(s: String?): String {
            if (s == null || s.length == 0) {
                return ""
            }
            val first = s[0]
            return if (Character.isUpperCase(first)) {
                s
            } else {
                Character.toUpperCase(first).toString() + s.substring(1)
            }
        }

        fun createEmailIntent(context: Context, content: String): Intent {
            val toEmail = context.getString(R.string.EMAIL_DEVELOPER)
            val release = Build.VERSION.RELEASE
            val SDK = Build.VERSION.SDK_INT
            val yourName = "customer"
            val phoneName = getDeviceName()
            val manager = context.packageManager
            var info: PackageInfo? = null
            try {
                info = manager.getPackageInfo(
                    context.packageName, 0
                )
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            val version1 = info!!.versionName
            val subject = context.getString(R.string.sodk_app_name) + " feedback:"
            val message =
                """
        --------------------
        Device information:
        
        Phone name: $phoneName
        API Level: $SDK
        Version: $release
        App version: $version1
        Username: $yourName
        --------------------
        
        Content: 
        
        $content
        """.trimIndent()
            val sendTo = Intent(Intent.ACTION_SENDTO)
            val uriText = "mailto:" + Uri.encode(toEmail) +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(message)
            val uri = Uri.parse(uriText)
            sendTo.data = uri
            val resolveInfos =
                context.packageManager.queryIntentActivities(sendTo, 0)

            // Emulators may not like this check...
            if (!resolveInfos.isEmpty()) {
                return sendTo
            }

            // Nothing resolves send to, so fallback to send...
            val send = Intent(Intent.ACTION_SEND)
            send.type = "text/plain"
            send.putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
            send.putExtra(Intent.EXTRA_SUBJECT, subject)
            send.putExtra(Intent.EXTRA_TEXT, message)
            return Intent.createChooser(send, "Send feedback for developer")
        }

        fun hasPermission(
            activity: Activity,
            strArr: Array<String>
        ): Boolean {
            val arrayList = ArrayList<Any>()
            for (str in strArr) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        str
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    arrayList.add(str)
                }
            }
            return arrayList.isEmpty()
        }

        fun shouldShowRequestPermissionRationale(
            activity: Activity,
            strArr: Array<String>
        ): Boolean {
            val arrayList = ArrayList<Any>()
            for (str in strArr) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, str)) {
                    arrayList.add(str)
                }
            }
            return arrayList.isEmpty()
        }

        fun checkPermissionAndroidR(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                false
            }
        }

        fun showPermissionBlockDialog(context: Activity, CODE_PMS: Int) {


        }

        fun openAppSetting(context: Activity, requestCode: Int) {
            val myAppSettings = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.packageName)
            )
            myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
            myAppSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(myAppSettings)
        }

        fun openPermissionAndroidR(activity: Activity, CODE_PMS: Int) {
            try {

                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", activity.packageName))
                activity.startActivityForResult(intent, CODE_PMS)
            } catch (e: Exception) {

                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                activity.startActivityForResult(intent, CODE_PMS)
            }

        }
    }
}