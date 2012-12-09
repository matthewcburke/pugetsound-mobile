package edu.pugetsound.vichar;

import edu.pugetsound.vichar.PostTweet.PostTweetCallback;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.*;

/**
 * Fragment for posting tweets and displaying current Twitter prompt/challenge.
 * @author Nathan Pastor
 * @version 10/25/12
 */
public class TweetFragment extends Fragment implements PostTweetCallback {

	//current Twitter challenge prompt
	private String curPrompt;
	private static final String INVALID_TWEET = "INVALID";
	/**
	 * Initializes display
	 * @return View The fragment UI encapsulated in a View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
	 * Sends tweet. Called when user presses OK button after entering tweet.
	 * @param view The OK button
	 */
	public void sendTweet(View view)
	{
		//get text of tweet
		EditText tweetPane = (EditText) getView().findViewById(R.id.tweet_pane);
		String tweet = tweetPane.getText().toString();
		//check tweet validity (length and tags)
		String toSend = checkTweetValidity(tweet);
		//only send if tweet is valid
		if(!toSend.equals(TweetFragment.INVALID_TWEET)) {
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
			//pass twitter wrapper object to worker thread
			TwitterWrapper wrapper = new TwitterWrapper(twitter);
			wrapper.setTweet(toSend);
			new PostTweet(this, wrapper);
		}
	}

	/**
	 * Called after attempted tweet post
	 * @param wrapper TwitterWrapper containing result of tweet post attempt
	 */
	public void onPostTweet(TwitterWrapper wrapper)  {
		//TODO:THIS IS TERRIBLE CODE! Determine behavior here, should be able to move tweet container in activity
		if(wrapper.getResult())  {
			if(getActivity() instanceof GameActivity) {
				((GameActivity) getActivity()).snapTwitterOff();
			}
		}
		
	}

	/**
	 * Checks validity of tweet, based on length, and adds necessary
	 * tags if not already included
	 * @param tweet Tweet to be checked
	 * @return Potentially modified tweet,  TweetFragment.INVALID_TWEET if tweet of invalid length
	 */
	private String checkTweetValidity(String tweet) {
		String modifiedTweet = tweet;
		ParseTweet pt = new ParseTweet(tweet);
		if(pt.checkLength()==false) {
			modifiedTweet = TweetFragment.INVALID_TWEET;
		} else {
			modifiedTweet = pt.addTags();
			ParseTweet pt2 = new ParseTweet(modifiedTweet);
			//if adding the tags made the tweet too long, set to invalid tweet
			if(!pt2.checkLength()) {
				modifiedTweet = TweetFragment.INVALID_TWEET;
			}
		}		
		return modifiedTweet;
	}
}
