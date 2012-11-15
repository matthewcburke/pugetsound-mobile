package edu.pugetsound.vichar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
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
 * Activity for the "Settings" screen
 * @author Davis Shurbert
 * @version 10/13/12 
 */
public class SettingsActivity extends Activity {

    final Context context = this;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        createButtons();
    }
    
    /*
     * Creates buttons
     */
    public void createButtons()   {
        
        Button mainmenub = (Button)findViewById(R.id.main_menu_button); //declaring the button
          mainmenub.setOnClickListener(mainMenuListener); //making the thing that checks if the button's been pushed
        Button aboutb = (Button)findViewById(R.id.about_button);
          aboutb.setOnClickListener(aboutListener); 
        Button twLogout = (Button) findViewById(R.id.logout_twitter_button);
          twLogout.setOnClickListener(twitterListener);
        Button toggleSound = (Button) findViewById(R.id.toggle_sound_button);
          toggleSound.setOnClickListener(soundListener);
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
      private OnClickListener twitterListener = new OnClickListener() {
    	  public void onClick(View v)  {
    		  
    		  logoutTwitter();  //initiate twitter "logout"
    	  }
      };
      private OnClickListener soundListener = new OnClickListener()  {
    	  public void onClick(View v)  {
    		  toggleSound();
    	  }
      };
  		

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_settings, menu);
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
            case R.id.enter_about:
            	startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
    	}
    }
    
    private void logoutTwitter() {
    	//TODO:need log in flags
    }
    
    private void toggleSound() {    	
    	PreferenceUtility pu = new PreferenceUtility();
    	Boolean soundEnabled = pu.returnBoolean(getString(R.string.toggle_sound_key), true, this);
    	
    	Log.d("SoundToggle", "sound enabled pre-set? " + soundEnabled);
    	
    	if(soundEnabled==null || !soundEnabled)  {
    		pu.saveBoolean(getString(R.string.toggle_sound_key), true, this);
    	} else {
    		pu.saveBoolean(getString(R.string.toggle_sound_key), false, this);
    	}
    	
    	pu = new PreferenceUtility();
    	try {
    		Boolean toggleResult = pu.returnBoolean(getString(R.string.toggle_sound_key), true, this);
    		System.out.println("sound enabled post-set? " + toggleResult);
    	} catch (NullPointerException ex) 	{ 
    		System.out.println("null pointer");
    	}    	
    }
}
