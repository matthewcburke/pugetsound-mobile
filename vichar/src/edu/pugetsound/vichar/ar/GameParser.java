package edu.pugetsound.vichar.ar;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class GameParser {
	
	private static final String TURRET_NAMESPACE = "turrets";
    private static final String TURRETBULLET_NAMESPACE = "turretsBullets";
    private static final String FIREBALL_NAMESPACE = "fireballs";
    private static final String MINION_NAMESPACE = "minions";
    private static final String BATTERY_NAMESPACE = "batteries";
    private static final String PLAYER_NAMESPACE = "player";
    private static final String EYEBALL_NAMESPACE = "eyeballs";
    private static final String PLATFORM_NAMESPACE = "platforms";
    private static final String POSITION_NAMESPACE = "position";
    private static final String ROTATION_NAMESPACE = "rotation";
    
  //JSON parsing
    protected static float[] poseData = new float[70];
    protected static boolean updated = false;
    protected static final int OBJ_SIZE = 7; 	// the number of array positions to use to represent a game object.
    private static int arrayLen = 70;
    
    /**
     * Parse the engineState JSONObject into a float array in ARGameRender.
     * 
     * @throws JSONException
     */
    protected static void parseEngineState(JSONObject engineState, String deviceUUID) throws JSONException
    {
    	poseData = new float[arrayLen];
    	int count = 0; 

    	//will opt returning null clear the objects?
    	JSONObject turrets = engineState.optJSONObject(TURRET_NAMESPACE);
    	if(turrets != null){
    		count = loadObject(turrets, 1.0f, count, deviceUUID, false);
    	}
    	// TODO change type indices
    	JSONObject turretBullets = engineState.optJSONObject(TURRETBULLET_NAMESPACE);
    	if(turretBullets != null){
    		count = loadObject(turretBullets, 1.0f, count, deviceUUID, false);
    	}

    	JSONObject fireballs = engineState.optJSONObject(FIREBALL_NAMESPACE);
    	if(fireballs != null){
    		count = loadObject(fireballs, 1.0f, count, deviceUUID, false);
    	}

    	JSONObject minions = engineState.optJSONObject(MINION_NAMESPACE);
    	if(minions != null){
    		count = loadObject(minions, 1.0f, count, deviceUUID, false);
    	}

    	JSONObject batteries = engineState.optJSONObject(BATTERY_NAMESPACE);
    	if(batteries != null){
    		count = loadObject(batteries, 1.0f, count, deviceUUID, true);
    	}

    	JSONObject player = engineState.optJSONObject(PLAYER_NAMESPACE);
    	// load player 
    	if(player != null)
    	{
    		DebugLog.LOGI(player.toString());
    	   	if( count + OBJ_SIZE >= arrayLen)
    	   	{
    	   		int newLen = arrayLen * 2;
        		resizeArray(poseData, newLen);
        		arrayLen = newLen;
        	}
    	   	poseData[count++] = 1.0f; // TODO use enums to represent the types of gameobjects.
    		count = parsePosition(player.getJSONObject(POSITION_NAMESPACE), count);
    		count = parseRotaion(player.getJSONObject(ROTATION_NAMESPACE), count);
    		updated = true;
//    		DebugLog.LOGI( "Parse:" + player.toString());
    	}
    	else DebugLog.LOGI("No Player");
    	
		JSONObject eyeballs = engineState.optJSONObject(EYEBALL_NAMESPACE);
    	if(eyeballs != null)
    	{
    		count = loadObject(eyeballs, 1.0f, count, deviceUUID, false);
    	}

    	JSONObject platforms = engineState.optJSONObject(PLATFORM_NAMESPACE);
    	if(platforms != null)
    	{
    		// TODO do something with the platforms
    	}
    }

    /**
     * A helper method to load the object in the array
     * @param type
     * @param typeIndex
     * @param i
     * @return
     * @throws JSONException
     */
    private static int loadObject(JSONObject type, float typeIndex, int i, String deviceUUID, boolean isBattery) throws JSONException
    {
    	Iterator<String> objItr = type.keys();

    	while( objItr.hasNext())
    	{
    		String thisEye = objItr.next();
    		JSONObject obj = type.getJSONObject(thisEye);
    		if( i + OBJ_SIZE >= arrayLen)
    		{
    			int newLen = arrayLen * 2;
    			resizeArray(poseData, newLen);
    			arrayLen = newLen;
    		}
    		if (deviceUUID.equals(thisEye))
    		{
    			return i;
    		}
    		else
    		{
    			poseData[i++] = typeIndex; // TODO use enums to represent the types of gameobjects.
    			i = parsePosition(obj.getJSONObject(POSITION_NAMESPACE), i);
    			if(!isBattery)
    			{
    				i = parseRotaion(obj.getJSONObject(ROTATION_NAMESPACE), i);
    			}
    			updated = true;
    		}
    	}
    	return i;
    }
    
    /**
     * loads position JSON data into poseData array.
     * @param xyz
     * @param i
     * @throws JSONException
     */
    private static int parsePosition(JSONObject xyz, int i) throws JSONException
    {
    	poseData[i++] = Float.parseFloat(xyz.getString("x"));
    	poseData[i++] = Float.parseFloat(xyz.getString("y"));
    	poseData[i++] = Float.parseFloat(xyz.getString("z"));
    	return i;
    }
    
    /**
     * loads rotation JSON data into poseData array. Designed to be called immediately after parsePosition.
     * @param xyz
     * @param i
     * @throws JSONException
     */
    private static int parseRotaion(JSONObject xyz, int i) throws JSONException
    {
    	poseData[i++] = Float.parseFloat(xyz.getString("x"));
    	poseData[i++] = Float.parseFloat(xyz.getString("y"));
    	poseData[i++] = Float.parseFloat(xyz.getString("z"));
    	return i;
    }

    private static float[] resizeArray (float[] oldArray, int newSize) {
    	int oldSize = oldArray.length;
    	float[] newArray = new float[newSize];
    	int preserveSize = Math.min(oldSize, newSize);
    	for(int i=0; i<preserveSize; i++){
    		newArray[i] = oldArray[i];
    	}
    	return newArray; 
    }
}
