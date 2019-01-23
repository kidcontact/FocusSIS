package com.slensky.focussis.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.slensky.focussis.di.component.ActivityComponent;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    private BaseActivity activity;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView(view);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            this.activity = (BaseActivity) context;
        }
    }

    protected ActivityComponent getActivityComponent() {
        if (activity != null) {
            return activity.getActivityComponent();
        }
        return null;
    }

    public BaseActivity getBaseActivity() {
        return activity;
    }

    protected abstract void setupView(View view);

}
