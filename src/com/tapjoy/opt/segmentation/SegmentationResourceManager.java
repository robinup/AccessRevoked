package com.tapjoy.opt.segmentation;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.tapjoy.opt.ModelController;
import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.segmentation.config.Configuration;
import com.tapjoy.opt.offerlist.OfferList;
import com.tapjoy.opt.resource.ResourceManager;

public class SegmentationResourceManager extends ResourceManager {
    // LeiTest - hack to override the reload interval

    private static Logger logger = Logger.getLogger(ResourceManager.class);

    public SegmentationResourceManager() {
        super();
        RELOAD_DELAY = 300000;
        setupCronJob();
    }

    @Override
    protected void initialize() {
        // TODO Auto-generated method stub
        container = SegmentationResourceDataContainer.getInstance();
        updateMgr = new SegmentationResourceUpdateManager(container);
        computeEngine = new SegmentationOfferListComputeEngine(container);

        System.out.println("Changqing -- Segmentation initialize");
        backupRsrcMgr =  ModelController.getRegModel(ModelController.getAlgoIndex(Configuration.BACKUP_ALGO_ID));
        System.out.println("Changqing -- Segmentation initialize - "
                + (backupRsrcMgr == null ? "true" : "false"));
    }

    @Override
    public String getOfferListKey(String defaultKey,
            HashMap<String, String> specs) {
        // Replace the algo part with own algo tag; attach the udid to it
        int pos = defaultKey.indexOf('.');
        return Configuration.ALGO_ID + defaultKey.substring(pos) + "." + specs.get("udid");

    }

    // Override the default version
    protected OfferList retrieveOrCreateOfferList(HashMap<String, String> specs) {
        // get the static key
        System.out
                .println("LeiTest -- retrieveOrCreateOfferList - backupRsrcMgr - "
                        + (backupRsrcMgr != null ? "not null" : "null"));
        String backupKey = backupRsrcMgr.getOfferListKey(specs, true);

        // String backupKey =
        // OptimizationService.getServiceEngine2().getOfferListKey(specs, true);
        System.out.println("LeiTest - retrieveOrCreateOfferList - backupKey: "
                + backupKey);
        if (backupKey == null) {
            logger.error("Requests mapped to empty key");
            System.out.println("LeiTest - retrieveOrCreateOfferList - 11111");
            return null;
        }

        // Check if the static version is in cache
        OfferList staticOl = OfferListCache.getInstance().retrieve(backupKey,
                true);
        if (staticOl == null) {
            System.out.println("LeiTest - retrieveOrCreateOfferList - 22222");
            return null;
        }

        // get the customized key
        String key = getOfferListKey(backupKey, specs);

        // check if the customized offer list is in cache. if so. serve it
        OfferList ol = OfferListCache.getInstance().retrieve(key, false);
        if (ol != null) {
            return ol;
        }

        return this.computeEngine.computeForDevice(key, specs, staticOl);
    }

    @Override
    public String getOfferListKey(HashMap<String, String> specs,
            boolean asStaticEngine) {
        return null;
    }

}
