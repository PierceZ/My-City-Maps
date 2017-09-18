package com.piercezaifman.mycitymaps.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.FirebaseReferenceHolder;
import com.piercezaifman.mycitymaps.util.RxBus;

/**
 * The base activity, all activities should extend from this.
 *
 * Created by piercezaifman on 2017-01-03.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private FirebaseReferenceHolder mFirebaseReferenceHolder = new FirebaseReferenceHolder();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracker tracker = App.getApp().getTracker();
        tracker.setScreenName(getClass().getName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    protected FirebaseReferenceHolder getFirebaseReferenceHolder() {
        return mFirebaseReferenceHolder;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirebaseReferenceHolder.cleanup();
        RxBus.unregister(this);
    }
}
