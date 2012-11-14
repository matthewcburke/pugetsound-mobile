package edu.pugetsound.vichar;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.pugetsound.vichar.SocketService.LocalBinder;
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
import android.view.View.OnTouchListener;
import android.widget.TextView;
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
	private NetworkingService networkingService = null;
	private Messenger networkingServiceMessenger = null;
    boolean isBoundToNetworkingService = false;
    private float touchX, touchY;
    private final String JSON_GAME_ENGINE_NAMESPACE = "engine";
    private String deviceUUID;
    private JSONObject gameState;
    private JSONObject deviceState;
      
    final Messenger mMessenger = new Messenger(new IncomingHandler());
	
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
        this.gameView.setOnTouchListener(this);
        gameContainer.setOnTouchListener(this);
        
        this.textView = (TextView) findViewById(R.id.game_view_text);
    	
        //Bind to the networking service
    	doBindNetworkingService();
    	try {
    		gameState = new JSONObject("{\"turret\":{\"position\":\"100,0,300\",\"ID\":\"1\"}}");
    	} catch(JSONException e) {
    		Log.i(this.toString(), "JSONException");
    	}
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
	    		//updateLocalGameState();
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
    	// TODO: remove this when we're properly using namespaces
    	JSONObject sendState = gameState;
    	
    	// Insert the device state into the proper namespace
    	// TODO: uncomment this when we're properly using namespaces
//    	JSONObject sendState = null;
//    	try {
//    		sendState = new JSONObject().put(deviceUUID, deviceState);
//    	} catch (JSONException e) {
//    		Log.i(this.toString(), "updateRemoteGameState: JSONException");
//    	}
    	
    	boolean isSuccessful = false;
    	if(isBoundToNetworkingService) {
    		//TODO re-enable posting
    		//networkingService.postJSONObject(gameState, null);
    		//networkingService.queueOutboundJson(gameState);
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
    		isSuccessful = true;
    	} else {
    		Log.i(this.toString(),"Not Bound to NetworkingService");
    	}
    	// TODO retries
    	return isSuccessful;
    }
    
    /**
     * Updates the local (client) gameState variable with
     * the contents of the server's game state.
     * @return
     */    
    private void updateLocalGameState(String str) {
    	try {
    		JSONObject json = new JSONObject(str);
    		Log.d(this.toString(), "New State: " + json.toString());
    		// Just pull out the official game state namespace
    		//gameState = json.get(JSON_GAME_ENGINE_NAMESPACE);
    		//onGameStateChange();
    	} catch(JSONException e) {
    		// do something
    		Log.d(this.toString(), "Couldn't Make JSON from string.");
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
	            	String str = msg
    				.getData()
    				.getString("" + NetworkingService.MSG_SET_JSON_STRING_VALUE);
	            	updateLocalGameState(str);
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
        				IBinder service)
        {
        	//TODO we're using both binder and messenger. Choose one?
    		//TODO should service run in separate thread?
    		Log.d(this.toString(),"Start onServiceConnected");
        	NetworkingService.LocalBinder binder = (NetworkingService.LocalBinder) service;
        	Log.d(this.toString(),"binder: " + binder.toString());
        	Log.d(this.toString(),"service: " + service.toString());

        	// Get a reference to the service interface
        	networkingService = binder.getService();
        	Log.d(this.toString(),"networkingService: " + networkingService.toString());
        	
        	// Create a Messenger that references the service
        	networkingServiceMessenger = networkingService.getMessenger();
        	Log.d(this.toString(),"networkingServiceMessenger: " + networkingServiceMessenger.toString());
        	
        	isBoundToNetworkingService = true;
	        Log.d(this.toString(),networkingService.toString());
	        Log.d(this.toString(),"Bound to HttpSevice");
	        
	        try {
		        Message msg = Message.obtain(null, NetworkingService.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            networkingServiceMessenger.send(msg);
	        } catch (RemoteException e) {
                //TODO handle RemoteException
	        	// The service crashed before we could do anything with it
            }
	        
	        networkingService.startPolling();
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
        //doUnbindSocketService();
    }
}
