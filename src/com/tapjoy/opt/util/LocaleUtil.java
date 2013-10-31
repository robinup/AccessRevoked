package com.tapjoy.opt.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleUtil {

	private final String ResourceBundleName="locale/LocaleResourceBundle";
	private ResourceBundle bundle;
	public LocaleUtil(Locale locale){
		if(bundle==null){
			try {
				bundle=ResourceBundle.getBundle(ResourceBundleName, locale);
				if(bundle.keySet().size()==0){
					bundle=ResourceBundle.getBundle(ResourceBundleName, new Locale("en"));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String get(String key){
		return bundle.getString(key);
	}	
	
	public static String timeDelayString(String timeDelay,String locale) {
		if (locale == null) {
			return timeDelay;
		}
		Locale loc = null;
		if (locale.indexOf("-") != -1) {
			String[] array = locale.split("-");
			if (array[0].equalsIgnoreCase("zh")
					&& !array[1].equalsIgnoreCase("cn")) {
				array[1] = "cht";
			}
			loc = new Locale(array[0].toLowerCase(), array[1].toUpperCase());
		} else {
			loc = new Locale(locale);
		}
		LocaleUtil localeUtil = new LocaleUtil(loc);
		if (timeDelay == null) {
			return "";
		} else {
			timeDelay = timeDelay.toLowerCase();
		}
		if (timeDelay.equals("within minutes")) {
			return localeUtil.get("withinminutes");
		} else if (timeDelay.equals("within an hour")) {
			return localeUtil.get("withinanhour");
		} else if (timeDelay.equals("within a few hours")) {
			return localeUtil.get("withinafewhours");
		} else if (timeDelay.equals("within 1-2 days")) {
			return localeUtil.get("within1-2days");
		} else if (timeDelay.equals("within 7 days")) {
			return localeUtil.get("within7days");
		} else if (timeDelay.equals("within 14 days")) {
			return localeUtil.get("within14days");
		} else if (timeDelay.equals("within 30 minutes")) {
			return localeUtil.get("within30minutes");
		} else {
			return timeDelay;
		}
	}
	
	public static void main(String[] args) {
		Locale locale= new Locale("es");
		LocaleUtil localeUtil = new LocaleUtil(locale);
		System.out.println(localeUtil.get("withinminutes"));	
	}
}
