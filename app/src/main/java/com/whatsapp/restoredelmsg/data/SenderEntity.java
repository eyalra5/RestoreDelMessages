package com.whatsapp.restoredelmsg.data;

public class SenderEntity {
    String sender;

    int numOfMessages;

    public SenderEntity(String sender) {
        this.sender = sender;
        this.numOfMessages = 0;
    }

    public void setNumOfMessages(String sender, int numOfMessages) {
        this.numOfMessages = numOfMessages;
        this.sender = sender;
    }
}
