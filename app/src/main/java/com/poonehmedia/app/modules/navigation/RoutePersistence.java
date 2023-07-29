package com.poonehmedia.app.modules.navigation;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RoutePersistence {
    private final String TAG = getClass().getSimpleName();
    private final Map<String, NavigationArgs> info = new HashMap<>();

    @Inject
    public RoutePersistence() {
    }

    /**
     * add next page data with an access key to Navigation graph.
     *
     * @param accessingKey String Java unique id to access the data in the next page.
     * @param data         {@link NavigationArgs} containing all needed info of the next destination.
     * @author Marthia
     */
    public void addRoute(String accessingKey, NavigationArgs data) {
        this.info.put(accessingKey, data);
        Log.i(TAG, "Adding Route Key = " + accessingKey);
    }

    /**
     * remove all invalidated info on fragment destroy. this is not essential but to avoid memory issues.
     *
     * @param accessingKey the key that accessed the info with. added in {@link RoutePersistence#addRoute(String, NavigationArgs)}
     */
    public void dispose(String accessingKey) {
        info.remove(accessingKey);
        Log.i(TAG, "Removing Route Key = " + accessingKey);
    }

    /**
     * @param accessingKey the key that accessed the info with. added in {@link RoutePersistence#addRoute(String, NavigationArgs)}
     * @return {@link NavigationArgs} object.
     */
    public NavigationArgs getRoute(String accessingKey) {
        return info.get(accessingKey);
    }
}
