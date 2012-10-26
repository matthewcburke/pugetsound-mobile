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
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class HttpService extends Service {

	
private final IBinder binder = new LocalBinder();


	public void send(JSONObject jsonobj) {
		final JSONObject thejsonobj = jsonobj;
		new Thread(new Runnable() {
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(" http://puppetmaster.pugetsound.edu:4242/gameState.json");
				try {
					StringEntity words = new StringEntity(thejsonobj.toString());
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
        HttpService getService() {
        return HttpService.this;
        }
    }


	@Override
	public IBinder onBind(Intent intent) {
		return binder;

	}

}

