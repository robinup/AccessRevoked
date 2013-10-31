package com.tapjoy.opt.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.object_cache.OfferCache;

public class VerticaConn {
	private static Logger logger = Logger.getLogger(VerticaConn.class);

	public static String getSchema() {
		String schema = "optimization";

		return schema;
	}

	public static String getTestSchema() {
		String schema = "opt_test"; 

		return schema;
	}

	private static Connection getBaseConnection() throws SQLException,
			ClassNotFoundException {
		String hostIp = null;
		String databaseName = null;
		String userName = null;
		String passWord = null;
		String port = null;
		
		Connection conn = null;
		
		//Class.forName("com.vertica.jdbc.Driver");
		Class.forName("com.vertica.Driver");
		
		if(OverallConfig.databaseServer.equals(OverallConfig.DatabaseServer.main.name)) {
			hostIp = OverallConfig.DatabaseServer.main.hostIp;
			databaseName = OverallConfig.DatabaseServer.main.databaseName;
			userName = OverallConfig.DatabaseServer.main.userName;
			passWord = OverallConfig.DatabaseServer.main.passWord;
			port = OverallConfig.DatabaseServer.main.port;		
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName, userName, passWord);   
		}
		else if(OverallConfig.databaseServer.equals(OverallConfig.DatabaseServer.backup.name)) {
			hostIp = OverallConfig.DatabaseServer.backup.hostIp;
			databaseName = OverallConfig.DatabaseServer.backup.databaseName;
			userName = OverallConfig.DatabaseServer.backup.userName;
			passWord = OverallConfig.DatabaseServer.backup.passWord;
			port = OverallConfig.DatabaseServer.backup.port;
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName, userName, passWord);   
		}
		else if(OverallConfig.databaseServer.equals(OverallConfig.DatabaseServer.verticaprod.name)) {
			//System.out.println("verticaprod connection !!!");
			hostIp = OverallConfig.DatabaseServer.verticaprod.hostIp;
			databaseName = OverallConfig.DatabaseServer.verticaprod.databaseName;
			userName = OverallConfig.DatabaseServer.verticaprod.userName;
			passWord = OverallConfig.DatabaseServer.verticaprod.passWord;
			port = OverallConfig.DatabaseServer.verticaprod.port;
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName + "?ssl=true", userName, passWord);   //added SSL connection   
		}
		else if(OverallConfig.databaseServer.equals(OverallConfig.DatabaseServer.verticaprod2.name)) {
			//System.out.println("verticaprod2 connection !!!");
			hostIp = OverallConfig.DatabaseServer.verticaprod2.hostIp;
			databaseName = OverallConfig.DatabaseServer.verticaprod2.databaseName;
			userName = OverallConfig.DatabaseServer.verticaprod2.userName;
			passWord = OverallConfig.DatabaseServer.verticaprod2.passWord;
			port = OverallConfig.DatabaseServer.verticaprod2.port;
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName + "?ssl=true", userName, passWord);   //added SSL connection
		}
		else {
			logger.fatal("missing database server setting!");
			return null;
		}	

		conn.setAutoCommit(false);

		return conn;
	}
	
	//product vertica only
	public static Connection getConnectionNew(String databasename, String schemaName) throws SQLException, ClassNotFoundException {  //added 10-15 LJ
		String hostIp = null;
		String databaseName = null;
		String userName = null;
		String passWord = null;
		String port = null;
		
		Connection conn = null;
		
		Class.forName("com.vertica.Driver");
		
		if(databasename.equals(OverallConfig.DatabaseServer.verticaprod.name)) {
			//System.out.println("verticaprod connection !!!");
			hostIp = OverallConfig.DatabaseServer.verticaprod.hostIp;
			databaseName = OverallConfig.DatabaseServer.verticaprod.databaseName;
			userName = OverallConfig.DatabaseServer.verticaprod.userName;
			passWord = OverallConfig.DatabaseServer.verticaprod.passWord;
			port = OverallConfig.DatabaseServer.verticaprod.port;
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName + "?ssl=true", userName, passWord);   //added SSL connection
		}
		else if(databasename.equals(OverallConfig.DatabaseServer.verticaprod2.name)) {
			//System.out.println("verticaprod2 connection !!!");
			hostIp = OverallConfig.DatabaseServer.verticaprod2.hostIp;
			databaseName = OverallConfig.DatabaseServer.verticaprod2.databaseName;
			userName = OverallConfig.DatabaseServer.verticaprod2.userName;
			passWord = OverallConfig.DatabaseServer.verticaprod2.passWord;
			port = OverallConfig.DatabaseServer.verticaprod2.port;
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName + "?ssl=true", userName, passWord);   //added SSL connection
		}
		else if(databasename.equals(OverallConfig.DatabaseServer.main.name)) {
			hostIp = OverallConfig.DatabaseServer.main.hostIp;
			databaseName = OverallConfig.DatabaseServer.main.databaseName;
			userName = OverallConfig.DatabaseServer.main.userName;
			passWord = OverallConfig.DatabaseServer.main.passWord;
			port = OverallConfig.DatabaseServer.main.port;		
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName, userName, passWord);   
		}
		else if(databasename.equals(OverallConfig.DatabaseServer.backup.name)) {
			hostIp = OverallConfig.DatabaseServer.backup.hostIp;
			databaseName = OverallConfig.DatabaseServer.backup.databaseName;
			userName = OverallConfig.DatabaseServer.backup.userName;
			passWord = OverallConfig.DatabaseServer.backup.passWord;
			port = OverallConfig.DatabaseServer.backup.port;
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName, userName, passWord);   
		}
		else if(databasename.equals(OverallConfig.DatabaseServer.localmain.name)) {
			hostIp = OverallConfig.DatabaseServer.localmain.hostIp;
			databaseName = OverallConfig.DatabaseServer.localmain.databaseName;
			userName = OverallConfig.DatabaseServer.localmain.userName;
			passWord = OverallConfig.DatabaseServer.localmain.passWord;
			port = OverallConfig.DatabaseServer.localmain.port;		
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName, userName, passWord);   
		}
		else if(databasename.equals(OverallConfig.DatabaseServer.localbackup.name)) {
			hostIp = OverallConfig.DatabaseServer.localbackup.hostIp;
			databaseName = OverallConfig.DatabaseServer.localbackup.databaseName;
			userName = OverallConfig.DatabaseServer.localbackup.userName;
			passWord = OverallConfig.DatabaseServer.localbackup.passWord;
			port = OverallConfig.DatabaseServer.localbackup.port;
			conn = DriverManager.getConnection("jdbc:vertica://" + hostIp
					+ ":" + port +"/" + databaseName, userName, passWord);   
		}
		else {
			logger.fatal("missing database server setting!");
			return null;
	}

	conn.setAutoCommit(false);
	
	conn.createStatement().executeUpdate("set search_path to " + schemaName);
	
	return conn;
	}

	public static Connection getConnection(String schemaName)
			throws SQLException, ClassNotFoundException {
		Connection conn = getBaseConnection();
		conn.createStatement()
				.executeUpdate("set search_path to " + schemaName);

		return conn;
	}

	public static Connection getConnection() throws SQLException,
			ClassNotFoundException {
		Connection conn = getBaseConnection();

		if (OverallConfig.isProduction == false) {
			conn.createStatement().executeUpdate(
					"set search_path to " + getTestSchema());
		} else {
			conn.createStatement().executeUpdate(
					"set search_path to " + getSchema());
		}

		return conn;
	}

	public static Connection getTestConnection() throws SQLException,
			ClassNotFoundException {
		Connection conn = getBaseConnection();
		conn.createStatement().executeUpdate(
				"set search_path to " + getTestSchema());

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
	
	public static void main(String[] args)
	{
		Connection conn = null;  //commented by LJ 08-16
		Connection conn2 = null; //only backup server of optimization.offers_raw has featured ads offers
		try {
			conn = VerticaConn.getConnectionNew("backup", "optimization");
			conn2 = VerticaConn.getConnectionNew("backup", "optimization");
			OfferCache.getInstance().resetMap(conn2, false);	
			OfferCache.getInstance().resetMap(conn, true);
		}  catch (Exception e) {
			e.printStackTrace();
			return;
		}  finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if(conn2 != null)
					conn2.close();
			} catch (SQLException e) {
				logger.error("run failed!!!", e);
			}
		}
	}
}
