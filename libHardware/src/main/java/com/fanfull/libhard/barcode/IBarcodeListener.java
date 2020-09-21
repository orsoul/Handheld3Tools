package com.fanfull.libhard.barcode;

import com.fanfull.libhard.IOperationListener;

public interface IBarcodeListener extends IOperationListener {
  void onScan();

  void onStopScan();
}
