package com.fanfull.libhard.finger;

import com.fanfull.libhard.IOperation;

public interface IFingerOperation extends IOperation {

  boolean send(byte[] data);

  /**
   * 注册一个指纹.
   *
   * @param fingerIndexBuff 指纹存放位置
   * @param fingerFeatureBuff 指纹特征码
   * @return 注册成功返回0
   */
  int addFinger(int[] fingerIndexBuff, byte[] fingerFeatureBuff);

  /**
   * 注册一个指纹.
   *
   * @param fingerIndexBuff 指纹存放位置
   * @return 注册成功返回0
   */
  int addFinger(int[] fingerIndexBuff);

  /**
   * 指纹库中搜索指纹.
   *
   * @param fingerIndexBuff 搜索到的指纹在 指纹库中的位置
   * @param fingerFeatureBuff 搜索到指纹 的特征码
   * @return 搜索成功返回0
   */
  int searchFinger(int[] fingerIndexBuff, byte[] fingerFeatureBuff);

  /**
   * 指纹库中搜索指纹.
   *
   * @param fingerIndexBuff 搜索到的指纹在 指纹库中的位置
   * @return 搜索成功返回0
   */
  int searchFinger(int[] fingerIndexBuff);

  /**
   * 获取指纹库中指纹的数量.
   *
   * @return 获取成功返回 指纹数量
   */
  int getFingerNum(int[] fingerNumBuff);

  /** 删除指纹库中 单个指纹. */
  boolean deleteFinger(int fingerIndex);

  /** 清空指纹库中 所有指纹. */
  boolean clearFinger();

  void setListener(IFingerListener listener);
}
