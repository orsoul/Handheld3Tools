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

  public static final String DB_NAME = "fingerPrint.db";
  public static final String TABLE_NAME = "fingerPrint";

  public static final String INDEX_FINGER_INDEX = "fingerIndex";
  public static final String INDEX_FINGER_ID = "fingerID";
  public static final String INDEX_FINGER_VERSION = "fingerVersion";
  public static final String INDEX_CARD_ID = "cardId";
  public static final String INDEX_USER_NAME = "userName";
  public static final String INDEX_FINGER_FEATURE = "fingerFeature";
  public static final String INDEX_TIME = "timeStamp";

  public static final int DB_VERSION = 3;

  // 创建表的 sql 语句
  private final String SQL_CREATE = "create table " + TABLE_NAME + "("
      + "id integer primary key autoincrement,"
      + INDEX_FINGER_INDEX + " integer unique,"
      + INDEX_FINGER_ID + " varchar(16),"
      + INDEX_FINGER_VERSION + " integer,"
      + INDEX_CARD_ID + " varchar(16),"
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
    contentValues.put(INDEX_FINGER_FEATURE, fingerBean.getFeatureString());

    contentValues.put(INDEX_FINGER_ID, fingerBean.getFingerId());
    contentValues.put(INDEX_FINGER_VERSION, fingerBean.getFingerVersion());
    contentValues.put(INDEX_CARD_ID, fingerBean.getCardId());
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
      int fingerIndexIndex = cursor.getColumnIndex(INDEX_FINGER_INDEX);
      int fingerIdIndex = cursor.getColumnIndex(INDEX_FINGER_ID);
      int featureIndex = cursor.getColumnIndex(INDEX_FINGER_FEATURE);
      int fingerVersionIndex = cursor.getColumnIndex(INDEX_FINGER_VERSION);

      int cardIdIndex = cursor.getColumnIndex(INDEX_CARD_ID);
      int userNameIndex = cursor.getColumnIndex(INDEX_USER_NAME);

      if (fingerIndexIndex > -1
          && fingerIdIndex > -1
          && featureIndex > -1
          && fingerVersionIndex > -1
          && cardIdIndex > -1
          && userNameIndex > -1) {

        FingerBean fingerBean = new FingerBean(
            cursor.getInt(fingerIndexIndex),
            cursor.getString(featureIndex),
            cursor.getString(fingerIdIndex),
            cursor.getInt(fingerVersionIndex)
        );
        fingerBean.setCardId(cursor.getString(cardIdIndex));
        fingerBean.setUserName(cursor.getString(userNameIndex));
        list.add(fingerBean);
      }
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
      int fingerIndexIndex = cursor.getColumnIndex(INDEX_FINGER_INDEX);
      int fingerIdIndex = cursor.getColumnIndex(INDEX_FINGER_ID);
      int featureIndex = cursor.getColumnIndex(INDEX_FINGER_FEATURE);
      int fingerVersionIndex = cursor.getColumnIndex(INDEX_FINGER_VERSION);

      int cardIdIndex = cursor.getColumnIndex(INDEX_CARD_ID);
      int userNameIndex = cursor.getColumnIndex(INDEX_USER_NAME);

      if (fingerIndexIndex > -1
          && fingerIdIndex > -1
          && featureIndex > -1
          && fingerVersionIndex > -1
          && cardIdIndex > -1
          && userNameIndex > -1) {

        reVal = new FingerBean(
            cursor.getInt(fingerIndexIndex),
            cursor.getString(featureIndex),
            cursor.getString(fingerIdIndex),
            cursor.getInt(fingerVersionIndex)
        );
        reVal.setCardId(cursor.getString(cardIdIndex));
        reVal.setUserName(cursor.getString(userNameIndex));
      }
    }
    cursor.close();
    LogUtils.d("queryFinger by index：%s, %s", fingerIndex, reVal);
    return reVal;
  }

  /** 根据指纹位置 删除指纹. */
  public int deleteByCardId(String cardId) {
    return sqliteDataBase.delete(FingerPrintSQLiteHelper.TABLE_NAME,
        INDEX_CARD_ID + "=?", new String[] { cardId });
  }

  /** 根据指纹位置 删除指纹. */
  public int deleteByFingerIndex(int fingerIndex) {
    return sqliteDataBase.delete(FingerPrintSQLiteHelper.TABLE_NAME,
        INDEX_FINGER_INDEX + "=?", new String[] { String.valueOf(fingerIndex) });
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