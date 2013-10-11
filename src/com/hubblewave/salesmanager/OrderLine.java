package com.hubblewave.salesmanager;

public class OrderLine {
  private long _id;
  private long orderid;
  private long productid;
  private String productname;
  private long quantity;
  private float price;
  private float discount;
  private float linetotal;

  public long getId() {
    return _id;
  }

  public void setId(long id) {
    this._id = id;
  }

  public long getOrderId() {
    return orderid;
  }

  public void setOrderId(long orderid) {
    this.orderid = orderid;
  }
  
  public long getProductId() {
	    return productid;
	  }

  public void setProductId(long productid) {
    this.productid = productid;
  }

  public String getProductName() {
	    return productname;
	  }

  public void setProductName(String productname) {
    this.productname = productname;
  }

  public long getQuantity() {
	    return quantity;
	  }

public void setQuantity(long quantity) {
  this.quantity = quantity;
}

  public float getPrice() {
	    return price;
	  }

	public void setPrice(float price) {
	  this.price = price;
	}

	  public float getDiscount() {
		    return discount;
		  }

		public void setDiscount(float discount) {
		  this.discount = discount;
		}

		  public float getLineTotal() {
			    return linetotal;
			  }

			public void setLineTotal(float linetotal) {
			  this.linetotal = linetotal;
			}

  // Will be used by the ArrayAdapter in the ListView
  @Override
  public String toString() {
	  return " "+_id+" "+orderid+" "+productid + " " + productname + " " + quantity + " " + price + " " + discount + " " + linetotal;
  }
  
  public String out() {
	  return " "+_id+" "+orderid+" "+productid + " " + productname + " " + quantity + " " + price + " " + discount + " " + linetotal;
  }
} 