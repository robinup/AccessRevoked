package com.tapjoy.opt.common;

import java.util.Comparator;


/**
 * currently only supporting double field for comparison
 * 
 * @author lli  revised by ljiang
 *
 */
public class RowDoubleFieldComparator implements Comparator<Row> {
	private String comparableFieldName;

	public RowDoubleFieldComparator(String comparableFieldName) {
		this.comparableFieldName = comparableFieldName;
	}

	public int compare(Row o1, Row o2) {
		if ((o1 == null) && (o2 == null)) {
			return 0;
		} else if ((o1 == null) && (o2 != null)) {
			return 1;
		} else if ((o1 != null) && (o2 == null)) {
			return -1;
		}

		Double field1 = new Double(o1.getColumn(comparableFieldName));
		Double field2 =  new Double(o2.getColumn(comparableFieldName));

		return field2.compareTo(field1); //changed to ensure the robustness of comparator
	}

}
