package edu.pugetsound.vichar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import android.view.MenuItem;
import android.content.Context;

// Testing git corruption

/**
 * Activity for the "about" screen, contains authors names 
 * and a button to main menu.
 * @author Davis Shurbert and Kirah Taylor
 * @version 10/13/12
 */

public class AboutActivity extends Activity {
    
	private WebView webView;
	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
 
		webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("http://puppetmaster.pugetsound.edu/about.html");
	}
	
}