package com.project.oats;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AbsenceActivity extends Activity {

	private static Toast toast;
	
	private static View layout;
	
	private TextView information;
	
	private EditText message;
	
	private ProgressBar loading;
	
	private SendAbsenceTask sat;
	
	private final String ABSENCE_ADDRESS = "https://oatsdailybeta.herokuapp.com/absence";
	
	private final String PREFS_NAME = "OatsPref";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_absence);
		
		message = (EditText)findViewById(R.id.absence_message);
		loading = (ProgressBar)findViewById(R.id.absence_loading);
		loading.getIndeterminateDrawable().setColorFilter(Color.rgb(200, 200, 200), android.graphics.PorterDuff.Mode.MULTIPLY);
		
		LayoutInflater inflater = getLayoutInflater();
	    layout = inflater.inflate(R.layout.info, (ViewGroup)findViewById(R.id.toast_layout_root));
	    information = (TextView)layout.findViewById(R.id.information);
	    
		toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER, 0, 0);
	    toast.setDuration(Toast.LENGTH_SHORT);
	    toast.setView(layout);
	    
	    sat = null;
	}
	
	public void onSendAbsenceClicked(View view) {
		showConfirmationDialog();
	}
	
	private void processResponse(JSONObject obj) {
		try {
			//showInfoDialog("Info", obj.toString());
			if(obj != null) {
				switch(obj.getInt("code")) {
					case 200:
						information.setText("The message is sent successfully.");
				    	toast.cancel();
					    toast.show();
					    finish();
						break;
					case 501:
						showLoginAgainDialog();
						break;
					case 502:
						showHaveCheckedInDialog();
						break;
					case 503:
						showInfoDialog("Error", "There is a problem when accessing database in the server.");
						break;
					default:
						showInfoDialog("Error", "Unknown error.");
						break;
				}
				loading.setVisibility(View.INVISIBLE);
			} else {
				loading.setVisibility(View.INVISIBLE);
				showConnectionSettingsDialog();
			}
		} catch(Exception e) {
			loading.setVisibility(View.INVISIBLE);
			showInfoDialog("Exception", e.getMessage());
		}
	}
	
	public void showConfirmationDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle("Confirm Your Action");
  
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you have written the right message?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	sat = new SendAbsenceTask(ABSENCE_ADDRESS);
        		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
        			sat.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message.getText().toString().replace('\n', ' '));
        		} else {
        			sat.execute(message.getText().toString().replace('\n', ' '));
        		}
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
	
	public void showConnectionSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle("Connection Problem");
  
        // Setting Dialog Message
        alertDialog.setMessage("Your phone is not connected to the Internet, please turn on your WiFi or Mobile Network.");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("WiFi", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        
        // On pressing Settings button
        alertDialog.setNeutralButton("Mobile Network", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
            	if(!isCallable(intent)) {
            		intent = new Intent(Intent.ACTION_MAIN);
            		intent.setClassName("com.android.phone", "com.android.phone.Settings");
            	}
                startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
    
    public void showInfoDialog(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle(title);
  
        // Setting Dialog Message
        alertDialog.setMessage(message);
  
        // on pressing cancel button
        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
    
    public void showHaveCheckedInDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle("Error");
  
        // Setting Dialog Message
        alertDialog.setMessage("You have checked-in or have sent one absence excuse message " +
    			"today so you cannot send another absence excuse message");
  
        // on pressing cancel button
        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            	finish();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
    
    public void showLoginAgainDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle("Error");
  
        // Setting Dialog Message
        alertDialog.setMessage("Your phone ID is not found in the server. Please login again");
  
        // on pressing cancel button
        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("token", "");
                editor.commit();
            	dialog.cancel();
            	setResult(CheckActivity.RESPONSE_CODE);
            	startActivity(new Intent(AbsenceActivity.this, LoginActivity.class));
				finish();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
    
    private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    
    @Override
	protected void onPause() {
        super.onPause();
        
        if(sat != null) {
        	sat.cancel(true);
        }
    }
	
	private class SendAbsenceTask extends AsyncTask<String, Void, JSONObject> {
		
		private final String CHARSET = "UTF-8";
		
		private String ADDRESS;
		
		public SendAbsenceTask(String s) {
			ADDRESS = s;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			message.setEnabled(false);
			loading.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
			if(isNetworkAvailable()) {
		    	OutputStream output = null;
				JSONObject obj = null;
				HashMap<String, String> error = new HashMap<String, String>();
				
				try {
					URLConnection connection = new URL(ADDRESS).openConnection();
					connection.setConnectTimeout(10000);
		    		connection.setDoInput(true);
		    		connection.setDoOutput(true);
		    		connection.setRequestProperty("Accept-Charset", CHARSET);
		            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
			    	output = connection.getOutputStream();
			    	
			    	SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
		            String query = String.format("access_token=%s&note=%s",
		            		URLEncoder.encode(pref.getString("token", ""), CHARSET),
		            		URLEncoder.encode(params[0], CHARSET));
		            if(query != null)
		                output.write(query.getBytes(CHARSET));
		            
					InputStream response = connection.getInputStream();
			        HttpURLConnection httpConnection = (HttpURLConnection)connection;
			        int status = httpConnection.getResponseCode();
			        BufferedReader reader = null;
			        if(status == 200) {
			        	String message = "";
		                reader = new BufferedReader(new InputStreamReader(response));
		                for (String line; (line = reader.readLine()) != null;) {
		                	message += line;
		                }
		                if (reader != null) {
		                	try { reader.close(); }
		                	catch (Exception e) { e.printStackTrace(); }
		                }
		                obj = new JSONObject(message);
			        }
		        } catch(Exception e) {
		        	error.put("message", e.getMessage());
		        	obj = new JSONObject(error);
		        } finally {
		             if (output != null) {
		            	 try { output.close(); return obj;}
		            	 catch (Exception e) {
		            		 error.put("message", e.getMessage());
		            		 obj = new JSONObject(error);
		            		 return obj;
		            	 }
		             }
		        }
				return obj;
		    }
			return null;
		}
		
		@Override
		protected void onPostExecute(JSONObject obj) {
			super.onPostExecute(obj);
			message.setEnabled(true);
			processResponse(obj);
		}
		
		private boolean isNetworkAvailable() {
	        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
	        
	        // if no network is available networkInfo will be null
	        // otherwise check if we are connected
	        if (networkInfo != null && networkInfo.isConnected()) {
	            return true;
	        }
	        return false;
	    }
	
	}
}
