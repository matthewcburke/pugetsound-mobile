package edu.pugetsound.vichar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Main activity, this is the first screen users will see
 * @author Nathan P
 * @version 10/16/12
 */
public class MainActivity extends Activity
							
{
    final Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void createButtons() {
        Button menub = (Button)findViewById(R.id.main_menu_button); //declaring the button
        menub.setOnClickListener(menuListener); //making the thing that checks if the button's been pushed
        
    }
    
    private OnClickListener menuListener = new OnClickListener() { //sets what happens when the button is pushed
        public void onClick(View v) { 
            
            startActivity(new Intent(context, MainMenuActivity.class));
        }
       };
       
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.enter_game:
            	startActivity(new Intent(this, GameActivity.class));
                return true;
            case R.id.enter_main_menu:
            	startActivity(new Intent(this, MainMenuActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void authorizeLoginwithVichar(View view)
    {
    	Intent login = new Intent(this, LoginActivity.class);
    	startActivity(login);
    }
    
    public void authorizeTwitter(View view)
    {
    	Intent twitterOAuthIntent = new Intent(this, TwitterOAuthActivity.class);
    	startActivity(twitterOAuthIntent);
    }
}
