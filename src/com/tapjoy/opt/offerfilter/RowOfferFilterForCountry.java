package com.tapjoy.opt.offerfilter;

import java.util.HashMap;

import com.tapjoy.opt.common.Row;

public class RowOfferFilterForCountry extends PrivateOfferFilter {
	private String primaryCountry = null;
	private String region;
	private String dma_code;
	private String city;
	
	
	public RowOfferFilterForCountry(HashMap<String, String> requestSpecs) {
		initiate(requestSpecs);
	}
	
	protected void initiate(HashMap<String, String> requestSpec){
		primaryCountry = requestSpec.get("primaryCountry");
		region = requestSpec.get("region");
		city = requestSpec.get("city");
		dma_code = requestSpec.get("dma_code");
	}
	
	public boolean isValid(Row row){
		if (primaryCountry == null) {
			return true;
		}
		
		// First check allowed countries
		String countries =  row.getColumn(COUNTRIES);
		if (countries != null && ( ! "".equals(countries)) && (! countries.contains(primaryCountry)) ) {
			return false;
		}
		
		// Second check country blacklist
		String countryBlacklist = row.getColumn(COUNTRIES_BLACKLIST);
		if (countryBlacklist != null && countryBlacklist.contains(primaryCountry) ) {
			return false;
		}
		
		// Next, check regions
		String regions =  row.getColumn(REGIONS);
		if (regions != null && region != null && ( ! "".equals(regions)) && (! regions.contains(region)) ) {
			return false;
		}
		
		// Next, check dma_code
		String dma_codes =  row.getColumn(DMA_CODES);
		if (dma_codes != null && dma_code != null && ( ! "".equals(dma_codes)) && (! dma_codes.contains(dma_code)) ) {
			return false;
		}
		
		// Last, city
		String cities =  row.getColumn(CITIES);
		if (cities != null && city != null && ( ! "".equals(cities)) && (! cities.contains(city)) ) {
			return false;
		}
		
		
		return true;
	}
}
