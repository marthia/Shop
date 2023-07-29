package com.poonehmedia.app.di;

import android.content.Context;

import androidx.room.Room;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.poonehmedia.app.BuildConfig;
import com.poonehmedia.app.data.framework.HeaderInterceptor;
import com.poonehmedia.app.data.framework.LoggerDatabase;
import com.poonehmedia.app.data.framework.SearchHistoryDatabase;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.data.framework.service.SearchApi;
import com.poonehmedia.app.modules.navigation.NavigationApi;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public final class AppModule {

    @Singleton
    @Provides
    public static OkHttpClient provideOkHttpClient(@ApplicationContext Context context, HeaderInterceptor headerInterceptor) {
        CookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES);
        builder.readTimeout(1, TimeUnit.MINUTES);
        builder.addInterceptor(headerInterceptor);

        builder.cookieJar(cookieJar);
        builder.cache(null);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
        return builder.build();
    }


    @Singleton
    @Provides
    public static Retrofit provideRetrofit(OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(client)
                .build();
    }

    @Singleton
    @Provides
    public static SearchHistoryDatabase provideSearchHistoryDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context,
                SearchHistoryDatabase.class, "search_history_database").build();
    }

    @Singleton
    @Provides
    public static LoggerDatabase provideLoggerDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context,
                LoggerDatabase.class, "logger_debug_database").build();
    }

    @Provides
    @Singleton
    public static Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    public static BaseApi provideApiService(Retrofit retrofit) {
        return retrofit.create(BaseApi.class);
    }

    @Singleton
    @Provides
    public static SearchApi provideSearchService(Retrofit retrofit) {
        return retrofit.create(SearchApi.class);
    }

    @Singleton
    @Provides
    public static NavigationApi provideNavigationApi(Retrofit retrofit) {
        return retrofit.create(NavigationApi.class);
    }
}
