package com.slensky.focussis.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.List;

/**
 * Created by slensky on 4/19/17.
 */

public abstract class NetworkTabAwareFragment extends NetworkFragment implements PageFragment {
    private static final String TAG = "NetworkTabAwareFragment";

    protected List<Fragment> tabFragments = null;
    protected int selectedTab;
    protected String title;

    public abstract boolean hasTabs();

    public abstract List<String> getTabNames();
    public List<Fragment> getTabFragments() {
        return tabFragments;
    }

    public void setTabFragments(List<Fragment> tabFragments) {
        this.tabFragments = tabFragments;
    }

    public void setSelectedTab(int selectedTabIdx) {
        selectedTab = selectedTabIdx;
    }

    public boolean isCurrentFragmentNested() {
        return false;
    }

    public String getTitle() {
        return title;
    }
}
