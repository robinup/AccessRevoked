package com.tapjoy.opt.resource;

import java.util.HashSet;

/**
 *  A simple container. Put whatever you need into it
 *
 */
public abstract class ResourceDataContainer {
	protected String identity;
	
	// To keep track which of the static offer lists are belonging to this offer list engine
	public HashSet<String> staticOfferLists; 
	
	//public abstract void importFromMC();

}
