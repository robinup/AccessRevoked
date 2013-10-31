package com.tapjoy.opt.offerfilter;

import java.util.HashMap;


public abstract class PrivateOfferFilter extends OfferFilter {
	// For private filters we may need to initiate the them for a specific request
	protected abstract void initiate(HashMap<String, String> requestSpec);
	
}
