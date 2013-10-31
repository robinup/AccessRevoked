package com.tapjoy.opt.common;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.maxmind.geoip.LookupService;


public class GeoIpService {
	private static Logger logger = Logger.getLogger(GeoIpService.class);
	private static LookupService cl;
	
	public static void initialize(){
		try {
			cl = new LookupService("/usr/local/share/GeoIP/GeoIPCity.dat", LookupService.GEOIP_MEMORY_CACHE );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("GeoIP Service Initialization Failure. ", e);
		}
	}
	
	public static LookupService getGeoIpService(){
		if (cl == null) {
			initialize();
		}
		return cl;
	}
}
