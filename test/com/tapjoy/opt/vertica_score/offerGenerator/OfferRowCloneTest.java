package com.tapjoy.opt.vertica_score.offerGenerator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.object_cache.OfferCache;

public class OfferRowCloneTest {
	@Test
	public void offerRowClone() throws SQLException, ClassNotFoundException,
			IOException {
		Connection conn = VerticaConn.getTestConnection();
		
		Map<String, Row> allOfferMap = OfferCache.getInstance().get(conn);
		String offerId = "2921c72e-f61b-4b65-a4c1-ea29abd0f4f6";
		Row offerRow = allOfferMap.get(offerId).clone();
		offerRow.setColumn(ColumnDef.CONVERT_SCORE, 0.01);
		
		assertEquals("0.01", offerRow.getColumn(ColumnDef.CONVERT_SCORE));
		assertEquals(offerId, offerRow.getColumn(ColumnDef.ID));
	}
}
