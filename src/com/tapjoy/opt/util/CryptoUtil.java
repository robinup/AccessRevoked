package com.tapjoy.opt.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {

	public static String signStringMD5(String str) {
		if(str == null) {
			return "";
		}
    	MessageDigest digest;
    	try {
    		digest = java.security.MessageDigest.getInstance("MD5");
    	}
    	catch(NoSuchAlgorithmException e) {
			return "";
    	}
		digest.update(str.getBytes());
		byte[] hashValue = digest.digest();
		String signature = (new BigInteger(hashValue)).toString();
		return signature;
	}
	
}
