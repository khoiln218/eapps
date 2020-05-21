package vn.gomisellers.apps.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefManager {
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(LiveConstants.PREF_NAME, Context.MODE_PRIVATE);
    }
}
