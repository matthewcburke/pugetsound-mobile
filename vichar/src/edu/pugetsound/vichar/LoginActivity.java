package edu.pugetsound.vichar;


import org.json.JSONException;
import org.json.JSONObject;

import edu.pugetsound.vichar.HttpService.LocalBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


/**
 * An activity that allows the user to log into the game through the Vi-Char
 * server instead of twitter. 
 * @author Kirah Taylor
 * @version 10/24/12
 */
@SuppressWarnings("unused")
public class LoginActivity extends Activity {
	
	HttpService myService;
    boolean isBound = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_login);
	    Button button = (Button)findViewById(R.id.button1);
		button.setOnClickListener(startListener);
	}
	
	private OnClickListener startListener = new OnClickListener() {
    	public void onClick(View v) {
    		//Intent intent = new Intent(LoginActivity.this, HttpService.class); 
            //bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        	EditText text  = (EditText)findViewById(R.id.editText1);
        	String words;
			words = text.getText().toString();
			//JSONObject jsonobj;
			//jsonobj= new JSONObject();
			//try {
			//	jsonobj.put("username", words);
			//	JSONObject header = new JSONObject();
			//	header.put("login", "authentication");

			//} 
			//catch (JSONException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			//myService.send(jsonobj);
			storeUsername(words);
			Intent next = new Intent(LoginActivity.this, MainMenuActivity.class);
			startActivity(next);
        }
    };
    
    private ServiceConnection myConnection = new ServiceConnection() { //need this to create the connected to the service
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            }

            public void onServiceDisconnected(ComponentName className) {
                isBound = false;
            }
        };
	
  /**
    * Stores the entered string to persistent storage
    * @param String username The string being saved
    */ 
    private void storeUsername(String username)
    {
    	PreferenceUtility prefs = new PreferenceUtility();
    	prefs.saveString("username", username, this);
    }

}
