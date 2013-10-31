package com.tapjoy.opt.segmentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.log4j.Logger;

import com.tapjoy.opt.common.HBaseConn;
import com.tapjoy.opt.conversion_matrix.ConversionMatrixResourceDataContainer;
import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.resource.ResourceUpdateManager;
import com.tapjoy.opt.segmentation.config.Configuration;
import com.tapjoy.opt.util.S3;

public class SegmentationResourceUpdateManager extends ResourceUpdateManager {
    protected static Logger logger = Logger
            .getLogger(SegmentationResourceUpdateManager.class);

    public SegmentationResourceUpdateManager(ResourceDataContainer dataContainer) {
        super(dataContainer);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void reloadDataResource() {
        // Download the segmentation score file from S3
        logger.info("SegmentationResourceUpdateManager :: reloadDataResource ");
        System.out.println("Changqing -- segmentation cvr loading ...");
        S3 s3 = new S3();
        try {
            s3.downloadAllFiles(Configuration.S3LocalCopyDir,
                    "tj-optimization", "segmentation", "");
            HashMap<String, Float> seg_score = new HashMap<String, Float>();

            File SegScoreFile = new File(Configuration.S3LocalCopyDir
                    + "/segmentation_score");
            @SuppressWarnings("resource")
            BufferedReader reader = new BufferedReader(new FileReader(
                    SegScoreFile));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\t");
                seg_score.put(items[0], Float.valueOf(items[1]));
            }
            ((SegmentationResourceDataContainer) dataContainer).seg_score = seg_score;

        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ((SegmentationResourceDataContainer) dataContainer).rttable = HBaseConn
                .initRTTable(Configuration.RT_TABLE_NAME);
        ((SegmentationResourceDataContainer) dataContainer).auxtables = new ArrayList<HTableInterface>();
        ((SegmentationResourceDataContainer) dataContainer).auxtables
                .add(((SegmentationResourceDataContainer) dataContainer).rttable);

        for (String tablestr : Configuration.AUX_TABLE_NAMES) {
            ((ConversionMatrixResourceDataContainer) dataContainer).auxtables
                    .add(HBaseConn.initTable(tablestr));
        }
        logger.info("SegmentationResourceUpdateManager :: reloadDataResource Done");
    }

    @Override
    protected HashSet<String> getStaticOfferListKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void setOfferListKeys(HashSet<String> offerListKeys) {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getIDKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Logger getLogger() {
        // TODO Auto-generated method stub
        return null;
    }

}
