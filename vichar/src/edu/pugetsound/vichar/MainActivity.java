package edu.pugetsound.vichar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * Main activity, this is the first screen users will see
 * @author Nathan P
 * @version 10/16/12
 */
public class MainActivity extends WifiRequiredActivity
{
    final Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //dithering
        getWindow().setFormat(PixelFormat.RGBA_8888);
        
        setContentView(R.layout.activity_main);
        PreferenceUtility prefs = new PreferenceUtility();
        Boolean twLoggedIn = prefs.returnBoolean(getString(R.string.tw_login_key), false, this);
        if(twLoggedIn == true) {
        	prefs.saveBoolean(getString(R.string.tw_login_key), true, this);
        	Button button = (Button)findViewById(R.id.login_with_twitter);
        	button.setText("Continue with Twitter");
        }
        Boolean firstLaunch = checkFirstLaunch(); //check if this the first app launch
        if(firstLaunch) firstLaunch();	//if so, executed appropriate instructions
    }
    
    @Override
    protected void onResume()  {
    	super.onResume();
    }
    
    /**
     * Called whenever the Activity becomes visible
     */
    @Override
    public void onStart() {
    	super.onStart();
    }

    @Override
    protected void onPause()  {
    	super.onPause();
    	
    }
    
    @Override
    protected void onStop()  {
    	super.onStop();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
       
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.enter_main_menu:
            	startActivity(new Intent(this, MainMenuActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void authorizeLoginwithVichar(View view)
    {
    	Intent intent = new Intent(this, LoginActivity.class);
    	startActivity(intent);
    }
    
    public void authorizeTwitter(View view)
    {
    	PreferenceUtility prefs = new PreferenceUtility();
    	boolean isLoggedIn = prefs.returnBoolean(getString(R.string.tw_login_key), false, this);
    	if(isLoggedIn == true){
    		Intent intent = new Intent(this, MainMenuActivity.class);
    		startActivity(intent);
    	}
    	else{
    	Intent twitterOAuthIntent = new Intent(this, TwitterOAuthActivity.class);
    	startActivity(twitterOAuthIntent);
    	}
    }
    
    /**
     * Check if this is the first time the application opened
     * @result True if this is first launch
     *         False if this is NOT first launch
     */
    private boolean checkFirstLaunch()  {
    	boolean result = false;
    	PreferenceUtility pu = new PreferenceUtility();

		String firstLaunch = pu.returnSavedString(getString(R.string.first_launch_flag), getString(R.string.prefs_error), this);
		if(firstLaunch==getString(R.string.prefs_error)) { 
			//if error returned, this is first launch.
			pu.saveString(getString(R.string.first_launch_flag), "true", this);
			result = true;
		}
		if(firstLaunch=="true")  {
			//if first launch is true, set to false (true here has been carried over from previous launch)
			pu.saveString(getString(R.string.first_launch_flag), "false", this);
		}
    	return result;
    }
    
    /**
     * Executes everything necessary if this is the first time
     * the application is opened
     */
    private void firstLaunch()  {
    	System.out.println("Setting first launch prefs");
    	PreferenceUtility pu = new PreferenceUtility();
    	//toggle sound on
    	pu.saveBoolean(getString(R.string.toggle_sound_key), true, this);
    	//set twitter logged in flag to false
    	pu.saveBoolean(getString(R.string.tw_login_key), false, this);
    }
}
