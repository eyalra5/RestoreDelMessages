package com.whatsapp.restoredelmsg.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Database(entities = {MessageEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static final Map<String, AppDatabase> databaseByType  = new ConcurrentHashMap<>();

    public abstract com.com.whatsapp.restoredelmsg.data.MessageDao messageDao();

    public static AppDatabase getDBInstance(Context context, String dbName) {
        if (databaseByType.get(dbName) == null) {
            synchronized (AppDatabase.class) {
                    databaseByType.put(dbName, Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class, dbName + "_db"
                    ).build());
            }
        }
        return databaseByType.get(dbName);
    }
}
