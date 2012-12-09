package edu.pugetsound.vichar;

/**
 * Parses tweets, checking validity, and adding any tags
 * required by the server.
 * @author Nathan P
 * @version 12/8/12
 */
public class ParseTweet {

	private String originalTweet;

	private static final int IS_UNKNOWN_TAG = 0;
	private static final int IS_VICHAR_TAG = 1;
	private static final int IS_EYE_TAG = 2;

	private static final int TWEET_LIMIT = 140;

	private static final String VICHAR_TAG = "vichargame";
	private static final String EYE_TAG = "eye";

	public ParseTweet(String tweet) {
		originalTweet = tweet;
	}

	/**
	 * Add any appropriate tags to tweet, if user hasn't already added them.
	 * As of 12/8, server requires tags {@link #VICHAR_TAG} and {@link #EYE_TAG} 
	 * to count a vote.
	 * @return tweet with any added tags concatenated
	 */
	public String addTags() {
		Boolean eyeTag = false;
		Boolean vicharTag = false;
		String modifiedTweet = originalTweet;
		int index = 0;
		//check each tag in tweet
		while(index < originalTweet.length()) {
			String curChar = originalTweet.substring(index, index+1);
//			Log.d("ParseTweetTest", "Check char at " + index + ": " + curChar);
			//if this is the beginning of a tag, check the type
			if(curChar.equals("#")) {
				if(checkTagType(index) == ParseTweet.IS_EYE_TAG) {
					eyeTag = true;
				}
				if(checkTagType(index) == ParseTweet.IS_VICHAR_TAG) {
					vicharTag = true;
				}
			}
			index++;
		}

		//add hash tags to end of tweet if not already included
		if(vicharTag==false) {
			modifiedTweet = modifiedTweet + " #" + ParseTweet.VICHAR_TAG;
		}
		if(eyeTag==false) {
			modifiedTweet = modifiedTweet + " #" + ParseTweet.EYE_TAG;
		}
		return modifiedTweet;
	}

	/**
	 * Checks for the type of tag
	 * @param index Index where hash tag begins
	 * @return Type of index
	 */
	private int checkTagType(int index) {
		int type = ParseTweet.IS_UNKNOWN_TAG;

//		Log.d("ParseTweetTest", "Checking tag type at " + index);
		//check if this is eye tag
		if(index + 4 <= originalTweet.length()) {
			if(originalTweet.substring(index + 1, index + 4).equalsIgnoreCase(ParseTweet.EYE_TAG)) {
//				Log.d("ParseTweetTest", "eye tag at " + index);
				type = ParseTweet.IS_EYE_TAG;
			}
		}
		//check if this is vichar tag
		if(index + 11 <= originalTweet.length()) {
			if(originalTweet.substring(index + 1, index + 11).equalsIgnoreCase(ParseTweet.VICHAR_TAG)) {
//				Log.d("ParseTweetTest", "vichar tag at " + index);
				type = ParseTweet.IS_VICHAR_TAG;
			}
		}		
		return type;
	}

	/**
	 * Checks validity of tweet length
	 * @return True if tweet is between 1 and 140 characters, False if otherwise 
	 */
	public Boolean checkLength() {
		if(originalTweet.length() > 0 && originalTweet.length() <= ParseTweet.TWEET_LIMIT) {
			return true;
		} else {
			return false;
		}
	}
}
