package com.piercezaifman.mycitymaps.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.crash.FirebaseCrash;
import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.BuildConfig;
import com.piercezaifman.mycitymaps.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.doorbell.android.Doorbell;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by piercezaifman on 2016-12-19.
 */

public final class Util {
    private Util() {
        // hidden constructor
    }

    /**
     * Checks if the location setting is on.
     * Taken from <a href="http://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled">here</a>
     */
    public static boolean isLocationEnabled() {
        Context context = App.getApp();
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                Util.log("Location setting", "Failed to check location setting", e);
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    public static SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(App.getApp());
    }

    /**
     * Take the name of a map file of the format "my_map_name.kml" and convert it to "My map name".
     */
    public static String formatMapFileName(String mapFileName) {
        String formattedName = mapFileName.split("\\.")[0];
        formattedName = formattedName.replace("_", " ");
        formattedName = formattedName.substring(0, 1).toUpperCase() + formattedName.substring(1);

        return formattedName;
    }

    /**
     * Convert a list of strings to a comma separated list.
     */
    @NonNull
    public static String commaSeparated(@Nullable List<String> list) {
        if (list == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String s : list) {
            builder.append(s);
            builder.append(",");
        }

        // remove the last comma
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    /**
     * Join a comma separated string into a real list.
     */
    @NonNull
    public static List<String> commaJoined(@Nullable String s) {
        if (s == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(s.split(",")));
    }

    public static void showFeedbackDialog(@NonNull Activity activity, @StringRes int metaTitle, @StringRes int messageHint) {
        Doorbell doorbellDialog = new Doorbell(activity,
                activity.getResources().getInteger(R.integer.doorbell_app_id),
                activity.getString(R.string.doorbell_key));

        doorbellDialog.addProperty(activity.getString(R.string.feedback_key_city), Util.getSharedPrefs().getString(App.PREFS_CITY_NAME, "N/A"));
        doorbellDialog.addProperty(activity.getString(R.string.feedback_key_title), activity.getString(metaTitle));
        doorbellDialog.setEmailHint(R.string.feedback_add_email_optional);
        doorbellDialog.setMessageHint(messageHint);
        doorbellDialog.setPoweredByVisibility(View.GONE);

        doorbellDialog.show();
    }

    /**
     * Log only in debug.
     */
    public static void log(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }
    }

    /**
     * Log error to logcat and Firebase.
     */
    public static void log(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable);
        } else {
            FirebaseCrash.logcat(Log.ERROR, tag, tag + ": " + message);
            FirebaseCrash.report(throwable);
        }
    }

    /**
     * Will process the data with the callable by splitting the data into the specified number of threads.
     * <b>T</b> is ths type of data being parsed, and <b>U</b> is the type of data being returned.
     */
    public static <T, U> Iterable<U> parseDataInParallel(@NonNull List<T> data, Func1<List<T>, U> worker) {
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadCount);
        Scheduler scheduler = Schedulers.from(threadPoolExecutor);

        AtomicInteger groupIndex = new AtomicInteger();

        return Observable.from(data).groupBy(k -> groupIndex.getAndIncrement() % threadCount)
                .flatMap(group -> group.observeOn(scheduler).toList().map(worker)).toBlocking().toIterable();

    }


    public static void logEvent(@StringRes int category, @StringRes int action, String label) {
        Tracker tracker = App.getApp().getTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(App.getApp().getString(category))
                .setAction(App.getApp().getString(action))
                .setLabel(label)
                .build());
    }

    public static void logCustomDimension(@IntegerRes int dimensionId, String value) {
        Tracker tracker = App.getApp().getTracker();
        tracker.send(new HitBuilders.ScreenViewBuilder()
                        .setCustomDimension(App.getApp().getResources().getInteger(dimensionId), value)
                        .build());
    }

    public static void setSpannableLink(String text, String highlight, ClickableSpan clickableSpan, TextView view) {
        SpannableString spannable = new SpannableString(text);
        Util.setSpannableLink(spannable, text, highlight, clickableSpan);

        view.setText(spannable);
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void setSpannableLink(SpannableString spannable, String text, String highlight, ClickableSpan clickableSpan) {
        int startHighlight = text.indexOf(highlight);
        int endHighlight = startHighlight + highlight.length();
        spannable.setSpan(clickableSpan, startHighlight, endHighlight, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(new UnderlineSpan(), startHighlight, endHighlight, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
