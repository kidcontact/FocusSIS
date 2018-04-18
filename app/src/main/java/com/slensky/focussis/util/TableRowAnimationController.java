package com.slensky.focussis.util;

import android.content.Context;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.slensky.focussis.R;

/**
 * Created by slensky on 4/15/18.
 */

public class TableRowAnimationController {
    public static final int initialAnimationStartOffset = 50;
    public static final int animationOffsetAdditiveFactor = 46;
    public static final double animationOffsetMultiplicativeFactor = 0.8;
    private Context context;
    private int animationStartOffset = initialAnimationStartOffset;
    private int count;

    public TableRowAnimationController(Context context) {
        this.context = context;
    }

    public Animation nextAnimation() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_with_slide);
        animation.setStartOffset(animationStartOffset);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animationStartOffset += animationOffsetAdditiveFactor * Math.pow(animationOffsetMultiplicativeFactor, count);
        count += 1;

        return animation;
    }

}
