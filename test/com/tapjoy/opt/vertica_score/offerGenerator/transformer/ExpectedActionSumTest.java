package com.tapjoy.opt.vertica_score.offerGenerator.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowFactory;
import com.tapjoy.opt.sql.OfferBase;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalNormCVR;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.ExpectedActionSum;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.Transformer;

public class ExpectedActionSumTest implements ColumnDef {

	public static Map<Row, Row> getGlobalCVRMap() {
		Map<Row, Row> globalCVRMap = new HashMap<Row, Row>();
		RowFactory factory = new RowFactory(GlobalNormCVR.columnList);

		// settup offerKeyRow ==> CVR offer mapping table
		Row os_rank = factory.newRow();
		os_rank.setColumn(OS, "Android");
		os_rank.setColumn(OFFERWALL_RANK, "1");
		os_rank.setColumn(CVR, "0.02");

		RowFactory keyFactory = new RowFactory(GlobalNormCVR.keyColumns);
		Row key_row = keyFactory.newRow();
		key_row.setColumn(OS, "Android");
		key_row.setColumn(OFFERWALL_RANK, "1");

		globalCVRMap.put(key_row, os_rank);

		os_rank = factory.newRow();
		os_rank.setColumn(OS, "Android");
		os_rank.setColumn(OFFERWALL_RANK, "2");
		os_rank.setColumn(CVR, "0.05");

		key_row = keyFactory.newRow();
		key_row.setColumn(OS, "Android");
		key_row.setColumn(OFFERWALL_RANK, "2");

		globalCVRMap.put(key_row, os_rank);

		return globalCVRMap;
	}

	public Map<Row, Row> getOfferMap() {
		Map<Row, Row> offerMap = new HashMap<Row, Row>();

		String[] columnList = OfferBase.columnList;
		String[] derivedColumnList = OfferBase.derivedColumnList;
		
		String[] keyColumnList = { OS, ID, OFFERWALL_RANK };

		// settup offerKeyRow ==> CVR offer mapping table
		RowFactory offerFactory = new RowFactory(columnList, derivedColumnList);
		Row offer = offerFactory.newRow();

		offer.setColumn(OS, "Android");
		offer.setColumn(ID, "OfferA");
		offer.setColumn(OFFERWALL_RANK, 1);
		offer.setColumn(IMPRESSIONS_SUM, 400);
		offer.setColumn(ACTIONS_SUM, 20);
		
		RowFactory keyFactory = new RowFactory(keyColumnList);

		Row offerKey = keyFactory.newRow();
		offerKey.setColumn(OS, "Android");
		offerKey.setColumn(ID, "OfferA");
		offerKey.setColumn(OFFERWALL_RANK, "1");
		
		offerMap.put(offerKey, offer);

		return offerMap;
	}

	@Test
	public void checkOfferMap() {
		Map<Row, Row> offerMap = getOfferMap();
		
		String[] columnList = { OS, ID, OFFERWALL_RANK };
		RowFactory keyFactory = new RowFactory(columnList);
		Row offerKey = keyFactory.newRow();
		offerKey.setColumn(OS, "Android");
		offerKey.setColumn(ID, "OfferA");
		offerKey.setColumn(OFFERWALL_RANK, "1");

		Row offerRow = offerMap.get(offerKey);

		assertEquals("OfferA", offerRow.getColumn(ID));
	}

	@Test
	public void checkTransformer() {
		Map<Row, Row> globalCVRMap = getGlobalCVRMap();

		String[] keyColumnList = { OS, ID, OFFERWALL_RANK };
		RowFactory keyFactory = new RowFactory(keyColumnList);

		Row offerKey = keyFactory.newRow();
		offerKey.setColumn(OS, "Android");
		offerKey.setColumn(ID, "OfferA");
		offerKey.setColumn(OFFERWALL_RANK, "1");
		

		Transformer transformer = new ExpectedActionSum(globalCVRMap);

		Map<Row, Row> offerMap = getOfferMap();

		offerMap = transformer.transform(offerMap);
		assertEquals(1, offerMap.size());
		
		//0.02*400
		assertEquals("8.0", offerMap.get(offerKey).getColumn(EXPECTED_ACTIONS_SUM));		
	}

	@Test
	public void checkExpectedConversion() {
		Map<Row, Row> cvrMap = getGlobalCVRMap();

		RowFactory factory = new RowFactory(GlobalNormCVR.keyColumns);
		Row os_rank = factory.newRow();
		
		os_rank.setColumn(OS, "Android");
		os_rank.setColumn(OFFERWALL_RANK, "1");
		
		Row result = cvrMap.get(os_rank);

		assertTrue(result != null);
		assertEquals("0.02", result.getColumn(CVR));
	}

	@Test
	public void checkOfferCVRMap() {
		Map<Row, Row> globalCVRMap = getGlobalCVRMap();

		Transformer transformer = new ExpectedActionSum(globalCVRMap);

		Map<Row, Row> offerMap = getOfferMap();

		offerMap = transformer.transform(offerMap);

		assertEquals(1, offerMap.size());
	}
}
