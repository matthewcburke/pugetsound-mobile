package edu.pugetsound.vichar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

class ConnectivityReceiver extends BroadcastReceiver {

	public interface ConnectivityListener {
		public void onConnectivityChange(boolean isConnected);
	}
	
	private ConnectivityListener callbackListener;
	
	public ConnectivityReceiver(ConnectivityListener cl) {
		callbackListener = cl;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
	    final String action = intent.getAction();
	    if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
	        if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
	        	callbackListener.onConnectivityChange(true);
	        } else {
	            // wifi connection was lost
	        	callbackListener.onConnectivityChange(false);
	        }
	    }
	}
	
	public IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		return filter;
	}

}
