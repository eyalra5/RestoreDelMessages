package com.whatsapp.restoredelmsg;

import android.util.Log;

import com.whatsapp.restoredelmsg.data.MessageEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDBUtils {
    List<MessageEntity> getAllNotifiedCheckedMsgsOfSenderFromDB(Map<String, Integer> senderToMsgCountMap){
        List<MessageEntity> messageEntityList = new ArrayList<>();
        List<MessageEntity> allMessagesList = MainActivity.DAO_UNHANDLED.getAllMessagesList();

        if (null == allMessagesList) return messageEntityList;

        Map<String, Integer> senderToMsgCountMapFromDB = new HashMap<>();
        for (MessageEntity msgBySender : allMessagesList) {
            int currentCnt = senderToMsgCountMapFromDB.getOrDefault(msgBySender.sender, 0);
            senderToMsgCountMapFromDB.put(msgBySender.sender, ++currentCnt);
        }

        for (Map.Entry<String, Integer>  entry : senderToMsgCountMapFromDB.entrySet()) {
            String sender = entry.getKey();
            int numOfMessages = entry.getValue();
            //TODO: since all messages are suspected as deleted.
            // maybe later the specific sender would found to reduce viewed message that wasn't del.
//            if (numOfMessages < senderToMsgCountMap.getOrDefault(sender,0 )) {
                for (MessageEntity msgBySender : allMessagesList) {
                    if (msgBySender.sender.equals(sender)) {
                        messageEntityList.add(msgBySender);
                        Log.d(WhatsAppNotificationListener.TAG, "id: " + msgBySender.id +
                    ", sender:" + msgBySender.sender + ", text:" + msgBySender.text);
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
}
