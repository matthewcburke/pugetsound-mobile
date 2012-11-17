package edu.pugetsound.vichar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

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
import android.os.IBinder;
import android.util.Log;


/**
 * A bound service that provides a simple, non-continuous interface with the server.
 * @author Kirah Taylor
 * @version 10/24/12
 */
public class HttpService extends Service {

	
private final IBinder binder = new LocalBinder();

   /**
 	* Takes JSON objects sent by activities and passes them to the server
 	* @param jsonobj The JSONObject to be sent to the server
 	*/
	public void send(JSONObject jsonobj) {
		final JSONObject thejsonobj = jsonobj;
		new Thread(new Runnable() {
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://puppetmaster.pugetsound.edu:4242/gameState.json");
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
	
	public JSONObject getJSON() {
		JSONObject json = null;
		try {
			URL url = new URL("http://puppetmaster.pugetsound.edu:4242/gameState.json");
			URLConnection connection = url.openConnection();
			connection.addRequestProperty("Referer", "http://puppetmaster.pugetsound.edu:4242/gameState.json");
	
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = reader.readLine()) != null) {
			 builder.append(line);
			}
	
			json = new JSONObject(builder.toString());
		} catch(JSONException e){
			Log.i(this.toString(), "getJSON(): JSONException");
		} catch(IOException e) {
			Log.i(this.toString(), "getJSON(): IOException");
		}
		
		return json;
	}

	/**
	 * Describes the interface for the IBiner object passed to activities upon binding
	 * @author Kirah Taylor
	 * @version 10/24/12
	 */
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

