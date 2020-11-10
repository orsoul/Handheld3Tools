package org.orsoul.baselib.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PreferenceItem<T> {
  private final String key;
  private final T defaultValue;

  private PreferenceItem(String key, T defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
  }

  private static Map<String, PreferenceItem> allPrefMap = new HashMap<>();

  public static synchronized <T> PreferenceItem newInstance(String key, T defaultValue) {
    if (allPrefMap.containsKey(key)) {
      return allPrefMap.get(key);
    }
    PreferenceItem<T> pref = new PreferenceItem<>(key, defaultValue);
    allPrefMap.put(key, pref);
    return pref;
  }

  public static Collection<PreferenceItem> values() {
    return allPrefMap.values();
  }

  public static void loadDefaults() {
    Collection<PreferenceItem> list = values();
    for (PreferenceItem pref : list) {
      pref.put(pref.getDefaultValue(), true);
    }
  }

  public String getKey() {
    return key;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public boolean put(T newValue, boolean noSaveIfExists) {
    if (noSaveIfExists && PreferenceUtil.contains(key)) {
      return false;
    }
    if (newValue instanceof String) {
      PreferenceUtil.put(key, (String) newValue);
    } else if (newValue instanceof Boolean) {
      PreferenceUtil.put(key, (Boolean) newValue);
    } else if (newValue instanceof Integer) {
      PreferenceUtil.put(key, (Integer) newValue);
    } else if (newValue instanceof Long) {
      PreferenceUtil.put(key, (Long) newValue);
    } else if (newValue instanceof Float) {
      PreferenceUtil.put(key, (Float) newValue);
    } else if (newValue instanceof Set) {
      PreferenceUtil.put(key, (Set) newValue);
    } else {
      return false;
    }

    if (CONFIGURATION_LISTENERS != null && !CONFIGURATION_LISTENERS.isEmpty()) {
      for (PreferenceItem.SharedPreferencesListener listener : CONFIGURATION_LISTENERS) {
        listener.onConfigurationChanged(this, newValue);
      }
    }
    return true;
  }

  public boolean put(T newValue) {
    return put(newValue, false);
  }

  public T getValue(T defaultValue) {
    if (defaultValue instanceof String) {
      return (T) PreferenceUtil.getString(key, (String) defaultValue);
    } else if (defaultValue instanceof Boolean) {
      Boolean v = PreferenceUtil.getBoolean(key, (Boolean) defaultValue);
      return (T) v;
    } else if (defaultValue instanceof Integer) {
      Integer v = PreferenceUtil.getInt(key, (Integer) defaultValue);
      return (T) v;
    } else if (defaultValue instanceof Long) {
      Long v = PreferenceUtil.getLong(key, (Long) defaultValue);
      return (T) v;
    } else if (defaultValue instanceof Float) {
      Float v = PreferenceUtil.getFloat(key, (Float) defaultValue);
      return (T) v;
    } else if (defaultValue instanceof Set) {
      Set v = PreferenceUtil.getStringSet(key, (Set) defaultValue);
      return (T) v;
    }
    return null;
  }

  public T getValue() {
    return getValue(defaultValue);
  }

  public static <T> boolean put(PreferenceUtilAbs<T> pref, T newValue, boolean noSaveIfExists) {
    return pref.put(newValue, noSaveIfExists);
  }

  public static <T> boolean put(PreferenceUtilAbs<T> pref, T newValue) {
    return pref.put(newValue, false);
  }

  public static void put(Map<PreferenceUtilAbs, Object> prefMap, boolean noSaveIfExists) {
    for (PreferenceUtilAbs pref : prefMap.keySet()) {
      pref.put(prefMap.get(pref), noSaveIfExists);
    }
  }

  public static void put(Map<PreferenceUtilAbs, Object> prefMap) {
    put(prefMap, false);
  }

  // ========================================监听器=====================================

  public interface SharedPreferencesListener {
    void onConfigurationChanged(PreferenceItem pref, Object newValue);
  }

  private static final List<SharedPreferencesListener> CONFIGURATION_LISTENERS =
      Collections.synchronizedList(new ArrayList<>());

  public static void addConfigurationListener(PreferenceItem.SharedPreferencesListener listener) {
    CONFIGURATION_LISTENERS.add(listener);
  }

  public static void removeConfigurationListener(
      PreferenceItem.SharedPreferencesListener listener) {
    CONFIGURATION_LISTENERS.remove(listener);
  }
}