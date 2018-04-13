package com.slensky.focussis.views;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by slensky on 4/11/18.
 */

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {
    private static final String TAG = "ScrollAwareFABBehavior";
    private final Handler handler = new Handler();
    private final int reappearDelay = 3000;

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                        nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull final FloatingActionButton child,
                               @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed);

        // make fab reappear after 3 secs
        handler.removeCallbacksAndMessages(null);
        if (dyConsumed > 0 || (dyConsumed == 0 && !child.isShown())) {
            Runnable showFab = new Runnable() {
                @Override
                public void run() {
                    child.show();
                }
            };
            handler.postDelayed(showFab, reappearDelay);
        }

        // show/hide fab on scroll up/down
        if (dyConsumed > 0 && child.isShown()) {
            child.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    fab.setVisibility(View.INVISIBLE);
                }
            });
        } else if (dyConsumed < 0 && !child.isShown()) {
            child.show();
        }
    }

    public void onHorizontalScroll(final FloatingActionButton child, View target, int x, int y, int oldx, int oldy) {
        if (!child.isShown()) {
            handler.removeCallbacksAndMessages(null);
            Runnable showFab = new Runnable() {
                @Override
                public void run() {
                    child.show();
                }
            };
            handler.postDelayed(showFab, reappearDelay);
        }
    }

}
