package com.project.oats;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

public class NotificationService extends IntentService {
	
	private IntentFilter matcher;
	
	public static final String CREATE = "Create";
    public static final String DELETE = "Delete";
    public static final int NULL_ID = 0;
    public static final int CHECKIN_ID = 1;
    public static final int CHECKOUT_ID = 2;
    public static final long NULL_TIME = -1;
    private final String PREFS_NAME = "OatsPref";
    private static final String PARENT_NAME = "Parent";
 
    public NotificationService() {
        super(PARENT_NAME);
        matcher = new IntentFilter();
        matcher.addAction(CREATE);
        matcher.addAction(DELETE);
    }
 
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        int notificationId = intent.getIntExtra("notificationId", NULL_ID);
         
        if (matcher.matchAction(action)) {         
            execute(action, notificationId);
        }
    }
    
    private void execute(String action, int notificationId) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        Intent i = new Intent(this, NotificationReceiver.class);
        PendingIntent pi;
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        long time;
        switch(notificationId) {
	        case CHECKIN_ID:
	        	i.putExtra("id", CHECKIN_ID);
	        	i.putExtra("msg", "Oats reminder for you to check in");
	        	pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
	        	
	        	time = pref.getLong("check_in_time", NULL_TIME);
	            if (CREATE.equals(action) && time >= 0) {
	                am.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pi);
	            }
	            if (DELETE.equals(action)) {
	                am.cancel(pi);
	                editor.putLong("check_in_time", NULL_TIME);
	                editor.commit();
	            }
	        	break;
	        case CHECKOUT_ID:
	        	i.putExtra("id", CHECKOUT_ID);
	        	i.putExtra("msg", "Oats reminder for you to check out");
	        	pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
	        	
	        	time = pref.getLong("check_out_time", NULL_TIME);
	            if (CREATE.equals(action) && time >= 0) {
	                am.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pi);
	            }
	            if (DELETE.equals(action)) {
	                am.cancel(pi);
	                editor.putLong("check_out_time", NULL_TIME);
	                editor.commit();
	            }
	        	break;
	        default:
	        	i.putExtra("id", CHECKIN_ID);
	        	i.putExtra("msg", "Oats reminder for you to check in");
	        	pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
	        	
	        	Intent i2 = new Intent(this, NotificationReceiver.class);
	        	i2.putExtra("id", CHECKOUT_ID);
	        	i2.putExtra("msg", "Oats reminder for you to check out");
	        	PendingIntent pi2 = PendingIntent.getBroadcast(this, 0, i2, PendingIntent.FLAG_UPDATE_CURRENT);
	        	
	            if (CREATE.equals(action)) {
	            	if((time = pref.getLong("check_in_time", NULL_TIME)) >= 0) {
	            		am.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pi);
	            	}
	            	if((time = pref.getLong("check_out_time", NULL_TIME)) >= 0) {
	            		am.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pi2);
	            	}
	            }
	            if (DELETE.equals(action)) {
	                am.cancel(pi);
	                editor.putLong("check_in_time", NULL_TIME);
	                editor.commit();
	                
	                am.cancel(pi2);
	                editor.putLong("check_out_time", NULL_TIME);
	                editor.commit();
	            }
        }
    }
}
