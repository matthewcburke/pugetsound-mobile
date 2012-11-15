package edu.pugetsound.vichar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * A utility for saving and retrieving values from persistent storage 
 * on mobile device. Values are stored in a key/value pair.
 * @author Nathan P
 * @version 10/15/12
 */
public class PreferenceUtility {

    /**
     * Saves string to to persistent storage in a key-value format
     * @param key The key, which will be the reference for the value
     * @param value The value which is to be saved
     * @param act The application context
     */
    public void saveString(String key, String value, Context con)
    {
    	Log.d("viCHar","save" + value + " commenced");
    	//instantiate a preference instance 
   		SharedPreferences sharedPref = con.getSharedPreferences(con.getString(R.string.prefs_key), 0);
   		//instantiate editor
   		SharedPreferences.Editor editor = sharedPref.edit();

   		//save key and value, then commit
    	editor.putString(key, value);
    	editor.commit();

    }
    
	
    /*
     * Returns saved String value at provided key
     * @param key Key to check saved value at
     * @param error String to return if retrieved value is NOT a String
     * @param act The application context
     * @return The value currently saved in key location, or error String       
     */
    public String returnSavedString(String key, String error, Context con)
    {
   		SharedPreferences sharedPref = con.getSharedPreferences(con.getString(R.string.prefs_key), 0);
   		//retrieve String, retrieve FAIL if pref with this name is not a String
   		String toReturn = sharedPref.getString(key, error);
   		
   		Log.d("viCHar", toReturn);
   		return toReturn;
    }
    
    /**
     * Saves boolean to persistent storage in a key-value format
     * @param key The key, which will be the reference for the value
     * @param bool The boolean being saved
     * @param act The application context
     */
    public void saveBoolean(String key, Boolean bool, Context con) {
    	Log.d("viCHar","save " + bool + " commenced");
    	SharedPreferences sharedPref = con.getSharedPreferences(con.getString(R.string.prefs_key), 0);
    	SharedPreferences.Editor editor = sharedPref.edit();
    	editor.putBoolean(key, bool);
    	editor.commit();
    }
    
    /**
     * Returns boolean from persistent storage to the caller at provided key
     * @param key The key to find the matching value for 
     * @param bool Boolean to be returned if key NOT found
     * @param act The application context
     * @return The value stored in the key location, or the error value
     */
    public boolean returnBoolean(String key, Boolean error, Context con) {
    	SharedPreferences sharedPref = con.getSharedPreferences(con.getString(R.string.prefs_key), 0);
    	boolean toReturn = sharedPref.getBoolean(key, error);
    	return toReturn;
    }
}
