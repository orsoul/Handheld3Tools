package com.fanfull.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.apkfuns.logutils.LogUtils;
import org.orsoul.baselib.util.DateFormatUtil;

@Entity
public class Bag3Entity {
  /** 已初始化，未刻码. */
  public static final int STATUS_INIT = 0;
  /** 已刻码. */
  public static final int STATUS_UPLOAD = 1;
  /** 重复刻码. */
  public static final int STATUS_UPLOAD_AGAIN = 2;
  /** 操作人. */
  public static String initOperator = "nobody";

  @PrimaryKey(autoGenerate = true)
  private long id;

  @ColumnInfo(name = "bagId")
  private String bagId;
  private String operator;
  private int status;
  private String time;
  private String tid;

  @Ignore
  private String uid;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getBagId() {
    return bagId;
  }

  public void setBagId(String bagId) {
    this.bagId = bagId;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getTid() {
    return tid;
  }

  public void setTid(String tid) {
    this.tid = tid;
  }

  public String getUid() {
    if (uid == null && bagId != null && bagId.length() == 24) {
      uid = bagId.substring(8, bagId.length() - 2);
    }
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  @Override public String toString() {
    return "Bag3Entity{" +
        "id=" + id +
        ", bagId='" + bagId + '\'' +
        ", operator='" + operator + '\'' +
        ", status=" + status +
        ", time='" + time + '\'' +
        ", tid='" + tid + '\'' +
        ", uid='" + uid + '\'' +
        '}';
  }

  public Bag3Entity insert(String bagId, String tid) {
    this.id = 0;
    this.bagId = bagId;
    this.tid = tid;
    this.status = STATUS_INIT;
    this.time = DateFormatUtil.getStringTime();
    Bag3DbHelper.getBag3EntityDao().insert(this);
    Bag3Entity byTime = Bag3DbHelper.getBag3EntityDao().getByTime(this.time);
    LogUtils.d("insert:%s", byTime);
    this.id = byTime.getId();
    return byTime;
  }

  public void update(int status) {
    this.operator = initOperator;
    this.status = status;
    Bag3DbHelper.getBag3EntityDao().update(this);
  }
}