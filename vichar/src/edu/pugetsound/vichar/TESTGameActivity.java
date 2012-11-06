package edu.pugetsound.vichar;

import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.Fragment;
//Import Fragment dependencies
import android.support.v4.app.FragmentActivity;

/**
 * TEST ACTIVITY. THIS WILL BE GONE SOON.
 * Extends FragmentActivity to ensure support for Android 2.x
 */
public class TESTGameActivity extends FragmentActivity {

	HttpService httpService;
	boolean isBoundToSocketService = false;
	boolean isBoundToHttpService = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GameAct", "About to set content view");
        setContentView(R.layout.activity_game_test);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //set default twitter prompt string on Tweet fragment
             
        doBindHttpService();
    }    

	/**
     * Sends tweet
     * @param view
     */
    public void sendTweet(View view)
    {
    	TweetFragment tweetFrag = (TweetFragment) getSupportFragmentManager().findFragmentById(R.id.tweet_fragment);
        tweetFrag.sendTweet(view);	
    }
    
    /**
     * Forms the connection between this Activity and the HttpService
     */
    private ServiceConnection httpServiceConnection = new ServiceConnection() 
    { //need this to create the connection to the service
        /**
         * Called when the service is started and bound to this Service Connection
         * @param className The classname of the activity
         * @param service the service's binder object
         */
    	public void onServiceConnected(
        				ComponentName className, 
        				IBinder service)
        {
        	Log.d(this.toString(),"Start onServiceConnected");
        	HttpService.LocalBinder binder = (HttpService.LocalBinder) service;
	        httpService = binder.getService();
	        Log.d(this.toString(),httpService.toString());
	        isBoundToHttpService = true;
	        Log.d(this.toString(),"Bound to HttpSevice");
	        
	        TweetFragment tweetFrag = (TweetFragment) getSupportFragmentManager().findFragmentById(R.id.tweet_fragment);   
	        JSONObject state = httpService.getJSON();
	        tweetFrag.setPrompt(state);
        }

    	/**
    	 * Called when the service stops or unbinds itself
    	 * @param className The Classname of the activity
    	 */
        public void onServiceDisconnected(ComponentName className) 
        {
        	isBoundToHttpService = false;
        }
    };
    
    /**
     * Binds this activity to the HttpService
     */
    private void doBindHttpService() {
    	Log.d(this.toString(),"Binding HttpSevice");
        bindService(new Intent(TESTGameActivity.this, HttpService.class), 
        		httpServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbinds this activity from the HttpService
     */
    private void doUnbindHttpService() {
        // Detach our existing connection.
        unbindService(httpServiceConnection);
    }


}
