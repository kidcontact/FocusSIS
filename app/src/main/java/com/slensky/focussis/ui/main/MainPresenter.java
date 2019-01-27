package com.slensky.focussis.ui.main;

import android.text.TextUtils;

import com.slensky.focussis.data.network.FocusApi;
import com.slensky.focussis.ui.base.BasePresenter;
import com.slensky.focussis.util.SchoolSingleton;

import org.apache.commons.lang.WordUtils;

import javax.inject.Inject;

public class MainPresenter
        extends BasePresenter<MainContract.ViewActions>
        implements MainContract.UserActions<MainContract.ViewActions> {

    private FocusApi api;

    private String username;
    private String password;

    @Inject
    public MainPresenter(FocusApi api) {
        this.api = api;
    }

    @Override
    public void onAttach(MainContract.ViewActions view) {
        super.onAttach(view);
        username = view.getUsername();
        password = view.getPassword();

        setupNavigation();
        onNavigationItemSelected(view.getSelectedNavigationItem());
    }

    private void setupNavigation() {
        if (isViewAttached()) {
            // show the user's name as the header
            String header = TextUtils.join(" ", username.split("\\."));
            view.setNavigationHeader(WordUtils.capitalizeFully(header));

            // show the user's email as the subheader
            String subheader = username;
            String domain = SchoolSingleton.getInstance().getSchool().getDomainName();
            if (domain != null) {
                subheader += "@" + domain;
            }
            view.setNavigationSubheader(subheader);
        }
    }

    @Override
    public void onNavigationItemSelected(MainContract.NavigationItem navigationItem) {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onDestroyActionMode() {

    }

    @Override
    public void onActionItemClicked() {

    }

}
