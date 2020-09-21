package com.fanfull.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { Bag3Entity.class }, version = 1, exportSchema = false)
public abstract class Bag3RoomDatabase extends RoomDatabase {
  public abstract Bag3EntityDao getBag3EntityDao();
}