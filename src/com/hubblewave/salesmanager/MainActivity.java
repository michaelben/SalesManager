package com.hubblewave.salesmanager;

import static com.hubblewave.salesmanager.util.Log.LOGD;
import static com.hubblewave.salesmanager.util.Log.d;
import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Xml;
import android.view.View;
import android.widget.FrameLayout;

import com.hubblewave.salesmanager.db.CategoryTable;
import com.hubblewave.salesmanager.db.CustomerTable;
import com.hubblewave.salesmanager.db.MySQLiteHelper;
import com.hubblewave.salesmanager.db.OrderLineTable;
import com.hubblewave.salesmanager.db.OrderTable;
import com.hubblewave.salesmanager.db.ProductTable;
import com.hubblewave.salesmanager.util.Log;
import com.hubblewave.salesmanager.util.Util;

public class MainActivity extends Activity {
	private SMApplication mApplication;
	private SMActivityHelper mHelper;

	private SharedPreferences preferences;
	FrameLayout mProgress;
	
	String dest="ftp";
	String remote_folder_prefix=".";
	boolean isPassive=true;
	boolean isAppend=false;
	String upload, download, username, password;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mApplication = (SMApplication) getApplication();
		mApplication.addActiveActivity(this);

		mHelper = new SMActivityHelper(this);

		mProgress = (FrameLayout) findViewById(R.id.progress_bar);
		preferences = getSharedPreferences(ConfigActivity.PREFERENCES_SALES,
				Activity.MODE_PRIVATE);
	}

	public void myClickHandler(View v) {
		switch (v.getId()) {
		case R.id.sales:
			startActivity(new Intent(MainActivity.this, CustomersActivity.class));
			break;
		case R.id.send_data:
			getPrefs();
			if(dest.equals("ftp")) {
				if (!(Util.isConnected(this))) {
					mHelper.showToast(this, "No network connection");
					break;
				}
				executeLoadAsyncTask(upload, username, password, upLoader);
			}
			else 
				executeLoadAsyncTask(upload, username, password, saveSdcard);
			break;
		case R.id.receive_data:
			getPrefs();
			//if(LOGD)
			//	executeLoadAsyncTask(download, username, password, xmlLoader);
			//else 
			if (dest.equals("ftp")) {
				if (!(Util.isConnected(this))) {
					mHelper.showToast(this, "No network connection");
					break;
				}
				executeLoadAsyncTask(download, username, password, downLoader);
			}
			else
				executeLoadAsyncTask(download, username, password, sdcardLoader);
			break;
		case R.id.config:
			startActivity(new Intent(MainActivity.this, ConfigActivity.class));
			break;
		}
	}

	private void getPrefs() {
		dest = preferences.getString(ConfigActivity.PREFERENCES_SALES_IMPORTEXPORT, ConfigActivity.PREFERENCES_SALES_IMPORTEXPORT_DEFAULT);
		isPassive = preferences.getBoolean(ConfigActivity.PREFERENCES_SALES_IS_PASSIVE, ConfigActivity.PREFERENCES_SALES_IS_PASSIVE_DEFAULT);
		remote_folder_prefix = preferences.getString(ConfigActivity.PREFERENCES_SALES_REMOTE_FOLDER_PREFIX, ConfigActivity.PREFERENCES_SALES_REMOTE_FOLDER_PREFIX_DEFAULT);
		isAppend = preferences.getBoolean(ConfigActivity.PREFERENCES_SALES_IS_APPEND, ConfigActivity.PREFERENCES_SALES_IS_APPEND_DEFAULT);
		
		download = preferences.getString(
				ConfigActivity.PREFERENCES_SALES_DOWNLOAD,
				ConfigActivity.PREFERENCES_SALES_DOWNLOAD_DEFAULT);
		download = "".equals(download) ? ConfigActivity.PREFERENCES_SALES_DOWNLOAD_DEFAULT
				: download;
		upload = preferences.getString(
				ConfigActivity.PREFERENCES_SALES_UPLOAD,
				ConfigActivity.PREFERENCES_SALES_UPLOAD_DEFAULT);
		upload = "".equals(upload) ? ConfigActivity.PREFERENCES_SALES_UPLOAD_DEFAULT
				: upload;
		username = preferences.getString(
				ConfigActivity.PREFERENCES_SALES_USERNAME,
				ConfigActivity.PREFERENCES_SALES_USERNAME_DEFAULT);
		username = "".equals(username) ? ConfigActivity.PREFERENCES_SALES_USERNAME_DEFAULT
				: username;
		password = preferences.getString(
				ConfigActivity.PREFERENCES_SALES_PASSWORD,
				ConfigActivity.PREFERENCES_SALES_PASSWORD_DEFAULT);
		password = "".equals(password) ? ConfigActivity.PREFERENCES_SALES_PASSWORD_DEFAULT
				: password;
	}

	private void executeLoadAsyncTask(String server, String username,
			String password, Loader loader) {
		// AsynTask can be executed only once. So create new task every time.
		LoadAsyncTask mTask = new LoadAsyncTask();
		mTask.mActivity = MainActivity.this;
		mTask.mContext = getApplicationContext();
		mTask.execute(server, username, password, loader);
	}

	private class LoadAsyncTask extends AsyncTask<Object, Void, String> {
		public Activity mActivity;
		public Context mContext;

		LoadAsyncTask() {
			super();
		}

		LoadAsyncTask(Activity activity, Context context) {
			super();
			this.mActivity = activity;
			this.mContext = context;
		}

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(Object... params) {
			return ((Loader) params[3]).load((String) params[0],
					(String) params[1], (String) params[2]);
		}

		@Override
		protected void onPostExecute(String res) {
			mProgress.setVisibility(View.INVISIBLE);
			Log.d("load return result: " + res);
			if (!"".equals(res))
				mHelper.showToast(MainActivity.this, res);
			else
				mHelper.showToast(MainActivity.this, "operation successful");
		}
	}

	interface Loader {
		String load(String server, String username, String password);
	}

	Loader upLoader = new Loader() {
		public String load(String server, String username, String password) {
			String result = "";
			FTPClient ftpClient = new FTPClient();

			try {
				ftpClient.connect(server);
				ftpClient.login(username, password);
				ftpClient.setType(FTPClient.TYPE_AUTO);
				ftpClient.setPassive(isPassive);
				ftpClient.changeDirectory(remote_folder_prefix);
				
				String out = orderToString();
				InputStream stream = new ByteArrayInputStream(out.getBytes("UTF-8"));
				if(isAppend)
					ftpClient.append(ConfigActivity.PREFERENCES_SALES_ORDER_FILENAME, stream, 0, null);
				else
					ftpClient.upload(ConfigActivity.PREFERENCES_SALES_ORDER_FILENAME, stream, 0, 0, null);			
				stream.close();

				out = orderLineToString();
				stream = new ByteArrayInputStream(out.getBytes("UTF-8"));
				if(isAppend)
					ftpClient.append(ConfigActivity.PREFERENCES_SALES_ORDERLINE_FILENAME, stream, 0, null);
				else
					ftpClient.upload(ConfigActivity.PREFERENCES_SALES_ORDERLINE_FILENAME, stream, 0, 0, null);
				stream.close();

				//logout is not a required command. some server may not implement it, if so it will return 502 error.
				//ftpClient.logout();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "IllegalStateException";
			} catch (FTPDataTransferException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return "FTPDataTransferException";
			} catch (FTPAbortedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return "FTPAbortedException";
			} catch (FTPIllegalReplyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "FTPIllegalReplyException";
			} catch (FTPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "FTPException";
			} catch (SocketException e) {
				return "SocketException";
			} catch (IOException e) {
				return "IOException";
			} finally {
				if (ftpClient.isConnected()) {
						try {
							ftpClient.disconnect(true);
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							result = "IllegalStateException";
						} catch (FTPIllegalReplyException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							result = "FTPIllegalReplyException";
						} catch (FTPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							result = "FTPException";
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							result = "IOException";
						} 
				}
				
				if ("".equals(result))
					result = "upload successful";
			}

			return result;
		}
	};


	Loader saveSdcard = new Loader() {
		public String load(String server, String username, String password) {
			String result = "";
			try {
				File myFile = new File(Environment.getExternalStorageDirectory() + "/orders.xml");
				FileOutputStream fout = new FileOutputStream(myFile);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fout));
				
				writer.write(orderToString());
				writer.close();
				
				myFile = new File(Environment.getExternalStorageDirectory() + "/orderlines.xml");
				fout = new FileOutputStream(myFile);
				writer = new BufferedWriter(new OutputStreamWriter(fout));
				
				writer.write(orderLineToString());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				result = "I/O Failed";
			} finally {
				
				if ("".equals(result))
					result = "save to SDCard successfully";
				
				return result;
			}

		}
	};

	String orderToString() {
		MySQLiteHelper dbHelper = MySQLiteHelper
				.getMySQLiteHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
	
		Cursor cursor = db.rawQuery("select * from " + OrderTable.TABLE_ORDER,
				null);
	
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		
	    StringBuilder sb = new StringBuilder();
	    
	    try{
	        sb.append("<Orders>\n");

			while (cursor.moveToNext()) {
				sb.append("\t<Order>\n");
				sb.append("\t\t<Id>" + cursor.getLong(0) + "</Id>\n");
				sb.append("\t\t<Date>" + df.format(new Date(cursor.getLong(1))) + "</Date>\n");
				sb.append("\t\t<CustomerId>" + cursor.getLong(2) + "</CustomerId>\n");
				sb.append("\t\t<OrderTotal>" + cursor.getFloat(3) + "</OrderTotal>\n");
				sb.append("\t</Order>\n");
			}
		
			sb.append("</Orders>");
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
			cursor.close();
	    }
	    
        return sb.toString();
	}
	
	String orderLineToString() {
		MySQLiteHelper dbHelper = MySQLiteHelper
				.getMySQLiteHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from "
				+ OrderLineTable.TABLE_ORDERLINE, null);

	    StringBuilder sb = new StringBuilder();
	    
	    try{
	    	sb.append("<OrderLines>\n");

			while (cursor.moveToNext()) {
				sb.append("\t<OrderLine>\n");
				sb.append("\t\t<Id>" + cursor.getLong(0) + "</Id>\n");
				sb.append("\t\t<OrderId>" + cursor.getLong(1) + "</OrderId>\n");
				sb.append("\t\t<ProductId>" + cursor.getLong(2) + "</ProductId>\n");
				sb.append("\t\t<ProductName>" + cursor.getString(3) + "</ProductName>\n");
				sb.append("\t\t<Quantity>" + cursor.getFloat(4) + "</Quantity>\n");
				sb.append("\t\t<Price>" + cursor.getFloat(5) + "</Price>\n");
				sb.append("\t\t<Discount>" + cursor.getFloat(6) + "</Discount>\n");
				sb.append("\t\t<LineTotal>" + cursor.getFloat(7) + "</LineTotal>\n");
				sb.append("\t</OrderLine>\n");
			}
		
			sb.append("</OrderLines>");
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    } finally {
			cursor.close();
	    }
	    
	    return sb.toString();
	}

	String orderToStringBySerializer() {
		MySQLiteHelper dbHelper = MySQLiteHelper
				.getMySQLiteHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
	
		Cursor cursor = db.rawQuery("select * from " + OrderTable.TABLE_ORDER,
				null);
	
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		
		//Android XmlSerializer does not add RETURN after each line.
		//This seems a bug, also it uses StringBuffer internally.
		//USE StringBuilder instead. 
	    XmlSerializer serializer = Xml.newSerializer();
	    StringWriter writer = new StringWriter();
	    
	    try{
		    serializer.setOutput(writer);
	        serializer.startDocument("UTF-8", true);
	        serializer.startTag("", "Orders");
	        //serializer.attribute("", "number", String.valueOf(messages.size()));
		
			while (cursor.moveToNext()) {
				serializer.startTag("", "Order");
	            serializer.startTag("", "Id");
	            serializer.text(""+cursor.getLong(0));
	            serializer.endTag("", "Id");
	            serializer.startTag("", "Date");
	            serializer.text(""+df.format(new Date(cursor.getLong(1))));
	            serializer.endTag("", "Date");
	            serializer.startTag("", "CustomerId");
	            serializer.text(""+cursor.getLong(2));
	            serializer.endTag("", "CustomerId");
	            serializer.startTag("", "OrderTotal");
	            serializer.text(""+cursor.getFloat(3));
	            serializer.endTag("", "OrderTotal");
	            serializer.endTag("", "Order");
			}
		
	        serializer.endTag("", "Orders");
	        serializer.endDocument();
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    } finally {
			cursor.close();
	    }
	    
        return writer.toString();
	}
	
	String orderLineToStringBySerializer() {
		MySQLiteHelper dbHelper = MySQLiteHelper
				.getMySQLiteHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from "
				+ OrderLineTable.TABLE_ORDERLINE, null);

		//Android XmlSerializer does not add RETURN after each line.
		//This seems a bug, also it uses StringBuffer internally.
		//USE StringBuilder instead. 
	    XmlSerializer serializer = Xml.newSerializer();
	    StringWriter writer = new StringWriter();
	    
	    try{
		    serializer.setOutput(writer);
	        serializer.startDocument("UTF-8", true);
	        serializer.startTag("", "OrderLines");

			while (cursor.moveToNext()) {
				serializer.startTag("", "OrderLine");
	            serializer.startTag("", "Id");
	            serializer.text(""+cursor.getLong(0));
	            serializer.endTag("", "Id");
	            serializer.startTag("", "OrderId");
	            serializer.text(""+cursor.getLong(1));
	            serializer.endTag("", "OrderId");
	            serializer.startTag("", "ProductId");
	            serializer.text(""+cursor.getLong(2));
	            serializer.endTag("", "ProductId");
	            serializer.startTag("", "ProductName");
	            serializer.text(cursor.getString(3));
	            serializer.endTag("", "ProductName");
	            serializer.startTag("", "Quantity");
	            serializer.text(""+cursor.getFloat(4));
	            serializer.endTag("", "Quantity");
	            serializer.startTag("", "Price");
	            serializer.text(""+cursor.getFloat(5));
	            serializer.endTag("", "Price");
	            serializer.startTag("", "Discount");
	            serializer.text(""+cursor.getFloat(6));
	            serializer.endTag("", "Discount");
	            serializer.startTag("", "LineTotal");
	            serializer.text(""+cursor.getFloat(7));
	            serializer.endTag("", "LineTotal");
				serializer.endTag("", "OrderLine");
			}
		
			serializer.endTag("", "OrderLines");
	        serializer.endDocument();
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    } finally {
			cursor.close();
	    }
	    
	    return writer.toString();
	}

	
	Loader downLoader = new Loader() {
		public String load(String server, String username, String password) {
			String result = "";
			FTPClient ftpClient = new FTPClient();

			try {
				try {
					if(LOGD) d("server="+server);
					ftpClient.connect(server);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "FTP connection failed";
				} catch (FTPIllegalReplyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "FTP connection failed";
				} catch (FTPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "FTP connection failed";
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "FTP connection failed";
				} 
				
				try {
					ftpClient.login(username, password);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "FTP login failed";
				} catch (FTPIllegalReplyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "FTP login failed";
				} catch (FTPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "FTP login failed";
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "FTP login failed";
				}
				
				ftpClient.setType(FTPClient.TYPE_AUTO);
				ftpClient.setPassive(isPassive);
				try {
					if(remote_folder_prefix != null && !"".equals(remote_folder_prefix))
						ftpClient.changeDirectory(remote_folder_prefix);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "change remote folder failed";
				} catch (FTPIllegalReplyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "change remote folder failed";
				} catch (FTPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "change remote folder failed";
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "change remote folder failed";
				}
				
				try {
					result = downloadFile(ftpClient, ConfigActivity.PREFERENCES_SALES_CATEGORY_FILENAME, categoryParser);
					result = downloadFile(ftpClient, ConfigActivity.PREFERENCES_SALES_PRODUCT_FILENAME, productParser);
					result = downloadFile(ftpClient, ConfigActivity.PREFERENCES_SALES_CUSTOMER_FILENAME, customerParser);
				} catch (IllegalStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					result = "data transder failed";
					return result;
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					result = "data transder failed";
					return result;
				}
				
				//logout is not a required command. some server may not implement it, if so it will return 502 error.
				//ftpClient.logout();
			} finally {
				if (ftpClient.isConnected()) {
					try {
						ftpClient.disconnect(true);
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						result = "IllegalStateException";
					} catch (FTPIllegalReplyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						result = "FTPIllegalReplyException";
					} catch (FTPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						result = "FTPException";
					} catch (IOException ioe) {
						result = "IOException";
					}
				}
				
				if ("".equals(result))
					result = "download successful";	
			}
			
			return result;
		}
	};

	private String downloadFile(FTPClient ftpClient, String file, Parser parser) {
			try {
				File myFile = new File(Environment.getExternalStorageDirectory() + "/" + file);
				ftpClient.download(file, myFile, 0, null);
				
				FileInputStream fin = new FileInputStream(myFile);			
				BufferedReader reader = new BufferedReader(new InputStreamReader(fin, "UTF8"));
			
				// use local parser for preventing synchronization issue
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser xpp = factory.newPullParser();
				
				xpp.setInput(reader);
				parser.parse(xpp);
	
				reader.close();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "UnsupportedEncodingException";
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "XmlPullParserException";
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "IllegalStateException";
			}  catch (FTPIllegalReplyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "FTPIllegalReplyException";
			} catch (FTPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "FTPException";
			} catch (FTPDataTransferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "FTPDataTransferException";
			} catch (FTPAbortedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "FTPAbortedException";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "IOException";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Exception";
			}
			
			return "download successful";
	}

	Loader sdcardLoader = new Loader() {
		public String load(String server, String username, String password) {
			String result = "";
			
			try {
				//read customers
				FileInputStream fIn = new FileInputStream(
						new File(Environment.getExternalStorageDirectory() + "/customers.xml"));
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(fIn, "UTF8"));
				
				// use local parser for preventing synchronization issue
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser xpp = factory.newPullParser();

				xpp.setInput(reader);
				customerParser.parse(xpp);
				reader.close();
				
				//read categories
				fIn = new FileInputStream(
						new File(Environment.getExternalStorageDirectory() + "/categories.xml"));
				reader = new BufferedReader(
						new InputStreamReader(fIn, "UTF8"));

				xpp.setInput(reader);
				categoryParser.parse(xpp);
				reader.close();
				
				//read products
				fIn = new FileInputStream(
						new File(Environment.getExternalStorageDirectory() + "/products.xml"));
				reader = new BufferedReader(
						new InputStreamReader(fIn, "UTF8"));

				xpp.setInput(reader);
				productParser.parse(xpp);
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
				result = "import failed";
			}
			
			if ("".equals(result))
				result = "import from SDCard successfully";
			
			return result;
		}
	};
	
	Loader xmlLoader = new Loader() {
		public String load(String server, String username, String password) {
			Resources resources = MainActivity.this.getResources();
			try {
				categoryParser.parse(resources.getXml(R.xml.categories));
				productParser.parse(resources.getXml(R.xml.products));
				customerParser.parse(resources.getXml(R.xml.customers));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "NumberFormatException";
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "NotFoundException";
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "XmlPullParserException";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "IOException";
			}
			
			return "load successful";
		}
	};
	
	interface Parser {
		void parse(XmlPullParser xpp) throws NumberFormatException, XmlPullParserException, IOException;
	}
	
	private Parser categoryParser = new Parser() {
		public void parse(XmlPullParser xpp) {
			LinkedList<ContentValues> list = new LinkedList<ContentValues>();
	
			try {
				while (reachNextStartTagFor(xpp, "category") != xpp.END_DOCUMENT) {
					if (LOGD)
						d("enter outer loop");
					ContentValues values = new ContentValues();
					int event_type = reachNextStartTag(xpp);
					if (LOGD)
						d("Tage=" + xpp.getName());
					while (event_type != xpp.END_DOCUMENT
							&& event_type != xpp.END_TAG
							&& !xpp.getName().equalsIgnoreCase("category")) {
						if (LOGD)
							d("Tage=" + xpp.getName());
						if (event_type == xpp.START_TAG) {
							String name = xpp.getName();
							String value = xpp.nextText();
							if (LOGD)
								d("Value=" + value);
							if (CategoryTable.COLUMN_ID
									.equalsIgnoreCase("_" + name)) {
								name = CategoryTable.COLUMN_ID;
								values.put(name, Long.parseLong(value));
							} else if (CategoryTable.COLUMN_CODE
									.equalsIgnoreCase(name)) {
								name = CategoryTable.COLUMN_CODE;
								values.put(name, value);
							} else if (CategoryTable.COLUMN_NAME
									.equalsIgnoreCase(name)) {
								name = CategoryTable.COLUMN_NAME;
								values.put(name, value);
							}
						}
						if (LOGD)
							d("before myXppNextTag");
						event_type = myXppNextTag(xpp);
						if (LOGD)
							d("after myXppNextTag");
					}
	
					if (event_type == xpp.END_DOCUMENT)
						break;
	
					list.add(values);
	
					if (LOGD)
						d("exit outer loop");
				}
	
				batchInsertSQLite(CategoryTable.TABLE_CATEGORY, list);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
		
	private Parser productParser = new Parser() {
		public void parse(XmlPullParser xpp) {
			try {
				LinkedList<ContentValues> list = new LinkedList<ContentValues>();
	
				while (reachNextStartTagFor(xpp, "product") != xpp.END_DOCUMENT) {
					ContentValues values = new ContentValues();
					int event_type = reachNextStartTag(xpp);
					if (LOGD)
						d("Tage=" + xpp.getName());
					while (event_type != xpp.END_DOCUMENT
							&& event_type != xpp.END_TAG
							|| !xpp.getName().equalsIgnoreCase("product")) {
						if (LOGD)
							d("Tage=" + xpp.getName());
						if (event_type == xpp.START_TAG) {
							String name = xpp.getName();
							String value = xpp.nextText();
							if (LOGD)
								d("Value=" + value);
							if (ProductTable.COLUMN_ID.equalsIgnoreCase("_" + name)) {
								name = ProductTable.COLUMN_ID;
								values.put(name, Long.parseLong(value));
							} else if (ProductTable.COLUMN_CODE
									.equalsIgnoreCase(name)) {
								name = ProductTable.COLUMN_CODE;
								values.put(name, value);
							} else if (ProductTable.COLUMN_NAME
									.equalsIgnoreCase(name)) {
								name = ProductTable.COLUMN_NAME;
								values.put(name, value);
							} else if (ProductTable.COLUMN_CATEGORYID
									.equalsIgnoreCase(name)) {
								name = ProductTable.COLUMN_CATEGORYID;
								values.put(name, Long.parseLong(value));
							} else if (ProductTable.COLUMN_PRICE
									.equalsIgnoreCase(name)) {
								name = ProductTable.COLUMN_PRICE;
								values.put(name, Float.parseFloat(value));
							}
						}
						event_type = myXppNextTag(xpp);
					}
	
					list.add(values);
				}
	
				batchInsertSQLite(ProductTable.TABLE_PRODUCT, list);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private Parser customerParser = new Parser() {
		public void parse(XmlPullParser xpp) throws NumberFormatException, XmlPullParserException, IOException {
				LinkedList<ContentValues> list = new LinkedList<ContentValues>();
	
				while (reachNextStartTagFor(xpp, "customer") != xpp.END_DOCUMENT) {
					ContentValues values = new ContentValues();
					int event_type = reachNextStartTag(xpp);
					if (LOGD)
						d("Tage=" + xpp.getName());
					while (event_type != xpp.END_DOCUMENT
							&& event_type != xpp.END_TAG
							|| !xpp.getName().equalsIgnoreCase("customer")) {
						if (LOGD)
							d("Tage=" + xpp.getName());
						if (event_type == xpp.START_TAG) {
							String name = xpp.getName();
							String value = xpp.nextText();
							if (LOGD)
								d("Value=" + value);
							if (CustomerTable.COLUMN_ID
									.equalsIgnoreCase("_" + name)) {
								name = CustomerTable.COLUMN_ID;
								values.put(name, Long.parseLong(value));
							} else if (CustomerTable.COLUMN_CODE
									.equalsIgnoreCase(name)) {
								name = CustomerTable.COLUMN_CODE;
								values.put(name, value);
							} else if (CustomerTable.COLUMN_NAME
									.equalsIgnoreCase(name)) {
								name = CustomerTable.COLUMN_NAME;
								values.put(name, value);
							} else if (CustomerTable.COLUMN_TAXID
									.equalsIgnoreCase(name)) {
								name = CustomerTable.COLUMN_TAXID;
								values.put(name, value);
							} else if (CustomerTable.COLUMN_ADDRESS
									.equalsIgnoreCase(name)) {
								name = CustomerTable.COLUMN_ADDRESS;
								values.put(name, value);
							} else if (CustomerTable.COLUMN_CITY
									.equalsIgnoreCase(name)) {
								name = CustomerTable.COLUMN_CITY;
								values.put(name, value);
							}
						}
						event_type = myXppNextTag(xpp);
					}
	
					list.add(values);
				}
				batchInsertSQLite(CustomerTable.TABLE_CUSTOMER, list);
		}
	};

	/*
	 * There are 3 approaches for batch sqlite insertion. 1. Transaction. block
	 * other access but later API there are exclusive or immediate mode and be
	 * able to set listener for performance reason. 2. Union Select. losing
	 * insertion order, plus limit 500 rows at a time for each statement. 3.
	 * batch insert sql statement introduced in sqlite version 3.7.11(Android
	 * JellyBean)
	 */
	private void batchInsertSQLite(String table,
			LinkedList<ContentValues> categories) {
		if (LOGD)
			d("enter batchInsert");

		SQLiteDatabase db = null;

		try {
			MySQLiteHelper dbHelper = MySQLiteHelper
					.getMySQLiteHelper(getApplicationContext());
			db = dbHelper.getWritableDatabase();
			db.delete(table, null, null); // clear the table
			db.beginTransaction(); // we don't have other access
			for (ContentValues values : categories) {
				if (LOGD)
					logValues(values);
				long ret = db.insert(table, null, values); // dont' insert empty
															// values
				if (LOGD)
					d("rowid " + ret + " is inserted into " + table);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}

		if (LOGD)
			d("exit batchInsert");
	}

	private void logValues(ContentValues values) {
		for (Entry<String, Object> value : values.valueSet())
			d(value.getKey() + "=" + value.getValue());
	}

	public static String getTextForTag(XmlPullParser xpp, String tag)
			throws XmlPullParserException, IOException {
		while (myXppNextTag(xpp) != xpp.START_TAG
				|| !xpp.getName().equalsIgnoreCase(tag))
			if (LOGD)
				d("Tag=" + xpp.getName());
		xpp.require(xpp.START_TAG, "", tag);
		String text = xpp.nextText();
		xpp.require(xpp.END_TAG, "", tag);

		return text;
	}

	private static int reachNextStartTagFor(XmlPullParser xpp, String tag)
			throws XmlPullParserException, IOException {
		int eventType;
		do {
			eventType = myXppNextTag(xpp);
		} while (eventType != xpp.END_DOCUMENT
				&& (eventType != xpp.START_TAG || !xpp.getName()
						.equalsIgnoreCase(tag)));

		return eventType;
	}

	private static int reachNextStartTag(XmlPullParser xpp)
			throws XmlPullParserException, IOException {
		int eventType;

		do {
			eventType = myXppNextTag(xpp);
		} while (eventType != xpp.END_DOCUMENT && (eventType != xpp.START_TAG));

		return eventType;
	}

	// xpp.nextTag does not pass TEXT event
	// myXppNextTag passes TEXT event and whitespace,
	private static int myXppNextTag(XmlPullParser xpp)
			throws XmlPullParserException, IOException {
		if (LOGD)
			d("enter myXppNextTag");

		int eventType = xpp.next();
		while (eventType != xpp.END_DOCUMENT && eventType != xpp.START_TAG
				&& eventType != xpp.END_TAG) { // skip whitespace
			if (LOGD)
				logEventType(xpp, eventType);
			eventType = xpp.next();
		}

		return eventType;
	}

	private static void logEventType(XmlPullParser xpp, int et) {
		if (et == xpp.START_TAG)
			d("start tag");
		else if (et == xpp.END_TAG)
			d("end tag");
		else if (et == xpp.END_DOCUMENT)
			d("end document");
	}

	private static final String TAG = "SALES MAIN";
}