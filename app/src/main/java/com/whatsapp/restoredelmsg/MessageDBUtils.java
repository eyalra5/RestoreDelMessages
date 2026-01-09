package com.whatsapp.restoredelmsg;

import static com.whatsapp.restoredelmsg.WhatsAppNotificationListener.TAG;

import android.os.Build;
import android.util.Log;

import com.whatsapp.restoredelmsg.data.MessageEntity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MessageDBUtils {

    public static final String CHANNEL_ID = "whatsapp_db_channel";
    public static com.com.whatsapp.restoredelmsg.data.MessageDao DAO_DEL;
    public static com.com.whatsapp.restoredelmsg.data.MessageDao DAO_UNHANDLED;
    List<MessageEntity> getAllNotifiedCheckedMsgsOfSenderFromDB(Map<String, Integer> senderToMsgCountMap,
                                                                Map<String, Integer> sendersBulkNamesMap){
        List<MessageEntity> messageEntityList = new ArrayList<>();
        List<MessageEntity> allMessagesList = DAO_UNHANDLED.getAllMessagesList();

        if (null == allMessagesList) return messageEntityList;

        for (String senderWithMsg: senderToMsgCountMap.keySet()) {
            String deletedSender = "";
            try {
                if (sendersBulkNamesMap.getOrDefault(senderWithMsg, 0) == 0) {
                    deletedSender = senderWithMsg;
                }
            } catch (Exception e) {
                Log.d(TAG, "Sender search for: " + senderWithMsg + ", " + e.getMessage());
            }

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
                    }
                    else {
                         //Do nothing;
                    }
                }
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
        /* means no sender been total removed,
         but single message of a sender and the sender at least one message on notification. */
        return !sender.isEmpty();
    }

}
