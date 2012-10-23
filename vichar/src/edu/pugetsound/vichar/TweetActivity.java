package edu.pugetsound.vichar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v4.app.*;

public class TweetActivity extends Fragment {

	private String curPrompt;
	
    @Override
    public View onCreateView(LayoutInflater inflater, 
    							ViewGroup container, Bundle savedInstanceState) {
        //get default prompt text
    	curPrompt = getString(R.string.default_twitter_prompt);
    	//set default prompt text
    	TextView twitterPrompt = (TextView) getView().findViewById(R.id.cur_twitter_challenge);
    	twitterPrompt.setText(curPrompt);
    	//inflate the fragment's layout
        return inflater.inflate(R.layout.activity_tweet, container, false);
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
}
