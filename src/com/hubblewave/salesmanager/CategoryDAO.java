package com.hubblewave.salesmanager;

import java.util.ArrayList;
import java.util.List;

import com.hubblewave.salesmanager.db.CategoryTable;
import com.hubblewave.salesmanager.db.MySQLiteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class CategoryDAO {

  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { CategoryTable.COLUMN_ID, CategoryTable.COLUMN_CODE,
      CategoryTable.COLUMN_NAME };

  public CategoryDAO(Context context) {
    dbHelper = MySQLiteHelper.getMySQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public Category createCategory(String code, String name) {
    ContentValues values = new ContentValues();
    values.put(CategoryTable.COLUMN_CODE, code);
    values.put(CategoryTable.COLUMN_NAME, name);
    long insertId = database.insert(CategoryTable.TABLE_CATEGORY, null,
        values);
    Cursor cursor = database.query(CategoryTable.TABLE_CATEGORY,
        allColumns, CategoryTable.COLUMN_ID + " = " + insertId, null,
        null, null, null);
    cursor.moveToFirst();
    Category newCategory = cursorToCategory(cursor);
    cursor.close();
    return newCategory;
  }

  public void deleteCategory(Category category) {
    long id = category.getId();
    System.out.println("Category deleted with id: " + id);
    database.delete(CategoryTable.TABLE_CATEGORY, CategoryTable.COLUMN_ID
        + " = " + id, null);
  }

  public List<Category> getAllCategorys() {
    List<Category> categories = new ArrayList<Category>();

    Cursor cursor = database.query(CategoryTable.TABLE_CATEGORY,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Category category = cursorToCategory(cursor);
      categories.add(category);
      cursor.moveToNext();
    }
    // Make sure to close the cursor
    cursor.close();
    return categories;
  }

  private Category cursorToCategory(Cursor cursor) {
    Category category = new Category();
    category.setId(cursor.getLong(0));
    category.setCode(cursor.getString(1));
    category.setName(cursor.getString(2));
    return category;
  }
} 