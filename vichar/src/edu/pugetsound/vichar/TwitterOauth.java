package edu.pugetsound.vichar;

import android.app.Activity;
import twitter4j.conf.*;
import twitter4j.*;
import twitter4j.auth.*;

public class TwitterOauth
{
	Twitter twitter; //instance of twitter, stores all relevant data
	
	public TwitterOauth(Activity act)
	{
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(act.getString(R.string.oauth_consumer_key))
		  .setOAuthConsumerSecret(act.getString(R.string.oauth_consumer_secret))
		  .setOAuthAccessToken("")
		  .setOAuthAccessTokenSecret("");
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}
	
	private void authorize()
	{
	    RequestToken requestToken = twitter.getOAuthRequestToken();
	    AccessToken accessToken = null;
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    while (null == accessToken) {
	    	System.out.println("Open the following URL and grant access to your account:");
	    	System.out.println(requestToken.getAuthorizationURL());
	        System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
	        String pin = br.readLine();
	        try{
	        	if(pin.length() > 0){
	            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
	        }else{
	           accessToken = twitter.getOAuthAccessToken();
	         }
	      } catch (TwitterException te) {
	        if(401 == te.getStatusCode()){
	          System.out.println("Unable to get the access token.");
	        }else{
	          te.printStackTrace();
	        }
	      }
	    }
	    //persist to the accessToken for future reference.
	    storeAccessToken(twitter.verifyCredentials().getId() , accessToken);
	    Status status = twitter.updateStatus(args[0]);
	    System.out.println("Successfully updated the status to [" + status.getText() + "].");
	    System.exit(0);
	  }
	  private static void storeAccessToken(int useId, AccessToken accessToken){
	    //store accessToken.getToken()
	    //store accessToken.getTokenSecret()
	}
	
	
}
