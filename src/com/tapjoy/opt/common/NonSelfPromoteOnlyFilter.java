package com.tapjoy.opt.common;

import java.util.HashMap;
import java.util.Map;


public class NonSelfPromoteOnlyFilter implements ColumnDef {
	public static  Map<String, Row> filter(Map<String, Row> map) {
		Map<String, Row> nonDeepLinkOffer = new HashMap<String, Row>();

		for (String id : map.keySet()) {
			Row row = map.get(id);
			Object selfPromoteOnly = row.getColumn(SELF_PROMOTE_ONLY);
			if ((selfPromoteOnly != null) && (selfPromoteOnly.toString().trim().equals("0") == true)) {
				nonDeepLinkOffer.put(id, row);
			}
		}

		return nonDeepLinkOffer;
	}
}
