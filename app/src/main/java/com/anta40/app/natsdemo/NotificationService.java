package com.anta40.app.natsdemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.nio.charset.StandardCharsets;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Duration;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class NotificationService extends Service {

    private static final String TAG_BOOT_EXECUTE_SERVICE = "BOOT_BROADCAST_SERVICE";
    public static boolean natsSvcIsRunning = false;
    public static Connection nc;

    public NotificationService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d("NATS", "NotificationService onCreate() method.");
        super.onCreate();
    }

    private Bundle bundleddata;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread t = new Thread(){
            public void run(){
                try {
                    Options.Builder oBuilder =
                            new Options.Builder()
                                    .server(Constants.NATS_BASE_URL)
                                    .reconnectWait(Duration.ofMinutes(3)).maxReconnects(-1);
                    oBuilder.connectionListener(new ConnectionListener() {
                        @Override
                        public void connectionEvent(Connection conn, Events type) {
                            if (type == Events.DISCONNECTED || type == Events.CLOSED) {
                                natsSvcIsRunning = false;
                                Log.d("NATS","nats server connection "+type.toString());
                            } else {
                                natsSvcIsRunning = true;
                                Log.d("NATS","nats server connection "+type.toString());
                            }
                        }
                    });
                    Options o = oBuilder.build();

                    nc = Nats.connect(o);
                    Subscription sub = nc.subscribe("req_auth");
                    Log.d("NATS","subscribing to "+sub.getSubject());
                    Message msg = sub.nextMessage(Duration.ZERO);
                    String response = new String(msg.getData(), StandardCharsets.UTF_8);
                    Log.d("NATS","got request "+response+" from "+msg.getReplyTo());

                    String[] c = response.split("\\|");

                    bundleddata = new Bundle();
                    bundleddata.putString("data", response);

                    sendNotification();

//                        nc.flush(Duration.ZERO);
                    nc.close();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        };
        t.start();

        return super.onStartCommand(intent,flags,startId);
    }

    private void sendNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Another Test Channel";
            String description = "Yet another channel for testing purpose";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("another-test-channel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundleddata);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 654731, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "test-channel")
               // .setSmallIcon(R.drawable.app_icon)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Test Request")
                .setContentText("Test Request")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Test Request"))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(654731, notificationBuilder.build());
    }
}

