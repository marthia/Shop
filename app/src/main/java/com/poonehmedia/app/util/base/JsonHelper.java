package com.poonehmedia.app.util.base;

import android.util.Log;

import com.google.gson.JsonElement;

public class JsonHelper {
    private static final String TAG = JsonHelper.class.getSimpleName();

    /**
     * This method checks for existence of a key in any {@link JsonElement}.
     *
     * @param element any {@link JsonElement}
     * @param key     string key of json objects
     * @return true if and only if the provided element: <ul>
     * <li> Is a {@link com.google.gson.JsonObject}
     * <li> Has the key
     * </ul>
     * false otherwise
     * @author Marthia
     */
    public static boolean has(JsonElement element, String key) {
        try {
            if (element == null || element.isJsonNull()) return false;
            if (element.isJsonArray() && element.getAsJsonArray().size() == 0) return false;
            return element.isJsonObject() && element.getAsJsonObject().has(key);
        } catch (Exception e) {
            Log.e("jsonHas", e.getMessage());
            return false;
        }
    }

    public static boolean isNotEmptyNorNull(JsonElement element) {
        try {
            if (element == null)
                return false;
            else if (element.isJsonNull())
                return false;
            else if (element.isJsonPrimitive()) {
                return !element.getAsString().isEmpty();
            } else if (element.isJsonArray()) {
                return element.getAsJsonArray().size() != 0;
            } else if (element.isJsonObject()) {
                return element.getAsJsonObject().size() != 0;
            }
            return false;
        } catch (Exception e) {
            Log.e("jsonEmpty", e.getMessage());
            return false;
        }
    }

    public static boolean isEmptyOrNull(JsonElement element) {
        return !isNotEmptyNorNull(element);
    }

    /**
     * @param element JsonElement to look into
     * @param key     field to return
     * @return String value of the requested field if is available, not null and not empty
     * @author Marhia
     */
    public static String tryGetString(JsonElement element, String key) {
        try {
            JsonElement item = element.getAsJsonObject().get(key);

            if (isEmptyOrNull(item))
                return "";
            else return item.getAsString();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }
}
