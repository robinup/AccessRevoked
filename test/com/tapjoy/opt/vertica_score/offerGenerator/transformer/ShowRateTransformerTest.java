package com.tapjoy.opt.vertica_score.offerGenerator.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowFactory;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalNormCVR;
import com.tapjoy.opt.vertica_score.offerGenerator.ShowRateTransformer;
import com.tapjoy.opt.vertica_score.showrate.OfferBudget;

public class ShowRateTransformerTest implements ColumnDef {

	public Map<Row, Row> getOfferMap() {
		Map<Row, Row> offerMap = new HashMap<Row, Row>();

		String[] columnList = { OS, ID, OFFERWALL_RANK, IMPRESSIONS_SUM, ACTIONS_SUM, EXPECTED_ACTIONS_SUM };
		String[] keyColumnList = { OS, ID, OFFERWALL_RANK };

		// settup offerKeyRow ==> CVR offer mapping table
		RowFactory offerFactory = new RowFactory(columnList);
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
	public void checkTransformer() throws SQLException, ClassNotFoundException, ParseException {
		String[] columnList = { OS, ID, OFFERWALL_RANK, IMPRESSIONS_SUM, ACTIONS_SUM, EXPECTED_ACTIONS_SUM };

		// settup offerKeyRow ==> CVR offer mapping table
		RowFactory offerFactory = new RowFactory(columnList);

		Row offer1 = offerFactory.newRow();

		offer1.setColumn(OS, "Android");
		offer1.setColumn(ID, "OfferA");
		offer1.setColumn(OFFERWALL_RANK, 1);
		offer1.setColumn(IMPRESSIONS_SUM, 800);
		offer1.setColumn(ACTIONS_SUM, 10);

		Row offer2 = offerFactory.newRow();

		offer2.setColumn(OS, "Android");
		offer2.setColumn(ID, "OfferA");
		offer2.setColumn(OFFERWALL_RANK, 1);
		offer2.setColumn(IMPRESSIONS_SUM, 400);
		offer2.setColumn(ACTIONS_SUM, 20);
				
		offer2.addDerivedColumn(SHOW_RATE_NEW, "1");
		
		assertEquals("800", offer1.getColumn(IMPRESSIONS_SUM));		
		assertEquals("400", offer2.getColumn(IMPRESSIONS_SUM));		
		assertEquals("1", offer2.getColumn(SHOW_RATE_NEW));		
	}

}
