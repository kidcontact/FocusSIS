package com.slensky.focussis.ui.about;

import com.slensky.focussis.ui.base.BasePresenter;
import com.slensky.focussis.ui.base.MvpView;

import javax.inject.Inject;

public class AboutPresenter
        extends BasePresenter<AboutContract.ViewActions>
        implements AboutContract.UserActions<AboutContract.ViewActions> {

    @Inject
    public AboutPresenter() {

    }

    @Override
    public void onEmailClick() {
        if (isViewAttached()) {
            view.sendEmail();
        }
    }

    @Override
    public void onGithubClick() {
        if (isViewAttached()) {
            view.showGithubPage();
        }
    }

}
