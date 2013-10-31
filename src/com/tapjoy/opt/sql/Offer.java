package com.tapjoy.opt.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;

public class Offer extends OfferBase implements ColumnDef {
	//private static String ALL_OFFERS = "select :columnList " + "from offers";   

	// Deep Link offers should be excluded from all offers
	private static String ALL_OFFERS = "select :columnList " + "from offers where item_type != 'DeeplinkOffer'";
	
	private static String ALL_FEATURE_OFFERS = "select :columnList " + "from offers_raw LEFT OUTER JOIN offers ON "+
 										"offers_raw.id = offers.id where offers_raw.item_type != 'DeeplinkOffer'";

	public static Map<String, Row> getAllOfferMap(Connection conn, boolean featureflag)
			throws SQLException {

		if(!featureflag)
			return OfferBase.getAllOfferMap(conn, ALL_OFFERS, featureflag);
		else
			return OfferBase.getAllOfferMap(conn, ALL_FEATURE_OFFERS, featureflag);
	}
}
