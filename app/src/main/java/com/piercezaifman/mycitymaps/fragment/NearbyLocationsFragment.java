package com.piercezaifman.mycitymaps.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.RecyclerViewClickListener;
import com.piercezaifman.mycitymaps.base.BaseFragment;
import com.piercezaifman.mycitymaps.data.MapLocation;
import com.piercezaifman.mycitymaps.util.FontHelper;
import com.piercezaifman.mycitymaps.util.RxBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to display a list of nearby locations, sorted by distance.
 * <p>
 * Created by piercezaifman on 2016-12-12.
 */

public class NearbyLocationsFragment extends BaseFragment {

    private static final float MAX_METERS = 1000;
    private static final float MAX_KILOMETERS = 1000;

    @BindView(R.id.fragment_nearby_locations_recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.fragment_nearby_locations_progressbar) ProgressBar mProgressBar;

    private LocationsAdapter mAdapter;

    public static NearbyLocationsFragment newInstance() {
        return new NearbyLocationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxBus.subscribe(RxBus.SUBJECT_MAP_UPDATED, this, (list) -> {
            if (mAdapter != null) {
                mAdapter.updateData((List<MapLocation>) list);
                mProgressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nearby_locations, container, false);
        ButterKnife.bind(this, v);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new LocationsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    // Can't be private because butterknife needs access
    static class LocationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_ROW = 1;

        private List<MapLocation> mDataset;
        private RecyclerViewClickListener mListener;

        LocationsAdapter() {
            mDataset = new ArrayList<>();
            mListener = (view, position) -> RxBus.publish(RxBus.SUBJECT_MAP_LOCATION_CLICKED, mDataset.get(position - 1).getId());
        }

        public void updateData(List<MapLocation> dataset) {
            mDataset = new ArrayList<>();
            mDataset.addAll(dataset);
            Collections.sort(mDataset, (m1, m2) -> {
                int result = ((Float) m1.getDistance()).compareTo(m2.getDistance());
                if (result == 0) {
                    result = m1.getTitle().compareTo(m2.getTitle());
                }

                return result;
            });

            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            RecyclerView.ViewHolder holder;
            if (viewType == VIEW_TYPE_ROW) {
                View v = LayoutInflater.from(context).inflate(R.layout.view_nearby_location_card, parent, false);
                holder = new RowViewHolder(v, mListener);
            } else {
                TextView header = new TextView(context);
                header.setTextColor(ContextCompat.getColor(context, R.color.title_text));
                header.setTypeface(null, Typeface.BOLD);
                header.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.title_text_size));

                RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.topMargin = context.getResources().getDimensionPixelSize(R.dimen.activity_padding);
                params.bottomMargin = context.getResources().getDimensionPixelSize(R.dimen.activity_padding);
                header.setLayoutParams(params);

                holder = new HeaderViewHolder(header);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof RowViewHolder) {
                MapLocation location = mDataset.get(position - 1);
                RowViewHolder rowHolder = (RowViewHolder) holder;
                rowHolder.mTitleView.setText(location.getTitle());

                float distance = location.getDistance();

                if (distance < 0) {
                    rowHolder.mSubtitleView.setVisibility(View.GONE);
                } else {
                    rowHolder.mSubtitleView.setVisibility(View.VISIBLE);
                    String formattedDistance;
                    if (distance > MAX_METERS) {
                        float distanceKm = distance / MAX_METERS;
                        if (distanceKm > MAX_KILOMETERS) {
                            formattedDistance = App.getApp().getString(R.string.nearby_locations_distance_kilometers_max);
                        } else {
                            formattedDistance = String.format(App.getApp().getString(R.string.nearby_locations_distance_kilometers), distance / MAX_METERS);
                        }
                    } else {
                        formattedDistance = String.format(App.getApp().getString(R.string.nearby_locations_distance_meters), distance);
                    }
                    rowHolder.mSubtitleView.setText(formattedDistance);
                }
            } else if (holder instanceof HeaderViewHolder) {
                HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
                if (position == 0) {
                    headerHolder.mHeaderView.setText(R.string.nearby_locations_header);
                }
            }
        }

        @Override
        public int getItemCount() {
            // Add 1 for the header
            return mDataset.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ROW;
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

            @BindView(R.id.view_nearby_location_card_textview_title) TextView mTitleView;
            @BindView(R.id.view_nearby_location_card_textview_subtitle) TextView mSubtitleView;

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
