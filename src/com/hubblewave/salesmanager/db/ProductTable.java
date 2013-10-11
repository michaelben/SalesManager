package com.hubblewave.salesmanager.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ProductTable {

  // Database table
  public static final String TABLE_PRODUCT = "product";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_CODE = "code";
  public static final String COLUMN_NAME = "name";
  public static final String COLUMN_CATEGORYID = "categoryid";
  public static final String COLUMN_PRICE = "price";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_PRODUCT
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_CODE + " text not null, " 
      + COLUMN_NAME + " text not null, " 
      + COLUMN_CATEGORYID + " integer," 
      + COLUMN_PRICE + " real" 
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    Log.w(ProductTable.class.getName(), "Upgrading database from version "
        + oldVersion + " to " + newVersion
        + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
    onCreate(database);
  }
} 