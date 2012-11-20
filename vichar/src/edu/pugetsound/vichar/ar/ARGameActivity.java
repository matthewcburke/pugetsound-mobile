/*==============================================================================
            Copyright (c) 2012 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    Formerly ImageTargets.java

@brief
    Based off of the Sample for ImageTargets

==============================================================================*/
/*
 * Modified by the augmented reality team of CSCI 240, University of Puget Sound
 *
 * @version 2012.11.10
 *
 * @author Matt Burke
 * 
 */

package edu.pugetsound.vichar.ar;

import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
// import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
//Import Fragment dependencies
import android.support.v4.app.FragmentActivity;

import com.qualcomm.QCAR.QCAR;

import edu.pugetsound.vichar.GameActivity;
import edu.pugetsound.vichar.Installation;
import edu.pugetsound.vichar.NetworkingService;
import edu.pugetsound.vichar.R;


/** The main activity for the ARGameActivity. */
public class ARGameActivity extends FragmentActivity implements OnTouchListener 
{
    // Application status constants:
    private static final int APPSTATUS_UNINITED         = -1;
    private static final int APPSTATUS_INIT_APP         = 0;
    private static final int APPSTATUS_INIT_QCAR        = 1;
    private static final int APPSTATUS_INIT_TRACKER     = 2;    
    private static final int APPSTATUS_INIT_APP_AR      = 3;
    private static final int APPSTATUS_LOAD_TRACKER     = 4;
    private static final int APPSTATUS_INITED           = 5;
    private static final int APPSTATUS_CAMERA_STOPPED   = 6;
    private static final int APPSTATUS_CAMERA_RUNNING   = 7;
    
    // Name of the native dynamic libraries to load:
    private static final String NATIVE_LIB_AR = "VicharAR";    
    private static final String NATIVE_LIB_QCAR = "QCAR"; 

    // Our OpenGL view:
    private QCARSampleGLView mGlView;
    
    // The view to display the sample splash screen:
    private ImageView mSplashScreenView;
    
    // The handler and runnable for the splash screen time out task.
    private Handler mSplashScreenHandler;
    private Runnable mSplashScreenRunnable;    
    
    // The minimum time the splash screen should be visible:
    private static final long MIN_SPLASH_SCREEN_TIME = 2000;    
    
    // The time when the splash screen has become visible:
    long mSplashScreenStartTime = 0;
    
    // Our renderer:
    private ARGameRenderer mRenderer;
    
    // Display size of the device
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    
    // The current application status
    private int mAppStatus = APPSTATUS_UNINITED;
    
    // The async tasks to initialize the QCAR SDK 
    private InitQCARTask mInitQCARTask;
    private LoadTrackerTask mLoadTrackerTask;

    // An object used for synchronizing QCAR initialization, dataset loading and
    // the Android onDestroy() life cycle event. If the application is destroyed
    // while a data set is still being loaded, then we wait for the loading
    // operation to finish before shutting down QCAR.
    private Object mShutdownLock = new Object();   
    
    // QCAR initialization flags
    private int mQCARFlags = 0;
    
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;
    private int mSplashScreenImageResource = 0;
    
    // The menu item for swapping data sets:
    MenuItem mDataSetMenuItem = null;
    boolean mIsStonesAndChipsDataSetActive  = false;
    
  //JSON namespaces
    private static final String GAME_ENGINE_NAMESPACE = "engine";
    private static final String WEB_NAMESPACE = "web";
    private String deviceUUID; // Device namespace
    	
	// Service Stuff
    private Messenger networkingServiceMessenger = null;
    boolean isBoundToNetworkingService = false;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    private float touchX, touchY;
    private JSONObject gameState; // TODO delete. deprecated.
    
    /** Static initializer block to load native libraries on start-up. */
    static
    {
        loadLibrary(NATIVE_LIB_QCAR);
        loadLibrary(NATIVE_LIB_AR);
    }
    
    /** An async task to initialize QCAR asynchronously. */
    private class InitQCARTask extends AsyncTask<Void, Integer, Boolean>
    {   
        // Initialize with invalid value
        private int mProgressValue = -1;
        
        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap with initialization:
            synchronized (mShutdownLock)
            {
                QCAR.setInitParameters(ARGameActivity.this, mQCARFlags);
                
                do
                {
                    // QCAR.init() blocks until an initialization step is complete,
                    // then it proceeds to the next step and reports progress in
                    // percents (0 ... 100%)
                    // If QCAR.init() returns -1, it indicates an error.
                    // Initialization is done when progress has reached 100%.
                    mProgressValue = QCAR.init();
                    
                    // Publish the progress value:
                    publishProgress(mProgressValue);
                    
                    // We check whether the task has been canceled in the meantime
                    // (by calling AsyncTask.cancel(true))
                    // and bail out if it has, thus stopping this thread.
                    // This is necessary as the AsyncTask will run to completion
                    // regardless of the status of the component that started is.
                } while (!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);
                
                return (mProgressValue > 0);                
            }
        }

        
        protected void onProgressUpdate(Integer... values)
        {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }

        
        protected void onPostExecute(Boolean result)
        {
            // Done initializing QCAR, proceed to next application
            // initialization status:
            if (result)
            {
                DebugLog.LOGD("InitQCARTask::onPostExecute: QCAR initialization" +
                                                            " successful");

                updateApplicationStatus(APPSTATUS_INIT_TRACKER);
            }
            else
            {
                // Create dialog box for display error:
                AlertDialog dialogError = new AlertDialog.Builder(ARGameActivity.this).create();
                dialogError.setButton(
                	DialogInterface.BUTTON_POSITIVE,
                    "Close",
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // Exiting application
                            System.exit(1);
                        }
                    }
                ); 
                
                String logMessage;

                // NOTE: Check if initialization failed because the device is
                // not supported. At this point the user should be informed
                // with a message.
                if (mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED)
                {
                    logMessage = "Failed to initialize QCAR because this " +
                        "device is not supported.";
                }
                else
                {
                    logMessage = "Failed to initialize QCAR.";
                }
                
                // Log error:
                DebugLog.LOGE("InitQCARTask::onPostExecute: " + logMessage +
                                " Exiting.");
                
                // Show dialog box with error message:
                dialogError.setMessage(logMessage);  
                dialogError.show();
            }
        }
    }
    
    
    /** An async task to load the tracker data asynchronously. */
    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
    {
        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap:
            synchronized (mShutdownLock)
            {
                // Load the tracker data set:
                return (loadTrackerData() > 0);                
            }
        }
        
        protected void onPostExecute(Boolean result)
        {
            DebugLog.LOGD("LoadTrackerTask::onPostExecute: execution " +
                        (result ? "successful" : "failed"));
            
            if (result)
            {
                // The stones and chips data set is now active:
                mIsStonesAndChipsDataSetActive = true;
                
                // Done loading the tracker, update application status: 
                updateApplicationStatus(APPSTATUS_INITED);
            }
            else
            {
                // Create dialog box for display error:
                AlertDialog dialogError = new AlertDialog.Builder(ARGameActivity.this).create();
                dialogError.setButton(
                	DialogInterface.BUTTON_POSITIVE,
                    "Close",
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // Exiting application
                            System.exit(1);
                        }
                    }
                ); 
                
                // Show dialog box with error message:
                dialogError.setMessage("Failed to load tracker data.");  
                dialogError.show();
            }
        }
    }

    private void storeScreenDimensions()
    {
        // Query display dimensions
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    
    /** Called when the activity first starts or the user navigates back
     * to an activity. */
    protected void onCreate(Bundle savedInstanceState)
    {
    	DebugLog.LOGD("ARGameActivity::onCreate");
    	super.onCreate(savedInstanceState);
//    	setContentView(R.layout.activity_ar);
    	
    	// Get the UUID we generated when this app was installed
    	deviceUUID = Installation.id(this);

    	//the whole screen becomes sensitive to touch
//    	View gameContainer = (View) findViewById(R.id.game_container);
//    	gameContainer.setOnTouchListener(this);

        //Bind to the networking service
    	doBindNetworkingService();
    	
        // Set the splash screen image to display during initialization:
    	mSplashScreenImageResource = R.drawable.splash;

    	// Load any sample specific textures:  
    	mTextures = new Vector<Texture>();
    	loadTextures();

    	// Query the QCAR initialization flags:
    	mQCARFlags = getInitializationFlags();

    	// Update the application status to start initializing application
    	updateApplicationStatus(APPSTATUS_INIT_APP);
    }

    
    /** We want to load specific textures from the APK, which we will later
    use for rendering. */
    private void loadTextures()
    {
    	// UPDATE:: We added these textures for the demo.
    	mTextures.add(Texture.loadTextureFromApk("tower_shell.png", getAssets()));
    	mTextures.add(Texture.loadTextureFromApk("tower_top.png", getAssets()));
    	mTextures.add(Texture.loadTextureFromApk("banana180.jpg", getAssets())); 
    }
    
    
    /** Configure QCAR with the desired version of OpenGL ES. */
    private int getInitializationFlags()
    {
        int flags = 0;
        
        // Query the native code:
        if (getOpenGlEsVersionNative() == 1)
        {
            flags = QCAR.GL_11;
        }
        else
        {
            flags = QCAR.GL_20;
        }
        
        return flags;
    }    
    
    
    /** native method for querying the OpenGL ES version.
     * Returns 1 for OpenGl ES 1.1, returns 2 for OpenGl ES 2.0. */
    public native int getOpenGlEsVersionNative();
    
    /** Native tracker initialization and deinitialization. */
    public native int initTracker();
    public native void deinitTracker();

    /** Native functions to load and destroy tracking data. */
    public native int loadTrackerData();
    public native void destroyTrackerData();    
    
    /** Native sample initialization. */
    public native void onQCARInitializedNative();    
        
    /** Native methods for starting and stoping the camera. */ 
    private native void startCamera();
    private native void stopCamera();
    
    /** Native method for setting / updating the projection matrix for AR content rendering */
    private native void setProjectionMatrix();
    
//    /** Native method for getting phone location */
//    private native float[] getCameraLocationNative();
//    
//    private JSONObject makePositionJSON(float[] nativeArray) throws JSONException
//    {	
//    	JSONObject position = new JSONObject();
//    	
//    	position.put("x", "" + nativeArray[0]);
//    	position.put("y", "" + nativeArray[1]);
//    	position.put("z", "" + nativeArray[2]);
//    	
//    	return position; 	
//    }
//    
//    private JSONObject makeRotationJSON(float[] nativeArray) throws JSONException
//    {	
//    	JSONObject rotation = new JSONObject();
//    	
//    	rotation.put("x", "" + nativeArray[4]);
//    	rotation.put("y", "" + nativeArray[5]);
//    	rotation.put("z", "" + nativeArray[6]);
//    	
//    	return rotation; 	
//    }

   /** Called when the activity will start interacting with the user.*/
    protected void onResume()
    {
        DebugLog.LOGD("ARGameActivity::onResume");
        super.onResume();
        
        // QCAR-specific resume operation
        QCAR.onResume();
        
        // We may start the camera only if the QCAR SDK has already been 
        // initialized
        if (mAppStatus == APPSTATUS_CAMERA_STOPPED)
        {
            updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
            
            // Reactivate flash if it was active before pausing the app
            if (mFlash)
            {
                boolean result = activateFlash(mFlash);
                DebugLog.LOGI("Turning flash "+(mFlash?"ON":"OFF")+" "+(result?"WORKED":"FAILED")+"!!");
            }
        }
        
        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }        
    }
    
    
    public void onConfigurationChanged(Configuration config)
    {
        DebugLog.LOGD("ARGameActivity::onConfigurationChanged");
        super.onConfigurationChanged(config);
        
        storeScreenDimensions();
        
        // Set projection matrix:
        if (QCAR.isInitialized())
            setProjectionMatrix();
    }
    
    /**
     * Called every time the gameState is updated with the remote game state.
     * This method should call other methods and contain very little logic of 
     * its own. 
     */
    private void onGameStateChange(String stateStr) {
    	
//    	pushDeviceState(obtainDeviceState());
    	
    	try {
    		JSONObject gameState = new JSONObject(stateStr);

    		//Pull out official namespaces
    		JSONObject engineState = gameState; // TODO: delete 
    		JSONObject webState = gameState; // TODO: delete 
    		// TODO uncomment for correct namespacing:
    		//JSONObject engineState = new JSONObject(stateStr).get(GAME_ENGINE_NAMESPACE);
    		//JSONObject webState = new JSONObject(stateStr).get(WEB_NAMESPACE);

    		// TODO: delete. just supports old turret test:
    		this.gameState = gameState; 
    		// TODO remove this with turret test stuff
    		DebugLog.LOGD(this.gameState.getJSONObject("turret").get("position").toString());
    	} catch(JSONException e) {
    		//shit!
    		e.printStackTrace();
    	}
    }
    

    /** Called when the system is about to start resuming a previous activity.*/
    protected void onPause()
    {
        DebugLog.LOGD("ARGameActivity::onPause");
        super.onPause();
        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
        
        if (mAppStatus == APPSTATUS_CAMERA_RUNNING)
        {
            updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
        }
        
        // QCAR-specific pause operation
        QCAR.onPause();
    }
    
    
    /** Native function to deinitialize the application.*/
    private native void deinitApplicationNative();

    
    /** The final call you receive before your activity is destroyed.*/
    protected void onDestroy()
    {
        DebugLog.LOGD("ARGameActivity::onDestroy");
        super.onDestroy();
        
        doUnbindNetworkingService();
        
        // Dismiss the splash screen time out handler:
        if (mSplashScreenHandler != null)
        {
            mSplashScreenHandler.removeCallbacks(mSplashScreenRunnable);
            mSplashScreenRunnable = null;
            mSplashScreenHandler = null;
        }        
        
        // Cancel potentially running tasks
        if (mInitQCARTask != null &&
            mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED)
        {
            mInitQCARTask.cancel(true);
            mInitQCARTask = null;
        }
        
        if (mLoadTrackerTask != null &&
            mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
        {
            mLoadTrackerTask.cancel(true);
            mLoadTrackerTask = null;
        }
        
        // Ensure that all asynchronous operations to initialize QCAR and loading
        // the tracker datasets do not overlap:
        synchronized (mShutdownLock) {
            
            // Do application deinitialization in native code
            deinitApplicationNative();
            
            // Unload texture
            mTextures.clear();
            mTextures = null;
            
            // Destroy the tracking data set:
            destroyTrackerData();
            
            // Deinit the tracker:
            deinitTracker();
            
            // Deinitialize QCAR SDK
            QCAR.deinit();   
        }
        
        System.gc();
    }

    
    /** NOTE: this method is synchronized because of a potential concurrent
     * access by ARGameActivity::onResume() and InitQCARTask::onPostExecute(). */
    private synchronized void updateApplicationStatus(int appStatus)
    {
        // Exit if there is no change in status
        if (mAppStatus == appStatus)
            return;

        // Store new status value      
        mAppStatus = appStatus;

        // Execute application state-specific actions
        switch (mAppStatus)
        {
            case APPSTATUS_INIT_APP:
                // Initialize application elements that do not rely on QCAR
                // initialization  
                initApplication();
                
                // Proceed to next application initialization status
                updateApplicationStatus(APPSTATUS_INIT_QCAR);
                break;

            case APPSTATUS_INIT_QCAR:
                // Initialize QCAR SDK asynchronously to avoid blocking the
                // main (UI) thread.
                // This task instance must be created and invoked on the UI
                // thread and it can be executed only once!
                try
                {
                    mInitQCARTask = new InitQCARTask();
                    mInitQCARTask.execute();
                }
                catch (Exception e)
                {
                    DebugLog.LOGE("Initializing QCAR SDK failed");
                }
                break;
                
            case APPSTATUS_INIT_TRACKER:
                // Initialize the ImageTracker
                if (initTracker() > 0)
                {
                    // Proceed to next application initialization status
                    updateApplicationStatus(APPSTATUS_INIT_APP_AR);     
                }
                break;
                
            case APPSTATUS_INIT_APP_AR:
                // Initialize Augmented Reality-specific application elements
                // that may rely on the fact that the QCAR SDK has been
                // already initialized
                initApplicationAR();
                
                // Proceed to next application initialization status
                updateApplicationStatus(APPSTATUS_LOAD_TRACKER);
                break;
                
            case APPSTATUS_LOAD_TRACKER:
                // Load the tracking data set
                //
                // This task instance must be created and invoked on the UI
                // thread and it can be executed only once!
                try
                {
                    mLoadTrackerTask = new LoadTrackerTask();
                    mLoadTrackerTask.execute();
                }
                catch (Exception e)
                {
                    DebugLog.LOGE("Loading tracking data set failed");
                }
                break;
                
            case APPSTATUS_INITED:
                // Hint to the virtual machine that it would be a good time to
                // run the garbage collector.
                //
                // NOTE: This is only a hint. There is no guarantee that the
                // garbage collector will actually be run.
                System.gc();

                // Native post initialization:
                onQCARInitializedNative();
                
                // The elapsed time since the splash screen was visible:
                long splashScreenTime = System.currentTimeMillis() - 
                                            mSplashScreenStartTime;
                long newSplashScreenTime = 0;
                if (splashScreenTime < MIN_SPLASH_SCREEN_TIME)
                {
                    newSplashScreenTime = MIN_SPLASH_SCREEN_TIME -
                                            splashScreenTime;   
                }
                
                // Request a callback function after a given timeout to dismiss
                // the splash screen:
                mSplashScreenHandler = new Handler();
                mSplashScreenRunnable =
                    new Runnable() {
                        public void run()
                        {
                            // Hide the splash screen
                            mSplashScreenView.setVisibility(View.INVISIBLE);
                            
                            // Activate the renderer
                            mRenderer.mIsActive = true;
    
                            // Now add the GL surface view. It is important
                            // that the OpenGL ES surface view gets added
                            // BEFORE the camera is started and video
                            // background is configured.
                            addContentView(mGlView, new LayoutParams(
                                            LayoutParams.MATCH_PARENT,
                                            LayoutParams.MATCH_PARENT));
                            
                            // Start the camera:
                            updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
                        }
                };

                mSplashScreenHandler.postDelayed(mSplashScreenRunnable,
                                                    newSplashScreenTime);
                break;
                
            case APPSTATUS_CAMERA_STOPPED:
                // Call the native function to stop the camera
                stopCamera();
                break;
                
            case APPSTATUS_CAMERA_RUNNING:
                // Call the native function to start the camera
                startCamera();
                setProjectionMatrix();
                break;
                
            default:
                throw new RuntimeException("Invalid application state");
        }
    }
    
    
    /** Tells native code whether we are in portait or landscape mode */
    private native void setActivityPortraitMode(boolean isPortrait);
    
    
    /** Initialize application GUI elements that are not related to AR. */
    private void initApplication()
    {
        // Set the screen orientation
        //
        // NOTE: It is recommended to set this because of the following reasons:
        //
        //    1.) Before Android 2.2 there is no reliable way to query the
        //        absolute screen orientation from an activity, therefore using 
        //        an undefined orientation is not recommended. Screen 
        //        orientation matching orientation sensor measurements is also
        //        not recommended as every screen orientation change triggers
        //        deinitialization / (re)initialization steps in internal QCAR 
        //        SDK components resulting in unnecessary overhead during 
        //        application run-time.
        //
        //    2.) Android camera drivers seem to always deliver landscape images
        //        thus QCAR SDK components (e.g. camera capturing) need to know 
        //        when we are in portrait mode. Before Android 2.2 there is no 
        //        standard, device-independent way to let the camera driver know 
        //        that we are in portrait mode as each device seems to require a
        //        different combination of settings to rotate camera preview 
        //        frames images to match portrait mode views. Because of this,
        //        we suggest that the activity using the QCAR SDK be locked
        //        to landscape mode if you plan to support Android 2.1 devices
        //        as well. Froyo is fine with both orientations.
        int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        
        // Apply screen orientation
        setRequestedOrientation(screenOrientation);
        
        // Pass on screen orientation info to native code
        setActivityPortraitMode(screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        storeScreenDimensions();
        
        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright.
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
              
        // Create and add the splash screen view
        mSplashScreenView = new ImageView(this);
        mSplashScreenView.setImageResource(mSplashScreenImageResource);
        addContentView(mSplashScreenView, new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        mSplashScreenStartTime = System.currentTimeMillis();

    }
    
    
    /** Native function to initialize the application. */
    private native void initApplicationNative(int width, int height);


    /** Initializes AR application components. */
    private void initApplicationAR()
    {        
        // Do application initialization in native code (e.g. registering
        // callbacks, etc.)
        initApplicationNative(mScreenWidth, mScreenHeight);

        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = QCAR.requiresAlpha();
        
        mGlView = new QCARSampleGLView(this);
        mGlView.init(mQCARFlags, translucent, depthSize, stencilSize);
        
        mRenderer = new ARGameRenderer();
        mGlView.setRenderer(mRenderer);
 
    }

    /** Invoked the first time when the options menu is displayed to give
     *  the Activity a chance to populate its Menu with menu items. */
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
                        
        mDataSetMenuItem = menu.add("Switch to Vichar dataset");
        menu.add("Toggle flash");
        menu.add("Trigger autofocus");
        
        SubMenu focusModes = menu.addSubMenu("Focus Modes");
        focusModes.add("Normal").setCheckable(true);
        focusModes.add("Continuous Autofocus").setCheckable(true);
        focusModes.add("Infinity").setCheckable(true);
        focusModes.add("Macro Mode").setCheckable(true);
        
        return true;
    }
    
    
    /** Tells native code to switch dataset as soon as possible*/
    private native void switchDatasetAsap();
    
    
    /** Invoked when the user selects an item from the Menu */
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item == mDataSetMenuItem)
        {
           switchDatasetAsap();
           mIsStonesAndChipsDataSetActive = !mIsStonesAndChipsDataSetActive;
           if (mIsStonesAndChipsDataSetActive)
           {
               item.setTitle("Switch to FlakesBox dataset");
           }
           else
           {
               item.setTitle("Switch to Vichar dataset");
           }
            
        }
        else if(item.getTitle().equals("Toggle flash"))
        {
            mFlash = !mFlash;
            boolean result = activateFlash(mFlash);
            DebugLog.LOGI("Turning flash "+(mFlash?"ON":"OFF")+" "+(result?"WORKED":"FAILED")+"!!");
        }
        else if(item.getTitle().equals("Trigger autofocus"))
        {
            boolean result = autofocus();
            DebugLog.LOGI("Autofocus requested"+(result?" successfully.":".  Not supported in current mode or on this device."));
        }
        else 
        {
            int arg = -1;
            if(item.getTitle().equals("Normal"))
                arg = 0;
            if(item.getTitle().equals("Continuous Autofocus"))
                arg = 1;
            if(item.getTitle().equals("Infinity"))
                arg = 2;
            if(item.getTitle().equals("Macro Mode"))
                arg = 3;
            
            if(arg != -1)
            {
                boolean result = setFocusMode(arg);
                if (result)
                {
                    item.setChecked(true);
                    if(checked != null && item != checked)
                        checked.setChecked(false);
                    checked = item;
                }
                
                DebugLog.LOGI("Requested Focus mode "+item.getTitle()+(result?" successfully.":".  Not supported on this device."));
            }
        }
        
        return true;
    }
    
    private MenuItem checked;
    private boolean mFlash = false;
    private native boolean activateFlash(boolean flash);
    private native boolean autofocus();
    private native boolean setFocusMode(int mode);
    
    /** Returns the number of registered textures. */
    public int getTextureCount()
    {
        return mTextures.size();
    }
  
    /** Returns the texture object at the specified index. */
    public Texture getTexture(int i)
    {
        return mTextures.elementAt(i);
    }
   
    /** A helper for loading native libraries stored in "libs/armeabi*". */
    public static boolean loadLibrary(String nLibName)
    {
        try
        {
            System.loadLibrary(nLibName);
            DebugLog.LOGI("Native library lib" + nLibName + ".so loaded");
            return true;
        }
        catch (UnsatisfiedLinkError ulee)
        {
            DebugLog.LOGE("The library lib" + nLibName +
                            ".so could not be loaded");
        }
        catch (SecurityException se)
        {
            DebugLog.LOGE("The library lib" + nLibName +
                            ".so was not allowed to be loaded");
        }
        
        return false;
    }
    
    /**
     * Capture touch events
     * @param v
     * @param event
     * @return
     */
    public boolean onTouch(View v, MotionEvent ev)
    {
        // TODO check other views above game view
        //if(v.equals(this.gameView)) {
        	//return true; // true indicates event is consumed
        //}

    	float dx = 0f;
    	float dy = 0f;

    	if(ev.getAction() == MotionEvent.ACTION_MOVE) {
    		dx = ev.getX() - touchX;
    		dy = ev.getY() - touchY;
    	}
    	if(ev.getAction() == MotionEvent.ACTION_DOWN 
    			|| ev.getAction() == MotionEvent.ACTION_MOVE) {
    		// Remember new touch coors
    		touchX = ev.getX();
    		touchY = ev.getY();
    	} else if(ev.getAction() == MotionEvent.ACTION_UP) {
    		// reset values
    		touchX = 0f;
    		touchY = 0f;
    		dx = 0f;
    		dy = 0f;
    	}

    	// Touch propagated to gameView.
    	float x = ev.getX();
    	float y = ev.getY();

    	try {
    		JSONObject turret = gameState.getJSONObject("turret");
    		String position = turret.getString("position");
    		//Log.d(this.toString(),position);
    		String[] coors = position.split("\\s*,\\s*");

    		// Calc change with floats
    		//		    	float turretX = Float.valueOf(coors[0].trim()).floatValue();
    		//		    	float turretZ = Float.valueOf(coors[2].trim()).floatValue();
    		//		    	coors[0] = Float.toString(turretX + dx);
    		//		    	coors[2] =  Float.toString(turretZ + dy);

    		// Calc change with ints
    		int turretX = Math.round(Float.valueOf(coors[0].trim()).floatValue());
    		int turretZ = Math.round(Float.valueOf(coors[2].trim()).floatValue());
    		coors[0] = Integer.toString(turretX + Math.round(dx));
    		coors[1] = Integer.toString(Math.round(Float.valueOf(coors[1].trim()).floatValue())); // for good measure
    		coors[2] = Integer.toString(turretZ + Math.round(dy));

    		for(int i = 0; i < coors.length; i++) {
    			if(i == 0) position = coors[i];
    			else position += "," + coors[i];
    		}

    		turret.put("position", position);
    		pushDeviceState(obtainDeviceState().put("turret", turret));
    	} catch (JSONException e) {
    		//something
    		Log.i(this.toString(),"JSONException");
    	}
    	return true; //Must return true to get move events
    }
    
    /**
     * Returns a blank device state JSONObject
     * Use this method to get a reference to a device state that you can modify
     * and push.
     * 
     * This just makes it extra clear that you are passing snapshots of info
     * to the server. Do not try to get info about the device from a local copy
     * of the device state.
     * 
     * @return
     */
    private JSONObject obtainDeviceState() {
    	return new JSONObject();
    }
    
    /**
     * Wraps a device state snapshot in the deviceUUID and pushes it to the 
     * server.
     * @param deviceState
     */
    private void pushDeviceState(JSONObject deviceState) {
    	try {
    		
//    		float[] cameraLoc = getCameraLocationNative();
//    		deviceState.put("position", makePositionJSON(cameraLoc));
//    		deviceState.put("rotation", makeRotationJSON(cameraLoc));
    		
    		JSONObject sendState = new JSONObject().put(deviceUUID, deviceState);
    		
    		// TODO: delete for proper namespacing:
    		sendState = deviceState; 
    		
    		if(isBoundToNetworkingService) {
        		Bundle b = new Bundle();
        		b.putString("" + NetworkingService.MSG_QUEUE_OUTBOUND_J_STRING, 
        				sendState.toString());
        		Message msg = Message.obtain(null, 
        				NetworkingService.MSG_QUEUE_OUTBOUND_J_STRING);
        		msg.setData(b);
        		try {
        			networkingServiceMessenger.send(msg);
        		} catch (RemoteException e) {
                    //TODO handle RemoteException
        			Log.i(this.toString(), "updateRemoteGameState: RemoteException");
                }
        	} else {
        		Log.i(this.toString(),"Not Bound to NetworkingService");
        	}
    	} catch(JSONException e) {
    		// This really shouldn't happen...right?
    		Log.i(this.toString(), "pushDeviceState: JSONException");
    	}
    }
    
    /**
     * Handles incoming messages from NetworkingService
     * @author DuBious
     *
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
	            case NetworkingService.MSG_SET_JSON_STRING_VALUE:
	            	String str = msg.getData().getString("" + NetworkingService.MSG_SET_JSON_STRING_VALUE);
	            	onGameStateChange(str);
	            	break;
	            default:
	                super.handleMessage(msg);
            }
        }
    }
    
    /**
     * Forms the connection between this Activity and the NetworkingService
     */
    private ServiceConnection networkingServiceConnection = new ServiceConnection() 
    { //need this to create the connection to the service
        /**
         * Called when the service is started and bound to this Service Connection
         * @param className The classname of the activity
         * @param service the service's binder object
         */
    	public void onServiceConnected(
        				ComponentName className, 
        				IBinder binder)
        {
    		Log.d(this.toString(),"Start onServiceConnected");
        	// Create a Messenger that references the service
        	networkingServiceMessenger = new Messenger(binder);
        	Log.d(this.toString(),"networkingServiceMessenger: " + networkingServiceMessenger.toString());
        	
        	isBoundToNetworkingService = true;
	        Log.d(this.toString(),"Bound to HttpSevice");
	        
	        try {
		        Message msg = Message.obtain(null, NetworkingService.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            networkingServiceMessenger.send(msg);
	        } catch (RemoteException e) {
                //TODO handle RemoteException
	        	// The service crashed before we could do anything with it
	        }
        }

    	/**
    	 * Called when the service stops or unbinds itself
    	 * @param className The Classname of the activity
    	 */
        public void onServiceDisconnected(ComponentName className) 
        {
        	isBoundToNetworkingService = false;
        }
    };
    
    /**
     * Binds this activity to the NetworkingService
     */
    private void doBindNetworkingService() {
    	Log.d(this.toString(),"Binding HttpSevice");
        bindService(new Intent(ARGameActivity.this, NetworkingService.class), 
        		networkingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbinds this activity from the NetworkingService
     */
    private void doUnbindNetworkingService() {
    	// Unregister Messenger
    	if (networkingServiceMessenger != null) {
            try {
                Message msg = Message.obtain(null, NetworkingService.MSG_UNREGISTER_CLIENT);
                msg.replyTo = mMessenger;
                networkingServiceMessenger.send(msg);
                networkingServiceMessenger = null;
            } catch (RemoteException e) {
                // There is nothing special we need to do if the service has crashed.
            }
        }
    	// Detach our existing connection.
    	
        unbindService(networkingServiceConnection);
    }
}


