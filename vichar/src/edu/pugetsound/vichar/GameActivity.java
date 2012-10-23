package edu.pugetsound.vichar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
//Import Fragment dependencies
import android.support.v4.app.FragmentActivity;

/**
 * The in-game Activity
 * Extends FragmentActivity to ensure support for Android 2.x
 */
public class GameActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_game, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handles item selection in menu
    	switch (item.getItemId()) {
            case R.id.enter_main_menu:
            	startActivity(new Intent(this, MainMenuActivity.class));
                return true;
            case R.id.enter_tweet:
            	startActivity(new Intent(this, TweetActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
    	}
    }
}
