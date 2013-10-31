package com.tapjoy.opt.vertica_score.offerGenerator.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowFactory;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.sql.Offer;
import com.tapjoy.opt.util.StringUtil;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.CountryFilter;

public class CountryFilterTest {
	private static Logger logger = Logger.getLogger(CountryFilterTest.class);
	private static Map<Row, Row> offerMap;

	@Before
	public void setUp() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();
		Map<String, Row> map = OfferCache.getInstance().get(conn);
		offerMap = new HashMap<Row, Row>();

		String[] keys = Offer.keyColumnList;
		RowFactory factory = new RowFactory(keys);
		for (Row row : map.values()) {
			String id = row.getColumn(ColumnDef.ID);

			Row keyRow = factory.newRow();
			keyRow.setColumn(ColumnDef.ID, id);

			offerMap.put(keyRow, row);
		}

		conn.close();
	}

	@Test
	public void checkUS() {
		CountryFilter filter = new CountryFilter("US");

		assertEquals(2507, offerMap.size());

		for (Row row : offerMap.values()) {
			List<String> countries = StringUtil.formattedStringToList(row
					.getColumn(ColumnDef.COUNTRIES));
			Set<String> countriesSet = StringUtil.listToSet(countries);

			logger.debug("ID:" + row.getColumn(ColumnDef.ID) + ", countries:"
					+ countriesSet);
		}

		offerMap = filter.transform(offerMap);

		assertEquals(1182, offerMap.size());
		for (Row row : offerMap.values()) {
			List<String> countries = StringUtil.formattedStringToList(row
					.getColumn(ColumnDef.COUNTRIES));
			Set<String> countriesSet = StringUtil.listToSet(countries);
			if (countriesSet.isEmpty() == false) {
				// logger.error("working on row:"+row +
				// " countriesSet is:"+countriesSet +
				// " countriesSet size is::"+countriesSet.size());
				assertTrue(countriesSet.contains("US"));
			}
		}
	}

	@Test
	public void checkGB() {
		CountryFilter filter = new CountryFilter("GB");

		offerMap = filter.transform(offerMap);

		assertEquals(863, offerMap.size());
		for (Row row : offerMap.values()) {
			List<String> countries = StringUtil.formattedStringToList(row
					.getColumn(ColumnDef.COUNTRIES));
			Set<String> countriesSet = StringUtil.listToSet(countries);
			assertTrue((countriesSet.size() == 0)
					|| countriesSet.contains("GB"));
		}
	}

	@Test
	public void checkJP() {
		CountryFilter filter = new CountryFilter("JP");

		offerMap = filter.transform(offerMap);

		assertEquals(847, offerMap.size());
		for (Row row : offerMap.values()) {
			List<String> countries = StringUtil.formattedStringToList(row
					.getColumn(ColumnDef.COUNTRIES));
			Set<String> countriesSet = StringUtil.listToSet(countries);
			assertTrue((countriesSet.size() == 0)
					|| countriesSet.contains("JP"));
		}
	}
}
