package com.tapjoy.opt.util;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.tapjoy.opt.common.MysqlConn;


public class MysqlConnTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		int counter = 0;

		try {
			String sqlString = "select id, partner_id, item_type from offers limit 3";
			Connection cn = MysqlConn.getBaseConnection();
			Statement stmt = cn.createStatement();
			
			ResultSet myResult = stmt.executeQuery(sqlString);
			
			while (myResult.next()) {
				if (myResult.getString("id") != null) {
					counter ++;
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(counter == 3);
	}

}
