package com.fanfull.libhard.finger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.finger.bean.FingerBean;

import java.util.ArrayList;
import java.util.List;

public class FingerPrintSQLiteHelper extends SQLiteOpenHelper {

  public static final String DB_NAME = "FingerPrint.db";
  public static final String TABLE_NAME = "FingerPrint";

  /** 指纹存放位置. */
  public static final String INDEX_FINGER_INDEX = "fingerIndex";
  /** 指纹id，业务暂未需要. */
  public static final String INDEX_FINGER_ID = "fingerId";
  /** 指纹版本. */
  public static final String INDEX_FINGER_VERSION = "fingerVersion";
  /** 指纹对应的用户id. */
  public static final String INDEX_USER_ID = "userId";
  /** 指纹对应的用户卡id. */
  public static final String INDEX_USER_IC = "userIc";
  /** 指纹编号，用于区分同一用户的不同指纹. */
  public static final String INDEX_FINGER_NUM = "fingerNum";
  /** 指纹状态，0表示未上传到服务器，1表示已上传到服务器，2表示与服务器指纹不一致. */
  public static final String INDEX_FINGER_STATUS = "fingerStatus";
  /** 指纹名. */
  public static final String INDEX_FINGER_NAME = "fingerName";
  /** 用户名. */
  public static final String INDEX_USER_NAME = "userName";
  /** 指纹特征码，1024个十六进制字符. */
  public static final String INDEX_FINGER_FEATURE = "fingerFeature";
  public static final String INDEX_TIME = "timeStamp";

  public static final int DB_VERSION = 6;

  // 创建表的 sql 语句
  private final String SQL_CREATE = "create table " + TABLE_NAME + "("
      + "id integer primary key autoincrement,"
      + INDEX_FINGER_INDEX + " integer unique,"
      + INDEX_FINGER_ID + " varchar(32),"
      + INDEX_FINGER_VERSION + " integer,"
      + INDEX_USER_ID + " varchar(32),"
      + INDEX_USER_IC + " varchar(32),"
      + INDEX_FINGER_NUM + " integer,"
      + INDEX_FINGER_STATUS + " integer,"
      + INDEX_FINGER_NAME + " varchar(16),"
      + INDEX_USER_NAME + " varchar(16),"
      + INDEX_TIME + " TimeStamp NOT NULL DEFAULT (datetime('now','localtime')),"
      + INDEX_FINGER_FEATURE + " varchar not null"
      + ");";
  private final String SQL_CLEAR_TABLE = "DELETE FROM " + TABLE_NAME;
  private final String
      SQL_RESET_ID = "UPDATE sqlite_sequence SET seq = 0 WHERE name = '" + TABLE_NAME + "'";

  private SQLiteDatabase sqliteDataBase;

  private FingerPrintSQLiteHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    sqliteDataBase = getWritableDatabase();
  }

  private static FingerPrintSQLiteHelper instance;

  public static FingerPrintSQLiteHelper init(Context context) {
    if (instance == null) {
      synchronized (FingerPrintSQLiteHelper.class) {
        if (instance == null) {
          instance = new FingerPrintSQLiteHelper(context);
        }
      }
    }
    return instance;
  }

  public static FingerPrintSQLiteHelper getInstance() {
    return instance;
  }

  public SQLiteDatabase getSqliteDataBase() {
    return sqliteDataBase;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // 数据库版本号变更会调用 onUpgrade 函数，在这根据版本号进行升级数据库
    LogUtils.d("onUpgrade oldVersion - newVersion: %s - %s", oldVersion, newVersion);
    if (oldVersion < newVersion) {
      db.execSQL("DROP TABLE " + TABLE_NAME);
      onCreate(db);
    }
  }

  /**
   *
   */
  public long saveOrUpdate(FingerBean fingerBean) {
    if (fingerBean == null) {
      return -1;
    }

    int fingerIndex = fingerBean.getFingerIndex();

    ContentValues contentValues = new ContentValues();
    contentValues.put(INDEX_FINGER_INDEX, fingerIndex);
    contentValues.put(INDEX_FINGER_FEATURE, fingerBean.getFeature());

    String fingerId = fingerBean.getFingerId();
    String userId = fingerBean.getUserId();
    int fingerVersion = fingerBean.getFingerVersion();
    int fingerNum = fingerBean.getFingerNum();
    //String fingerId = String.format("%s_%s_%s_%s", fingerIndex, userId, fingerVersion, fingerNum);
    contentValues.put(INDEX_FINGER_ID, fingerId);
    contentValues.put(INDEX_USER_ID, userId);
    contentValues.put(INDEX_USER_IC, fingerBean.getUserIc());
    contentValues.put(INDEX_FINGER_VERSION, fingerVersion);
    contentValues.put(INDEX_FINGER_NUM, fingerNum);
    contentValues.put(INDEX_FINGER_STATUS, fingerBean.getFingerStatus());
    contentValues.put(INDEX_FINGER_NAME, fingerBean.getFingerName());
    contentValues.put(INDEX_USER_NAME, fingerBean.getUserName());

    long res;
    if (null != queryFingerByFingerIndex(fingerIndex)) {
      String whereClause;
      String[] whereArgs;
      whereClause = INDEX_FINGER_INDEX + "=?";
      whereArgs = new String[] { String.valueOf(fingerIndex) };
      res = sqliteDataBase.update(FingerPrintSQLiteHelper.TABLE_NAME, contentValues,
          whereClause,
          whereArgs);
      //                    INDEX_CARD_NO + "=? or " + INDEX_FACE_ID + "=?",
      //                    new String[]{cardNo, faceId});
      LogUtils.d("update finger: %s, %s", res, fingerBean.getFingerIndex());
    } else {
      res = sqliteDataBase.insert(FingerPrintSQLiteHelper.TABLE_NAME, null, contentValues);
      LogUtils.d("insert finger: %s, %s", res, fingerBean);
    }

    return res;
  }

  public boolean containFinger(int fingerIndex) {
    String selection;
    String[] selectionArgs;
    selection = INDEX_FINGER_INDEX + "=?";
    selectionArgs = new String[] { String.valueOf(fingerIndex) };

    Cursor cursor = sqliteDataBase.query(FingerPrintSQLiteHelper.TABLE_NAME, null,
        selection,
        selectionArgs, null, null, null);
    boolean reVal = cursor.moveToNext();
    cursor.close();
    return reVal;
  }

  /** 查询所有已用的指纹位置. */
  public List<Integer> queryAllFingerIndex() {
    Cursor cursor = sqliteDataBase.query(FingerPrintSQLiteHelper.TABLE_NAME, null,
        null, null, null, null, null);
    List<Integer> list = new ArrayList<>();
    while (cursor.moveToNext()) {
      list.add(cursor.getInt(cursor.getColumnIndex(INDEX_FINGER_INDEX)));
    }
    return list;
  }

  public List<FingerBean> queryAllFinger() {
    Cursor cursor = sqliteDataBase.query(FingerPrintSQLiteHelper.TABLE_NAME, null,
        null, null, null, null, null);
    List<FingerBean> list = new ArrayList<>();
    while (cursor.moveToNext()) {
      final FingerBean fingerBean = genFingerBean(cursor);
      list.add(fingerBean);
    }
    cursor.close();
    return list;
  }

  private FingerBean genFingerBean(Cursor cursor) {
    if (cursor == null) {
      return null;
    }

    int fingerIndexIndex = cursor.getColumnIndex(INDEX_FINGER_INDEX);
    int featureIndex = cursor.getColumnIndex(INDEX_FINGER_FEATURE);

    int fingerIdIndex = cursor.getColumnIndex(INDEX_FINGER_ID);

    int cardIdIndex = cursor.getColumnIndex(INDEX_USER_ID);
    int userIcIndex = cursor.getColumnIndex(INDEX_USER_IC);
    int fingerVersionIndex = cursor.getColumnIndex(INDEX_FINGER_VERSION);
    int numIndex = cursor.getColumnIndex(INDEX_FINGER_NUM);
    int statusIndex = cursor.getColumnIndex(INDEX_FINGER_STATUS);
    int fingerNameIndex = cursor.getColumnIndex(INDEX_FINGER_NAME);
    int userNameIndex = cursor.getColumnIndex(INDEX_USER_NAME);

    FingerBean fingerBean = new FingerBean(
        cursor.getInt(fingerIndexIndex),
        cursor.getString(featureIndex),
        cursor.getString(fingerIdIndex),
        cursor.getInt(fingerVersionIndex)
    );
    fingerBean.setUserId(cursor.getString(cardIdIndex));
    fingerBean.setUserIc(cursor.getString(userIcIndex));
    fingerBean.setFingerNum(cursor.getInt(numIndex));
    fingerBean.setFingerStatus(cursor.getInt(statusIndex));
    fingerBean.setFingerName(cursor.getString(fingerNameIndex));
    fingerBean.setUserName(cursor.getString(userNameIndex));
    return fingerBean;
  }

  public List<FingerBean> queryFingersByUserId(String userId) {

    String selection;
    String[] selectionArgs;
    selection = INDEX_USER_ID + "=?";
    selectionArgs = new String[] { String.valueOf(userId) };

    Cursor cursor = sqliteDataBase.query(FingerPrintSQLiteHelper.TABLE_NAME, null,
        selection,
        selectionArgs, null, null, null);

    List<FingerBean> list = new ArrayList<>();
    while (cursor.moveToNext()) {
      final FingerBean fingerBean = genFingerBean(cursor);
      list.add(fingerBean);
    }

    cursor.close();
    return list;
  }

  /** 根据指纹在指纹库中的位置 查询. */
  public FingerBean queryFingerByFingerIndex(int fingerIndex) {

    String selection;
    String[] selectionArgs;
    selection = INDEX_FINGER_INDEX + "=?";
    selectionArgs = new String[] { String.valueOf(fingerIndex) };

    Cursor cursor = sqliteDataBase.query(FingerPrintSQLiteHelper.TABLE_NAME, null,
        selection,
        selectionArgs, null, null, null);

    FingerBean reVal = null;
    if (cursor.moveToFirst()) {
      reVal = genFingerBean(cursor);
    }
    cursor.close();
    LogUtils.d("queryFinger by index：%s, %s", fingerIndex, reVal);
    return reVal;
  }

  /** 获取 记录的数量. */
  public int getAllCount() {
    Cursor cursor = sqliteDataBase.rawQuery("select count(id) from " + TABLE_NAME, null);
    int count;
    if (cursor.moveToFirst()) {
      count = cursor.getInt(0);
    } else {
      count = -1;
    }
    cursor.close();
    return count;
  }

  /** 根据指纹位置 删除指纹. */
  public int deleteByUserId(String userId) {
    return sqliteDataBase.delete(FingerPrintSQLiteHelper.TABLE_NAME,
        INDEX_USER_ID + "=?", new String[] { userId });
  }

  /** 根据指纹位置 删除指纹. */
  public int deleteByFingerIndex(int fingerIndex) {
    return sqliteDataBase.delete(FingerPrintSQLiteHelper.TABLE_NAME,
        INDEX_FINGER_INDEX + "=?", new String[] { String.valueOf(fingerIndex) });
  }

  public int delete(FingerBean finger) {
    if (finger == null) {
      return -2;
    }
    return deleteByFingerIndex(finger.getFingerIndex());
  }

  public int delete(List<FingerBean> fingers) {
    if (fingers == null) {
      return 0;
    }

    int count = 0;
    for (FingerBean finger : fingers) {
      if (0 < delete(finger)) {
        count++;
      }
    }
    return count;
  }

  public int deleteOldData(long time) {
    return sqliteDataBase.delete(FingerPrintSQLiteHelper.TABLE_NAME,
        INDEX_TIME + "<?", new String[] { String.valueOf(time) });
  }

  /**
   * 删除 所有数据 并 重设ID
   */
  public void clearAll() {
    sqliteDataBase.execSQL(SQL_CLEAR_TABLE);
    sqliteDataBase.execSQL(SQL_RESET_ID);
  }
}