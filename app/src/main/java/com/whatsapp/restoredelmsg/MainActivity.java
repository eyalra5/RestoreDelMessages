package com.whatsapp.restoredelmsg;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.whatsapp.restoredelmsg.data.AppDatabase;
import com.whatsapp.restoredelmsg.data.MessageEntity;
import com.whatsapp.restoredelmsg.ui.MessageAdapter;
import com.whatsapp.restoredelmsg.ui.DeletedMessageViewModel;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "restoredelmsg.Main";
    public static final String CHANNEL_ID = "whatsapp_db_channel";
    private DeletedMessageViewModel viewModel;
    private MessageAdapter adapter = null;
    public MessageDBUtils messageDBUtils = null;
    private int _lastPotentialMsgDeletedNum = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button openSettingsButton = findViewById(R.id.openSettingsButton);

        Button clearNotifyMessageFromDBButton = findViewById(R.id.clearNotifyMessageFromDBButton);
        Button clearDeletedMessageFromDBButton = findViewById(R.id.clearDeletedMessageFromDBButton);

        adapter = new MessageAdapter();
        viewModel = new ViewModelProvider(this).get(DeletedMessageViewModel.class);
        viewModel.getAllMessages().observe(this, adapter::setMessages);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        messageDBUtils = new MessageDBUtils();
        MessageDBUtils.DAO_DEL = AppDatabase.getDBInstance(this, "DELETED_MSG1").messageDao();
        MessageDBUtils.DAO_UNHANDLED = AppDatabase.getDBInstance(this, "UNHANDLED_MSG1").messageDao();

        openSettingsButton.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });
        if (!isNotificationServiceEnabled()) {
            Toast.makeText(this, "Please enable notification access", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }

        askNotificationPermission();
        createNotificationChannel();

        clearNotifyMessageFromDBButton.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                for (MessageEntity m : MessageDBUtils.DAO_UNHANDLED.getAllMessagesList()) {
                    MessageDBUtils.DAO_UNHANDLED.delete(m);
                    WhatsAppNotificationListener.deleteViewSize = 0;
                }
            });
        });

        clearDeletedMessageFromDBButton.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                for (MessageEntity m : MessageDBUtils.DAO_DEL.getAllMessagesList()) {
                    MessageDBUtils.DAO_DEL.delete(m);
                }
            });
        });

        // You can start your message-listening logic here (e.g., via NotificationListenerService)
        startBackgroundTask();
    }

    private void startBackgroundTask() {
        // Example: simulate background check every few seconds
        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(10000);
                    Log.d(TAG, "monitor service up");
                    if (!isNotificationActive() || WhatsAppNotificationListener.deleteViewSize != _lastPotentialMsgDeletedNum) {
                        _lastPotentialMsgDeletedNum = WhatsAppNotificationListener.deleteViewSize;
                        showUserNotification("BluBlu", "Keep monitor whatsApp! (" +
                                _lastPotentialMsgDeletedNum +
                                " messages been restored)");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean isNotificationActive() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            for (StatusBarNotification sbn : manager.getActiveNotifications()) {
                if (sbn.getPackageName().equals(getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(pkgName);
    }


    private void showUserNotification(String title, String text) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_whatsapp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), notification);
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100 // request code
                );
            }
        }
    }
    @Override
    protected void onDestroy() {
        /* Since deleted messages can be notify while this APP is on,
            save the known messages to verify on NEXT enabling
         */
        for (MessageEntity m : MessageDBUtils.DAO_UNHANDLED.getAllMessagesList()) {
            MessageDBUtils.DAO_UNHANDLED.delete(m);
        }
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    MessageDBUtils.CHANNEL_ID,
                    "WhatsApp Monitor Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Monitors WhatsApp messages and alerts user.");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
