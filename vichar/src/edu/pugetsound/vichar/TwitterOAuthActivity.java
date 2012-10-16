package edu.pugetsound.vichar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

/**
 * Provides interface and guts for logging into Twitter via ViChar app
 * @author Nathan Pastor & Michael Dubois
 * @version 10/15/12
 */
public class TwitterOAuthActivity extends Activity {

	Twitter twitter;
	WebView webView;
	String  oAuthUrl;
	RequestToken requestToken;
	
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
		try
		{
			requestToken = twitter.getOAuthRequestToken();
			Log.d("ViChar", "request token retrieved");
			oAuthUrl = requestToken.getAuthorizationURL();
			Log.d("ViChar", "url retrieved");
			//WebView webview = (WebView) findViewById(R.id.twitter_auth_webview);
			Log.d("Vichar", oAuthUrl);
			System.out.println(this.webView);
			this.webView.loadUrl(oAuthUrl);
		}
		catch (TwitterException te)
		{
			System.out.println(te.getStatusCode());
			if(401 == te.getStatusCode())
	        {
	        	System.out.println("Unable to get the request token.");
	        } else{
	        	te.printStackTrace();
	        }
		}
		catch (IllegalStateException ex)
		{
			System.out.println(ex);
		}
	}
    
    /**
     * Parses authorization pin entered by user, and finishes Twitter
     * authorization process by retrieving Oauth access token
     * @param view The calling view
     */
    public void sendPin(View view)
    {
    	EditText editText = (EditText) findViewById(R.id.twitter_auth_pin);
		String pin = editText.getText().toString();
		System.out.println(pin);		
		AccessToken accessToken = null;
		try{
	         if(pin.length() > 0)
	         {
	        	 accessToken = twitter.getOAuthAccessToken(requestToken, pin);
	         }else
	         {
	        	 accessToken = twitter.getOAuthAccessToken();
	         }
	         System.out.println("pin received...now saving accessToken");
	         storeAccessToken(accessToken);
	    } 
		catch (TwitterException te) 
		{
	        if(401 == te.getStatusCode()){
	          System.out.println("Unable to get the access token.");
	        }else{
	          te.printStackTrace();
	        }
		}
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
}
