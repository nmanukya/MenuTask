package com.example.menutask.adapter;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.menutask.ui.HomeView;
import com.example.menutask.ui.SavedView;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    Bundle bundle;

   public PagerAdapter(FragmentManager fm, int NumOfTabs, Bundle bundle) {
    //public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.bundle = bundle;
    }

    @Nullable
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                HomeView tab1 = new HomeView();
                if(bundle != null){
                    tab1.setArguments(bundle);
                }

                return tab1;
            case 1:
                SavedView tab2 = new SavedView();
                // tab2.setArguments(bundle);
                return tab2;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }
}
