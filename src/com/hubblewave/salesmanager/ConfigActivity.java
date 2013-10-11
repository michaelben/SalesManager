/* 
 * Copyright (C) 2010 WaveConn
 * Author BQ
 */

package com.hubblewave.salesmanager;

import com.hubblewave.salesmanager.util.Log;
import com.hubblewave.salesmanager.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

public class ConfigActivity extends Activity {
	private RadioGroup radio_group;
	private RadioGroup radio_group_passive;
	private RadioGroup radio_group_append;
	private RadioButton ftp;
	private RadioButton sdcard;
	private RadioButton passive;
	private RadioButton active;
	private RadioButton append;
	private RadioButton replace;
	private EditText download;
	private EditText upload;
	private EditText username;
	private EditText password;
	private EditText remote_folder;
	private RelativeLayout ftp_layout;
	private SharedPreferences preferences;
	
	public static final String PREFERENCES_SALES = "sales.pref";
	public static final String PREFERENCES_SALES_DOWNLOAD = "download.server";
	public static final String PREFERENCES_SALES_UPLOAD = "upload.server";
	public static final String PREFERENCES_SALES_USERNAME = "user";
	public static final String PREFERENCES_SALES_PASSWORD = "password";
	public static final String PREFERENCES_SALES_IMPORTEXPORT = "importexport";
	
	public static final String PREFERENCES_SALES_DOWNLOAD_DEFAULT = "ftp.yourdomain.com";
	public static final String PREFERENCES_SALES_UPLOAD_DEFAULT = "ftp.yourdomain.com";
	public static final String PREFERENCES_SALES_USERNAME_DEFAULT = "username@yourdomain.com";
	public static final String PREFERENCES_SALES_PASSWORD_DEFAULT = "password";
	public static final String PREFERENCES_SALES_IMPORTEXPORT_DEFAULT = "ftp";
	
	public static final String PREFERENCES_SALES_REMOTE_FOLDER_PREFIX = "remote.folder.prefix";
	public static final String PREFERENCES_SALES_IS_PASSIVE = "isPassive";
	public static final String PREFERENCES_SALES_IS_APPEND = "isAppend";
	
	public static final String PREFERENCES_SALES_REMOTE_FOLDER_PREFIX_DEFAULT = "";
	public static final boolean PREFERENCES_SALES_IS_PASSIVE_DEFAULT = true;
	public static final boolean PREFERENCES_SALES_IS_APPEND_DEFAULT = false;

	public static final String PREFERENCES_SALES_CATEGORY_FILENAME = "categories.xml";
	public static final String PREFERENCES_SALES_PRODUCT_FILENAME = "products.xml";
	public static final String PREFERENCES_SALES_CUSTOMER_FILENAME = "customers.xml";
	public static final String PREFERENCES_SALES_ORDER_FILENAME = "orders.xml";
	public static final String PREFERENCES_SALES_ORDERLINE_FILENAME = "orderlines.xml";
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.config);

		setTitle("Configuration");
		
		preferences = getSharedPreferences(PREFERENCES_SALES, Activity.MODE_PRIVATE);

    	ftp_layout = (RelativeLayout) findViewById(R.id.ftp_layout);
		download = (EditText) findViewById(R.id.download_server);
		upload = (EditText) findViewById(R.id.upload_server);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		remote_folder = (EditText) findViewById(R.id.remote_folder);
		download.setText(preferences.getString(ConfigActivity.PREFERENCES_SALES_DOWNLOAD, ConfigActivity.PREFERENCES_SALES_DOWNLOAD_DEFAULT));
		upload.setText(preferences.getString(ConfigActivity.PREFERENCES_SALES_UPLOAD, ConfigActivity.PREFERENCES_SALES_UPLOAD_DEFAULT));
		username.setText(preferences.getString(ConfigActivity.PREFERENCES_SALES_USERNAME, ConfigActivity.PREFERENCES_SALES_USERNAME_DEFAULT));
		password.setText(preferences.getString(ConfigActivity.PREFERENCES_SALES_PASSWORD, ConfigActivity.PREFERENCES_SALES_PASSWORD_DEFAULT));
		remote_folder.setText(preferences.getString(ConfigActivity.PREFERENCES_SALES_REMOTE_FOLDER_PREFIX, ConfigActivity.PREFERENCES_SALES_REMOTE_FOLDER_PREFIX_DEFAULT));
		
		//this has come after, because it uses download,... in listener
		radio_group = (RadioGroup) findViewById(R.id.radio_group);
		radio_group.setOnCheckedChangeListener(targetOnCheckedChangeListener); 
		ftp = (RadioButton) findViewById(R.id.ftp);
		sdcard = (RadioButton) findViewById(R.id.sdcard);
		String target = preferences.getString(ConfigActivity.PREFERENCES_SALES_IMPORTEXPORT, ConfigActivity.PREFERENCES_SALES_IMPORTEXPORT_DEFAULT);
		if(Log.LOGD) Log.d("target="+target);
		if(target.equals("ftp")) ftp.setChecked(true);
		else sdcard.setChecked(true);
		
		radio_group_passive = (RadioGroup) findViewById(R.id.radio_group_passive);
		radio_group_passive.setOnCheckedChangeListener(passiveOnCheckedChangeListener);
		passive = (RadioButton) findViewById(R.id.isPassive);
		active = (RadioButton) findViewById(R.id.isActive);
		boolean isPassive = preferences.getBoolean(ConfigActivity.PREFERENCES_SALES_IS_PASSIVE, ConfigActivity.PREFERENCES_SALES_IS_PASSIVE_DEFAULT);
		if(isPassive) passive.setChecked(true);
		else active.setChecked(true);
		
		radio_group_append = (RadioGroup) findViewById(R.id.radio_group_append);
		radio_group_append.setOnCheckedChangeListener(appendOnCheckedChangeListener);
		append = (RadioButton) findViewById(R.id.isAppend);
		replace = (RadioButton) findViewById(R.id.isReplace);
		boolean isAppend = preferences.getBoolean(ConfigActivity.PREFERENCES_SALES_IS_APPEND, ConfigActivity.PREFERENCES_SALES_IS_APPEND_DEFAULT);
		if(isAppend) append.setChecked(true);
		else replace.setChecked(true);
	}

	OnCheckedChangeListener targetOnCheckedChangeListener = new OnCheckedChangeListener() {
	public void onCheckedChanged(RadioGroup group, int checkedId) {
	    switch(checkedId){
	    case R.id.ftp:
	    	Util.setEnabledAll(ftp_layout, true);
        	break;
	    case R.id.sdcard:
	    	Util.setEnabledAll(ftp_layout, false);
        	break;
	    }
	}};
	
	OnCheckedChangeListener passiveOnCheckedChangeListener = new OnCheckedChangeListener() {
	public void onCheckedChanged(RadioGroup group, int checkedId) {
	    switch(checkedId){
	    case R.id.isPassive:
        	break;
	    case R.id.isActive:
        	break;
	    }
	}};
	
	OnCheckedChangeListener appendOnCheckedChangeListener = new OnCheckedChangeListener() {
	public void onCheckedChanged(RadioGroup group, int checkedId) {
	    switch(checkedId){
	    case R.id.isAppend:
        	break;
	    case R.id.isReplace:
        	break;
	    }
	}};
	
    public void myClickHandler(View v) {
        switch (v.getId()) {
        case R.id.save:
        	String target;
        	int selectedId = radio_group.getCheckedRadioButtonId();
        	if(selectedId == R.id.ftp) 
        		target = "ftp";
        	else target = "sdcard";
        	
        	boolean isPassive;
        	selectedId = radio_group_passive.getCheckedRadioButtonId();
        	if(selectedId == R.id.isPassive) 
        		isPassive = true;
        	else isPassive = false;
        	
        	boolean isAppend;
        	selectedId = radio_group_append.getCheckedRadioButtonId();
        	if(selectedId == R.id.isAppend) 
        		isAppend = true;
        	else isAppend = false;
        	
        	preferences.edit()
        	.putString(PREFERENCES_SALES_DOWNLOAD, download.getText().toString())
        	.putString(PREFERENCES_SALES_UPLOAD, upload.getText().toString())
        	.putString(PREFERENCES_SALES_USERNAME, username.getText().toString())
        	.putString(PREFERENCES_SALES_PASSWORD, password.getText().toString())
        	.putString(PREFERENCES_SALES_REMOTE_FOLDER_PREFIX, remote_folder.getText().toString())
        	.putString(PREFERENCES_SALES_IMPORTEXPORT, target)
        	.putBoolean(PREFERENCES_SALES_IS_PASSIVE, isPassive)
        	.putBoolean(PREFERENCES_SALES_IS_APPEND, isAppend)
			.commit();
        	finish();
            break;
        }
    }

    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

}
