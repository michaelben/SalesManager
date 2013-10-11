package com.hubblewave.salesmanager;

public class Order {
  private long _id;
  private long date;
  private long customerid;
  private float ordertotal;

  public long getId() {
    return _id;
  }

  public void setId(long id) {
    this._id = id;
  }

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
  }
  
  public long getCustomerId() {
	    return customerid;
	  }

  public void setCustomerId(long customerid) {
    this.customerid = customerid;
  }

  public float getOrderTotal() {
	    return ordertotal;
	  }

public void setOrderTotal(float ordertotal) {
  this.ordertotal = ordertotal;
}

  // Will be used by the ArrayAdapter in the ListView
  @Override
  public String toString() {
    return ""+_id+" "+date+" "+customerid+" "+ordertotal;
  }
  
  public String out() {
	return this.toString();
  }
} 