package com.tapjoy.opt.vertica_score.target.app.etl;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ListSQLExecutor;

public class AppRerunAgg {
	private static Logger logger = Logger.getLogger(AppRerunAgg.class);

	public static String initStatementList[] = {
			"truncate table target_app_publisher_agg",
			"truncate table target_app_agg" };

	public static String statementList[] = {
			"insert into target_app_publisher_agg "
					+ "select q.offer_id as offer_id, q.publisher_app_id as publisher_app_id, "
					+ "q.installs as installs, r.returners as returners "
					+ "from "
					+ "( "
					+ "select offer_id, publisher_app_id, sum(total_install) as installs "
					+ "from app_install "
					+ "where install_day between date(now()) - 28 and date(now()) - 2 "
					+ "group by 1, 2 "
					+ ") q "
					+ "left join "
					+ "( "
					+ "select offer_id, publisher_app_id, sum(total_rerun) as returners "
					+ "from app_dayone_rerun "
					+ "where install_day between date(now()) - 28 and date(now()) - 2 "
					+ "group by 1, 2 "
					+ ") r "
					+ "on q.offer_id = r.offer_id and q.publisher_app_id = r.publisher_app_id "
					+ "where r.returners > 0",

			"insert into target_app_agg "
					+ "select offer_id, sum(installs) as installs, sum(returners) as returners "
					+ "from target_app_publisher_agg " + "group by 1" };

	public static void execute(Connection conn) throws SQLException {
		logger.debug("ETL for app");

		ListSQLExecutor.listSqlExecute(conn, initStatementList);
		
		ListSQLExecutor.listSqlExecute(conn, statementList);
	}
}
