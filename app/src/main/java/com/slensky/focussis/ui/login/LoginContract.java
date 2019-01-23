package com.slensky.focussis.ui.login;

import com.slensky.focussis.data.network.FocusDebugApi;
import com.slensky.focussis.ui.base.MvpPresenter;
import com.slensky.focussis.ui.base.MvpView;

public class LoginContract {

    public interface ViewActions extends MvpView {
        void showSchoolMessage();

        void showAuthenticatingProgress();

        void hideAuthenticatingProgress();

        void showLanguageChangeProgress();

        void hideLanguageChangeProgress();

        String getUsername();

        void setUsername(String username);

        void setUsernameBlankError(boolean usernameBlankError);

        String getPassword();

        void setPassword(String password);

        void setPasswordBlankError(boolean passwordBlankError);

        boolean getRememberMe();

        void setRememberMe(boolean saveLogin);

        boolean isDisableAutoSignIn();

        void showServerError();

        void showTimeoutError();

        void showAuthError();

        void showSchoolVerificationAuthError();

        void showLanguageError();

        boolean checkIsDebugCredentials(String user, String pass);

        FocusDebugApi getDebugApiInstance();

        void startMainActivity(String user, String pass);
    }

    public interface UserActions<V extends MvpView> extends MvpPresenter<V> {
        void onLogin();

        void onReadSchoolMessage();

        void onChangeLanguageToEnglish();
    }

}
