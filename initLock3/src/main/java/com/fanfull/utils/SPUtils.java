package com.fanfull.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.fanfull.base.BaseApplication;
import com.fanfull.contexts.MyContexts;

/**
 * @author Administrator
 * <p>
 * SharedPreferences 工具类
 */
public class SPUtils {

  //    public static String MyContexts.PREFERENCE_NAME = "Cniao_Pref_Common";

  /**
   * put int preferences
   *
   * @param key The name of the preference to modify
   * @param value The new value for the preference
   * @return True if the new values were successfully written to persistent storage.
   */
  public static boolean putInt(Context context, String key, int value) {
    SharedPreferences settings =
        context.getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putInt(key, value);
    return editor.commit();
  }

  public static boolean putInt(String key, int value) {
    SharedPreferences settings = BaseApplication.getContext()
        .getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putInt(key, value);
    return editor.commit();
  }

  /**
   * get int preferences
   *
   * @param key The name of the preference to retrieve
   * @param defaultValue Value to return if this preference does not exist
   * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a
   * preference with
   * this name that is not a int
   */
  public static int getInt(Context context, String key, int defaultValue) {
    SharedPreferences settings =
        context.getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    return settings.getInt(key, defaultValue);
  }

  public static int getInt(String key, int defaultValue) {
    SharedPreferences settings = BaseApplication.getContext()
        .getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    return settings.getInt(key, defaultValue);
  }

  /**
   * get int preferences
   *
   * @param key The name of the preference to retrieve
   * @return The preference value if it exists, or -1. Throws ClassCastException if there is a
   * preference with this
   * name that is not a int
   * @see #getInt(android.content.Context, String, int)
   */
  public static int getInt(Context context, String key) {
    return getInt(context, key, -1);
  }

  public static int getInt(String key) {
    return getInt(key, -1);
  }

  /**
   * put boolean preferences
   *
   * @param key The name of the preference to modify
   * @param value The new value for the preference
   * @return True if the new values were successfully written to persistent storage.
   */
  public static boolean putBoolean(Context context, String key, boolean value) {
    SharedPreferences settings =
        context.getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putBoolean(key, value);
    return editor.commit();
  }

  public static boolean putBoolean(String key, boolean value) {
    SharedPreferences settings = BaseApplication.getContext()
        .getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putBoolean(key, value);
    return editor.commit();
  }

  /**
   * get boolean preferences, default is false
   *
   * @param key The name of the preference to retrieve
   * @return The preference value if it exists, or false. Throws ClassCastException if there is a
   * preference with this
   * name that is not a boolean
   * @see #getBoolean(android.content.Context, String, boolean)
   */
  public static boolean getBoolean(Context context, String key) {
    return getBoolean(context, key, false);
  }

  public static boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  /**
   * get boolean preferences
   *
   * @param key The name of the preference to retrieve
   * @param defaultValue Value to return if this preference does not exist
   * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a
   * preference with
   * this name that is not a boolean
   */
  public static boolean getBoolean(Context context, String key, boolean defaultValue) {
    SharedPreferences settings =
        context.getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    return settings.getBoolean(key, defaultValue);
  }

  public static boolean getBoolean(String key, boolean defaultValue) {
    SharedPreferences settings = BaseApplication.getContext()
        .getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    return settings.getBoolean(key, defaultValue);
  }

  public static boolean putString(Context context, String key, String value) {
    SharedPreferences settings =
        context.getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(key, value);
    return editor.commit();
  }

  public static boolean putString(String key, String value) {
    SharedPreferences settings = BaseApplication.getContext()
        .getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(key, value);
    return editor.commit();
  }

  public static String getString(Context context, String key, String defaultValue) {
    SharedPreferences settings =
        context.getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    return settings.getString(key, defaultValue);
  }

  public static String getString(String key, String defaultValue) {
    SharedPreferences settings = BaseApplication.getContext()
        .getSharedPreferences(MyContexts.PREFERENCE_NAME, Context.MODE_PRIVATE);
    return settings.getString(key, defaultValue);
  }

  public static String getString(Context context, String key) {
    return getString(context, key, null);
  }

  public static String getString(String key) {
    return getString(key, null);
  }
}
