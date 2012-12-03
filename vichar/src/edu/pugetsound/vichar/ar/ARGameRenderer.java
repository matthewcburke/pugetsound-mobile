/*==============================================================================
            Copyright (c) 2012 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    ImageTargetsRenderer.java

@brief
    Sample for ImageTargets

==============================================================================*/


package edu.pugetsound.vichar.ar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;

import com.qualcomm.QCAR.QCAR;


/** The renderer class for the ImageTargets sample. */
public class ARGameRenderer implements GLSurfaceView.Renderer
{
	/*
	 * format of the jfloatArray:
	 * 	array of floats[type, posX, posY, posZ, rotX, rotY, rotZ], numobjects (each object is 7 long),
	 * 	turret = 1
	 * 	turret bullets = 2
	 * 	fireballs = 3
	 * 	minions = 4
	 * 	batteries = 5
	 *	player = 6
	 *	eyeballs = 7
	 *	platforms = 8
	 */
	public float[] myTest = {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,   	// turret at the origin
							 1.0f, 50.0f, 50.0f, 0.0f, 0.0f, 0.0f, 0.0f, 	// turret offset 50 in the x and y directions
							 1.0f, -50.0f, 50.0f, 0.0f, 0.0f, 0.0f, 0.0f};  // turret offset -50 x and 50 y
	public static boolean updated = true;
    public boolean mIsActive = false;
    
    /** Native function for initializing the renderer. */
    public native void initRendering();
    
    
    /** Native function to update the renderer. */
    public native void updateRendering(int width, int height);

    
    /** Called when the surface is created or recreated. */
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        DebugLog.LOGD("GLRenderer::onSurfaceCreated");

        // Call native function to initialize rendering:
        initRendering();
        
        // Call QCAR function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        QCAR.onSurfaceCreated();
    }
    
    
    /** Called when the surface changed size. */
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        DebugLog.LOGD("GLRenderer::onSurfaceChanged");
        
        // Call native function to update rendering when render surface parameters have changed:
        updateRendering(width, height);

        // Call QCAR function to handle render surface size changes:
        QCAR.onSurfaceChanged(width, height);
    }    
    
    
    /** The native render function. 
     *  
     *  update = true if the array has been updated by the JSON object
     *  test = an array of location and rotation information: see format in above definition.
     * 
     * */    
    public native void renderFrame(boolean update, float[] test, int objSize);
    
    
    /** Called to draw the current frame. */
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        // Call our native function to render content
        renderFrame(updated, myTest, ARGameActivity.OBJ_SIZE);
//        updated = false;
    }
}
