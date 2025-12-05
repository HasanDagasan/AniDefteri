package com.hasandagasan.anidefteri;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String langCode = prefs.getString("selectedLanguage", null);
            if (langCode != null) {
                LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langCode);
                AppCompatDelegate.setApplicationLocales(appLocale);
            }

    }
}
    