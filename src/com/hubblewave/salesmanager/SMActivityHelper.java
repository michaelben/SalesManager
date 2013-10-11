/* 
 * Copyright (C) 2010 WaveConn
 * Author BQ
 */

package com.hubblewave.salesmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.widget.Toast;

/**
 * A helper class that implements operations required by Activities in the
 * system. Notably, this includes launching the Activities for edit.
 */
public class SMActivityHelper {
	private Activity mContext;

	/**
	 * Preferred constructor.
	 * 
	 * @param context
	 *            the Activity to be used for things like calling startActivity
	 */
	public SMActivityHelper(Activity context) {
		mContext = context;
	}

	/**
	 * @see #
	 */
	// intentionally private; tell IDEs not to warn us about it
	@SuppressWarnings("unused")
	private SMActivityHelper() {
	}

	public static Toast sToast;
	
    public void showToast(Context context, int resid) {
        if (sToast == null) {
            sToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        sToast.setText(resid);
        sToast.show();
    }

    public void showToast(Context context, String msg) {
    	if(msg != null) {
	        if (sToast == null) {
	            sToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
	        }
	        sToast.setText(msg);
	        sToast.show();
    	}
    }
	
    protected static Object unserialize(byte[] bytes) {
    	if (bytes == null) return null;
    	
    	ObjectInput is;
    	Object o = null;
		try {
			is = new ObjectInputStream(new ByteArrayInputStream(bytes));
	    	o = is.readObject();
	    	is.close();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return o;
    }
    
    protected static byte[] serialize(Object o) {
    	if(o == null) return null;
    	
    	ObjectOutput oos;
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return baos.toByteArray();
    }
	
	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
	        public boolean verify(String hostname, SSLSession session) {
	                return true;
	        }
	};

	/**
	 * Trust every server - dont check for any certificate
	 */
	private static void trustAllHosts() {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                        return new java.security.cert.X509Certificate[] {};
	                }

	                public void checkClientTrusted(X509Certificate[] chain,
	                                String authType) throws CertificateException {
	                }

	                public void checkServerTrusted(X509Certificate[] chain,
	                                String authType) throws CertificateException {
	                }
	        } };

	        // Install the all-trusting trust manager
	        try {
	                SSLContext sc = SSLContext.getInstance("TLS");
	                sc.init(null, trustAllCerts, new java.security.SecureRandom());
	                HttpsURLConnection
	                                .setDefaultSSLSocketFactory(sc.getSocketFactory());
	        } catch (Exception e) {
	                e.printStackTrace();
	        }
	}

	HttpURLConnection getHttp(URL url) {
	    HttpURLConnection http = null;
	
	    if (url.getProtocol().toLowerCase().equals("https")) {
	        trustAllHosts();
	            HttpsURLConnection https = null;
				try {
					https = (HttpsURLConnection) url.openConnection();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            if(https != null) https.setHostnameVerifier(DO_NOT_VERIFY);
	            http = https;
	    } else {
	            try {
					http = (HttpURLConnection) url.openConnection();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    }
	    
	    return http;
	}

	public Map getMap(Bundle b) {
		Map<String, String> map = new HashMap<String, String>();
		for(String key : b.keySet()) map.put(key, b.getString(key));
		return map;
	}

	public Bundle getBundle(Map<String, String> m) {
		Bundle b = new Bundle();
		for(String key : m.keySet()) b.putString(key, m.get(key));
		return b;
	}
	
	public void exit() {
		//1. java.lang.System.exit(code) stops vm with shutdown hooks for GC etc.
		//2. java.lang.Runtime.halt() stops vm without shutdown hooks. should not be used.
		//3. android.os.Process.killProcess kills the process with the pid,
		//   and releases the memory immediately.
		
		//kill the current process, not the AlarmInitReceiver which runs in a remote process
		SMApplication application = (SMApplication)mContext.getApplication();
		application.onTerminate();
		Process.killProcess(Process.myPid());
	}
}