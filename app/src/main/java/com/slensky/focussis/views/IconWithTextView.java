package com.slensky.focussis.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slensky.focussis.R;

/**
 * Created by slensky on 5/16/17.
 */

public class IconWithTextView extends FrameLayout {

    private Drawable icon;
    private String text;
    private String hint;

    private RelativeLayout layout;
    private ImageView iconView;
    private TextView textView;
    private TextView hintView;

    public IconWithTextView(@NonNull Context context) {
        this(context, null);
    }

    public IconWithTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.IconWithTextView,
                0, 0
        );
        try {
            icon = a.getDrawable(R.styleable.IconWithTextView_icon);
            text = a.getString(R.styleable.IconWithTextView_text);
            hint = a.getString(R.styleable.IconWithTextView_hint);
        } finally {
            a.recycle();
        }

        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context
     *           the current context for the view.
     */
    private void initializeViews(Context context) {
        inflate(getContext(), R.layout.view_icon_with_text, this);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        layout.setClickable(false);
        super.setOnClickListener(l);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        layout = findViewById(R.id.relative_layout);
        iconView = (ImageView) findViewById(R.id.row_icon);
        iconView.setImageDrawable(icon);
        iconView.setColorFilter(Color.argb(132, 0, 0, 0), PorterDuff.Mode.MULTIPLY);
        textView = (TextView) findViewById(R.id.text_main);
        textView.setText(text);
        hintView = (TextView) findViewById(R.id.text_hint);
        hintView.setText(hint);
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
        iconView.setImageDrawable(icon);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        textView.setText(text);
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
        hintView.setText(hint);
    }
}
