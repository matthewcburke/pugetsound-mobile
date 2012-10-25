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

//Screen to set settings, 
public class SettingsActivity extends Activity {

    final Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        createButtons();
    }
    
    //creates buttons
    public void createButtons()   {
        
        Button mainmenub = (Button)findViewById(R.id.main_menu_button); //declaring the button
          mainmenub.setOnClickListener(mainMenuListener); //making the thing that checks if the button's been pushed
        Button aboutb = (Button)findViewById(R.id.about_button);
          aboutb.setOnClickListener(aboutListener); 
      }

    //Creates listeners on buttons to see if they've been pushed.
      private OnClickListener mainMenuListener = new OnClickListener() { //sets what happens when the button is pushed
        public void onClick(View v) { 
            //What happens when button is pushed
            startActivity(new Intent(context, MainMenuActivity.class));
          }
         };
      private OnClickListener aboutListener = new OnClickListener() { //sets what happens when the button is pushed
        public void onClick(View v) { 
                      
            startActivity(new Intent(context, AboutActivity.class));
          }
         };           

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handles item selection in menu
    	switch (item.getItemId()) {
            case R.id.enter_main_menu:
            	startActivity(new Intent(this, MainMenuActivity.class));
                return true;
            case R.id.enter_about:
            	startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
    	}
    }
}
