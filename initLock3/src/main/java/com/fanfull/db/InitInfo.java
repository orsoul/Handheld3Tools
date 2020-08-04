package com.fanfull.db;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

/**
 * Entity mapped to table INIT_INFO.
 */
public class InitInfo {

  private Long id;
  /** Not-null value. */
  private String init_bagid;

  public InitInfo() {
  }

  public InitInfo(Long id) {
    this.id = id;
  }

  public InitInfo(Long id, String init_bagid) {
    this.id = id;
    this.init_bagid = init_bagid;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  /** Not-null value. */
  public String getInit_bagid() {
    return init_bagid;
  }

  /** Not-null value; ensure this value is available before it is saved to the database. */
  public void setInit_bagid(String init_bagid) {
    this.init_bagid = init_bagid;
  }
}