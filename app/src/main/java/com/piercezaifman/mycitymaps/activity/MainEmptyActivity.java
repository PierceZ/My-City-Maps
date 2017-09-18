package com.piercezaifman.mycitymaps.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.util.Util;

/**
 * An empty activity to start at so you can navigate to the correct activity.
 * <p>
 * Created by piercezaifman on 2017-01-04.
 */

public class MainEmptyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent;

        // go straight to main if a city was already picked and license agreed to
        if (Util.getSharedPrefs().contains(App.PREFS_CITY_KEY) && Util.getSharedPrefs().getBoolean(App.PREFS_LICENSE_AGREED_KEY, false)) {
            activityIntent = new Intent(this, MainActivity.class);
        } else {
            activityIntent = new Intent(this, CitySelectionActivity.class);
        }

        startActivity(activityIntent);
        finish();
    }
}
