package com.tapjoy.opt.publisher_opt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.tapjoy.opt.ModelController;
import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.offerlist.CompoundRow;
import com.tapjoy.opt.offerlist.OfferList;
import com.tapjoy.opt.offerlist.OfferListWithref;
import com.tapjoy.opt.publisher_opt.config.Configuration;
import com.tapjoy.opt.resource.ResourceManager;
import com.tapjoy.opt.util.StringUtil;

public class PublisherOptimizationResourceManager extends ResourceManager {

    private static Logger logger = Logger.getLogger(ResourceManager.class);
    public static HashSet<String> pubopt_app_list;

    public PublisherOptimizationResourceManager() {
        super();
        RELOAD_DELAY = 900;
        MCKEY_BASE = "PublisherOptimization";
        setupCronJob();
    }

    @Override
    protected void initialize() {
        // TODO Auto-generated method stub
        container = PublisherOptimizationResourceDataContainer.getInstance();
        updateMgr = new PublisherOptimizationResourceUpdateManager(container);
        computeEngine = new PublisherOptimizationOfferListComputeEngine(
                container);

        System.out.println("ChangqingTest -- Publisher OPT initialize");
        backupRsrcMgr = ModelController.getRegModel(ModelController
                .getAlgoIndex(Configuration.BACKUP_ALGO_ID));
        System.out.println("ChangqingTest -- Publisher OPT initialize - "
                + (backupRsrcMgr == null ? "no backup resourcemanager"
                        : "there is backup resourcemanager"));
    }

    public String getOfferListKey(HashMap<String, String> specs,
            boolean asStaticEngine) {
        StringBuffer buff = new StringBuffer();

        // Algo
        String algo = specs.get("algorithm");
        if (StringUtil.findInArray(algo, Configuration.algorithms)) {
            buff.append(algo);
        } else if (asStaticEngine) {
            algo = Configuration.DEFAULT_ALGO;
            buff.append(Configuration.DEFAULT_ALGO);
        } else {
            return null;
        }

        // Platform
        buff.append(".");
        if (specs.get("source") == null) {
            return null;
        } else {
            String source = specs.get("source");
            if ("offerwall".equals(source)) {
                buff.append("0");
            } else {
                buff.append("1");
            }
        }

        // os
        buff.append(".");
        String os = specs.get("platform");
        if (!StringUtil.findInArray(os, Configuration.OS.activeOs)) {
            return null;
        } else {
            buff.append(os.toLowerCase());
        }

        // Country
        buff.append(".");

        // app
        buff.append(".");
        String appid = specs.get("app_id");
        if (appid == null)
            return null;
        else {
            buff.append(specs.get("app_id"));
        }

        // device
        String device = specs.get("device_type");
        if (device == null) {
            return null;
        }
        buff.append(".");
        buff.append(device.toLowerCase());

        return buff.toString();
    }

    protected OfferList retrieveOrCreateOfferList(HashMap<String, String> specs) {
        // get the static key
        // System.out
        //        .println("Publisher OPT -- retrieveOrCreateOfferList - backupRsrcMgr - "
        //                + (backupRsrcMgr != null ? "not null" : "null"));
        String backupKey = backupRsrcMgr.getOfferListKey(specs, true);

        //System.out
        //        .println("Publisher OPT - retrieveOrCreateOfferList - backupKey: "
        //                + backupKey);
        if (backupKey == null) {
            logger.error("Requests mapped to empty key");
            System.out
                    .println("Publisher OPT - retrieveOrCreateOfferList - backupkey null");
            return null;
        }

        // Check if the static version is in cache
        OfferList staticOl = OfferListCache.getInstance().retrieve(backupKey,
                true);
        if (staticOl == null) {
            System.out
                    .println("Publisher OPT - retrieveOrCreateOfferList - backup offerList null");
            return null;
        }

        // get the customized key
        String key = getOfferListKey(specs, true);
        // System.out.println("Publisher OPT - new key " + key);

        // check if the customized offer list is in cache. if so. serve it
        LinkedList<String> ol = PublisherOptimizationResourceDataContainer.cachedList
                .get(key);

        if (ol == null) {
            specs.put("algorithm", Configuration.BACKUP_ALGO_ID);
            return staticOl;
        }

        // algo starts
        long startTS = System.nanoTime();
        @SuppressWarnings("unchecked")
        List<CompoundRow> offers = (List<CompoundRow>) staticOl.getOffers();
        HashMap<String, CompoundRow> allOfferMap = new HashMap<String, CompoundRow>();

        Iterator<CompoundRow> iter = offers.iterator();
        while (iter.hasNext()) {
            CompoundRow of = iter.next();
            allOfferMap.put(of.id, of);
        }

        HashSet<String> subqueset = new HashSet<String>(); // ensure that there
                                                           // is no redundant
                                                           // offers from
                                                           // history of more
                                                           // than one offer

        ArrayList<CompoundRow> updateOffers = new ArrayList<CompoundRow>();
        Iterator<String> iter1 = ol.iterator();
        while (iter1.hasNext()) {
            String curOfferID = iter1.next();
            if (allOfferMap.containsKey(curOfferID)) {
                updateOffers.add(allOfferMap.get(curOfferID));
                subqueset.add(curOfferID);
            }
        }
        iter = offers.iterator();
        while (iter.hasNext()) {
            CompoundRow of = iter.next();
            if (!subqueset.contains(of.id))
                updateOffers.add(of);
        }

        long endTS = System.nanoTime();
        //logger.info("Finished real-time algo. Time used: " + (endTS - startTS)
        //        + " nanoseconds");
        OfferList custormizedOfferList = new OfferListWithref(key,
                updateOffers, true);

        return custormizedOfferList;
    }

    @Override
    public String getOfferListKey(String defaultKey,
            HashMap<String, String> specs) {
        return null;
    }
}
