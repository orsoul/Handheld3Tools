package com.fanfull.handheldtools.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;

public class DialogUtil {
  private Activity context;
  private LoadingPopupView loadingPopupView;
  private final String LOADING_INFO = "正在加载...";

  public DialogUtil(Activity context) {
    this.context = context;
  }

  public void setLoadingPopupView(LoadingPopupView loadingView) {
    if (loadingPopupView != null) {
      loadingPopupView.dismiss();
      loadingPopupView.destroy();
    }
    loadingPopupView = loadingView;
  }

  public void showLoadingView(String msg) {
    if (loadingPopupView == null) {
      loadingPopupView = new XPopup.Builder(context)
          .asLoading(LOADING_INFO);
    }
    loadingPopupView.setTitle(msg);
    loadingPopupView.show();
  }

  public void showLoadingView() {
    showLoadingView(LOADING_INFO);
  }

  public void dismissLoadingView() {
    if (loadingPopupView != null && loadingPopupView.isShow()) {
      loadingPopupView.dismiss();
    }
  }

  AlertDialog alertDialog;

  public void showDialog(String info) {
    if (alertDialog == null) {
      alertDialog = new AlertDialog.Builder(context)
          .setMessage(info)
          .setPositiveButton("确定", null)
          .create();
    }
    if (!alertDialog.isShowing()) {
      alertDialog.setMessage(info);
      alertDialog.show();
    }
  }

  public void destroy() {
    if (loadingPopupView != null) {
      loadingPopupView.destroy();
    }
    if (alertDialog != null) {
      alertDialog.dismiss();
    }
    context = null;
  }

  public static void showInfo(Context context, String info) {
    new XPopup.Builder(context)
        .autoDismiss(true)
        .dismissOnBackPressed(true)
        .isDestroyOnDismiss(true)
        .asConfirm(info, null, null).show();
  }

  public static android.app.AlertDialog createAndShowDialog(Activity act, boolean cancelable,
      String title,
      String info,
      String posText, String negText, String neuText, MyDialogOnClickListener listener) {
    if (null == act || act.isFinishing()) {
      return null;
    }
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(act);
    builder.setTitle(title);
    builder.setMessage(info);
    if (!TextUtils.isEmpty(posText)) {
      builder.setPositiveButton(posText, listener);
    }
    if (!TextUtils.isEmpty(negText)) {
      builder.setNegativeButton(negText, listener);
    }
    if (!TextUtils.isEmpty(neuText)) {
      builder.setNeutralButton(neuText, listener);
    }
    builder.setCancelable(cancelable);
    builder.setOnCancelListener(listener);
    android.app.AlertDialog alertDialog = builder.create();
    alertDialog.show();
    return alertDialog;
  }

  public android.app.AlertDialog createAndShowDialog(boolean cancelable, String title, String info,
      String posText, String negText, String neuText, MyDialogOnClickListener listener) {
    return createAndShowDialog(context, cancelable, title, info, posText, negText, neuText,
        listener);
  }

  public interface MyDialogOnClickListener
      extends DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    @Override default void onClick(DialogInterface dialog, int which) {
      switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
          onClickPositive(dialog);
          break;
        case DialogInterface.BUTTON_NEGATIVE:
          onClickNegative(dialog);
          break;
        case DialogInterface.BUTTON_NEUTRAL:
          onClickNeutral(dialog);
          break;
      }
    }

    @Override default void onCancel(DialogInterface dialog) {
      onCancel(dialog);
    }

    default void onClickPositive(DialogInterface dialog) {
    }

    default void onClickNegative(DialogInterface dialog) {
    }

    default void onClickNeutral(DialogInterface dialog) {
    }
  }
}
