package edu.pugetsound.vichar;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

	//JSON namespaces
	private static final String GAME_ENGINE_NAMESPACE = "engine";
    private static final String WEB_NAMESPACE = "web";
    private String deviceUUID; // Device namespace
	
    // Views
	private View gameView;
	private TextView textView;

	// Service Stuff
    private Messenger networkingServiceMessenger = null;
    boolean isBoundToNetworkingService = false;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    private float touchX, touchY;
    private JSONObject gameState;
    
    private boolean activeTwitter = false;
    private float touchTwX;
    private int actionUp = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//Force landscape orientation because it is required by AR Module
    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	setContentView(R.layout.activity_game);
        
    	// Get the UUID we generated when this app was installed
    	deviceUUID = Installation.id(this);
    	
        //the whole screen becomes sensitive to touch
        View gameContainer = (View) findViewById(R.id.game_container);
        this.gameView = (View) findViewById(R.id.augmented_reality_fragment);
 //       this.gameView.setOnTouchListener(this);
 //       gameContainer.setOnTouchListener(this);
        

        
        ImageButton tweetFragTab = (ImageButton)findViewById(R.id.tweet_frag_button);
        tweetFragTab.setOnTouchListener(tweetFragTabListener);
        
        this.textView = (TextView) findViewById(R.id.game_view_text);

        //Bind to the networking service
    	doBindNetworkingService();
    	
    	// TODO remove this
    	try {
    		// Reset turret to keep things simple
    		gameState = new JSONObject("{\"turret\":{\"position\":\"100,0,300\",\"ID\":\"1\"}}");
    		pushDeviceState(gameState);
    	} catch(JSONException e) {
    		Log.i(this.toString(), "JSONException");
    	}

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
    		}
    		if(ev.getAction() == MotionEvent.ACTION_DOWN 
    				|| ev.getAction() == MotionEvent.ACTION_MOVE) {
    			// Remember new touch coors
    			touchX = ev.getX();
    			touchY = ev.getY();
    		} else if(ev.getAction() == MotionEvent.ACTION_UP) {
    			// reset values
    			touchX = 0f;
    			touchY = 0f;
    			dx = 0f;
    			dy = 0f;
    		}

    		// Touch propagated to gameView.
            float x = ev.getX();
            float y = ev.getY();
	    	
	    	try {
		    	JSONObject turret = gameState.getJSONObject("turret");
		    	String position = turret.getString("position");
		    	//Log.d(this.toString(),position);
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
		    	pushDeviceState(obtainDeviceState().put("turret", turret));
	    	} catch (JSONException e) {
	    		//something
	    		Log.i(this.toString(),"JSONException");
	    	}
    	}
        return true; //Must return true to get move events
    }
    
    /**
     * Called every time the gameState is updated with the remote game state.
     * This method should call other methods and contain very little logic of 
     * its own. 
     */
    private void onGameStateChange(String stateStr) {
    	try {
    		JSONObject gameState = new JSONObject(stateStr);
    		
    		//Pull out official namespaces
    		JSONObject engineState = gameState; // TODO: delete 
    		JSONObject webState = gameState; // TODO: delete 
    		// TODO uncomment for correct namespacing:
    		//JSONObject engineState = new JSONObject(stateStr).get(GAME_ENGINE_NAMESPACE);
    		//JSONObject webState = new JSONObject(stateStr).get(WEB_NAMESPACE);
    		
    		// TODO: delete. just supports old turret test:
    		this.gameState = gameState; 
    		// TODO remove this with turret test stuff
    		this.textView.setText(this.gameState.getJSONObject("turret").get("position").toString());
    	} catch(JSONException e) {
    		//shit!
    		e.printStackTrace();
    	}
    }
    
    /**
     * Returns a blank device state JSONObject
     * Use this method to get a reference to a device state that you can modify
     * and push.
     * 
     * This just makes it extra clear that you are passing snapshots of info
     * to the server. Do not try to get info about the device from a local copy
     * of the device state.
     * 
     * @return
     */
    private JSONObject obtainDeviceState() {
    	return new JSONObject();
    }
    
    /**
     * Wraps a device state snapshot in the deviceUUID and pushes it to the 
     * server.
     * @param deviceState
     */
    private void pushDeviceState(JSONObject deviceState) {
    	try {
    		
    		JSONObject sendState = new JSONObject().put(deviceUUID, deviceState);
    		
    		// TODO: delete for proper namespacing:
    		sendState = deviceState; 
    		
    		if(isBoundToNetworkingService) {
        		Bundle b = new Bundle();
        		b.putString("" + NetworkingService.MSG_QUEUE_OUTBOUND_J_STRING, 
        				sendState.toString());
        		Message msg = Message.obtain(null, 
        				NetworkingService.MSG_QUEUE_OUTBOUND_J_STRING);
        		msg.setData(b);
        		try {
        			networkingServiceMessenger.send(msg);
        		} catch (RemoteException e) {
                    //TODO handle RemoteException
        			Log.i(this.toString(), "updateRemoteGameState: RemoteException");
                }
        	} else {
        		Log.i(this.toString(),"Not Bound to NetworkingService");
        	}
    	} catch(JSONException e) {
    		// This really shouldn't happen...right?
    		Log.i(this.toString(), "pushDeviceState: JSONException");
    	}
    }
    
    /**
     * Handles incoming messages from NetworkingService
     * @author DuBious
     *
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
	            case NetworkingService.MSG_SET_JSON_STRING_VALUE:
	            	String str = msg.getData().getString("" + NetworkingService.MSG_SET_JSON_STRING_VALUE);
	            	onGameStateChange(str);
	            	break;
	            default:
	                super.handleMessage(msg);
            }
        }
    }
    
    /**
     * Forms the connection between this Activity and the NetworkingService
     */
    private ServiceConnection networkingServiceConnection = new ServiceConnection() 
    { //need this to create the connection to the service
        /**
         * Called when the service is started and bound to this Service Connection
         * @param className The classname of the activity
         * @param service the service's binder object
         */
    	public void onServiceConnected(
        				ComponentName className, 
        				IBinder binder)
        {
    		Log.d(this.toString(),"Start onServiceConnected");
        	// Create a Messenger that references the service
        	networkingServiceMessenger = new Messenger(binder);
        	Log.d(this.toString(),"networkingServiceMessenger: " + networkingServiceMessenger.toString());
        	
        	isBoundToNetworkingService = true;
	        Log.d(this.toString(),"Bound to HttpSevice");
	        
	        try {
		        Message msg = Message.obtain(null, NetworkingService.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            networkingServiceMessenger.send(msg);
	        } catch (RemoteException e) {
                //TODO handle RemoteException
	        	// The service crashed before we could do anything with it
	        }
        }

    	/**
    	 * Called when the service stops or unbinds itself
    	 * @param className The Classname of the activity
    	 */
        public void onServiceDisconnected(ComponentName className) 
        {
        	isBoundToNetworkingService = false;
        }
    };
    
    /**
     * Binds this activity to the NetworkingService
     */
    private void doBindNetworkingService() {
    	Log.d(this.toString(),"Binding HttpSevice");
        bindService(new Intent(GameActivity.this, NetworkingService.class), 
        		networkingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbinds this activity from the NetworkingService
     */
    private void doUnbindNetworkingService() {
    	// Unregister Messenger
    	if (networkingServiceMessenger != null) {
            try {
                Message msg = Message.obtain(null, NetworkingService.MSG_UNREGISTER_CLIENT);
                msg.replyTo = mMessenger;
                networkingServiceMessenger.send(msg);
                networkingServiceMessenger = null;
            } catch (RemoteException e) {
                // There is nothing special we need to do if the service has crashed.
            }
        }
    	// Detach our existing connection.
    	
        unbindService(networkingServiceConnection);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindNetworkingService();
        //doUnbindHttpService();
        //doUnbindSocketService();

    }
    
    
}
