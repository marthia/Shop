package com.poonehmedia.app.data.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class PreferenceManager {
    private final SharedPreferences preference;
    private final SharedPreferences.Editor editor;


    /**
     * Main class for working with sharedPreference. this is the only class responsible for manipulating
     * any sharedPreference data.
     */
    @SuppressLint("CommitPrefEdits")
    @Inject
    public PreferenceManager(@ApplicationContext Context context) {
        preference = context.getSharedPreferences("base_pref", Context.MODE_PRIVATE);
        editor = preference.edit();
    }

    public void saveLanguage(String lang) {
        editor.putString("lang", lang);
        editor.commit();
    }

    /**
     * @return app wide language specified from api. default is persian (fa-IR).
     * @author Marthia
     */
    public String getLanguage() {
        return preference.getString("lang", "fa-IR");
    }

    /**
     * @return latest valid token specified by server. expiration occurs every 30 minutes (unconfirmed).
     * @author Marthia
     */
    public String getToken() {
        return preference.getString("token", "");
    }

    public void setToken(String token) {
        editor.putString("token", token).apply();
    }

    public void clearAll() {
        preference.getAll().clear();
    }

    /**
     * @return updated current username. this is only retrieved by response params.
     * @author Marthia
     */
    public String getUser() {
        return preference.getString("username", "");
    }

    public void setUser(String username) {
        editor.putString("username", username).apply();
    }

    public String getFullName() {
        return preference.getString("fullName", "");
    }

    public void setFullName(String fullName) {
        editor.putString("fullName", fullName).apply();
    }

    /**
     * clear user profile info: `username` and `fullname`
     */
    public void clearUser() {
        editor.putString("username", "").apply();
        editor.putString("fullName", "").apply();
    }

    /**
     * @param forceRedirect JsonObject containing a future navigation redirect that we must handle
     *                      in the next reopening of the app. used to return to same step in cart when
     *                      user was not already logged in and in ProductFragment where we need to
     *                      user wishList or waitList features.
     * @author Marthia
     */
    public void saveReturn(JsonObject forceRedirect) {
        editor.putString("return", forceRedirect.toString()).apply();
    }

    /**
     * @return latest saved return object or null if not specified.
     * @author Marthia
     */
    public JsonObject getReturn() {
        String str = preference.getString("return", null);
        if (str != null) {
            return JsonParser.parseString(str).getAsJsonObject();
        }

        return null;
    }

    public void clearReturn() {
        editor.remove("return").commit();
    }

    /**
     * @return user configurable setting for logging requests. this is used for server to use special logging
     * mechanisms for a better debugging.
     * @author Marthia
     */
    public boolean getDebuggingState() {
        return preference.getBoolean("isDebug", false);
    }

    public void setDebuggingState(boolean state) {
        editor.putBoolean("isDebug", state).apply();
    }


    /**
     * This is only used for debugging.
     *
     * @param request complete toString() value of a request
     * @author Marthia
     */
    public void saveRequest(String request) {
        editor.putString("request", request).commit();
    }

    public String getLastRequest() {
        return preference.getString("request", "");
    }

    public void saveSessionId(String sessionId) {
        editor.putString("sessionId", sessionId).apply();
    }

    public String getSessionId() {
        return preference.getString("sessionId", "");
    }
}
