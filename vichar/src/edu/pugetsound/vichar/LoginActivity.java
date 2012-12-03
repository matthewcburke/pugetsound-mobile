package edu.pugetsound.vichar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

public class LoginActivity extends Activity {
	
	EditText snEntry;
	Boolean validSn; //is current screen name of valid length?

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		validSn = false;
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_login);
	    Button button = (Button)findViewById(R.id.enter_guest);
		button.setOnClickListener(startListener);
		setButtonInactive();
		
		snEntry = (EditText) findViewById(R.id.sn_entry);	
		
		//set text listener, to adjust state of button depending on length of text
		snEntry.addTextChangedListener(new TextWatcher() {			
		    public void onTextChanged(CharSequence s, int start, int before, int count) {
				return;
			}		   
		    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		    	return;
		    }		
		    public void afterTextChanged(Editable s) {
		    	if(snEntry.getText().toString().length() < 3) {
		    		if(validSn==true) {
		    			validSn = false;
			    		setButtonInactive();	
		    		}		    			    		
		    	} else {
		    		if(validSn==false) {
		    			validSn = true;
			    		setButtonActive();		
		    		}		    		    		
		    	}
		    }
		});
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setButtonInactive() {
		Log.d("UI", "set button inactive");
		Button button = (Button)findViewById(R.id.enter_guest);
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			button.setBackgroundDrawable(getResources().getDrawable(R.drawable.inactive_grayscale));
		} else {
			button.setBackground(getResources().getDrawable(R.drawable.inactive_grayscale));
		}
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void setButtonActive() {
		Log.d("UI", "set button active");
		Button button = (Button)findViewById(R.id.enter_guest);
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			button.setBackgroundDrawable(getResources().getDrawable(R.drawable.grayscale_button_bg));
		} else {
			button.setBackground(getResources().getDrawable(R.drawable.grayscale_button_bg));
		}
	}
	
	private OnClickListener startListener = new OnClickListener() {
    	public void onClick(View v) {
    		if(validSn) { //only accept button press if screen name is of valid length
	        	EditText text  = (EditText)findViewById(R.id.sn_entry);
	        	String words;
				words = text.getText().toString();
				storeUsername(words);			
				Intent next = new Intent(LoginActivity.this, MainMenuActivity.class);
				startActivity(next);
    		}
        }
    };
    
	
  /**
    * Stores the entered string to persistent storage
    * @param String username The string being saved
    */ 
    private void storeUsername(String username)
    {
    	PreferenceUtility prefs = new PreferenceUtility();
    	prefs.saveString(getString(R.string.screenname_key), username, this);
    	
    	//also need to set twitter login flag to false		
		prefs.saveBoolean(getString(R.string.tw_login_key), false, this);
    }

}
