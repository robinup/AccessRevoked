package com.tapjoy.opt.vertica_score.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import com.vertica.PGStatement;

public class VerticaLoader {
	private static Logger logger = Logger.getLogger(VerticaLoader.class);

	@SuppressWarnings("deprecation")
	public static void loadFromIS(Connection conn, InputStream is,
			String tableName) throws SQLException {
		try {
			String sql = "copy "
					+ tableName
					+ " FROM STDIN "
					+ " gzip delimiter E'\\001' null '\\\\N' record terminator E'\\n' direct trailing nullcols; ";
			// +
			// " gzip delimiter E'\\001' null '\\\\N' record terminator E'\\n' EXCEPTIONS 'except.log' REJECTED DATA 'rejects.log' trailing nullcols; ";

			PGStatement stmt = (PGStatement) conn.createStatement();
			stmt.executeCopyIn(sql, is);

			long insertedRow = stmt.getNumAcceptedRows();
			long rejectedRow = stmt.getLongNumRejectedRows();
			logger.debug("Inserted rows : " + insertedRow
					+ ", Rejected rows : " + rejectedRow + " for table "
					+ tableName);

			if (rejectedRow > 0) {
				logger.error(rejectedRow + " rows have been rejected!!!");
			}
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			logger.error("loading failed!!!", e);
		}
	}

	public static void loadFile(Connection conn, String fileName,
			String tableName) {
		try {
			InputStream is = new FileInputStream(fileName);
			loadFromIS(conn, is, tableName);
			is.close();
		} catch (SQLException e) {
			logger.error("loadFile failed!!!", e);
		} catch (FileNotFoundException e) {
			logger.error("loadFile failed!!!", e);
		} catch (IOException e) {
			logger.error("loadFile failed!!!", e);
		}
	}

	public static void loadDirectory(Connection conn, String directory,
			String tableName) {
		try {
			File folder = new File(directory);
			File[] files = folder.listFiles();

			for (File file : files) {
				if (file.isFile()) {
					InputStream is = new FileInputStream(file.getPath());
					loadFromIS(conn, is, tableName);
					is.close();
				}
			}
		} catch (SQLException e) {
			logger.error("loadFile failed!!!", e);
		} catch (FileNotFoundException e) {
			logger.error("loadFile failed!!!", e);
		} catch (IOException e) {
			logger.error("loadFile failed!!!", e);
		}
	}
}
