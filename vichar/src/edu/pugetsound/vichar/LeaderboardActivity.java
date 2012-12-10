package edu.pugetsound.vichar;

import java.net.URL;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.os.Bundle;
import android.view.Gravity; 
import android.webkit.WebView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/**
 * Takes a dummy JSONObject, converts it to an ArrayList of String[]s of length 2,
 * sorts the ArrayList from highest to lowest scores, and places the sorted 
 * list into a table. Like a bawss.
 * @author Davis Shurbert
 * @version 10/23/12
 */
public class LeaderboardActivity extends Activity implements GetJSONArrayTask.JSONArrayReceiver {
    
    final Context context = this;
    public static JSONArray myJson; //JSON Array to be parsed
    public static JSONObject tableJson;
    public static ArrayList<String[]> names = new ArrayList<String[]>(); //ArrayList of String[]s 
    //from our JSONObject with keys at [0] and values at [1]
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        getJson();
        tableJson = parseJSONArray(myJson);
        names = getJSONStrings(tableJson);
        names = sortScores(names);
        createButtons();
        createTable();
        
        //help with banding on gradients
        getWindow().setFormat(PixelFormat.RGBA_8888);
        
    }
    
    
    public void getJson() {
        //gets Json from webserver using the HttpService and sets myJson 
        //equal to the return value.
        try {
        URL url = new URL("http://puppetmaster.pugetsound.edu:1730/leaderboard.json");
        new GetJSONArrayTask(this).execute(url);
        }
        catch(Exception E) {
            System.out.println("exception caught");
        }
    }
    
    
    public void onReceiveJSONArray(String jstring) {
        try {
            myJson = new JSONArray(jstring);
            System.out.println(jstring);
        }
        catch(Exception e) {
            
        }
    }
    
    public JSONObject parseJSONArray(JSONArray array) {
        JSONObject retval = new JSONObject();
        int leng = array.length();
        for (int i = 0; i<leng; i++) {
            try {
            JSONObject temp = array.getJSONObject(i);
            String namestr = temp.getString("names");
            String scorestr = temp.getString("scores");
            retval.put(namestr, scorestr);
            }
            catch(Exception e) {
                
            }
            
        }
        
        return retval;
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
    
    private void createButtons() {
        Button backb = (Button)findViewById(R.id.main_menu_button); 
        backb.setOnClickListener(backListener);
    }

    private OnClickListener backListener = new OnClickListener() { //sets what happens when the button is pushed
        public void onClick(View v) { 
            
            startActivity(new Intent(context, MainMenuActivity.class));
        }
       };
    
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
        TableRow row6 = new TableRow(this);
        TableRow row7 = new TableRow(this);
        TableRow row8 = new TableRow(this);
        TableRow row9 = new TableRow(this);
        TableRow row10 = new TableRow(this);

        
        //creates title row
        TextView title = new TextView(this);  
        title.setText("Scores");  
        title.setTextSize(16, 18);
        title.setTextColor(getResources().getColor(R.color.leaderboard_color));
        title.setGravity(Gravity.CENTER); //centers text in parent View
        title.setTypeface(Typeface.SERIF, Typeface.BOLD);  //sets font
        row0.addView(title); //adds the TextView to the title (first) row
       
        if (halt >= 1) {
          //creates row #1
            TextView name1 = new TextView(this);  
            name1.setText(names.get(0)[0]); //sets name to corresponding key from ArrayList  
            name1.setTextColor(getResources().getColor(R.color.leaderboard_color));
            TextView score1 = new TextView(this);  
            score1.setTextColor(getResources().getColor(R.color.leaderboard_color));
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
            name2.setTextColor(getResources().getColor(R.color.leaderboard_color));
            TextView score2 = new TextView(this);  
            score2.setText(names.get(1)[1]); 
            score2.setTextColor(getResources().getColor(R.color.leaderboard_color));
            row2.addView(new TextView(this));
            row2.addView(name2);
            row2.addView(new TextView(this));
            row2.addView(score2);
        }
            
        if (halt >= 3) {
          //creates row #3
            TextView name3 = new TextView(this);  
            name3.setText(names.get(2)[0]);   
            name3.setTextColor(getResources().getColor(R.color.leaderboard_color));
            TextView score3 = new TextView(this);  
            score3.setText(names.get(2)[1]); 
            score3.setTextColor(getResources().getColor(R.color.leaderboard_color));
            row3.addView(new TextView(this));
            row3.addView(name3);
            row3.addView(new TextView(this));
            row3.addView(score3);
        }
        if (halt >= 4) {
          //creates row #4
            TextView name4 = new TextView(this);  
            name4.setText(names.get(3)[0]);   
            name4.setTextColor(getResources().getColor(R.color.leaderboard_color));
            TextView score4 = new TextView(this);  
            score4.setText(names.get(3)[1]);
            score4.setTextColor(getResources().getColor(R.color.leaderboard_color));
            row4.addView(new TextView(this));
            row4.addView(name4);
            row4.addView(new TextView(this));
            row4.addView(score4);
        }
        if (halt >= 5) {
          //creates row #5
            TextView name5 = new TextView(this);  
            name5.setText(names.get(4)[0]);   
            name5.setTextColor(getResources().getColor(R.color.leaderboard_color));
            TextView score5 = new TextView(this);  
            score5.setText(names.get(4)[1]); 
            score5.setTextColor(getResources().getColor(R.color.leaderboard_color));
            row5.addView(new TextView(this));
            row5.addView(name5);
            row5.addView(new TextView(this));
            row5.addView(score5);
        }
        if (halt >= 6) {
            //creates row #6
              TextView name6 = new TextView(this);  
              name6.setText(names.get(5)[0]);   
              name6.setTextColor(getResources().getColor(R.color.leaderboard_color));
              TextView score6 = new TextView(this);  
              score6.setText(names.get(5)[1]); 
              score6.setTextColor(getResources().getColor(R.color.leaderboard_color));
              row6.addView(new TextView(this));
              row6.addView(name6);
              row6.addView(new TextView(this));
              row6.addView(score6);
          }
        if (halt >= 7) {
            //creates row #7
              TextView name7 = new TextView(this);  
              name7.setText(names.get(6)[0]);   
              name7.setTextColor(getResources().getColor(R.color.leaderboard_color));
              TextView score7 = new TextView(this);  
              score7.setText(names.get(6)[1]); 
              score7.setTextColor(getResources().getColor(R.color.leaderboard_color));
              row7.addView(new TextView(this));
              row7.addView(name7);
              row7.addView(new TextView(this));
              row7.addView(score7);
          }
        if (halt >= 8) {
            //creates row #8
              TextView name8 = new TextView(this);  
              name8.setText(names.get(7)[0]);   
              name8.setTextColor(getResources().getColor(R.color.leaderboard_color));
              TextView score8 = new TextView(this);  
              score8.setText(names.get(7)[1]); 
              score8.setTextColor(getResources().getColor(R.color.leaderboard_color));
              row8.addView(new TextView(this));
              row8.addView(name8);
              row8.addView(new TextView(this));
              row8.addView(score8);
          }
        if (halt >= 9) {
            //creates row #9
              TextView name9 = new TextView(this);  
              name9.setText(names.get(8)[0]);   
              name9.setTextColor(getResources().getColor(R.color.leaderboard_color));
              TextView score9 = new TextView(this);  
              score9.setText(names.get(8)[1]); 
              score9.setTextColor(getResources().getColor(R.color.leaderboard_color));
              row9.addView(new TextView(this));
              row9.addView(name9);
              row9.addView(new TextView(this));
              row9.addView(score9);
          }
        if (halt >= 10) {
            //creates row #10
              TextView name10 = new TextView(this);  
              name10.setText(names.get(9)[0]);   
              name10.setTextColor(getResources().getColor(R.color.leaderboard_color));
              TextView score10 = new TextView(this);  
              score10.setText(names.get(9)[1]); 
              score10.setTextColor(getResources().getColor(R.color.leaderboard_color));
              row10.addView(new TextView(this));
              row10.addView(name10);
              row10.addView(new TextView(this));
              row10.addView(score10);
          }
            
        table.addView(row0); //add rows to table
        table.addView(row1);
        table.addView(row2);
        table.addView(row3);
        table.addView(row4);
        table.addView(row5);
        table.addView(row6);
        table.addView(row7);
        table.addView(row8);
        table.addView(row9);
        table.addView(row10);
        
        //puts the table on the screen
        addContentView(table, new LayoutParams( LayoutParams.MATCH_PARENT , LayoutParams.MATCH_PARENT ) );
        }


}
