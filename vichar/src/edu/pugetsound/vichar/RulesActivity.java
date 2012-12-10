package edu.pugetsound.vichar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;

/**
 * Activity for the "Rules" screen, implements a sliding view of images to explain how
 * to play game
 * @author Nathan Pastor
 * @version 10/16/12
 */
public class RulesActivity extends WifiRequiredActivity {

/*
 * (non-Javadoc)
 * @see android.app.Activity#onCreate(android.os.Bundle)
 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //hide title/action bar: let rules fill as much screen as possible
        requestWindowFeature(Window.FEATURE_NO_TITLE);	
        
        setContentView(R.layout.activity_rules);        
    }
}
