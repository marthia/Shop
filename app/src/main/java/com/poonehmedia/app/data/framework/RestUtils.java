package com.poonehmedia.app.data.framework;

import android.net.Uri;
import android.net.UrlQuerySanitizer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.BuildConfig;
import com.poonehmedia.app.CrashReportException;

import org.acra.ACRA;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RestUtils {

    /**
     * This class is for parsing urls. it has support for query parameters and also appending query parameters
     * from a jsonObject.
     */
    @Inject
    public RestUtils() {
    }

    /**
     * @param path path to be appended with project level base url.
     * @return String full link.
     */
    public String resolveUrl(String path) {
        return resolveUrl(path, new JsonObject());
    }


    /**
     * @param path   path to be appended with project level base url. could has query parameters itself:
     *               in that case the params from JsonObject argument have a higher priority as it might
     *               have been changed by user to reflect a change in data.
     * @param params JsonObject key value pairs of params to be used as query parameters.
     *               this could be empty for appending no query parameters.
     * @return String full link with appended query parameters if provided.
     * @author Marthia
     */
    public String resolveUrl(String path, JsonObject params) {
        try {
            Uri.Builder builtUri = Uri.parse(BuildConfig.baseUrl + Uri.parse(BuildConfig.baseUrl + path).getPath().replaceAll("^/*", "")).buildUpon();

            UrlQuerySanitizer querySanitizer = new UrlQuerySanitizer(BuildConfig.baseUrl + path);
            List<UrlQuerySanitizer.ParameterValuePair> parameterList = querySanitizer.getParameterList();

            for (int i = 0; i < parameterList.size(); i++) {
                UrlQuerySanitizer.ParameterValuePair pair = parameterList.get(i);
                if (!params.has(pair.mParameter)) // prioritize params over static path query params
                    params.addProperty(pair.mParameter, pair.mValue);
            }

            if (params.size() > 0) {
                for (Map.Entry<String, JsonElement> entry : params.entrySet()) {
                    builtUri.appendQueryParameter(entry.getKey(), entry.getValue().getAsString());
                }
            }

            return builtUri.toString();

        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("An Error occured while resolving link: " + path, e));
            return null;
        }
    }

}
