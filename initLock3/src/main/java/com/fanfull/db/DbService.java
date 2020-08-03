package com.fanfull.db;

import android.content.Context;
import com.fanfull.base.BaseApplication;
import com.fanfull.db.InitInfoDao.Properties;
import java.util.List;

public class DbService {

  private static final String TAG = DbService.class.getSimpleName();
  private static DbService instance;
  private static Context appContext;
  private DaoSession mDaoSession;
  private InitInfoDao mInitDao;

  private DbService() {
  }

  public static DbService getInstance(Context context) {
    if (instance == null) {
      instance = new DbService();
      if (appContext == null) {
        appContext = context.getApplicationContext();
      }
      instance.mDaoSession = BaseApplication.getDaoSession(context);
      instance.mInitDao = instance.mDaoSession.getInitInfoDao();
    }
    return instance;
  }

  /**
   * load one Initinfo info by id
   *
   * @return Initinfo
   */
  public InitInfo loadInitinfo(long id) {
    return mInitDao.load(id);
  }

  /**
   * load all Initinfo info
   *
   * @return list
   */
  public List<InitInfo> loadAllInitinfo() {
    return mInitDao.loadAll();
  }

  /**
   * query list with where clause
   * ex: begin_date_time >= ? AND end_date_time <= ?
   *
   * @param where where clause, include 'where' word
   * @param params query parameters
   */

  public List<InitInfo> queryInitInfos(String where, String... params) {
    return mInitDao.queryRaw(where, params);
  }

  public boolean queryInitedByBagid(String bagid) {
    InitInfo initInfo = null;
    initInfo = mInitDao.queryBuilder()
        .where(Properties.Init_bagid.eq(bagid)).unique();
    return initInfo != null;
  }

  /**
   * insert or update Initinfo
   *
   * @return insert or update Initinfo id
   */
  public long saveInitinfo(InitInfo Initinfo) {
    return mInitDao.insertOrReplace(Initinfo);
  }

  public void updateInitinfo(InitInfo Initinfo) {
    mInitDao.update(Initinfo);
  }

  /**
   * insert or update noteList use transaction
   */
  public void saveInitinfoLists(final List<InitInfo> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    mInitDao.getSession().runInTx(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < list.size(); i++) {
          InitInfo Initinfo = list.get(i);
          mInitDao.insertOrReplace(Initinfo);
        }
      }
    });
  }

  /**
   * delete one initinfo
   */
  public void deleteInitInfo(InitInfo initInfo) {
    mInitDao.delete(initInfo);
  }
}  
