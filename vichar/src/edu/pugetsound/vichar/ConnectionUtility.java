package edu.pugetsound.vichar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
/**
 * Provides resources for checking network connectivity
 * @author Nathan P
 *
 */
public class ConnectionUtility
{	
	/**
	 * Checks network connectivity of device
	 * @param c application context
	 * @return 0 if connection disabled
			   1 if connection enabled but no connectivity
			   2 if connection enabled and connectivity
	 */
	public int checkConnection(Context c)  {
		int result = 0;
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		//NetworkInfo ni = cm.getActiveNetworkInfo();
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
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
			} else {
				result = 1;
				return result;
			}			
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
