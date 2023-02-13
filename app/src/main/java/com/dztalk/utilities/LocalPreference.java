package com.dztalk.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.dztalk.models.UserProperties;

public class LocalPreference {
    private final SharedPreferences sharedPreferences;

    public LocalPreference(Context context){
        sharedPreferences = context.getSharedPreferences(UserProperties.KEY_PREFERENCE, Context.MODE_PRIVATE);
    }

    /**
     * Put string value into local preference
     * @param key
     * @param value
     */
    public void putString(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Put boolean value into local preference
     * @param key
     * @param value
     */
    public void putBoolean(String key, Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Get String value from local preference
     * @param key
     * @return
     */
    public String getString(String key){
        return sharedPreferences.getString(key,null);
    }

    /**
     * Get Boolean value from local preference
     * @param key
     * @return
     */
    public Boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * Clear the local preference
     */
    public void clear(){
        sharedPreferences.edit().clear();
        sharedPreferences.edit().apply();
    }
}
