package com.tapjoy.opt.common;

import java.util.HashMap;
import java.util.Map;

import com.tapjoy.opt.config.OverallConfig;

public class NonDeepLinkOfferFilter implements ColumnDef {
	public static  Map<String, Row> filter(Map<String, Row> map) {
		Map<String, Row> nonDeepLinkOffer = new HashMap<String, Row>();

		for (String id : map.keySet()) {
			Row row = map.get(id);
			Object itemType = row.getColumn(ITEM_TYPE);
			if ((itemType != null) && (itemType.equals(OverallConfig.OFFER.DEEP_LINK_OFFER) == false)) {
				nonDeepLinkOffer.put(id, row);
			}
		}

		return nonDeepLinkOffer;
	}
}
