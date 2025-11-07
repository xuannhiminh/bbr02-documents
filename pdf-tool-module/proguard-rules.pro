# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.ezteam.baseproject.EzListener
-dontwarn com.ezteam.baseproject.activity.BaseActivity
-dontwarn com.ezteam.baseproject.adapter.BaseRecyclerAdapter
-dontwarn com.ezteam.baseproject.dialog.BaseDialog
-dontwarn com.ezteam.baseproject.dialog.BuilderDialog
-dontwarn com.ezteam.baseproject.extensions.BitmapExtensionKt
-dontwarn com.ezteam.baseproject.extensions.FileExtensionsKt
-dontwarn com.ezteam.baseproject.photopicker.PickImageActivity$Companion
-dontwarn com.ezteam.baseproject.utils.DateUtils
-dontwarn com.ezteam.baseproject.utils.FileSaveManager
-dontwarn com.ezteam.baseproject.utils.IAPUtils
-dontwarn com.ezteam.baseproject.utils.KeyboardUtils
-dontwarn com.ezteam.baseproject.utils.PathUtils
-dontwarn com.ezteam.baseproject.utils.ViewUtils
-dontwarn com.ezteam.baseproject.viewmodel.BaseViewModel
-dontwarn java.lang.invoke.StringConcatFactory