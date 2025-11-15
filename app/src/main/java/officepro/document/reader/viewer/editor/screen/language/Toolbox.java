package office.pdf.document.reader.viewer.editor.screen.language;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;

import office.pdf.document.reader.viewer.editor.R;

import java.util.Locale;

public class Toolbox {
    public static int getFlagCountry(String code) {
        switch (code) {
            case "ar":
                return R.drawable.flag_ar;
            case "en-UK":
                return R.drawable.flag_uk;
            case "en-US":
                return R.drawable.flag_us;
            case "en-AU":
                return R.drawable.flag_aus;
            case "en-IN":
                return R.drawable.flag_hi;
            case "es":
                return R.drawable.flag_es;
            case "fr":
                return R.drawable.flag_fr;
            case "hi":
                return R.drawable.flag_hi;
            case "in":
                return R.drawable.flag_id;
            case "ja":
                return R.drawable.flag_jp;
            case "ko":
                return R.drawable.flag_ko;
            case "pt":
                return R.drawable.flag_pt;
            case "ru":
                return R.drawable.flag_ru;
            case "tr":
                return R.drawable.flag_tr;
            case "vi":
                return R.drawable.flag_vi;
            case "zh":
                return R.drawable.flag_cn;
            case "zh-TW":
                return R.drawable.flag_cn;
            case "fa":
                return R.drawable.flag_ir;
            case "nl":
                return R.drawable.flag_nl;
            case "uk":
                return R.drawable.flag_ua;
            case "iw-IL":
                return R.drawable.flag_he;
            case "it":
                return R.drawable.flag_it;
            case "fr-CA":
                return R.drawable.flag_ca;
            case "de":
                return R.drawable.flag_de;
            case "es-US":
                return R.drawable.flag_us;
            case "es-MX":
                return R.drawable.flag_latam;
            case "pt-BR":
                return R.drawable.flag_br;
            default:
                return 0;
        }
    }

    public static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
