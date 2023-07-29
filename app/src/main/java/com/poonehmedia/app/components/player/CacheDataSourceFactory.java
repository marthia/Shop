package com.poonehmedia.app.components.player;

import android.content.Context;
import android.util.Log;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.poonehmedia.app.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CacheDataSourceFactory implements DataSource.Factory {
    private static final String TAG = "CacheDataSourceFactory";
    private static SimpleCache simpleCache;
    private final Context context;
    private final DataSource.Factory defaultDataSourceFactory;
    private final long maxFileSize, maxCacheSize;

    public CacheDataSourceFactory(Context context, long maxCacheSize, long maxFileSize) {
        super();
        this.context = context;
        this.maxCacheSize = maxCacheSize;
        this.maxFileSize = maxFileSize;
        String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));

        DefaultBandwidthMeter.Builder b = new DefaultBandwidthMeter.Builder(context);
        DefaultBandwidthMeter bandwidthMeter = b.build();

        defaultDataSourceFactory = new DefaultDataSourceFactory(this.context,
                bandwidthMeter,
                new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));
    }

    @NotNull
    @Override
    public DataSource createDataSource() {
        LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(maxCacheSize);
        ExoDatabaseProvider provider = new ExoDatabaseProvider(context);
        simpleCache = getInstance(evictor, provider);

        Log.d(TAG, "createDataSource() called" + context.getCacheDir());

        return new CacheDataSource(simpleCache, defaultDataSourceFactory.createDataSource(),
                new FileDataSource(), new CacheDataSink(simpleCache, maxFileSize),
                CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
    }

    public SimpleCache getInstance(LeastRecentlyUsedCacheEvictor evictor, ExoDatabaseProvider provider) {
        if (simpleCache == null)
            simpleCache = new SimpleCache(new File(context.getCacheDir(), "media"), evictor, provider);
        return simpleCache;
    }
}
