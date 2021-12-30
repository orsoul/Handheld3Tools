package org.orsoul.baselib.data.db.entity;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PrimaryBagDao {
  @Query("SELECT * FROM PrimaryBag")
  List<PrimaryBagEntity> getAll();

  @Query("SELECT * FROM PrimaryBag WHERE batchId = :batchId")
  List<PrimaryBagEntity> loadAllByBatchId(String batchId);

  @Query("SELECT * FROM PrimaryBag WHERE batchId = :batchId AND uploadStatus = :status")
  List<PrimaryBagEntity> loadAllByBatchIdAndStatus(String batchId, int status);

  @Insert
  void insertAll(PrimaryBagEntity... bags);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertOrUpdateAll(PrimaryBagEntity... bags);

  @Update
  void updateAll(PrimaryBagEntity... bags);

  @Query("UPDATE PrimaryBag SET uploadStatus = :newStatus WHERE batchId = :batchId")
  void updateStatusWhere(int newStatus, String batchId);

  @Query("UPDATE PrimaryBag SET uploadStatus = :newStatus WHERE batchId = :batchId AND uploadStatus = :oldStatus")
  void updateStatusWhere(int newStatus, String batchId, int oldStatus);

  @Query("Delete FROM PrimaryBag WHERE uploadStatus = :status")
  int deleteByStatus(int status);

  @Query("Delete FROM PrimaryBag WHERE timeStamp < :timeStamp")
  int deleteTimeStampLe(String timeStamp);

  @Delete
  void deleteAll(PrimaryBagEntity... bags);
}
