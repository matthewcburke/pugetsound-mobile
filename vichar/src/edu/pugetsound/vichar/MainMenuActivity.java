package edu.pugetsound.vichar;

import edu.pugetsound.vichar.ar.ARGameActivity; // MB: so we can start the ARGameActivity
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.graphics.PixelFormat;
import android.widget.Button;
import android.graphics.drawable.ScaleDrawable;
import android.widget.FrameLayout;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.content.Context;

/**
 * Activity for the main menu screen, contains buttons to navigate to other 
 * important screens.
 * @author Davis Shurbert
 *
 */
public class MainMenuActivity extends WifiRequiredActivity {
	final Context context = this;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//help with banding on gradients
		getWindow().setFormat(PixelFormat.RGBA_8888);

		setContentView(R.layout.activity_main_menu);

		//set title to screenname        
		setTitle(getScreenname());
		createButtons();            
	}

	private void createButtons() {
		Button gameb = (Button)findViewById(R.id.game_button); //declaring the button
		gameb.setOnClickListener(gameListener); //making the thing that checks if the button's been pushed

		//animation on play button
		final Animation animation = new AlphaAnimation(1, (float) 0.8); // Change alpha from fully visible to invisible
		animation.setDuration(1000); // duration - half a second
		animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
		animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
		animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
		final Button btn = (Button) findViewById(R.id.game_button);
		btn.startAnimation(animation);

		//scaling on play button icon

		// Get the scale drawable from your resources
		ScaleDrawable scaleDraw = (ScaleDrawable)getResources().getDrawable(R.drawable.robot_icon);
		// set the Level to 1 (or anything higher than 0)
		scaleDraw.setLevel(1);

		FrameLayout foreb = (FrameLayout)findViewById(R.id.foreground_gameb);

		// Now assign the drawable where you need it

		//    foreb.setForeground(scaleDraw);


		Button mainb = (Button)findViewById(R.id.about_button); //declaring the button
		mainb.setOnClickListener(aboutListener); //making the thing that checks if the button's been pushed

		Button leaderboardb = (Button)findViewById(R.id.leaderboard_button); 
		leaderboardb.setOnClickListener(leaderboardListener);

		Button rulesb = (Button)findViewById(R.id.rules_button); 
		rulesb.setOnClickListener(rulesListener);

		Button settingsb = (Button)findViewById(R.id.settings_button); 
		settingsb.setOnClickListener(settingsListener);
	}

	private OnClickListener settingsListener = new OnClickListener() { //sets what happens when the button is pushed
		public void onClick(View v) { 
			logoutTwitter();
			startActivity(new Intent(context, MainActivity.class));
		}
	};

	private OnClickListener rulesListener = new OnClickListener() { //sets what happens when the button is pushed
		public void onClick(View v) { 

			startActivity(new Intent(context, RulesActivity.class));
		}
	};

	private OnClickListener leaderboardListener = new OnClickListener() { //sets what happens when the button is pushed
		public void onClick(View v) { 

			startActivity(new Intent(context, LeaderboardActivity.class));
		}
	};

	private OnClickListener gameListener = new OnClickListener() { //sets what happens when the button is pushed
		public void onClick(View v) { 

			startActivity(new Intent(context, ARGameActivity.class)); // MB: Start ARGameActivity
			v.clearAnimation();
		}
	};

	private OnClickListener aboutListener = new OnClickListener() { //sets what happens when the button is pushed
		public void onClick(View v) { 

			startActivity(new Intent(context, AboutActivity.class));
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
		return true;
	}

	/**
	 * Returns current user's screenname
	 * @return Username of current user
	 */
	private String getScreenname() {
		PreferenceUtility pu = new PreferenceUtility();
		return pu.returnSavedString(getString(R.string.screenname_key), getString(R.string.prefs_error), this);
	}    

	/**
	 * "Logs out" of twitter. Doesn't actually erase any OAuth information
	 */
	public void logoutTwitter() {
		PreferenceUtility pu = new PreferenceUtility();
		pu.saveBoolean(getString(R.string.tw_login_key), false, this);
		startActivity(new Intent(this, MainActivity.class)); //redirect to login screen 
	}
}