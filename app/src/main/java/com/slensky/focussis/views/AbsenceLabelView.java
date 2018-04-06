package com.slensky.focussis.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slensky.focussis.R;
import com.slensky.focussis.data.Absences;

/**
 * Created by slensky on 5/30/17.
 */

public class AbsenceLabelView extends LinearLayout {

    private Absences.Status status;
    private TextView label;

    public AbsenceLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AbsenceLabelView,
                0, 0
        );
        try {
            int status = a.getInt(R.styleable.AbsenceLabelView_status, -1);
            if (status == 0) {
                this.status = Absences.Status.ABSENT;
            }
            else if (status == 1) {
                this.status = Absences.Status.EXCUSED;
            }
            else if (status == 2) {
                this.status = Absences.Status.LATE;
            }
            else if (status == 3) {
                this.status = Absences.Status.TARDY;
            }
            else if (status == 4) {
                this.status = Absences.Status.MISC;
            }
            else if (status == 5){
                this.status = Absences.Status.OFFSITE;
            }
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
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_absence_label, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setBackgroundResource(R.drawable.absence_label);
        label = (TextView) this.findViewById(R.id.text_label);
        if (status != null) {
            update();
        }
    }

    public void setStatus(Absences.Status status) {
        this.status = status;
        label = (TextView) this.findViewById(R.id.text_label);
        update();
    }

    private void update() {
        Drawable background = this.getBackground();
        int red = Color.parseColor("#F44336");
        int green = Color.parseColor("#4CAF50");
        if (status == Absences.Status.ABSENT || status == Absences.Status.EXCUSED) {
            if (background instanceof ShapeDrawable) {
                ((ShapeDrawable)background).getPaint().setColor(red);
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable)background).setColor(red);
            } else if (background instanceof ColorDrawable) {
                ((ColorDrawable)background).setColor(red);
            }
            label.setTextColor(ContextCompat.getColor(getContext(), R.color.textPrimaryDark));
        }
        else {
            if (background instanceof ShapeDrawable) {
                ((ShapeDrawable)background).getPaint().setColor(green);
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable)background).setColor(green);
            } else if (background instanceof ColorDrawable) {
                ((ColorDrawable)background).setColor(green);
            }
            label.setTextColor(ContextCompat.getColor(getContext(), R.color.textPrimaryDark));
        }
        switch (status) {
            case ABSENT:
                label.setText("A");
                break;
            case EXCUSED:
                label.setText("E");
                break;
            case LATE:
                label.setText("L");
                break;
            case TARDY:
                label.setText("T");
                break;
            case MISC:
                label.setText("M");
                break;
            case OFFSITE:
                label.setText("O");
        }
    }

}
