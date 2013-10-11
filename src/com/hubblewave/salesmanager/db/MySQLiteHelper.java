package com.hubblewave.salesmanager.db;

import com.hubblewave.salesmanager.util.Log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "sales.db";
  private static final int DATABASE_VERSION = 1;

  private MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  private static MySQLiteHelper mMySQLiteHelper;
  
  public static synchronized MySQLiteHelper getMySQLiteHelper(Context context) {
	  if (mMySQLiteHelper == null)
		  mMySQLiteHelper = new MySQLiteHelper(context);

	  return mMySQLiteHelper;
  }
  
  // Method is called during creation of the database
  @Override
  public void onCreate(SQLiteDatabase database) {
	  CategoryTable.onCreate(database);
	  ProductTable.onCreate(database);
      CustomerTable.onCreate(database);
      OrderTable.onCreate(database);
      OrderLineTable.onCreate(database);
  }

  // Method is called during an upgrade of the database,
  // e.g. if you increase the database version
  @Override
  public void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    CategoryTable.onUpgrade(database, oldVersion, newVersion);
    ProductTable.onUpgrade(database, oldVersion, newVersion);
    CustomerTable.onUpgrade(database, oldVersion, newVersion);
    OrderTable.onUpgrade(database, oldVersion, newVersion);
    OrderLineTable.onUpgrade(database, oldVersion, newVersion);
  }
}
 