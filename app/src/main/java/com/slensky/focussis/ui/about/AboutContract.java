package com.slensky.focussis.ui.about;

import com.slensky.focussis.ui.base.MvpPresenter;
import com.slensky.focussis.ui.base.MvpView;

public interface AboutContract {

    interface ViewActions extends MvpView {
        void sendEmail();

        void showGithubPage();
    }

    interface UserActions<V extends MvpView> extends MvpPresenter<V> {
        void onEmailClick();

        void onGithubClick();
    }

}
