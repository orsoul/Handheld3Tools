package com.fanfull.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import java.util.List;

@Dao
public interface BaseDao<T> {

  @Insert
  void insert(T... items);

  @Insert
  void insert(List<T> items);

  @Update
  void update(T... items);

  @Update
  void update(List<T> items);

  @Delete
  void delete(T... items);

  @Delete
  void delete(List<T> items);
}