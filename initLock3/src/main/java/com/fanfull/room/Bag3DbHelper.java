package com.fanfull.room;

import android.content.Context;
import androidx.room.Room;

public class Bag3DbHelper {
  private static Bag3RoomDatabase database;

  public static void init(Context context) {
    if (database == null) {
      database = Room.databaseBuilder(context, Bag3RoomDatabase.class, "Bag3Info.db")
          .allowMainThreadQueries()
          .build();
    }
  }

  public static Bag3EntityDao getBag3EntityDao() {
    return database.getBag3EntityDao();
  }
}
