package com.tapjoy.opt.vertica_score.offerGenerator.transformer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;

/**
 * Appending an additon column called EXPECTED_ACTIONS_SUM and SCORE
 * 
 * @author lli
 * 
 */
public class CountryFilter implements Transformer, Filter, ColumnDef {
	private static Logger logger = Logger.getLogger(CountryFilter.class);
	private String country = null;

	public CountryFilter(String country) {
		this.country = country;
	}

	/**
	 * check is the offer valid for the country
	 * 
	 * @param row
	 * @return
	 */
	public static boolean isValidRow(Row row, String country) {
		boolean isValid = true;

		if ( (country != null) && (country.length() > 0) ) {
			String countries = row.getColumn(COUNTRIES);

			// filtering those offers having country target, but the country is
			// not part of them
			if ((countries != null) && (countries.length() != 0)
					&& ((countries.contains(country) == false))) {
				isValid = false;
				logger.debug("based on country::" + country
						+ ", found the invalid " + row);
			}
		}

		return isValid;
	}

	/**
	 * filtering offers won't show up for the country
	 */
	@Override
	public Map<Row, Row> transform(Map<Row, Row> rowMap) {
		Iterator<Row> iter = rowMap.values().iterator();
		while (iter.hasNext()) {
			Row row = iter.next();

			// filtering those offers having country target, but the country is
			// not part of them
			if (isValidRow(row, country) == false) {
				iter.remove();
				logger.debug("based on country::" + country
						+ ", filtering out " + row);
			}
		}

		return rowMap;
	}

	/**
	 * filtering offers won't show up for the country
	 */
	public List<Row> transform(List<Row> offerList) {
		Iterator<Row> iter = offerList.iterator();
		while (iter.hasNext()) {
			Row row = iter.next();
			String countries = row.getColumn(COUNTRIES);

			// filtering those offers having country target, but the country is
			// not part of them
			if ((countries != null) && (countries.length() != 0)
					&& ((countries.contains(country) == false))) {
				iter.remove();
				logger.debug("based on country::" + country
						+ ", filtering out " + row);
			}
		}

		return offerList;
	}
}
