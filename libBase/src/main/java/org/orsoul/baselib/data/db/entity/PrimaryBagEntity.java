package org.orsoul.baselib.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "PrimaryBag")
public class PrimaryBagEntity {
  public static final int STATUS_NOT_UPLOAD = 0;
  public static final int STATUS_UPLOAD = 1;
  public static final int STATUS_UPLOAD_REFUSE = 2;

  public static final int SAVE_DAY = 15;

  @PrimaryKey(autoGenerate = true)
  private Long id;

  private String taskName;
  private String bagId;
  private String tid;
  private String batchId;
  private int taskType;

  /** 上传状态：0=未上传，1=已上传，2=上传被拒绝 */
  @ColumnInfo(name = "uploadStatus")
  private int uploadStatus = STATUS_NOT_UPLOAD;

  private String timeStamp;

  private String userId;
  private String checkerId;
  private String coverCode;
  private String data;

  @Ignore
  private String storeId;

  public PrimaryBagEntity() {
  }

  @Ignore
  public PrimaryBagEntity(String bagId, String tid, int taskType, String userId,
      String checkerId) {
    this.bagId = bagId;
    this.tid = tid;
    this.taskType = taskType;
    this.userId = userId;
    this.checkerId = checkerId;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PrimaryBagEntity that = (PrimaryBagEntity) o;
    return Objects.equals(bagId, that.bagId) &&
        Objects.equals(batchId, that.batchId);
  }

  @Override public int hashCode() {
    return Objects.hash(bagId, batchId);
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getBagId() {
    return this.bagId;
  }

  public void setBagId(String bagId) {
    this.bagId = bagId;
  }

  public String getTid() {
    return this.tid;
  }

  public void setTid(String tid) {
    this.tid = tid;
  }

  public String getBatchId() {
    return this.batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

  public int getTaskType() {
    return this.taskType;
  }

  public void setTaskType(int taskType) {
    this.taskType = taskType;
  }

  public int getUploadStatus() {
    return this.uploadStatus;
  }

  public void setUploadStatus(int uploadStatus) {
    this.uploadStatus = uploadStatus;
  }

  public String getUserId() {
    return this.userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getCheckerId() {
    return this.checkerId;
  }

  public void setCheckerId(String checkerId) {
    this.checkerId = checkerId;
  }

  public String getTimeStamp() {
    return this.timeStamp;
  }

  public void setTimeStamp(String timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getStoreId() {
    return this.storeId;
  }

  public void setStoreId(String storeId) {
    this.storeId = storeId;
  }

  public String getCoverCode() {
    return this.coverCode;
  }

  public void setCoverCode(String coverCode) {
    this.coverCode = coverCode;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public void insert() {
    AppDatabase.getPrimaryBagDao().insertAll(this);
  }

  public void insertOrUpdateAll() {
    AppDatabase.getPrimaryBagDao().insertOrUpdateAll(this);
  }

  public void update() {
    AppDatabase.getPrimaryBagDao().updateAll(this);
  }

  public void delete() {
    AppDatabase.getPrimaryBagDao().deleteAll(this);
  }
}
