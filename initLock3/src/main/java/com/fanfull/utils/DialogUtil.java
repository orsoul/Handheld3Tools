package com.fanfull.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.contexts.MyContexts;
import com.fanfull.contexts.StaticString;
import com.fanfull.initbag3.R;
import com.fanfull.socket.ReplyParser;
import com.fanfull.view.SetIpFullScreenPopup;
import com.lxj.xpopup.XPopup;

public class DialogUtil {
  private Activity mAct;

  private ProgressDialog mProgressDia;
  private AlertDialog mAlertDialog;

  public DialogUtil(Activity act) {
    mAct = act;
  }

  private void createProgressDialog() {
    mProgressDia = new ProgressDialog(mAct);
    mProgressDia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDia.setIndeterminate(false);
    mProgressDia.setCancelable(false);
    mProgressDia.setCanceledOnTouchOutside(true);
  }

  public void setProgressDialogTitle(CharSequence text) {
    if (null == mProgressDia) {
      createProgressDialog();
    }
    mProgressDia.setTitle(text);
  }

  public void setProgressDialogCancelListener(OnCancelListener listener) {
    if (null == mProgressDia) {
      createProgressDialog();
    }
    mProgressDia.setOnCancelListener(listener);
  }

  public void showProgressDialog() {
    showProgressDialog(null);
  }

  public void showProgressDialog(CharSequence message) {
    if (null == mProgressDia) {
      createProgressDialog();
    }
    mProgressDia.setMessage(message);

    if (!mProgressDia.isShowing()) {
      mProgressDia.show();
    }
  }

  public boolean progressDialogIsShowing() {
    if (null == mProgressDia) {
      return false;
    }
    return mProgressDia.isShowing();
  }

  public void dismissProgressDialog() {
    if (mProgressDia != null && mProgressDia.isShowing()) {
      mProgressDia.dismiss();
    }
  }

  /**
   * 点击 确定后 退出 当前 activity
   */
  public void showDialogFinishActivity(String info) {
    if (null == mAct || mAct.isFinishing()) {
      LogUtils.d("dialog is finish");
      return;
    }
    Builder builder = new Builder(mAct);
    builder.setTitle("提示");
    builder.setMessage(info);
    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
        mAct.finish();
      }
    });
    builder.show();
  }

  /**
   *
   */
  public void showDialog(Object info) {
    LogUtils.v("TAG", " data=ppp" + " info = " + info.toString());
    if (mAct == null) {
      return;
    }
    if (null == mAlertDialog) {
      Builder builder = new Builder(mAct);
      builder.setIcon(R.drawable.dialog_title_alarm_48)
          .setTitle(MyContexts.TEXT_DIALOG_TITLE)
          .setMessage(String.valueOf(info))
          .setNegativeButton(MyContexts.TEXT_OK, null);
      mAlertDialog = builder.create();
    } else {
      mAlertDialog.setMessage(String.valueOf(info));
    }
    mAlertDialog.show();
  }

  public void showDialogOnUiThread(Object info) {
    mAct.runOnUiThread(() -> showDialog(info));
  }

  public boolean isShowingInfoDialog() {
    if (mAlertDialog != null) {
      return mAlertDialog.isShowing();
    }
    return false;
  }

  public void dismissAlertDialog() {
    if (mAlertDialog != null && mAlertDialog.isShowing()) {
      mAlertDialog.dismiss();
    }
  }

  /**
   * @param posText 确定按钮 文字
   * @param negText 取消按钮 文字
   */
  public void showDialog2Button(String info, String posText, String negText,
      DialogInterface.OnClickListener posListener, DialogInterface.OnClickListener negListener) {
    if (null == mAct || mAct.isFinishing()) {
      return;
    }
    Builder builder = new Builder(mAct);
    builder.setTitle(MyContexts.TEXT_DIALOG_TITLE);
    builder.setMessage(info);
    builder.setPositiveButton(posText, posListener);
    builder.setNegativeButton(negText, negListener);
    if (negListener != null) {
      builder.setOnCancelListener(dialog -> negListener.onClick(dialog, 0));
    }
    builder.show();
  }

  public void showDialog2ButtonOnUiThread(String info, String posText, String negText,
      DialogInterface.OnClickListener posListener, DialogInterface.OnClickListener negListener) {
    mAct.runOnUiThread(() -> showDialog2Button(info, posText, negText, posListener, negListener));
  }

  public void showDialog2Button(String info, String posText,
      DialogInterface.OnClickListener posListener) {
    showDialog2Button(info, posText, "取消", posListener,
        null);
  }

  public void showDialog2ButtonOnUiThread(String info, String posText,
      DialogInterface.OnClickListener posListener) {
    mAct.runOnUiThread(() -> showDialog2Button(info, posText, posListener));
  }

  /**
   * 弹出 服务器回复 信息
   */
  public String showReplyDialog() {

    String info = ReplyParser.parseReply(StaticString.information);
    showDialog(info);
    return info;
  }

  public void showNegativeReplyDialog(String info, String btnText) {
    if (null == mAct || mAct.isFinishing()) {
      LogUtils.d("dialog is finish");
      return;
    }
    Builder builder = new Builder(mAct);
    builder.setIcon(R.drawable.dialog_title_alarm_48);
    builder.setTitle(MyContexts.TEXT_DIALOG_TITLE);
    builder.setMessage(info);
    builder.setNegativeButton(btnText, null);
    builder.show();
  }

  public void showOneButtomDialog(String info, String btnText,
      DialogInterface.OnClickListener listener) {
    if (null == mAct || mAct.isFinishing()) {
      return;
    }
    Builder builder = new Builder(mAct);
    builder.setIcon(R.drawable.dialog_title_alarm_48);
    builder.setTitle(MyContexts.TEXT_DIALOG_TITLE);
    builder.setMessage(info);
    builder.setNegativeButton(btnText, listener);
    builder.show();
  }

  public void showSalverInfoDialog(String money, String bagNum,
      DialogInterface.OnClickListener listener) {
    if (null == mAct || mAct.isFinishing()) {
      LogUtils.d("dialog is finish");
      return;
    }
    Builder builder = new Builder(mAct);
    builder.setTitle("托盘信息确认");
    builder.setMessage(money + " 圆  " + bagNum + " 袋");
    builder.setNegativeButton(MyContexts.TEXT_CANCEL, null);
    builder.setPositiveButton(MyContexts.TEXT_OK, listener);
    builder.show();
  }

  public void destroy() {
    dismissProgressDialog();
    dismissAlertDialog();
    mProgressDia = null;
    mAlertDialog = null;
    mAct = null;
  }

  public void showWifiNotConnect(DialogInterface.OnClickListener listener) {
    showDialog2Button("WiFi未连接，请检查网络设置", "设置网络", "离线模式", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        String packageName = "com.android.settings";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.setClassName(packageName, packageName + ".wifi.WifiSettings");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mAct.startActivityForResult(intent, 1024);
      }
    }, listener);
  }

  public void showSetIpPopup(SetIpFullScreenPopup.SetIpCallback callback) {
    new XPopup.Builder(mAct).asCustom(new SetIpFullScreenPopup(mAct, callback)).show();
  }
}
