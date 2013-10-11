/**
 * Copyright (C) 2010 WaveConn
 * @author BQ
 */
package com.hubblewave.salesmanager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hubblewave.salesmanager.util.Base64;
import com.hubblewave.salesmanager.util.Log;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.SharedPreferences;

public class SMApplication extends Application {
	ArrayList<Activity>	mActiveActivities;	//XXX: memory leak?
											//TODO: or use ActivityManager.RunningTaskInfo?
	
	static SharedPreferences preferences;
	static final String PREFERENCES_DB = "mini_db";

	static final String CUSTOMER_ID = "customer.id";
	static final String PRODUCTI_ID = "product.id";
	static final String CATEGORY_ID = "category.id";
	static final String ORDER = "order";
	
	static String mCustomerId;
	static String mProductId;
	static String mCategoryId;
	static LinkedList<Map<String, String>> mOrder;
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate() {
    	super.onCreate();
    	
    	preferences = getSharedPreferences(
				PREFERENCES_DB,
				Activity.MODE_PRIVATE);
    	
    	mCustomerId = preferences.getString(CUSTOMER_ID, null);
    	mProductId = preferences.getString(CUSTOMER_ID, null);
    	mCategoryId = preferences.getString(CUSTOMER_ID, null);

    	String s = preferences.getString(ORDER, null);
    	if (s != null)
    		mOrder = (LinkedList<Map<String, String>>) SMActivityHelper.unserialize(Base64.decode(s));
    	
    	mActiveActivities = new ArrayList<Activity>();
    }

    @Override
    public void onTerminate() {
    	if(Log.LOGD) Log.v("onTerminate gets called");
    	
    	/*
    	ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    	List<RunningTaskInfo> rtis = am.getRunningTasks(1);
    	RunningTaskInfo rti = rtis.get(0);
    	* it seems lack of list of active activities in RunningTaskInfo API in 1.5
    	*/
    	
    	for (Activity a: mActiveActivities)
    		if (a != null) a.finish();
    	mActiveActivities.clear();
    	
    	super.onTerminate();
    }
    
    public void addActiveActivity(Activity a) {
    	mActiveActivities.add(a);
    }
    
}
