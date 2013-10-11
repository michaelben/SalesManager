package com.hubblewave.salesmanager.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CustomerTable {

  // Database table
  public static final String TABLE_CUSTOMER = "customer";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_CODE = "code";
  public static final String COLUMN_NAME = "name";
  public static final String COLUMN_TAXID = "taxid";
  public static final String COLUMN_ADDRESS = "address";
  public static final String COLUMN_CITY = "city";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_CUSTOMER
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_CODE + " text not null, " 
      + COLUMN_NAME + " text not null, " 
      + COLUMN_TAXID + " text," 
      + COLUMN_ADDRESS + " text," 
      + COLUMN_CITY + " text" 
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    Log.w(CustomerTable.class.getName(), "Upgrading database from version "
        + oldVersion + " to " + newVersion
        + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER);
    onCreate(database);
  }
} 