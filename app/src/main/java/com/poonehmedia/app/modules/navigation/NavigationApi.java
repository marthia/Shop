package com.poonehmedia.app.modules.navigation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface NavigationApi {

    @GET("{path}")
    Single<Response<JsonElement>> get(@Path(value = "path", encoded = true) String path);

    @GET
    Single<Response<JsonElement>> getFullPath(@Url String url);


    @POST("{path}")
    Single<Response<JsonElement>> post(@Path(value = "path", encoded = true) String path, @Body JsonObject body);

    @POST
    Single<Response<JsonElement>> postFullPath(@Url String url, @Body JsonObject body);
}
