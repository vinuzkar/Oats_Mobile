package com.project.oats;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import org.json.JSONObject;

import com.project.oats.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class CheckActivity extends Activity implements LocationListener {
	
	private static Toast toast;
	
	private static View layout;
	
	private TextView information;
	
	private ToggleButton check;
	
	private ProgressBar loading;
	
	private String id;
	
	private GetLocation lokasi;
	
	private ConnectActivity act;
	
	private final String PREFS_NAME = "OatsPref";
	
	private final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	private final String CHECKIN_ADDRESS = "http://quiet-meadow-1305.herokuapp.com/checkin";
	
	private final String CHECKOUT_ADDRESS = "http://quiet-meadow-1305.herokuapp.com/checkout";
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
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	//private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_FULLSCREEN;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if(getDeviceDefaultOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.activity_check_port);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setContentView(R.layout.activity_check_land);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		check = (ToggleButton)findViewById(R.id.check);
		loading = (ProgressBar)findViewById(R.id.loading);
		
		LayoutInflater inflater = getLayoutInflater();
	    layout = inflater.inflate(R.layout.info, (ViewGroup)findViewById(R.id.toast_layout_root));
	    information = (TextView)layout.findViewById(R.id.information);
	    
		toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER, 0, 0);
	    toast.setDuration(Toast.LENGTH_SHORT);
	    toast.setView(layout);
	    
	    lokasi = null;
	    act = null;

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
			mSystemUiHider.hide();
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
	
	public void onToggleClicked(View view) {
    	((ToggleButton)view).toggle();
    	((ToggleButton)view).setPressed(true);
		try {
	    	check.setEnabled(false);
	    	lokasi = new GetLocation(view);
    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
    			lokasi.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    		} else {
    			lokasi.execute();
    		}
	    } catch(Exception e) {
	    	loading.setVisibility(View.GONE);
	    	((ToggleButton)view).toggle();
	    	showInfoDialog("Exception", e.getMessage());
	    	check.setEnabled(true);
	    }
	}
	
	public void onShowPerformanceClicked(View view) {
		startActivity(new Intent(this, PerformanceActivity.class));
	}
	
	private void processLocation(View view, Location l) {
		if(l != null) {
	    	if(id != null && !id.equals("")) {
	    		String URL_ADDRESS;
	    		if(((ToggleButton)view).isChecked()) {
	    			URL_ADDRESS = CHECKOUT_ADDRESS;
	    		} else {
	    			URL_ADDRESS = CHECKIN_ADDRESS;
	    		}
	    		
	    		act = new ConnectActivity(view, l);
	    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
	    			act.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, URL_ADDRESS, id);
	    		} else {
	    			act.execute(URL_ADDRESS, id);
	    		}
	    	} else {
	    		loading.setVisibility(View.GONE);
    			information.setText("Cannot get identificator of your mobile phone.");
		    	toast.cancel();
			    toast.show();
			    check.setEnabled(true);
	    	}
    	} else {
    		loading.setVisibility(View.GONE);
    		showLocationSettingsDialog(1);
    		check.setEnabled(true);
    	}
	}
	
	private void processResponse(View view, JSONObject obj, Location l) {
		try {
			if(obj != null) {
				String isCheckIn, isCheckOut;
				if(((ToggleButton)view).isChecked()) {
					isCheckIn = "checked-out";
					isCheckOut = "checked-in";
				} else {
					isCheckIn = "checked-in";
					isCheckOut = "checked-out";
				}
				//showInfoDialog("Info", obj.toString());
				switch(obj.getInt("code")) {
					case 200:
						SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
						Date date = format.parse(obj.getString("time"));
						Calendar cal = Calendar.getInstance();
						cal.setTime(date);
						TimeZone tz = cal.getTimeZone();
						showInfoDialog("Success", "You have successfully " + isCheckIn + " at " +
								 cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) + ", " +
								 cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + ' ' +
								 cal.get(Calendar.DATE) + ", " +
								 cal.get(Calendar.YEAR) + ' ' +
								 cal.get(Calendar.HOUR) + ':' + cal.get(Calendar.MINUTE) + ':' + cal.get(Calendar.SECOND) +
								 ' ' + cal.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US) + ' ' +
								 tz.getDisplayName(false, TimeZone.SHORT, Locale.US));
						((ToggleButton)view).toggle();
						break;
					case 501:
						showInfoDialog("Error", "Your mobile phone identificator is not registered in the server.");
						startActivity(new Intent(this, LoginActivity.class));
						finish();
						break;
					case 502:
						showLocationSettingsDialog(2);
						break;
					case 503:
						showInfoDialog("Error", "There is a problem when accessing database in the server.");
						break;
					case 504:
						showInfoDialog("Error", "You have " + isCheckIn + " before and have not " + isCheckOut + "again.");
						((ToggleButton)view).toggle();
						break;
					default:
						showInfoDialog("Error", "Unknown error.");
						break;
				}
			} else {
				showConnectionSettingsDialog();
			}
		} catch(Exception e) {
			showInfoDialog("Exception", e.getMessage());
		} finally {
			check.setEnabled(true);
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
        
        if(lokasi != null) {
        	if(act != null) {
        		if(!lokasi.isCancelled()) {
        			lokasi.cancel(true);
        		}
        		if(!act.isCancelled()) {
        			act.cancel(true);
        		}
        	} else {
        		if(!lokasi.isCancelled()) {
        			lokasi.cancel(true);
        		}
        	}
        }
        SharedPreferences isToggleChecked = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = isToggleChecked.edit();
        editor.putBoolean("isToggleChecked", check.isChecked());
        editor.commit();
    }

	@Override
	protected void onResume() {
        super.onResume();
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        if((id = pref.getString("token", "")) == null || id.equals("")) {
        	startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        check.setChecked(pref.getBoolean("isToggleChecked", false));
    }
	
	/**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showLocationSettingsDialog(int cases) {
    	//cases == 1, problem retrieving location
    	//cases == 2, current location is out of desired radius
    	
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        
        switch(cases) {
        case 1:
        	alertDialog.setTitle("Problem Retrieving Location");
        	alertDialog.setMessage("There is a problem when retrieving your current location. " +
        			"Please turn on your WiFi/Mobile Network or GPS.");
        	break;
        case 2:
        	alertDialog.setTitle("Fail to Check-In/Out");
        	alertDialog.setMessage("Your current location is outside of the desired radius. " +
        			"Current location accuracy maybe the cause of this. " +
        			"Would you like to turn on your WiFi/Mobile Network or GPS to improve accuracy?");
        	break;
        }
  
        // On pressing Settings button
        alertDialog.setPositiveButton("WiFi Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        
     // On pressing Settings button
        alertDialog.setNeutralButton("GPS Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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

	@Override
	public void onLocationChanged(Location arg0) {
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}
	
	private class GetLocation extends AsyncTask<Void, Void, Location> {
		
		private View view;
		
		public GetLocation(View v) {
			view = v;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loading.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Location doInBackground(Void...voids) {
			LocationManager lm = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
			
			Location loc1 = null, loc2 = null;
			
	        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

	        boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	        if (!isGPSEnabled && !isNetworkEnabled) {
	        	return null;
	        } else {
	            if (isNetworkEnabled) {
	                lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, CheckActivity.this, Looper.getMainLooper());
	                if (lm != null) {
	                    loc1 = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	                }
	            }

	            if (isGPSEnabled) {
	            	lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, CheckActivity.this, Looper.getMainLooper());
	                if (lm != null) {
	                    loc2 = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                }
	            }
	            
	            if(loc1 != null && loc2 != null) {
	            	if(loc1.getAccuracy() < loc2.getAccuracy()) {
	            		return loc2;
	            	} else {
	            		return loc1;
	            	}
	            } else if(loc1 != null) {
	            	return loc1;
	            } else if(loc2 != null) {
	            	return loc2;
	            } else {
	            	return null;
	            }
	        }
		}
		
		@Override
		protected void onPostExecute(Location l) {
			super.onPostExecute(l);
			processLocation(view, l);
		}
	}
	
	private class ConnectActivity extends AsyncTask<String, Void, JSONObject> {
		
		private final String CHARSET = "UTF-8";
		
		private View view;
		
		private Location loc;
		
		public ConnectActivity(View v, Location l) {
			view = v;
			loc = l;
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
			if(isNetworkAvailable()) {
		    	OutputStream output = null;
				JSONObject obj = null;
				HashMap<String, String> error = new HashMap<String, String>();
				
				try {
					URLConnection connection = new URL(params[0]).openConnection();
					connection.setConnectTimeout(10000);
		    		connection.setDoInput(true);
		    		connection.setDoOutput(true);
		    		connection.setRequestProperty("Accept-Charset", CHARSET);
		            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
			    	output = connection.getOutputStream();
			    	
		            String query = String.format("access_token=%s&latitude=%s&longitude=%s",
		            		URLEncoder.encode(params[1], CHARSET),
		            		URLEncoder.encode(String.format("%.20f", loc.getLatitude()), CHARSET),
		            		URLEncoder.encode(String.format("%.20f", loc.getLongitude()), CHARSET));
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
			loading.setVisibility(View.GONE);
			processResponse(view, obj, loc);
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
