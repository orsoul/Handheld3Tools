package com.fanfull.handheldtools.preference;

import org.orsoul.baselib.data.preference.PreferenceItem;
import org.orsoul.baselib.data.preference.PreferenceUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class MyPreference {
  static {
    //PREFERENCE_FILE_NAME = MyPreferenceUtil.class.getSimpleName();
    PreferenceUtil.setPreferenceFileName(MyPreference.class.getSimpleName());
  }

  public static Collection<PreferenceItem> values() {
    return PreferenceItem.values();
  }

  public static final PreferenceItem<Boolean> FIRST_USE =
      PreferenceItem.newInstance("FIRST_USE", true);

  public static final PreferenceItem<String> SERVER_IP1 =
      PreferenceItem.newInstance("SERVER_IP1", "192.168.18.100");
  public static final PreferenceItem<Integer> SERVER_PORT1 =
      PreferenceItem.newInstance("SERVER_PORT1", 10001);
  //new PreferenceUtil<>("SERVER_IP", "192.168.18.100");
  public static final PreferenceItem<Set<String>> HIS_IP =
      PreferenceItem.newInstance("HIS_IP", Collections.EMPTY_SET);
  public static final PreferenceItem<Set<Integer>> HIS_PORT =
      PreferenceItem.newInstance("HIS_PORT", Collections.EMPTY_SET);

  public static final PreferenceItem<String> NETTY_OP =
      PreferenceItem.newInstance("NETTY_OP", null);

  public static final PreferenceItem<String> FUNC_MAIN =
      PreferenceItem.newInstance("FUNC_MAIN", null);
}
