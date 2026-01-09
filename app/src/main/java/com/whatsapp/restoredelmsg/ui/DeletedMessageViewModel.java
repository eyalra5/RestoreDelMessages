package com.whatsapp.restoredelmsg.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.whatsapp.restoredelmsg.data.AppDatabase;
import com.whatsapp.restoredelmsg.data.MessageEntity;

import java.util.List;

public class DeletedMessageViewModel extends AndroidViewModel {

    private final LiveData<List<MessageEntity>> allMessages;

    public DeletedMessageViewModel(Application application) {
        super(application);
        allMessages = AppDatabase.getDBInstance(application , "DELETED_MSG1").messageDao().getAllMessages();
    }

    public LiveData<List<MessageEntity>> getAllMessages() {
        return allMessages;
    }
}

