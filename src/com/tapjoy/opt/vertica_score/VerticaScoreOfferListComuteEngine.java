package com.tapjoy.opt.vertica_score;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.resource.OfferListComputeEngine;
import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferFileWriter;

public class VerticaScoreOfferListComuteEngine extends OfferListComputeEngine {
	private static Logger logger = Logger.getLogger(VerticaScoreOfferListComuteEngine.class);

	public VerticaScoreOfferListComuteEngine(ResourceDataContainer container) {
		super(container);
	}

	@Override
	public boolean computeStaticSegments(){

		Map<RankedOfferKey, List<Row>> rankedOfferMap = ((VerticaScoreResourceDataContainer) dataContainer).rankedOfferMap;
		Map<String, Row> allOfferMap = ((VerticaScoreResourceDataContainer) dataContainer).allOfferMap;
		Date current = ((VerticaScoreResourceDataContainer) dataContainer).current;

		// For each of the offer list files downloaded from S3, create an OfferListWithRef Obj and 
		//   store that Obj to the Persistent Cache	
		HashSet<String> offerListKeys = new HashSet<String>();
		
		Connection conn = null;

		try {
			conn = VerticaConn.getConnection();
			if (conn == null) {
				logger.error("ERROR -- in computeStaticSegments Vertica conn is NULL");
				return false;
			}

			if (rankedOfferMap == null) {
				logger.fatal("Ranked offer map is null!!!");
				return false;
			} 
			
			if (allOfferMap == null) {
				logger.fatal("all offer map is null!!!");
				return false;
			} 
			
			System.out.printf("going to call outputFile for offer generator and ranking - LJ\n");
			
			OptOfferFileWriter.outputFile(conn, rankedOfferMap, allOfferMap, current, offerListKeys);
			VerticaScoreResourceDataContainer.getInstance().staticOfferLists = offerListKeys;
			
			return true;
		} catch (SQLException e) {
			logger.error("MapRefreshJob SQLException:", e);

		} catch (IOException e) {
			logger.error("MapRefreshJob IOException:", e);
		} catch (ClassNotFoundException e) {
			logger.error("MapRefreshJob ClassNotFoundException:", e);
		} catch (Throwable t) {
			logger.error("fatal throwable caught!!!", t);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("run failed!!!", e);
			}
		}
		
		return false;
	}

}
