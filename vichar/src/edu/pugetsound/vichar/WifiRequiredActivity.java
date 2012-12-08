package edu.pugetsound.vichar;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class WifiRequiredActivity extends FragmentActivity {
	
	protected ConnectionUtility mConnectivityUtility;
	
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 mConnectivityUtility = new ConnectionUtility(this);
	 }
	
	 /**
     * Called whenever the Activity becomes visible
     */
    @Override
    protected void onStart() {
    	super.onStart();
    	mConnectivityUtility.requireConnectivity(); //check network connectivity
    }
    
    @Override
    protected void onResume()  {
    	super.onResume();
    	mConnectivityUtility.registerReceiver(); //check network connectivity
    }
    
    @Override
    protected void onPause()  {
    	super.onPause();
    	mConnectivityUtility.unregisterReceiver();
    	
    }
    
    @Override
    protected void onStop()  {
    	super.onStop();
    	mConnectivityUtility.dismissDialog();
    }

}
