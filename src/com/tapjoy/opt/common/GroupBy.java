package com.tapjoy.opt.common;

import java.util.HashMap;
import java.util.Map;


public class GroupBy {

	public interface CallBackFunc {
		/**
		 * return the group by function for the row
		 * 
		 * @param row
		 * @return
		 */
		public void calculate(Row row, Row result, String key);
	}

	public static class CallBackPair {
		public String columnName;
		public CallBackFunc callBack;

		public CallBackPair() {
		}
	}

	private String groupByColumns[];
	private CallBackPair[] callbacks;
	private CallBackPair[] globalCallbacks;
	private String resultColumns[];
	private String globalResultColumns[];

	// key row factory
	private RowFactory keyRowFactory;
	// result row factory
	private RowFactory resultRowFactory;
	private RowFactory globalResultRowFactory;
	// result map keyRow===>resultRow
	private Map<Row, Row> groupByResult = new HashMap<Row, Row>();
	// result of the global groupByResult
	private Row globalResultRow = null;

	private void init(String groupByColumns[], CallBackPair[] callbacks, CallBackPair[] globalCallbacks) {
		this.groupByColumns = groupByColumns;
		this.callbacks = callbacks;
		this.globalCallbacks = globalCallbacks;

		if ((groupByColumns != null) && (groupByColumns.length != 0)) {
			this.resultColumns = new String[this.groupByColumns.length
					+ callbacks.length];

			for (int i = 0; i < this.groupByColumns.length; i++) {
				resultColumns[i] = groupByColumns[i];
			}

			for (int i = 0; i < this.callbacks.length; i++) {
				resultColumns[this.groupByColumns.length + i] = callbacks[i].columnName;
			}

			keyRowFactory = new RowFactory(groupByColumns);
			resultRowFactory = new RowFactory(resultColumns);
		} 
		
		if(globalCallbacks != null) {
			this.globalResultColumns = new String[globalCallbacks.length];
			for (int i = 0; i < this.globalCallbacks.length; i++) {
				globalResultColumns[i] = globalCallbacks[i].columnName;
			}

			globalResultRowFactory = new RowFactory(globalResultColumns);
			globalResultRow = globalResultRowFactory.newRow();
		}
	}

	public GroupBy(String groupByColumns[], CallBackPair[] callbacks, CallBackPair[] globalCallbacks) {
		init(groupByColumns, callbacks, globalCallbacks);
	}

	public GroupBy(String groupByColumns[], CallBackPair[] callbacks) {
		init(groupByColumns, callbacks, null);
	}
	
	public GroupBy(CallBackPair[] callbacks) {
		init(null, null, callbacks);
	}

	public void proceess(Row row) {
		if ((groupByColumns != null) && (groupByColumns.length != 0)) {
			Row key = keyRowFactory.newRow();
			for (String keyColumn : groupByColumns) {
				Object data = row.getColumn(keyColumn);
				key.setColumn(keyColumn, data);
			}

			Row resultRow = groupByResult.get(key);
			if (resultRow == null) {
				resultRow = resultRowFactory.newRow();
				for (String keyColumn : groupByColumns) {
					Object data = row.getColumn(keyColumn);
					resultRow.setColumn(keyColumn, data);
				}

				groupByResult.put(key, resultRow);
			}

			for (CallBackPair callback : callbacks) {
				if (callback.callBack != null) {
					callback.callBack.calculate(row, resultRow,
							callback.columnName);
				}
			}
		}
		
		if(globalCallbacks != null) {
			for (CallBackPair callback : globalCallbacks) {
				callback.callBack.calculate(row, globalResultRow,
						callback.columnName);
			}
		}
	}

	public Map<Row, Row> getGroupByResult() {
		return groupByResult;
	}

	public Row getGlobGroupByResult() {
		return this.globalResultRow;
	}
}
