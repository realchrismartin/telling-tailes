package com.telling.tailes.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.telling.tailes.R;;

//Each activity owns its toolbar. title set within in case we want to tell users where they are
//
//reference for toolbar impl: https://code.luasoftware.com/tutorials/android/android-setup-appbar-actionbar/
//reference for applying toolbar to real activities:
// https://stackoverflow.com/questions/40929686/constraintlayout-vs-coordinator-layout


public class HTestActivity extends AppCompatActivity {
    Toolbar mainToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h_test); //this should be the specific activity

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mainToolbar.setTitle("");
        setSupportActionBar(mainToolbar);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_hamburger_menu);
        mainToolbar.setOverflowIcon(drawable);
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_feed:  {
                Intent intent = new Intent(this, StoryFeedActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.create_new_story: {
                Intent intent = new Intent(this, CreateStoryActivity.class);
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
                Intent intent = new Intent(this, UserSettingsActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }


}

