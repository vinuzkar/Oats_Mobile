package com.project.oats;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.project.oats.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class PerformanceActivity extends Activity {
	
	private Button from, to;
	
	private XYPlot graph;
	
	private int mDate, mMonth, mYear;
	
	private int datePickerId;
	
	private OnDateSetListener dateSetListener;
	
	private Date fromDate, toDate;
	
	private final long ONE_WEEK = 604800000;
	
	private final int DATEPICKER = 0xFFFFFF;
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

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
			setContentView(R.layout.activity_performance_port);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}/* else {
			setContentView(R.layout.activity_login_land);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}*/
		
		from = (Button)findViewById(R.id.from);
		to = (Button)findViewById(R.id.to);
		
		graph = (XYPlot)findViewById(R.id.graph);
		Number axis[] = {0};
		Number ordinat[] = {0};
		XYSeries series = new SimpleXYSeries(
                Arrays.asList(axis),
                Arrays.asList(ordinat),
                "Performance Graph");
		graph.addSeries(series, new BarFormatter(Color.argb(100, 0, 200, 0), Color.rgb(0, 80, 0)));
		graph.setDomainStepValue(3);
		graph.setTicksPerRangeLabel(3);
		
		graph.setDomainLabel("Date");
		graph.getDomainLabelWidget().pack();
		graph.setRangeLabel("Worktime");
		graph.getRangeLabelWidget().pack();
		graph.setGridPadding(15, 0, 15, 0);
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		fromDate = c.getTime();
		toDate = c.getTime();
	    from.setText(Html.fromHtml("<small>From:</small> " +
	    		c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + ' ' +
	    		c.get(Calendar.DATE) + ", " + c.get(Calendar.YEAR)));
	    to.setText(Html.fromHtml("<small>To:</small> " +
	    		c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + ' ' +
	    		c.get(Calendar.DATE) + ", " + c.get(Calendar.YEAR)));
	    
	    dateSetListener = new OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				String months[] = {"January ", "February ", "March ", "April ", "May ", "June ",
						"July ", "August ", "September ", "October ", "November ", "December "
				};
				
				Button b = (Button)findViewById(datePickerId);
				if(datePickerId == R.id.from) {
					b.setText(Html.fromHtml("<small>From:</small> " +
							months[view.getMonth()] + view.getDayOfMonth() + ", " + view.getYear()));
					fromDate= (new GregorianCalendar(view.getYear(), view.getMonth(), view.getDayOfMonth())).getTime();
				} else if(datePickerId == R.id.to) {
					b.setText(Html.fromHtml("<small>To:</small> " +
							months[view.getMonth()] + view.getDayOfMonth() + ", " + view.getYear()));
					toDate= (new GregorianCalendar(view.getYear(), view.getMonth(), view.getDayOfMonth())).getTime();
				}
			}
	    	
	    };

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
	
	public void showDatePicker(View view) {
        final Calendar c = Calendar.getInstance();
        mDate = c.get(Calendar.DATE);
        mMonth = c.get(Calendar.MONTH);
        mYear = c.get(Calendar.YEAR);
        
        if(((Button)view).getId() == R.id.from) {
        	datePickerId = R.id.from;
        } else if(((Button)view).getId() == R.id.to) {
        	datePickerId = R.id.to;
        }
        
        showDialog(DATEPICKER);
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DATEPICKER:
			return new DatePickerDialog(this, dateSetListener, mYear, mMonth, mDate);
		default:
			return null;
		}
	}
	
	public void onShowClicked(View view) {
		long fromEpoch = fromDate.getTime();
		long toEpoch = toDate.getTime();
		if(toEpoch - fromEpoch > 0 && toEpoch - fromEpoch <= ONE_WEEK) {
			
		} else if(toEpoch - fromEpoch <= 0) {
			showInfoDialog("Error", "The 'to' date must be later than the 'from' date.");
		} else if(toEpoch - fromEpoch > ONE_WEEK) {
			showInfoDialog("Error", "The period exceeds the limit (1 week).");
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
}
