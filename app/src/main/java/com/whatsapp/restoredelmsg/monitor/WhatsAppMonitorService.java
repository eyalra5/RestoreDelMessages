package com.whatsapp.restoredelmsg.monitor;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.whatsapp.restoredelmsg.R;

public abstract class WhatsAppMonitorService extends Service {

    public static final String CHANNEL_ID = "whatsapp_monitor_channel";
    private static final int FOREGROUND_ID = 1001;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Keep service running in foreground with a low-priority notification
        Notification ongoingNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BluBlu")
                .setContentText("Monitoring WhatsApp messages...")
                .setSmallIcon(R.drawable.ic_whatsapp)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build();

        startForeground(FOREGROUND_ID, ongoingNotification);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // restart if killed
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "WhatsApp Monitor Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Monitors WhatsApp messages and alerts user.");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
