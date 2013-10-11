/* 
 * Copyright (C) 2010 WaveConn
 * Author BQ
 */

package com.hubblewave.salesmanager;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.hubblewave.salesmanager.db.CategoryTable;
import com.hubblewave.salesmanager.db.MySQLiteHelper;
import com.hubblewave.salesmanager.db.OrderLineTable;
import com.hubblewave.salesmanager.db.OrderTable;
import com.hubblewave.salesmanager.db.ProductTable;
import com.hubblewave.salesmanager.util.Log;

/**
 * 
 */
public class SalesOrderActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private Button mPlusButton;
	private Button mMinusButton;
	private Button mQtyButton;
	private Button mSaveButton;
	private TextView mTotalAmount;
	private EditText mQtyInput;
	private Button mOKButton;
	private long mCustomerId = -1;
	private long mCategoryId = -1;
	private Product mProductItem = null;
	private NoDefaultSpinner mCategorySpinner;
	private NoDefaultSpinner mProductSpinner;
	private ProductAdapter<Product> mProductAdapter;
	private ArrayAdapter<Category> mCategoryAdapter;
	private ListView mListView;
	private SimpleAdapter mListViewAdapter;
	private RelativeLayout mQtyLayout;

	private SMActivityHelper mHelper;
	private SMApplication mApplication;
	private MySQLiteHelper dbHelper;
	  private SQLiteCursorLoader loader=null;
	  
	private Context mContext;
	private ConnectivityManager mConnMngr;
	private SharedPreferences preferences;
	
	public static final int CATEGORY_LOADER = 0;
	public static final int PRODUCT_LOADER = 1;
	public static final int ORDER_LOADER = 2;
	
	List<Category> mCategories = new LinkedList<Category>();
	List<Product> mProducts = new LinkedList<Product>();
	List<Map<String, String>> mOrders = new LinkedList<Map<String, String>>();
	int mListPos = -1;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		if(Log.LOGD) Log.d("onCreate");

		dbHelper = MySQLiteHelper.getMySQLiteHelper(this);
		
		// get the URL we are being asked to view
		Bundle params = getIntent().getExtras();

		if ((params == null) && (icicle != null)) {
			// perhaps we have the URI in the icicle instead?
			params = icicle;
		}
		
		mCustomerId = Long.parseLong(params.getString("customer_id"));
		if(Log.LOGD) Log.d("mCustomerId=" + mCustomerId);
		
		mApplication = (SMApplication)getApplication();
		mApplication.addActiveActivity(this);
		mHelper = new SMActivityHelper(this);
		
		setContentView(R.layout.salesorder);
		
		mCategorySpinner = (NoDefaultSpinner) findViewById(R.id.category_spinner);
		mCategoryAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, mCategories);
		mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCategorySpinner.setAdapter(mCategoryAdapter);
		mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
	    		mCategoryId = ((Category)(mCategoryAdapter.getItem(position))).getId();
	    		if(Log.LOGD) Log.d("mCategoryId=" + mCategoryId + " position="+position + " id="+id);
	    		mProductAdapter.getFilter().filter(""+mCategoryId);
	    		
    			mProductSpinner.setEnabled(true);
    			if(mProductAdapter.getCount() > 0) {
    				mProductSpinner.setSelection(0);
    				mProductItem = (Product)mProductAdapter.getItem(0);
    			}
    			else mProductItem = null;
	    	}
	    	public void onNothingSelected(AdapterView<?> parent){
	    		
	    	}
	    });	
		
		mProductSpinner = (NoDefaultSpinner) findViewById(R.id.product_spinner);
		mProductAdapter = new ProductAdapter(this, android.R.layout.simple_spinner_dropdown_item, mProducts);
		mProductAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mProductSpinner.setAdapter(mProductAdapter);
		mProductSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
	    		mProductItem = (Product)mProductAdapter.getItem(position);
	    		if(Log.LOGD) Log.d("mProductId=" + mProductItem.getId() + " position="+position + " id="+id);
				mMinusButton.setEnabled(true);
				mQtyButton.setEnabled(true);
	    		//getSupportLoaderManager().restartLoader(ORDER_LOADER, null, SalesOrderActivity.this);
	    	}
	    	public void onNothingSelected(AdapterView<?> parent){
	    		
	    	}
	    });
		mProductSpinner.setEnabled(false);
		
		mListView = (ListView) findViewById(R.id.order_list);
		
		mListViewAdapter = new SimpleAdapter(this,
				mOrders, R.layout.order_list_item,
				new String[] { OrderLineTable.COLUMN_PRODUCTNAME, OrderLineTable.COLUMN_QUANTITY, OrderLineTable.COLUMN_PRICE},
				new int[] {R.id.order_description, R.id.order_quantity, R.id.order_price}
				);
		
		mListView.setAdapter(mListViewAdapter);
		
	    mListView.setOnItemClickListener(listOnItemClickListener);
	    mListView.setOnItemSelectedListener(listOnItemSelectedListener);

		mPlusButton = (Button) findViewById(R.id.plus);
		mPlusButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mProductItem != null) {
					Map<String, String> orderline = new HashMap<String, String>();
					orderline.put(OrderLineTable.COLUMN_PRODUCTNAME, mProductItem.getName());
					orderline.put(OrderLineTable.COLUMN_QUANTITY, "1");
					float price = mProductItem.getPrice();
					orderline.put(OrderLineTable.COLUMN_PRICE, ""+price);
					synchronized(mOrders) {
						mOrders.add(orderline);
				        mListViewAdapter.notifyDataSetChanged();
					}
			        
			        mListPos = mOrders.size() - 1;
			        //mListView.setSelection(mListPos);
			        //mySetSelection(mListPos);
			        
			        float oldtotal = Float.parseFloat(mTotalAmount.getText().toString());
			        String total = String.format("%.2f", oldtotal+price);
			        mTotalAmount.setText(total);
				    
					mMinusButton.setEnabled(true);
					mQtyButton.setEnabled(true);
					mSaveButton.setEnabled(true);
				}
			}
		});
		mPlusButton.setEnabled(false);
		
		mMinusButton = (Button) findViewById(R.id.minus);
		mMinusButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mOrders.size() > 0) {
					Map<String, String> values = mOrders.get(mListPos);
					long qty = Long.parseLong(values.get(OrderLineTable.COLUMN_QUANTITY));
					float price = Float.parseFloat(values.get(OrderLineTable.COLUMN_PRICE));
					float oldtotal = Float.parseFloat(mTotalAmount.getText().toString());					
					String total = String.format("%.2f", oldtotal-price*qty);
					
					synchronized(mOrders) {
						mOrders.remove(mListPos);
				        mListViewAdapter.notifyDataSetChanged();
					}

			        if(mListPos > 0) mListPos--;

					if(mOrders.size() > 0) {
						mTotalAmount.setText(total);
						//mListView.setSelection(mListPos);
					} else {
						mTotalAmount.setText("0.00");	//due to rounding error, make sure it restores to 0 when no item left
			        	mMinusButton.setEnabled(false);
						mQtyButton.setEnabled(false);
						mSaveButton.setEnabled(false);
					}
				}
			}
		});
		mMinusButton.setEnabled(false);

		mQtyButton = (Button) findViewById(R.id.qty);
		mQtyButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
					showQtyInputLayout();
			}
		});
		mQtyButton.setEnabled(false);
		
		mSaveButton = (Button) findViewById(R.id.save);
		mSaveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveOrder(toContentValues(mOrders));
				finish();
			}
		});
		mSaveButton.setEnabled(false);
		
		mOKButton = (Button) findViewById(R.id.button_ok);
		mOKButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Map<String, String> values = mOrders.get(mListPos);
				long oldqty = Long.parseLong(values.get(OrderLineTable.COLUMN_QUANTITY));
				float price = Float.parseFloat(values.get(OrderLineTable.COLUMN_PRICE));
				
				long newqty = Long.parseLong(mQtyInput.getText().toString());
				
				values.put(OrderLineTable.COLUMN_QUANTITY, ""+newqty);
				synchronized (mOrders) {
					mOrders.set(mListPos, values);
					mListViewAdapter.notifyDataSetChanged();
				}
				
				float oldtotal = Float.parseFloat(mTotalAmount.getText().toString());
				String total = String.format("%.2f", oldtotal + (newqty-oldqty)*price);
				mTotalAmount.setText(total);
				
				mQtyLayout.setVisibility(View.INVISIBLE);
			}
		});
		
		mTotalAmount = (TextView) findViewById(R.id.total_amount);
		mQtyInput = (EditText) findViewById(R.id.qty_input);
		mQtyInput.setRawInputType(Configuration.KEYBOARD_12KEY);
		mQtyLayout = (RelativeLayout) findViewById(R.id.qty_layout);
		
		// set the title to the name of the page
		setTitle(getResources().getString(R.string.salesorder_title));
		
		getSupportLoaderManager().initLoader(CATEGORY_LOADER, null, this);
		getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
		//getSupportLoaderManager().initLoader(ORDER_LOADER, null, this);

		// Set the menu shortcut keys to be default keys for the activity as
		// well
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		
		if(Log.LOGD) Log.d("exit onCreate");
	}

	private void showQtyInputLayout() {
		if(mListPos != -1) {
			Map<String, String> values = mOrders.get(mListPos);
			mQtyInput.setText(""+values.get(OrderLineTable.COLUMN_QUANTITY));
			mQtyLayout.setVisibility(View.VISIBLE);
		}
	}
	
	TextView lastdesc;
	TextView lastqty;
	TextView lastprice;

	private View myGetView(int position) {
		if (Log.LOGD) Log.d("position="+position);
		if (Log.LOGD) Log.d("FirstVisiblePosition="+mListView.getFirstVisiblePosition());
		if (Log.LOGD) Log.d("getHeaderViewsCount="+mListView.getHeaderViewsCount());
		int firstPosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount(); // This is the same as child #0
		int wantedChild = position - firstPosition;
		// Say, first visible position is 8, you want position 10, wantedChild will now be 2
		// So that means your view is child #2 in the ViewGroup:
		if (Log.LOGD) Log.d("wantedChild="+wantedChild);
		if (Log.LOGD) Log.d("getChildCount()="+mListView.getChildCount());
		if (wantedChild < 0 || wantedChild >= mListView.getChildCount()) {
		  Log.w("Unable to get view for desired position, because it's not being displayed on screen.");
		  return null;
		}
		if (Log.LOGD) Log.d("wantedChild="+wantedChild);
		// Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
		
		View wantedView = mListView.getChildAt(wantedChild);
		if (Log.LOGD) Log.d("wantedView = "+wantedView);
		return wantedView;
	}
	
	private void mySetSelection(int position) {
		TextView desc;
		TextView qty;
		TextView price;
		
		if(lastdesc != null && lastqty != null && lastprice != null) {
            lastdesc.setBackgroundResource(R.drawable.my_border);
            lastqty.setBackgroundResource(R.drawable.my_border);
            lastprice.setBackgroundResource(R.drawable.my_border);
		}
		
		View view = myGetView(position);
		if(view != null) {
			desc=(TextView) view.findViewById(R.id.order_description);
			qty=(TextView) view.findViewById(R.id.order_quantity);
			price=(TextView) view.findViewById(R.id.order_price);
            desc.setBackgroundColor(Color.GREEN);
            qty.setBackgroundColor(Color.GREEN);
            price.setBackgroundColor(Color.GREEN);
            lastdesc = desc;
            lastqty = qty;
            lastprice = price;
		}

    	mListPos = position;		
	}
	
	AdapterView.OnItemSelectedListener listOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id){

    		Log.d("list item selected="+position);
    		//view.setSelected(true);
    		//mySetSelection(position);
    	}
    	public void onNothingSelected(AdapterView<?> parent) {
    		
    	}
    };
    
	AdapterView.OnItemClickListener listOnItemClickListener = new AdapterView.OnItemClickListener() {
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id){

    		Log.d("list item clicked="+position);
    		view.setSelected(true);
    		//mySetSelection(position);
    		mListPos = position;
    		//showQtyInputLayout();
    	}
    };
    
	private List<ContentValues> toContentValues(List<Map<String, String>> order) {
		List<ContentValues> data = new LinkedList<ContentValues>();
		
		for (Map<String, String> orderline : order) {
			ContentValues values = new ContentValues();
			for(String key : orderline.keySet()) {
				values.put(key, orderline.get(key));
			}
			data.add(values);
		}
		
		return data;
	}
	
	private void saveOrder(List<ContentValues> order) {
		MySQLiteHelper dbHelper = MySQLiteHelper.getMySQLiteHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		//save order table
		ContentValues values = new ContentValues();
		values.put(OrderTable.COLUMN_DATE, new Date().getTime());
		values.put(OrderTable.COLUMN_CUSTOMERID, mCustomerId);
		values.put(OrderTable.COLUMN_ORDERTOTAL, Float.parseFloat(mTotalAmount.getText().toString()));
		db.insert(OrderTable.TABLE_ORDER, null, values);
		
		//get orderid
		Cursor cursor = db.rawQuery("select last_insert_rowid() from " + OrderTable.TABLE_ORDER, null);
		cursor.moveToFirst();
		long orderid = cursor.getLong(0);
		cursor.close();
		
		//save orderline table
		db.beginTransaction();	//we don't have other access
		for (ContentValues orderline : order) {
			orderline.put(OrderLineTable.COLUMN_ORDERID, orderid);
			orderline.put(OrderLineTable.COLUMN_PRODUCTID, mProductItem.getId());
			orderline.put(OrderLineTable.COLUMN_DISCOUNT, 0);
			long qty = orderline.getAsLong(OrderLineTable.COLUMN_QUANTITY);
			float price = orderline.getAsFloat(OrderLineTable.COLUMN_PRICE);
			orderline.put(OrderLineTable.COLUMN_LINETOTAL, qty*price);
			db.insert(OrderLineTable.TABLE_ORDERLINE, null, orderline);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
    	try {
	    	switch(loaderId) {
	    	case CATEGORY_LOADER:
	            loader= new SQLiteCursorLoader(this, dbHelper, "SELECT "
	            		+ " * "
	                + "FROM " + CategoryTable.TABLE_CATEGORY, null);
	            break;
	    	case PRODUCT_LOADER:
	            loader= new SQLiteCursorLoader(this, dbHelper, "SELECT "
	            		+ "* "
	                + "FROM " + ProductTable.TABLE_PRODUCT, null);
	            break;
	    	case ORDER_LOADER:
	            loader= new SQLiteCursorLoader(this, dbHelper, "SELECT "
	            		+ OrderLineTable.COLUMN_PRODUCTNAME + ", "
	            		+ OrderLineTable.COLUMN_QUANTITY + ", "
	            		+ OrderLineTable.COLUMN_PRICE + " "
	                + "FROM " + OrderLineTable.TABLE_ORDERLINE + " "
	                + "WHERE " + OrderLineTable.COLUMN_ORDERID + " != -1 AND " + OrderLineTable.COLUMN_ORDERID + " = "
	                + "(SELECT "
	                + OrderTable.COLUMN_ID + " "
	                + "FROM " + OrderTable.TABLE_ORDER + " "
	                + "WHERE " + OrderTable.COLUMN_CUSTOMERID + " = " + mCustomerId + ")"
	                , null);
	            break;
	    	}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return(loader);
      }

      public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    	  switch(loader.getId()) {
    	  case CATEGORY_LOADER:
    		  while(cursor.moveToNext()) {
    	        	Category category = new Category();
    	        	category.setId(cursor.getInt(cursor.getColumnIndex(CategoryTable.COLUMN_ID)));
    	        	category.setCode(cursor.getString(cursor.getColumnIndex(CategoryTable.COLUMN_CODE)));
    	        	category.setName(cursor.getString(cursor.getColumnIndex(CategoryTable.COLUMN_NAME)));
    	        	mCategories.add(category);
    	        }
    	 
    	        if(Log.LOGD) Log.d(""+cursor.getCount());
    	        if(Log.LOGD) for (Category c: mCategories) Log.d(c.out());
    	        
    	        if(cursor.getCount() == 0)
    	        	mCategoryAdapter.notifyDataSetInvalidated();
    	        else
    	        	mCategoryAdapter.notifyDataSetChanged();
    	        
    	        //cursor.close();
    	        
	    	  mProductSpinner.setEnabled(true);
    		  break;
    	  case PRODUCT_LOADER:
    		  while(cursor.moveToNext()) {
  	        	Product product = new Product();
  	        	product.setId(cursor.getLong(cursor.getColumnIndex(ProductTable.COLUMN_ID)));
  	        	product.setCode(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_CODE)));
  	        	product.setName(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_NAME)));
  	        	product.setCategoryId(cursor.getLong(cursor.getColumnIndex(ProductTable.COLUMN_CATEGORYID)));
  	        	product.setPrice(cursor.getFloat(cursor.getColumnIndex(ProductTable.COLUMN_PRICE)));
  	        	mProducts.add(product);
	  	      }
	  	 
  	          if(Log.LOGD) Log.d(""+cursor.getCount());
    		  if(Log.LOGD) for (Product prod: mProducts) Log.d(prod.out());
    		  
	  	        if(cursor.getCount() == 0)
	  	        	mProductAdapter.notifyDataSetInvalidated();
	  	        else
	  	        	mProductAdapter.notifyDataSetChanged();
	  	        
	  	        //cursor.close();
	  	        
		    	  mPlusButton.setEnabled(true);
    		  break;
    	  case ORDER_LOADER:
    		  //mListViewAdapter.changeCursor(cursor);
    		  break;
    	  }
      }

      public void onLoaderReset(Loader<Cursor> loader) {
    	  switch(loader.getId()) {
    	  case CATEGORY_LOADER:
    	        mCategories.clear();
    	        mCategoryAdapter.notifyDataSetInvalidated();
    		  break;
    	  case PRODUCT_LOADER:
  	        mProducts.clear();
  	        mProductAdapter.notifyDataSetInvalidated();
    		  break;
    	  case ORDER_LOADER:
    		  //mListViewAdapter.changeCursor(null);
    		  break;
    	  }
      }
      
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		
		Bundle b = intent.getExtras();
		if(b != null) {
			//updateDisplay(b);
		}

		super.onNewIntent(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Put the URL currently being viewed into the icicle
		//bundleParams(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
