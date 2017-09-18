package com.piercezaifman.mycitymaps.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.base.NavigationFragment;
import com.piercezaifman.mycitymaps.util.Util;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Allow users to send feedback via email or to direct them to leave a review.
 * <p>
 * Created by piercezaifman on 2016-12-12.
 */

public class FeedbackFragment extends NavigationFragment {

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public int getTitleId() {
        return R.string.nav_feedback;
    }

    @OnClick(R.id.fragment_feedback_card_city)
    public void onClickCityFeedback() {
        Util.showFeedbackDialog(getActivity(), R.string.feedback_title_city, R.string.feedback_missing_city_edittext_hint);
    }

    @OnClick(R.id.fragment_feedback_card_map)
    public void onClickMapFeedback() {
        Util.showFeedbackDialog(getActivity(), R.string.feedback_title_map, R.string.feedback_map_edittext_hint);
    }

    @OnClick(R.id.fragment_feedback_card_general)
    public void onClickGeneralFeedback() {
        Util.showFeedbackDialog(getActivity(), R.string.feedback_title_general, R.string.feedback_general_edittext_hint);
    }

}
