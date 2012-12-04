package edu.pugetsound.vichar;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;

/**
 * Wraps Twitter related information. Used to pass between Twitter network classes and 
 * their calling classes. Care should be taken in calling getter methods: in a given instance,
 * it's likely most of the member fields won't have been initialized
 * @author Nathan P
 */
public class TwitterWrapper {
	private Twitter tw;
	private Boolean result;
	private RequestToken rt;
	private AccessToken at;
	private String pin;
	private String tweet;
	private String sn;

	public TwitterWrapper(Twitter tw)  {
		this.tw = tw;
	}

	public void setResult(Boolean result)  {
		this.result = result;
	}
	
	public void setScreenName(String sn)  {
		this.sn = sn;
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

	public String getScreenName()  {
		return sn;
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

