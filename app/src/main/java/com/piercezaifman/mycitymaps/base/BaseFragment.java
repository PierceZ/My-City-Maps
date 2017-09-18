package com.piercezaifman.mycitymaps.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.FirebaseReferenceHolder;
import com.piercezaifman.mycitymaps.util.RxBus;

/**
 * The base fragment that all fragments should extend from.
 *
 * Created by piercezaifman on 2017-01-03.
 */

public abstract class BaseFragment extends Fragment {

    private FirebaseReferenceHolder mFirebaseReferenceHolder = new FirebaseReferenceHolder();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracker tracker = App.getApp().getTracker();
        tracker.setScreenName(getClass().getName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    protected FirebaseReferenceHolder getFirebaseReferenceHolder() {
        return mFirebaseReferenceHolder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFirebaseReferenceHolder.cleanup();
        RxBus.unregister(this);
    }
}
