package edu.pugetsound.vichar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class httpService extends Service {

	
private final IBinder binder = new LocalBinder();


	public void send(Intent intent) {
		final Intent theIntent = intent;
		new Thread(new Runnable() {
			public void run() {
				Bundle extras;
				String newString;
				extras = theIntent.getExtras();
				newString= extras.getString("WORDS");
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://puppetmaster.pugetsound.edu:1730");
				JSONObject jsonobj;
				jsonobj = new JSONObject();
				try {
					jsonobj.put("oh man", newString);
					JSONObject header = new JSONObject();
					header.put("man this header", "headering it up");
				}
				catch (JSONException ex){
					//do nothing
				}
				try {
					StringEntity words = new StringEntity(jsonobj.toString());
					words.setContentType("application/json;charset=UTF-8");
					words.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
					httppost.setEntity(words);
					httpclient.execute(httppost);
				}
				catch (ClientProtocolException stuff) {
					//do nothing
				}
				catch (UnsupportedEncodingException thing) {
					//do nothing
				} 
				catch (IOException e) {
					// do nothing
				}
			}
	    }).start();
	}

	public class LocalBinder extends Binder {
        httpService getService() {
        return httpService.this;
        }
    }


	@Override
	public IBinder onBind(Intent intent) {
		return binder;

	}

}


