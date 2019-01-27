package com.slensky.focussis.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by slensky on 4/20/17.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private Fragment[] fragments = new Fragment[0];
    private String[] fragmentTitles = new String[0];

    ViewPagerAdapter(Fragment[] fragments, String[] fragmentTitles, FragmentManager manager) {
        super(manager);
        this.fragments = fragments;
        this.fragmentTitles = fragmentTitles;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    void setFragments(Fragment[] fragments) {
        this.fragments = fragments;
    }

    public void clear() {
        fragments = new Fragment[0];
        fragmentTitles = new String[0];
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position < fragmentTitles.length) {
            return fragmentTitles[position];
        }
        return null;
    }

}
