package com.tapjoy.opt.common;

import com.tapjoy.opt.common.GroupBy.CallBackFunc;

public class DoubleSum implements CallBackFunc {
	//private static Logger logger = Logger.getLogger(Sum.class);

	// the column that summary value from
	private String sumColumn = "";

	public DoubleSum(String sumColumn) {
		this.sumColumn = sumColumn;
	}

	public void calculate(Row row, Row result, String key) {
		Object sum = result.getColumn(key);
		//logger.debug("sum for key::" + key + " is::" + sum);

		Object columnValue = row.getColumn(sumColumn);
		//logger.debug("columnValue for::" + sumColumn + " is::" + columnValue);

		Double value = 0.0;
		if ((columnValue != null) && (columnValue.toString().length() > 0)) {
			value = new Double(columnValue.toString());
		}

		if ((sum == null) || (sum.toString().length() == 0)) {
			sum = value;
		} else {
			sum = Double.parseDouble(sum.toString()) + value;
		}

		//logger.debug("Now sum for key::" + key + " is::" + sum);
		result.setColumn(key, sum);
	}
}