package edu.pugetsound.vichar;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.pugetsound.vichar.SocketService.LocalBinder;
import android.annotation.SuppressLint;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.support.v4.app.Fragment;
//Import Fragment dependencies
import android.support.v4.app.FragmentActivity;

/**
 * The in-game Activity
 * Extends FragmentActivity to ensure support for Android 2.x
 * @author Michael DuBois
 */
public class GameActivity extends FragmentActivity implements OnTouchListener {

	private View gameView;
	private TextView textView;
	//private SocketService socketService;
	private HttpService httpService;
    boolean isBoundToSocketService = false;
    boolean isBoundToHttpService = false;
    private float touchX, touchY;
    private JSONObject gameState;
    
    private boolean activeTwitter = false;
    private float touchTwX;
    private int actionUp = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        //the whole screen becomes sensitive to touch
        View gameContainer = (View) findViewById(R.id.game_container);
        this.gameView = (View) findViewById(R.id.augmented_reality_fragment);
 //       this.gameView.setOnTouchListener(this);
 //       gameContainer.setOnTouchListener(this);
        

        
        ImageButton tweetFragTab = (ImageButton)findViewById(R.id.tweet_frag_button);
        tweetFragTab.setOnTouchListener(tweetFragTabListener);
        
        this.textView = (TextView) findViewById(R.id.game_view_text);
//    	doBindSocketService();
//    	Intent intent = new Intent(GameActivity.this, SocketService.class);
//    	startService(intent);
    	
    	//doBindHttpService();
    	//Intent intent = new Intent(GameActivity.this, HttpService.class);
    	//startService(intent);
        /*// Bind this activity to Networking Service
        Intent intent = new Intent(GameActivity.this, HttpService.class);
        bindService(intent, socketServiceConnection, Context.BIND_AUTO_CREATE);*/
    	
//    	do {
//    		//wait for HttpService
//    	} while(!isBoundToHttpService);
//    	try {
//    		gameState = new JSONObject("{\"turret\":{\"position\":\"100,0,300\",\"ID\":\"1\"}}");
//    	} catch(JSONException e) {
//    		Log.i(this.toString(), "JSONException");
//    	}
    	//updateLocalGameState();
    }
    

    
       
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_game, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handles item selection in menu
    	switch (item.getItemId()) {
            case R.id.enter_main_menu:
            	startActivity(new Intent(this, MainMenuActivity.class));
                return true;            
            default:
                return super.onOptionsItemSelected(item);
    	}
    }    
    
    private OnTouchListener tweetFragTabListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent me) { 
			return tweetContainerTouch(v, me);
		}
       };
    
    private boolean tweetContainerTouch(View v, MotionEvent me)  {
    	View tweetContainer = (View) findViewById(R.id.tweet_container);
    	View gameContainer = (View) findViewById(R.id.game_container);
    	switch (me.getAction()) {
			case MotionEvent.ACTION_DOWN:
				actionUp=0;
				touchTwX = me.getRawX();
				System.out.println("action down at " + touchTwX);       	
			case MotionEvent.ACTION_MOVE:
				actionUp=0;				
				System.out.println("motion event x " + me.getRawX());				
				//only execute changes if user hasn't dragged too far left
				if(me.getRawX()>gameContainer.getWidth()-tweetContainer.getWidth()) {   
					//calculate motion change
					float delta = me.getRawX() - touchTwX;  
					System.out.println("Delta " + delta);
					touchTwX = me.getRawX();
	   		
					//calculate new x coordinate of view
					System.out.println("right location " + tweetContainer.getRight());
					//set new params
					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tweetContainer.getLayoutParams();
					params.rightMargin = params.rightMargin - (int) delta;
					tweetContainer.setLayoutParams(params);  
				}
			case MotionEvent.ACTION_UP:
				actionUp++;
				if(actionUp==2) {
					System.out.println("------ACTION UP OR CANCEL------");						
					if(tweetContainer.getLeft() < gameContainer.getWidth() - 200) {
						FrameLayout.LayoutParams paramsSuccess = (FrameLayout.LayoutParams) tweetContainer.getLayoutParams();
						paramsSuccess.rightMargin = 0;
						tweetContainer.setLayoutParams(paramsSuccess);  
					} else {		        			
						FrameLayout.LayoutParams paramsReset = (FrameLayout.LayoutParams) tweetContainer.getLayoutParams();
						paramsReset.rightMargin = -300;
						tweetContainer.setLayoutParams(paramsReset);  	        		
					}
				}   		        		
   			}			
        return true;
    }
       
    /**
     * Looks at current gamestate for twitter challenge
     * @return True if new challenge, false if not
     */
    private void UpdateTwitterState()  {
    	if(activeTwitter)  return;  //if there is alread a twitter challenge, we don't need to do anything
    	
 
    	//TODO:take json, parse, check if there is a twitter vote. Update activeTwitter flag
    	
    	//assuming there is a new twitter challenge...
    	TweetFragment tweetFrag = (TweetFragment) getSupportFragmentManager().findFragmentById(R.id.tweet_fragment);
        tweetFrag.setPrompt(getString(R.string.default_twitter_prompt));
    }
    
    /**
     * Sends tweet
     * @param view
     */
    public void sendTweet(View view)     {
    	TweetFragment tweetFrag = (TweetFragment) getSupportFragmentManager().findFragmentById(R.id.tweet_fragment);
        tweetFrag.sendTweet(view);	
    }
    /**
     * Capture touch events
     * @param v
     * @param event
     * @return
     */
    public boolean onTouch(View v, MotionEvent ev)
    {
        // TODO check other views above game view
        //if(v.equals(this.gameView)) {
        	//return true; // true indicates event is consumed
        //}
    	if(v.equals(this.gameView)) {
    		
    		float dx = 0f;
    		float dy = 0f;
    		
    		if(ev.getAction() == MotionEvent.ACTION_MOVE) {
    			dx = ev.getX() - touchX;
    			dy = ev.getY() - touchY;
    			Log.d(this.toString(),"Touch move");
    		}
    		if(ev.getAction() == MotionEvent.ACTION_DOWN 
    				|| ev.getAction() == MotionEvent.ACTION_MOVE) {
    			// Remember new touch coors
    			touchX = ev.getX();
    			touchY = ev.getY();
    			Log.d(this.toString(),"Touch down or move");
    		} else if(ev.getAction() == MotionEvent.ACTION_UP) {
    			// reset values
    			touchX = 0f;
    			touchY = 0f;
    			dx = 0f;
    			dy = 0f;
    			Log.d(this.toString(),"Touch up");
    		}

    		// Touch propagated to gameView.
            float x = ev.getX();
            float y = ev.getY();
            Log.d(this.toString(),"Touch event propagated to gameView");
	    	Log.d(this.toString(),"startX: " + x);
	    	Log.d(this.toString(),"startY: " + y);
	    	Log.d(this.toString(),"dX: " + dx);
	    	Log.d(this.toString(),"dY: " + dy);
	    	
	    	try {
	    		// TODO fix likely concurrency problems. 
	    		// TODO local gameState should be polled on a timer.
	    		updateLocalGameState();
		    	JSONObject turret = gameState.getJSONObject("turret");
		    	//JSONObject turret = json.getJSONObject("turret");
		    	String position = turret.getString("position");
		    	Log.d(this.toString(),position);
		    	String[] coors = position.split("\\s*,\\s*");
		    	
		    	// Calc change with floats
//		    	float turretX = Float.valueOf(coors[0].trim()).floatValue();
//		    	float turretZ = Float.valueOf(coors[2].trim()).floatValue();
//		    	coors[0] = Float.toString(turretX + dx);
//		    	coors[2] =  Float.toString(turretZ + dy);
		    	
		    	// Calc change with ints
		    	int turretX = Math.round(Float.valueOf(coors[0].trim()).floatValue());
		    	int turretZ = Math.round(Float.valueOf(coors[2].trim()).floatValue());
		    	coors[0] = Integer.toString(turretX + Math.round(dx));
		    	coors[1] = Integer.toString(Math.round(Float.valueOf(coors[1].trim()).floatValue())); // for good measure
		    	coors[2] = Integer.toString(turretZ + Math.round(dy));
		    	
		    	for(int i = 0; i < coors.length; i++) {
		    		if(i == 0) position = coors[i];
		    		else position += "," + coors[i];
		    	}
		    	
		    	turret.put("position", position);
		    	Log.d(this.toString(),turret.toString());
		    	gameState.put("turret", turret);
		    	Log.d(this.toString(),gameState.toString());
		    	updateRemoteGameState();
		    	this.textView.setText(gameState.getJSONObject("turret").get("position").toString());
		    	//updateLocalGameState();
	    	} catch (JSONException e) {
	    		//something
	    		Log.i(this.toString(),"JSONException");
	    	}
    	}
        return true; //Must return true to get move events
    }
    
    /**
     * Updates the remote game state with the contents
     * of the local gameState variable.
     * @return
     */
    private boolean updateRemoteGameState() {
    	boolean isSuccessful = false;
    	if(isBoundToHttpService) {
    		httpService.send(gameState);
    		Log.d(this.toString(), httpService.getJSON().toString());
    		isSuccessful = true;
    	} else {
    		Log.i(this.toString(),"Not Bound to HttpService");
    	}
    	// TODO retries
    	return isSuccessful;
    }
    
    /**
     * Updates the local (client) gameState variable with
     * the contents of the server's game state.
     * @return
     */
    private boolean updateLocalGameState() {
    	boolean isSuccessful = false;
    	if(isBoundToHttpService) {
    		gameState = httpService.getJSON();
    		Log.d(this.toString(), httpService.getJSON().toString());
    		isSuccessful = true;
    	} else {
    		Log.i(this.toString(),"Not Bound to HttpService");
    	}
    	// TODO retries
    	return isSuccessful;
    }
    
    /**
     * Is x and y inside view? I don't know... Try this function.
     * Generally better to register the view itself with a touch 
     * listener, but in special cases, use this.
     * @param view
     * @param rx
     * @param ry
     * @return true if x and y falls inside given view, false if not.
     */
    private boolean isViewContains(View view, float rx, float ry) {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = view.getWidth();
        int h = view.getHeight();

        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }
    
//    private ServiceConnection socketServiceConnection = new ServiceConnection() 
//    { //need this to create the connection to the service
//        public void onServiceConnected(
//        				ComponentName className, 
//        				IBinder service)
//        {
//        	Log.d(this.toString(),"Start onServiceConnected");
//        	SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
//	        socketService = binder.getService();
//	        isBoundToSocketService = true;
//	        Log.d(this.toString(),"Bound to SocketSevice");
//        }
//
//        public void onServiceDisconnected(ComponentName className) 
//        {
//        	isBoundToSocketService = false;
//        }
//    };
//    
//    private void doBindSocketService() {
//    	Log.d(this.toString(),"Binding SocketSevice");
//        bindService(new Intent(GameActivity.this, SocketService.class), 
//        		socketServiceConnection, Context.BIND_AUTO_CREATE);
//        isBoundToSocketService = true;
//    }
//
//
//    private void doUnbindSocketService() {
//        if (isBoundToSocketService) {
//            // Detach our existing connection.
//            unbindService(socketServiceConnection);
//            isBoundToSocketService = false;
//        }
//    }
    
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
	        updateLocalGameState();
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
        bindService(new Intent(GameActivity.this, HttpService.class), 
        		httpServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbinds this activity from the HttpService
     */
    private void doUnbindHttpService() {
        // Detach our existing connection.
        unbindService(httpServiceConnection);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //doUnbindHttpService();
        //doUnbindSocketService();
    }
    
    
}
