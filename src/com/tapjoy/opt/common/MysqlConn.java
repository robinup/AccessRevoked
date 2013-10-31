package com.tapjoy.opt.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.tapjoy.opt.config.OverallConfig;

public class MysqlConn {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MysqlConn.class);

	public static Connection getBaseConnection() throws SQLException, ClassNotFoundException {
		String	hostIp = OverallConfig.MySQLConf.host;
		String	databaseName = OverallConfig.MySQLConf.database;
		String	userName = OverallConfig.MySQLConf.user;
		String	passWord = OverallConfig.MySQLConf.password;
		int	port = OverallConfig.MySQLConf.port;

		Connection conn = null;
		Class.forName("com.mysql.jdbc.Driver");

		conn = DriverManager.getConnection("jdbc:mysql://" + hostIp
				+ ":" + port +"/" + databaseName, userName, passWord);

		conn.setAutoCommit(false);

		return conn;
	}


	public static Connection getMySqlTestConnection() throws
	ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
		String hostIp = "replica.tapjoy.com";
		String dbName = "tapjoy_db";
		String userName = "tapjoy";
		String passWord = "xatrugAxu6";
		String url = "jdbc:mysql://" + hostIp + ":3306/";
		String driver = "com.mysql.jdbc.Driver";

		Connection conn = null;

		Class.forName(driver).newInstance();
		conn = DriverManager.getConnection(url + dbName, userName, passWord);
		conn.setAutoCommit(false);

		return conn;
	}

}
