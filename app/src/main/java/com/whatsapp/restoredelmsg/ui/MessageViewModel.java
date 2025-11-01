package com.whatsapp.restoredelmsg.ui;

//import androidx.lifecycle.AndroidViewModel;

//import com.whatsapp.showdelmsg.com.whatsapp.restoreDelMsg.data.AppDatabase;
//import com.whatsapp.showdelmsg.com.whatsapp.restoreDelMsg.data.MessageEntity;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.whatsapp.restoredelmsg.data.AppDatabase;
import com.whatsapp.restoredelmsg.data.MessageEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageViewModel extends AndroidViewModel {

    private final LiveData<List<MessageEntity>> allMessages;

    public MessageViewModel(Application application) {
        super(application);
        allMessages = AppDatabase.getDBInstance(application , "DELETED_MSG1").messageDao().getAllMessages();
    }

    public LiveData<List<MessageEntity>> getAllMessages() {
        return allMessages;
    }
}

