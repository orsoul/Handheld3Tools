package com.fanfull.handheldtools.ui.view;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;

public class DialogUtil {
  private Context context;
  private LoadingPopupView loadingPopupView;
  private final String LOADING_INFO = "正在加载...";

  public DialogUtil(Context context) {
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
}
