package com.fanfull.room;

import androidx.room.Dao;
import androidx.room.Query;
import java.util.List;

@Dao
public interface Bag3EntityDao extends BaseDao<Bag3Entity> {

  @Query("SELECT * FROM Bag3Entity")
  List<Bag3Entity> getAll();

  @Query("SELECT * FROM Bag3Entity WHERE bagId IN (:bagIds)")
  List<Bag3Entity> getByBagId(String... bagIds);

  @Query("SELECT * FROM Bag3Entity WHERE status = :status")
  List<Bag3Entity> getByStatus(int status);

  @Query("SELECT * FROM Bag3Entity WHERE time BETWEEN :startTime AND :endTime")
  List<Bag3Entity> getByTimeBetween(String startTime, String endTime);

  @Query("SELECT * FROM Bag3Entity WHERE time = :time LIMIT 1")
  Bag3Entity getByTime(String time);
}