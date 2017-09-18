package com.piercezaifman.mycitymaps;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;

/**
 * App class used to initialize anything for the whole app and hold some references for convenience.
 *
 * Created by piercezaifman on 2016-12-22.
 */

public class App extends Application {

    public static final String PREFS_CITY_NAME = "PREFS_CITY_NAME";
    public static final String PREFS_CITY_KEY = "PREFS_CITY_KEY";
    public static final String PREFS_RECENT_MAPS = "PREFS_RECENT_MAPS";
    public static final String PREFS_LICENSE_AGREED_KEY = "PREFS_LICENSE_AGREED_KEY";

    private static App sApp;
    private static FirebaseAnalytics sAnalytics;
    private static Tracker sTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        sAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public static App getApp() {
        return sApp;
    }

    synchronized public Tracker getTracker() {
        if (sTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            sTracker = analytics.newTracker(getString(R.string.ga_tracking_id));
        }
        return sTracker;
    }

    public static FirebaseAnalytics getAnalytics() {
        return sAnalytics;
    }
}
