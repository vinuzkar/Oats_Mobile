package com.project.oats;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

public class NotificationReceiver extends BroadcastReceiver {
	
	private final String PREFS_NAME = "OatsPref";

	@Override
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, 0);
		String selectedDays = pref.getString("repeating_day", "0000000");
		Calendar c = Calendar.getInstance();
		
		if(selectedDays.charAt(c.get(Calendar.DAY_OF_WEEK) - 1) == '1') {
			long id = intent.getLongExtra("id", 0);
	        String msg = intent.getStringExtra("msg");
	        
	        Notification n;
	        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, CheckActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
	        
	        if (Build.VERSION.SDK_INT >= 11) {
		        Notification.Builder builder = new Notification.Builder(context);
		        builder.addAction(R.drawable.ic_launcher, "Oats Reminder", pi);
		        builder.setTicker(msg);
		        builder.setContentText(msg);
		        n = builder.build();
	        } else {
	        	n = new Notification(R.drawable.ic_launcher, msg, System.currentTimeMillis());
	        	n.setLatestEventInfo(context, "Oats Reminder", msg, pi);
	        }
	        
	        // TODO check user preferences
	        n.sound = Uri.parse(OatsMobile.getRingtone());
	        n.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	        n.flags |= Notification.FLAG_AUTO_CANCEL;      
	         
	        NotificationManager nm = (NotificationManager)
	                                    context.getSystemService(Context.NOTIFICATION_SERVICE);
	        nm.notify((int)id, n);
		}
    }
}
