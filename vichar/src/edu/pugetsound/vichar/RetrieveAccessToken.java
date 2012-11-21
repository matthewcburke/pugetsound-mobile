package edu.pugetsound.vichar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.os.AsyncTask;

/**
 * Retrieves OAuth access token in non-UI worker thread
 * @author Nathan P
 * @version 11/19/12
 */
public class RetrieveAccessToken extends AsyncTask<TwitterWrapper, TwitterWrapper, TwitterWrapper>
{
	private AccessTokenCallback caller;
	
	public interface AccessTokenCallback {
		public void PostAccessToken(TwitterWrapper wrapper);
	}
	
	public RetrieveAccessToken(AccessTokenCallback caller, TwitterWrapper wrapper) {
		this.caller = caller;
		execute(wrapper);
	}
	
	/**
	 * Retrieves access token
	 * @param Array of Strings, contain OAuth pin entered by user
	 * @return True if successful, false if not
	 */
	@Override
    protected TwitterWrapper doInBackground(TwitterWrapper...wrappers)
    {    		
		TwitterWrapper wrapper = wrappers[0];
		String pin = wrapper.getPin();
		Twitter twitter = wrapper.getTwitter();		
		RequestToken requestToken = wrapper.getRequestToken();
		System.out.println(pin + ": pin in worker thread");
        boolean result = false;
        try{
        	if(pin.length() > 0)
        	{
        		wrapper.setAccessToken(twitter.getOAuthAccessToken(requestToken, pin));
        		System.out.println(wrapper.getAccessToken() + "if");
        	}
        	else
        	{
        		wrapper.setAccessToken(twitter.getOAuthAccessToken());
        		System.out.println(wrapper.getAccessToken() + "else");
        	}
        	result = true;        	
        } 
        catch (TwitterException te) 
        {
        	if(401 == te.getStatusCode()){
        		System.out.println("Unable to get the access token.");
        	}else{
        		te.printStackTrace();
        	}
        }        
        wrapper.setResult(result);
        return wrapper;
    }
	
    /**
     * No UI to update here
     * @param Result of access token retrieval attempt
     */
    @Override
    protected void onPostExecute(TwitterWrapper wrapper)         {
    	caller.PostAccessToken(wrapper);
    }
}
