package com.piercezaifman.mycitymaps;

import android.view.View;

/**
 * Click listener interface used for a recycler view, so you can use the position.
 *
 * Created by piercezaifman on 2016-12-15.
 */

public interface RecyclerViewClickListener {

    void onClick(View view, int position);
}
