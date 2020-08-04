package com.fanfull.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table INIT_INFO.
 */
public class InitInfoDao extends AbstractDao<InitInfo, Long> {

  public static final String TABLENAME = "INIT_INFO";

  /**
   * Properties of entity InitInfo.<br/>
   * Can be used for QueryBuilder and for referencing column names.
   */
  public static class Properties {
    public final static Property Id = new Property(0, Long.class, "id", true, "_id");
    public final static Property Init_bagid =
        new Property(1, String.class, "init_bagid", false, "INIT_BAGID");
  }

  ;

  public InitInfoDao(DaoConfig config) {
    super(config);
  }

  public InitInfoDao(DaoConfig config, DaoSession daoSession) {
    super(config, daoSession);
  }

  /** Creates the underlying database table. */
  public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
    String constraint = ifNotExists ? "IF NOT EXISTS " : "";
    db.execSQL("CREATE TABLE " + constraint + "'INIT_INFO' (" + //
        "'_id' INTEGER PRIMARY KEY ," + // 0: id
        "'INIT_BAGID' TEXT NOT NULL );"); // 1: init_bagid
  }

  /** Drops the underlying database table. */
  public static void dropTable(SQLiteDatabase db, boolean ifExists) {
    String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'INIT_INFO'";
    db.execSQL(sql);
  }

  /** @inheritdoc */
  @Override
  protected void bindValues(SQLiteStatement stmt, InitInfo entity) {
    stmt.clearBindings();

    Long id = entity.getId();
    if (id != null) {
      stmt.bindLong(1, id);
    }
    stmt.bindString(2, entity.getInit_bagid());
  }

  /** @inheritdoc */
  @Override
  public Long readKey(Cursor cursor, int offset) {
    return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
  }

  /** @inheritdoc */
  @Override
  public InitInfo readEntity(Cursor cursor, int offset) {
    InitInfo entity = new InitInfo( //
        cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
        cursor.getString(offset + 1) // init_bagid
    );
    return entity;
  }

  /** @inheritdoc */
  @Override
  public void readEntity(Cursor cursor, InitInfo entity, int offset) {
    entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
    entity.setInit_bagid(cursor.getString(offset + 1));
  }

  /** @inheritdoc */
  @Override
  protected Long updateKeyAfterInsert(InitInfo entity, long rowId) {
    entity.setId(rowId);
    return rowId;
  }

  /** @inheritdoc */
  @Override
  public Long getKey(InitInfo entity) {
    if (entity != null) {
      return entity.getId();
    } else {
      return null;
    }
  }

  /** @inheritdoc */
  @Override
  protected boolean isEntityUpdateable() {
    return true;
  }
}