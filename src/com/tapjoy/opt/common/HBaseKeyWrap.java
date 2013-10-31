package com.tapjoy.opt.common;

import org.apache.hadoop.hbase.util.Bytes;


//written by Robin Li for HBase key optimization

public class HBaseKeyWrap {

	protected static byte[] constructKey (int token_i, String udid_s){
		byte[] udid = udid_s.getBytes();
		int salt = ((int) udid[0])<<24 | (token_i);

		byte[] key = Bytes.add(Bytes.toBytes(salt), udid);
		return key;
	}

}
