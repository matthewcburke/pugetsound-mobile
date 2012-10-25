package edu.pugetsound.vichar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.*;

public class TweetFragment extends Fragment {

	//current Twitter challenge prompt
	private String curPrompt;
	//current Twitter post attempt
	private int postAttempt;
	//max post attempts
	private final int POST_ATTEMPT_LIMIT = 3;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, 
    							ViewGroup container, Bundle savedInstanceState) {
    	postAttempt = 0;
    	
//        //get default prompt text
//    	curPrompt = getString(R.string.default_twitter_prompt);    	
//    	//set default prompt text
//    	TextView twitterPrompt = (TextView) getView().findViewById(R.id.cur_twitter_challenge);
//    	twitterPrompt.setText(curPrompt);
    	//inflate the fragment's layout
    	Log.d("TF", "commencing inflation");
        return inflater.inflate(R.layout.fragment_tweet, container, false);
    }
    
    /**
     * Sets prompt and updates display
     * @param newPrompt New Twitter prompt to display
     */
    public void setPrompt(String newPrompt)
    {
    	curPrompt = newPrompt;
    	//update display
    	TextView twitterPrompt = (TextView) getView().findViewById(R.id.cur_twitter_challenge);
    	twitterPrompt.setText(curPrompt);
    }
    
    /**
     * Sends tweet
     * @param view
     */
    public void sendTweet(View view)
    {
    	//retrieve access token and access secret from memory
    	PreferenceUtility prefs = new PreferenceUtility();
    	String accessToken = prefs.returnSavedString(getString(R.string.access_token_key), 
    													getString(R.string.prefs_error), this.getActivity());
    	String accessSecret = prefs.returnSavedString(getString(R.string.access_token_secret_key), 
														getString(R.string.prefs_error), this.getActivity()); 
        //initialize twitter
    	ConfigurationBuilder cb = new ConfigurationBuilder();
    	cb.setOAuthConsumerKey(getString(R.string.oauth_consumer_key))
    	  .setOAuthConsumerSecret(getString(R.string.oauth_consumer_secret))
    	  .setOAuthAccessToken(accessToken)
    	  .setOAuthAccessTokenSecret(accessSecret);
    	TwitterFactory tf = new TwitterFactory(cb.build());
    	Twitter twitter = tf.getInstance();
    	//pass twitter object to worker thread
    	new PostTweet().execute(twitter);
    }
    	
   
    /**
     * Posts tweets
     * @author Nathan P
     * @version 10/23/12
     */
    private class PostTweet extends AsyncTask<Twitter, Boolean, Boolean>   {
    	
    	/**
    	 * Posts tweet via worker thread
    	 */
     	@Override
        protected Boolean doInBackground(Twitter...twitters)   {
     		boolean result = false;  
     		//get twitter object
     		Twitter twitter = twitters[0];
     		//get text of tweet
     		EditText tweetPane = (EditText) getView().findViewById(R.id.tweet_pane);
     		String tweet = tweetPane.getText().toString();
        	//execute the post!
            try {
            	twitter.updateStatus(tweet);
            	result = true;
            }
            catch (TwitterException te)  {
            	//attempt maximum of POST_ATTEMPT_LIMIT times on failure
    			if(postAttempt<POST_ATTEMPT_LIMIT)  {
    				postAttempt++;
    				new PostTweet().execute(twitter);
    			} else {
    				postAttempt=0;
    			}
    		}
            return result;
     	}
    }
}
