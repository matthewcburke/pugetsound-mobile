package edu.pugetsound.vichar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
							
{
    final Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createButtons();
        checkConnection(); //check network connectivity
        Boolean firstLaunch = checkFirstLaunch(); //check if this the first app launch
        if(firstLaunch) firstLaunch();	//if so, executed appropriate instructions
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onResume()  {
    	super.onResume();
    	checkConnection();
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
            case R.id.enter_game:
            	startActivity(new Intent(this, GameActivity.class));
                return true;
            case R.id.enter_main_menu:
            	startActivity(new Intent(this, MainMenuActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void authorizeLoginwithVichar(View view)
    {
    	Intent login = new Intent(this, LoginActivity.class);
    	startActivity(login);
    }
    
    public void authorizeTwitter(View view)
    {
    	Intent twitterOAuthIntent = new Intent(this, TwitterOAuthActivity.class);
    	startActivity(twitterOAuthIntent);
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
    	try {
    		Boolean firstLaunch = pu.returnBoolean(getString(R.string.first_launch_flag), null, this);
    		if(firstLaunch==true) { 
    			//if first launch is true, set to false (true here has been carried over from previous launch)
    			pu.saveBoolean(getString(R.string.first_launch_flag), false, this);
    		}
    	} catch (NullPointerException ex)  {
    		//if null pointer then this flag hasn't been initialized,
    		//meaning this is first launch
    		pu.saveBoolean(getString(R.string.first_launch_flag), true, this);
    		Log.d("MainAct", "Caught null pointer, first launch! set first launch flag to true");
    		result = true;
    	}
    	System.out.println("first launch? " + result);
    	return result;
    }
    
    /**
     * Executes everything necessary if this is the first time
     * the application is opened
     */
    private void firstLaunch()  {
    	System.out.println("Setting first launch prefs");
    	PreferenceUtility pu = new PreferenceUtility();
    	//toggled sound on
    	pu.saveBoolean(getString(R.string.toggle_sound_key), true, this);
    	System.out.println("supposedly set toggle sound key to true. Did I? checking...");
    	System.out.println(pu.returnBoolean(getString(R.string.toggle_sound_key), false, this));
    }
}
