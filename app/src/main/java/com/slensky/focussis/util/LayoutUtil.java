package com.slensky.focussis.util;

import android.content.Context;

/**
 * Created by slensky on 4/15/18.
 */

public class LayoutUtil {
    public static int dpToPixels(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
