package com.whatsapp.restoredelmsg;

import com.whatsapp.restoredelmsg.data.MessageEntity;

import android.app.Notification;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class WhatsAppNotificationListener extends NotificationListenerService {

    public static final String TAG = "WhatsAppListener";
    private final Map<String, Integer> numOfMessages = new HashMap<>() ;
    private volatile MessageEntity cachedMessageEntity = null;
    /*
    allocate memory → defaults
   ↓
superclass ctor
   ↓
field initializers run
   - messageDBUtils = new MessageDBUtils()
   ↓
constructor body
   - print "Inside constructor"

     */
    private final MessageDBUtils messageDBUtils = new MessageDBUtils();
    /*

Allocate object memory (defaults)
   ↓
Call superclass constructors
   ↓
Initialize instance fields (top to bottom)
   ↓
Run subclass constructor body

----
private final MessageDBUtils messageDBUtils;
    would set default initial: messageDBUtils=0
AND on ctor:
    messageDBUtils = new MessageDBUtils();
actully would be allocate like:
allocate memory → defaults
   ↓
superclass ctor
   ↓
(no inline field initializers)
   ↓
constructor body
   - messageDBUtils = new MessageDBUtils()
   - print "Inside constructor"
     */

//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }

//    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.d(TAG, "in onNotificationPosted()");

        if (sbn == null) return;
        String packageName = sbn.getPackageName();

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        // Only listen for WhatsApp notifications
        if (!"com.whatsapp".equals(packageName)) {
            return;
        }

        Bundle extras = notification.extras;
        if (extras == null) return;

        String sender = extras.getString(Notification.EXTRA_TITLE); // Sender name
        String text = extras.getString(Notification.EXTRA_TEXT);   // Message body

        if (isSuspectAsDeletionAction(text) || numOfMessages.size() > 1) {
            /*
            means last notification of batch - sender <count messages>.
            means use 'numOfMessages' to verify which sender deleted message
             */
            Executors.newSingleThreadExecutor().execute(() -> {
                /*
                Main thread cant access DB to avoid long lock
                 */
                List<MessageEntity> messageEntityList = messageDBUtils.searchForDeletedMessage(this,
                        numOfMessages);
                for (MessageEntity messageEntityEle : messageEntityList) {
                    insertDBSafe(MainActivity.DAO_DEL, messageEntityEle);
                    deleteEntryDBSafe(MainActivity.DAO_UNHANDLED, messageEntityEle);
                }
            });
//            createNotificationChannel();
//            showNotification( "Hello!", "This is a test notification.");

            clearPreviousCycle();
        } else if (isSeparatorMsg(sender, text)) {
            MessageEntity messageEntity = getCachedMessageEntity();
            if (messageEntity != null) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    insertDBSafe(MainActivity.DAO_UNHANDLED, messageEntity);
                });
                clearPreviousCycle();
            } else {
                /*
                'Separator' Notification means the user has been mark messages as been read.
                 */
            }
        } else if (isOneMessageInCache()) {
            prepareForDeletionUpdate(sender, text);
        } else if (!sender.equals("WhatsApp")) {
            prepareForDeletionUpdate(sender, text);
            setCachedMessageEntity(new MessageEntity(sender, text, System.currentTimeMillis()));
        } else {
            clearPreviousCycle();
            Log.d(TAG, "UNHANDLED STATE");
        }

        // Print each word in the resulting array
//        System.out.println("Words in the sentence:");
//        for (String word : words) {
//            System.out.println(word);
//        }


        // You could broadcast this to your activity if you want to display it:
        // sendBroadcast(new Intent("com.yourapp.MESSAGE_RECEIVED")
        //         .putExtra("sender", title)
        //         .putExtra("message", text));
        // Save message to database
//        if (text.)
//        Executors.newSingleThreadExecutor().execute(() -> {
//            AppDatabase.getDBInstance(this, "UNHANDLED_MSG1").messageDao().
//                    insert(new MessageEntity(sender,
//                            text,
//                            System.currentTimeMillis()));
//        });

//        Log.d(TAG, "WhatsApp Notification Received:");
//        Log.d(TAG, "Title: " + sender.replaceAll("\\p{Cf}", ""));
//        Log.d(TAG, "Text: " + text.replaceAll("\\p{Cf}", ""));
    }

//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn) {
//        // Optional: handle removed notifications
//    }

//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            String channelId = "my_channel_id";
//            String channelName = "General Notifications";
//            String channelDesc = "Notifications from my app";
//
//            NotificationChannel channel = new NotificationChannel(
//                    channelId,
//                    channelName,
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            channel.setDescription(channelDesc);
//
//            NotificationManager notificationManager =
//                    this.getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }

    private boolean isSeparatorMsg(String sender, String text) {
        String[] words = text.split("\\s+");
//        Toast.makeText(this, "WhatsApp message from: " + words[0] + ",  " +
//                        new String(sender.getBytes(StandardCharsets.UTF_8))
//                        + " → " + new String(text.getBytes(StandardCharsets.UTF_8)),
//                Toast.LENGTH_LONG).show();

        if (words.length < 3) return false;

        try {
            Integer.parseInt(words[0].
                        replaceAll("[\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]", ""));
            Integer.parseInt(words[3].
                        replaceAll("[\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]", ""));
            return sender.equals("WhatsApp");
        } catch (Exception e) {
            return false;
        }
    }

    private void clearPreviousCycle() {
        setCachedMessageEntity(null);
        numOfMessages.clear();
    }

    private boolean isOneMessageInCache() {
        MessageEntity messageEntity = getCachedMessageEntity();
        return  messageEntity != null;
    }

    private boolean isSuspectAsDeletionAction(String text) {
        return null == text;
    }

    private synchronized void setCachedMessageEntity(MessageEntity messageEntity) {
        cachedMessageEntity = messageEntity;
    }
    private synchronized MessageEntity getCachedMessageEntity() {
        return cachedMessageEntity;
    }
    
    private void prepareForDeletionUpdate(String sender, String text) {
        int numOfSenderMsgs = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            numOfSenderMsgs = numOfMessages.getOrDefault(sender, 0);
        }
        numOfMessages.put(sender, numOfSenderMsgs+1);
        Log.d(TAG,"numOfSenderMsgs:" + numOfSenderMsgs +
                ", sender:" + sender +  ", text: " + text);
    }

    private void insertDBSafe(com.com.whatsapp.restoredelmsg.data.MessageDao dao,
                              MessageEntity messageEntity) {
        boolean shouldRetry = false;
        do {
            try {
                dao.insert(messageEntity);
            } catch (SQLiteConstraintException e) {
                shouldRetry = true;
            }
        }while (shouldRetry);
    }

    private void deleteEntryDBSafe(com.com.whatsapp.restoredelmsg.data.MessageDao dao,
                                   MessageEntity messageEntity) {
        boolean shouldRetry = false;
        do {
            try {
                dao.delete(messageEntity);
            } catch (Exception e){
                shouldRetry = true;
                Log.d(TAG, "Deletion failed. may racing. exception" + e.getMessage());
            }
        }while (shouldRetry);
    }


    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.i(TAG, "Listener connected ✅");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.w(TAG, "Listener disconnected ⚠️");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        Log.d(TAG, "in onNotificationPosted()");

        if (sbn == null) return;
        String packageName = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            packageName = sbn.getPackageName();
        }

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        // Only listen for WhatsApp notifications
        if ("com.whatsapp".equals(packageName)) {
            return;
        }

        Bundle extras = notification.extras;
        if (extras == null) return;

        String sender = extras.getString(Notification.EXTRA_TITLE); // Sender name
        String text = extras.getString(Notification.EXTRA_TEXT);   // Message body

        // Optional: handle notification removal if needed
        Executors.newSingleThreadExecutor().execute(() -> {
                /*
                Main thread cant access DB to avoid long lock
                 */
            List<MessageEntity> messageEntityList = messageDBUtils.searchForDeletedMessage(this,
                    numOfMessages);
            for (MessageEntity messageEntityEle : messageEntityList) {
                insertDBSafe(MainActivity.DAO_DEL, messageEntityEle);
                deleteEntryDBSafe(MainActivity.DAO_UNHANDLED, messageEntityEle);
            }
        });
    }

//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
}

