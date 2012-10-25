package edu.pugetsound.vichar;

import edu.pugetsound.vichar.SocketService.LocalBinder;
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
import android.view.View.OnTouchListener;
//Import Fragment dependencies
import android.support.v4.app.FragmentActivity;

/**
 * The in-game Activity
 * Extends FragmentActivity to ensure support for Android 2.x
 */
public class GameActivity extends FragmentActivity implements OnTouchListener {

	private View gameView;
	private SocketService socketService;
    boolean isBoundToSocketService = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        //the whole screen becomes sensitive to touch
        View gameContainer = (View) findViewById(R.id.game_container);
        this.gameView = (View) findViewById(R.id.augmented_reality_fragment);
        this.gameView.setOnTouchListener(this);
        gameContainer.setOnTouchListener(this);
        
    	doBindSocketService();
    	Intent intent = new Intent(GameActivity.this, SocketService.class);
    	startService(intent);
        /*// Bind this activity to Networking Service
        Intent intent = new Intent(GameActivity.this, HttpService.class);
        bindService(intent, socketServiceConnection, Context.BIND_AUTO_CREATE);*/
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
    		// Touch propagated to gameView.
            float x = ev.getX();
            float y = ev.getY();
            Log.d(this.toString(),"Touch event propagated to gameView");
	    	Log.d(this.toString(),"startX: " + x);
	    	Log.d(this.toString(),"startY: " + y);
	    	
	    	// Update server
           /* Intent intent = new Intent(GameActivity.this, HttpService.class);
            intent.putExtra("touchX", x);
            intent.putExtra("touchY", y);
            intent.putExtra("touchTimestamp", ev.getDownTime());
            httpService.send(intent);*/
	    	//socketService.readJSON();
	    	socketService.postTest();
    	}
        return false; //false indicates the event is not consumed
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
    
    private ServiceConnection socketServiceConnection = new ServiceConnection() 
    { //need this to create the connection to the service
        public void onServiceConnected(
        				ComponentName className, 
        				IBinder service)
        {
        	Log.d(this.toString(),"Start onServiceConnected");
	        LocalBinder binder = (LocalBinder) service;
	        socketService = binder.getService();
	        isBoundToSocketService = true;
	        Log.d(this.toString(),"Bound to SocketSevice");
        }

        public void onServiceDisconnected(ComponentName className) 
        {
        	isBoundToSocketService = false;
        }
    };
    
    private void doBindSocketService() {
    	Log.d(this.toString(),"Binding SocketSevice");
        bindService(new Intent(GameActivity.this, SocketService.class), 
        		socketServiceConnection, Context.BIND_AUTO_CREATE);
        isBoundToSocketService = true;
    }


    private void doUnbindSocketService() {
        if (isBoundToSocketService) {
            // Detach our existing connection.
            unbindService(socketServiceConnection);
            isBoundToSocketService = false;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindSocketService();
    }
}
