package com.slensky.focussis.util;

import android.content.Context;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.slensky.focussis.R;

/**
 * Created by slensky on 5/21/18.
 */

public class CardViewAnimationController {
    public static final int initialAnimationStartOffset = 0;
    public static final int animationOffsetAdditiveFactor = 67;
    private Context context;
    private int animationStartOffset = initialAnimationStartOffset;
    private int count;

    public CardViewAnimationController(Context context) {
        this.context = context;
    }

    public Animation nextAnimation() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in);
        animation.setStartOffset(animationStartOffset);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animationStartOffset += animationOffsetAdditiveFactor;
        count += 1;

        return animation;
    }
}
