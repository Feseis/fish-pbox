package com.feseis.phonebox.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPrefs {

    private static final String FILE_NAME = "pbox";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_PHONE = "phone";

    private final SharedPreferences prefs;

    public AppPrefs(Context context) {
        this.prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getBaseUrl() {
        return prefs.getString(KEY_BASE_URL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public void save(String baseUrl, String phone) {
        prefs.edit()
                .putString(KEY_BASE_URL, baseUrl)
                .putString(KEY_PHONE, phone)
                .apply();
    }
}
