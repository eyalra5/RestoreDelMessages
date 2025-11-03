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
//
        // You can start your message-listening logic here (e.g., via NotificationListenerService)
//        startBackgroundTask();
    }
//
//    private void startBackgroundTask() {
//        // Example: simulate background check every few seconds
//        new Thread(() -> {
//            try {
//                while (true) {
//                    Thread.sleep(5000); // every 5 seconds
//
//                    Log.d(WhatsAppNotificationListener_.TAG, "monitor service up");
////                    WhatsAppNotificationListener.
////                    if (!isNotificationActive(FOREGROUND_ID)) {
////                        showUserNotification("BluBlu", "Keep monitor whatsApp!");
////                    }
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // restart if killed
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        return null; // not a bound service
//    }
//
//    private void showUserNotification(String title, String text) {
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(text)
//                .setSmallIcon(R.drawable.ic_whatsapp)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true)
//                .build();
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
//                != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), notification);
//    }
//
//    private boolean isNotificationActive(int notificationId) {
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            for (StatusBarNotification sbn : manager.getActiveNotifications()) {
//                if (sbn.getPackageName().equals(getPackageName())) {
////                if (sbn.getId() == notificationId) {
//                    return true; // ✅ Found active notification
//                }
//            }
//        }
//        return false; // ❌ Not active
//    }
//
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
