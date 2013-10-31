package com.tapjoy.opt.common;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;


public class GenericQuery {
	private static Logger logger = Logger.getLogger(GenericQuery.class);

	public List<Row> runQuery(Connection conn, String query, String[] columns,
			String[] derivedColumns, int minRows, int sleepInSeconds, boolean featureflag )
			throws SQLException {
		List<Row> rows = new ArrayList<Row>();

		while (true) {
			rows = runQuery(conn, query, columns, derivedColumns, featureflag);

			if (rows.size() > minRows) {
				break;
			} else {
				logger.error(rows.size() + "Rows returned. not enough rows returned, retrying after sleep");
				try {
					rows.clear();
					Thread.sleep(sleepInSeconds * 1000);
				} catch (InterruptedException e) {
					logger.error("runQuery failed!!!", e);
				}
			}
		}

		return rows;
	}
	
	/*public List<Row> runQuery(Connection conn, String query, String[] columns,
			String[] derivedColumns, int minRows, int sleepInSeconds)
			throws SQLException {
		List<Row> rows = new ArrayList<Row>();

		while (true) {
			rows = runQuery(conn, query, columns, derivedColumns, false);

			if (rows.size() > minRows) {
				break;
			} else {
				logger.error(rows.size() + "Rows returned. not enough rows returned, retrying after sleep");
				try {
					rows.clear();
					Thread.sleep(sleepInSeconds * 1000);
				} catch (InterruptedException e) {
					logger.error("runQuery failed!!!", e);
				}
			}
		}

		return rows;
	}*/

	public Map<Row, Row> runQueryToKeyMap(Connection conn, String query,
			String[] columns, String[] derivedColumns, String[] keyColumns, boolean featureflag)
			throws SQLException {
		List<Row> resultSet = runQuery(conn, query, columns, derivedColumns, featureflag);
		Map<Row, Row> map = Row.toKeyMap(resultSet, keyColumns);

		return map;
	}
	
	//Added by LJ
	public String runQueryToToken(Connection conn, String query) throws SQLException
	{
		Statement stmt = null;
		String res = new String();

		try {
			stmt = conn.createStatement();
			
			logger.debug("executing query<" + query);
			ResultSet myResult = stmt.executeQuery(query);
			logger.debug("Done executing query>");

			while (myResult.next()) {
			
				res = myResult.getString("token");
				break;
			}
			
		} catch (SQLException e) {
			logger.error("runQuery failed!!!", e);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

		return res;
	}

	public List<Row> runQuery(Connection conn, String query, String[] columns,
			String[] derivedColumns, boolean featureflag) throws SQLException {
		List<Row> rowList = new ArrayList<Row>();

		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			String columnList = "";
			for (String col : columns) {
				if(featureflag && Arrays.asList(ColumnDef.offer_raw).contains(col))
                     col = "offers_raw." + col;   //for featured ads by Justin
				if (columnList.length() > 0) {
					columnList += ", " + col;
				} else {
					columnList += col;
				}
			}

			String replacedSQL = query.replaceFirst(":columnList", columnList);
			
			logger.debug("executing query<" + replacedSQL);
			ResultSet myResult = stmt.executeQuery(replacedSQL);
			logger.debug("Done executing query>");

			RowFactory rowFactory = new RowFactory(columns, derivedColumns);
			while (myResult.next()) {
				Row row = rowFactory.newRow();

				for (String col : columns) {
					row.setColumn(col, myResult.getString(col));
				}

				rowList.add(row);
			}
			
			if(featureflag)  //check query
			{
				logger.info("featured ads executing query<" + replacedSQL);
				for(Row r: rowList)
				{
					logger.info(r.getColumns().toString());
				}
			}
			else
			{
				logger.info("non-featured ads executing query<" + replacedSQL);
			}
		} catch (SQLException e) {
			logger.error("runQuery failed!!!", e);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

		return rowList;
	}
}
