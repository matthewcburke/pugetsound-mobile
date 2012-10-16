//package edu.pugetsound.vichar;
//
//import android.os.Bundle;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.webkit.WebView;
//import android.widget.EditText;
//import android.support.v4.app.DialogFragment;
//
//public class TwitterOauthPinDialog extends DialogFragment {
//	
//    public interface TwitterOauthPinDialogListener 
//    {
//        public void onDialogPositiveClick(DialogFragment dialog, String pin);
//        public void onDialogNegativeClick(DialogFragment dialog);
//    }
//    
//    //Implementing class (MainActivity for example) becomes this
//    static TwitterOauthPinDialogListener topdListener;
//    
//    private WebView webView;
//    private String webViewUrl;
//    
//    public static TwitterOauthPinDialog newInstance(Activity activity, String url) 
//	{
//        // Verify that the host activity implements the callback interface
//        try {
//            // Instantiate the NoticeDialogListener so we can send events with it
//            topdListener = (TwitterOauthPinDialogListener) activity;
//        } 
//        catch (ClassCastException e) {
//            // The activity doesn't implement the interface, throw exception
//            throw new ClassCastException(activity.toString()
//                    + " must implement TwitterOauthPinDialogListener");
//        }
//        TwitterOauthPinDialog frag = new TwitterOauthPinDialog();
//        System.out.println("Auth URL passed in: " + url);
//        frag.setUrl(url);
//        return frag;
//	}
//    
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState) {
//	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//	    // Get the layout inflater
//	    LayoutInflater inflater = getActivity().getLayoutInflater();
//	    
//	    // Inflate and set the layout for the dialog
//	    // Pass null as the parent view because its going in the dialog layout
//	    builder.setView(inflater.inflate(R.layout.dialog_twitter_oauth_pin, null))
//	    // Add action buttons
//	           .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
//	        	   @Override
//	               public void onClick(DialogInterface dialog, int id) 
//	        	   {
//	        		   EditText editText = (EditText) TwitterOauthPinDialog.this.getDialog().findViewById(R.id.twitter_auth_pin);
//	        		   String pin = editText.getText().toString();
//	        		   topdListener.onDialogPositiveClick(TwitterOauthPinDialog.this, pin);
//	               }
//	           })
//	           .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
//	               public void onClick(DialogInterface dialog, int id) {
//	                   TwitterOauthPinDialog.this.getDialog().cancel();
//	               }
//	           });
//	    return builder.create();
//	}
//	
//	@Override
//	public void onActivityCreated (Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		this.webView = (WebView) this.getView().findViewById(R.id.twitter_auth_webview);
//		this.loadUrl();
//	}
//
//	public void setUrl(String url) {
//		this.webViewUrl = url;
//	}
//	
//	public void loadUrl() {
//		System.out.println("Loading URL: " + this.webViewUrl);
//		//WebView webview = (WebView) this.getDialog().findViewById(R.id.twitter_auth_webview);
//		//System.out.println("Got webview ref");
//		System.out.println(this.webView);
//		this.webView.loadUrl(this.webViewUrl);
//		System.out.println("Loaded URL");
//	}
//
//}
