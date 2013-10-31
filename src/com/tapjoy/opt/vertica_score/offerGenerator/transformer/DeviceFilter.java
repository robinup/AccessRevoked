package com.tapjoy.opt.vertica_score.offerGenerator.transformer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.util.StringUtil;

/**
 * Appending an additon column called EXPECTED_ACTIONS_SUM and SCORE
 * @author lli
 *
 */
public class DeviceFilter implements Transformer, Filter, ColumnDef {
	private static Logger logger = Logger.getLogger(DeviceFilter.class);
	private String os = null;
	private String device = null;

	public DeviceFilter(String os, String device) {
		this.os = os;
		this.device = device;
	}

	/**
	 * filtering offers won't show up for the country
	 */
	@Override
	public Map<Row, Row> transform(Map<Row, Row> rowMap) {
		Iterator<Row> iter = rowMap.values().iterator();
		while (iter.hasNext()) {
			Row row = iter.next();
			List<String> devices = OfferRowUtil.getDevicesFromOffer(row, os);
			Set<String> devicesSet = StringUtil.listToSet(devices);
			
			// filtering those offers having device target, but the country is not part of them
			if ((devices != null) && (devices.size() != 0) && (devicesSet.contains(device) == false)) {
				iter.remove();
				logger.debug("based on devices::"+devices+", filtering out "+row);
			}
		}

		return rowMap;
	}

	/**
	 * check is the offer valid for the device
	 * 
	 * @param row
	 * @return
	 */
	public static boolean isValidRow(Row row, String os, String device) {
		boolean isValid = true;
		
		List<String> devices = OfferRowUtil.getDevicesFromOffer(row, os);
		Set<String> devicesSet = StringUtil.listToSet(devices);
		
		// filtering those offers having country target, but the country is not part of them
		// filtering those offers having device target, but the country is not part of them
		if ((devices != null) && (devices.size() != 0) && (devicesSet.contains(device) == false)) {
			isValid = false;
			logger.debug("based on country::"+device+", found the invalid "+row);
		}
		
		return isValid;
	}
	
	/**
	 * filtering offers won't show up for the country
	 */
	public List<Row> transform(List<Row> offerList) {
		Iterator<Row> iter = offerList.iterator();
		while (iter.hasNext()) {
			Row row = iter.next();
			if(isValidRow(row,  os,  device) == false) {
				iter.remove();
				logger.debug("based on device::"+device+", filtering out "+row);
			}
		}

		return offerList;
	}
}
