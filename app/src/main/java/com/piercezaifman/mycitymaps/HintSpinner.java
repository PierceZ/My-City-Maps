package com.piercezaifman.mycitymaps;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Collection;

/**
 * Spinner to show a hint in the first row.
 *
 * Created by piercezaifman on 2017-01-06.
 */

public class HintSpinner extends AppCompatSpinner {

    private OnItemPickedListener mListener;
    private HintAdapter mAdapter;

    public HintSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        String hint;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HintSpinner, 0, 0);

        try {
            hint = a.getString(R.styleable.HintSpinner_hint);
        } finally {
            a.recycle();
        }
        if (hint == null) {
            throw new RuntimeException("No hint provided");
        }

        mAdapter = new HintAdapter(context, android.R.layout.simple_spinner_item, hint);
        setAdapter(mAdapter);

        super.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0 && mListener != null) {
                    mListener.onItemSelected(mAdapter.getItem(i));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public String getSelectedItem() {
        return (String) super.getSelectedItem();
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        throw new RuntimeException("Use setOnItemPickedListener instead");
    }

    public void setOnItemPickedListener(OnItemPickedListener onItemPickedListener) {
        mListener = onItemPickedListener;
    }

    public void setData(Collection<String> data) {
        mAdapter.clear();
        mAdapter.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

    public interface OnItemPickedListener {
        void onItemSelected(String item);
    }

    /**
     * Adapter used to show a hint before selecting.
     *
     * Created by piercezaifman on 2017-01-06.
     */

    public static class HintAdapter extends ArrayAdapter<String> {

        private String mHint = "";

        public HintAdapter(@NonNull Context context, int resource, @NonNull String hint) {
            super(context, resource);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mHint = hint;
        }

        @Override
        public int getCount() {
            return super.getCount() + 1;
        }

        @Nullable
        @Override
        public String getItem(int position) {
            String item;
            if (position == 0) {
                item = mHint;
            } else {
                item = super.getItem(position - 1);
            }
            return item;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            if (position == 0) {
                view.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.secondary_text));
            } else {
                view.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.primary_text));
            }
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            if (position == 0) {
                view.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.secondary_text));

                // making the view clickable makes it consume the clicks so nothing will happen
                view.setClickable(true);
            } else {
                view.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.primary_text));

                // making the view NOT clickable prevents it from consuming click events
                view.setClickable(false);
            }
            return view;
        }

    }
}
