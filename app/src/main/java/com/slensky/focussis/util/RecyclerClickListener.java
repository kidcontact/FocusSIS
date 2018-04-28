package com.slensky.focussis.util;

import android.view.View;

/**
 * Created by slensky on 4/20/18.
 */

public interface RecyclerClickListener {

    /**
     * Interface for Recycler View Click listener
     **/

    void onClick(View view, int position);

    void onLongClick(View view, int position);

}
