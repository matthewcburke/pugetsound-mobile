package edu.pugetsound.vichar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Main activity, this is the first screen users will see
 * @author Nathan P
 * @version 10/16/12
 */
public class MainActivity extends Activity
	implements ConnectivityReceiver.ConnectivityListener
{
    final Context context = this;
    private ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createButtons();
        PreferenceUtility prefs = new PreferenceUtility();
        String loginInfo = prefs.returnSavedString(getString(R.string.access_token_key), getString(R.string.prefs_error), this);
        if(loginInfo != getString(R.string.prefs_error)) {
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
    	IntentFilter filter = connectivityReceiver.getIntentFilter();
		registerReceiver(connectivityReceiver, filter);
    }
    
    /**
     * Called whenever the Activity becomes visible
     */
    @Override
    public void onStart() {
    	super.onStart();
    	checkConnection(); //check network connectivity
    }

    @Override
    protected void onPause()  {
    	super.onPause();
    	unregisterReceiver(connectivityReceiver);
    }
    
    public void onConnectivityChange(boolean isConnected) {
    	if(!isConnected) {
    		ConnectionDialog cd = new ConnectionDialog(this);
        	cd.show();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void createButtons() {
        Button menub = (Button)findViewById(R.id.main_menu_button); //declaring the button
        menub.setOnClickListener(menuListener); //making the thing that checks if the button's been pushed
        
    }
    
    private OnClickListener menuListener = new OnClickListener() { //sets what happens when the button is pushed
        public void onClick(View v) { 
            
            startActivity(new Intent(context, MainMenuActivity.class));
        }
       };
       
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
     * Checks network connection status, and prompts user to connect
     * if there is no connection
     */
    private void checkConnection()  {
        ConnectionUtility cu = new ConnectionUtility();
        //if no connection, or connection with no connectivity
        if(cu.checkConnection(this)!=2) {
        	ConnectionDialog cd = new ConnectionDialog(this);
        	cd.show();
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
