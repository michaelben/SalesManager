package com.hubblewave.salesmanager;

import java.util.ArrayList;
import java.util.List;

import com.hubblewave.salesmanager.db.MySQLiteHelper;
import com.hubblewave.salesmanager.db.ProductTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ProductDAO {

  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { ProductTable.COLUMN_ID,
		  ProductTable.COLUMN_CODE,
		  ProductTable.COLUMN_NAME,
		  ProductTable.COLUMN_CATEGORYID,
		  ProductTable.COLUMN_PRICE };

  public ProductDAO(Context context) {
    dbHelper = MySQLiteHelper.getMySQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public Product createProduct(String code, String name, long categoryid, float price) {
    ContentValues values = new ContentValues();
    values.put(ProductTable.COLUMN_CODE, code);
    values.put(ProductTable.COLUMN_NAME, name);
    values.put(ProductTable.COLUMN_CATEGORYID, categoryid);
    values.put(ProductTable.COLUMN_PRICE, price);
    long insertId = database.insert(ProductTable.TABLE_PRODUCT, null,
        values);
    Cursor cursor = database.query(ProductTable.TABLE_PRODUCT,
        allColumns, ProductTable.COLUMN_ID + " = " + insertId, null,
        null, null, null);
    cursor.moveToFirst();
    Product newProduct = cursorToProduct(cursor);
    cursor.close();
    return newProduct;
  }

  public void deleteProduct(Product product) {
    long id = product.getId();
    System.out.println("Product deleted with id: " + id);
    database.delete(ProductTable.TABLE_PRODUCT, ProductTable.COLUMN_ID
        + " = " + id, null);
  }

  public List<Product> getAllProducts() {
    List<Product> products = new ArrayList<Product>();

    Cursor cursor = database.query(ProductTable.TABLE_PRODUCT,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Product product = cursorToProduct(cursor);
      products.add(product);
      cursor.moveToNext();
    }
    // Make sure to close the cursor
    cursor.close();
    return products;
  }

  private Product cursorToProduct(Cursor cursor) {
    Product product = new Product();
    product.setId(cursor.getLong(0));
    product.setCode(cursor.getString(1));
    product.setName(cursor.getString(2));
    product.setCategoryId(cursor.getLong(3));
    product.setPrice(cursor.getFloat(4));
    return product;
  }
} 