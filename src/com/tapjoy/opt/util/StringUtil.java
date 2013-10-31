package com.tapjoy.opt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringUtil {
	/**
	 * from formatted String to list of String
	 * 
	 * @return
	 */
	public static List<String> formattedStringToList(String formattedString) {
		List<String> strList = new ArrayList<String>();

		String str = formattedString;
		str = str.replace('[', ' ').replace(']', ' ').replace('"', ' ');
		String[] strArray = str.split(",");
		for (int i = 0; i < strArray.length; i++) {
			strList.add(strArray[i].trim());
		}

		return strList;
	}
	
	/**
	 * from formatted String to Hashmap
	 * 
	 * the string should be such format:  key1:val1::key2:val2:: ....  ::keyX:valX
	 * 
	 * @return
	 */
	public static HashMap<String, String> formattedStringToMap(String formattedString) {
		HashMap<String, String> stringMap = new HashMap<String, String>();

		String [] strParts = formattedString.split("::");
		for (String pair : strParts){
			int pos = pair.indexOf(":");
			if (pos != -1){
				stringMap.put(pair.substring(0, pos), pair.substring(pos+1, pair.length()));
			}
		}
		
		return stringMap;
	}

	/**
	 * from list of string to set of string
	 * 
	 * @param list
	 * @return
	 */
	public static Set<String> listToSet(List<String> list) {
		Set<String> stringSet = new HashSet<String>();
		for (String str : list) {
			if(str.length()>0) {
				stringSet.add(str);				
			}
		}

		return stringSet;
	}

	/**
	 * from list of String to lower String
	 * 
	 * @return
	 */
	public static List<String> toLowerCase(List<String> strList) {
		List<String> updatedList = new ArrayList<String>();

		for (String str : strList) {
			updatedList.add(str.toLowerCase());
		}

		return updatedList;
	}

	/**
	 * from list of String to upper String
	 * 
	 * @return
	 */
	public static List<String> toUpperCase(List<String> strList) {
		List<String> updatedList = new ArrayList<String>();

		for (String str : strList) {
			updatedList.add(str.toUpperCase());
		}

		return updatedList;
	}
	
	/**
	 * Join a String array 
	 * 
	 */
	public static String joinArray(String [] arr){
		StringBuffer sbuf = new StringBuffer();
		for (String s : arr){
			sbuf.append(s);
		}
		
		return sbuf.toString();
	}
	
	public static boolean findInArray(String target, String [] source) {
		if (target == null || source == null) {
			return false;
		}
		
		for (String s : source) {
			if (target.equals(s)) {
				return true;
			}
		}
		
		return false;
	} 
}

