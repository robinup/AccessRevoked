package com.tapjoy.opt.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.tapjoy.opt.common.GenericQuery;

public class Token {
	private static String TOKEN_SQL = "select max(token) as token " + "from optsoa_tokens where table_name='";
	
	public static String getToken(Connection conn, String tablename)
			throws SQLException {

		return new GenericQuery().runQueryToToken(conn, TOKEN_SQL+tablename+"'");
	}
}
