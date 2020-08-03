package com.fanfull.entity;

/**
 * @author Keung
 * @ClassName: IPConfigBean
 * @Description: IP XML配置的实体类
 * @date 2015-3-12 下午02:05:34
 */
public class IPConfigBean {

  private String ip;
  private String prot;

  public IPConfigBean() {
  }

  public IPConfigBean(String ip, String prot) {
    super();
    this.ip = ip;
    this.prot = prot;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getProt() {
    return Integer.parseInt(prot);
  }

  public void setProt(String prot) {
    this.prot = prot;
  }

  @Override
  public String toString() {
    return "ConfigXML [ip=" + ip + ", prot=" + prot + "]";
  }
}
