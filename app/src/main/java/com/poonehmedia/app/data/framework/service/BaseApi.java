package com.poonehmedia.app.data.framework.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Tag;
import retrofit2.http.Url;

public interface BaseApi {

    @GET("{path}")
    Single<Response<JsonElement>> get(@Path(value = "path", encoded = true) String path);

    @GET("{path}")
    Single<Response<JsonElement>> get(@Tag String tag,
                                      @Path(value = "path", encoded = true) String path
    );

    @POST("{path}")
    Single<Response<JsonElement>> post(@Path(value = "path", encoded = true) String path, @Body JsonObject body);

    @POST
    Single<Response<JsonElement>> postFullPath(@Url String url, @Body JsonObject body);

    @POST
    Single<Response<JsonElement>> postFullPath(@Tag String tag, @Url String url, @Body JsonObject body);

    @POST("{path}")
    Single<Response<JsonElement>> post(@Tag String tag,
                                       @Path(value = "path", encoded = true) String path,
                                       @Body JsonObject body
    );

    @GET
    Single<Response<JsonElement>> getFullPath(@Url String url);

    @GET
    Single<Response<JsonElement>> getFullPath(@Tag String tag, @Url String url);

    @Multipart
    @POST("{path}")
    Single<Response<JsonElement>> postDataWithFile(
            @Path(value = "path", encoded = true) String path,
            @Part List<MultipartBody.Part> files,
            @PartMap Map<String, RequestBody> params
    );

    // TODO CHECK WHY THE RETURN RESPONSE IS STRING
    @GET("{path}")
    Single<Response<String>> deleteAddress(@Path(value = "path", encoded = true) String deleteLink);
}
