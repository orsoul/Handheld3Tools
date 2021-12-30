package org.orsoul.baselib.data.db.entity;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {PrimaryBagEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
  public abstract PrimaryBagDao primaryBagDao();

  private static AppDatabase database;

  public static AppDatabase init(Context context) {
    if (database == null) {
      database = Room.databaseBuilder(context,
          AppDatabase.class, "primaryBag.db")
          .allowMainThreadQueries()
          .addMigrations(new Migration(1, 2) {
            @Override public void migrate(SupportSQLiteDatabase database) {
              database.execSQL("ALTER TABLE device ADD COLUMN deviceCode TEXT");
            }
          }).build();
    }
    return database;
  }

  public static AppDatabase getInstance() {
    return database;
  }

  public static PrimaryBagDao getPrimaryBagDao() {
    return database == null ? null : database.primaryBagDao();
  }
}
