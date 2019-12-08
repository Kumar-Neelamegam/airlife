package at.jku.mobilecomputing.airlife.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import at.jku.mobilecomputing.airlife.DomainObjects.Data;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefUtils {
    private static final String SHARED_PREF_NAME = "airlifeshared";
    private static SharedPrefUtils INSTANCE = null;
    private SharedPreferences preferences;

    public static SharedPrefUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SharedPrefUtils(context);
            return INSTANCE;
        }
        return INSTANCE;
    }

    private SharedPrefUtils(Context context) {
        preferences = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    }

    public void saveLatestAQI(String location) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Aqi", location);
        editor.apply();
    }

    public String getLatestAQI() {
        return preferences.getString("Aqi", "");
    }


    public void saveLatestTemp(String temperature) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("temperature", temperature);
        editor.apply();
    }

    public String getLatestTemp() {
        return preferences.getString("temperature", "");
    }



    public void isDarkMode(Boolean b) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("darkMode", b);
        editor.apply();
    }

    public Boolean isDarkMode() {
        return preferences.getBoolean("darkMode", false);
    }

    public void rateCardDone(Boolean b) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("rateCard", b);
        editor.apply();
    }

    public void setAppInstallTime(Long time) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("appInstallTime", time);
        editor.apply();
    }

    public Long getAppInstallTime() {
        return preferences.getLong("appInstallTime", 0);
    }

    public void clearAllPrefs() {
        preferences.edit().clear().apply();
    }
}
