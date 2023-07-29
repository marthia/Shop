package com.poonehmedia.app.util.base;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Consumer;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.AppParams;
import com.poonehmedia.app.data.model.Comment;
import com.poonehmedia.app.data.model.CommentResponse;
import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.ui.interfaces.OnResponseCallback;
import com.poonehmedia.app.util.ui.AndroidUtils;

import org.acra.ACRA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.OkHttpClient;
import retrofit2.Response;

/**
 * singleton class used to access common code for working with data
 *
 * @author Marthia
 */

@Singleton
public class DataController {

    private final String TAG = getClass().getSimpleName();

    private final Context context;


    private final PreferenceManager preferenceManager;

    /**
     * injected okhttp client used mainly for session invalidation.
     */
    private final OkHttpClient okHttpClient;

    /**
     * injected gson object to be used in converting Json types to java class.
     */
    private final Gson gson;

    /**
     * interface used to show server messages (error or success) in snackBar. this is used in {@link com.poonehmedia.app.ui.base.MainActivity}.
     */
    private OnResponseCallback uiMessagesResponseCallback;
    private Consumer<Boolean> invalidateSession;

    @Inject
    public DataController(@ApplicationContext Context context, Gson gson, PreferenceManager preferenceManager, OkHttpClient okHttpClient) {
        this.context = context;
        this.gson = gson;
        this.preferenceManager = preferenceManager;
        this.okHttpClient = okHttpClient;
    }

    /**
     * @return reversed JsonArray. used to conveniently invert any list. rather than using any special
     * ui technique
     * @author Marthia
     */
    public static JsonArray reverse(JsonArray array) {
        JsonArray newJsonArray = new JsonArray();
        for (int i = array.size() - 1; i >= 0; i--)
            newJsonArray.add(array.get(i));
        return newJsonArray;
    }

    /**
     * @param response retrofit response.
     * @return true if the server response is null or empty.
     */
    private boolean isEmptyResponse(Response<JsonElement> response) {
        return response.body() == null || !response.body().isJsonObject();
    }

    /**
     * @param body is the server body json object
     * @return List of JsonObject containing `items` as List rather than a JsonArray
     * this conversion is mainly used in PagedListAdapter which only supports collections not JsonArray
     * @author Marthia
     * @see DataController#extractDataItemsAsJsonArray(JsonElement)
     * @see DataController#extractDataItemsAsJsonArray(Response)
     */
    public List<JsonObject> extractDataItems(JsonElement body) {
        List<JsonObject> list = new ArrayList<>();
        try {
            JsonArray array = extractDataItemsAsJsonArray(body);
            for (JsonElement obj : array) {
                list.add(obj.getAsJsonObject());
            }
        } catch (Exception e) {
            Log.e("extractDataItems", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not extract dataItems from : " + body.toString(), e));
        }

        return list;
    }

    /**
     * @param response server response
     * @return size of data items as integer or 0 if fails
     * @author Marthia
     */
    public int getDataItemsCount(JsonObject response) {
        try {
            return response.get("data").getAsJsonObject()
                    .get("items").getAsJsonArray().size();
        } catch (Exception e) {
            Log.e("dataItemCount", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("getDataItemsCount", e));
            return 0;
        }
    }

    /**
     * @param response is the server response
     * @return List of JsonObject containing `items` as JsonArray which is the raw sever structure
     * @author Marthia
     * @see DataController#extractDataItems(JsonElement)
     * @see DataController#extractDataItemsAsJsonArray(JsonElement)
     */
    public JsonArray extractDataItemsAsJsonArray(Response<JsonElement> response) {
        if (isEmptyResponse(response))
            return new JsonArray();

        return extractDataItemsAsJsonArray(response.body().getAsJsonObject());
    }

    /**
     * this overloaded method is for supporting json body as input in addition to the response
     *
     * @param body is the server body json object
     * @return List of JsonObject containing `items` as JsonArray which is the raw sever structure
     * @author Marthia
     * @see DataController#extractDataItems(JsonElement)
     * @see DataController#extractDataItemsAsJsonArray(Response)
     */
    public JsonArray extractDataItemsAsJsonArray(JsonElement body) {

        JsonArray list = new JsonArray();
        try {
            if (JsonHelper.isEmptyOrNull(body) && !body.isJsonObject())
                return list;

            JsonObject element = body.getAsJsonObject();

            if (!JsonHelper.has(element, "data") || JsonHelper.isEmptyOrNull(element.get("data")))
                return list;


            JsonObject data = element.get("data").getAsJsonObject();

            if (!JsonHelper.has(data, "items") || JsonHelper.isEmptyOrNull(data.get("items")))
                return list;

            return data.get("items").getAsJsonArray();

        } catch (Exception e) {
            Log.e("dataItems", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error extracting dataItems from " + body, e));
        }

        return list;
    }

    /**
     * @param response the server response
     * @param field    the field to search in (data -> info) hierarchy : only int values are supported for example
     *                 limitstart, start, total among others
     * @return int value associated with the mentioned fields or -1 if failed
     * @author Marthia
     */
    public int getInfoInt(Response<JsonElement> response, String field) {
        if (isEmptyResponse(response))
            return -1;
        return getInfoInt(response.body().getAsJsonObject(), field);
    }

    /**
     * @param body  the server response
     * @param field the field to search in (data -> info) hierarchy
     * @return String value of specified field in info
     * @author Marthia
     */
    public String getInfoString(JsonObject body, String field) {
        try {

            if (!JsonHelper.has(body, "data") || JsonHelper.isEmptyOrNull(body.get("data")))
                return "";

            JsonObject data = body.get("data").getAsJsonObject();

            if (!JsonHelper.has(data, "info") || JsonHelper.isEmptyOrNull(data.get("info")))
                return "";

            JsonObject info = data.getAsJsonObject("info");

            if (!JsonHelper.has(info, field) || JsonHelper.isEmptyOrNull(info.get(field)))
                return "";

            return info.get(field).getAsString();

        } catch (Exception e) {
            Log.e("info field (` " + field + " `)", "Not Provided");
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not extract info field (` " + field + " `)", e));
        }
        return "";
    }

    /**
     * @param body  extracted body of the server response as JsonObject
     * @param field the field to search in (data -> info) flow : only int values are supported for example
     *              limitstart, start, total among others
     * @return int value associated with the mentioned fields or -1 if failed
     * @author Marthia
     */
    public int getInfoInt(JsonObject body, String field) {
        try {

            if (!JsonHelper.has(body, "data") || JsonHelper.isEmptyOrNull(body.get("data")))
                return -1;

            JsonObject data = body.get("data").getAsJsonObject();

            if (!JsonHelper.has(data, "info") || JsonHelper.isEmptyOrNull(data.get("info")))
                return -1;

            JsonObject info = data.getAsJsonObject("info");

            if (!JsonHelper.has(info, field) || JsonHelper.isEmptyOrNull(info.get(field)))
                return -1;

            return info.get(field).getAsInt();
        } catch (Exception e) {
            Log.e("getInfoInt", "info field (` " + field + " `)");
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not extract info field (` " + field + " `)", e));
        }
        return -1;
    }

    /**
     * @param response server response body
     * @param type     module field to look for : the order of expected keys are (`data` -> `modules` as array of json object)
     * @return JsonObject module. it is essential for the module json object to have a `type` field otherwise this method cannot find it
     * and returns null
     * @author Marthia
     */
    public JsonObject extractModule(JsonElement response, String type) {
        try {
            if (JsonHelper.isEmptyOrNull(response) && !response.isJsonObject())
                return null;

            JsonObject element = response.getAsJsonObject();
            if (!JsonHelper.has(element, "data") || JsonHelper.isEmptyOrNull(element.get("data")))
                return null;

            JsonObject data = element.getAsJsonObject("data");

            if (!JsonHelper.has(data, "modules") || JsonHelper.isEmptyOrNull(data.get("modules")))
                return null;

            JsonArray modules = data.getAsJsonArray("modules");
            for (JsonElement item : modules) {
                if (JsonHelper.isEmptyOrNull(item))
                    continue;

                if (item.getAsJsonObject().get("type").getAsString().equals(type))
                    return item.getAsJsonObject();
            }
        } catch (Exception e) {
            Log.e("module", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error extracting module with type of " + type, e));
        }
        return null;
    }

    /**
     * @param cartInfo module
     * @return cart total count : order of expected fields are (`content` -> `checkout_text`)
     * @author Marthia
     */
    public int getCartTotalCount(JsonObject cartInfo) {
        try {
            return cartInfo.get("content").getAsJsonObject()
                    .get("checkout_text").getAsInt();

        } catch (Exception e) {
            Log.e("cartCount", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error extracting `checkout_text`. data: " + cartInfo.toString(), e));
        }
        return -1;
    }

    public void onSuccess(Response<JsonElement> response) {
        if (isEmptyResponse(response))
            return;

        if (response.isSuccessful()) {
            JsonElement data = response.body();

            // show server messages if this callback is set from MainActivity. as we show SnackBars and
            // the they should be shown in a specific area in MainActivity.
            uiMessagesResponseCallback.onSuccess(response);

            String responseMessage = getErrorMessageOrNull(data);
            String message = getMessageOrEmpty(data);

            if (responseMessage != null)
                uiMessagesResponseCallback.onFailure(responseMessage, null);

            if (!message.isEmpty()) {
                uiMessagesResponseCallback.onFailure(message, null);
            }

            if (!JsonHelper.has(data, "params"))
                return;

            // save new tokens on every response
            JsonObject params = data.getAsJsonObject().get("params").getAsJsonObject();
            if (params.has("csrftoken")) {
                String token = params.get("csrftoken").getAsString();
                preferenceManager.setToken(token);
            }

            // clear app's cookies based on sever params.
            // could happen any time if server wants to, but the main reason of this was to avoid startup blank pages
            // if the session was previously expired and the user restarts the app. in that case this param is true.
            if (params.has("clear_cache")) {
                if (params.get("clear_cache").getAsInt() == 1) {
                    clearCookies();
                    Log.i(TAG, "cookies cleared");
                }
            }

            // save user name and full name based on params: this way even if these values change by user
            // we have always the updated values
            if (params.has("username")) {
                // notify invalidate menus because of login/logout only if there has been a change
                // this is useful to only trigger our listener when there is a change and to distinguish
                // between login and logout
                boolean isChanged = !params.get("username").getAsString().equals(preferenceManager.getUser());
                if (isChanged)
                    invalidateSession.accept(JsonHelper.isNotEmptyNorNull(params.get("username")));

                String username = params.get("username").getAsString();
                String fullName = params.get("fullname").getAsString();
                preferenceManager.setUser(username);
                preferenceManager.setFullName(fullName);
            }
        } else
            uiMessagesResponseCallback.onFailure(response.errorBody().toString(), new NullPointerException());
    }

    public void onFailure(Throwable throwable) {
        throwable.printStackTrace();
        uiMessagesResponseCallback.onFailure(context.getString(R.string.error_connecting_to_server), throwable);
    }

    public void clearCookies() {
        ((PersistentCookieJar) okHttpClient.cookieJar()).clear();
        preferenceManager.clearUser();
    }

    public void setUiMessagesResponseCallback(OnResponseCallback uiMessagesResponseCallback) {
        this.uiMessagesResponseCallback = uiMessagesResponseCallback;
    }

    /**
     * @param item        product data item JsonObject
     * @param optionalKey the field to look for as price otherwise the default key (`price`) will be used
     *                    be sure to send null to use the default key
     * @return List of Price items
     * @author Marthia
     */
    public List<Price> extractPrice(JsonObject item, String optionalKey) {
        List<Price> result = new ArrayList<>();
        String key = "price";
        String fallbackText = "no_price_text";

        if (optionalKey != null) key = optionalKey;

        if (item.has(key) && item.get(key).getAsJsonArray().size() > 0) {
            JsonArray parr = item.get(key).getAsJsonArray();

            for (JsonElement r : parr) {
                Price price = new Price();
                JsonObject row = r.getAsJsonObject();

                price.setPrice(row.get("price_html").getAsString());

                if (JsonHelper.has(row, "discount") && JsonHelper.isNotEmptyNorNull(row))
                    price.setDiscount(row.get("discount").getAsString());

                if (row.has("p_p_u") && !row.get("p_p_u").getAsString().equals("")) {
                    price.setDescription(row.get("p_p_u").getAsString());
                }
                result.add(price);
            }
        }
        if (result.size() == 0 && JsonHelper.has(item, fallbackText)) {
            Price price = new Price();
            price.setPrice(item.get(fallbackText).getAsString());
            result.add(price);
        }
        return result;
    }

    public Object[] extractPriceAndMetadata(JsonObject item, String optionalKey) {
        Object[] priceBundle = new Object[2];

        List<Price> result = new ArrayList<>();
        String key = "price";
        String fallbackText = "no_price_text";

        if (optionalKey != null) key = optionalKey;

        if (item.has(key) && item.get(key).getAsJsonArray().size() > 0) {
            JsonArray parr = item.get(key).getAsJsonArray();

            for (JsonElement r : parr) {
                Price price = new Price();
                JsonObject row = r.getAsJsonObject();

                price.setPrice(row.get("price_html").getAsString());

                if (JsonHelper.has(row, "discount") && JsonHelper.isNotEmptyNorNull(row))
                    price.setDiscount(row.get("discount").getAsString());

                if (row.has("p_p_u") && !row.get("p_p_u").getAsString().equals("")) {
                    price.setDescription(row.get("p_p_u").getAsString());
                }
                result.add(price);
            }
        }
        if (result.size() == 0 && JsonHelper.has(item, fallbackText)) {
            Price price = new Price();
            price.setPrice(item.get(fallbackText).getAsString());
            result.add(price);
            priceBundle[0] = true;
        } else priceBundle[0] = false;

        priceBundle[1] = result;

        return priceBundle;
    }

    /**
     * @param cartInfo cart info module
     * @param id       product id to look for its count in cart if available
     * @return cart count for the provided product id or 0 if anything goes wrong
     * @author Marthia
     */
    public int getCountForProductId(JsonObject cartInfo, String id) {
        try {
            if (!JsonHelper.has(cartInfo, "content"))
                return 0;

            if (JsonHelper.isEmptyOrNull(cartInfo.get("content")))
                return 0;

            JsonObject content = cartInfo.get("content").getAsJsonObject();
            if (!JsonHelper.has(content, "products"))
                return 0;

            if (JsonHelper.isEmptyOrNull(content.get("products")))
                return 0;

            JsonObject products = content.get("products").getAsJsonObject();

            if (products.has(id)) return products.get(id).getAsInt();
            else return 0;

        } catch (Exception e) {
            Log.e("productCountCart", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not extract cart count for ProductId = " + id, e));
            return 0;
        }
    }

    /**
     * @param cartInfo module info for cart
     * @return String error messages related to cart or empty if not available
     * @author Marthia
     */
    public String extractAddToCartFailureMessage(JsonObject cartInfo) {
        try {
            JsonArray messages = cartInfo.get("content").getAsJsonObject().get("messages").getAsJsonArray();

            if (messages.size() == 0) return "";

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < messages.size(); i++) {
                String msg = messages.get(i).getAsJsonObject().get("msg").getAsString();
                builder.append(msg);
                builder.append("\n");
            }

            return builder.toString();
        } catch (Exception e) {
            Log.e("errorMessage", e.getMessage());
            return "";
        }
    }

    /**
     * @param response server response
     * @return boolean determined by the provided boolean flag in every response; the order of expected field are (`data` -> `info` -> `hasError`)
     * @author Marthia
     */
    public boolean isError(Response<JsonElement> response) {
        try {

            if (isEmptyResponse(response))
                return false;

            JsonObject body = response.body().getAsJsonObject();
            if (!JsonHelper.has(body, "data"))
                return false;

            JsonObject data = body.get("data").getAsJsonObject();

            if (!JsonHelper.has(data, "info"))
                return false;

            JsonObject info = data.get("info").getAsJsonObject();
            if (!JsonHelper.has(info, "hasError"))
                return false;

            if (JsonHelper.isEmptyOrNull(info.get("hasError")))
                return false;

            return info.get("hasError").getAsBoolean();


        } catch (Exception e) {
            Log.e("isError", e.getMessage());
            return true;
        }
    }

    /**
     * @param response server response body
     * @return String error message or null if not provided or anything goes wrong
     * @author Marthia
     */
    public String getErrorMessageOrNull(JsonElement response) {
        try {
            if (JsonHelper.isEmptyOrNull(response))
                return null;

            JsonObject body = response.getAsJsonObject();
            if (!JsonHelper.has(body, "data"))
                return null;

            JsonObject data = body.get("data").getAsJsonObject();

            if (!JsonHelper.has(data, "info"))
                return null;

            JsonObject info = data.get("info").getAsJsonObject();
            if (!JsonHelper.has(info, "messages"))
                return null;

            JsonObject messages = info.get("messages").getAsJsonObject();

            if (JsonHelper.isEmptyOrNull(messages))
                return null;

            if (!JsonHelper.has(messages, "error"))
                return null;

            if (JsonHelper.isEmptyOrNull(messages.get("error")))
                return null;

            return messages.get("error").getAsString();
        } catch (Exception e) {
            Log.e("isError", e.getMessage());
        }
        return null;
    }

    /**
     * @param response server response body
     * @return String custom server message for every request or empty.
     * this is mainly used in cart steps to show any server messages as a module
     * @author Marthia
     */
    public String getMessageOrEmpty(JsonElement response) {
        try {

            if (JsonHelper.isEmptyOrNull(response))
                return "";

            JsonObject body = response.getAsJsonObject();
            if (!JsonHelper.has(body, "data"))
                return "";

            JsonObject data = body.get("data").getAsJsonObject();

            if (!JsonHelper.has(data, "info"))
                return "";

            JsonObject info = data.get("info").getAsJsonObject();
            if (!JsonHelper.has(info, "messages"))
                return "";

            JsonObject messages = info.get("messages").getAsJsonObject();

            if (JsonHelper.isEmptyOrNull(messages))
                return "";

            String key = "message";
            if (!JsonHelper.has(messages, key)) {
                key = "warning";
                if (!JsonHelper.has(messages, key))
                    return "";
            }

            if (JsonHelper.isEmptyOrNull(messages.get(key)))
                return "";

            return messages.get(key).getAsString();
        } catch (Exception e) {
            Log.e("isError", e.getMessage());
            return "";
        }
    }

    /**
     * @param styles   String containing the directory of css files in assets
     * @param js       String containing the directory of javascript files in assets
     * @param body     String main content as html
     * @param language used to decide which styling files to use : rtl or ltr
     * @return String containing a complete html text with all the styling and js declares
     * @author Marthia
     * @see DataController#getWebViewJs(boolean)
     * @see DataController#getWebViewStyles(Context)
     */
    public String generateHtmlContent(String styles, String js, String body, String language) {

        String s = "<!doctype html>\n"

                + "<html xml:lang=\"" + language + "\" lang=\"" + language + "\" >\n"

                + "<head>\n"

                + "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n"

                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"

                + styles + "\n" + js + "\n" + "</head>\n"

                + "<body>" + body + "</body>\n"

                + "</html>";

        Log.i("generatedHtml", s);
        return s;
    }

    /**
     * @param context android context
     * @return html css file references
     * @author Marthia
     */
    public String getWebViewStyles(Context context) {

        String cssType = "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/css/";

        String style_rtl = "";
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<style>");
            InputStream is = context.getAssets().open("css/style-rtl.css");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();


            int colorSecondary = AndroidUtils.getAttr(context, R.attr.colorSecondary);
            String color = "#";
            String s = Integer.toHexString(colorSecondary);
            s = s.substring(2);
            color = color.concat(s);
            sb.append("</style>");

            style_rtl = sb.toString();
            // the escapes at the end of look-up phrase are needed although android says they're redundant
            // DO NO DELETE THEM OR IT'LL CRASH
            style_rtl = style_rtl.replaceAll("\\{\\{SECONDARY_COLOR\\}\\}", color);
        } catch (IOException e) {
            e.printStackTrace();
            ACRA.getErrorReporter().handleException(new CrashReportException("getWebViewStyles", e));
        }

        return cssType + "bootstrap.min.css\">\n"
                + cssType + "all.css\">\n"
                + cssType + "front.css\">\n"
                + cssType + "responsive.css\">\n"
                + cssType + "responsive-rtl.css\">\n"
                + cssType + "front-rtl.css\">\n"
                + style_rtl + "\n";
    }

    /**
     * @param hasChart include extra files for chart if needed
     * @return html javascript file references in assets
     * @author Marthia
     */
    public String getWebViewJs(boolean hasChart) {
        String jsType = "<script type=\"text/javascript\" src=\"file:///android_asset/js/";

        // default javascript files
        String js = jsType + "jquery-3.5.1.min.js\"></script>\n"
                + jsType + "jquery.serialize-object.min.js\"></script>\n"
                + jsType + "script.js\"></script>\n"
                + jsType + "loading.js\"></script>\n"
                + jsType + "error.js\"></script>\n";

        if (hasChart) {
            js += jsType + "highcharts.js\"></script>\n";
        }
        return js;
    }

    /**
     * @param data server response
     * @return Application params as `AppParams` object used for tracking version updates
     * @author Marthia
     */
    public AppParams extractForceUpdate(Response<JsonElement> data) {
        try {
            AppParams appParams = new AppParams();

            if (isEmptyResponse(data))
                return null;

            JsonObject element = data.body().getAsJsonObject();

            if (!JsonHelper.has(element, "params"))
                return null;

            if (JsonHelper.isEmptyOrNull(element.get("params")))
                return null;

            JsonObject params = element.getAsJsonObject("params");

            if (!JsonHelper.has(params, "app_ver") && !JsonHelper.has(params, "update_force"))
                return null;

            if (JsonHelper.isEmptyOrNull(params.get("app_ver")) || JsonHelper.isEmptyOrNull(params.get("update_force")))
                return null;

            appParams.setAppVersion(params.get("app_ver").getAsFloat());
            appParams.setForceUpdate(params.get("update_force").getAsString().equals("1"));

            return appParams;
        } catch (Exception e) {
            Log.e("extractForceUpdate", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error while extracting ForceUpdate", e));
        }
        return null;
    }

    /**
     * @param text any given string
     * @return replaces the given string special persian and arabic characters with the standard values used in server
     * to provide better ux in searching here and there. if no special characters has been used no replacement occur to avoid any
     * ui complications
     * @author Marthia
     */
    public String replaceSpecialCharacters(String text) {
        String regexIsOnlyEnglish = "^[a-zA-Z0-9$@$!,~\\\\/\\-'\"%*?&#^`;()\\n:-_. +]+$";

        if (text.matches(regexIsOnlyEnglish))
            return text;

        String d = text;
        // persian
        d = d.replace("۰", "0");
        d = d.replace("۱", "1");
        d = d.replace("۲", "2");
        d = d.replace("۳", "3");
        d = d.replace("۴", "4");
        d = d.replace("۵", "5");
        d = d.replace("۶", "6");
        d = d.replace("۷", "7");
        d = d.replace("۸", "8");
        d = d.replace("۹", "9");

        // arabic
        d = d.replace("٠", "0");
        d = d.replace("١", "1");
        d = d.replace("٢", "2");
        d = d.replace("٣", "3");
        d = d.replace("٤", "4");
        d = d.replace("٥", "5");
        d = d.replace("٦", "6");
        d = d.replace("٧", "7");
        d = d.replace("٨", "8");
        d = d.replace("٩", "9");

        d = d.replace("ي", "ی");
        d = d.replace("ك", "ک");

        return d;
    }

    /**
     * @param response server response body
     * @param param    field to look for in the provided json object body
     * @return JsonObject field inside (`data` -> `info` -> param)
     */
    public JsonObject extractInfo(JsonElement response, String param) {
        try {
            if (JsonHelper.isEmptyOrNull(response))
                return null;

            JsonObject body = response.getAsJsonObject();
            if (!JsonHelper.has(body, "data"))
                return null;

            JsonObject data = body.get("data").getAsJsonObject();

            if (!JsonHelper.has(data, "info"))
                return null;

            JsonObject info = data.get("info").getAsJsonObject();

            if (param == null)
                return info;

            if (!JsonHelper.has(info, param))
                return null;

            return info.get(param).getAsJsonObject();

        } catch (Exception e) {
            Log.e("Info " + param, "Error while extracting info" + param);
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not extract info item " + param, e));
            return null;
        }
    }

    public String getPersianNumber(int digit) {
        String d = String.valueOf(digit);

        d = d.replace("0", "۰");
        d = d.replace("1", "۱");
        d = d.replace("2", "۲");
        d = d.replace("3", "۳");
        d = d.replace("4", "۴");
        d = d.replace("5", "۵");
        d = d.replace("6", "۶");
        d = d.replace("7", "۷");
        d = d.replace("8", "۸");
        d = d.replace("9", "۹");

        return d;
    }

    public String replaceFullPathLinksWithPath(String url, String compareUrl) {
        try {
            URL link = new URL(url);
            URL compare = new URL(compareUrl);

            if (link.getHost().equalsIgnoreCase(compare.getHost()))
                return link.getPath(); // replace full path

        } catch (MalformedURLException ignore) {

        }
        return url; // url was a path
    }

    public List<Comment> bindCommentsJsonToObject(JsonArray content) {

        List<Comment> comments = new ArrayList<>();

        if (JsonHelper.isEmptyOrNull(content))
            return comments;
        try {
            for (int i = 0; i < content.size(); i++) {
                Comment comment = new Comment();

                JsonObject obj = content.get(i).getAsJsonObject();
                comment.setId(obj.get("id").getAsString());
                comment.setComment(obj.get("text").getAsString());
                comment.setDate(obj.get("date").getAsString());
                comment.setUserName(obj.get("author").getAsString());

                if (JsonHelper.has(obj, "actions")) {
                    JsonObject action = obj.get("actions").getAsJsonObject();

                    if (JsonHelper.has(action, "vote_good")) {
                        comment.setLikeEnabled(true);
                        comment.setLikeLink(action.get("vote_good").getAsJsonObject());
                    } else comment.setLikeEnabled(false);

                    if (JsonHelper.has(action, "vote_bad")) {
                        comment.setDislikeEnabled(true);
                        comment.setDislikeLink(action.get("vote_bad").getAsJsonObject());
                    } else comment.setDislikeEnabled(false);

                    if (JsonHelper.has(action, "quote")) {
                        comment.setReplyEnabled(true);
                        comment.setReplyLink(action.get("quote").getAsJsonObject());
                    } else comment.setReplyEnabled(false);

                    if (JsonHelper.has(action, "report")) {
                        comment.setInappropriateEnabled(true);
                        comment.setInappropriateLink(action.get("report").getAsJsonObject());
                    } else comment.setInappropriateEnabled(false);

//                    comment.setLiked(!JsonHelper.has(action, "vote_good") || !JsonHelper.has(action, "vote_bad"));
                }

                JsonObject vote = obj.get("vote").getAsJsonObject();
                comment.setLikeCount(vote.get("good").getAsString());
                comment.setDislikeCount(vote.get("bad").getAsString());
                comment.setRate(vote.get("total").getAsString());

                comments.add(comment);
            }
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not bind Comments Object in (DataController). object: " + content.toString(), e));
        }
        return comments;
    }

    public void subscribeInvalidateSession(Consumer<Boolean> action) {
        this.invalidateSession = action;
    }

    public CommentResponse extractFunctionMessage(Response<JsonElement> response) {
        CommentResponse result = new CommentResponse();

        if (response.body() == null)
            return result;

        String successFunction = "jcomments.message";
        String errorFunction = "jcomments.error";
        String reportFunction = "jcomments.closeReport";
        String quoteFunction = "quote";
        String voteFunction = "updateVote";
        String errorType = "al";
        String commonType = "js";

        class FunctionExtractor {
            public String getText(String line) {
                String result = "";
                Pattern p = Pattern.compile("'(.*?)'");
                Matcher m = p.matcher(line);
                if (m.find())
                    result = m.group(1);
                return result;
            }
        }

        FunctionExtractor extractor = new FunctionExtractor();

        try {
            JsonArray array = response.body().getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                String function = obj.get("d").getAsString();
                if (function.contains(errorFunction)) { // error
                    result.setErrorMessage(extractor.getText(function));
                } else if (obj.get("n").getAsString().equals(commonType)) { // message
                    // normal message
                    if (function.contains(successFunction))
                        result.setMessage(extractor.getText(function));
                        // quote message
                    else if (function.contains(quoteFunction))
                        result.setQuoteMessage(extractor.getText(function));

                        // vote message
                    else if (function.contains(voteFunction))
                        result.setVoteMessage(extractor.getText(function));

                        // report message
                    else if (function.contains(reportFunction))
                        result.setReportMessage(extractor.getText(function));
                }
            }
        } catch (Exception e) {
            Log.e("commentFunction", "could not extract comment function: " + e.getMessage());
        }
        return result;
    }

    /**
     * This method is used to limit the size of a JsonArray
     *
     * @param array JsonArray to reduce
     * @param limit size limit
     * @return reduced size JsonArray with the max of limit
     * @author Marhia
     */
    public JsonArray reduceBy(JsonArray array, int limit) {

        JsonArray result = new JsonArray();

        for (int i = 0; i < array.size(); i++)
            if (i < limit) result.add(array.get(i));

        return result;
    }

    public <T, V> ArrayList<T> convertMapToList(Map<V, T> items) {

        ArrayList<T> params = new ArrayList<>();

        Set<Map.Entry<V, T>> entries = items.entrySet();
        for (Map.Entry<V, T> ch :
                entries) {
            params.add(ch.getValue());
        }
        return params;
    }

    public <T> List<T> convertJsonArrayToList(JsonArray array, Class<T> tClass) {
        ArrayList<T> items = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {

            JsonElement item = array.get(i);
            T object = gson.fromJson(item, tClass);
            items.add(object);
        }
        return items;
    }
}
