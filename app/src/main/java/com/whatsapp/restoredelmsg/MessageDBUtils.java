    package com.whatsapp.restoredelmsg;

import static com.whatsapp.restoredelmsg.WhatsAppNotificationListener.TAG;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.whatsapp.restoredelmsg.data.MessageEntity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MessageDBUtils {

    public static final String CHANNEL_ID = "whatsapp_db_channel";

    List<MessageEntity> getAllNotifiedCheckedMsgsOfSenderFromDB(Map<String, Integer> senderToMsgCountMap,
                                                                Map<String, Integer> sendersBulkNamesMap,
                                                                Context context){
//    List<MessageEntity> getAllNotifiedCheckedMsgsOfSenderFromDB(Map<String, Integer> senderToMsgCountMap){
        List<MessageEntity> messageEntityList = new ArrayList<>();
        List<MessageEntity> allMessagesList = MainActivity.DAO_UNHANDLED.getAllMessagesList();

        if (null == allMessagesList) return messageEntityList;

//        Map<String, Integer> senderToMsgCountMapFromDB = new HashMap<>();
//        for (MessageEntity msgBySender : allMessagesList) {
//            int currentCnt = senderToMsgCountMapFromDB.getOrDefault(msgBySender.sender, 0);
//            senderToMsgCountMapFromDB.put(msgBySender.sender, ++currentCnt);
//        }

        for (String senderWithMsg: senderToMsgCountMap.keySet()) {
//        for (Map.Entry<String, Integer>  entry : senderToMsgCountMap.entrySet()) {
            String deletedSender = "";
            try {
                if (sendersBulkNamesMap.getOrDefault(senderWithMsg, 0 ) == 0) {
                    deletedSender = senderWithMsg;
                }
            } catch (Exception e) {
                Log.d(TAG, "Sender search for: " + senderWithMsg + ", " + e.getMessage());
            }
//            String sender = entry.getKey();
//            int numOfMessages = entry.getValue();

            //TODO: since all messages are suspected as deleted.
            // maybe later the specific sender would found to reduce viewed message that wasn't del.
//            int senderToMsgCount = senderToMsgCountMap.getOrDefault(sender,0 );
//            if ((senderToMsgCount == 1) && numOfMessages < senderToMsgCount) {
                for (MessageEntity messageEntity : allMessagesList) {
                    if (messageEntity.sender.equals(deletedSender) || !isSenderKnown(deletedSender) &&
                            !isDeletionDurationPassed(messageEntity)) {
                        messageEntityList.add(messageEntity);
                        Log.d(TAG, "id: " + messageEntity.id +
                    ", sender:" + messageEntity.sender + ", text:" + messageEntity.text);
//                        showUserNotification("WhatsApp monitor alert",
//                                deletedSender + " may deleted some messages",
//                                context);
                    }
                    else {
                         //Do nothing;
                    }
                }
//            }
        }
        return messageEntityList;
    }

    private boolean isGroupSender(String sender) {
        String[] words = sender.split("\\s+");
        return Arrays.asList(words).contains(":");
    }

    private String extractSenderOfSenderGroup(String sender) {
        String[] words = sender.split("\\s+");
        return words.length> 0 ? words[0] : "";
    }

    private boolean isDeletionDurationPassed(MessageEntity messageEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return messageEntity.timestamp + Duration.ofHours(1).toMillis() < //heuristic time
                    System.currentTimeMillis();
        }
        return false;
    }

    private boolean isSenderKnown(String sender) {
        // means no sender been total removed, but single message of a sender and the sender at least one message on notification.
        return !sender.equals("");
    }

//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "WhatsApp Monitor Service",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            channel.setDescription("Monitors WhatsApp messages and alerts user.");
//
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            if (manager != null) manager.createNotificationChannel(channel);
//        }
//    }
//
//    private void ss() {
//        // Keep service running in foreground with a low-priority notification
//        Notification ongoingNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("BluBlu")
//                .setContentText("Monitoring WhatsApp messages...")
//                .setSmallIcon(R.drawable.ic_whatsapp)
//                .setPriority(NotificationCompat.PRIORITY_MIN)
//                .setOngoing(true)
//                .build();
//
//        startForeground(FOREGROUND_ID, ongoingNotification);
//    }

    private void showUserNotification(String title, String text, Context context) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_whatsapp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), notification);
    }
}
