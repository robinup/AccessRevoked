package com.tapjoy.opt.vertica_score.offerGenerator;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.EmptyRankedScoreException;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;

public class RankOfferJson implements ColumnDef {
	private static Logger logger = Logger.getLogger(RankOfferJson.class);

	private RankedOfferKey rankedOfferKey;
	private boolean enabled;

	public RankOfferJson(RankedOfferKey rankedOfferKey, boolean enabled) {
		this.rankedOfferKey = rankedOfferKey;
		this.enabled = enabled;
	}

	private String toJsonString(Row row) throws EmptyRankedScoreException {
		String jsonString = "";

		if (row.getColumn(ColumnDef.RANK_ADJUSTED_SCORE) == null) {
			throw new EmptyRankedScoreException("null adjusted score for Row:"
					+ row);
		}

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugOffer(row, "coverting to Json");
		}

		Integer rankIndex = 0;
		if (row.getColumn(ColumnDef.RANK_INDEX) != null) {
			rankIndex = new Integer(row.getColumn(ColumnDef.RANK_INDEX)
					.toString());
		} else {
			throw new EmptyRankedScoreException("missing ranked index:" + row);
		}

		/*
		 * String escapedOfferName = JSONObject.escape(row
		 * .getColumn(ColumnDef.OFFER_NAME));
		 */
		String baseString = "";
		boolean enableShowRate = false;
		if ((row.getColumn(ColumnDef.SHOW_RATE) != null)
				&& (row.getColumn(ColumnDef.SHOW_RATE_NEW) != null)) {
			Double showRate = new Double(row.getColumn(ColumnDef.SHOW_RATE))+0.0;
			Double showRateNew = new Double(
					row.getColumn(ColumnDef.SHOW_RATE_NEW));

			if (Math.abs(showRate - showRateNew) > 0.00001) {
				enableShowRate = true;
			}
		}

		if (enableShowRate) {
			logger.debug("found a row with new show rate new field::" + row
					+ " with show rate index::"
					+ row.getColumnIndex(ColumnDef.SHOW_RATE_NEW));

			baseString = "\"rank_index\":\"" + rankIndex + "\", "
					+ "\"offer_id\":\"" + row.getColumn(ColumnDef.ID) + "\", "
					+ "\"rank_score\":\""
					+ row.getColumn(ColumnDef.RANK_ADJUSTED_SCORE) + "\", "
					+ "\"offer_name\":\""
					+ row.getColumn(ColumnDef.OFFER_NAME).replaceAll("\"", "")
					+ "\", " + "\"offer_type\":\""
					+ row.getColumn(ColumnDef.ITEM_TYPE) + "\", "
					+ "\"offer_bid\":\"" + row.getColumn(ColumnDef.BID)
					+ "\", " + "\"show_rate\":\""
					+ row.getColumn(ColumnDef.SHOW_RATE) + "\", "
					+ "\"show_rate_new\":\""
					+ row.getColumn(ColumnDef.SHOW_RATE_NEW) + "\", "
					+ "\"partner_balance\":\""
					+ row.getColumn(ColumnDef.PARTNER_BALANCE) + "\" }";
		} else {
			baseString = "\"rank_index\":\"" + rankIndex + "\", "
					+ "\"offer_id\":\"" + row.getColumn(ColumnDef.ID) + "\", "
					+ "\"rank_score\":\""
					+ row.getColumn(ColumnDef.RANK_ADJUSTED_SCORE) + "\", "
					+ "\"offer_name\":\""
					+ row.getColumn(ColumnDef.OFFER_NAME).replaceAll("\"", "")
					+ "\", " + "\"offer_type\":\""
					+ row.getColumn(ColumnDef.ITEM_TYPE) + "\", "
					+ "\"offer_bid\":\"" + row.getColumn(ColumnDef.BID)
					+ "\", " + "\"show_rate\":\""
					+ row.getColumn(ColumnDef.SHOW_RATE) + "\", "
					+ "\"partner_balance\":\""
					+ row.getColumn(ColumnDef.PARTNER_BALANCE) + "\" }";
		}

		Integer isAudition = 0;
		if (row.getColumn(ColumnDef.IS_AUDITION) != null) {
			isAudition = new Integer(row.getColumn(ColumnDef.IS_AUDITION)
					.toString());
		}

		if (isAudition == 1) {
			jsonString = "{\"auditioning\":\"true\", " + baseString;
		} else {
			jsonString = "{" + baseString;
		}

		return jsonString;
	}

	public String toJsonString(List<Row> rowList)
			throws EmptyRankedScoreException, IOException, ParseException {
		StringBuffer buff = new StringBuffer();

		buff.append("{\n        \"key\":\"");

		// append key
		buff.append(rankedOfferKey.toKeyString());

		buff.append("\",\n        \"enabled\":\"");

		if (enabled) {
			buff.append("true");
		} else {
			buff.append("false");
		}

		buff.append("\",\n        \"offers\":\n        [");

		boolean isFirst = true;
		for (Row row : rowList) {
			if (isFirst) {
				buff.append("\n        " + toJsonString(row));
				isFirst = false;
			} else {
				buff.append(",\n" + toJsonString(row));
			}
		}

		buff.append("\n        ]\n}\n");

		String jsonString = buff.toString();

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new StringReader(jsonString));
		JSONObject jsonObject = (JSONObject) obj;
		logger.debug(jsonObject.get("key"));

		return jsonString;
	}
}
