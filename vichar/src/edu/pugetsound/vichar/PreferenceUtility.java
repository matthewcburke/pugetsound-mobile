package edu.pugetsound.vichar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.util.Log;

public class PreferenceUtility {

    /**
     * saves string to to persistent storage in a key-value format
     * @param key The key, which will be the reference for the value
     * @param value The value which is to be saved
     * @param act The current activity, passed for context purposes
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
     * @param act Current activity
     * @return The value currently saved in key location
     *         Returns FAIL if retrieved object is NOT a String
     */
    public String returnSavedString(String key, Activity act)
    {
   		SharedPreferences sharedPref = act.getSharedPreferences(act.getString(R.string.prefs_key), 0);
   		//retrieve String, retrieve FAIL if pref with this name is not a String
   		String toReturn = sharedPref.getString(key, "FAIL");
   		
   		Log.d("viCHar", toReturn);
   		return toReturn;
    }
}
