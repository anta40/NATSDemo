package com.anta40.app.natsdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.nats.client.Connection;
import io.nats.client.Duration;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class MainActivity extends AppCompatActivity {

    private Connection nc;
    private MaterialButton btnPublish, btnSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPublish = (MaterialButton)  findViewById(R.id.btn_nats_publish);
        btnSubscribe = (MaterialButton) findViewById(R.id.btn_nats_subscribe);

        Options.Builder oBuilder =
                new Options.Builder()
                        .server(Constants.NATS_BASE_URL)
                        .reconnectWait(Duration.ofMinutes(3)).maxReconnects(-1);
        Options opts = oBuilder.build();

        try {
            nc = Nats.connect(opts);
        }
        catch (InterruptedException ie){
            Log.d("NATS", "InterruptedException at MainActivity: "+ie.getMessage());
        }
        catch (IOException ioe){
            Log.d("NATS", "IOException at MainActivity: "+ ioe.getMessage());
        }
        if (!NotificationService.natsSvcIsRunning){
            Intent start = new Intent(MainActivity.this.getApplicationContext(), NotificationService.class);
            start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            start.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            MainActivity.this.startService(start);
        }

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("NATS", "Trying to publish...");
                nc.publish("rcpt", "msg".getBytes(StandardCharsets.UTF_8));
            }
        });

        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Subscription sub = nc.subscribe("topic");
                Log.d("NATS","subscribing to "+sub.getSubject());
                Message msg = null;

                try {
                    msg = sub.nextMessage(Duration.ZERO);
                    String response = new String(msg.getData(), StandardCharsets.UTF_8);
                    Log.d("NATS","got request "+response+" from "+msg.getReplyTo());
                }
                catch (InterruptedException ie){
                    Log.d("NATS", "InterruptedException: "+ie.getMessage());
                }
            }
        });
    }
}