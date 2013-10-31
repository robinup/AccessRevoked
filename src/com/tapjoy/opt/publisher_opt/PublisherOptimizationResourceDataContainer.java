package com.tapjoy.opt.publisher_opt;

import java.util.HashMap;
import java.util.LinkedList;

import com.tapjoy.opt.resource.ResourceDataContainer;

public class PublisherOptimizationResourceDataContainer extends
        ResourceDataContainer {
    // For Linear Regression Engine, we really don't need this container

    public static PublisherOptimizationResourceDataContainer instance = new PublisherOptimizationResourceDataContainer();
    public static HashMap<String, LinkedList<String>> cachedList = new HashMap<String, LinkedList<String>>();

    private PublisherOptimizationResourceDataContainer() {
        this.identity = "PublisherOptimization";
    }

    public static PublisherOptimizationResourceDataContainer getInstance() {
        return instance;
    }
}
