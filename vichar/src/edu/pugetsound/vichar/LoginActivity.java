package edu.pugetsound.vichar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;


/**
 * An activity that allows the user to log into the game through the Vi-Char
 * server instead of twitter. 
 * @author Kirah Taylor & Nathan Pastor
 * @version 12/9/12
 */

public class LoginActivity extends Activity {	
	
	EditText snEntry;
	Boolean validSn; //is current screen name of valid length?

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);		
		//minimize button pixelation
		getWindow().setFormat(PixelFormat.RGBA_8888);
		
		validSn = false;
	   
	    setContentView(R.layout.activity_login);
	    Button button = (Button) findViewById(R.id.enter_guest);
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
	
	/**
	 * Sets button state to inactive
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setButtonInactive() {
		Log.d("UI", "set button inactive");
		Button button = (Button)findViewById(R.id.enter_guest);
		//deal with deprecated method calls, ugh. setting background selector
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			button.setBackgroundDrawable(getResources().getDrawable(R.drawable.enter_guest_bg));
		} else {
			button.setBackground(getResources().getDrawable(R.drawable.enter_guest_bg));
		}
		
		//set opacity
        AlphaAnimation alpha = new AlphaAnimation(1f, 0.50f);
        alpha.setFillAfter(true);
        button.startAnimation(alpha);
	}
	
	/**
	 * Sets button state to active
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void setButtonActive() {
		Log.d("UI", "set button active");
		Button button = (Button)findViewById(R.id.enter_guest);
		//gross deprecated method calls, setting background selector
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			button.setBackgroundDrawable(getResources().getDrawable(R.drawable.grayscale_button_bg));
		} else {
			button.setBackground(getResources().getDrawable(R.drawable.grayscale_button_bg));
		}		
		
		//set opacity
        AlphaAnimation alpha = new AlphaAnimation(0.50f, 1f);
        alpha.setFillAfter(true);
        button.startAnimation(alpha);
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
