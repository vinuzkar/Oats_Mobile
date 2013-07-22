package com.project.oats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationSetter extends BroadcastReceiver {

	@Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, NotificationService.class);
        service.setAction(NotificationService.CREATE);
        context.startService(service);
    }

}
