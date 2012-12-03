/*==============================================================================
            Copyright (c) 2012 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    ImageTargets.cpp

@brief
    Sample for ImageTargets

==============================================================================*/
/*
 * Modified by the augmented reality team of CSCI 240, University of Puget Sound
 *
 * @version 2012.10.25
 *
 * @authors Matt Burke, Thomas Freeman, Erin Jamroz, David Greene, Selah-Mae Ross
 *
 * Most modifications are accompanied by a comment including the text: 'UPDATE::'
 *
 * */


#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <sys/time.h> // UPDATE:: to be used in animation

#ifdef USE_OPENGL_ES_1_1
#include <GLES/gl.h>
#include <GLES/glext.h>
#else
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

#include <QCAR/QCAR.h>
#include <QCAR/CameraDevice.h>
#include <QCAR/Renderer.h>
#include <QCAR/VideoBackgroundConfig.h>
#include <QCAR/Trackable.h>
#include <QCAR/Tool.h>
#include <QCAR/Tracker.h>
#include <QCAR/TrackerManager.h>
#include <QCAR/ImageTracker.h>
#include <QCAR/CameraCalibration.h>
#include <QCAR/UpdateCallback.h>
#include <QCAR/DataSet.h>

#include "SampleUtils.h"
#include "Texture.h"
#include "CubeShaders.h"
#include "Teapot.h"
 #include "SampleMath.h"    //To get phones location

// UPDATE:: Our models to be displayed
// TODO: Should we put all of these .h files into one gameObjects.h file?
#include "banana.h"
#include "tower_top.h"
#include "tower_shell.h"

#ifdef __cplusplus
extern "C"
{
#endif

// Textures:
int textureCount                = 0;
static const int tower_shellIndex		= 0;
static const int tower_topIndex	= 1;
static const int banana180Index			= 2;
Texture** textures              = 0;

// OpenGL ES 2.0 specific:
#ifdef USE_OPENGL_ES_2_0
unsigned int shaderProgramID    = 0;
GLint vertexHandle              = 0;
GLint normalHandle              = 0;
GLint textureCoordHandle        = 0;
GLint mvpMatrixHandle           = 0;
#endif

// UPDATE:: added this variable to assist animating rotations for demo.
float turAng = 0.0;
float phoneLoc[6] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

// Screen dimensions:
unsigned int screenWidth        = 0;
unsigned int screenHeight       = 0;

// Indicates whether screen is in portrait (true) or landscape (false) mode
bool isActivityInPortraitMode   = false;

// The projection matrix used for rendering virtual objects:
QCAR::Matrix44F projectionMatrix;

// Constants:
static const float kObjectScale = 120.f; // UPDATE:: increased the scale to properly display our models. It was 3 for the teapots.

QCAR::DataSet* dataSetStonesAndChips    = 0;
QCAR::DataSet* dataSetFlakesBox = 0;

bool switchDataSetAsap          = false;

// Object to receive update callbacks from QCAR SDK
class ImageTargets_UpdateCallback : public QCAR::UpdateCallback
{   
    virtual void QCAR_onUpdate(QCAR::State& /*state*/)
    {
        if (switchDataSetAsap)
        {
            switchDataSetAsap = false;

            // Get the image tracker:
            QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
            QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
                trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
            if (imageTracker == 0 || dataSetStonesAndChips == 0 || dataSetFlakesBox == 0 ||
                imageTracker->getActiveDataSet() == 0)
            {
                LOG("Failed to switch data set.");
                return;
            }
            
            if (imageTracker->getActiveDataSet() == dataSetStonesAndChips)
            {
                imageTracker->deactivateDataSet(dataSetStonesAndChips);
                imageTracker->activateDataSet(dataSetFlakesBox);
            }
            else
            {
                imageTracker->deactivateDataSet(dataSetFlakesBox);
                imageTracker->activateDataSet(dataSetStonesAndChips);
            }
        }
    }
};

ImageTargets_UpdateCallback updateCallback;

JNIEXPORT int JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_getOpenGlEsVersionNative(JNIEnv *, jobject)
{
#ifdef USE_OPENGL_ES_1_1        
    return 1;
#else
    return 2;
#endif
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_setActivityPortraitMode(JNIEnv *, jobject, jboolean isPortrait)
{
    isActivityInPortraitMode = isPortrait;
}



JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_switchDatasetAsap(JNIEnv *, jobject)
{
    switchDataSetAsap = true;
}


JNIEXPORT int JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_initTracker(JNIEnv *, jobject)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_initTracker");
    
    // Initialize the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::Tracker* tracker = trackerManager.initTracker(QCAR::Tracker::IMAGE_TRACKER);
    if (tracker == NULL)
    {
        LOG("Failed to initialize ImageTracker.");
        return 0;
    }

    LOG("Successfully initialized ImageTracker.");
    return 1;
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_deinitTracker(JNIEnv *, jobject)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_deinitTracker");

    // Deinit the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    trackerManager.deinitTracker(QCAR::Tracker::IMAGE_TRACKER);
}


JNIEXPORT int JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_loadTrackerData(JNIEnv *, jobject)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_loadTrackerData");
    
    // Get the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
                    trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
    if (imageTracker == NULL)
    {
        LOG("Failed to load tracking data set because the ImageTracker has not"
            " been initialized.");
        return 0;
    }

    // Create the data sets:
    dataSetStonesAndChips = imageTracker->createDataSet();
    if (dataSetStonesAndChips == 0)
    {
        LOG("Failed to create a new tracking data.");
        return 0;
    }

    dataSetFlakesBox = imageTracker->createDataSet();
    if (dataSetFlakesBox == 0)
    {
        LOG("Failed to create a new tracking data.");
        return 0;
    }

    // Load the data sets:
    if (!dataSetStonesAndChips->load("StonesAndChips.xml", QCAR::DataSet::STORAGE_APPRESOURCE))
    {
        LOG("Failed to load data set.");
        return 0;
    }

    if (!dataSetFlakesBox->load("FlakesBox.xml", QCAR::DataSet::STORAGE_APPRESOURCE))
    {
        LOG("Failed to load data set.");
        return 0;
    }

    // Activate the data set:
    if (!imageTracker->activateDataSet(dataSetStonesAndChips))
    {
        LOG("Failed to activate data set.");
        return 0;
    }

    LOG("Successfully loaded and activated data set.");
    return 1;
}


JNIEXPORT int JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_destroyTrackerData(JNIEnv *, jobject)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_destroyTrackerData");

    // Get the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
        trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
    if (imageTracker == NULL)
    {
        LOG("Failed to destroy the tracking data set because the ImageTracker has not"
            " been initialized.");
        return 0;
    }
    
    if (dataSetStonesAndChips != 0)
    {
        if (imageTracker->getActiveDataSet() == dataSetStonesAndChips &&
            !imageTracker->deactivateDataSet(dataSetStonesAndChips))
        {
            LOG("Failed to destroy the tracking data set StonesAndChips because the data set "
                "could not be deactivated.");
            return 0;
        }

        if (!imageTracker->destroyDataSet(dataSetStonesAndChips))
        {
            LOG("Failed to destroy the tracking data set stonesAndChips.");
            return 0;
        }

        LOG("Successfully destroyed the data set stonesAndChips.");
        dataSetStonesAndChips = 0;
    }

    if (dataSetFlakesBox != 0)
    {
        if (imageTracker->getActiveDataSet() == dataSetFlakesBox &&
            !imageTracker->deactivateDataSet(dataSetFlakesBox))
        {
            LOG("Failed to destroy the tracking data set FlakesBox because the data set "
                "could not be deactivated.");
            return 0;
        }

        if (!imageTracker->destroyDataSet(dataSetFlakesBox))
        {
            LOG("Failed to destroy the tracking data set FlakesBox.");
            return 0;
        }

        LOG("Successfully destroyed the data set FlakesBox.");
        dataSetFlakesBox = 0;
    }

    return 1;
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_onQCARInitializedNative(JNIEnv *, jobject)
{
    // Register the update callback where we handle the data set swap:
    QCAR::registerCallback(&updateCallback);

    // Comment in to enable tracking of up to 2 targets simultaneously and
    // split the work over multiple frames:
    QCAR::setHint(QCAR::HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 2);
    QCAR::setHint(QCAR::HINT_IMAGE_TARGET_MULTI_FRAME_ENABLED, 1);
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameRenderer_renderFrame(JNIEnv *, jobject)
{
    //LOG("Java_edu_pugetsound_vichar_ar_GLRenderer_renderFrame");

    // Clear color and depth buffer 
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Get the state from QCAR and mark the beginning of a rendering section
    QCAR::State state = QCAR::Renderer::getInstance().begin();
    
    // Explicitly render the Video Background
    QCAR::Renderer::getInstance().drawVideoBackground();
       
#ifdef USE_OPENGL_ES_1_1
    // Set GL11 flags:
    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_NORMAL_ARRAY);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);

    glEnable(GL_TEXTURE_2D);
    glDisable(GL_LIGHTING);
        
#endif

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    // Did we find any trackables this frame?
    int tIdx = 0;
    if (state.getNumActiveTrackables() > 0)
    {
        // Get the trackable:
        const QCAR::Trackable* trackable = state.getActiveTrackable(tIdx);
        QCAR::Matrix44F modelViewMatrix =
            QCAR::Tool::convertPose2GLMatrix(trackable->getPose());

        //Begin additions by Erin================================================================================
        if (state.getNumActiveTrackables() > 1) {
        	const QCAR::Trackable* trackable2 = state.getActiveTrackable(tIdx+1);

        	//Get pos of target1 (center of image) and target2 (tower target)
        	QCAR::Matrix34F target1 = trackable->getPose();
        	QCAR::Matrix34F target2 = trackable2->getPose();

        	//Get phone's position of target1 (center image) and new target2 (tower)
        	QCAR::Matrix34F posObject1 = SampleMath::phoneCoorMatrix(&target1);
        	QCAR::Matrix34F posObject2 = SampleMath::calcSecondPos(&target1, &target2);

        	//Get position of object2 relative to object 1
        	QCAR::Matrix34F posRelative = SampleMath::vectorAdd(&posObject1, &posObject2);

        	LOG("==========================");
        	LOG("New target position: (%f,%f,%f)", posRelative.data[3], posRelative.data[7], posRelative.data[11]);
        	LOG("==========================");
        }

        QCAR::Matrix34F test;   //gets inverse pos matrix
        QCAR::Matrix34F pos;   //Gets positional data
        pos = trackable->getPose();

        //Get inverse
        QCAR::Matrix34F temp;
        test = SampleMath::phoneCoorMatrix(&temp);

        /*
        //Get phones distance
        float dist = SampleMath::getDistance(&pos);

        LOG("=========================================");
        LOG("%f", dist);
        LOG("=========================================");
		*/

        /*
        //Print results
//        LOG("Poisiton:");
//        LOG("%f %f %f %f",pos.data[0], pos.data[1], pos.data[2], pos.data[3]);
//        LOG("%f %f %f %f",pos.data[4], pos.data[5], pos.data[6], pos.data[7]);
//        LOG("%f %f %f %f",pos.data[8], pos.data[9], pos.data[10],pos.data[11]);
//        LOG("Inverse:");
//        LOG("%f %f %f %f",test.data[0], test.data[1], test.data[2], test.data[3]);
//        LOG("%f %f %f %f",test.data[4], test.data[5], test.data[6], test.data[7]);
//        LOG("%f %f %f %f",test.data[8], test.data[9], test.data[10], test.data[11]);
//        LOG("=========================");
        phoneLoc[0] = test.data[3];
        phoneLoc[1] = test.data[7];
        phoneLoc[2] = test.data[11];
        */
        //End============================================================================================


        // UPDATE:: Load the trackable position into a second modelViewMatrix to display second item.
        QCAR::Matrix44F modelViewMatrix2 =
                    QCAR::Tool::convertPose2GLMatrix(trackable->getPose());
        QCAR::Matrix44F modelViewMatrix3 =
                            QCAR::Tool::convertPose2GLMatrix(trackable->getPose());

        // Assign Textures according in the texture indices defined at the beginning of the file, and based
        // on the loadTextures() method in ARGameActivity.java.
        const Texture* const tower_shellTexture = textures[tower_shellIndex];
        const Texture* const tower_topTexture = textures[tower_topIndex];
        const Texture* const bananaTexture = textures[banana180Index];

#ifdef USE_OPENGL_ES_1_1
        // Load projection matrix:
        glMatrixMode(GL_PROJECTION);
        glLoadMatrixf(projectionMatrix.data);

        // Load model view matrix:
        glMatrixMode(GL_MODELVIEW);
        glLoadMatrixf(modelViewMatrix.data);
        glTranslatef(0.f, 0.f, kObjectScale);
        glScalef(kObjectScale, kObjectScale, kObjectScale);

        // Draw object:
        glBindTexture(GL_TEXTURE_2D, thisTexture->mTextureID);
        glTexCoordPointer(2, GL_FLOAT, 0, (const GLvoid*) &teapotTexCoords[0]);
        glVertexPointer(3, GL_FLOAT, 0, (const GLvoid*) &teapotVertices[0]);
        glNormalPointer(GL_FLOAT, 0,  (const GLvoid*) &teapotNormals[0]);
        glDrawElements(GL_TRIANGLES, NUM_TEAPOT_OBJECT_INDEX, GL_UNSIGNED_SHORT,
                       (const GLvoid*) &teapotIndices[0]);
#else


        //Draw the tower_top.
        QCAR::Matrix44F modelViewProjection;

        // Quick and dirty demonstration of animation. The tower_top turns to face the banana.
        if( turAng < 180.0)
        {
        	turAng = turAng + 1.0;
        }

        // UPDATE:: translate, rotate and scale the tower_top.
        SampleUtils::translatePoseMatrix(100.0f, 0.0f, kObjectScale,
                                    &modelViewMatrix.data[0]);
        // Animate the tower_top spinning 180 deg.
        SampleUtils::rotatePoseMatrix(turAng, 0.0f, 0.0f, 1.0f,
                        			    	&modelViewMatrix.data[0]);
        // So the tower_top appears upright
        SampleUtils::rotatePoseMatrix(90.0f, 1.0f, 0.0f, 0.0f,
                        			&modelViewMatrix.data[0]);
        SampleUtils::scalePoseMatrix(kObjectScale, kObjectScale, kObjectScale,
                                    &modelViewMatrix.data[0]);
        SampleUtils::multiplyMatrix(&projectionMatrix.data[0],
                                    &modelViewMatrix.data[0] ,
                                    &modelViewProjection.data[0]);

        glUseProgram(shaderProgramID);
         
        glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
                              (const GLvoid*) &tower_topVerts[0]);
        glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0,
                              (const GLvoid*) &tower_topNormals[0]);
        glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
                              (const GLvoid*) &tower_topTexCoords[0]);
        
        glEnableVertexAttribArray(vertexHandle);
        glEnableVertexAttribArray(normalHandle);
        glEnableVertexAttribArray(textureCoordHandle);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tower_topTexture->mTextureID);
        glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
                           (GLfloat*)&modelViewProjection.data[0] );
        glDrawArrays(GL_TRIANGLES, 0, tower_topNumVerts);

        SampleUtils::checkGlError("ImageTargets renderFrame");


        // UPDATE:: Draw a banana positioned on the other side of the target.
        // TO DO:: write a method so we aren't repeating the above code!
        QCAR::Matrix44F modelViewProjection2;

        SampleUtils::translatePoseMatrix(-100.0f, 0.0f, kObjectScale,
        		&modelViewMatrix2.data[0]);
        SampleUtils::rotatePoseMatrix( 90.0f, 1.0f, 0.0f, 0.0f,
        		&modelViewMatrix2.data[0]);
        SampleUtils::scalePoseMatrix(kObjectScale, kObjectScale, kObjectScale,
        		&modelViewMatrix2.data[0]);
        SampleUtils::multiplyMatrix(&projectionMatrix.data[0],
        		&modelViewMatrix2.data[0] ,
        		&modelViewProjection2.data[0]);

        glUseProgram(shaderProgramID);

        glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
        		(const GLvoid*) &bananaVerts[0]);
        glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0,
        		(const GLvoid*) &bananaNormals[0]);
        glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
        		(const GLvoid*) &bananaTexCoords[0]);

        glEnableVertexAttribArray(vertexHandle);
        glEnableVertexAttribArray(normalHandle);
        glEnableVertexAttribArray(textureCoordHandle);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, bananaTexture->mTextureID); // UPDATE:: apply a different texture.
        glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
        		(GLfloat*)&modelViewProjection2.data[0] );
        glDrawArrays(GL_TRIANGLES, 0, bananaNumVerts);

        SampleUtils::checkGlError("ImageTargets renderFrame");

        // draw third object
        QCAR::Matrix44F modelViewProjection3;

        SampleUtils::translatePoseMatrix(0.0f, 0.0f, kObjectScale,
        		&modelViewMatrix3.data[0]);
        SampleUtils::rotatePoseMatrix( 0.0f, 0.0f, 0.0f, 0.0f,
        		&modelViewMatrix3.data[0]);
        SampleUtils::scalePoseMatrix(kObjectScale, kObjectScale, kObjectScale,
        		&modelViewMatrix3.data[0]);
        SampleUtils::multiplyMatrix(&projectionMatrix.data[0],
        		&modelViewMatrix3.data[0] ,
        		&modelViewProjection3.data[0]);

        glUseProgram(shaderProgramID);

        glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
        		(const GLvoid*) &tower_shellVerts[0]);
        glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0,
        		(const GLvoid*) &tower_shellNormals[0]);
        glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
        		(const GLvoid*) &tower_shellTexCoords[0]);

        glEnableVertexAttribArray(vertexHandle);
        glEnableVertexAttribArray(normalHandle);
        glEnableVertexAttribArray(textureCoordHandle);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tower_shellTexture->mTextureID); // UPDATE:: apply a different texture.
        glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
        		(GLfloat*)&modelViewProjection3.data[0] );
        glDrawArrays(GL_TRIANGLES, 0, tower_shellNumVerts);

        SampleUtils::checkGlError("ImageTargets renderFrame");

#endif

    }

    glDisable(GL_DEPTH_TEST);

#ifdef USE_OPENGL_ES_1_1        
    glDisable(GL_TEXTURE_2D);
    glDisableClientState(GL_VERTEX_ARRAY);
    glDisableClientState(GL_NORMAL_ARRAY);
    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
#else
    glDisableVertexAttribArray(vertexHandle);
    glDisableVertexAttribArray(normalHandle);
    glDisableVertexAttribArray(textureCoordHandle);
#endif

    QCAR::Renderer::getInstance().end();
}

// TODO: write this function to return the camera location.
JNIEXPORT jfloatArray JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_getCameraLocation(JNIEnv * env, jobject)
{

	jfloatArray cameraLocation;
	cameraLocation = env->NewFloatArray(6);

	// Set an array full of zeros to test my use of the jni. Replace the values in coordArray with the
	// phone location and rotation.
	jfloat coordArray[6] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

	env->SetFloatArrayRegion(cameraLocation, 0, 6, phoneLoc);
	//delete state;
	return cameraLocation;
}

void
configureVideoBackground()
{
    // Get the default video mode:
    QCAR::CameraDevice& cameraDevice = QCAR::CameraDevice::getInstance();
    QCAR::VideoMode videoMode = cameraDevice.
                                getVideoMode(QCAR::CameraDevice::MODE_DEFAULT);


    // Configure the video background
    QCAR::VideoBackgroundConfig config;
    config.mEnabled = true;
    config.mSynchronous = true;
    config.mPosition.data[0] = 0.0f;
    config.mPosition.data[1] = 0.0f;
    
    if (isActivityInPortraitMode)
    {
        //LOG("configureVideoBackground PORTRAIT");
        config.mSize.data[0] = videoMode.mHeight
                                * (screenHeight / (float)videoMode.mWidth);
        config.mSize.data[1] = screenHeight;

        if(config.mSize.data[0] < screenWidth)
        {
            LOG("Correcting rendering background size to handle missmatch between screen and video aspect ratios.");
            config.mSize.data[0] = screenWidth;
            config.mSize.data[1] = screenWidth * 
                              (videoMode.mWidth / (float)videoMode.mHeight);
        }
    }
    else
    {
        //LOG("configureVideoBackground LANDSCAPE");
        config.mSize.data[0] = screenWidth;
        config.mSize.data[1] = videoMode.mHeight
                            * (screenWidth / (float)videoMode.mWidth);

        if(config.mSize.data[1] < screenHeight)
        {
            LOG("Correcting rendering background size to handle missmatch between screen and video aspect ratios.");
            config.mSize.data[0] = screenHeight
                                * (videoMode.mWidth / (float)videoMode.mHeight);
            config.mSize.data[1] = screenHeight;
        }
    }

    LOG("Configure Video Background : Video (%d,%d), Screen (%d,%d), mSize (%d,%d)", videoMode.mWidth, videoMode.mHeight, screenWidth, screenHeight, config.mSize.data[0], config.mSize.data[1]);

    // Set the config:
    QCAR::Renderer::getInstance().setVideoBackgroundConfig(config);
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_initApplicationNative(
                            JNIEnv* env, jobject obj, jint width, jint height)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_initApplicationNative");
    
    // Store screen dimensions
    screenWidth = width;
    screenHeight = height;
        
    // Handle to the activity class:
    jclass activityClass = env->GetObjectClass(obj);

    jmethodID getTextureCountMethodID = env->GetMethodID(activityClass,
                                                    "getTextureCount", "()I");
    if (getTextureCountMethodID == 0)
    {
        LOG("Function getTextureCount() not found.");
        return;
    }

    textureCount = env->CallIntMethod(obj, getTextureCountMethodID);    
    if (!textureCount)
    {
        LOG("getTextureCount() returned zero.");
        return;
    }

    textures = new Texture*[textureCount];

    jmethodID getTextureMethodID = env->GetMethodID(activityClass,
        "getTexture", "(I)Ledu/pugetsound/vichar/ar/Texture;");

    if (getTextureMethodID == 0)
    {
        LOG("Function getTexture() not found.");
        return;
    }

    // Register the textures
    for (int i = 0; i < textureCount; ++i)
    {

        jobject textureObject = env->CallObjectMethod(obj, getTextureMethodID, i); 
        if (textureObject == NULL)
        {
            LOG("GetTexture() returned zero pointer");
            return;
        }

        textures[i] = Texture::create(env, textureObject);
    }
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_initApplicationNative finished");
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_deinitApplicationNative(JNIEnv* env, jobject obj)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_deinitApplicationNative");

    // Release texture resources
    if (textures != 0)
    {    
        for (int i = 0; i < textureCount; ++i)
        {
            delete textures[i];
            textures[i] = NULL;
        }
    
        delete[]textures;
        textures = NULL;
        
        textureCount = 0;
    }
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_startCamera(JNIEnv *, jobject)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_startCamera");

    // Initialize the camera:
    if (!QCAR::CameraDevice::getInstance().init())
        return;

    // Configure the video background
    configureVideoBackground();

    // Select the default mode:
    if (!QCAR::CameraDevice::getInstance().selectVideoMode(
                                QCAR::CameraDevice::MODE_DEFAULT))
        return;

    // Start the camera:
    if (!QCAR::CameraDevice::getInstance().start())
        return;

    // Uncomment to enable flash
//    if(QCAR::CameraDevice::getInstance().setFlashTorchMode(true))
//    LOG("IMAGE TARGETS : enabled torch");

    // Uncomment to enable infinity focus mode, or any other supported focus mode
    // See CameraDevice.h for supported focus modes
    if(QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_INFINITY))
    	LOG("IMAGE TARGETS : enabled infinity focus");
    if(QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_CONTINUOUSAUTO))
    	LOG("IMAGE TARGETS : enabled continuous focus");

    // Start the tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::Tracker* imageTracker = trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER);
    if(imageTracker != 0)
        imageTracker->start();
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_stopCamera(JNIEnv *, jobject)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_stopCamera");

    // Stop the tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::Tracker* imageTracker = trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER);
    if(imageTracker != 0)
        imageTracker->stop();
    
    QCAR::CameraDevice::getInstance().stop();
    QCAR::CameraDevice::getInstance().deinit();
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_setProjectionMatrix(JNIEnv *, jobject)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameActivity_setProjectionMatrix");

    // Cache the projection matrix:
    const QCAR::CameraCalibration& cameraCalibration =
                                QCAR::CameraDevice::getInstance().getCameraCalibration();
    projectionMatrix = QCAR::Tool::getProjectionGL(cameraCalibration, 2.0f,
                                            2000.0f);
}


JNIEXPORT jboolean JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_activateFlash(JNIEnv*, jobject, jboolean flash)
{
    return QCAR::CameraDevice::getInstance().setFlashTorchMode((flash==JNI_TRUE)) ? JNI_TRUE : JNI_FALSE;
}


JNIEXPORT jboolean JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_autofocus(JNIEnv*, jobject)
{
    return QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_TRIGGERAUTO) ? JNI_TRUE : JNI_FALSE;
}


JNIEXPORT jboolean JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_setFocusMode(JNIEnv*, jobject, jint mode)
{
    int qcarFocusMode;

    switch ((int)mode)
    {
        case 0:
            qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_NORMAL;
            break;
        
        case 1:
            qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_CONTINUOUSAUTO;
            break;
            
        case 2:
            qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_INFINITY;
            break;
            
        case 3:
            qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_MACRO;
            break;
    
        default:
            return JNI_FALSE;
    }
    
    return QCAR::CameraDevice::getInstance().setFocusMode(qcarFocusMode) ? JNI_TRUE : JNI_FALSE;
}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameRenderer_initRendering(
                                                    JNIEnv* env, jobject obj)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameRenderer_initRendering");

    // Define clear color
    glClearColor(0.0f, 0.0f, 0.0f, QCAR::requiresAlpha() ? 0.0f : 1.0f);
    
    // Now generate the OpenGL texture objects and add settings
    for (int i = 0; i < textureCount; ++i)
    {
        glGenTextures(1, &(textures[i]->mTextureID));
        glBindTexture(GL_TEXTURE_2D, textures[i]->mTextureID);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textures[i]->mWidth,
                textures[i]->mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                (GLvoid*)  textures[i]->mData);
    }
#ifndef USE_OPENGL_ES_1_1
  
    shaderProgramID     = SampleUtils::createProgramFromBuffer(cubeMeshVertexShader,
                                                            cubeFragmentShader);

    vertexHandle        = glGetAttribLocation(shaderProgramID,
                                                "vertexPosition");
    normalHandle        = glGetAttribLocation(shaderProgramID,
                                                "vertexNormal");
    textureCoordHandle  = glGetAttribLocation(shaderProgramID,
                                                "vertexTexCoord");
    mvpMatrixHandle     = glGetUniformLocation(shaderProgramID,
                                                "modelViewProjectionMatrix");

#endif

}


JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameRenderer_updateRendering(
                        JNIEnv* env, jobject obj, jint width, jint height)
{
    LOG("Java_edu_pugetsound_vichar_ar_ARGameRenderer_updateRendering");

    // Update screen dimensions
    screenWidth = width;
    screenHeight = height;

    // Reconfigure the video background
    configureVideoBackground();
}


#ifdef __cplusplus
}
#endif
