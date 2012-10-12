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
        this.authorizeTwitter();
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
    /*public void sendMessage(View view) 
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
    }*/
	
    /**
     * Authorize user
     */
    private void authorizeTwitter()
	{
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(
				this.getString(R.string.oauth_consumer_key), 
				this.getString(R.string.oauth_consumer_secret));
    	
		try
		{
			RequestToken requestToken = twitter.getOAuthRequestToken();
			showTwPinDialog(requestToken.getAuthorizationURL());
		}
		catch (TwitterException te)
		{
			if(401 == te.getStatusCode())
	        {
	        	System.out.println("Unable to get the request token.");
	        } else{
	        	te.printStackTrace();
	        }
		}	    
	}
    
    /**
     * Calls Oauth dialog for logging into Twitter
     */
    public void showTwPinDialog(String url)
    {
    	DialogFragment dialog = TwitterOauthPinDialog.newInstance(this, url);
    	dialog.show(getSupportFragmentManager(), "TwitterOauthPinDialog");
    	//((TwitterOauthPinDialog) dialog).loadUrl();
    }
    
    /**
     * 
     */
    public void onDialogPositiveClick(DialogFragment dialog, String pin)
    {
	    try
	    {
	    	AccessToken accessToken = twitter.getOAuthAccessToken(twitter.getOAuthRequestToken(), pin);
            //persist the access token
            storeAccessToken((int)twitter.verifyCredentials().getId(), accessToken);
	    } 
	    catch (TwitterException te) 
	    {
	        if(401 == te.getStatusCode())
	        {
	        	System.out.println("Unable to get the access token.");
	        } else{
	        	te.printStackTrace();
	        }
	    }
	    // Remove the pin dialog
	    dialog.dismiss();  	
    }
    
    /**
     * If the dialog is cancelled, dismiss it
     */
    public void onDialogNegativeClick(DialogFragment dialog)
    {
    	dialog.dismiss();
    }
    
    private void storeAccessToken(int useId, AccessToken accessToken)
    {
		PreferenceUtility prefs = new PreferenceUtility();
		prefs.saveString(this.getString(R.string.oauth_access_token), accessToken.getToken(), this);
		prefs.saveString(this.getString(R.string.oauth_token_secret), accessToken.getTokenSecret(), this);
	}
}
