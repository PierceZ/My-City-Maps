package com.piercezaifman.mycitymaps.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.activity.LicenseActivity;
import com.piercezaifman.mycitymaps.base.NavigationFragment;
import com.piercezaifman.mycitymaps.data.City;
import com.piercezaifman.mycitymaps.util.RxBus;
import com.piercezaifman.mycitymaps.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Displays information about the app, including licenses.
 * <p>
 * Created by piercezaifman on 2016-12-12.
 */

public class AboutFragment extends NavigationFragment {

    private static final String[] LIBS = {"AboutLibraries", "AndroidIconics", "Butterknife", "fastadapter",
            "android_maps_utils", "rxandroid", "rxjava", "appcompat_v7", "design", "recyclerview_v7", "support_v4"};

    @BindView(R.id.fragment_about_recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.fragment_about_textview_eula) TextView mLicenseTextview;

    private LicensesAdapter mAdapter;

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        RxBus.subscribe(RxBus.SUBJECT_CITIES_LOADED, this, (cities) -> {

            if (mAdapter != null) {
                List<City> cityList = (List<City>) cities;
                mAdapter.updateData(cityList);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, v);

        setupEULA();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new LicensesAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        loadCities();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_about, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_about_software_licenses:
                new LibsBuilder()
                        .withLicenseShown(true)
                        .withFields(R.string.class.getFields())
                        .withLibraries(LIBS)
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .start(getActivity());
                return true;
            default:
                break;
        }

        return false;
    }

    private void setupEULA() {
        String highlight = getString(R.string.eula_highlight);
        Util.setSpannableLink(highlight, highlight, new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LicenseActivity.class);
                intent.putExtra(LicenseActivity.INTENT_EXTRA_LICENSE, getString(R.string.end_user_license_agreement));
                startActivity(intent);
            }
        }, mLicenseTextview);
    }

    private void loadCities() {
        DatabaseReference citiesReference = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_cities_reference));
        getFirebaseReferenceHolder().addValueEventListener(citiesReference, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<City> cities = new ArrayList<>();
                for (DataSnapshot citySnapshot : dataSnapshot.getChildren()) {
                    City city = citySnapshot.getValue(City.class);
                    city.setKey(citySnapshot.getKey());
                    cities.add(city);
                }

                RxBus.publish(RxBus.SUBJECT_CITIES_LOADED, cities);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Util.log("Loading cities", "Failed to load", databaseError.toException());
                View v = getView();
                if (v != null) {
                    Snackbar.make(getView(), R.string.city_selection_loading_error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public int getTitleId() {
        return R.string.nav_about;
    }

    // Can't be private because butterknife needs access
    static class LicensesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<City> mDataset;
        private AboutFragment mFragment;

        LicensesAdapter(AboutFragment fragment) {
            mFragment = fragment;
            mDataset = new ArrayList<>();
        }

        public void updateData(List<City> dataset) {
            mDataset = new ArrayList<>();
            mDataset.addAll(dataset);
            Collections.sort(mDataset, (c1, c2) -> c1.toFormattedString().compareTo(c2.toFormattedString()));

            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            RecyclerView.ViewHolder holder;
            View v = LayoutInflater.from(context).inflate(R.layout.view_license_row, parent, false);
            holder = new RowViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof RowViewHolder) {
                City city = mDataset.get(position);
                RowViewHolder rowHolder = (RowViewHolder) holder;
                rowHolder.mCityView.setText(city.toFormattedString());
                rowHolder.mDisclaimerView.setText(city.getDisclaimer());

                String licenseHighlight = App.getApp().getString(R.string.license_details);
                Util.setSpannableLink(licenseHighlight, licenseHighlight, new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(city.getLicenseLink()));
                        mFragment.startActivity(browserIntent);
                    }
                }, rowHolder.mLicenseView);

                String licenseOfflineHighlight = App.getApp().getString(R.string.license_details_offline);
                Util.setSpannableLink(licenseOfflineHighlight, licenseOfflineHighlight, new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mFragment.getActivity(), LicenseActivity.class);
                        intent.putExtra(LicenseActivity.INTENT_EXTRA_LICENSE, city.getLicenseOffline());
                        mFragment.startActivity(intent);
                    }
                }, rowHolder.mLicenseOfflineView);
            }
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        static class RowViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.view_license_row_textview_city) TextView mCityView;
            @BindView(R.id.view_license_row_textview_disclaimer) TextView mDisclaimerView;
            @BindView(R.id.view_license_row_textview_license) TextView mLicenseView;
            @BindView(R.id.view_license_row_textview_license_offline) TextView mLicenseOfflineView;

            RowViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }
        }
    }

}
