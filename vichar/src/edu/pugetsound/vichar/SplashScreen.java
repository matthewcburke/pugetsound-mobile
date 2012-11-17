package edu.pugetsound.vichar;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

/**"Loading" screen to run when application is launched.
 * 
 * @author Davis Shurbert
 *
 */
public class SplashScreen extends Activity {
    private long splashDelay = 1000;//stays on screen for 2 seconds
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        
        //Basically a thread "that can be scheduled for one-time or 
        //repeated execution by a Timer."
        TimerTask task = new TimerTask() //the thread
        {
            @Override
            public void run() {//must override run() method
                finish();//terminates the screen so users can't navigate to it using the back button
                Intent mainIntent = new Intent().setClass(SplashScreen.this, MainActivity.class);
                startActivity(mainIntent);//starts the main activity
            }
            
        };
        Timer timer = new Timer();
        timer.schedule(task, splashDelay);//implements the delay and runs the thread              
    }    
}
