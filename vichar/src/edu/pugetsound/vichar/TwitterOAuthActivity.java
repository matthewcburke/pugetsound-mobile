package edu.pugetsound.vichar;

import edu.pugetsound.vichar.RetrieveAccessToken.AccessTokenCallback;
import edu.pugetsound.vichar.RetrieveRequestToken.RequestTokenCallback;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

/**
 * Provides interface for logging into Twitter via ViChar app
 * @author Nathan Pastor & Michael Dubois
 * @version 10/15/12
 */
public class TwitterOAuthActivity extends Activity
									implements RequestTokenCallback, AccessTokenCallback {

	Twitter twitter;
	WebView webView;
	String  oAuthUrl;
	RequestToken requestToken;
	AccessToken accessToken;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_oauth);
        this.webView = (WebView) findViewById(R.id.oauth_webview);
        //do following to ensure no address bar is visible
        this.webView.setWebViewClient(new WebViewClient()
	        {
	        	public boolean shouldOverrideUrlLoading(WebView view, String url)
	        	{
	        		view.loadUrl(url);
	        		return false;
	        	}        	
	        });
        this.webView.requestFocus();
        Log.d("ViChar", "Activity created, calling authorizeTwitter");
        authorizeTwitter();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_twitter_oauth, menu);
        return true;
    }
    
    /**
     * Authorize user. Launches authorization url.
     */
    private void authorizeTwitter()
	{
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(getString(R.string.oauth_consumer_key), getString(R.string.oauth_consumer_secret));
    	Log.d("ViChar", "twitter initialized");
    	//retrieve request token
    	new RetrieveRequestToken(this, new TwitterWrapper(twitter));
	}
    
    public void PostRequestToken(TwitterWrapper wrapper) {
    	if(wrapper.getResult()) {
    		requestToken = wrapper.getRequestToken();
    		twitter = wrapper.getTwitter();
    		oAuthUrl = wrapper.getRequestToken().getAuthorizationURL();
			Log.d("ViChar", "url retrieved");
			Log.d("Vichar", oAuthUrl);
			webView.loadUrl(oAuthUrl);
    	} else {
    		//TODO: decide behavior if request token retrieval fails
    		System.out.println("request token retireval failed"); 
    	}
    }
    
    /**
     * Parses authorization pin entered by user, and finishes Twitter
     * authorization process by retrieving OAuth access token
     * @param view The calling view
     */
    public void sendPin(View view)
    {
    	//get pin entered by user
    	EditText editText = (EditText) findViewById(R.id.twitter_auth_pin);
		String pin = editText.getText().toString();
		System.out.println(requestToken);
		//retrieve access token, using request token and pin
		TwitterWrapper wrapper = new TwitterWrapper(twitter);
		wrapper.setRequestToken(requestToken);
		wrapper.setPin(pin);
		new RetrieveAccessToken(this, wrapper);		
	}
    
    /**
     * Saves access token to persistent storage, so that user information
     * can be persisted across multiple game sessions
     * @param accessToken
     */
    private void storeAccessToken(AccessToken accessToken)
    {
    	PreferenceUtility prefs = new PreferenceUtility();
    	prefs.saveString(getString(R.string.access_token_key), accessToken.getToken(), this);
    	prefs.saveString(getString(R.string.access_token_secret_key), (String)accessToken.getTokenSecret(), this);
    }
    
    /**
     * Finishes login by advancing to next activity or prompting retry
     * @param loginResult Result of login attempt
     */
    public void PostAccessToken(TwitterWrapper wrapper)    {
    	if(wrapper.getResult()) 	{
    		accessToken = wrapper.getAccessToken(); //temp store access token
    		PreferenceUtility pu = new PreferenceUtility();
    		pu.saveString(getString(R.string.tw_login_key), "true", this); //set logged in flag to true 
    		pu.saveString(getString(R.string.screenname_key), wrapper.getScreenName(), this); //save screenname			
    		storeAccessToken(accessToken); //save access token
    		//and then move on to main menu    		
    		Intent mainActIntent = new Intent(this, MainMenuActivity.class);
        	startActivity(mainActIntent);
    	} else	{
    		//TODO: decide what will happen when login fails
    	}
    }   
}
