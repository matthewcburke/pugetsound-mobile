package edu.pugetsound.vichar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import android.view.MenuItem;
import android.content.Context;


public class AboutActivity extends Activity {
    
    final Context context = this;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        createButtons();
    }

    public void createButtons()   {
    	Button mainmenub = (Button)findViewById(R.id.main_menu_button); //declaring the button
    	mainmenub.setOnClickListener(mainMenuListener); //making the thing that checks if the button's been pushed  
    }

    private OnClickListener mainMenuListener = new OnClickListener() { //sets what happens when the button is pushed
    	public void onClick(View v) { 
    		startActivity(new Intent(context, MainMenuActivity.class));
    	}
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_about, menu);
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
    
    public void goToTestGameAct(View view)  {
    	Intent testGameActIntent = new Intent(this, TESTGameActivity.class);
    	startActivity(testGameActIntent);
    }
}
