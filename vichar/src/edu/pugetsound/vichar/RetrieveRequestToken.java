package edu.pugetsound.vichar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.os.AsyncTask;

/**
 * Retrieves OAuth request token in separate thread
 * @author Nathan P
 * @version 10/18/12
 */
public class RetrieveRequestToken extends AsyncTask<TwitterWrapper, TwitterWrapper, TwitterWrapper> {

	private RequestTokenCallback caller;

	/**
	 * Callback interface
	 */
	public interface RequestTokenCallback {
		public void PostRequestToken(TwitterWrapper wrapper);
	}
	
	public RetrieveRequestToken(RequestTokenCallback caller, TwitterWrapper wrapper)  {
		this.caller = caller;
		execute(wrapper);
	}

	/**
	 * Retrieve request token
	 * @param wrappers Array of TwitterWrappers, containing an instance of Twitter
	 * @return updated TwitterWrapper
	 */
	@Override
	protected TwitterWrapper doInBackground(TwitterWrapper...wrappers)
	{
		TwitterWrapper wrapper = wrappers[0];
		Twitter twitter = wrapper.getTwitter();
		boolean result = false;
		try 
		{
			wrapper.setRequestToken(twitter.getOAuthRequestToken()); 
			result = true;
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
		catch(Exception ex) {
			System.out.println(ex);
			ex.printStackTrace();
		}
		wrapper.setResult(result);
		return wrapper;
	}

	/**
	 * Results of authorization
	 * @param wrapper The updated TwitterWrapper containing request token
	 */
	@Override
	protected void onPostExecute(TwitterWrapper wrapper) 
	{    
		caller.PostRequestToken(wrapper);        	
	}

}
