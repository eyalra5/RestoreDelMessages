package com.whatsapp.restoredelmsg.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class MessageEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String sender;
    public String text;
    public long timestamp;

    public MessageEntity(String sender, String text, long timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }
}

