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
    private int totalNumOfMessages = 0;
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

        if (null == sender) return;

        if (isValidSeparatorMsg(sender, text) || isValidSeparatorSenderMsg(sender, text)) {
            String[] words = null;
            int whatsAppNotificationNumOfMessages;
            try {
                assert text != null;
                words = splitIntoWords(text);
                whatsAppNotificationNumOfMessages = getNumOfMessagesFromText(words);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (getTotalNumOfMessages() > whatsAppNotificationNumOfMessages  ||
                isDeleteMessageWhichAleadyRead(whatsAppNotificationNumOfMessages)) {
                    /*
                    means last notification of batch - sender <count messages>.
                    means use 'numOfMessages' to verify which sender deleted message
                     */
                Executors.newSingleThreadExecutor().execute(() -> {
                        /*
                        Main thread cant access DB to avoid long lock
                         */
                    List<MessageEntity> messageEntityList = messageDBUtils.getAllNotifiedCheckedMsgsOfSenderFromDB(numOfMessages);
                    for (MessageEntity messageEntityEle : messageEntityList) {
                        insertDBSafe(MainActivity.DAO_DEL, messageEntityEle);
                        deleteEntryDBSafe(MainActivity.DAO_UNHANDLED, messageEntityEle);
                        decSafeNumOfMessagesBySender(messageEntityEle.sender);
                    }
                });
                setCachedMessageEntity(null);
            }
            else if (isOneMessageInCache()) {
                MessageEntity messageEntity = getCachedMessageEntity();
                if (messageEntity != null) {
//            if (messageEntity != null && getTotalNumOfMessages() <= whatsAppNotificationNumOfMessages) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        insertDBSafe(MainActivity.DAO_UNHANDLED, messageEntity);
                        setSafeNumOfMessagesBySender(messageEntity.sender,
                                whatsAppNotificationNumOfMessages);
//                        if (whatsAppNotificationNumOfMessages < MainActivity.DAO_UNHANDLED.getAllMessagesList().size()) {
//                            deleteIrrelevantEntries(MainActivity.DAO_UNHANDLED,
//                                    sender,
//                                    whatsAppNotificationNumOfMessages);
//                            setSafeNumOfMessagesBySender(messageEntity.sender,
//                                    whatsAppNotificationNumOfMessages);
//                        }
                    });
                    setCachedMessageEntity(null);
//                    clearPreviousCycleBySender(sender);
                }
            }
                /*
                'Separator' Notification summerier user messages.
                 */
//                if (getSafeNumOfMessagesBySender(sender) > whatsAppNotificationNumOfMessages) {
//                    /*
//                    means last notification of batch - sender <count messages>.
//                    means use 'numOfMessages' to verify which sender deleted message
//                     */
//                    Executors.newSingleThreadExecutor().execute(() -> {
//                        /*
//                        Main thread cant access DB to avoid long lock
//                         */
//                        List<MessageEntity> messageEntityList = messageDBUtils.getAllNotifiedCheckedMsgsOfSenderFromDB(numOfMessages);
//                        for (MessageEntity messageEntityEle : messageEntityList) {
//                            insertDBSafe(MainActivity.DAO_DEL, messageEntityEle);
//                            deleteEntryDBSafe(MainActivity.DAO_UNHANDLED, messageEntityEle);
//                        }
//                        setSafeNumOfMessagesBySender(sender, whatsAppNotificationNumOfMessages);
//                    });
////            createNotificationChannel();
////            showNotification( "Hello!", "This is a test notification.");
//
//                    clearPreviousCycleBySender(sender);
//
//                }
//        }

//        if ( || getSafeNumOfMessagesBySender(sender) > 1) {
//            /*
//            means last notification of batch - sender <count messages>.
//            means use 'numOfMessages' to verify which sender deleted message
//             */
//            Executors.newSingleThreadExecutor().execute(() -> {
//                /*
//                Main thread cant access DB to avoid long lock
//                 */
//                List<MessageEntity> messageEntityList = messageDBUtils.getAllNotifiedCheckedMsgsOfSenderFromDB(numOfMessages);
//                for (MessageEntity messageEntityEle : messageEntityList) {
//                    insertDBSafe(MainActivity.DAO_DEL, messageEntityEle);
//                    deleteEntryDBSafe(MainActivity.DAO_UNHANDLED, messageEntityEle);
//                }
//            });
////            createNotificationChannel();
////            showNotification( "Hello!", "This is a test notification.");
//
//            clearPreviousCycleBySender(sender);
//        } else if (isSeparatorMsg(sender, text)) {
//            MessageEntity messageEntity = getCachedMessageEntity();
//            if (messageEntity != null) {
//                Executors.newSingleThreadExecutor().execute(() -> {
//                    insertDBSafe(MainActivity.DAO_UNHANDLED, messageEntity);
//                });
//                clearPreviousCycleBySender(sender);
//            } else {
//                /*
//                'Separator' Notification summerier user messages.
//                 */
//            }
//        } else if (isOneMessageInCache()) {
//            prepareForDeletionUpdate(sender, text);
        }
        else if ((isValidSeparatorDoubleMsg(sender, text) || isDelSeperator(text)) &&
                isOneMessageInCache()) {
            MessageEntity messageEntity = getCachedMessageEntity();
            if (messageEntity != null) {
//            if (messageEntity != null && getTotalNumOfMessages() <= whatsAppNotificationNumOfMessages) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    insertDBSafe(MainActivity.DAO_UNHANDLED, messageEntity);
//                    TODO: is it??
                    incSafeNumOfMessagesBySender(messageEntity.sender);
//                    deleteIrrelevantEntries(MainActivity.DAO_UNHANDLED,
//                            messageEntity.sender,
//                            whatsAppNotificationNumOfMessages);
                });
                setCachedMessageEntity(null);
//                    clearPreviousCycleBySender(sender);
            }
        }
        else if (!sender.equals("WhatsApp")) {
//            prepareForDeletionUpdate(sender, text);
            setCachedMessageEntity(new MessageEntity(sender, text, System.currentTimeMillis()));
        } else {
            clearPreviousCycleBySender("UNKNOW_STATE");
            Log.d(TAG, "UNHANDLED STATE");
        }
    }

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

    private boolean isValidSeparatorDoubleMsg(String sender, String text) {
        if (!sender.equals("WhatsApp") && null != text) {
            try {
                return getCachedMessageEntity().sender.equals(sender) &&
                        getCachedMessageEntity().text.equals(text);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private boolean isDelSeperator(String text) {
        return text.equals("‏הודעה זו נמחקה");
    }
    private boolean isValidSeparatorSenderMsg(String sender, String text) {
        if (!sender.equals("WhatsApp") && null != text) {
            try {
                String[] words = splitIntoWords(text);
                // true example: 3 הודעות חדשות
                return words.length == 3 && getNumOfMessagesFromText(words) > 0;
                //            return sender.equals("WhatsApp");
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private boolean isValidSeparatorMsg(String sender, String text) {
        if (!sender.equals("WhatsApp") || null == text) return false;

//        String[] words = text.split("\\s+");
//        Toast.makeText(this, "WhatsApp message from: " + words[0] + ",  " +
//                        new String(sender.getBytes(StandardCharsets.UTF_8))
//                        + " → " + new String(text.getBytes(StandardCharsets.UTF_8)),
//                Toast.LENGTH_LONG).show();

        try {
            String[] words = splitIntoWords(text);
            return getNumOfMessagesFromText(words) > 0  &&  getNumOfSendersFromText(words) > 0;
//            return sender.equals("WhatsApp");
        } catch (Exception e) {
            return false;
        }
    }
    private String[] splitIntoWords(String text) throws Exception {
        String[] words = text.split("\\s+");
        if (words.length < 3) throw new Exception("Bad input. text is: " + text);
        return words;
    }
    private int getNumOfSendersFromText(String[] words) throws Exception {
        return Integer.parseInt(words[3].
                replaceAll("[\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]", ""));
    }
    private int getNumOfMessagesFromText(String[] words) throws Exception {
        return Integer.parseInt(words[0].
                replaceAll("[\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]", ""));
    }
    private void clearPreviousCycleBySender(String sender) {
        setCachedMessageEntity(null);
//        setSafeNumOfMessagesBySender(sender, 0);
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
            numOfSenderMsgs = getSafeNumOfMessagesBySender(sender);
        }
        setSafeNumOfMessagesBySender(sender, numOfSenderMsgs+1);
        Log.d(TAG,"numOfSenderMsgs:" + numOfSenderMsgs +
                ", sender:" + sender +  ", text: " + text);
    }

    private void insertDBSafe(com.com.whatsapp.restoredelmsg.data.MessageDao dao,
                              MessageEntity messageEntity) {
        boolean shouldRetry = false;
        do {
            try {
                dao.insert(messageEntity);
//                incSafeNumOfMessagesBySender(messageEntity.sender);
            } catch (SQLiteConstraintException e) {
                shouldRetry = true;
            }
        }while (shouldRetry);
    }

    private void deleteIrrelevantEntries(com.com.whatsapp.restoredelmsg.data.MessageDao dao,
                                         String mySender,
                                         int numOfMessageToKeepByOrd) {
        boolean shouldRetry = false;
        do {
            try {
                List<MessageEntity> messageEntityList = dao.getAllMessagesList();
                int keepCnt = 0;
                for (MessageEntity m: messageEntityList) {
                    if (messageEntityList.size() - keepCnt <= numOfMessageToKeepByOrd) break;
                    if (m.sender.equals(mySender) ) {
                        dao.delete(m);
                        decSafeNumOfMessagesBySender(m.sender);
                        keepCnt++;
                    }
                }
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

    private boolean isDeleteMessageWhichAleadyRead(int whatsAppNotificationNumOfMessages) {
        return getTotalNumOfMessages() > whatsAppNotificationNumOfMessages;
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

        Log.d(TAG, "in onNotificationRemoved()");

        if (sbn == null) return;
        String packageName = sbn.getPackageName();

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        // Only listen for WhatsApp notifications
        if (!"com.whatsapp".equals(packageName)) return;

        Bundle extras = notification.extras;
        if (extras == null) return;

        String sender = extras.getString(Notification.EXTRA_TITLE); // Sender name
        String text = extras.getString(Notification.EXTRA_TEXT);   // Message body

//        Executors.newSingleThreadExecutor().execute(() -> {
//        //new Thread explanation: Main thread cant access DB to avoid long lock
//            List<MessageEntity> messageEntityList = messageDBUtils.getAllNotifiedCheckedMsgsOfSenderFromDB(numOfMessages);
//            for (MessageEntity messageEntityEle : messageEntityList) {
//                deleteEntryDBSafe(MainActivity.DAO_UNHANDLED, messageEntityEle);
//            }
//        });
    }

    private synchronized int getSafeNumOfMessagesBySender(String sender) {
        return numOfMessages.getOrDefault(sender, 0);
    }

    private synchronized void setSafeNumOfMessagesBySender(String sender, int num) {
        numOfMessages.put(sender, numOfMessages.getOrDefault(sender, 0)+1);
        totalNumOfMessages = num;
    }
    private synchronized void incSafeNumOfMessagesBySender(String sender) {
        numOfMessages.put(sender, numOfMessages.getOrDefault(sender, 0)+1);
        totalNumOfMessages++;
    }
    private synchronized void decSafeNumOfMessagesBySender(String sender) {
        numOfMessages.put(sender, numOfMessages.getOrDefault(sender, 1)-1);
        totalNumOfMessages--;
    }
    private synchronized int getTotalNumOfMessages() {
        return totalNumOfMessages;
    }

//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
}

