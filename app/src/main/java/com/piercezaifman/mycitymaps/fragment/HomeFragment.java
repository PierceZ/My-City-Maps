package com.piercezaifman.mycitymaps.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.RecyclerViewClickListener;
import com.piercezaifman.mycitymaps.SearchAdapter;
import com.piercezaifman.mycitymaps.activity.MapActivity;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to display the home screen with a list of maps.
 * <p>
 * Created by piercezaifman on 2016-12-12.
 */

public class HomeFragment extends NavigationFragment {

    private static final String SEARCH_COLUMN = "search_column";
    private static final int SEARCH_COLUMN_INDEX = 1;

    @BindView(R.id.fragment_home_recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.fragment_home_progressbar) ProgressBar mProgressBar;

    private City mCity;
    private Map<String, String> mMapNamesMap = new HashMap<>();
    private MapAdapter mAdapter;
    private SearchAdapter mSearchAdapter;
    private List<String> mRecentMaps;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchAdapter = new SearchAdapter(getActivity());
        setHasOptionsMenu(true);

        mRecentMaps = Util.commaJoined(Util.getSharedPrefs().getString(App.PREFS_RECENT_MAPS, null));

        RxBus.subscribe(RxBus.SUBJECT_MAP_CARD_CLICKED, this, (name) -> {
            String formattedName = (String) name;
            onMapPicked(formattedName);
        });

        RxBus.subscribe(RxBus.SUBJECT_MAPS_LOADED, this, (city) -> {
            mCity = (City) city;

            // convert names to a readable format
            for (String name : mCity.getMaps()) {
                mMapNamesMap.put(Util.formatMapFileName(name), name);
            }

            showMaps();
        });

        loadMaps();
    }

    private void onMapPicked(String map) {
        String mapFileName = mMapNamesMap.get(map);

        Util.logEvent(R.string.ga_category_home, R.string.ga_action_map_picked, mCity.toString() + "/" + map);

        if (!mRecentMaps.contains(mapFileName)) {
            mRecentMaps.add(0, mapFileName);
            int numColumns = getResources().getInteger(R.integer.num_columns);
            if (mRecentMaps.size() > numColumns) {
                mRecentMaps = mRecentMaps.subList(0, numColumns);
            }
            showRecentMaps();
            Util.getSharedPrefs().edit().putString(App.PREFS_RECENT_MAPS, Util.commaSeparated(mRecentMaps)).apply();
        }

        Intent mapIntent = new Intent(getActivity(), MapActivity.class);
        mapIntent.putExtra(MapActivity.EXTRA_CITY, mCity);
        mapIntent.putExtra(MapActivity.EXTRA_FILENAME, mapFileName);
        startActivity(mapIntent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // In case the city was changed, we'll get the recent maps and refresh the list.
        mRecentMaps = Util.commaJoined(Util.getSharedPrefs().getString(App.PREFS_RECENT_MAPS, null));
        if (mRecentMaps.size() == 0 && mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, v);

        mRecyclerView.setHasFixedSize(true);
        final int numColumns = getResources().getInteger(R.integer.num_columns);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), numColumns);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int spanSize = 1;

                //set span size for headers to fill the full span
                if (position == 0 || (mRecentMaps.size() > 0 && position == (mRecentMaps.size() + 1))) {
                    spanSize = numColumns;
                }
                return spanSize;
            }
        });
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new MapAdapter();
        mRecyclerView.setAdapter(mAdapter);

        showMaps();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_home, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.fragment_home_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.home_search_hint));
        searchView.setSuggestionsAdapter(mSearchAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                String map = mSearchAdapter.getSuggestion(position);
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                searchMenuItem.collapseActionView();
                onMapPicked(map);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mSearchAdapter.updateSearchSuggestions(query, mAdapter.getMaps());
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void loadMaps() {
        String cityPath = getString(R.string.firebase_cities_reference) + "/" + Util.getSharedPrefs().getString(App.PREFS_CITY_KEY, "");

        DatabaseReference cityReference = FirebaseDatabase.getInstance().getReference(cityPath);
        getFirebaseReferenceHolder().addValueEventListener(cityReference, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                City city = dataSnapshot.getValue(City.class);
                RxBus.publish(RxBus.SUBJECT_MAPS_LOADED, city);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Util.log("Loading maps", "Failed to load", databaseError.toException());

                View v = getView();
                if (v != null) {
                    Snackbar.make(getView(), R.string.home_failed_load_maps, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showRecentMaps() {
        List<String> formattedRecentMaps = new ArrayList<>();
        for (String fileName : mRecentMaps) {
            formattedRecentMaps.add(Util.formatMapFileName(fileName));
        }
        mAdapter.updateRecent(formattedRecentMaps);
    }

    private void showMaps() {
        List<String> sortedNames = new ArrayList<>(mMapNamesMap.keySet());
        if (sortedNames.size() > 0 && mAdapter != null && mProgressBar != null && mRecyclerView != null) {
            Collections.sort(sortedNames);
            showRecentMaps();
            mAdapter.updateData(sortedNames);
            mAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getTitleId() {
        return R.string.nav_home;
    }

    // Can't be private because butterknife needs access
    static class MapAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_ROW = 1;

        private List<String> mMaps;
        private List<String> mRecentMaps;
        private RecyclerViewClickListener mListener;

        MapAdapter() {
            mMaps = new ArrayList<>();
            mRecentMaps = new ArrayList<>();
            mListener = (view, position) -> RxBus.publish(RxBus.SUBJECT_MAP_CARD_CLICKED, getItem(position));
        }

        public List<String> getMaps() {
            return mMaps;
        }

        public void updateRecent(@NonNull List<String> maps) {
            mRecentMaps.clear();
            mRecentMaps.addAll(maps);
        }

        public void updateData(@NonNull List<String> maps) {
            mMaps.clear();
            mMaps.addAll(maps);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            RecyclerView.ViewHolder holder;
            if (viewType == VIEW_TYPE_ROW) {
                View v = LayoutInflater.from(context).inflate(R.layout.view_map_card, parent, false);
                holder = new MapAdapter.RowViewHolder(v, mListener);
            } else {
                TextView header = new TextView(context);
                header.setTextColor(ContextCompat.getColor(context, R.color.title_text));
                header.setTypeface(null, Typeface.BOLD);
                header.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.title_text_size));

                RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.topMargin = context.getResources().getDimensionPixelSize(R.dimen.activity_padding);
                params.bottomMargin = context.getResources().getDimensionPixelSize(R.dimen.activity_padding);
                header.setLayoutParams(params);

                holder = new MapAdapter.HeaderViewHolder(header);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MapAdapter.RowViewHolder) {
                String name = getItem(position);
                MapAdapter.RowViewHolder rowHolder = (MapAdapter.RowViewHolder) holder;
                rowHolder.mTitleView.setText(name);
            } else if (holder instanceof MapAdapter.HeaderViewHolder) {
                MapAdapter.HeaderViewHolder headerHolder = (MapAdapter.HeaderViewHolder) holder;
                if (position == 0 && mRecentMaps.size() > 0) {
                    headerHolder.mHeaderView.setText(R.string.home_recent_maps);
                } else {
                    headerHolder.mHeaderView.setText(R.string.home_all_maps);
                }
            }
        }

        public String getItem(int position) {
            String result = null;
            if (position > 0 && position < mRecentMaps.size() + 1) {
                result = mRecentMaps.get(position - 1);
            } else if (position > (mRecentMaps.size() + (mRecentMaps.size() > 0 ? 1 : 0))) {
                result = mMaps.get(position - 1 - (mRecentMaps.size() + (mRecentMaps.size() > 0 ? 1 : 0)));
            }

            return result;
        }

        @Override
        public int getItemCount() {
            // Add 1 for the header and another 1 if we need to show the recent header
            return mMaps.size() + 1 + (mRecentMaps.size() > 0 ? (mRecentMaps.size() + 1) : 0);
        }

        @Override
        public int getItemViewType(int position) {
            return (position == 0 || (mRecentMaps.size() > 0 && position == (mRecentMaps.size() + 1))) ? VIEW_TYPE_HEADER : VIEW_TYPE_ROW;
        }

        static class HeaderViewHolder extends RecyclerView.ViewHolder {

            TextView mHeaderView;

            HeaderViewHolder(TextView header) {
                super(header);
                mHeaderView = header;
            }
        }

        static class RowViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private RecyclerViewClickListener mListener;

            @BindView(R.id.view_map_card_textview_title) TextView mTitleView;

            RowViewHolder(View v, RecyclerViewClickListener listener) {
                super(v);
                ButterKnife.bind(this, v);
                mListener = listener;
                v.setOnClickListener(this);

                FontHelper.setFont(v.getContext(), FontHelper.FONT_MEDIUM, mTitleView);
            }

            @Override
            public void onClick(View view) {
                mListener.onClick(view, getAdapterPosition());
            }
        }
    }
}
