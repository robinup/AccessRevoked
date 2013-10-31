package com.tapjoy.opt.common;

import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.junit.Test;

import com.tapjoy.opt.common.Counter;
import com.tapjoy.opt.common.GroupBy;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowFactory;
import com.tapjoy.opt.common.GroupBy.CallBackPair;

public class GroupByTest {

	/**
	 * Testing the null values in the keys
	 */
	@Test
	public void countTest() {
		String[] columns = { "A", "B", "C" };
		String[] groupByColumns = { "A", "B" };
		RowFactory factory = new RowFactory(columns);
		RowFactory keyFactory = new RowFactory(groupByColumns);

		CallBackPair[] callbackpairs = new CallBackPair[1];
		callbackpairs[0] = new CallBackPair();
		callbackpairs[0].columnName = "count";
		callbackpairs[0].callBack = new Counter();

		GroupBy groupBy = new GroupBy(groupByColumns, callbackpairs);

		for (int i = 0; i < 5; i++) {
			Row row = factory.newRow();
			groupBy.proceess(row);
		}

		Map<Row, Row> results = groupBy.getGroupByResult();
		Row keyRow = keyFactory.newRow();

		for (String keyColumn : groupByColumns) {
			Object data = keyRow.getColumn(keyColumn);
			keyRow.setColumn(keyColumn, data);
		}

		Row resultRow = results.get(keyRow);
		assertEquals("5", resultRow.getColumn("count"));
	}

	/**
	 * Testing the NOT null values in the keys
	 */
	@Test
	public void countNotNullTest() {
		String[] columns = { "A", "B", "C" };
		String[] groupByColumns = { "A", "B" };
		RowFactory factory = new RowFactory(columns);
		RowFactory keyFactory = new RowFactory(groupByColumns);

		CallBackPair[] callbackpairs = new CallBackPair[1];
		callbackpairs[0] = new CallBackPair();
		callbackpairs[0].columnName = "count";
		callbackpairs[0].callBack = new Counter();

		GroupBy groupBy = new GroupBy(groupByColumns, callbackpairs);

		for (int i = 0; i < 10; i++) {
			Row row = factory.newRow();
			row.setColumn("A", "a");
			row.setColumn("B", "b");
			row.setColumn("C", "c" + i);
			groupBy.proceess(row);
		}

		for (int i = 0; i < 2; i++) {
			Row row = factory.newRow();
			row.setColumn("A", "a");
			row.setColumn("B", "b" + i);
			row.setColumn("C", "c" + i);
			groupBy.proceess(row);
		}

		Map<Row, Row> results = groupBy.getGroupByResult();

		Row keyRow = keyFactory.newRow();
		keyRow.setColumn("A", "a");
		keyRow.setColumn("B", "b");

		Row resultRow = results.get(keyRow);
		assertEquals("10", resultRow.getColumn("count"));

		keyRow = keyFactory.newRow();
		keyRow.setColumn("A", "a");
		keyRow.setColumn("B", "b1");

		resultRow = results.get(keyRow);
		assertEquals("1", resultRow.getColumn("count"));
	}

	@Test
	public void globalCountTest() {
		String[] columns = { "A", "B", "C" };

		RowFactory factory = new RowFactory(columns);

		CallBackPair[] callbackpairs = new CallBackPair[1];
		callbackpairs[0] = new CallBackPair();
		callbackpairs[0].columnName = "count";
		callbackpairs[0].callBack = new Counter();

		GroupBy groupBy = new GroupBy(callbackpairs);

		for (int i = 0; i < 5; i++) {
			Row row = factory.newRow();
			groupBy.proceess(row);
		}

		Row resultRow = groupBy.getGlobGroupByResult();
		assertEquals("5", resultRow.getColumn("count"));
	}
}
