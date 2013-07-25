package com.project.oats;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public class NotificationReceiver extends BroadcastReceiver {

	@Override
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {
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
