package edu.pugetsound.vichar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            case R.id.enter_game:
            	startActivity(new Intent(this, GameActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * saves username to persistent storage
     * @param user
     * @param loginType
     */
    private void saveUsername(String user, boolean loginType)
    {
    	Log.d("viCHar","save username commenced");
   		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.username_prefs), 0);
   		SharedPreferences.Editor editor = sharedPref.edit();
   		
    	if(loginType==false) //if this is a Twitter username
    	{    		
    		editor.putString(getString(R.string.twitter_username_key), user);
    		editor.commit();
    	}
    	else  //if this is a ViChar server username
    	{
    		editor.putString(getString(R.string.vichar_username_key), user);
    		editor.commit();
    	}
    	Log.d("viCHar","commited!");
    	
    	checkSave(loginType);
    }
    
    /*
     * Test method, return saved values
     */
    private String checkSave(boolean loginType)
    {
    	String toReturn;
   		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.username_prefs), 0);
   		if (loginType==false)
   		{
   			toReturn = sharedPref.getString(getString(R.string.twitter_username_key), "Fail");
   		}
   		else
   		{
   			toReturn = sharedPref.getString(getString(R.string.vichar_username_key), "Fail");
   		}
   		Log.d("viCHar", toReturn);
   		return toReturn;
    }
    
    /**
     * Called when either ok button pressed
     * @param view
     */
    public void sendMessage(View view) 
    {
    	Log.d("viCHar","send message commenced");

    	if(view.getId()==R.id.ok_tw_username)
    	{
    		EditText editText = (EditText) findViewById(R.id.enter_twitter_username);
    		String message = editText.getText().toString();
    		saveUsername(message, false);    		
    	}
    	else
    	{
    		EditText editText = (EditText) findViewById(R.id.enter_vichar_username);
    		String message = editText.getText().toString();
    		saveUsername(message, true);
    	}  	
    }
    

}
