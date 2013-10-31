package com.tapjoy.opt.vertica_score.showrate;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.EmailLogger;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowFactory;

public class OfferBudget implements ColumnDef {
	public static String[] offerKeyColumns = { OFFER_ID };
	private static Logger logger = Logger.getLogger(OfferBudget.class);

	private Map<String, Double> showRateMap = new HashMap<String, Double>();

	public Map<String, Double> retrieveShowRate(Connection conn)
			throws SQLException, ParseException {
		Map<Row, Row> dailyBudgetMap = DailyBudget.getDailyBudget(conn);
		
		int prevHourDelay = this.getPreviousHourDelay(dailyBudgetMap);
		Map<Row, Row> hourlyConversionMap = HourlyConversion.getHourlyConversion(conn, prevHourDelay);

		Iterator<Entry<Row, Row>> iter = dailyBudgetMap.entrySet().iterator();
		RowFactory offerRowFactory = new RowFactory(offerKeyColumns);
		while (iter.hasNext()) {
			Entry<Row, Row> entry = iter.next();
			Row row = entry.getValue();
			String offer_id = row.getColumn(OFFER_ID);

			Row keyRow = offerRowFactory.newRow();
			keyRow.setColumn(OFFER_ID, offer_id);
			Row dailyRow = dailyBudgetMap.get(keyRow);
			Row hourlyRow = hourlyConversionMap.get(keyRow);

			if (dailyRow != null) {
				if (dailyRow.getColumn(SHOW_RATE) != null) {
					double showRate = new Double(dailyRow.getColumn(SHOW_RATE));
					int dailyBudget = new Integer(
							dailyRow.getColumn(DAILY_BUDGET) == null ? "0"
									: dailyRow.getColumn(DAILY_BUDGET));
					int partnerBalance = new Integer(
							dailyRow.getColumn(PARTNER_BALANCE) == null ? "0"
									: dailyRow.getColumn(PARTNER_BALANCE));
					int dailyConversion = new Integer(
							dailyRow.getColumn(DAILY_CONVERSION) == null ? "0"
									: dailyRow.getColumn(DAILY_CONVERSION));
					int hourlyConversion = 0;

					if (hourlyRow != null) {
						hourlyConversion = new Integer(
								hourlyRow.getColumn(HOURLY_CONVERSION) == null ? "0"
										: hourlyRow
												.getColumn(HOURLY_CONVERSION));
					}

					int deltaInMinutes = 0;

					if (dailyRow.getColumn(LAST_CONVERSION_TIME) != null) {
						DateFormat df = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss zzz");
						Date date = df.parse(dailyRow
								.getColumn(LAST_CONVERSION_TIME) + " GMT");

						Date current = new Date();
						long diffInMilliseconds = current.getTime()
								- date.getTime();
						deltaInMinutes = (int) (diffInMilliseconds / (60 * 1000));						
					}

					setupShowRate(offer_id, row.getColumn(ITEM_TYPE),
							row.getColumn(OFFER_NAME), showRate, dailyBudget,
							partnerBalance, dailyConversion, hourlyConversion,
							deltaInMinutes);
				}
			} else {
				logger.warn("hourly row is missing:" + offer_id);
			}
		}

		return showRateMap;
	}
	
	/**
	 * Simulating the Perl code logic to get the last hour (GMT) that has the full hourly conversion record
	 * 
	 * @param dailyBudgetMap
	 * @return
	 */
	private int getPreviousHourDelay(Map<Row, Row> dailyBudgetMap){
		Iterator<Entry<Row, Row>> iter = dailyBudgetMap.entrySet().iterator();
		long diffInMS = Long.MAX_VALUE;
		Date current = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");

		try{
			while (iter.hasNext()) {
				Entry<Row, Row> entry = iter.next();
				Row dailyRow = entry.getValue();

				if (dailyRow.getColumn(LAST_CONVERSION_TIME) != null) {

					Date date = df.parse(dailyRow.getColumn(LAST_CONVERSION_TIME) + " GMT");


					long diffInMilliseconds = current.getTime() - date.getTime();
					if(diffInMS > diffInMilliseconds){
						diffInMS  = diffInMilliseconds;
					}
				}
			}
		} catch(Exception e){
			logger.error("OfferBudget:getPreviouseHourDelay caused error", e);
		}

		return (int)(diffInMS / 1000) + 3600;
	}

	private void setupShowRate(String offerId, String item_type,
			String offerName, double show_rate, int daily_budget,
			int partner_balance, int daily_convs, int convs, int delta) {

		// check if no spending or unlimited budget, full speed
		if (daily_budget > 0 && daily_convs == 0 || daily_budget == 0) {
			showRateMap.put(offerId, 1.0);
			logger.debug("offer id:" + offerId + ", offer name:" + offerName
					+ "show rate:1.0");
		} else if (partner_balance < 0 || daily_convs >= daily_budget) {
			showRateMap.put(offerId, 0.0);
			logger.debug("offer id:" + offerId + ", offer name:" + offerName
					+ "show rate:0.0");
			EmailLogger.getInstance().info(
					logger,
					"Offer " + offerName + "(" + offerId
							+ ") is stopped due to budget constraints");

		} else if (daily_budget > 0 && daily_convs > 0) {
			// just be cautious of over spending
			if (show_rate < 0.001) {
				logger.debug("offer id:" + offerId + ", offer name:"
						+ offerName + "show rate:0.0");
				EmailLogger
						.getInstance()
						.info(logger,
								"Offer "
										+ offerName
										+ "("
										+ offerId
										+ ") is throttled  due to projected over spending by native show rate");
				return;
			}

			if (show_rate < 0.01 && item_type.equals("GenericOffer")) {
				// be more conservative on Generic offers
				showRateMap.put(offerId, 0.0);

				logger.debug("offer id:" + offerId + ", offer name:"
						+ offerName + "show rate:0.0");
				EmailLogger
						.getInstance()
						.info(logger,
								"Offer "
										+ offerName
										+ "("
										+ offerId
										+ ") is throttled  due to projected over spending by native show rate on generic offer");
				return;
			}

			if (convs == 0) {
				// no conversion in the last hour, slight risk of overshooting
				// in the next 15 minutes, but will take it to maximize spending
				showRateMap.put(offerId, 1.0);
				logger.debug("offer id:" + offerId + ", offer name:"
						+ offerName + "show rate:1.0");
				return;
			}

			double exp_convs = daily_convs + (delta + 15.0) * convs / 60.0;
			if (exp_convs > daily_budget) {
				// # in 15 minutes, it will overshoot, we will just throttle it
				showRateMap.put(offerId, 0.0);
				logger.debug("offer id:" + offerId + ", offer name:"
						+ offerName + "show rate:0.0");
				EmailLogger
						.getInstance()
						.info(logger,
								"Offer "
										+ offerName
										+ "("
										+ offerId
										+ ") is throttled  due to projected over spending");
				return;
			}

			double exp_convs2 = daily_convs + (delta + 30.0) * convs / 60.0;
			if (exp_convs2 > daily_budget) {
				// # in the next 30 minutes, it will overshoot, slow it down
				double sr = 4 * (daily_budget - exp_convs) / convs;

				// # that's how we should even it out in the next 15 minutes
				if (sr > 1.0) {
					sr = 1.0;
				}

				showRateMap.put(offerId, sr);
				logger.debug("offer id:" + offerId + ", offer name:"
						+ offerName + "show rate:" + sr);

				EmailLogger.getInstance().info(
						logger,
						"Offer " + offerName + "(" + offerId
								+ ") is slowed down, show_rate =" + sr);

				return;
			}

			showRateMap.put(offerId, 1.0);

			// # low risk of overshooting
			logger.debug("offer id:" + offerId + ", offer name:" + offerName
					+ "show rate:1.0");
		}
	}
}
