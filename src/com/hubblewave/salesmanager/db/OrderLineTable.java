package com.hubblewave.salesmanager.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class OrderLineTable {

  // Database table
  public static final String TABLE_ORDERLINE = "orderline";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_ORDERID = "orderid";
  public static final String COLUMN_PRODUCTID = "productid";
  public static final String COLUMN_PRODUCTNAME = "productname";
  public static final String COLUMN_QUANTITY = "quantity";
  public static final String COLUMN_PRICE = "price";
  public static final String COLUMN_DISCOUNT = "discount";
  public static final String COLUMN_LINETOTAL = "linetotal";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_ORDERLINE
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_ORDERID + " integer not null, " 
      + COLUMN_PRODUCTID + " integer not null, " 
      + COLUMN_PRODUCTNAME + " text, " 
      + COLUMN_QUANTITY + " integer, " 
      + COLUMN_PRICE + " real," 
      + COLUMN_DISCOUNT + " real," 
      + COLUMN_LINETOTAL + " real" 
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    Log.w(OrderLineTable.class.getName(), "Upgrading database from version "
        + oldVersion + " to " + newVersion
        + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERLINE);
    onCreate(database);
  }
} 