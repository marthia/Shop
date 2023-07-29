package com.poonehmedia.app.data.framework.service;

import com.google.gson.JsonElement;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SearchApi {

    @GET("{path}")
    Single<Response<JsonElement>> getSuggestions(@Path("path") String path);


    @GET("{path}")
    Single<Response<JsonElement>> search(
            @Path("path") String path,
            @Query("searchword") String query,
            @Query("limitstart") int pageStart,
            @Query("limit") int limit
    );

}
