package officepro.document.reader.viewer.editor.screen.language;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class LocaleManager {

    public static final String LANGUAGE_DEFAULT = "en";
    private static final String LANGUAGE_KEY = "KEY_LANGUAGE";
    private static LocaleManager localeManager;
    private final SharedPreferences prefs;
    private Context context;

    public static LocaleManager getInstance(Context context) {
        if (localeManager == null) {
            localeManager = new LocaleManager(context);
        }
        return localeManager;
    }

    public LocaleManager(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Context setLocale(Context context) {
        return updateResources(context, getPrefLanguage());
    }

    public Context setNewLocale(Context context, String language) {
        setPrefLanguage(language);
        return updateResources(context, language);
    }

    public static Context setLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        context = context.createConfigurationContext(config);
        return context;
    }

    public String getPrefLanguage() {
        return prefs.getString(LANGUAGE_KEY, LANGUAGE_DEFAULT);
    }

    @SuppressLint("ApplySharedPref")
    public void setPrefLanguage(String language) {
        prefs.edit().putString(LANGUAGE_KEY, language).commit();
    }

    private Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        config.setLocale(locale);
        config.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(config);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setLocaleForApi24(Configuration config, Locale target) {
        Set<Locale> set = new LinkedHashSet<>();
        set.add(target);
        LocaleList all = LocaleList.getDefault();
        for (int i = 0; i < all.size(); i++) {
            set.add(all.get(i));
        }
        Locale[] locales = set.toArray(new Locale[0]);
        config.setLocales(new LocaleList(locales));
    }

    public static String[] lstLanguage = new String[]{
            "English (US)",
            "English (UK)",
            "English (Australia)",
            "English (India)",
            "Português",
            "Tiếng Việt",
            "русский",
            "हिन्दी",
            "日本語",
            "한국어",
            "Türk",
            "French",
            "Spanish",
            "简体中文",
            "繁體中文",
            "فارسی",
            "Netherlands",
            "Українська",
            "Italiano",
            "Français (Canada)",
            "Deutsch",
            "עִבְרִית",
            "Español (US)",
            "Español (Latinoamérica)",
            "Português (Brasil)"
    };

    public static String[] lstCodeLanguage = new String[]{
            "en-US",
            "en-UK",
            "en-AU",
            "en-IN",
            "pt",
            "vi",
            "ru",
            "hi",
            "ja",
            "ko",
            "tr",
            "fr",
            "es",
            "zh",
            "zh-TW",
            "fa",
            "nl",
            "uk",
            "it",
            "fr-CA",
            "de",
            "iw-IL",
            "es-US",
            "es-MX",
            "pt-BR"
    };

    public void restart(Activity activity) {
        Intent i = new Intent(activity, activity.getClass());
        activity.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}

