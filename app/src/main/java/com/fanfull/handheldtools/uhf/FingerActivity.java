package com.fanfull.handheldtools.uhf;

import android.os.Bundle;
import android.view.KeyEvent;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.base.InitModuleActivity;
import com.fanfull.libhard.finger.impl.FingerprintController;

public class FingerActivity extends InitModuleActivity {

  private FingerprintController fingerprintController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_finger);
  }

  @Override
  protected void initModule() {
    fingerprintController = FingerprintController.getInstance();
    fingerprintController.open();
  }

  @Override
  protected void onEnterPress() {
    if (fingerprintController.isSearch()) {
      fingerprintController.stopSearchFingerPrint();
    } else {
      fingerprintController.startSearchFingerPrint();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onDestroy() {
    fingerprintController.release();
    super.onDestroy();
  }
}
