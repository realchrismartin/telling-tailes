//package com.telling.tailes.activity;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.ActionMenuView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.PopupMenu;
//import androidx.appcompat.widget.Toolbar;
//
//import androidx.annotation.NonNull;
//
//
//import com.telling.tailes.R;
//
//
// NB: retired in favor of HTestActivity. Differences: this has a left-justified hamburger menu that
// doesn't currently work (possible conflict with Navigation limits) and a constraint layout
//public class HTwoTestActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
//    Toolbar toolbar;
//    //PopupMenu menu;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_h_two_test); // activity for demo's sake only
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("");
//        toolbar.setNavigationIcon(R.drawable.ic_hamburger_menu); //upper left icon of choice determined here
//        Log.d("create", "create?");
//
//        setSupportActionBar(toolbar);
//
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d("navbar", "click");
//                showMenu(view);
//            }
//        });
//
//
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//
//    }
//
//    public void showMenu(View view) {
//        PopupMenu menu = new PopupMenu(this, toolbar);
//        menu.setOnMenuItemClickListener(this);
//        MenuInflater inflater = menu.getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu.getMenu());
//
//        menu.show();
//    }
//    @Override
//        public boolean onMenuItemClick (MenuItem item){
//            switch (item.getItemId()) {
//                case R.id.view_feed:
//                    Intent intent = new Intent(this, StoryFeedActivity.class);
//                    startActivity(intent);
//                    return true;
//                default:
//                    return false;
//            }
//        }
//
//}
//
//

// options menu is the ellipsis menu
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu,menu);
//        return super.onCreateOptionsMenu(menu);
//    }

//        @Override
//        public boolean onOptionsItemSelected (@NonNull MenuItem item){
//            if (item.getItemId() == R.id.view_feed) {
//                Intent intent = new Intent(this, StoryFeedActivity.class);
//                startActivity(intent);
//            }
//            return super.onOptionsItemSelected(item);
//        }
//    }

