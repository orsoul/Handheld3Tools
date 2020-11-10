package org.orsoul.baselib.util;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.SPUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PreferenceUtilAbs<T> {

  private final String key;
  private final T defaultValue;

  protected PreferenceUtilAbs(String key, T defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
  }

  private static Map<String, PreferenceUtilAbs> allPrefMap = new HashMap<>();

  protected static synchronized <T> PreferenceUtilAbs newInstance(String key, T defaultValue) {
    if (allPrefMap.containsKey(key)) {
      return allPrefMap.get(key);
    }
    PreferenceUtilAbs<T> pref = new PreferenceUtilAbs<>(key, defaultValue);
    allPrefMap.put(key, pref);
    return pref;
  }

  public static Collection<PreferenceUtilAbs> values() {
    return allPrefMap.values();
  }

  public static void loadDefaults() {
    Collection<PreferenceUtilAbs> list = values();
    for (PreferenceUtilAbs pref : list) {
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
    if (noSaveIfExists && contains(key)) {
      return false;
    }
    if (newValue instanceof String) {
      put(key, (String) newValue);
    } else if (newValue instanceof Boolean) {
      put(key, (Boolean) newValue);
    } else if (newValue instanceof Integer) {
      put(key, (Integer) newValue);
    } else if (newValue instanceof Long) {
      put(key, (Long) newValue);
    } else if (newValue instanceof Float) {
      put(key, (Float) newValue);
    } else if (newValue instanceof Set) {
      put(key, (Set) newValue);
    } else {
      return false;
    }

    if (CONFIGURATION_LISTENERS != null && !CONFIGURATION_LISTENERS.isEmpty()) {
      for (SharedPreferencesListener listener : CONFIGURATION_LISTENERS) {
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
      return (T) getString(key, (String) defaultValue);
    } else if (defaultValue instanceof Boolean) {
      Boolean v = getBoolean(key, (Boolean) defaultValue);
      return (T) v;
    } else if (defaultValue instanceof Integer) {
      Integer v = getInt(key, (Integer) defaultValue);
      return (T) v;
    } else if (defaultValue instanceof Long) {
      Long v = getLong(key, (Long) defaultValue);
      return (T) v;
    } else if (defaultValue instanceof Float) {
      Float v = getFloat(key, (Float) defaultValue);
      return (T) v;
    } else if (defaultValue instanceof Set) {
      Set v = getStringSet(key, (Set) defaultValue);
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
    void onConfigurationChanged(PreferenceUtilAbs pref, Object newValue);
  }

  private static final List<SharedPreferencesListener> CONFIGURATION_LISTENERS =
      Collections.synchronizedList(new ArrayList<>());

  public static void addConfigurationListener(SharedPreferencesListener listener) {
    CONFIGURATION_LISTENERS.add(listener);
  }

  public static void removeConfigurationListener(SharedPreferencesListener listener) {
    CONFIGURATION_LISTENERS.remove(listener);
  }

  // =============================================================================

  protected static String PREFERENCE_FILE_NAME = PreferenceUtilAbs.class.getSimpleName();
  private static SPUtils sDefaultSPUtils;

  /**
   * Set the default instance of {@link SPUtils}.
   *
   * @param spUtils The default instance of {@link SPUtils}.
   */
  public static void setDefaultSPUtils(final SPUtils spUtils) {
    sDefaultSPUtils = spUtils;
  }

  public static void setPreferenceFileName(final String spFileName) {
    setDefaultSPUtils(SPUtils.getInstance(spFileName));
  }

  private static SPUtils getDefaultSPUtils() {
    return sDefaultSPUtils != null ? sDefaultSPUtils : SPUtils.getInstance(PREFERENCE_FILE_NAME);
  }

  /**
   * Put the string value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   */
  public static void put(@NonNull final String key, final String value) {
    put(key, value, getDefaultSPUtils());
  }

  /**
   * Put the string value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   */
  public static void put(@NonNull final String key, final String value, final boolean isCommit) {
    put(key, value, isCommit, getDefaultSPUtils());
  }

  /**
   * Return the string value in sp.
   *
   * @param key The key of sp.
   * @return the string value if sp exists or {@code ""} otherwise
   */
  public static String getString(@NonNull final String key) {
    return getString(key, getDefaultSPUtils());
  }

  /**
   * Return the string value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @return the string value if sp exists or {@code defaultValue} otherwise
   */
  public static String getString(@NonNull final String key, final String defaultValue) {
    return getString(key, defaultValue, getDefaultSPUtils());
  }

  /**
   * Put the int value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   */
  public static void put(@NonNull final String key, final int value) {
    put(key, value, getDefaultSPUtils());
  }

  /**
   * Put the int value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   */
  public static void put(@NonNull final String key, final int value, final boolean isCommit) {
    put(key, value, isCommit, getDefaultSPUtils());
  }

  /**
   * Return the int value in sp.
   *
   * @param key The key of sp.
   * @return the int value if sp exists or {@code -1} otherwise
   */
  public static int getInt(@NonNull final String key) {
    return getInt(key, getDefaultSPUtils());
  }

  /**
   * Return the int value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @return the int value if sp exists or {@code defaultValue} otherwise
   */
  public static int getInt(@NonNull final String key, final int defaultValue) {
    return getInt(key, defaultValue, getDefaultSPUtils());
  }

  /**
   * Put the long value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   */
  public static void put(@NonNull final String key, final long value) {
    put(key, value, getDefaultSPUtils());
  }

  /**
   * Put the long value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   */
  public static void put(@NonNull final String key, final long value, final boolean isCommit) {
    put(key, value, isCommit, getDefaultSPUtils());
  }

  /**
   * Return the long value in sp.
   *
   * @param key The key of sp.
   * @return the long value if sp exists or {@code -1} otherwise
   */
  public static long getLong(@NonNull final String key) {
    return getLong(key, getDefaultSPUtils());
  }

  /**
   * Return the long value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @return the long value if sp exists or {@code defaultValue} otherwise
   */
  public static long getLong(@NonNull final String key, final long defaultValue) {
    return getLong(key, defaultValue, getDefaultSPUtils());
  }

  /**
   * Put the float value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   */
  public static void put(@NonNull final String key, final float value) {
    put(key, value, getDefaultSPUtils());
  }

  /**
   * Put the float value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   */
  public static void put(@NonNull final String key, final float value, final boolean isCommit) {
    put(key, value, isCommit, getDefaultSPUtils());
  }

  /**
   * Return the float value in sp.
   *
   * @param key The key of sp.
   * @return the float value if sp exists or {@code -1f} otherwise
   */
  public static float getFloat(@NonNull final String key) {
    return getFloat(key, getDefaultSPUtils());
  }

  /**
   * Return the float value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @return the float value if sp exists or {@code defaultValue} otherwise
   */
  public static float getFloat(@NonNull final String key, final float defaultValue) {
    return getFloat(key, defaultValue, getDefaultSPUtils());
  }

  /**
   * Put the boolean value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   */
  public static void put(@NonNull final String key, final boolean value) {
    put(key, value, getDefaultSPUtils());
  }

  /**
   * Put the boolean value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   */
  public static void put(@NonNull final String key, final boolean value, final boolean isCommit) {
    put(key, value, isCommit, getDefaultSPUtils());
  }

  /**
   * Return the boolean value in sp.
   *
   * @param key The key of sp.
   * @return the boolean value if sp exists or {@code false} otherwise
   */
  public static boolean getBoolean(@NonNull final String key) {
    return getBoolean(key, getDefaultSPUtils());
  }

  /**
   * Return the boolean value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @return the boolean value if sp exists or {@code defaultValue} otherwise
   */
  public static boolean getBoolean(@NonNull final String key, final boolean defaultValue) {
    return getBoolean(key, defaultValue, getDefaultSPUtils());
  }

  /**
   * Put the set of string value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   */
  public static void put(@NonNull final String key, final Set<String> value) {
    put(key, value, getDefaultSPUtils());
  }

  /**
   * Put the set of string value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   */
  public static void put(@NonNull final String key,
      final Set<String> value,
      final boolean isCommit) {
    put(key, value, isCommit, getDefaultSPUtils());
  }

  /**
   * Return the set of string value in sp.
   *
   * @param key The key of sp.
   * @return the set of string value if sp exists
   * or {@code Collections.<String>emptySet()} otherwise
   */
  public static Set<String> getStringSet(@NonNull final String key) {
    return getStringSet(key, getDefaultSPUtils());
  }

  /**
   * Return the set of string value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @return the set of string value if sp exists or {@code defaultValue} otherwise
   */
  public static Set<String> getStringSet(@NonNull final String key,
      final Set<String> defaultValue) {
    return getStringSet(key, defaultValue, getDefaultSPUtils());
  }

  /**
   * Return all values in sp.
   *
   * @return all values in sp
   */
  public static Map<String, ?> getAll() {
    return getAll(getDefaultSPUtils());
  }

  /**
   * Return whether the sp contains the preference.
   *
   * @param key The key of sp.
   * @return {@code true}: yes<br>{@code false}: no
   */
  public static boolean contains(@NonNull final String key) {
    return contains(key, getDefaultSPUtils());
  }

  /**
   * Remove the preference in sp.
   *
   * @param key The key of sp.
   */
  public static void remove(@NonNull final String key) {
    remove(key, getDefaultSPUtils());
  }

  /**
   * Remove the preference in sp.
   *
   * @param key The key of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   */
  public static void remove(@NonNull final String key, final boolean isCommit) {
    remove(key, isCommit, getDefaultSPUtils());
  }

  /**
   * Remove all preferences in sp.
   */
  public static void clear() {
    clear(getDefaultSPUtils());
  }

  /**
   * Remove all preferences in sp.
   *
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   */
  public static void clear(final boolean isCommit) {
    clear(isCommit, getDefaultSPUtils());
  }

  ///////////////////////////////////////////////////////////////////////////
  // dividing line
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Put the string value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key, final String value,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value);
  }

  /**
   * Put the string value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key,
      final String value,
      final boolean isCommit,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value, isCommit);
  }

  /**
   * Return the string value in sp.
   *
   * @param key The key of sp.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the string value if sp exists or {@code ""} otherwise
   */
  public static String getString(@NonNull final String key, @NonNull final SPUtils spUtils) {
    return spUtils.getString(key);
  }

  /**
   * Return the string value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the string value if sp exists or {@code defaultValue} otherwise
   */
  public static String getString(@NonNull final String key,
      final String defaultValue,
      @NonNull final SPUtils spUtils) {
    return spUtils.getString(key, defaultValue);
  }

  /**
   * Put the int value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key, final int value,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value);
  }

  /**
   * Put the int value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key,
      final int value,
      final boolean isCommit,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value, isCommit);
  }

  /**
   * Return the int value in sp.
   *
   * @param key The key of sp.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the int value if sp exists or {@code -1} otherwise
   */
  public static int getInt(@NonNull final String key, @NonNull final SPUtils spUtils) {
    return spUtils.getInt(key);
  }

  /**
   * Return the int value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the int value if sp exists or {@code defaultValue} otherwise
   */
  public static int getInt(@NonNull final String key, final int defaultValue,
      @NonNull final SPUtils spUtils) {
    return spUtils.getInt(key, defaultValue);
  }

  /**
   * Put the long value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key, final long value,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value);
  }

  /**
   * Put the long value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key,
      final long value,
      final boolean isCommit,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value, isCommit);
  }

  /**
   * Return the long value in sp.
   *
   * @param key The key of sp.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the long value if sp exists or {@code -1} otherwise
   */
  public static long getLong(@NonNull final String key, @NonNull final SPUtils spUtils) {
    return spUtils.getLong(key);
  }

  /**
   * Return the long value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the long value if sp exists or {@code defaultValue} otherwise
   */
  public static long getLong(@NonNull final String key, final long defaultValue,
      @NonNull final SPUtils spUtils) {
    return spUtils.getLong(key, defaultValue);
  }

  /**
   * Put the float value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key, final float value,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value);
  }

  /**
   * Put the float value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key,
      final float value,
      final boolean isCommit,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value, isCommit);
  }

  /**
   * Return the float value in sp.
   *
   * @param key The key of sp.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the float value if sp exists or {@code -1f} otherwise
   */
  public static float getFloat(@NonNull final String key, @NonNull final SPUtils spUtils) {
    return spUtils.getFloat(key);
  }

  /**
   * Return the float value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the float value if sp exists or {@code defaultValue} otherwise
   */
  public static float getFloat(@NonNull final String key, final float defaultValue,
      @NonNull final SPUtils spUtils) {
    return spUtils.getFloat(key, defaultValue);
  }

  /**
   * Put the boolean value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key, final boolean value,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value);
  }

  /**
   * Put the boolean value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key,
      final boolean value,
      final boolean isCommit,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value, isCommit);
  }

  /**
   * Return the boolean value in sp.
   *
   * @param key The key of sp.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the boolean value if sp exists or {@code false} otherwise
   */
  public static boolean getBoolean(@NonNull final String key, @NonNull final SPUtils spUtils) {
    return spUtils.getBoolean(key);
  }

  /**
   * Return the boolean value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the boolean value if sp exists or {@code defaultValue} otherwise
   */
  public static boolean getBoolean(@NonNull final String key,
      final boolean defaultValue,
      @NonNull final SPUtils spUtils) {
    return spUtils.getBoolean(key, defaultValue);
  }

  /**
   * Put the set of string value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key, final Set<String> value,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value);
  }

  /**
   * Put the set of string value in sp.
   *
   * @param key The key of sp.
   * @param value The value of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void put(@NonNull final String key,
      final Set<String> value,
      final boolean isCommit,
      @NonNull final SPUtils spUtils) {
    spUtils.put(key, value, isCommit);
  }

  /**
   * Return the set of string value in sp.
   *
   * @param key The key of sp.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the set of string value if sp exists
   * or {@code Collections.<String>emptySet()} otherwise
   */
  public static Set<String> getStringSet(@NonNull final String key,
      @NonNull final SPUtils spUtils) {
    return spUtils.getStringSet(key);
  }

  /**
   * Return the set of string value in sp.
   *
   * @param key The key of sp.
   * @param defaultValue The default value if the sp doesn't exist.
   * @param spUtils The instance of {@link SPUtils}.
   * @return the set of string value if sp exists or {@code defaultValue} otherwise
   */
  public static Set<String> getStringSet(@NonNull final String key,
      final Set<String> defaultValue,
      @NonNull final SPUtils spUtils) {
    return spUtils.getStringSet(key, defaultValue);
  }

  /**
   * Return all values in sp.
   *
   * @param spUtils The instance of {@link SPUtils}.
   * @return all values in sp
   */
  public static Map<String, ?> getAll(@NonNull final SPUtils spUtils) {
    return spUtils.getAll();
  }

  /**
   * Return whether the sp contains the preference.
   *
   * @param key The key of sp.
   * @param spUtils The instance of {@link SPUtils}.
   * @return {@code true}: yes<br>{@code false}: no
   */
  public static boolean contains(@NonNull final String key, @NonNull final SPUtils spUtils) {
    return spUtils.contains(key);
  }

  /**
   * Remove the preference in sp.
   *
   * @param key The key of sp.
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void remove(@NonNull final String key, @NonNull final SPUtils spUtils) {
    spUtils.remove(key);
  }

  /**
   * Remove the preference in sp.
   *
   * @param key The key of sp.
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void remove(@NonNull final String key, final boolean isCommit,
      @NonNull final SPUtils spUtils) {
    spUtils.remove(key, isCommit);
  }

  /**
   * Remove all preferences in sp.
   *
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void clear(@NonNull final SPUtils spUtils) {
    spUtils.clear();
  }

  /**
   * Remove all preferences in sp.
   *
   * @param isCommit True to use {@link SharedPreferences.Editor#commit()},
   * false to use {@link SharedPreferences.Editor#apply()}
   * @param spUtils The instance of {@link SPUtils}.
   */
  public static void clear(final boolean isCommit, @NonNull final SPUtils spUtils) {
    spUtils.clear(isCommit);
  }
}
