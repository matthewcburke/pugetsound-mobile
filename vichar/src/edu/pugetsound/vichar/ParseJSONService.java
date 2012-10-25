/**
 * 
 */
package edu.pugetsound.vichar;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


/**
 * @author Davis Shurbert
 * @version 10/24/12
 */
public class ParseJSONService extends Service {

    private final IBinder myBinder = new LocalBinder();
    
    public class LocalBinder extends Binder {
        ParseJSONService getService() {
        return ParseJSONService.this;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
                return myBinder;
    }

}
