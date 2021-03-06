package com.tapjoy.opt.logistic_regression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.MurmurHash3V2;
import com.tapjoy.opt.logistic_regression.config.Configuration;
import com.tapjoy.opt.offerlist.CompoundRow;
import com.tapjoy.opt.offerlist.CompoundRow.CompoundRowNewCompare;
import com.tapjoy.opt.offerlist.OfferList;
import com.tapjoy.opt.offerlist.OfferListWithref;
import com.tapjoy.opt.resource.OfferListComputeEngine;
import com.tapjoy.opt.resource.ResourceDataContainer;

public class LogisticRegressionOfferListComputeEngine extends OfferListComputeEngine {
        
        private static Logger logger = Logger.getLogger(LogisticRegressionOfferListComputeEngine.class);
        
        public LogisticRegressionOfferListComputeEngine(
                        ResourceDataContainer dataContainer) {
                super(dataContainer);
                // TODO Auto-generated constructor stub
                
        }

        @Override
        public boolean computeStaticSegments() {
                // TODO Auto-generated method stub
                return false;
        }
        
        
        // Generate device-customized Offer list. 
        @SuppressWarnings("unchecked")
		public OfferList computeForDevice(String resultKey, HashMap<String, String> request, OfferList staticOL){
                
                long start = System.nanoTime();
                
                String userId = request.get("udid");
                String source = request.get("source");
                String platform = request.get("platform");
                String deviceType = request.get("device_type");
                String exchangeRate = request.get("currency_exchange_rate");
                String locale = request.get("locale").toLowerCase();
                String city = request.get("city").toLowerCase();
                String country = request.get("primaryCountry").toUpperCase();
                String appId = request.get("app_id");
                String maxRankStr = request.get("max_rank");
                String alphaStr = request.get("alpha");
                
                int maxRank = Configuration.OFFERWALL_RANK_END;
                if (maxRankStr != null) {
                        int tmp = Integer.parseInt(maxRankStr);
                        if (tmp < maxRank) {
                                maxRank = tmp;
                        }
                }
                
                Double alpha = 0.5;
                if (alphaStr != null) {
                        Double tmp = Double.parseDouble(alphaStr);
                        if (tmp > 0 && tmp < 1) {
                                alpha = tmp;
                        }
                }
                
                if (country == null || country.length() != 2) {
                        country = "US";
                }
                
                ContextPredictor predictor = LogisticRegressionResourceDataContainer.getInstance().getContextPredictor(source, platform);
                if (predictor == null) {
                        logger.error(String.format("Invalid segment [%s.%s].", source, platform));         
                        request.put("algorithm", Configuration.BACKUP_ALGO_ID);
                        return staticOL;
                }
        
                
                List<CompoundRow> offers = new ArrayList<CompoundRow>();
                for(CompoundRow tmpo: (List<CompoundRow>)(staticOL.getOffers()))
                {
                	offers.add(tmpo);
                }
                	
                for(CompoundRow row : offers){
                        
                        double pcvr = predictor.predict(source, platform, deviceType, 
                                        country, city, locale, 
                                        1, maxRank, 
                                        Double.parseDouble(exchangeRate), row.id, appId, userId);
                        row.resv = alpha * Math.log(row.getBid()) + 
                                        (1 - alpha) * pcvr;
                }
                
                // Sort here
                Collections.sort(offers, new CompoundRowNewCompare());
                OfferListWithref offerlist = new OfferListWithref(resultKey, offers, true);
                
                long end = System.nanoTime();
                
                logger.info(String.format("Request [source=%s, platform=%s, device_type=%s, exchange_rate=%s, country=%s, city=%s, language=%s, app_id=%s] served in %d milliseconds",
                                source, platform, deviceType, exchangeRate, country, city, locale, appId,
                                TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS)));   
                return offerlist;
        }

}
