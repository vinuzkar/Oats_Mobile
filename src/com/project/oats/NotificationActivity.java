package com.project.oats;

import java.util.Calendar;
import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class NotificationActivity extends FragmentActivity {
	
	private CheckBox checkInBox, checkOutBox;
	
	private Button repeatingDay, checkInTime, checkOutTime;
	
	private Calendar checkIn, checkOut;
	
	private String selectedDays;
	
	private TimePickerFragment timePicker;
	
	private DayPickerFragment dayPicker;
	
	private OnCheckedChangeListener listener;
	
	private boolean is24h; 
	
	private final String PREFS_NAME = "OatsPref";
	
	public static final int CHECK_IN = 1;
	
	public static final int CHECK_OUT = 2;
	
	public static final int REPEATING_DAY = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if(getDeviceDefaultOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.activity_notification_port);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setContentView(R.layout.activity_notification_land);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		repeatingDay = (Button)findViewById(R.id.repeating_day);
		checkInBox = (CheckBox)findViewById(R.id.check_in_notification);
		checkOutBox = (CheckBox)findViewById(R.id.check_out_notification);
		checkInTime = (Button)findViewById(R.id.check_in_time);
		checkOutTime = (Button)findViewById(R.id.check_out_time);
		
		timePicker = new TimePickerFragment();
		timePicker.setCaller(this);
		dayPicker = new DayPickerFragment();
		dayPicker.setCaller(this);
	    
	    listener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton view, boolean isChecked) {
				Intent service = new Intent(NotificationActivity.this, NotificationService.class);
				int id = view.getId();
				if(isChecked) {
					switch(id) {
	                case R.id.check_in_notification:
	                	checkIn = incrementDateIfBefore(checkIn);
	                	setNotificationTime(CHECK_IN, checkIn);
	                	break;
	                case R.id.check_out_notification:
	                	checkOut = incrementDateIfBefore(checkOut);
	                	setNotificationTime(CHECK_OUT, checkOut);
	                	break;
	                }
				} else {
	                service.setAction(NotificationService.DELETE);
	                switch(id) {
	                case R.id.check_in_notification:
	                	service.putExtra("notificationId", NotificationService.CHECKIN_ID);
	                	checkIn = Calendar.getInstance();
	                	setTimeButtonText(CHECK_IN, checkIn, false);
	                	break;
	                case R.id.check_out_notification:
	                	service.putExtra("notificationId", NotificationService.CHECKOUT_ID);
	                	checkOut = Calendar.getInstance();
	                	setTimeButtonText(CHECK_OUT, checkOut, false);
	                	break;
	                }
				}
				startService(service);
			}
	    	
	    };
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
	
	public void showTimePicker(View view) {
		int id = view.getId();
		timePicker.setButton((Button)view);
		timePicker.setIs24H(is24h);
        if(id == R.id.check_in_time) {
        	timePicker.setTime(checkIn);
        } else if(id == R.id.check_out_time) {
        	timePicker.setTime(checkOut);
        }
        timePicker.show(getSupportFragmentManager(), "timePicker");
    }
	
	public void showDayPicker(View view) {
		dayPicker.setIsChecked(selectedDays);
        dayPicker.show(getSupportFragmentManager(), "timePicker");
	}
	
	private Calendar incrementDateIfBefore(Calendar c) {
		Calendar temp = c;
		temp.set(Calendar.SECOND, 0);
		temp.set(Calendar.MILLISECOND, 0);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if(temp.before(cal) || temp.equals(cal)) {
			temp.add(Calendar.DATE, 1);
		}
		return temp;
	}
	
	private void setNotificationTime(int id, Calendar c) {
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        Intent service = new Intent(NotificationActivity.this, NotificationService.class);
    	service.setAction(NotificationService.CREATE);
        
        if(id == CHECK_IN) {
        	editor.putLong("check_in_time", c.getTimeInMillis());
        	service.putExtra("notificationId", NotificationService.CHECKIN_ID);
        } else if(id == CHECK_OUT) {
        	editor.putLong("check_out_time", c.getTimeInMillis());
        	service.putExtra("notificationId", NotificationService.CHECKOUT_ID);
        }
        
    	editor.commit();
    	startService(service);
	}
	
	public void setCheckInTime(Calendar c) {
		checkIn = incrementDateIfBefore(c);
		if(checkInBox.isChecked()) {
			setNotificationTime(CHECK_IN, checkIn);
		}
	}
	
	public void setCheckOutTime(Calendar c) {
		checkOut = incrementDateIfBefore(c);
		if(checkOutBox.isChecked()) {
			setNotificationTime(CHECK_OUT, checkOut);
		}
	}
	
	public void setButtonText(int id, CharSequence text) {
		Button b = (Button)findViewById(id);
		b.setText(text);
	}
	
	public void setTimeButtonText(int id, Calendar c, boolean is24h) {
		if(is24h) {
			int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        	switch(id) {
			case CHECK_IN:
		    	checkInTime.setText(Html.fromHtml("<small>Check-in notification:</small> " +
			    		String.format("%02d", hourOfDay) + ':' +
			    		String.format("%02d", checkIn.get(Calendar.MINUTE))));
		    	break;
			case CHECK_OUT:
				checkOutTime.setText(Html.fromHtml("<small>Check-out notification:</small> " +
	            		String.format("%02d", hourOfDay) + ':' +
	            		String.format("%02d", checkOut.get(Calendar.MINUTE))));
				break;
			}
        } else {
        	int hourOfDay = c.get(Calendar.HOUR);
            if(hourOfDay == 0) {
            	hourOfDay = 12;
            }
			switch(id) {
			case CHECK_IN:
		    	checkInTime.setText(Html.fromHtml("<small>Check-in notification:</small> " +
			    		hourOfDay + ':' + String.format("%02d", checkIn.get(Calendar.MINUTE)) +
			    		' ' + checkIn.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US)));
		    	break;
			case CHECK_OUT:
				checkOutTime.setText(Html.fromHtml("<small>Check-out notification:</small> " +
	            		hourOfDay + ':' + String.format("%02d", checkOut.get(Calendar.MINUTE)) +
	            		' ' + checkOut.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US)));
				break;
			}
        }
	}
	
	public void setRepeatingDay(boolean daysRepeating[]) {
		String days = "";
		for(int i = 0; i < 7; i++) {
			if(daysRepeating[i]) {
				days += '1';
			} else {
				days += '0';
			}
		}
		selectedDays = days;
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("repeating_day", days);
        editor.commit();
	}
	
	public void setRepeatingDayButton(String daysRepeating) {
		String days = "";
		for(int i = 0; i < 7; i++) {
			if(daysRepeating.charAt(i) == '1') {
				if(days.length() > 0) {
					switch(i) {
					case 0:
						days += " Sun"; break;
					case 1:
						days += ", Mon"; break;
					case 2:
						days += ", Tue"; break;
					case 3:
						days += ", Wed"; break;
					case 4:
						days += ", Thu"; break;
					case 5:
						days += ", Fri"; break;
					case 6:
						days += ", Sat"; break;
					}
				} else {
					switch(i) {
					case 0:
						days += " Sun"; break;
					case 1:
						days += " Mon"; break;
					case 2:
						days += " Tue"; break;
					case 3:
						days += " Wed"; break;
					case 4:
						days += " Thu"; break;
					case 5:
						days += " Fri"; break;
					case 6:
						days += " Sat"; break;
					}
				}
			}
		}
		if(days.length() == 0) {
			days = " Never";
		}
		repeatingDay.setText(Html.fromHtml("<big>Repeat</big><br />" + days));
	}
	
	public void setRepeatingDayButton(boolean daysRepeating[]) {
		String days = "";
		for(int i = 0; i < 7; i++) {
			if(daysRepeating[i]) {
				if(days.length() > 0) {
					switch(i) {
					case 0:
						days += " Sun"; break;
					case 1:
						days += ", Mon"; break;
					case 2:
						days += ", Tue"; break;
					case 3:
						days += ", Wed"; break;
					case 4:
						days += ", Thu"; break;
					case 5:
						days += ", Fri"; break;
					case 6:
						days += ", Sat"; break;
					}
				} else {
					switch(i) {
					case 0:
						days += " Sun"; break;
					case 1:
						days += " Mon"; break;
					case 2:
						days += " Tue"; break;
					case 3:
						days += " Wed"; break;
					case 4:
						days += " Thu"; break;
					case 5:
						days += " Fri"; break;
					case 6:
						days += " Sat"; break;
					}
				}
			}
		}
		if(days.length() == 0) {
			days = " Never";
		}
		repeatingDay.setText(Html.fromHtml("<big>Repeat</big><br />" + days));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_notification_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings_menu:
			startActivity(new Intent(this, NotificationSettingsActivity.class));
			return true;
		case R.id.help_menu:
			DialogFragment dialog = new HelpDialogFragment();
	        dialog.show(getSupportFragmentManager(), "HelpDialogFragment");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		checkInBox.setOnCheckedChangeListener(null);
		checkOutBox.setOnCheckedChangeListener(null);
	}

	@Override
	protected void onResume() {
        super.onResume();
        is24h = OatsMobile.is24hFormat();
        
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        
        long l = pref.getLong("check_in_time", NotificationService.NULL_TIME);
        if(l >= 0) {
        	c1.setTimeInMillis(l);
        	checkInBox.setChecked(true);
        } else {
        	c1.set(Calendar.SECOND, 0);
			c1.set(Calendar.MILLISECOND, 0);
        }
        
        l = pref.getLong("check_out_time", NotificationService.NULL_TIME);
        if(l >= 0) {
        	c2.setTimeInMillis(l);
        	checkOutBox.setChecked(true);
        } else {
        	c2.set(Calendar.SECOND, 0);
			c2.set(Calendar.MILLISECOND, 0);
        }
        
        checkInBox.setOnCheckedChangeListener(listener);
		checkOutBox.setOnCheckedChangeListener(listener);
        checkIn = c1;
        checkOut = c2;
        selectedDays = pref.getString("repeating_day", "0000000");
        
        setRepeatingDayButton(selectedDays);
        setTimeButtonText(CHECK_IN, checkIn, is24h);
        setTimeButtonText(CHECK_OUT, checkOut, is24h);
    }
}
