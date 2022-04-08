package com.fanfull.handheldtools.main;

import android.app.Activity;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.ui.AboutActivity;
import com.fanfull.handheldtools.ui.BagCheckActivity;
import com.fanfull.handheldtools.ui.BagSearchActivity;
import com.fanfull.handheldtools.ui.BarcodeActivity;
import com.fanfull.handheldtools.ui.CoverBagActivity;
import com.fanfull.handheldtools.ui.FingerActivity;
import com.fanfull.handheldtools.ui.InitBag3Activity;
import com.fanfull.handheldtools.ui.NettyActivity;
import com.fanfull.handheldtools.ui.NfcActivity;
import com.fanfull.handheldtools.ui.OldBagActivity;
import com.fanfull.handheldtools.ui.SocketActivity;
import com.fanfull.handheldtools.ui.SoundActivity;
import com.fanfull.handheldtools.ui.UhfActivity;
import com.fanfull.handheldtools.ui.UhfLotScanActivity;
import com.fanfull.handheldtools.ui.ZcLockActivity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.blankj.utilcode.util.StringUtils.getString;

public class FuncNode extends BaseNode {
  String name;
  Class<? extends Activity> activityClass;

  public FuncNode(String name, Class<? extends Activity> activityClass) {
    this.name = name;
    this.activityClass = activityClass;
  }

  public String getName() {
    return name;
  }

  public Class<? extends Activity> getActivityClass() {
    return activityClass;
  }

  @Nullable @Override public List<BaseNode> getChildNode() {
    return null;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FuncNode funcBean = (FuncNode) o;
    return Objects.equals(name, funcBean.name) &&
        Objects.equals(activityClass, funcBean.activityClass);
  }

  @Override public int hashCode() {
    return Objects.hash(name, activityClass);
  }

  public static List<BaseNode> genModuleList() {
    List<BaseNode> list = new ArrayList<>();
    FuncNode funcBean;

    funcBean = new FuncNode(getString(R.string.main_check_bag), BagCheckActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_uhf), UhfActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_nfc), NfcActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_finger), FingerActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_barcode), BarcodeActivity.class);
    list.add(funcBean);

    return list;
  }

  public static List<BaseNode> genBusinessList() {
    List<BaseNode> list = new ArrayList<>();
    FuncNode funcBean;

    funcBean = new FuncNode(getString(R.string.main_old_bag), OldBagActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_init_bag3), InitBag3Activity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_cover_bag), CoverBagActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_zc), ZcLockActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_bag_search), BagSearchActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_uhf_lot), UhfLotScanActivity.class);
    list.add(funcBean);

    return list;
  }

  public static List<BaseNode> genOtherList() {
    List<BaseNode> list = new ArrayList<>();
    FuncNode funcBean;

    funcBean = new FuncNode(getString(R.string.main_netty), NettyActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_socket), SocketActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_sound), SoundActivity.class);
    list.add(funcBean);

    funcBean = new FuncNode(getString(R.string.main_about), AboutActivity.class);
    list.add(funcBean);

    return list;
  }

  public static final int FUNC_TYPE_UHF = 10001;
  public static final int FUNC_TYPE_NFC = 10002;

  public static FuncNode newInstance(int funcType) {
    switch (funcType) {
      case FUNC_TYPE_UHF:
        return new FuncNode(getString(R.string.main_uhf), UhfActivity.class);
      case FUNC_TYPE_NFC:
        return new FuncNode(getString(R.string.main_uhf), NfcActivity.class);
    }
    return null;
  }
}
