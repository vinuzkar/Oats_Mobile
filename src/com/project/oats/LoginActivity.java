package com.project.oats;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import com.project.oats.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class LoginActivity extends Activity {
	
	private static Toast toast;
	
	private static View layout;
	
	private TextView information;
	
	private EditText userid, pass;
	
	private ProgressBar loading;
	
	private Button login;
	
	private LoginTask logtask;
	
	private final String LOGIN_ADDRESS = "http://oats.ap01.aws.af.cm/Login";
	
	private final String PREFS_NAME = "OatsPref";
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 0;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if(getDeviceDefaultOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.activity_login_port);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setContentView(R.layout.activity_login_land);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		userid = (EditText)findViewById(R.id.userid);
		pass = (EditText)findViewById(R.id.pass);
		loading = (ProgressBar)findViewById(R.id.login_loading);
		login = (Button)findViewById(R.id.login);
		logtask = null;
		
		LayoutInflater inflater = getLayoutInflater();
	    layout = inflater.inflate(R.layout.info, (ViewGroup)findViewById(R.id.toast_layout_root));
	    information = (TextView)layout.findViewById(R.id.information);
	    
		toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER, 0, 0);
	    toast.setDuration(Toast.LENGTH_SHORT);
	    toast.setView(layout);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSystemUiHider.hide();
			}
		});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	public void onLoginClicked(View view) {
		try {
	    	login.setEnabled(false);
	    	if(!userid.getText().toString().equals("") && !pass.getText().toString().equals("")) {
		    	logtask = new LoginTask(LOGIN_ADDRESS);
	    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
	    			logtask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, userid.getText().toString(), pass.getText().toString());
	    		} else {
	    			logtask.execute(userid.getText().toString(), pass.getText().toString());
	    		}
	    	} else {
	    		login.setEnabled(true);
	    	}
	    } catch(Exception e) {
	    	loading.setVisibility(View.GONE);
	    	showInfoDialog("Exception", e.getMessage());
	    	login.setEnabled(true);
	    }
	}
	
	private int getDeviceDefaultOrientation() {

	    WindowManager windowManager =  (WindowManager) getSystemService(WINDOW_SERVICE);

	    Configuration config = getResources().getConfiguration();

	    int rotation = windowManager.getDefaultDisplay().getRotation();

	    if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
	            config.orientation == Configuration.ORIENTATION_LANDSCAPE)
	        || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&    
	            config.orientation == Configuration.ORIENTATION_PORTRAIT))
	      return Configuration.ORIENTATION_LANDSCAPE;
	    else 
	      return Configuration.ORIENTATION_PORTRAIT;
	}
	
	@Override
	protected void onPause() {
        super.onPause();
        
        if(logtask != null) {
        	logtask.cancel(true);
        }
    }
	
	private void processResponse(JSONObject obj) {
		try {
			if(obj != null) {
				//showInfoDialog("Info", obj.toString());
				switch(obj.getInt("code")) {
					case 200:
		    			information.setText("You have successfully logged-in.");
				    	toast.cancel();
					    toast.show();
					    SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
				        SharedPreferences.Editor editor = pref.edit();
				        editor.putString("token", obj.optString("token"));
				        editor.commit();
				        startActivity(new Intent(this, CheckActivity.class));
				        finish();
						break;
					case 501:
						showInfoDialog("Error", "Combination of e-mail and password is not matched.");
						break;
					case 502:
						showInfoDialog("Error", "Your e-mail is not registered in the server.");
						break;
					case 503:
						showInfoDialog("Error", "There is a problem when accessing database in the server.");
						break;
					default:
						showInfoDialog("Error", "Unknown error.");
						break;
				}
				loading.setVisibility(View.GONE);
			} else {
				loading.setVisibility(View.GONE);
				showConnectionSettingsDialog();
			}
		} catch(Exception e) {
			loading.setVisibility(View.GONE);
			showInfoDialog("Exception", e.getMessage());
		} finally {
			login.setEnabled(true);
		}
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
    
    private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    
    private class LoginTask extends AsyncTask<String, Void, JSONObject> {
		
		private final String CHARSET = "UTF-8";
		
		private String ADDRESS;
		
		public LoginTask(String s) {
			ADDRESS = s;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
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
			    	
		            String query = String.format("email=%s&password=%s",
		            		URLEncoder.encode(params[0], CHARSET),
		            		URLEncoder.encode(params[1], CHARSET));
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
