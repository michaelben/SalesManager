package com.hubblewave.salesmanager;

public class Customer {
  private long _id;
  private String code;
  private String name;
  private String taxid;
  private String address;
  private String city;

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

  public String getTaxId() {
	    return taxid;
	  }

public void setTaxId(String taxid) {
  this.taxid = taxid;
}

public String getAddress() {
    return address;
  }

public void setAddress(String address) {
this.address = address;
}

public String getCity() {
    return city;
  }

public void setCity(String city) {
this.city = city;
}

  // Will be used by the ArrayAdapter in the ListView
  @Override
  public String toString() {
    return code + " - " + name;
  }
  
  public String out() {
    return _id + ", " + code + ", " + name + ", " + taxid + ", " + address + ", " + "city";
  }
} 