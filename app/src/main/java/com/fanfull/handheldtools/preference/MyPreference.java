package com.fanfull.handheldtools.preference;

import java.util.Collection;
import org.orsoul.baselib.data.preference.PreferenceItem;
import org.orsoul.baselib.data.preference.PreferenceUtil;

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

  public static final PreferenceItem<String> SERVER_IP =
      PreferenceItem.newInstance("SERVER_IP", "192.168.18.100");
  //new PreferenceUtil<>("SERVER_IP", "192.168.18.100");
}
