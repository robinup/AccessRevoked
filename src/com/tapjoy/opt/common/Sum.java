package com.tapjoy.opt.common;

import com.tapjoy.opt.common.GroupBy.CallBackFunc;

public class Sum implements CallBackFunc {

	// the column that summary value from
	private String sumColumn = "";

	public Sum(String sumColumn) {
		this.sumColumn = sumColumn;
	}

	public void calculate(Row row, Row result, String key) {
		Object sum = result.getColumn(key);
		Object columnValue = row.getColumn(sumColumn);

		Long value = 0L;
		if ((columnValue != null) && (columnValue.toString().length() > 0)) {
			value = new Long(columnValue.toString());
		}

		if ((sum == null) || (sum.toString().length() == 0)) {
			sum = value;
		} else {
			sum = Long.parseLong(sum.toString()) + value;
		}

		result.setColumn(key, sum);
	}
}