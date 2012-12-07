package edu.pugetsound.vichar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;
/**
 * Provides resources for checking network connectivity
 * @author Nathan P
 *
 */
public class ConnectionUtility implements ConnectivityReceiver.ConnectivityListener
{	
	private ConnectivityReceiver connectivityReceiver;
	private Context context;
	private ConnectionDialog cd;
	
	public ConnectionUtility(Context c) {
		context = c;
	}
	
	public void onConnectivityChange(boolean isConnected) {
		requireConnectivity();
	}
	
	public void requireConnectivity() {
		dismissDialog(); // dismiss existing dialogs if they exist
		if(checkConnection(context)!=2) {
			if(checkConnection(context)==3) {
				cd = new ConnectionDialog(context, ConnectionDialog.CONNECTING);
			} else {
				cd = new ConnectionDialog(context, ConnectionDialog.NO_CONNECTION);
			}
			
	    	cd.show();
		}
	}
	
	public void dismissDialog() {
		if(cd != null) {
			cd.dismiss();
			cd = null;
		}
	}
	
	public void registerReceiver() {
		connectivityReceiver = new ConnectivityReceiver(this);
		IntentFilter filter = connectivityReceiver.getIntentFilter();
		context.registerReceiver(connectivityReceiver, filter);
	}
	
	public void unregisterReceiver() {
		context.unregisterReceiver(connectivityReceiver);
	}
	
	public void cleanUp() {
		dismissDialog();
	}

	/**
	 * Checks network connectivity of device
	 * @param c application context
	 * @return 0 if connection disabled
			   1 if connection enabled but no connectivity
			   2 if connection enabled and connectivity
			   3 if wifi is trying to connect
	 */
	public int checkConnection(Context c)  {
		int result = 0;
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		WifiManager wm = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		//NetworkInfo ni = cm.getActiveNetworkInfo();
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		SupplicantState supplicantState = wm.getConnectionInfo().getSupplicantState();
		//if there is no active connection...
		if(ni==null)
		{
			return result;
		} else {	
			//if connectivity...
			if(ni.isConnected()==true) {
				result = 2;
				return result;
			//else no connectivity...
			} else if(isConnecting(supplicantState)) {
				return 3;
			} else {
				result = 1;
				return result;
			}			
		}
	}
	
	@SuppressLint("NewApi") 
	static boolean isConnecting(SupplicantState state) 
		throws IllegalArgumentException
	{
        switch(state) {
            case AUTHENTICATING:
            case ASSOCIATING:
            case ASSOCIATED:
            case FOUR_WAY_HANDSHAKE:
            case GROUP_HANDSHAKE:
                return true;
            case COMPLETED:
            case DISCONNECTED:
            case INTERFACE_DISABLED:
            case INACTIVE:
            case SCANNING:
            case DORMANT:
            case UNINITIALIZED:
            case INVALID:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }
	
	/**
	 * Allows user to pick a Wifi connection 
	 * @param c application context
	 */
	public void connectToWifi(Context c)	{
		WifiManager wm = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		wm.setWifiEnabled(true);
		//wait while wifi enables..
		while(wm.isWifiEnabled()==false);
		//calls Android wifi choosing menu
		c.startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
	}

}
