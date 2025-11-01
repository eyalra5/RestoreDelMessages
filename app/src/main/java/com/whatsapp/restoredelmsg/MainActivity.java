package com.whatsapp.restoredelmsg;

import static com.whatsapp.restoredelmsg.monitor.WhatsAppMonitorService.CHANNEL_ID;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.whatsapp.restoredelmsg.data.AppDatabase;
import com.whatsapp.restoredelmsg.monitor.WhatsAppMonitorService;
import com.whatsapp.restoredelmsg.ui.MessageAdapter;
import com.whatsapp.restoredelmsg.ui.MessageViewModel;

import org.jspecify.annotations.NonNull;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "restoredelmsg.Main";
    private MessageViewModel viewModel;
//    private static AppDatabase notifiedMsgDBInstance;
    private MessageAdapter adapter = null;

    public MessageDBUtils messageDBUtils = null;

    public static com.com.whatsapp.restoredelmsg.data.MessageDao DAO_DEL;
    public static com.com.whatsapp.restoredelmsg.data.MessageDao DAO_UNHANDLED;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button openSettingsButton = findViewById(R.id.openSettingsButton);

        adapter = new MessageAdapter();
        viewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        viewModel.getAllMessages().observe(this, adapter::setMessages);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        messageDBUtils = new MessageDBUtils();
        DAO_DEL = AppDatabase.getDBInstance(this, "DELETED_MSG1").messageDao();
        DAO_UNHANDLED = AppDatabase.getDBInstance(this, "UNHANDLED_MSG1").messageDao();

        openSettingsButton.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });
        if (!isNotificationServiceEnabled()) {
            Toast.makeText(this, "Please enable notification access", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }

//        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//        startActivity(intent);

//        askNotificationPermission();

//        Intent serviceIntent = new Intent(this, WhatsAppMonitorService.class);
//        ContextCompat.startForegroundService(this, serviceIntent);

//        AppDatabase messages = AppDatabase.getDBInstance(this, "DELETED_MSG11");
//        List<MessageEntity> msgs = messages.messageDao().getAllMessages();
//        if (!msgs.isEmpty()){
//            Log.d(TAG, "send:" + msgs.getFirst().sender +
//                    " text: " + msgs.getFirst().text);
//
//            adapter.setMessages(msgs);
//        }
//        List<MessageEntity> messages = adapter.getMessages();
//        for ( int ind = 0; ind < messages.size(); ind++) {
//            allUnhandledMessages.messageDao().insert(messages.get(ind));
//        }

//        notifiedMsgDBInstance = AppDatabase.getDBInstance(this, "NOTIFIED_MSG");
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
    }

    public boolean isNotificationActive(Context context, int notificationId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            for (StatusBarNotification sbn : manager.getActiveNotifications()) {
                if (sbn.getId() == notificationId) {
                    return true; // ✅ Found active notification
                }
            }
//        }
        return false; // ❌ Not active
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(pkgName);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == 100) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // ✅ Permission granted — now you can show notifications
////                showNotification("Welcome", "Notifications are enabled!");
//                showUserNotification("Welcome", "Notifications from main activity are enabled!");
//            } else {
//                // ❌ Permission denied — handle gracefully
//                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    private void showUserNotification(String title, String text) {
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(text)
//                .setSmallIcon(R.drawable.ic_whatsapp)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true)
//                .build();
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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

//    public void showNotification(String title, String message) {
//        String channelId = "my_channel_id"; // same as above
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(R.drawable.ic_launcher_foreground)  // required
//                .setContentTitle(title)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true);
//
//        // (Optional) Tap action – opens an activity
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                this,
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//        builder.setContentIntent(pendingIntent);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(
//                    this,
//                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
//                    100 // request code
//            );
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        notificationManager.notify(1001, builder.build()); // 1001 = notification ID
//    }

//    private void askNotificationPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
//                    != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(
//                        this,
//                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
//                        100 // request code
//                );
//            }
//        }
//    }
//    @Override
//    protected void onDestroy() {
//        /* Since deleted messages can be notify while this APP is on,
//            save the known messages to verify on NEXT enabling
//         */
//
//        AppDatabase allUnhandledMessages = AppDatabase.getDBInstance(this, "UNHANDLED_MSG1");
//
////        List<MessageEntity> messages = adapter.getMessages();
////        for ( int ind = 0; ind < messages.size(); ind++) {
////            MessageEntity msg = messages.get(ind);
////            allUnhandledMessages.messageDao().insert(msg);
////        }
//        super.onDestroy();
//    }
}
