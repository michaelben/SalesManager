package com.hubblewave.salesmanager;

import java.util.ArrayList;
import java.util.List;

import com.hubblewave.salesmanager.db.MySQLiteHelper;
import com.hubblewave.salesmanager.db.OrderLineTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class OrderLineDAO {

  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { OrderLineTable.COLUMN_ID,
		  OrderLineTable.COLUMN_ORDERID,
		  OrderLineTable.COLUMN_PRODUCTID,
		  OrderLineTable.COLUMN_PRODUCTNAME,
		  OrderLineTable.COLUMN_QUANTITY,
		  OrderLineTable.COLUMN_PRICE,
		  OrderLineTable.COLUMN_DISCOUNT,
      OrderLineTable.COLUMN_LINETOTAL };

  public OrderLineDAO(Context context) {
    dbHelper = MySQLiteHelper.getMySQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public OrderLine createOrderLine(long orderid, long productid, String productname, long quantity, float price, float discount, float linetotal) {
    ContentValues values = new ContentValues();
    values.put(OrderLineTable.COLUMN_ORDERID, orderid);
    values.put(OrderLineTable.COLUMN_PRODUCTID, productid);
    values.put(OrderLineTable.COLUMN_PRODUCTNAME, productname);
    values.put(OrderLineTable.COLUMN_QUANTITY, quantity);
    values.put(OrderLineTable.COLUMN_PRICE, price);
    values.put(OrderLineTable.COLUMN_DISCOUNT, discount);
    values.put(OrderLineTable.COLUMN_LINETOTAL, linetotal);
    long insertId = database.insert(OrderLineTable.TABLE_ORDERLINE, null,
        values);
    Cursor cursor = database.query(OrderLineTable.TABLE_ORDERLINE,
        allColumns, OrderLineTable.COLUMN_ID + " = " + insertId, null,
        null, null, null);
    cursor.moveToFirst();
    OrderLine newOrderLine = cursorToOrderLine(cursor);
    cursor.close();
    return newOrderLine;
  }

  public void deleteOrderLine(OrderLine orderline) {
    long id = orderline.getId();
    System.out.println("OrderLine deleted with id: " + id);
    database.delete(OrderLineTable.TABLE_ORDERLINE, OrderLineTable.COLUMN_ID
        + " = " + id, null);
  }

  public List<OrderLine> getAllOrderLines() {
    List<OrderLine> orderlines = new ArrayList<OrderLine>();

    Cursor cursor = database.query(OrderLineTable.TABLE_ORDERLINE,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      OrderLine orderline = cursorToOrderLine(cursor);
      orderlines.add(orderline);
      cursor.moveToNext();
    }
    // Make sure to close the cursor
    cursor.close();
    return orderlines;
  }

  private OrderLine cursorToOrderLine(Cursor cursor) {
    OrderLine orderline = new OrderLine();
    orderline.setId(cursor.getLong(0));
    orderline.setOrderId(cursor.getLong(1));
    orderline.setProductId(cursor.getLong(2));
    orderline.setProductName(cursor.getString(3));
    orderline.setQuantity(cursor.getLong(4));
    orderline.setPrice(cursor.getFloat(5));
    orderline.setDiscount(cursor.getFloat(6));
    orderline.setLineTotal(cursor.getFloat(7));
    return orderline;
  }
} 