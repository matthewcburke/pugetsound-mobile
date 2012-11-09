package edu.pugetsound.vichar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


/**
 * A service that syncronizes a local JSONObject with it's remote counterpart
 * @author Kirah Taylor & Michael DuBois
 * @version 10/24/12
 */
public class NetworkingService extends Service {

	public static final String DEFAULT_DOMAIN_IP = "10.150.2.55";
	public static final String DEFAULT_DOMAIN = "http://" + DEFAULT_DOMAIN_IP;
	public static final long POLL_INTERVAL = 100L; //in milliseconds
	private final IBinder binder = new LocalBinder();
	private Timer timer = new Timer();
	private boolean polling = false;
	// Keeps track of all current registered clients.
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	int mValue = 0; // Holds last value set by a client.
	
	static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_INT_VALUE = 3;
    static final int MSG_SET_STRING_VALUE = 4;
    static final int MSG_SET_JSON_STRING_VALUE = 5;
    static final int MSG_TIMER_TICK = 6;
    static final int MSG_RET_JSON_STRING_FROM_SERVER = 7;
    
	// Messenger to handle inbound messages
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
            	Log.d(this.toString(), "handleMessage: registering client: " + msg.replyTo.toString());
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_TIMER_TICK:
            	break;
            case MSG_RET_JSON_STRING_FROM_SERVER:
            	passTheBuck(msg.getData());
            	break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    
    private void passTheBuck(Bundle b) {
    	Message msg = Message.obtain(null, MSG_SET_JSON_STRING_VALUE);
    	msg.setData(b);
    	
    	//Send msg to subscribers
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list\
            	Log.i(this.toString(), "Activity client is dead.");
                mClients.remove(i);
            }
        }
    }
    
   private void serveJSONObject(JSONObject json) {
    	//Bundle JSONObject as a string
    	Bundle b = new Bundle();
    	b.putString("" + MSG_SET_JSON_STRING_VALUE, json.toString());
    	Message msg = Message.obtain(null, MSG_SET_JSON_STRING_VALUE);
    	msg.setData(b);
    	
    	//Send msg to subscribers
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list\
            	Log.i(this.toString(), "Activity client is dead.");
                mClients.remove(i);
            }
        }
    }
    
   /**
    * Starts the polling clock. Subsequent calls have no effect.
    */
	public void startPolling() {
		if(!polling) {
			// run pollingTask every 100ms
			timer.scheduleAtFixedRate(pollingTask, 0, POLL_INTERVAL);
			polling = true;
		}
	}
	
	/**
	 * Stops the polling clock. Subsequent calls have no effect.
	 */
	public void stopPolling() {
		pollingTask.cancel();
		polling = false;
	}
	
	private abstract class PollingTask extends TimerTask {
		protected JSONObject outboundJson = null;
		public void setOutboundJson(JSONObject json) {
			outboundJson = json;
		}
	}
	
	private PollingTask pollingTask = new PollingTask(){
		
		public void run() {
			//Notify the host Service of timer tick
			Message msg = Message.obtain(null, MSG_TIMER_TICK);
			try {
				mMessenger.send(msg);
			} catch(RemoteException e) {
				//This should never happen... right?
				Log.i(this.toString(), "RemoteException from timer thread.");
			}
			
	 		String returnStr = "";
	 		
	 		// POST the latest state
	 		//HttpClient httpClient = new DefaultHttpClient();
			//HttpPost httpPost = new HttpPost(url);
			try {
				String urlString = "http://10.150.2.55:4242/";
				URL url = new URL(urlString);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if(outboundJson != null) {
					Log.d(this.toString(), "SENDING JSON: " + outboundJson.toString());
					conn.setDoOutput(true);	
					//conn.setDoInput(true);
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					conn.setRequestProperty("Accept", "application/json");
					conn.setRequestMethod("POST");
					conn.connect();
					OutputStream out = conn.getOutputStream();
					StringEntity se = new StringEntity(outboundJson.toString());
					se.setContentEncoding(
							new BasicHeader(HTTP.CONTENT_TYPE, 
									"application/json;charset=UTF-8"));
					se.writeTo(out);
					out.flush();
					out.close();
					outboundJson = null;
					
					int status = conn.getResponseCode();
					System.out.println("ResponseCode: " + status);
					
					conn.disconnect();
				}

				//TODO right now server check for POST/GET, should return something on POST too
				// In the meantime, we do two separate requests.
				URL url2 = new URL(urlString);
				HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
				
				// Parse the response into a JSONObject
				InputStream in = conn2.getInputStream();
				// Use the encoding listed in the HTTP header
				String encoding = conn2.getContentEncoding();
				// Or, if missing, default to UTF-8 instead of system default
				encoding = encoding == null ? "UTF-8" : encoding;
				String body = IOUtils.toString(in, encoding);
				in.close();
				Log.d(this.toString(),"body: " + body.toString());
				returnStr = body;

				int status = conn2.getResponseCode();
				System.out.println("ResponseCode: " + status);
				conn2.disconnect();
			}
			catch (ClientProtocolException e) {
				//do nothing
				Log.i(this.toString(), "ClientProtocolException");
			}
			catch (UnsupportedEncodingException e) {
				//do nothing
				Log.i(this.toString(), "UnsupportedEncodingException");
			} 
			catch (IOException e) {
				// do nothing
				Log.i(this.toString(), "IOException");
			}
	 		
	 		//TODO check for failure
	 		
	 		// return the resulting String via Messenger
	 		// first, Bundle JSON string as a string
	    	Bundle b = new Bundle();
	    	b.putString("" + MSG_SET_JSON_STRING_VALUE, returnStr);
	    	msg = Message.obtain(null, MSG_RET_JSON_STRING_FROM_SERVER);
	    	msg.setData(b);
	    	try {
				mMessenger.send(msg);
			} catch(RemoteException e) {
				//This should never happen... right?
				Log.i(this.toString(), "RemoteException from timer thread.");
			}
		}
	};
	
	public void queueOutboundJson(JSONObject json) {
		pollingTask.setOutboundJson(json);
	}
	
	/**
 	* Takes JSON objects sent by activities and passes them to the server
 	* @param jsonobj The JSONObject to be sent to the server
 	*/
	@SuppressLint("NewApi") // Suppress lint warnings b/c we check API level
	public void postJSONObject(JSONObject json, String url) {
		if(url == null){
			// default to
			url = DEFAULT_DOMAIN + ":4242";
		}
		
		PostJSONObject task = new PostJSONObject();
		task.setUrl(url);
		task.execute(json);
	}
	
	@SuppressLint("NewApi") // Suppress lint warnings b/c we check API level
	public void fetchJSONObject(URL url) {
		GetJSONObject task = new GetJSONObject();
		task.execute(url);
	}
	
	private class PostJSONObject extends AsyncTask<JSONObject, Void, String> {
		
		// The destination url
		private String url;
		
		@Override
	    protected String doInBackground(JSONObject... jsons) {
	 		if(url == null) {
	 			return null;
	 		}
			
			String response = "";
			for(JSONObject json: jsons) {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);
				try {
					StringEntity se = new StringEntity(json.toString());
					se.setContentType("application/json;charset=UTF-8");
					se.setContentEncoding(
							new BasicHeader(HTTP.CONTENT_TYPE, 
									"application/json;charset=UTF-8"));
					httpPost.setEntity(se);
					httpClient.execute(httpPost);
				}
				catch (ClientProtocolException e) {
					//do nothing
				}
				catch (UnsupportedEncodingException e) {
					//do nothing
				} 
				catch (IOException e) {
					// do nothing
				}
	 		}
	 		return response;
	    }

	    @Override
	    protected void onPostExecute(String result) {
			//TODO check success and return true or false??
	    	//TODO retries?
			if(result.equals("200")) {
			    	  //someObject.someFunction(true);
			  }
	    }
	    
	    protected void setUrl(String newUrl){
	    	url = newUrl;	    	
	    }
	}
	
	private class GetJSONObject extends AsyncTask<URL, Void, JSONObject> {
	 	@Override
	    protected JSONObject doInBackground(URL... urls) {
	 		// Initialize return object
	 		JSONObject json = null;
	 		
	 		// Sorry, but we're only going to support one URL
	 		URL url = urls[0];
	 		
	 		try {
				//URL url = new URL("http://puppetmaster.pugetsound.edu:4242/gameState.json");
				URLConnection connection = url.openConnection();
		
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while((line = reader.readLine()) != null) {
					builder.append(line);
				}
		
				json = new JSONObject(builder.toString());
			} catch(JSONException e){
				Log.i(this.toString(), "getJSON(): JSONException");
			} catch(IOException e) {
				Log.i(this.toString(), "getJSON(): IOException");
			}
	 		
	 		//TODO check for failure
	 		
	 		// return the resulting JSONObject
	 		return json;
	    }

	    @Override
	    protected void onPostExecute(JSONObject result) {
	    	// Call function to update gameState
	    	serveJSONObject(result);
	    }
	}
	
	//TODO this is stupid
	public IBinder getMessengerBinder() {
		return mMessenger.getBinder();
	}
	
	/**
	 * Describes the interface for the IBinder object passed to activities upon binding
	 */
	public class LocalBinder extends Binder {
		NetworkingService getService() {
			return NetworkingService.this;
	    }
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

}

