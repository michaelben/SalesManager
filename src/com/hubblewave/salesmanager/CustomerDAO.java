package com.hubblewave.salesmanager;

import java.util.ArrayList;
import java.util.List;

import com.hubblewave.salesmanager.db.CustomerTable;
import com.hubblewave.salesmanager.db.MySQLiteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class CustomerDAO {

  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { CustomerTable.COLUMN_ID,
		  CustomerTable.COLUMN_CODE,
		  CustomerTable.COLUMN_NAME,
		  CustomerTable.COLUMN_TAXID,
		  CustomerTable.COLUMN_ADDRESS,
      CustomerTable.COLUMN_CITY };

  public CustomerDAO(Context context) {
    dbHelper = MySQLiteHelper.getMySQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public Customer createCustomer(String code, String name, String taxid, String address, String city) {
    ContentValues values = new ContentValues();
    values.put(CustomerTable.COLUMN_CODE, code);
    values.put(CustomerTable.COLUMN_NAME, name);
    values.put(CustomerTable.COLUMN_TAXID, taxid);
    values.put(CustomerTable.COLUMN_ADDRESS, address);
    values.put(CustomerTable.COLUMN_CITY, city);
    long insertId = database.insert(CustomerTable.TABLE_CUSTOMER, null,
        values);
    Cursor cursor = database.query(CustomerTable.TABLE_CUSTOMER,
        allColumns, CustomerTable.COLUMN_ID + " = " + insertId, null,
        null, null, null);
    cursor.moveToFirst();
    Customer newCustomer = cursorToCustomer(cursor);
    cursor.close();
    return newCustomer;
  }

  public void deleteCustomer(Customer customer) {
    long id = customer.getId();
    System.out.println("Customer deleted with id: " + id);
    database.delete(CustomerTable.TABLE_CUSTOMER, CustomerTable.COLUMN_ID
        + " = " + id, null);
  }

  public List<Customer> getAllCustomers() {
    List<Customer> customers = new ArrayList<Customer>();

    Cursor cursor = database.query(CustomerTable.TABLE_CUSTOMER,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Customer customer = cursorToCustomer(cursor);
      customers.add(customer);
      cursor.moveToNext();
    }
    // Make sure to close the cursor
    cursor.close();
    return customers;
  }

  private Customer cursorToCustomer(Cursor cursor) {
    Customer customer = new Customer();
    customer.setId(cursor.getLong(0));
    customer.setCode(cursor.getString(1));
    customer.setName(cursor.getString(2));
    return customer;
  }
} 