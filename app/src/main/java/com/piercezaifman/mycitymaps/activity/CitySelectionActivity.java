package com.piercezaifman.mycitymaps.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.base.BaseActivity;
import com.piercezaifman.mycitymaps.util.RxBus;

/**
 * Activity to select a city.
 * <p>
 * Created by piercezaifman on 2017-01-04.
 */

public class CitySelectionActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_selection);

        RxBus.subscribe(RxBus.SUBJECT_LICENSE_AGREED, this, o -> {
            startActivity(new Intent(CitySelectionActivity.this, MainActivity.class));
            finish();
        });
    }
}
