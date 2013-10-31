package com.tapjoy.opt.vertica_score.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigurationLookback {
	public Map<String, Integer> lookBackDates = new HashMap<String, Integer>();

	public void init(Map<String, Integer> lookBackDates) {
		this.lookBackDates = lookBackDates;
	}

	public Integer getLookBackDate(String id) {
		return lookBackDates.get(id);
	}

	public Set<Integer> getLookBackDates() {
		Set<Integer> dates = new HashSet<Integer>();

		for (String id : lookBackDates.keySet()) {
			dates.add(lookBackDates.get(id));
		}

		return dates;
	}

	public Set<String> getIds(Integer lookbackDate) {
		Set<String> ids = new HashSet<String>();

		for (String id : lookBackDates.keySet()) {
			if (lookBackDates.get(id) == lookbackDate) {
				ids.add(id);
			}
		}

		return ids;
	}

	public String getIdString(Integer lookbackDate) {
		Set<String> ids = getIds(lookbackDate);

		StringBuffer idListStr = new StringBuffer("");
		for (String id : ids) {
			if (idListStr.length() == 0) {
				idListStr.append("'" + id + "'");
			} else {
				idListStr.append(", " + "'" + id + "'");
			}
		}

		return idListStr.toString();
	}

	public String getAllIdString() {
		StringBuffer idListStr = new StringBuffer("");
		for (String id : lookBackDates.keySet()) {
			if (idListStr.length() == 0) {
				idListStr.append("'"+id+"'");
			} else {
				idListStr.append(", " + "'"+id+"'");
			}

		}

		return idListStr.toString();
	}

	/**
	 * return the mapping of lookback dates to country codes
	 * 
	 * @return
	 */
	public Map<Integer, Set<String>> getLookBackIds() {
		Map<Integer, Set<String>> lookBackCountryCode = new HashMap<Integer, Set<String>>();
		Set<Integer> allLookbackDates = getLookBackDates();

		for (Integer lookbackDate : allLookbackDates) {
			Set<String> ids = getIds(lookbackDate);
			lookBackCountryCode.put(lookbackDate, ids);
		}

		return lookBackCountryCode;
	}
}
