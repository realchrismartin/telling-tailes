package com.telling.tailes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.telling.tailes.R;;

//Each activity owns its toolbar. to make this toolbar apply to other activities:
//use lines 25-26; functions: onCreateOptionsMenu(), onOptionsItemSelected();

public class HTestActivity extends AppCompatActivity {
    Toolbar mainToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h_test); //this should be the specific activity

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

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

