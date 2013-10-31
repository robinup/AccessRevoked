package com.tapjoy.opt.vertica_score.etl.sql.global;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowDefaultComparator;

public class GlobalNormCVR implements ColumnDef {
	private static Logger logger = Logger.getLogger(GlobalNormCVR.class);

	public static String GLOBAL_CVR_SQL = "select :columnList "
			+ "from gen_global_norm_ctr_cvr;";
	public static String[] columnList = { OS, OFFERWALL_RANK, CVR };
	public static String[] keyColumns = { OS, OFFERWALL_RANK };

	/**
	 * returning the map for global CVR, OS_RANK ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<Row, Row> getNormCVRMap(Connection conn) throws SQLException {
		List<Row> globalRank = new GenericQuery().runQuery(conn,
				GLOBAL_CVR_SQL, columnList, null, false);
		Map<Row, Row> map = Row.toKeyMap(globalRank, keyColumns);

		if (logger.isDebugEnabled()) {
			debugCVRMap(map);
		}

		return map;
	}

	private void debugCVRMap(Map<Row, Row> map) {
		logger.debug("transform with globalCVRMap::");

		List<Row> keyList = new ArrayList<Row>();
		for (Row key : map.keySet()) {
			keyList.add(key);
		}
		Collections.sort(keyList, new RowDefaultComparator());

		for (Row key : keyList) {
			Row value = map.get(key);
			logger.debug("Global CVR Key::" + key + " value::" + value);
		}
	}
}
