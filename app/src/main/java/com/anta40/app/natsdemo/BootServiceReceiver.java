package com.anta40.app.natsdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootServiceReceiver extends BroadcastReceiver {
    private static final String TAG_BOOT_BROADCAST_RECEIVER = "NATS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d(TAG_BOOT_BROADCAST_RECEIVER, action);

        if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Intent i = new Intent(context, NotificationService.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startService(i);
        }
    }
}
