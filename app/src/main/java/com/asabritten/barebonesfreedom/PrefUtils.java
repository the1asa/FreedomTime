package com.asabritten.barebonesfreedom;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by t410 on 1/31/2016.
 */
public class PrefUtils
{
    public static final String USERNAME = "";
    public static final String PASSWORD = "";

    /**
     * Called to save supplied value in shared preferences against given password.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param value   Value to save
     */
    public static void saveToPrefs(Context context, String key, String value)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Called to retrieve required value from shared preferences, identified by given password.
     * Default value will be returned of no value found or error occurred.
     *
     * @param context      Context of caller activity
     * @param key          Key to find value against
     * @param defaultValue Value to return if no data found against given password
     * @return Return the value found against given password, default if not found or any error occurs
     */
    public static String getFromPrefs(Context context, String key, String defaultValue)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try
        {
            return sharedPrefs.getString(key, defaultValue);
        } catch (Exception e)
        {
            e.printStackTrace();
            return defaultValue;
        }
    }
}
