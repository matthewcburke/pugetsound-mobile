package edu.pugetsound.vichar;

import android.app.Activity;
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
     * @param act The current, caller activity. Passed for context purposes
     */
    public void saveString(String key, String value, Activity act)
    {
    	Log.d("viCHar","save" + value + " commenced");
    	//instantiate a preference instance 
   		SharedPreferences sharedPref = act.getSharedPreferences(act.getString(R.string.prefs_key), 0);
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
     * @param act Current activity
     * @return The value currently saved in key location, or error String       
     */
    public String returnSavedString(String key, String error, Activity act)
    {
   		SharedPreferences sharedPref = act.getSharedPreferences(act.getString(R.string.prefs_key), 0);
   		//retrieve String, retrieve FAIL if pref with this name is not a String
   		String toReturn = sharedPref.getString(key, error);
   		
   		Log.d("viCHar", toReturn);
   		return toReturn;
    }
}
