package com.tapjoy.opt.common;

import java.util.Comparator;


/**
 * currently only supporting double field for comparison
 * 
 * @author lli
 *
 */
public class RowDefaultComparator implements Comparator<Row> {

	public RowDefaultComparator() {
	}

	public int compare(Row o1, Row o2) {
		if ((o1 == null) && (o2 == null)) {
			return 0;
		} else if ((o1 == null) && (o2 != null)) {
			return 1;
		} else if ((o1 != null) && (o2 == null)) {
			return -1;
		}

		return (o1.toString().compareTo(o2.toString()));
	}

}
