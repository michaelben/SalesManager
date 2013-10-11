package com.hubblewave.salesmanager;

import java.util.ArrayList;
import java.util.List;

import com.hubblewave.salesmanager.db.MySQLiteHelper;
import com.hubblewave.salesmanager.db.OrderTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class OrderDAO {

  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { OrderTable.COLUMN_ID,
		  OrderTable.COLUMN_DATE,
		  OrderTable.COLUMN_CUSTOMERID,
      OrderTable.COLUMN_ORDERTOTAL };

  public OrderDAO(Context context) {
    dbHelper = MySQLiteHelper.getMySQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public Order createOrder(long date, long customerid, float ordertotal) {
    ContentValues values = new ContentValues();
    values.put(OrderTable.COLUMN_DATE, date);
    values.put(OrderTable.COLUMN_CUSTOMERID, customerid);
    values.put(OrderTable.COLUMN_ORDERTOTAL, ordertotal);
    long insertId = database.insert(OrderTable.TABLE_ORDER, null,
        values);
    Cursor cursor = database.query(OrderTable.TABLE_ORDER,
        allColumns, OrderTable.COLUMN_ID + " = " + insertId, null,
        null, null, null);
    cursor.moveToFirst();
    Order newOrder = cursorToOrder(cursor);
    cursor.close();
    return newOrder;
  }

  public void deleteOrder(Order order) {
    long id = order.getId();
    System.out.println("Order deleted with id: " + id);
    database.delete(OrderTable.TABLE_ORDER, OrderTable.COLUMN_ID
        + " = " + id, null);
  }

  public List<Order> getAllOrders() {
    List<Order> orders = new ArrayList<Order>();

    Cursor cursor = database.query(OrderTable.TABLE_ORDER,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Order order = cursorToOrder(cursor);
      orders.add(order);
      cursor.moveToNext();
    }
    // Make sure to close the cursor
    cursor.close();
    return orders;
  }

  private Order cursorToOrder(Cursor cursor) {
    Order order = new Order();
    order.setId(cursor.getLong(0));
    order.setDate(cursor.getLong(1));
    order.setCustomerId(cursor.getLong(2));
    order.setOrderTotal(cursor.getFloat(3));
    return order;
  }
} 