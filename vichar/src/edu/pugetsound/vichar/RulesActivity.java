package edu.pugetsound.vichar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;

/**
 * Activity for the "Rules" screen, blank screen with button to main menu at this point.  
 * @author Davis Shurbert 
 * @version 10/16/12
 */
public class RulesActivity extends WifiRequiredActivity {
    
    final Context context = this;

/*
 * (non-Javadoc)
 * @see android.app.Activity#onCreate(android.os.Bundle)
 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        createButtons();
    }

    private void createButtons()   {
        
        Button mainmenub = (Button)findViewById(R.id.main_menu_button); //declaring the button
          mainmenub.setOnClickListener(mainMenuListener); //making the thing that checks if the button's been pushed
      }

      private OnClickListener mainMenuListener = new OnClickListener() { //sets what happens when the button is pushed
        public void onClick(View v) { 
            
            startActivity(new Intent(context, MainMenuActivity.class));
          }
         };
         
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_rules, menu);
        return true;
    }
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
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
