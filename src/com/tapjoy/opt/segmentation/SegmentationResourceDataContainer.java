package com.tapjoy.opt.segmentation;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.hbase.client.HTableInterface;

import com.tapjoy.opt.resource.ResourceDataContainer;

public class SegmentationResourceDataContainer extends ResourceDataContainer {

    private static SegmentationResourceDataContainer instance = new SegmentationResourceDataContainer();

    public HashMap<String, Float> seg_score = null;

    public HTableInterface rttable = null;

    public String htabletoken = null;

    public ArrayList<HTableInterface> auxtables = null;

    public int rttoken = 0;

    private SegmentationResourceDataContainer() {
        this.identity = "seg_score";
    }

    public static SegmentationResourceDataContainer getInstance() {
        return instance;
    }

}