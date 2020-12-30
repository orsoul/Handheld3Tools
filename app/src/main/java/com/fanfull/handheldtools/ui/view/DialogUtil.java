package com.fanfull.handheldtools.ui.view;

import android.content.Context;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;

public class DialogUtil {
  private Context context;
  private LoadingPopupView loadingPopupView;
  private final String LOADING_INFO = "正在加载...";

  public DialogUtil(Context context) {
    this.context = context;
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
    loadingPopupView.dismiss();
  }

  public void destroy() {
    if (loadingPopupView != null) {
    }
    context = null;
  }
}
