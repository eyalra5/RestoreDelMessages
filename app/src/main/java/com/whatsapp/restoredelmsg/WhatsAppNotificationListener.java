package com.whatsapp.restoredelmsg;


import com.whatsapp.restoredelmsg.data.MessageEntity;

import android.app.Notification;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

public class WhatsAppNotificationListener extends NotificationListenerService {
    public static final String TAG = "WhatsAppListener";
    public static int deleteViewSize = 0;
    private final Map<String, Integer> _sendersBulkNamesMap = new HashMap<>() ;
    private final Map<String, Integer> _numOfMessages = new HashMap<>() ;
    private volatile MessageEntity _cachedMessageEntity = null;
    private int _totalNumOfMessages = 0;

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
    private final MessageDBUtils _messageDBUtils = new MessageDBUtils();
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

    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.d(TAG, "in onNotificationPosted()");
        deletionLogicRun(sbn,
                1 // as message notification exist more than one
        );
    }
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
                return (words.length >= 3) && getNumOfMessagesFromText(words) > 0;
                //            return sender.equals("WhatsApp");
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private boolean isValidSeparatorMsg(String sender, String text) {
        if (!sender.equals("WhatsApp") || null == text) return false;
        try {
            String[] words = splitIntoWords(text);
            return getNumOfMessagesFromText(words) > 0  &&  getNumOfSendersFromText(words) > 0;
        } catch (Exception e) {
            return false;
        }
    }
    private String[] splitIntoWords(String text){
        String [] words = {text};
        try {
            words = text.split("\\s+");
        } catch (Exception e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return words;
    }
    private int getNumOfSendersFromText(String[] words) {
        try{
            return (words.length < 3 ? 0 : Integer.parseInt(words[3].
                    replaceAll("[\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]", "")));
        } catch (Exception e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            return 0;
        }
    }

    private int getNumOfMessagesFromText(String[] words){
        try {
            return (words.length < 1 ? 0 : Integer.parseInt(words[0].
                    replaceAll("[\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]", "")));
        } catch (Exception e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            return 0;
        }
    }
    private void clearPreviousCycleBySender() {
        setCachedMessageEntity(null);
    }

    private boolean isOneMessageInCache() {
        MessageEntity messageEntity = getCachedMessageEntity();
        return  messageEntity != null;
    }

    private synchronized void setCachedMessageEntity(MessageEntity messageEntity) {
        _cachedMessageEntity = messageEntity;
        if (messageEntity == null)  _sendersBulkNamesMap.clear();
    }
    private synchronized MessageEntity getCachedMessageEntity() {
        return _cachedMessageEntity;
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

    private boolean isNotifyMessageDeleted(int whatsAppNotificationNumOfMessages, int sendersBulkMinSize) {
        return getTotalNumOfMessages() > whatsAppNotificationNumOfMessages || // means one notification been removed
                _sendersBulkNamesMap.size() > sendersBulkMinSize; // more than one sender been sent . the absent is the deleting sender
    }
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.i(TAG, "Listener connected ");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.w(TAG, "Listener disconnected ️");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "in onNotificationRemoved()");
        deletionLogicRun(sbn, 0 /*  Case of Notification was the only once, and been remove so not notification left */);
    }

    private synchronized void setSafeNumOfMessagesBySender(String sender, int num) {
        _numOfMessages.put(sender, _numOfMessages.getOrDefault(sender, 0)+1);
        _totalNumOfMessages = num;
    }
    private synchronized void incSafeNumOfMessagesBySender(String sender) {
        _numOfMessages.put(sender, _numOfMessages.getOrDefault(sender, 0)+1);
        _totalNumOfMessages++;
    }
    private synchronized void decSafeNumOfMessagesBySender(String sender) {
        _numOfMessages.put(sender, _numOfMessages.getOrDefault(sender, 1)-1);
        _totalNumOfMessages--;
    }
    private synchronized int getTotalNumOfMessages() {
        return _totalNumOfMessages;
    }

    private void deletionLogicRun(StatusBarNotification sbn, int sendersBulkMinSize){
        if (sbn == null) return;
        String packageName = sbn.getPackageName();

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        // Only listen on WhatsApp notifications
        if (!"com.whatsapp".equals(packageName)) {
            return;
        }

        Bundle extras = notification.extras;
        if (extras == null) return;

        String sender = extras.getString(Notification.EXTRA_TITLE); // Sender name
        String text = extras.getString(Notification.EXTRA_TEXT);   // Message body

        if (null == sender || null == text) return;

        if (isValidSeparatorMsg(sender, text) || isValidSeparatorSenderMsg(sender, text)) {
            String[] words = splitIntoWords(text);
            int whatsAppNotificationNumOfMessages = getNumOfMessagesFromText(words);
            if (whatsAppNotificationNumOfMessages == 0) {
                return;
            }
            if (isNotifyMessageDeleted(whatsAppNotificationNumOfMessages, sendersBulkMinSize)) {
                    /*
                    means last notification of batch - sender <count messages>.
                    means use 'numOfMessages' to verify which sender deleted message
                     */
                Map<String, Integer> sendersBulkNamesMap = new HashMap<>(_sendersBulkNamesMap);

                Executors.newSingleThreadExecutor().execute(() -> {
                        /* As Main thread cant access DB to avoid long lock */
                    List<MessageEntity> messageEntityList = _messageDBUtils.getAllNotifiedCheckedMsgsOfSenderFromDB(_numOfMessages,
                                                                                                                    sendersBulkNamesMap);
                    for (MessageEntity messageEntityEle : messageEntityList) {
                        insertDBSafe(MessageDBUtils.DAO_DEL, messageEntityEle);
                        deleteEntryDBSafe(MessageDBUtils.DAO_UNHANDLED, messageEntityEle);
                        decSafeNumOfMessagesBySender(messageEntityEle.sender);
                    }
                    deleteViewSize = MessageDBUtils.DAO_DEL.getAllMessagesList().size();
                });

                setCachedMessageEntity(null);
            }
            else if (isOneMessageInCache()) {
                MessageEntity messageEntity = getCachedMessageEntity();
                  if (messageEntity != null &&
                          getTotalNumOfMessages() <= whatsAppNotificationNumOfMessages) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        insertDBSafe(MessageDBUtils.DAO_UNHANDLED, messageEntity);
                        setSafeNumOfMessagesBySender(messageEntity.sender,
                                                     whatsAppNotificationNumOfMessages);
                    });
                    setCachedMessageEntity(null);
                }
            }
            /* 'Separator' Notification summerier user messages. */
        }
        else if ((isValidSeparatorDoubleMsg(sender, text) || isDelSeperator(text)) &&
                isOneMessageInCache()) {
            MessageEntity messageEntity = getCachedMessageEntity();
            if (messageEntity != null) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    insertDBSafe(MessageDBUtils.DAO_UNHANDLED, messageEntity);
//                    TODO: is it??
                    incSafeNumOfMessagesBySender(messageEntity.sender);
                });
                setCachedMessageEntity(null);
            }
        }
        else if (!sender.equals("WhatsApp")) {
            setCachedMessageEntity(new MessageEntity(sender, text, System.currentTimeMillis()));
            _sendersBulkNamesMap.putIfAbsent(sender,1);
        } else {
            clearPreviousCycleBySender();
            Log.d(TAG, "UNHANDLED STATE");
        }
    }

}

