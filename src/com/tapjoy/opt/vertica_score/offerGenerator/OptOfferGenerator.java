package com.tapjoy.opt.vertica_score.offerGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;

public interface OptOfferGenerator {
	public abstract String getSegment();

	public abstract RankedOfferKey getRankedOfferKey(String currentOS,
			String device, String algorithm);

	// Offer Aggregation
	public abstract void executeOfferAgg(Connection conn) throws SQLException;

	// Retrieving opt offer map
	public abstract Map<String, Row> getOptOfferMap(Connection conn) throws SQLException;

	// CVR, OS_OFFERID_RANK ==> ROW
	public abstract Map<Row, Row> getOfferNormCVRMap(Connection conn) throws SQLException;

	public abstract Map<Row, Row> getOfferList(Connection conn,
			Map<String, Row> allOfferMap) throws SQLException;

	public abstract void updateMap(Connection conn, 
			Map<RankedOfferKey, List<Row>> rankedOfferMap) throws SQLException;
}