package com.example.maychatapplication.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.maychatapplication.Fragment.ListChatFragment;
import com.example.maychatapplication.Fragment.ListGroupFragment;
import com.example.maychatapplication.Fragment.ListUserFragment;


public class MainViewPagerAdapter extends FragmentStatePagerAdapter {
    public MainViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ListChatFragment();

            case 1:
                return new ListUserFragment();

            case 2:
                return new ListGroupFragment();

            default:
                return new ListChatFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
