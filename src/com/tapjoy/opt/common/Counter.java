package com.tapjoy.opt.common;

import com.tapjoy.opt.common.GroupBy.CallBackFunc;

public class Counter implements CallBackFunc {
	public void calculate(Row row, Row result, String key) {
		String count = result.getColumn(key);

		if (count == null) {
			count = "1";
		} else {
			count = new Integer(Integer.parseInt(count) + 1).toString();
		}

		result.setColumn(key, count);
	}
}