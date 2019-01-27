package com.slensky.focussis.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.slensky.focussis.FocusApplication;
import com.slensky.focussis.di.component.ActivityComponent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment implements MvpView {

    private final static String TAG = "BaseFragment";

    private BaseActivity activity;
    private Unbinder unbinder;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            this.activity = (BaseActivity) context;
        }
        ((FocusApplication) activity.getApplication()).getComponent().inject(this);
    }

    protected ActivityComponent getActivityComponent() {
        if (activity != null) {
            return activity.getActivityComponent();
        }
        return null;
    }

    protected abstract void setupView(View view);

    public void setUnbinder(Unbinder unbinder) {
        this.unbinder = unbinder;
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }

}
