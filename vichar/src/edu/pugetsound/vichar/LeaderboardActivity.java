package edu.pugetsound.vichar;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.os.Bundle;
import android.view.Gravity; 
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.graphics.Typeface;

/**
 * Takes a dummy JSONObject, converts it to an ArrayList of String[]s of length 2,
 * sorts the ArrayList from highest to lowest scores, and places the sorted 
 * list into a table. Like a bawss.
 * @author Davis Shurbert
 * @version 10/23/12
 */
public class LeaderboardActivity extends Activity {
	
    public static JSONObject myJson = new JSONObject(); //JSON Object to be parsed
    public static ArrayList<String[]> names = new ArrayList<String[]>(); //ArrayList of String[]s 
    //from our JSONObject with keys at [0] and values at [1]
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        createJson();
        names = getJSONStrings(myJson);
        names = sortScores(names);
        createTable();
    }
    
	
    public void createJson() {
        try{           
        myJson.put("Davis", "9000000");
        myJson.put("Nathan", "5");
        myJson.put("Micheal", "2456");
        myJson.put("Troll", "Not a score!?");
        myJson.put("Kirah", "5462");
        myJson.put("Not Top Five", "1");
        myJson.put("Robert", "4652");
        
        }
        catch(JSONException ex){
        System.out.print("createJson failed");
        }
        
    }
    
    /**
     * Converts JSONObject into an ArrayList of String[] and returns said list.
     * @param json The JSONObject
     * @return The ArrayList<String[]> representation of the JSONObject
     */
    public ArrayList<String[]> getJSONStrings(JSONObject json) {
        ArrayList<String[]> retVal = new ArrayList<String[]>();//the ArrayList to be returned
        try {
            JSONArray leaders = json.names();//creates an ordered JSONArray of the keys  
            for (int i = 0 ; i < json.length(); i++) { //stores keys of myJson in retVal
                String key = leaders.get(i).toString(); //gets key
                String val = json.get(key).toString(); //gets value associated with key 
                String[] temp = {key, val}; //puts both strings in array
                retVal.add(temp); //adds array to retVal 
            }
           
        }
        catch(JSONException ex) {
            System.out.print("getJSONStrings failed");
        }
        return retVal;
    }
    
    /**
     * Sorts an Array List of String[]s of length 2.
     * @param jarray The unsorted ArrayList representation of the JSONObject
     * @return The sorted ArrayList
     */
    public ArrayList<String[]> sortScores(ArrayList<String[]> jarray)
    {
        //insertion sort
        ArrayList<String[]> retVal = jarray; //the Array List to be returned
        for (int i = 0; i < retVal.size(); i++) {
            int max = 0;
            int index = i;
            for (int j = i; j < retVal.size(); j++) {
                int k;
                try {//removes bad indices from list if necessary by catching
                    //exception thrown by parseInt
                    k = Integer.parseInt(retVal.get(j)[1]);
                }
                catch (NumberFormatException e) {
                    retVal.remove(j);
                    j--;
                    continue;
                }
                if (k > max) {
                    max = k;
                    index = j;
                }
            }
            //swap
            String[] swap = retVal.get(i);
            retVal.set(i, retVal.get(index));
            retVal.set(index, swap);
        }
            
        return retVal;
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_leaderboard, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handles item selection in menu
    	switch (item.getItemId()) {
            case R.id.enter_main_menu:
            	startActivity(new Intent(this, MainMenuActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
    	}
    }
    /**
     * Creates and displays a table of names and scores from our static 
     * "names" ArrayList
     */
    private void createTable() {
        int halt = names.size(); //to avoid array index exceptions.
        
        TableLayout table = new TableLayout(this);
        table.setStretchAllColumns(true);
        table.setShrinkAllColumns(true);
        TableRow row0 = new TableRow(this);
        row0.setGravity(Gravity.CENTER_HORIZONTAL);//centers title
        TableRow row1 = new TableRow(this); //creates new rows for table
        TableRow row2 = new TableRow(this);
        TableRow row3 = new TableRow(this);
        TableRow row4 = new TableRow(this);
        TableRow row5 = new TableRow(this);
        
        //creates title row
        TextView title = new TextView(this);  
        title.setText("Scores");  
        title.setTextSize(16, 18);  
        title.setGravity(Gravity.CENTER); //centers text in parent View
        title.setTypeface(Typeface.SERIF, Typeface.BOLD);  //sets font
        row0.addView(title); //adds the TextView to the title (first) row
       
        if (halt >= 1) {
          //creates row #1
            TextView name1 = new TextView(this);  
            name1.setText(names.get(0)[0]); //sets name to corresponding key from ArrayList  
            TextView score1 = new TextView(this);  
            score1.setText(names.get(0)[1]); //sets score to corresponding value
                //Each TextView can only have one parent, so we need to
                //create a new instance of a blank TextView every time we want
                //to add one to the the table for spacing purposes.    
            row1.addView(new TextView(this)); //empty TextView for spacing
            row1.addView(name1); //adds name to row 
            row1.addView(new TextView(this));
            row1.addView(score1); //adds score to row
        }
        
        if (halt >= 2) {
          //creates row #2
            TextView name2 = new TextView(this);  
            name2.setText(names.get(1)[0]);   
            TextView score2 = new TextView(this);  
            score2.setText(names.get(1)[1]); 
            row2.addView(new TextView(this));
            row2.addView(name2);
            row2.addView(new TextView(this));
            row2.addView(score2);
        }
            
        if (halt >= 3) {
          //creates row #3
            TextView name3 = new TextView(this);  
            name3.setText(names.get(2)[0]);   
            TextView score3 = new TextView(this);  
            score3.setText(names.get(2)[1]); 
            row3.addView(new TextView(this));
            row3.addView(name3);
            row3.addView(new TextView(this));
            row3.addView(score3);
        }
        if (halt >= 4) {
          //creates row #4
            TextView name4 = new TextView(this);  
            name4.setText(names.get(3)[0]);   
            TextView score4 = new TextView(this);  
            score4.setText(names.get(3)[1]); 
            row4.addView(new TextView(this));
            row4.addView(name4);
            row4.addView(new TextView(this));
            row4.addView(score4);
        }
        if (halt >= 5) {
          //creates row #5
            TextView name5 = new TextView(this);  
            name5.setText(names.get(4)[0]);   
            TextView score5 = new TextView(this);  
            score5.setText(names.get(4)[1]); 
            row5.addView(new TextView(this));
            row5.addView(name5);
            row5.addView(new TextView(this));
            row5.addView(score5);
        }
            
        table.addView(row0); //add rows to table
        table.addView(row1);
        table.addView(row2);
        table.addView(row3);
        table.addView(row4);
        table.addView(row5);
        setContentView(table); //puts the table on the screen
    }
}
