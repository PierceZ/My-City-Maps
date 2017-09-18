package com.piercezaifman.mycitymaps;

import android.content.Context;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

import java.util.List;

/**
 * Adapter for a search view.
 * <p>
 * Created by piercezaifman on 2017-02-27.
 */

public class SearchAdapter extends SimpleCursorAdapter {

    private static final String SEARCH_COLUMN = "search_column";
    private static final int SEARCH_COLUMN_INDEX = 1;

    public SearchAdapter(Context context) {
        super(context,
                R.layout.view_search_row,
                null,
                new String[]{SEARCH_COLUMN},
                new int[]{R.id.view_search_row_textview},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    public String getSuggestion(int position) {
        return ((MatrixCursor)getItem(position)).getString(SEARCH_COLUMN_INDEX);
    }

    public void updateSearchSuggestions(String query, List<String> searchValues) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{ BaseColumns._ID, SEARCH_COLUMN });
        for (int i = 0; i < searchValues.size(); i++) {
            String value = searchValues.get(i);
            if (value.toLowerCase().contains(query.toLowerCase())) {
                cursor.addRow(new Object[] {i, value});
            }
        }
        changeCursor(cursor);
    }
}
