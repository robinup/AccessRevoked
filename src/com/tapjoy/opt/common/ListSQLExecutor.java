package com.tapjoy.opt.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Enable us to execute a list of SQL statements in sequence
 * 
 * @author lli
 */
public class ListSQLExecutor {
	private static Logger logger = Logger.getLogger(ListSQLExecutor.class);
	private String[] sqlList;

	public ListSQLExecutor(String[] sqlList) {
		this.sqlList = sqlList;
	}

	public ListSQLExecutor findAndReplace(String find, String replace) {
		sqlList = listSQLReplace(sqlList, find, replace);
		return this;
	}

	public void execute(Connection conn) throws SQLException {
		listSqlExecute(conn, sqlList);
	}

	/**
	 * Executing SQL one by one from the array
	 * 
	 * @param conn
	 * @param sqlList
	 * @throws SQLException
	 */
	public static void listSqlExecute(Connection conn, String[] sqlList)
			throws SQLException {
		logger.debug("listSqlExecute<");
		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			for (String sqlString : sqlList) {
				logger.info("Executing::" + sqlString);

				stmt.execute(sqlString);
				logger.info("DONE with executing");
			}

			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			logger.error("listSqlExecute fatal error!!!", e);

			// throw again
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
				logger.debug("listSqlExecute>");
			}
		}
	}

	/**
	 * replace list of sql statements with parsed in pattern
	 */
	public static String[] listSQLReplace(String[] sqlList, String find,
			String replace) {
		Pattern pattern = Pattern.compile(find);
		String[] replacedSQL = new String[sqlList.length];

		int index = 0;
		for (String sql : sqlList) {
			Matcher matcher = pattern.matcher(sql);
			replacedSQL[index++] = matcher.replaceAll(replace);
		}

		return replacedSQL;
	}

	/**
	 * replace a sql statements with parsed in pattern
	 */
	public static String listSQLReplace(String sql, String find, String replace) {
		Pattern pattern = Pattern.compile(find);

		Matcher matcher = pattern.matcher(sql);
		String replacedSQL = matcher.replaceAll(replace);

		return replacedSQL;
	}
}
