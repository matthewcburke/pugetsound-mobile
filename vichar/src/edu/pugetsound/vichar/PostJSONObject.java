package edu.pugetsound.vichar;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class PostJSONObject extends AsyncTask<JSONObject, Void, String> {

	private URL url;
	private PostStatusReceiver caller;
	
	private interface PostStatusReceiver {
		public void onReceivePostStatus(String status);
	}
	
	public PostJSONObject(PostStatusReceiver caller, URL url) {
		this.caller = caller;
		this.url = url;
	}
	
	@Override
    protected String doInBackground(JSONObject... jsons) {
		int status = 0;
		// Sorry, but we're only going to support one JSONObject
		JSONObject outboundJson = jsons[0];
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	 		String outboundJStr = outboundJson.toString();
			Log.d(this.toString(), "SENDING JSON: " + outboundJStr);
			conn.setDoOutput(true);	
			//conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestMethod("POST");
			conn.connect();
			OutputStream out = conn.getOutputStream();
			StringEntity se = new StringEntity(outboundJStr);
			se.setContentEncoding(
					new BasicHeader(HTTP.CONTENT_TYPE, 
							"application/json;charset=UTF-8"));
			se.writeTo(out);
			out.flush();
			out.close();
			outboundJStr = null;
			
			// It seems like getting the response code ensures that the 
			// data is transferred. It's also just a good idea to check 
			// it.
			
			// TODO return as string or int??
			status = conn.getResponseCode();
			
			// Close connection
			conn.disconnect();
		} catch(IOException e) {
			e.printStackTrace();
		}
 		return "" + status;
    }

    @Override
    protected void onPostExecute(String result) {
		if(result.equals("200")) {
			//success
		} else {
			//TODO retries?
		}
    }

}
