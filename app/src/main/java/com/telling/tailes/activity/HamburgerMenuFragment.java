package com.telling.tailes.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.telling.tailes.R;
import com.telling.tailes.databinding.FragmentToolbarBinding;


public class HamburgerMenuFragment extends Fragment {
//    private Toolbar mainToolbar;
    private FragmentToolbarBinding viewBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mainToolbar = (Toolbar) viewBinding.findViewById(R.id.mainToolbar);
//        ((AppCompatActivity) getActivity()).setSupportActionBar(mainToolbar);
        setHasOptionsMenu(true);
//        setMenuVisibility(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentToolbarBinding.inflate(inflater, container, false);
//        setHasOptionsMenu(true);
//        setMenuVisibility(true);
        return viewBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        getActivity().invalidateOptionsMenu();
//        setHasOptionsMenu(true);
        viewBinding.mainToolbar.inflateMenu(R.menu.main_menu);
        Drawable drawable = ContextCompat.getDrawable(getContext(),
                R.drawable.ic_hamburger_menu);
        viewBinding.mainToolbar.setOverflowIcon(drawable);
        /*viewBinding.mainToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    // Navigate to settings screen
                    return true;
                case R.id.action_done:
                    // Save profile changes
                    return true;
                default:
                    return false;
            }
        });*/

        viewBinding.mainToolbar.setNavigationIcon(R.drawable.ic_hamburger_menu);
        viewBinding.mainToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: fix
                Log.d("nav onClick", "done");
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
//        setHasOptionsMenu(true);
        super.onCreateOptionsMenu(menu, inflater);
//        setMenuVisibility(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_feed: {
                Intent intent = new Intent(getContext(), StoryFeedActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.create_new_story: {
                Intent intent = new Intent(getContext(), CreateStoryActivity.class);
                startActivity(intent);
                return true;
            }

//            case R.id.view_my_stories: { #TODO: filter story feed activity for my authored stories
//                Intent intent = new Intent(this, StoryFeedActivity.class);
//                startActivity(intent);
//                return true;
//            }
//            case R.id.view_my_drafts: { #TODO: filter story feed activity for my drafts
//                Intent intent = new Intent(this, StoryFeedActivity.class);
//                startActivity(intent);
//                return true;
//            }
//            case R.id.view_my_bookmarks: { #TODO: filter story feed activity for my bookmarked stories... bookmarked, not liked?
//                Intent intent = new Intent(this, StoryFeedActivity.class);
//                startActivity(intent);
//                return true;
//            }
//            case R.id.view_saved_authors: { #TODO: view list of my saved authors
//                Intent intent = new Intent(this, StoryFeedActivity.class);
//                startActivity(intent);
//                return true;
//            }


            case R.id.view_settings: {
                Intent intent = new Intent(getContext(), UserSettingsActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }


}

