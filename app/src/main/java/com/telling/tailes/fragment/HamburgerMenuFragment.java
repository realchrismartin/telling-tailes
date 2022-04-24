package com.telling.tailes.fragment;

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
import com.telling.tailes.activity.CreateStoryActivity;
import com.telling.tailes.activity.FollowedAuthorsActivity;
import com.telling.tailes.activity.StoryFeedActivity;
import com.telling.tailes.activity.UserSettingsActivity;
import com.telling.tailes.databinding.FragmentToolbarBinding;


public class HamburgerMenuFragment extends Fragment {
    private Intent intent;
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
                intent = new Intent(getContext(), StoryFeedActivity.class);
                startActivity(intent);
                return true;
            case R.id.create_new_story:
                // Save profile changes
                Log.d("menu onClick", "NEW STORY");
                intent = new Intent(getContext(), CreateStoryActivity.class);
                startActivity(intent);
                return true;
            case R.id.view_my_stories:
                Log.d("menu onClick", "VIEW MY STORIES");
                intent = new Intent(getContext(), StoryFeedActivity.class);
                intent.putExtra("feedFilter", "My T(ai)les");
                startActivity(intent);
                return true;
            case R.id.view_my_drafts:
                Log.d("menu onClick", "VIEW MY DRAFTS");
                intent = new Intent(getContext(), StoryFeedActivity.class);
                intent.putExtra("feedFilter", "Drafts");
                startActivity(intent);
                return true;
            case R.id.view_my_bookmarks:
                Log.d("menu onClick", "VIEW BOOKMARKS");
                intent = new Intent(getContext(), StoryFeedActivity.class);
                intent.putExtra("feedFilter", "Bookmarks");
                startActivity(intent);
                return true;
            case R.id.view_saved_authors:
                Log.d("menu onClick", "VIEW FOLLOWED AUTHORS");
                intent = new Intent(getContext(), FollowedAuthorsActivity.class);
                startActivity(intent);
                return true;
            case R.id.view_settings:
                Log.d("menu onClick", "USER SETTINGS");
                intent = new Intent(getContext(), UserSettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                Log.d("menu onClick", "default (ruh roh)");
                return false;
        }
    }


}

