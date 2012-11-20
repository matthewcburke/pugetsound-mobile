package edu.pugetsound.vichar;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;

/**
 * A Twitter wrapper class, for Twitter information between TwitterUtility
 * and calling activity
 * @author Nathan P
 *
 */
public class TwitterWrapper {
	private Twitter tw;
	private Boolean result;
	private RequestToken rt;
	private AccessToken at;
	private String pin;
	private String tweet;

	public TwitterWrapper(Twitter tw)  {
		this.tw = tw;
	}

	public void setResult(Boolean result)  {
		this.result = result;
	}
	
	public void setTweet(String tweet) {
		this.tweet = tweet;
	}

	public void setRequestToken(RequestToken rt) {
		this.rt = rt;
	}

	public void setAccessToken(AccessToken at)  {
		this.at = at;
	}
	
	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getPin()  {
		return pin;
	}
	
	public AccessToken getAccessToken()  {
		return at;
	}

	public RequestToken getRequestToken() {
		return rt;
	}
	
	public String getTweet()  {
		return tweet;
	}

	public Twitter getTwitter() {
		return tw;
	}

	public Boolean getResult() {
		return result;
	}
}

