package com.tapjoy.opt.publisher_opt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.tapjoy.opt.publisher_opt.config.Configuration;
import com.tapjoy.opt.resource.OfferListComputeEngine;
import com.tapjoy.opt.resource.ResourceDataContainer;

public class PublisherOptimizationOfferListComputeEngine extends
        OfferListComputeEngine {
    private static Logger logger = Logger
            .getLogger(PublisherOptimizationOfferListComputeEngine.class);

    public PublisherOptimizationOfferListComputeEngine(
            ResourceDataContainer dataContainer) {
        super(dataContainer);
    }

    /**
     * Parse an offerlist result file to a List of CompoundRow
     * 
     * @param file
     * @param parser
     * @param allOfferMap
     * @return
     */
    private LinkedList<String> parseOfferListFile(File file, JSONParser parser) {
        LinkedList<String> offerlist = new LinkedList<String>();
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            Object obj = parser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray offers = (JSONArray) jsonObject.get("offers");
            Iterator<Object> iterator = offers.iterator();

            while (iterator.hasNext()) {
                JSONObject offer = (JSONObject) iterator.next();
                String offerid = (String) offer.get("offer_id");
                offerlist.add(offerid);
            }
        } catch (Exception e) {
            logger.error("parseOfferListFile() -- Exception happend - "
                    + e.getMessage());
            return null;
        }
        return offerlist;
    }

    @Override
    // TODO -- add status check for this function
    public boolean computeStaticSegments() {
        // logger.debug("LeiTest -- LinearRegressionOfferListComputeEngine -- ");

        // For each of the offer list files downloaded from S3, create an
        // OfferListWithRef Obj and
        // store that Obj to the Persistent Cache
        HashSet<String> offerListKeys = new HashSet<String>();
        // Connection conn = null;
        try {
            JSONParser parser = new JSONParser();
            //System.out.println("start to read publisher offerwall in computeStaticSegments");
            File pubOfferlistDir = new File(Configuration.S3LocalCopyDir
                    + "/publisher_offerwall/");
            if (pubOfferlistDir != null && !pubOfferlistDir.exists()) // each
                                                                      // launch
                                                                      // of
                                                                      // OptSOA,
                                                                      // we have
                                                                      // a new
                                                                      // config
                                                                      // file
            {
                pubOfferlistDir.mkdir();
            }

            if (pubOfferlistDir == null || !pubOfferlistDir.isDirectory()) {
                return false;
            }

            File[] files = pubOfferlistDir.listFiles();
            if (files.length == 0) {
                return false;
            }

            for (File file : files) {
                String key = file.getName();
                //logger.info("computeStaticSegments() -- generating offer list for key: "
                //        + key);
                LinkedList<String> offerlist = parseOfferListFile(file, parser);
                offerListKeys.add(key);
                PublisherOptimizationResourceDataContainer.cachedList.put(key,
                        offerlist);
            }

            PublisherOptimizationResourceDataContainer.getInstance().staticOfferLists = offerListKeys;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }
}
