package edu.pugetsound.vichar;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
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

import edu.pugetsound.vichar.HttpService.LocalBinder;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Implements Socket connection and I/O between this Android client
 * and a socket on the game server.
 * @author Michael DuBois
 */
public class SocketService extends Service {

	public static String serverIp = "10.150.2.55";
	//public static int serverport = 4242;
	public static int serverPort = 1730;
	Socket s;
	private InputStream dis;
	private OutputStream dos;
	private int message;
	private final IBinder binder = new LocalBinder();

	@Override
    public void onCreate() {
        super.onCreate();
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i("SocketService", "onStartCommand");
		super.onStartCommand(intent, flags, startId);

		s = new Socket();
        Runnable connect = new ConnectSocket(serverIp, serverPort);
        new Thread(connect).start();
		
        return START_STICKY;
    }
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        try {
            s.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        s = null;
    }
	
	/**
	 * A test JSON post.
	 */
	public void postTest() {
		JSONObject jsonobj = new JSONObject();
		
		// Write to JSONObject
		try {
			jsonobj.put("socketTest", "123");
		} catch (JSONException ex){
			//do nothing
		}
		
		if (s.isConnected()) {
			// Set headers
			try {
				dos = (OutputStream) s.getOutputStream();
				
				StringEntity str = new StringEntity(jsonobj.toString());
				str.setContentType("application/json;charset=UTF-8");
				str.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
				str.writeTo(dos);
				dos.flush();
				Log.d("SocketService", "postTest : Posted " + str.toString());
				Log.d("SocketService", "postTest : It was " + str.getContentLength() + "chars long.");
			} catch (IOException e) {
				Log.i("SocketService", "postTest : Cannot write to stream, Socket is closed");
			}
		}
		
		
	}
	
//	public void writeJSON(JSONObject json) {
//		try {
//			if (s.isConnected()) {
//				Log.i("SocketService", "writeToStream : Writing " + json.toString());
//				dos.writeBytes(json.toString() + '\n');
//			} else {
//				Log.i("SocketService", "writeToStream : Cannot write to stream, Socket is closed");
//			}
//		} catch(Exception IOException) {
//			// Do something
//		}
//	}
	
//	public JSONObject readJSON() {
//		JSONObject json = null;
//		if (s.isConnected()) {
//			try {				
//				//String str 
//				Log.i("SocketService", "readJSON : Reading");
//				//String str = dis.readUTFBytes(dis.bytesAvailable);
//				//Log.i("SocketService", "readJSON : Got " + baos.toString());
//				//json = new JSONObject(str);
//				//Log.i("SocketService", "readJSON : Converts to " + json.toString());
//			} catch(IOException e) {
//				// Do something
//				Log.e("readJSON()", e.getMessage());
//				
//			}
//			//catch(JSONException e) {
//				// Do something
//				//Log.e("readJSON()", e.getMessage());
//			//}
//		}
//		return json;
//	}
	
//	public void writeToStream(double lat, double lon) {
//	    try {
//	        if (s.isConnected()){
//	            Log.i("AsynkTask", "writeToStream : Writing lat, lon");
//	            dos.writeDouble(lat);
//	        } else {
//	            Log.i("AsynkTask", "writeToStream : Cannot write to stream, Socket is closed");
//	        }
//	    } catch (Exception e) {
//	        Log.i("AsynkTask", "writeToStream : Writing failed");
//	    }
//	}

//	public int readFromStream() {
//	    try {
//	        if (s.isConnected()) {
//	            Log.i("AsynkTask", "readFromStream : Reading message");
//	            message = dis.readInt();
//	        } else {
//	            Log.i("AsynkTask", "readFromStream : Cannot Read, Socket is closed");
//	        }
//	    } catch (Exception e) {
//	        Log.i("AsynkTask", "readFromStream : Writing failed");
//	    }
//	    return message;
//	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(this.toString(),"onBind SocketService");
		return binder;
	}
	
	/**
	 * A Local Binder. See Android Service Binder Pattern.
	 */
	public class LocalBinder extends Binder {
		SocketService getService() {
			return SocketService.this;
        }
    }
	
	/**
	 * A Runnable class to connect the SocketService socket
	 * @implements Runnable
	 * @author DuBious
	 */
	private class ConnectSocket implements Runnable {
        String  socketAddr;
        int socketPort;
        int TIMEOUT=5000;

        public ConnectSocket(String addr, int port) {
            socketAddr = addr;
            socketPort = port;
        }

        @Override
        public void run() {

            SocketAddress socketAddress = new InetSocketAddress(socketAddr, socketPort);
            try {
            	System.out.println(s);
                s.connect(socketAddress, TIMEOUT);
                System.out.println(s.getOutputStream());
                System.out.println("test42");
                            Log.i("connectSocket()", "Connection Succesful");
            } catch (IOException e) {
                Log.e("connectSocket()", e.getMessage());
                e.printStackTrace();
            }       
        }
    }

}


