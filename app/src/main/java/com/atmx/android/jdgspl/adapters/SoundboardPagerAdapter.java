/*
 * SoundboardPagerAdapter
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.atmx.android.jdgspl.PagerFragment;

import java.util.ArrayList;

public class SoundboardPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Integer> mTitles;
    private Context mContext;

    public SoundboardPagerAdapter(FragmentManager fm, ArrayList<Integer> titles, Context context) {
        super(fm);
        this.mContext = context;
        this.mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return PagerFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(mTitles.get(position));
    }
}
