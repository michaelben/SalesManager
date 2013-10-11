package com.hubblewave.salesmanager.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class OrderTable {

  // Database table
  public static final String TABLE_ORDER = "salesorder";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_DATE = "date";
  public static final String COLUMN_CUSTOMERID = "customerid";
  public static final String COLUMN_ORDERTOTAL = "ordertotal";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_ORDER 
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_DATE + " integer not null, " //the number of seconds since 1970-01-01 00:00:00 UTC
      + COLUMN_CUSTOMERID + " integer not null, " 
      + COLUMN_ORDERTOTAL + " real not null" 
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    Log.w(OrderTable.class.getName(), "Upgrading database from version "
        + oldVersion + " to " + newVersion
        + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER );
    onCreate(database);
  }
} 