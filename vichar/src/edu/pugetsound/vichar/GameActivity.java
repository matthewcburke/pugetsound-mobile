package edu.pugetsound.vichar;

import android.os.Bundle;
import android.view.Menu;
//Import Fragment dependencies
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;

/**
 * The in-game Activity
 * Extends FragmentActivity to ensure support for Android 2.x
 */
public class GameActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_game, menu);
        return true;
    }
}
