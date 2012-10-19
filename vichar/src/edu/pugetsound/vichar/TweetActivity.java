package edu.pugetsound.vichar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class TweetActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tweet, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handles item selection in menu
    	switch (item.getItemId()) {
            case R.id.enter_main_menu:
            	startActivity(new Intent(this, MainMenuActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
    	}
    }
}
