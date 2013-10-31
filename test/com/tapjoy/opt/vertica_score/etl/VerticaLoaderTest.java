package com.tapjoy.opt.vertica_score.etl;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.junit.Ignore;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.VerticaLoader;

@Ignore
public class VerticaLoaderTest {
	private static Logger logger = Logger.getLogger(VerticaLoaderTest.class);

	@Ignore
	public void loadOfferWallViewFile() throws SQLException,
			ClassNotFoundException, FileNotFoundException {
		Connection conn = null;
		String tableName = "actions";
		String fileName = "bin/data/actions/part-m-00044.gz";

		logger.debug("loadOfferWallViewFile");

		conn = VerticaConn.getTestConnection();
		VerticaLoader.loadFile(conn, fileName, tableName);
	}

	@Ignore
	public void loadOfferWallViewsFile() throws SQLException,
			ClassNotFoundException, FileNotFoundException {
		Connection conn = null;
		String tableName = "offerwall_views";
		String dirName = "bin/data/offerwall_views";

		conn = VerticaConn.getTestConnection();
		VerticaLoader.loadDirectory(conn, dirName, tableName);
	}

	@Ignore
	public void loadActionsFile() throws SQLException, ClassNotFoundException,
			FileNotFoundException {
		Connection conn = null;
		String tableName = "actions";
		String dirName = "bin/data/actions";

		conn = VerticaConn.getTestConnection();
		VerticaLoader.loadDirectory(conn, dirName, tableName);
	}
}
