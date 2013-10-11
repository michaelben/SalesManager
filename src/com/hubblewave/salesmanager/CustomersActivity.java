/* 
 * Copyright (C) 2010 WaveConn
 * Author BQ
 */

package com.hubblewave.salesmanager;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.hubblewave.salesmanager.db.CustomerTable;
import com.hubblewave.salesmanager.db.MySQLiteHelper;
import com.hubblewave.salesmanager.util.Log;

/**
 * 
 */
public class CustomersActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	static final String LOG_TAG = "Customers";
	
	private SMApplication mApplication;
	private SMActivityHelper mHelper;
	
	private MySQLiteHelper dbHelper;
	  private ArrayAdapter adapter=null;
	  private SQLiteCursorLoader loader=null;
	  
	  private EditText mEditText;
	  private ListView mListView;
	  
	  private List<Customer> customers = new ArrayList<Customer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = (SMApplication)getApplication();
		mApplication.addActiveActivity(this);

		mHelper = new SMActivityHelper(this);
	    dbHelper=MySQLiteHelper.getMySQLiteHelper(this);
		
		setContentView(R.layout.customers);
		
		// set the title to the name of the page
		setTitle(getResources().getString(R.string.customers_title));
		
		mEditText = (EditText)findViewById(R.id.search_box);
		mEditText.addTextChangedListener(filterTextWatcher);
		
		mListView = (ListView)findViewById(android.R.id.list);
	    adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, customers);
	    mListView.setAdapter(adapter);
	    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
	    		if(Log.LOGD) Log.d("item clicked");
	    		long customer_id = customers.get(position).getId();
	    		if(Log.LOGD) Log.d("mCustomerId=" + customer_id + " position="+position + " id="+id);
	            Intent intent = new Intent();
	            intent.putExtra("customer_id", ""+customer_id);
	            intent.setComponent(new ComponentName("com.hubblewave.salesmanager", "com.hubblewave.salesmanager.SalesOrderActivity"));
	            startActivity(intent);
	    	}
	    });

	    getSupportLoaderManager().initLoader(0, null, this);
	    
		// use the menu shortcut keys as default key bindings for the entire
		// activity
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
	}

	  @Override
	  protected void onResume() {
	    super.onResume();
	  }

	  @Override
	  protected void onPause() {
	    dbHelper.close();
	    super.onPause();
	  }
	  
	  @Override
	  protected void onDestroy() {
	      super.onDestroy();
	      mEditText.removeTextChangedListener(filterTextWatcher);
	  }

    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        loader=
            new SQLiteCursorLoader(this, dbHelper, "SELECT "
            		+ CustomerTable.COLUMN_ID + ", "
            		+ CustomerTable.COLUMN_CODE + ", "
            		+ CustomerTable.COLUMN_NAME + " "
                + "FROM " + CustomerTable.TABLE_CUSTOMER + " ORDER BY " + CustomerTable.COLUMN_NAME, null);

        return(loader);
      }

      public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    	  while(cursor.moveToNext()) {
        	Customer customer = new Customer();
        	customer.setId(cursor.getInt(cursor.getColumnIndex(CustomerTable.COLUMN_ID)));
        	customer.setCode(cursor.getString(cursor.getColumnIndex(CustomerTable.COLUMN_CODE)));
        	customer.setName(cursor.getString(cursor.getColumnIndex(CustomerTable.COLUMN_NAME)));
        	customers.add(customer);
        }
 
	        if(Log.LOGD) Log.d(""+cursor.getCount());
    	if(Log.LOGD) for (Customer customer: customers) Log.d(customer.out());
        if(cursor.getCount() == 0)
        	adapter.notifyDataSetInvalidated();
        else
        	adapter.notifyDataSetChanged();
        
        //cursor.close();
      }

      public void onLoaderReset(Loader<Cursor> loader) {
        customers.clear();
        adapter.notifyDataSetInvalidated();
      }
      
      private TextWatcher filterTextWatcher = new TextWatcher() {

    	    public void afterTextChanged(Editable s) {
    	    }

    	    public void beforeTextChanged(CharSequence s, int start, int count,
    	            int after) {
    	    }

    	    public void onTextChanged(CharSequence s, int start, int before,
    	            int count) {
    	    	if(Log.LOGD) Log.d("textChanged: "+s);
    	    	CustomersActivity.this.adapter.getFilter().filter(s);
    	    }

    	};
}
