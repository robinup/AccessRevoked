package com.tapjoy.opt.vertica_score.target.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;

public class AppInstallReturner implements ColumnDef {
	public static String SEL_SQL = "select q.offer_id, q.publisher_app_id, q.installs, r.returners "
			+ "from "
			+ "( "
			+ "select offer_id, publisher_app_id, sum(total_install) as installs "
			+ "from app_install "
			+ "where install_day between date(now()) - 28 and date(now()) - 2 group by 1, 2 "
			+ ") q "
			+ "left join "
			+ "( "
			+ "select offer_id, publisher_app_id, sum(total_rerun) as returners "
			+ "from app_dayone_rerun "
			+ "where install_day between date(now()) - 28 and date(now()) - 2 "
			+ "group by 1, 2 having sum(total_rerun) > 0) r "
			+ "on "
			+ "(q.offer_id = r.offer_id and q.publisher_app_id = r.publisher_app_id) "
			+ "order by r.returners desc";

	public static String[] columnList = { OFFER_ID, PUBLISHER_APP_ID, INSTALLS,
			RETURNERS };

	public static String[] keyColumns = { OFFER_ID, PUBLISHER_APP_ID };

	/**
	 * returning the map for OFFER_ID, PUBLISHER_APP_ID ==> OFFER_ID,
	 * PUBLISHER_APP_ID, INSTALLS, RETURNERS
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<Row, Row> getInstallReturner(Connection conn)
			throws SQLException {
		List<Row> installReturnerList = new GenericQuery().runQuery(conn,
				SEL_SQL, columnList, null, false);
		Map<Row, Row> map = Row.toKeyMap(installReturnerList, keyColumns);

		return map;
	}
}
