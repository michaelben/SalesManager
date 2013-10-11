package com.hubblewave.salesmanager;

public class Product {
  private long _id;
  private String code;
  private String name;
  private long categoryid;
  private float price;

  public long getId() {
    return _id;
  }

  public void setId(long id) {
    this._id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
  
  public String getName() {
	    return name;
	  }

  public void setName(String name) {
    this.name = name;
  }
  
  public long getCategoryId() {
	    return categoryid;
	  }

	public void setCategoryId(long categoryid) {
	  this.categoryid = categoryid;
	}
	
	public float getPrice() {
	    return price;
	  }
	
	public void setPrice(float price) {
	this.price = price;
	}

  // Will be used by the ArrayAdapter in the ListView
  @Override
  public String toString() {
    return name;
  }
  
  public String out() {
	    return _id + ", " + code + ", " + name + ", " + categoryid + ", " + price;
  }
} 