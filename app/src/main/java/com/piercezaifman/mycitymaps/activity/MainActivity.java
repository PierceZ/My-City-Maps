package com.piercezaifman.mycitymaps.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.base.BaseActivity;
import com.piercezaifman.mycitymaps.base.NavigationFragment;
import com.piercezaifman.mycitymaps.data.City;
import com.piercezaifman.mycitymaps.fragment.AboutFragment;
import com.piercezaifman.mycitymaps.fragment.CitySelectionFragment;
import com.piercezaifman.mycitymaps.fragment.FeedbackFragment;
import com.piercezaifman.mycitymaps.fragment.HomeFragment;
import com.piercezaifman.mycitymaps.util.RxBus;
import com.piercezaifman.mycitymaps.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main activity to navigate to the different fragments.
 * <p>
 * Created by piercezaifman on 2016-12-09.
 */

public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view) NavigationView mNavigationView;

    private TextView mCityTitleView;

    private int mSelectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            selectNavItem(R.id.nav_home);
        }

        mCityTitleView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_textview_city);
        mCityTitleView.setText(Util.getSharedPrefs().getString(App.PREFS_CITY_NAME, ""));

        RxBus.subscribe(RxBus.SUBJECT_CITY_PICKED, this, (cityObject) -> {
            City city = (City) cityObject;
            mCityTitleView.setText(city.getName());
        });
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        mNavigationView.setNavigationItemSelectedListener((menuItem) -> {
            selectNavItem(menuItem.getItemId());
            return true;
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void selectNavItem(int itemId) {
        if (itemId != mSelectedId) {
            NavigationFragment fragment;

            //Check to see which item was being clicked and perform appropriate action
            switch (itemId) {
                case R.id.nav_home:
                    fragment = HomeFragment.newInstance();
                    break;
                case R.id.nav_city:
                    fragment = CitySelectionFragment.newInstance();
                    break;
                case R.id.nav_feedback:
                    fragment = FeedbackFragment.newInstance();
                    break;
                case R.id.nav_about:
                    fragment = AboutFragment.newInstance();
                    break;
                default:
                    fragment = HomeFragment.newInstance();
                    break;
            }

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();


            // Highlight the selected item, update the title, and close the drawer
            MenuItem oldItem = mNavigationView.getMenu().findItem(mSelectedId);
            MenuItem newItem = mNavigationView.getMenu().findItem(itemId);

            if (oldItem != null) {
                oldItem.setChecked(false);
            }
            if (newItem != null) {
                mNavigationView.getMenu().findItem(itemId).setChecked(true);
            }


            mSelectedId = itemId;
            setTitle(fragment.getTitleId());
        }
        mDrawerLayout.closeDrawers();
    }
}
