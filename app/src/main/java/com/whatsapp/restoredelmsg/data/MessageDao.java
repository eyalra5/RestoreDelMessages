package com.com.whatsapp.restoredelmsg.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.whatsapp.restoredelmsg.data.MessageEntity;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert
    void insert(MessageEntity message);

    @Delete
    void delete(MessageEntity message);
//    @Insert
//    void insert(MessageEntity message);

    @Query("SELECT * FROM messages ORDER BY sender DESC")
    LiveData<List<MessageEntity>> getAllMessages();

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    List<MessageEntity> getAllMessagesList();


}
