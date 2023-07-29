package com.poonehmedia.app.util.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    /**
     * This method should only be used in Activity's attachBaseContext to override baseContext
     *
     * @param context  base context
     * @param language standard language code
     * @return Context with injected language configuration
     * @author Marthia
     */
    public static Context getLanguageAwareContext(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(language);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setSystemLocale(configuration, locale);
        } else {
            setSystemLocaleLegacy(configuration, locale);
        }
        configuration.setLayoutDirection(locale);
        return context.createConfigurationContext(configuration);
    }

    /**
     * This method only works on pre-Nougat Android versions
     *
     * @param config {@link Configuration} file
     * @param locale a custom locale
     * @author Marthia
     * @see LocaleHelper#setSystemLocale(Configuration, Locale)
     */
    public static void setSystemLocaleLegacy(Configuration config, Locale locale) {
        config.locale = locale;
    }

    /**
     * This is the new api for updating System Configuration
     *
     * @param config {@link Configuration} file
     * @param locale a custom locale
     * @author Marthia
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static void setSystemLocale(Configuration config, Locale locale) {
        config.setLocale(locale);
    }
}