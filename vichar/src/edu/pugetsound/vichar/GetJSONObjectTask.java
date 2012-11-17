package edu.pugetsound.vichar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class GetJSONObjectTask extends AsyncTask<URL, Void, JSONObject> {

	private JSONObjectReceiver caller;
	
	private interface JSONObjectReceiver {
		public void onReceiveJSONObject(JSONObject json);
	}
	
	public GetJSONObjectTask(JSONObjectReceiver caller) {
		this.caller = caller;
	}
	
	@Override
    protected JSONObject doInBackground(URL... urls) {
 		// Initialize return object
 		JSONObject json = null;
 		
 		// Sorry, but we're only going to support one URL
 		URL url = urls[0];
 		
 		try {
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			// Parse the response into a JSON string:
			// First, get input stream
			InputStream in = conn.getInputStream();
			
			// Use the encoding listed in the HTTP header
			String encoding = conn.getContentEncoding();
			// Or, if missing, default to UTF-8 instead of system default
			encoding = encoding == null ? "UTF-8" : encoding;
			
			// Read out the body as a string
			String body = IOUtils.toString(in, encoding);
			in.close(); // close the stream

			// It seems like getting the response code ensures that the 
			// data is transferred. It's also just a good idea to check it.
			int status = conn.getResponseCode();
			
			// Close connection
			conn.disconnect();
	
			json = new JSONObject(body);
		} catch(JSONException e){
			Log.i(this.toString(), "getJSON(): JSONException");
		} catch(IOException e) {
			Log.i(this.toString(), "getJSON(): IOException");
		}
 		
 		//TODO check for failure and retry a few times?
 		
 		// return the resulting JSONObject
 		return json;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
    	this.caller.onReceiveJSONObject(result);
    }

}
