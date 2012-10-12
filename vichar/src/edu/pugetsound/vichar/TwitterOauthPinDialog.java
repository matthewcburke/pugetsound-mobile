package edu.pugetsound.vichar;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.support.v4.app.DialogFragment;

public class TwitterOauthPinDialog extends DialogFragment {
	
    public interface TwitterOauthPinDialogListener 
    {
        public void onDialogPositiveClick(DialogFragment dialog, String pin);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    //Implementing class (MainActivity for example) becomes this
    static TwitterOauthPinDialogListener topdListener;
    
    public static TwitterOauthPinDialog newInstance(Activity activity) 
	{
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events with it
            topdListener = (TwitterOauthPinDialogListener) activity;
        } 
        catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
        TwitterOauthPinDialog frag = new TwitterOauthPinDialog();
        return frag;
	}
    
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.dialog_twitter_oauth_pin, null))
	    // Add action buttons
	           .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
	        	   @Override
	               public void onClick(DialogInterface dialog, int id) 
	        	   {
	        		   EditText editText = (EditText) getActivity().findViewById(R.id.twitter_auth_pin);
	        		   String pin = editText.getText().toString();
	        		   topdListener.onDialogPositiveClick(TwitterOauthPinDialog.this, pin);
	               }
	           })
	           .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   TwitterOauthPinDialog.this.getDialog().cancel();
	               }
	           });      
	    return builder.create();
	}
	
	

}
