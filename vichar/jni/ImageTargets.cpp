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
#include "SampleMath.h"
#include "Texture.h"
#include "CubeShaders.h"
#include "Teapot.h"

// UPDATE:: Our models to be displayed
// TODO: Should we put all of these .h files into one gameObjects.h file?
#include "banana.h"
#include "tower_top.h"
#include "tower_shell.h"
#include "cube.h"

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
float turAng = 0.0;  //UNUSED

// An array used to pass camera pose information. The first entry is used as a flag to indicate whether or not a target is in sight.
// the next three are x, y, and z locations respectively, and the last three are degrees of rotation around the x, y, and z axis respectively.
float phoneLoc[7] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

int drawCount = 0.0;

QCAR::Matrix44F modelViewMatrix;

// Screen dimensions:
unsigned int screenWidth        = 0;
unsigned int screenHeight       = 0;

// Indicates whether screen is in portrait (true) or landscape (false) mode
bool isActivityInPortraitMode   = false;

// The projection matrix used for rendering virtual objects:
QCAR::Matrix44F projectionMatrix;

// Constants:
static const float kObjectScale = 20.f; // UPDATE:: increased the scale to properly display our models. It was 3 for the teapots.

QCAR::DataSet* dataSetVichar    = 0;
QCAR::DataSet* dataSetFlakesBox = 0;

bool switchDataSetAsap          = false;
//Known scale values entered, unkown values remain as kObjectScale
static const int turretId = 1;
float turretScale[3] = {20.0, 20.0, 20.0};

static const int turretBulletId = 2;
float turretBulletScale[3] = {5.0, 5.0, 5.0};

//Turrent Base unimplemented thus far
//static const int turretBaseId=*****;
//float turretBaseScale[3]={20,20,40};

static const int fireballId = 3;
float fireballScale[3] = {20.0, 20.0, 20.0};   //These values assigned before fireball model created -- could need adjustment

static const int minionId = 4;
float minionScale[3] = {kObjectScale, kObjectScale, kObjectScale};

static const int batteryId = 5;
float batteryScale[3] = {15.0, 15.0, 15.0};

static const int playerId = 6;
float playerScale[3] = {2 * 20.0, 2 * 20.0, 2 * 20.0};

static const int eyeballId = 7;
float eyeScale[3] = {kObjectScale, kObjectScale, kObjectScale};

static const int platformId = 8;
float platformScale[3] = {kObjectScale, kObjectScale, kObjectScale};

typedef struct _Model {
	int id;
	
	// memory addresses
	float* vertPointer;
	float* normPointer;
    float* texPointer;

	unsigned int numVerts;

	Texture* modelTex;

	float pos[3];
	float ang[3];
	float scale[3];

} Model;

int modelCount;
Model drawList[100];

int interpLength = 0;
const int MAX_INTERPLENGTH=150; //TODO make resizable
float interpList[MAX_INTERPLENGTH][7];

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
            if (imageTracker == 0 || dataSetVichar == 0 || dataSetFlakesBox == 0 ||
                imageTracker->getActiveDataSet() == 0)
            {
                LOG("Failed to switch data set.");
                return;
            }

            if (imageTracker->getActiveDataSet() == dataSetVichar)
            {
                imageTracker->deactivateDataSet(dataSetVichar);
                imageTracker->activateDataSet(dataSetFlakesBox);
            }
            else
            {
                imageTracker->deactivateDataSet(dataSetFlakesBox);
                imageTracker->activateDataSet(dataSetVichar);
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
    dataSetVichar = imageTracker->createDataSet();
    if (dataSetVichar == 0)
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
    if (!dataSetVichar->load("vichar.xml", QCAR::DataSet::STORAGE_APPRESOURCE))
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
    if (!imageTracker->activateDataSet(dataSetVichar))
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

    if (dataSetVichar != 0)
    {
        if (imageTracker->getActiveDataSet() == dataSetVichar &&
            !imageTracker->deactivateDataSet(dataSetVichar))
        {
            LOG("Failed to destroy the tracking data set vichar because the data set "
                "could not be deactivated.");
            return 0;
        }

        if (!imageTracker->destroyDataSet(dataSetVichar))
        {
            LOG("Failed to destroy the tracking data set vichar.");
            return 0;
        }

        LOG("Successfully destroyed the data set vichar.");
        dataSetVichar = 0;
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



void
updateDrawList()
{
//Retrieve JSON object or parsed object HERE

//this method pulls substantially from updateDominoTransform in Dominoes.cpp

for(int i = 0; i<interpLength; i++)
{
	 
	Model* current= &drawList[i];

	float* position = &current->pos[0];
	float* angle = &current->ang[0];
	float* scale = &current->scale[0];

	current->id= (int) interpList[i][0];
	int id = (int) interpList[i][0];

	switch (id)
	    {
	        case 1: //Turret
	        	current->vertPointer=&tower_topVerts[0];
	        	current->normPointer=&tower_topNormals[0];
	        	current->texPointer=&tower_topTexCoords[0];

				scale[0]=turretScale[0];
				scale[1]=turretScale[1];
				scale[2]=turretScale[2];

	        	current->modelTex= textures[1];
	        	break;

	        case 2: //Turrent Shell
	        	current->vertPointer=&tower_shellVerts[0];
	        	current->normPointer=&tower_shellNormals[0];
	        	current->texPointer=&tower_shellTexCoords[0];
	        	current->numVerts=tower_shellNumVerts;

				scale[0]=turretBulletScale[0];
				scale[1]=turretBulletScale[1];
				scale[2]=turretBulletScale[2];

	        	current->modelTex=textures[2];
	            break;

	        case 3:  //Fireball
	        	current->vertPointer=&tower_shellVerts[0];
	        	current->normPointer=&tower_shellNormals[0];
	        	current->texPointer=&tower_shellTexCoords[0];
	        	current->numVerts=tower_shellNumVerts;

				scale[0]=fireballScale[0];
				scale[1]=fireballScale[1];
				scale[2]=fireballScale[2];

	        	current->modelTex=textures[2];

	            break;

	        case 4:		//Minion
	        	current->vertPointer=&tower_shellVerts[0];
	        	current->normPointer=&tower_shellNormals[0];
	        	current->texPointer=&tower_shellTexCoords[0];
	        	current->numVerts=tower_shellNumVerts;

				scale[0]=minionScale[0];
				scale[1]=minionScale[1];
				scale[2]=minionScale[2];

	        	current->modelTex=textures[2];
	        	break;

	        case 5:		//Battery
	        	current->vertPointer=&tower_shellVerts[0];
	        	current->normPointer=&tower_shellNormals[0];
	        	current->texPointer=&tower_shellTexCoords[0];
	        	current->numVerts=tower_shellNumVerts;

				scale[0]=batteryScale[0];
				scale[1]=batteryScale[1];
				scale[2]=batteryScale[2];

	        	current->modelTex=textures[2];
	        	break;

	        case 6:		//Player
	        	current->vertPointer=&tower_topVerts[0];
	        	current->normPointer=&tower_topNormals[0];
	        	current->texPointer=&tower_topTexCoords[0];
	        	current->numVerts=tower_topNumVerts;

				scale[0]=playerScale[0];
				scale[1]=playerScale[1];
				scale[2]=playerScale[2];

	        	current->modelTex= textures[1];
	        	break;

	        case 7:		//Eyeball
	        	current->vertPointer=&tower_topVerts[0];
	        	current->normPointer=&tower_topNormals[0];
	        	current->texPointer=&tower_topTexCoords[0];
	        	current->numVerts=tower_topNumVerts;

				scale[0]=eyeScale[0];
				scale[1]=eyeScale[1];
				scale[2]=eyeScale[2];

	        	current->modelTex= textures[1];
	        	break;

	        case 8:		//Platform
	        	current->vertPointer=&tower_shellVerts[0];
	        	current->normPointer=&tower_shellNormals[0];
	        	current->texPointer=&tower_shellTexCoords[0];
	        	current->numVerts=tower_shellNumVerts;

				scale[0]=platformScale[0];
				scale[1]=platformScale[1];
				scale[2]=platformScale[2];

	        	current->modelTex=textures[2];
	        	break;

	        default:
	        	return;
	    }

	//float position[3];
	position[0]=interpList[i][1];
	position[1]=interpList[i][2];
	position[2]=interpList[i][3];

	//float angle[3];
	angle[0]=interpList[i][4];
	angle[1]=interpList[i][5];
	angle[2]=interpList[i][6];

	drawCount=drawCount+1;
	}
}

/** The native render function.
 *
 *  update = true if the array has been updated by the JSON object
 *  test = an array of location and rotation information: see format below.
 *
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
 * */
JNIEXPORT void JNICALL
Java_edu_pugetsound_vichar_ar_ARGameRenderer_renderFrame(JNIEnv * env, jobject obj, jboolean updated, jfloatArray test, jint objSize)
{
	bool update;
	update = (bool) updated; //so we know whether or not to update the drawlist.
	float testScale = 0.1f; // don't set to zero. 1/testScale is used to scale the phone location going to the game engine.

//	if(update)
//		{
//		LOG("Start RenderFrame. Updated: true");
//		}
//	else
//	{
//		LOG("Start RenderFrame. Updated: false");
//	}

	if(update){
		int i = 0;
		int j = 0;
		jsize len = env->GetArrayLength(test);
		jfloat* posData = env->GetFloatArrayElements(test, 0);
		while(i<len && posData[(i/objSize)*objSize] != 0){
//			LOG("JSON to JNI test. Pos. %d : %f", i, posData[i]); //print the elements of the array.
			interpList[i/objSize][i%objSize]= (float) posData[i]; //can't scale here, it screws up the angles
			i++;
		}
		interpLength=(i)/objSize;
		//LOG("%i", interpLength);
		env->ReleaseFloatArrayElements(test, posData, 0); //release memory
	}

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
    for(int tIdx = 0; tIdx < state.getNumActiveTrackables(); tIdx++)
    {
        // Get the trackable:
        const QCAR::Trackable* trackable = state.getActiveTrackable(tIdx);
        QCAR::Matrix44F modelViewMatrix =
            QCAR::Tool::convertPose2GLMatrix(trackable->getPose());

//Begin additions by Erin================================================================================
        QCAR::Matrix34F test;   //gets inverse pos matrix
        QCAR::Matrix34F pos;   //Gets positional data
        pos = trackable->getPose();

        //Get inverse
        test = SampleMath::phoneCoorMatrix(&pos);

        //Get Euler angles
        QCAR::Vec3F euler = SampleMath::getEulerAngles(&test);

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
        phoneLoc[0] = 1.0f;
        phoneLoc[1] = test.data[3];
        phoneLoc[2] = test.data[7];
        phoneLoc[3] = test.data[11];
        phoneLoc[4] = euler.data[0];
		phoneLoc[5] = euler.data[1];
	    phoneLoc[6] = euler.data[2];
	    //print phone pose data
//	    LOG("x: %f, y: %f, z: %f, xRot: %f, yRot: %f, zRot: %f",
//	    		phoneLoc[1],phoneLoc[2],phoneLoc[3],phoneLoc[4],phoneLoc[5],phoneLoc[6]);
//End============================================================================================

        // Assign Textures according in the texture indices defined at the beginning of the file, and based
        // on the loadTextures() method in ARGameActivity.java.


        const Texture* const tower_shellTexture = textures[tower_shellIndex];
        const Texture* const tower_topTexture = textures[tower_topIndex];
        const Texture* const platformTexture = textures[8];

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
		/* MATRIX GUIDE
		ModelViewMatrix = starting point matrix, with no mods is the center of the image target.
		ProjectionMatrix = Is set utilizing a method in this class, when called by the Java. Is not modified by
		drawing process.
		ModelViewProjection = (w/o prefab transforms) Modified ModelViewMatrix * Projection
		*/

		drawCount=0;
		//obtain list of objects to draw -- fills drawList.
		updateDrawList();

		//Get modelViewMatrix
		modelViewMatrix = QCAR::Tool::convertPose2GLMatrix(trackable->getPose());

		//The final matrix that is used to draw
		QCAR::Matrix44F modelViewProjection;

        glUseProgram(shaderProgramID);
		
		// Render the models
		for (int i = 0; i < drawCount; i++) {
			
			//Reinitalize ModelViewMatrix to its initialstate, as at this point it gets modified.
			modelViewMatrix = QCAR::Tool::convertPose2GLMatrix(trackable->getPose());

			//Loads the current model from the drawList
			Model* model = &drawList[i];

			//NON-HARDCODED VERSION

			float& vert = *model->vertPointer;
			float& norm = *model->normPointer;
			float& tex = *model->texPointer;

			glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
								 (const GLvoid*) &vert);
			glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0,
								  (const GLvoid*) &norm);
			glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
								  (const GLvoid*) &tex);
	
			//Open GL initialization
			glEnableVertexAttribArray(vertexHandle);
			glEnableVertexAttribArray(normalHandle);
			glEnableVertexAttribArray(textureCoordHandle);

			//Prep Transforms
			float* position=&model->pos[0];
			float* angle=&model->ang[0];
			float* scale=&model->scale[0];
			for(int i = 0; i < 3; i++)
			{
				position[i] = position[i] * testScale;
			}

			//LOG("%f%f%f",position[0],position[1],position[2]);

			//Begin Transforms
			//BE WARY OF GETTING RID OR ADDING KObject SCALE
			SampleUtils::translatePoseMatrix(position[0], position[1], kObjectScale + position[2],
										&modelViewMatrix.data[0]);
			// So the tower_top appears upright
			SampleUtils::rotatePoseMatrix(90.0f + angle[0], 1.0f, 0.0f, 0.0f,
                        				&modelViewMatrix.data[0]);
			SampleUtils::rotatePoseMatrix(angle[1], 0.0f, 1.0f, 0.0f,
                        				&modelViewMatrix.data[0]);
			SampleUtils::rotatePoseMatrix(angle[2], 0.0f, 0.0f, 1.0f,
										&modelViewMatrix.data[0]);
			SampleUtils::scalePoseMatrix(scale[0], scale[1], scale[2],
										&modelViewMatrix.data[0]);

			//Combine projectionMatrix and modelViewMatrix to create final modelViewProejctionMatrix
			SampleUtils::multiplyMatrix(&projectionMatrix.data[0],
										&modelViewMatrix.data[0] ,
										&modelViewProjection.data[0]);

			//Assign and bind texture -- once again this is hard coded to turrets
			glActiveTexture(GL_TEXTURE0);

			//un-hardcoding
			glBindTexture(GL_TEXTURE_2D, model->modelTex->mTextureID);

			//apply modelViewProjectionMatrix
			glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
							   (GLfloat*)&modelViewProjection.data[0] );

			//Un-hardcoding
			glDrawArrays(GL_TRIANGLES, 0, model->numVerts);
			}

//		LOG("Render Frame Complete");		 
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
	//Deinit Open GL
    glDisableVertexAttribArray(vertexHandle);
    glDisableVertexAttribArray(normalHandle);
    glDisableVertexAttribArray(textureCoordHandle);
#endif
	//END
    QCAR::Renderer::getInstance().end();
}

// TODO: write this function to return the camera location.
JNIEXPORT jfloatArray JNICALL
Java_edu_pugetsound_vichar_ar_ARGameActivity_getCameraLocation(JNIEnv * env, jobject)
{

	jfloatArray cameraLocation;
	cameraLocation = env->NewFloatArray(7);
	// phone location and rotation.
	jfloat coordArray[7] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	env->SetFloatArrayRegion(cameraLocation, 0, 7, phoneLoc);
	phoneLoc[0] = 0.0f; // reset flag to no target in sight ?? Bad idea?
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
