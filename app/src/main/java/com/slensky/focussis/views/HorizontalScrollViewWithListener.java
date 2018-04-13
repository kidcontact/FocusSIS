package com.slensky.focussis.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by slensky on 4/12/18.
 */

public class HorizontalScrollViewWithListener extends HorizontalScrollView {

    private boolean mCurrentlyTouching;
    private boolean mCurrentlyFling;

    public interface HorizontalScrollViewListener {
        public void onScrollChanged(HorizontalScrollViewWithListener scrollView, int x, int y, int oldx, int oldy);
        public void onEndScroll();
    }

    private HorizontalScrollViewListener scrollViewListener = null;

    public HorizontalScrollViewWithListener(Context context) {
        super(context);
    }

    public HorizontalScrollViewWithListener(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollViewWithListener(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setHorizontalScrollViewListener(HorizontalScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);
        mCurrentlyFling = true;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
        }

        if (Math.abs(t - oldt) < 2 || t >= getMeasuredHeight() || t == 0) {
            if(!mCurrentlyTouching){
                if (scrollViewListener != null) {
                    scrollViewListener.onEndScroll();
                }
            }
            mCurrentlyFling = false;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCurrentlyTouching = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mCurrentlyTouching = false;
                if(!mCurrentlyFling){
                    if (scrollViewListener != null) {
                        scrollViewListener.onEndScroll();
                    }
                }
                break;

            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCurrentlyTouching = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mCurrentlyTouching = false;
                if(!mCurrentlyFling){
                    if (scrollViewListener != null) {
                        scrollViewListener.onEndScroll();
                    }
                }
                break;

            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
