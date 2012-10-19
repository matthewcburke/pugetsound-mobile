package edu.pugetsound.vichar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class TestActivity extends Activity {

	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_test, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.enter_game:
            	startActivity(new Intent(this, GameActivity.class));
                return true;
            case R.id.item1:
            	startActivity(new Intent(this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
