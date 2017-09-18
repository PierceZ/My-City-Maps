package com.piercezaifman.mycitymaps.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.HintSpinner;
import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.activity.LicenseActivity;
import com.piercezaifman.mycitymaps.base.NavigationFragment;
import com.piercezaifman.mycitymaps.data.City;
import com.piercezaifman.mycitymaps.util.FontHelper;
import com.piercezaifman.mycitymaps.util.RxBus;
import com.piercezaifman.mycitymaps.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to select a city.
 * <p>
 * Created by piercezaifman on 2017-01-04.
 */

public class CitySelectionFragment extends NavigationFragment {

    @BindView(R.id.fragment_city_selection_country_spinner) HintSpinner mCountrySpinner;
    @BindView(R.id.fragment_city_selection_state_spinner) HintSpinner mStateSpinner;
    @BindView(R.id.fragment_city_selection_city_spinner) HintSpinner mCitySpinner;
    @BindView(R.id.fragment_city_selection_loading_section) View mLoadingLayout;
    @BindView(R.id.fragment_city_selection_section) View mSelectionLayout;
    @BindView(R.id.fragment_city_selection_textview_cantfindcity) TextView mCantFindCityView;
    @BindView(R.id.fragment_city_selection_license_agree) View mLicenseContainer;
    @BindView(R.id.fragment_city_selection_textview_license) TextView mLicenseTextview;
    @BindView(R.id.fragment_city_selection_button_license) Button mLicenseAgreeButton;

    private Map<String, Map<String, Map<String, City>>> mCityMap = new HashMap<>();

    public static CitySelectionFragment newInstance() {
        return new CitySelectionFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxBus.subscribe(RxBus.SUBJECT_CITIES_LOADED, this, (cities) -> {

            List<City> cityList = (List<City>) cities;
            for (City city : cityList) {
                if (!mCityMap.containsKey(city.getCountry())) {
                    mCityMap.put(city.getCountry(), new HashMap<>());
                }
                Map<String, Map<String, City>> stateMap = mCityMap.get(city.getCountry());
                if (!stateMap.containsKey(city.getState())) {
                    stateMap.put(city.getState(), new HashMap<>());
                }
                Map<String, City> cityMap = stateMap.get(city.getState());
                cityMap.put(city.getName(), city);
            }

            showCountries();
        });

        loadCities();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_city_selection, container, false);
        ButterKnife.bind(this, v);

        FontHelper.setFont(getContext(), FontHelper.FONT_LIGHT, mCantFindCityView);

        mCantFindCityView.setOnClickListener((view) -> {
            Util.showFeedbackDialog(getActivity(), R.string.feedback_title_city, R.string.feedback_missing_city_edittext_hint);
        });

        mCountrySpinner.setOnItemPickedListener((country) -> {
            Set<String> states = mCityMap.get(country).keySet();

            // this means there is no states for this country
            if (states.size() == 1 && states.contains(null)) {
                mStateSpinner.setVisibility(View.GONE);

                List<String> cities = new ArrayList<>();
                cities.addAll(mCityMap.get(country).get(null).keySet());
                Collections.sort(cities);
                mCitySpinner.setData(cities);
                mCitySpinner.setVisibility(View.VISIBLE);
                mLicenseContainer.setVisibility(View.GONE);
                mCitySpinner.setSelection(0);
            } else if (states.size() > 0) {
                List<String> stateList = new ArrayList<>();
                stateList.addAll(states);
                Collections.sort(stateList);
                mStateSpinner.setData(stateList);

                mStateSpinner.setVisibility(View.VISIBLE);
                mLicenseContainer.setVisibility(View.GONE);
                mStateSpinner.setSelection(0);
                mCitySpinner.setVisibility(View.GONE);
            }
        });

        mStateSpinner.setOnItemPickedListener((state) -> {
            List<String> cities = new ArrayList<>();
            cities.addAll(mCityMap.get(mCountrySpinner.getSelectedItem()).get(state).keySet());
            Collections.sort(cities);
            mCitySpinner.setData(cities);
            mCitySpinner.setVisibility(View.VISIBLE);
            mLicenseContainer.setVisibility(View.GONE);
            mCitySpinner.setSelection(0);
        });

        mCitySpinner.setOnItemPickedListener((pickedCity) -> {
            setupLicense();
            mLicenseContainer.setVisibility(View.VISIBLE);
        });


        setupLicenseAgreeButton();
        showCountries();

        return v;
    }

    private void setupLicense() {
        String licenseAgree = getString(R.string.eula_warning);
        String highlight = getString(R.string.eula_highlight);
        SpannableString spannableLicense = new SpannableString(licenseAgree);

        Util.setSpannableLink(spannableLicense, licenseAgree, highlight, new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LicenseActivity.class);
                intent.putExtra(LicenseActivity.INTENT_EXTRA_LICENSE, getString(R.string.end_user_license_agreement));
                startActivity(intent);
            }
        });

        String cityTermsHighlight = getString(R.string.eula_city_highlight);
        Util.setSpannableLink(spannableLicense, licenseAgree, cityTermsHighlight, new ClickableSpan() {
            @Override
            public void onClick(View view) {
                City city = mCityMap.get(mCountrySpinner.getSelectedItem()).get(mStateSpinner.getSelectedItem()).get(mCitySpinner.getSelectedItem());
                Intent intent = new Intent(getActivity(), LicenseActivity.class);
                intent.putExtra(LicenseActivity.INTENT_EXTRA_LICENSE, city.getLicenseOffline());
                startActivity(intent);
            }
        });

        mLicenseTextview.setText(spannableLicense);
        mLicenseTextview.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupLicenseAgreeButton() {
        mLicenseAgreeButton.setOnClickListener(view -> {
            City city = mCityMap.get(mCountrySpinner.getSelectedItem()).get(mStateSpinner.getSelectedItem()).get(mCitySpinner.getSelectedItem());
            Util.getSharedPrefs().edit()
                    .putString(App.PREFS_CITY_NAME, city.getName())
                    .putString(App.PREFS_CITY_KEY, city.getKey()).apply();

            Toast.makeText(App.getApp(), getString(R.string.city_selection_toast, city.getName()), Toast.LENGTH_LONG).show();
            Util.getSharedPrefs().edit().remove(App.PREFS_RECENT_MAPS).apply();
            RxBus.publish(RxBus.SUBJECT_CITY_PICKED, city);

            Util.logCustomDimension(R.integer.ga_custom_dimension_city, city.toString());
            mLicenseContainer.setVisibility(View.GONE);

            Util.getSharedPrefs().edit().putBoolean(App.PREFS_LICENSE_AGREED_KEY, true).apply();
            RxBus.publish(RxBus.SUBJECT_LICENSE_AGREED, true);
        });
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

    private void showCountries() {
        List<String> countryNames = new ArrayList<>();
        countryNames.addAll(mCityMap.keySet());
        Collections.sort(countryNames);

        if (countryNames.size() > 0 && mCountrySpinner != null && mLoadingLayout != null && mSelectionLayout != null) {
            mCountrySpinner.setData(countryNames);
            mLoadingLayout.setVisibility(View.GONE);
            mSelectionLayout.setVisibility(View.VISIBLE);

            if (countryNames.size() == 1) {
                mCountrySpinner.setSelection(1);
            }
        }
    }

    @Override
    public int getTitleId() {
        return R.string.nav_city;
    }
}
