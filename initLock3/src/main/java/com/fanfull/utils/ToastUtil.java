package com.fanfull.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.fanfull.base.BaseApplication;
import com.fanfull.contexts.StaticString;
import com.fanfull.initbag3.R;

/**
 * 弹出Toast的一个工具类，这里主要是增加了对系统Toast背景的修改
 *
 * @author Administrator
 */
public class ToastUtil {
  public static ProgressDialog xh_pDialog = null;

  /**
   * @Description 在屏幕中间 弹出土司 , LENGTH_SHORT
   */
  public static void showToastInCenter(Object text) {
    Toast toast =
        Toast.makeText(BaseApplication.getContext(), String.valueOf(text), Toast.LENGTH_SHORT);
    toast.setGravity(Gravity.CENTER, 0, 0);
    toast.show();
  }

  /**
   * @param context 上下文对象
   * @param msg 要显示的信息
   * @param timeTag 时间参数 若是“s”表示短时间显示 若是“l”（小写L）表示长时间显示
   * @Des
   */
  public static void toast(Context context, String msg, int timeTag) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View layout = inflater.inflate(R.layout.mytoast, null);
    TextView title = (TextView) layout.findViewById(R.id.tvTitleToast);
    title.setText("Attention");
    TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
    text.setText(msg);

    Toast toast = new Toast(context);
    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
    toast.setDuration(timeTag);
    toast.setView(layout);
    toast.show();
  }

  /**
   * @param @param context
   * @param @return 设定文件
   * @return Boolean 返回类型
   * @throws
   * @Title: judgeNET
   * @Description: 等待socket通信回复
   */
  public static void showWaiting(Activity context) {
    xh_pDialog = new ProgressDialog(context);
    // 设置进度条风格，风格为圆形，旋转的
    xh_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    // 设置ProgressDialog 标题
    xh_pDialog.setTitle("等待操作中••••••");
    // 设置ProgressDialog 的进度条是否不明确 false 就是不设置为不明确
    xh_pDialog.setIndeterminate(false);
    // 设置ProgressDialog 是否可以按退回键取消
    xh_pDialog.setCancelable(true);
    xh_pDialog.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        StaticString.netThread_flag = false;
        xh_pDialog = null;
        Log.v("MyToast", "netThread_flag" + StaticString.netThread_flag);
      }
    });
    xh_pDialog.show();
  }

  /**
   * @author daniel
   * @date 17/7-15 15:19
   * @descibe 提示用户对话框
   */
  public static void showImply(Context context, String message) {
    xh_pDialog = new ProgressDialog(context);
    // 设置进度条风格，风格为圆形，旋转的
    xh_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    // 设置ProgressDialog 标题
    xh_pDialog.setTitle(message);
    xh_pDialog.setMessage("正在操作中••••••");
    // 设置ProgressDialog 的进度条是否不明确 false 就是不设置为不明确
    xh_pDialog.setIndeterminate(false);
    // 设置ProgressDialog 是否可以按退回键取消
    xh_pDialog.setCancelable(true);
    xh_pDialog.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        StaticString.netThread_flag = false;
        xh_pDialog = null;
        Log.v("MyToast", "netThread_flag" + StaticString.netThread_flag);
      }
    });
    xh_pDialog.show();
  }

  /**
   * @return void 返回类型
   * @throws
   * @Title: closeWaiting
   * @Description: 取消progressdialog的显示
   */
  public static void closeWaiting() {
    if (xh_pDialog != null && xh_pDialog.isShowing()) {
      xh_pDialog.dismiss();
    }
  }

  /**
   * @param @return 设定文件
   * @return Boolean 返回类型
   * @throws
   * @Title: judgePD
   * @Description: 判断progressdialog的显示状态来决定是否取消网络连接
   */
  public static Boolean judgePD() {
    if (xh_pDialog != null) {
      if (xh_pDialog.isShowing()) {
        return true;
      } else {
        return false;
      }
    }
    return true;
  }
}
