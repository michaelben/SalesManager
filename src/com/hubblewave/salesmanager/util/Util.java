package com.hubblewave.salesmanager.util;

import com.hubblewave.salesmanager.SalesOrderActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class Util {
	public static boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}

	public static boolean isConnected(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
	public static void setEnabledAll(View v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.setFocusable(enabled);
    	
    	if(v instanceof ViewGroup) {
    		ViewGroup vg = (ViewGroup) v;
	       	for (int i = 0; i < vg.getChildCount(); i++)
	        	setEnabledAll(vg.getChildAt(i), enabled);
    	}
	}
}