package edu.pugetsound.vichar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


/**
 * A service that syncronizes a local JSONObject with it's remote counterpart
 * 
 * IMPORTANT NOTE: This service uses org.json.simple.JSONObject, 
 * NOT org.json.JSONObject! Don't be fooled. The simple library has better
 * merging support.
 * 
 * @author Kirah Taylor & Michael DuBois
 * @version 10/24/12
 */
public class NetworkingService extends Service {

	public static final String DEFAULT_SERVER_IP = "10.150.2.55";
	public static final String DEFAULT_PORT = "1730";
	public static final String DEFAULT_DOMAIN 
		= "http://" + DEFAULT_SERVER_IP + ":" + DEFAULT_PORT + "";
	
	private Timer timer = new Timer();
	private boolean polling = false;
	private JSONParser jsonParser = new JSONParser();
	private PollingTask pollingTask = null;
	public static final long POLL_INTERVAL = 100L; //in milliseconds

	// Messenger to handle inbound messages
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    // Roster of all registered clients.
 	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
 	int mValue = 0; // Holds last value set by a client.
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_JSON_STRING_VALUE = 3;
	public static final int MSG_RET_JSON_STRING_FROM_SERVER = 4;
	public static final int MSG_QUEUE_OUTBOUND_J_STRING = 5;
	public static final int MSG_INTERNAL_MISCONFIG = 6;
	public static final int MSG_NETWORKING_FAILURE = 7;
    
    /**
     * IncomingHandler performs internal functions in response to the received 
     * messages of each predefined type denoted by NetworkingServices MSG_ 
     * constants.
     * @author Michael DuBois
     */
    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
        	mValue = msg.what; // Remember this msg type
        	switch (mValue) {
	            
        		case MSG_RET_JSON_STRING_FROM_SERVER: {
	            	serveMsg(MSG_RET_JSON_STRING_FROM_SERVER, msg.getData());
	            	break;
	            }
	            
	            case MSG_QUEUE_OUTBOUND_J_STRING: {
	            	String jStr = msg.getData()
	            			.getString("" + MSG_QUEUE_OUTBOUND_J_STRING);
	            	queueOutboundJString(jStr);
	            	break;
	            }
	            
	            case MSG_NETWORKING_FAILURE: {
	            	// Tell caller activities
	            	serveMsg(MSG_NETWORKING_FAILURE, null);
	            	// Try to re-establish the connection
	            	restartPolling();
	            	break;
	            }
	            
	            case MSG_REGISTER_CLIENT: {
	                addClient(msg.replyTo);
	                startPolling(); // Starts polling if not already
	                break;
	            }
	            
	            case MSG_UNREGISTER_CLIENT: {
	            	removeClient(msg.replyTo);
	                break;
	            }
	            
	            default: {
	                super.handleMessage(msg);
	            }
	        }
        }
    }
    
    /**
     * Add a client to the client array
     * @param m
     */
    private void addClient(Messenger m) {
    	mClients.add(m);
    }
    
    /**
     * Remove a client from the client array and check the validity of service's 
     * existence
     * @param m reference to the client to be removed
     */
	private void removeClient(Messenger m) {
		mClients.remove(m);
		checkVitals();
	}
	
	/**
	 * Remove a client from the client array by index and check the validity of 
	 * service's existence
	 * @param i the index (in the client array) for the client to be removed
	 */
	private void removeClient(int i) {
		mClients.remove(i);
		checkVitals();
	}
	
	/**
	 * Checks if we have clients, kills self if we don't.
	 */
	private void checkVitals() {
		if(mClients.size() <= 0) {
			// We have no clients, time to die.
			this.stopPolling();
			this.stopSelf();
		}
	}
    
    /**
     * Serves a Bundle to all mClients
     * @param b an Android Bundle containing a JSON_STRING
     */
    private void serveMsg(int code, Bundle b) {
    	Message msg = Message.obtain(null, code);
    	if(b != null) {
    		msg.setData(b);
    	}
    	
    	//Send msg to subscribers
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list\
            	Log.i(this.toString(), "Activity client is dead.");
                removeClient(i);
            }
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	
	@Override
	public void onDestroy() {
		this.stopPolling();
		super.onDestroy();
	}
	
//------------------------------------------------------------------------------
// POLLING
//------------------------------------------------------------------------------

   /**
    * Starts the polling clock. Subsequent calls have no effect.
    */
	public void startPolling() {
		if(!polling) {
			try {
				if(pollingTask == null) {
					URL url = new URL(DEFAULT_DOMAIN);
					pollingTask = new PollingTask(url);
				}
				// TODO change to ScheduledThreadPoolExecutor and Future
				// run pollingTask every 100ms
				timer.scheduleAtFixedRate(pollingTask, 0, POLL_INTERVAL);
				polling = true;
			} catch (MalformedURLException e) {
				serveMsg(MSG_INTERNAL_MISCONFIG, null);
			}
		}
	}
	
	/**
	 * Stops the polling clock. Subsequent calls have no effect.
	 */
	public void stopPolling() {
		if(pollingTask != null) {
			pollingTask.cancel();
		}
		polling = false;
	}
	
	public void restartPolling() {
		if(pollingTask != null) {
			pollingTask.resetFailureCount();
		}
	}
	
	public void queueOutboundJString(String jStr) {
		try {
			JSONObject json = (JSONObject) jsonParser.parse(jStr);
			pollingTask.outboundQueue.offer(json);
		} catch(ParseException e) {
			// do something
			Log.i(this.toString(), "offer ParseException!");
		}
	}
	
	/**
	 * A Runnable task that is run every time the polling clock ticks.
	 * This task pulls JSON from the server and passes it back to the
	 * caller as a JSON string. When outboundJSON objects are queued
	 * for processing, the task merges and POSTs them first, then performs 
	 * the GET request.
	 * 
	 * @author Michael DuBois
	 */
    private class PollingTask extends TimerTask {
		public volatile ConcurrentLinkedQueue<JSONObject> outboundQueue 
			= new ConcurrentLinkedQueue<JSONObject>();
		protected ConcurrentLinkedQueue<JSONObject> retryQueue 
		= new ConcurrentLinkedQueue<JSONObject>();
		// Track consecutive failures
		protected final static int MAX_CONSECUTIVE_FAILURES = 10;
		protected int consecutiveFailures = 0;
		protected URL url = null;
		
		PollingTask(URL remoteUrl) {
			this.url = remoteUrl;
		}
		
		public void run() {
			if(consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
				msgCaller(MSG_NETWORKING_FAILURE, null);
				return;
			}
			
			try {
				JSONObject json = getMergedQueue();
				String ret = post(json.toString());
				if(ret==null && consecutiveFailures < MAX_CONSECUTIVE_FAILURES) {
					// Put it in the retry queue for the next round
					retryQueue.offer(json);
				} else if(ret != null) {
					returnJstrToCaller(ret);
				}
			} catch (UnsupportedEncodingException e){
				Log.i(this.toString(), "UnsupportedEncodingException");
				msgCaller(MSG_NETWORKING_FAILURE, null);
			} catch (ProtocolException e){
				Log.i(this.toString(), "ProtocolException");
				msgCaller(MSG_NETWORKING_FAILURE, null);
			} catch (IOException e){
				Log.i(this.toString(), "IOException");
				msgCaller(MSG_NETWORKING_FAILURE, null);
			}
		}
		
		protected JSONObject getMergedQueue() {
			// Lock only this queue for the duration of this operation
			synchronized(outboundQueue) {
				// Process the outbound queue
				JSONObject outboundJson = outboundQueue.poll(); // null if empty
				if(outboundJson == null) {
					outboundJson = new JSONObject();
				}
				// If/While something remains in the queue,
				while(outboundQueue.peek() != null) {
					// merge it into outboundJson 
					// this way we don't have to do multiple POSTs
					outboundJson.putAll(outboundQueue.poll());
				}
				return outboundJson;
			}
		}
		
		protected String post(String jstr) 
			throws UnsupportedEncodingException, 
			ProtocolException, 
			IOException
		{
			String ret = null;
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);	
			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestMethod("POST");
			conn.connect();
			OutputStream out = conn.getOutputStream();
			StringEntity se = new StringEntity(jstr);
			se.setContentEncoding(
					new BasicHeader(HTTP.CONTENT_TYPE, 
							"application/json;charset=UTF-8"));
			se.writeTo(out);
			out.flush();
			out.close();
			
			// It seems like getting the response code ensures that the 
			// data is transferred. It's also just a good idea to check 
			// it.
			int status = conn.getResponseCode();
			//System.out.println("ResponseCode: " + status);
			
			if(status == 200) {
				consecutiveFailures = 0;
				ret = parseResponseBody(conn);
			} else {
				consecutiveFailures++;
			}

			// Close connection - so it doesn't mess with the next one
			conn.disconnect();
			return ret;
		}
		
		protected String parseResponseBody(HttpURLConnection conn) 
			throws IOException
		{
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
			return body;
		}
		
		protected void returnJstrToCaller(String jstr)
		{
			// return the resulting String via Messenger
	 		// first, Bundle JSON string as a string
	    	Bundle b = new Bundle();
	    	b.putString("" + MSG_RET_JSON_STRING_FROM_SERVER, jstr);
	    	Message msg = Message.obtain(null, MSG_RET_JSON_STRING_FROM_SERVER);
	    	msg.setData(b);
	    	try {
				mMessenger.send(msg);
			} catch(RemoteException e) {
				// The caller thread is dead?
				// There's really nothing we can do if this happens...
				Log.i(this.toString(), "RemoteException from timer thread.");
			}
		}
		
		protected void msgCaller(int code, Bundle b) {
			Message msg = Message.obtain(null, code);
	    	if(b != null) {
	    		msg.setData(b);
	    	}
	    	try {
				mMessenger.send(msg);
			} catch(RemoteException e) {
				// The caller thread is dead?
				// There's really nothing we can do if this happens...
				Log.i(this.toString(), "RemoteException from timer thread.");
			}
		}
		
		public void resetFailureCount() {
			consecutiveFailures = 0;
		}
		
	}	
	
//	private PollingTask pollingTask = new PollingTask()
//	{
//		
//		public void run() {
//			String returnStr = "{}"; // empty JSON string by default
//			String urlString = DEFAULT_DOMAIN;
//
//			try { // POST/GET the latest states
//				URL url = new URL(urlString);
//				HttpURLConnection conn = 
//						(HttpURLConnection) url.openConnection();
//				
//				// Process the outbound queue
//				JSONObject outboundJson = outboundQueue.poll(); // null if empty
//				// While something remains in the queue, 
//				synchronized(outboundQueue) {
//					while(outboundQueue.peek() != null) {
//						// merge it into outboundJson 
//						// this way we don't have to do multiple POSTs
//						outboundJson.putAll(outboundQueue.poll());
//					}
//				}
//				
//				// If we have something to send
//				if(outboundJson != null) {
//					String outboundJStr = outboundJson.toString();
//					//Log.d(this.toString(), "SENDING JSON: " + outboundJStr);
//					conn.setDoOutput(true);	
//					//conn.setDoInput(true);
//					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
//					conn.setRequestProperty("Accept", "application/json");
//					conn.setRequestMethod("POST");
//					conn.connect();
//					OutputStream out = conn.getOutputStream();
//					StringEntity se = new StringEntity(outboundJStr);
//					se.setContentEncoding(
//							new BasicHeader(HTTP.CONTENT_TYPE, 
//									"application/json;charset=UTF-8"));
//					se.writeTo(out);
//					out.flush();
//					out.close();
//					outboundJStr = null;
//					
//					// It seems like getting the response code ensures that the 
//					// data is transferred. It's also just a good idea to check 
//					// it.
//					int status = conn.getResponseCode();
//					if(status == 200) {
//						consecutiveFailures = 0;
//					} else {
//						// Hope to get it on the next try
//						consecutiveFailures++;
//						//TODO do something if it fails over and over
//					}
//					System.out.println("ResponseCode: " + status);
//					
//					// Close connection - so it doesn't mess with the next one
//					conn.disconnect();
//				}
//
//				//TODO right now server check for POST/GET, should return 
//				//something on POST too In the meantime, we do two requests.
//				URL url2 = new URL(urlString);
//				HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
//				
//				// Parse the response into a JSON string:
//				// First, get input stream
//				InputStream in = conn2.getInputStream();
//				
//				// Use the encoding listed in the HTTP header
//				String encoding = conn2.getContentEncoding();
//				// Or, if missing, default to UTF-8 instead of system default
//				encoding = encoding == null ? "UTF-8" : encoding;
//				
//				// Read out the body as a string
//				String body = IOUtils.toString(in, encoding);
//				in.close(); // close the stream
//				//Log.d(this.toString(),"body: " + body.toString());
//				returnStr = body;
//
//				// It seems like getting the response code ensures that the 
//				// data is transferred. It's also just a good idea to check it.
//				int status = conn2.getResponseCode();
//				if(status == 200) {
//					consecutiveFailures = 0;
//				} else {
//					consecutiveFailures++;
//				}
//				System.out.println("ResponseCode: " + status);
//				
//				// Close connection
//				conn2.disconnect();
//			}
//			catch (ClientProtocolException e) {
//				//TODO: connection problem exception
//				Log.i(this.toString(), "ClientProtocolException");
//			}
//			catch (UnsupportedEncodingException e) {
//				//TODO: connection problem exception
//				Log.i(this.toString(), "UnsupportedEncodingException");
//			} 
//			catch (IOException e) {
//				//TODO: connection problem exception
//				Log.i(this.toString(), "IOException");
//			}
//	 		
//	 		//TODO check for failure
//	 		
//	 		// return the resulting String via Messenger
//	 		// first, Bundle JSON string as a string
//	    	Bundle b = new Bundle();
//	    	b.putString("" + MSG_SET_JSON_STRING_VALUE, returnStr);
//	    	Message msg = Message.obtain(null, MSG_RET_JSON_STRING_FROM_SERVER);
//	    	msg.setData(b);
//	    	try {
//				mMessenger.send(msg);
//			} catch(RemoteException e) {
//				// The caller thread is dead?
//				// There's really nothing we can do if this happens...
//				Log.i(this.toString(), "RemoteException from timer thread.");
//			}
//		}
//	};
}

