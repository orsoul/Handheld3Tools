package com.fanfull.libhard.lock3;

import com.apkfuns.logutils.LogUtils;
import java.util.List;
import org.orsoul.baselib.util.ThreadUtil;
import org.orsoul.baselib.util.lock.Lock3Bean;

public class ReadLockTask extends ThreadUtil.ThreadRunnable {
  private byte[] uid = new byte[7];

  private byte[] tid = new byte[12];
  private byte[] epc = new byte[12];

  @Override public void run() {
    Lock3Operation lock3Operation = Lock3Operation.getInstance();
    int res = lock3Operation.readUidAndTid(uid, tid, epc);
    if (res != 0) {
      onFailed(res);
      return;
    }

    /* 读nfc 长度固定的数据区 */
    Lock3Bean lock3Bean = new Lock3Bean();
    lock3Bean.addAllSa();
    boolean readLockNfc = lock3Operation.readLockNfc(lock3Bean, false);
    if (!readLockNfc) {
      onFailed(-5);
      return;
    }

    /* 读nfc 长度不固定的数据区 */
    byte[] buff = lock3Bean.getInfoUnit(Lock3Bean.SA_STATUS).buff;
    int handOverNum = buff[2];
    Lock3Bean.InfoUnit handoverInfo = null;
    if (1 <= handOverNum && handOverNum <= 5) {
      Lock3Bean bean = new Lock3Bean();
      bean.addOneSa(Lock3Bean.SA_HAND_OVER, handOverNum * 7 * 4);
      readLockNfc = lock3Operation.readLockNfc(bean, false);
      if (readLockNfc) {
        handoverInfo = bean.getInfoUnit(Lock3Bean.SA_HAND_OVER);
      }
    }

    List<Lock3Bean.InfoUnit> infoList = lock3Bean.getWillDoList();
    if (handoverInfo != null) {
      infoList.add(handoverInfo);
    }
    onSuccess(uid, tid, epc, infoList);
  }

  protected void onSuccess(byte[] uid, byte[] tid, byte[] epc, List<Lock3Bean.InfoUnit> infoList) {
  }

  /**
   * 读nfc失败时 回调.
   *
   * @param errorCode 结果码<br />
   * * 结果码=0 执行成功<br/>
   * * 结果码=-1 参数错误<br/>
   * * 结果码=-2 nfc寻卡失败<br/>
   * * 结果码=-3 读tid失败<br/>
   * * 结果码=-4 读epc失败<br/>
   */
  protected void onFailed(int errorCode) {
    LogUtils.d("onFailed:%s", errorCode);
  }
}
