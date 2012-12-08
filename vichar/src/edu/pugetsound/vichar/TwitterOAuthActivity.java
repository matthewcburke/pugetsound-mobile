package edu.pugetsound.vichar;

import edu.pugetsound.vichar.RetrieveAccessToken.AccessTokenCallback;
import edu.pugetsound.vichar.RetrieveRequestToken.RequestTokenCallback;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

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
		//hide title/action bar: let webview fill as much screen as possible
		requestWindowFeature(Window.FEATURE_NO_TITLE);	
		
		//set layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter_oauth);	
		
		//set a couple properties of webview...
		this.webView = (WebView) findViewById(R.id.oauth_webview);			
		this.webView.setWebViewClient(new WebViewClient() 
		{
			//do following to ensure no address bar is visible
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				view.loadUrl(url);
				return false;
			}
			
			//display loading widget on page load
			@Override
	        public void onPageStarted(WebView view, String url, Bitmap bm) {
				ProgressBar loading = (ProgressBar) findViewById(R.id.twitter_loading);
				loading.setVisibility(View.VISIBLE);
				Log.d("UI", "Load started");
		        super.onPageStarted(view, url, bm);
		    }
			
			//dismiss loading widget when URL loaded
			@Override
			public void onPageFinished(WebView view, String url) {
				Log.d("UI", "Load ended");
				try {
					ProgressBar loading = (ProgressBar) findViewById(R.id.twitter_loading);
					loading.setVisibility(View.GONE);
				} catch (NullPointerException ex) {
					Log.d("UI", "no twitter loading widget");
				}
				super.onPageFinished(view, url);
		    }
		});
		
		//fix the focus issues occuring between webview and edittext
		//now, focus will be given to webview when it's selected
		webView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) { 
				case MotionEvent.ACTION_DOWN: 
				case MotionEvent.ACTION_UP: 
					if (!v.hasFocus()) { 
						v.requestFocus(); 
					} 
					break; 
				} 
				return false; 
			}
		});

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

	/**
	 * Called after request token retrieval, either advances to next step or handles failure
	 * @param wrapper TwitterWrapper which contains result of request token retrieval attempt
	 */
	public void PostRequestToken(TwitterWrapper wrapper) {
		if(wrapper.getResult()) {
			requestToken = wrapper.getRequestToken();
			twitter = wrapper.getTwitter();
			oAuthUrl = wrapper.getRequestToken().getAuthorizationURL();
			Log.d("ViChar", "url retrieved");
			Log.d("Vichar", oAuthUrl);
			webView.loadUrl(oAuthUrl);
		} else {
			ConnectionUtility cu = new ConnectionUtility();
			int connected = cu.checkConnection(this);
			//if connection, try again
			if(connected == 2) {
				new RetrieveAccessToken(this, wrapper);
			} else { //if no network connection or poor connectivity, show connection dialog
				ConnectionDialog cd = new ConnectionDialog(this);
				cd.show();
			}
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
	 * @param accessToken access token to be saved
	 */
	private void storeAccessToken(AccessToken accessToken)
	{
		PreferenceUtility prefs = new PreferenceUtility();
		prefs.saveString(getString(R.string.access_token_key), accessToken.getToken(), this);
		prefs.saveString(getString(R.string.access_token_secret_key), (String)accessToken.getTokenSecret(), this);
	}

	/**
	 * Finishes login by advancing to next activity or prompting retry
	 * @param wrapper TwitterWrapper which contains result of access token retrieval attempt
	 */
	public void PostAccessToken(TwitterWrapper wrapper)    {
		if(wrapper.getResult()) 	{
			accessToken = wrapper.getAccessToken(); //temp store access token
			PreferenceUtility pu = new PreferenceUtility();
			pu.saveBoolean(getString(R.string.tw_login_key), true, this); //set logged in flag to true 
			pu.saveString(getString(R.string.screenname_key), wrapper.getScreenName(), this); //save screenname			
			storeAccessToken(accessToken); //save access token
			//and then move on to main menu    		
			Intent mainActIntent = new Intent(this, MainMenuActivity.class);
			startActivity(mainActIntent);
		} else	{
			ConnectionUtility cu = new ConnectionUtility();
			int connected = cu.checkConnection(this);
			//if connection, try again
			if(connected == 2) {
				new RetrieveAccessToken(this, wrapper);
			} else { //if no network connection or poor connectivity, show connection dialog
				ConnectionDialog cd = new ConnectionDialog(this);
				cd.show();
			}
		}
	}   
}
