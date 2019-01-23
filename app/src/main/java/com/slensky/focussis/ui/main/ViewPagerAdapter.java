package com.slensky.focussis.ui.main;

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
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public void setFragmentList(Collection<Fragment> fragmentList) {
        this.mFragmentList.clear();
        this.mFragmentList.addAll(fragmentList);
    }

    public void clear() {
        mFragmentList.clear();
        mFragmentTitleList.clear();
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position < mFragmentTitleList.size()) {
            return mFragmentTitleList.get(position);
        }
        return null;
    }

    public List<Fragment> getFragments() {
        return mFragmentList;
    }

}
