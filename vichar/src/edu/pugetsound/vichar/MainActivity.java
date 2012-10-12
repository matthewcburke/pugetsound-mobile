package edu.pugetsound.vichar;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;


public class MainActivity extends FragmentActivity 
							implements TwitterOauthPinDialog.TwitterOauthPinDialogListener 
{
	private Twitter twitter;
	
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
    
    /**
     * Calls Oauth dialog for logging into Twitter
     */
    public void showTwPinDialog(String url)
    {
    	DialogFragment dialog = TwitterOauthPinDialog.newInstance(this);
    	dialog.setUrl(url);
    	dialog.show(getSupportFragmentManager(), "TwitterOauthPinDialog");
    }
    
    /**
     * 
     */
    public void onDialogPositiveClick(DialogFragment dialog, String pin)
    {
    	AccessToken accessToken = null;
	    try
	    {
            accessToken = twitter.getOAuthAccessToken(twitter.getOAuthRequestToken(), pin);
	    } 
	    catch (TwitterException te) 
	    {
	        if(401 == te.getStatusCode())
	        {
	        	System.out.println("Unable to get the access token.");
	        }else{
	        	te.printStackTrace();
	        }
	    }
    
	    //persist to the accessToken for future reference.
	    try
	    {
	    	storeAccessToken((int)twitter.verifyCredentials().getId(), accessToken);
	    }
	    catch (TwitterException e)
	    {
	    	//lol
	    }
	    dialog.dismiss();    	
    }
    
    /**
     * 
     */
    public void onNegativeClick(DialogFragment dialog)
    {
    	dialog.dismiss();
    }
	
    /**
     * Authorize user
     */
    private void authorize()
	{
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(getString(R.string.oauth_consumer_key))
		  .setOAuthConsumerSecret(getString(R.string.oauth_consumer_secret))
		  .setOAuthAccessToken("")
		  .setOAuthAccessTokenSecret("");
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
    	
		try
		{
			RequestToken requestToken = twitter.getOAuthRequestToken();
			showTwPinDialog(requestToken.getAuthorizationURL());
		}
		catch (TwitterException e)
		{
			
		}	    
	}
    

	  private static void storeAccessToken(int useId, AccessToken accessToken)
	  {
		  PreferenceUtility prefs = new PreferenceUtility();
		  prefs.saveString(getString(R.string.oauth_access_token), accessToken.getToken(), this);
		  prefs.saveString(getString(R.string.oauth_token_secret), accessToken.getTokenSecret(), this);
	}
}
