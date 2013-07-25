package com.project.oats;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.System;

public class OatsMobile extends Application {
	
	public static SharedPreferences sp;
	
	private static final String RINGTONE_PREF = "ringtone_pref";
	
	private static final String TIME_FORMAT = "time_format";
	
	@Override
    public void onCreate() {
        super.onCreate();
         
        PreferenceManager.setDefaultValues(this, R.xml.notification_settings, false);
        sp = PreferenceManager.getDefaultSharedPreferences(this);   
    }
     
    public static String getRingtone() {
        return sp.getString(RINGTONE_PREF, System.DEFAULT_NOTIFICATION_URI.toString());
    }
    
    public static boolean is24hFormat() {
        return sp.getBoolean(TIME_FORMAT, false);
    }
}
