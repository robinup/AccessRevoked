package com.tapjoy.opt.util;

import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

public class MClient {
	// The max retry times
	private static int maxretry = 3;
	// The max wait time
	private static int timeout = 100;
	
	// For Add, Set, Touch, Delete w/o customized transcoder
	public static boolean executeCmd(MemcachedClient mc, String cmd, String key, int exp, Object value, int timeOut){
		int retry = 0;
		if (timeOut == -1){ timeOut = timeout; }
		
		while (retry < maxretry) {
			try{
				OperationFuture<java.lang.Boolean> of = null;
				if ("add".equals(cmd)){
					of = mc.add(key, exp, value);
				} else if ("set".equals(cmd)){
					of = mc.set(key, exp, value);
				} else if ("delete".equals(cmd)){
					of = mc.delete(key);
				} else if ("touch".equals(cmd)){
					of = mc.touch(key, exp);
				} 

				if (of != null && of.get(timeOut, TimeUnit.MILLISECONDS).booleanValue()) {
					return true;
				}
			} catch (Exception e) {
				System.out.println("LeiTest - executeCmd - error happend: " + e.getMessage() + " ;;; " + e.getStackTrace().toString());
			}

			retry ++;
		}
		
		return false;
	}

}
