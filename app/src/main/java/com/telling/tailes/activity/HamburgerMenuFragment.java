package com.telling.tailes.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.telling.tailes.R;
import com.telling.tailes.databinding.FragmentToolbarBinding;
import com.telling.tailes.util.IntentUtils;


public class HamburgerMenuFragment extends Fragment {
    private FragmentToolbarBinding viewBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentToolbarBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewBinding.mainToolbar.inflateMenu(R.menu.main_menu);
        Drawable drawable = ContextCompat.getDrawable(getContext(),
                R.drawable.ic_hamburger_menu);
        viewBinding.mainToolbar.setOverflowIcon(drawable);
        viewBinding.mainToolbar.setOnMenuItemClickListener(this::handleMenuClick);
    }

    private boolean handleMenuClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_feed:
                // Navigate to settings screen
                Log.d("menu onClick", "VIEW FEED");
                IntentUtils.openStoryFeedActivity(getContext());
                return true;
            case R.id.create_new_story:
                // Save profile changes
                Log.d("menu onClick", "NEW STORY");
                IntentUtils.openCreateStoryActivity(getContext());
                return true;
            default:
                Log.d("menu onClick", "default (ruh roh)");
                return false;
        }
    }


}

