package edu.pugetsound.vichar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.os.AsyncTask;
import android.widget.EditText;


/**
 * Posts tweets via worker thread
 * @author Nathan P
 * @version 10/23/12
 */
public class PostTweet extends AsyncTask<TwitterWrapper, TwitterWrapper, TwitterWrapper>   {
	
	private int postAttempt;  //current Twitter post attempt	
	private final int POST_ATTEMPT_LIMIT = 3;  //max post attempts
	private PostTweetCallback caller; //caller class
	
	public interface PostTweetCallback  {
		public void onPostTweet(TwitterWrapper wrapper);
	}
	
	public PostTweet(PostTweetCallback caller, TwitterWrapper wrapper)  {
		this.caller = caller;
		execute(wrapper);
	}
	
	/**
	 * Posts tweet in background thread
	 * @param Twitter[] Array of Twitter objects, which contain necessary
	 *                  OAuth identification tokens.
	 */
	@Override
	protected TwitterWrapper doInBackground(TwitterWrapper...wrappers)   {
		boolean result = false;  
		//get twitter object
		TwitterWrapper wrapper = wrappers[0];
		Twitter twitter = wrapper.getTwitter();
		//execute the post!
		try {
			twitter.updateStatus(wrapper.getTweet());
			result = true;
		}
		catch (TwitterException te)  {
			//attempt maximum of POST_ATTEMPT_LIMIT times on failure
			if(postAttempt<POST_ATTEMPT_LIMIT)  {
				postAttempt++;
				new PostTweet(caller, wrapper).execute(wrapper);
				System.out.println("Failed post attempt " + postAttempt);
			} else {
				postAttempt=0;
			}
		}
		wrapper.setResult(result);
		return wrapper;
	}
	
	protected void onPostExecute(TwitterWrapper wrapper)  {
		System.out.println("Enter onPostExecute");
		caller.onPostTweet(wrapper);
	}
}
