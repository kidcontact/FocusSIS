package com.slensky.focussis.views;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by slensky on 4/12/18.
 */

public class AlignmentAwareTextInputLayout extends TextInputLayout {

    public AlignmentAwareTextInputLayout(Context context) {
        super(context);
    }

    public AlignmentAwareTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlignmentAwareTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getBaseline()
    {
        EditText editText = getEditText();
//        return getMeasuredHeight() - (editText.getMeasuredHeight() - editText.getBaseline());
        return  editText.getBaseline();
    }

}
