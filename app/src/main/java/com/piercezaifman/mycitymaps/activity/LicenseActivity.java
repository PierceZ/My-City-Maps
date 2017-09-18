package com.piercezaifman.mycitymaps.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.base.BaseActivity;

/**
 * Used to display a license or terms.
 * Created by piercezaifman on 2017-02-22.
 */

public class LicenseActivity extends BaseActivity {

    public static final String INTENT_EXTRA_LICENSE = "INTENT_EXTRA_LICENSE";

    private String mLicenseText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        if (savedInstanceState != null) {
            mLicenseText = savedInstanceState.getParcelable(INTENT_EXTRA_LICENSE);
        } else {
            Bundle extras = getIntent().getExtras();
            mLicenseText = extras.getString(INTENT_EXTRA_LICENSE);
        }

        TextView textView = (TextView) findViewById(R.id.activity_license_textview);
        textView.setText(mLicenseText);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(INTENT_EXTRA_LICENSE, mLicenseText);

        super.onSaveInstanceState(outState);
    }
}
