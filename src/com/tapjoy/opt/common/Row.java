package com.tapjoy.opt.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Row {
	private Map<String, Integer> columnMap = null;
	private Map<String, Integer> derivedColumnMap = null;
	private Map<Integer, String> indexMap = null;
	private String rowData[];

	public static class UndefinedColumnException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public UndefinedColumnException(String msg) {
			super(msg);
		}
	}

	public Row(Map<String, Integer> columnMap,
			Map<String, Integer> derivedColumnMap, Map<Integer, String> indexMap) {
		this.columnMap = columnMap;
		this.derivedColumnMap = derivedColumnMap;
		this.indexMap = indexMap;
		this.rowData = new String[columnMap.size() + derivedColumnMap.size()];
	}
	
	public Row(Map<String, Integer> columnMap, Map<String, Integer> derivedColumnMap, 
			Map<Integer, String> indexMap, String [] rowData) {
		this.columnMap = columnMap;
		this.derivedColumnMap = derivedColumnMap;
		this.indexMap = indexMap;
		this.rowData = rowData;
	}

	public static Map<Row, Row> toKeyMap(List<Row> rows, String[] keyColumns)
			throws SQLException {
		RowFactory keyFactory = new RowFactory(keyColumns);
		Map<Row, Row> keyMap = new HashMap<Row, Row>();
		for (Row row : rows) {

			Row key = keyFactory.newRow();
			for (String keyColumn : keyColumns) {
				key.setColumn(keyColumn, row.getColumn(keyColumn));
			}

			keyMap.put(key, row);
		}

		return keyMap;
	}

	/**
	 * clone the Row
	 */
	@Override
	public Row clone() {
		Row cloneRow = new Row(columnMap, derivedColumnMap, indexMap);

		for (int i = 0; i < rowData.length; i++) {
			cloneRow.rowData[i] = rowData[i];
		}

		return cloneRow;
	}
	
	public void addDerivedColumn(String column, String value) {
		String[] newRowData = new String[rowData.length + 1];

		for (int i = 0; i < rowData.length; i++) {
			newRowData[i] = rowData[i];
		}

		newRowData[rowData.length] = value;		
		rowData = newRowData;

		// cloning derivedColumnMap
		Map<String, Integer> newDerivedColumnMap = new HashMap<String, Integer>();
		for(String key:derivedColumnMap.keySet()) {
			newDerivedColumnMap.put(key, derivedColumnMap.get(key));
		}
		
		newDerivedColumnMap.put(column, rowData.length-1);
		derivedColumnMap = newDerivedColumnMap;

		// cloning indexMap
		Map<Integer, String> newIndexMap = new HashMap<Integer, String>();
		for(Integer key:indexMap.keySet()) {
			newIndexMap.put(key, indexMap.get(key));
		}

		newIndexMap.put(rowData.length-1, column);
		indexMap = newIndexMap;
	}

	/**
	 * copy values from another row
	 * 
	 * @return
	 */
	public void copy(Row row) {
		List<String> columnList = getColumnList();

		for (String column : columnList) {
			Object data = row.getColumn(column);
			this.setColumn(column, data);
		}
	}

	public String[] getColumns() {
		List<String> columnList = getColumnList();
		String[] columns = new String[columnList.size()];

		int i = 0;
		for (String col : columnList) {
			columns[i++] = col;
		}

		return columns;
	}

	/**
	 * returning the list of columns
	 * 
	 * @return
	 */
	public List<String> getColumnList() {
		List<String> columns = null;

		columns = new ArrayList<String>();
		for (int i = 0; i < rowData.length; i++) {
			columns.add(indexMap.get(i));
		}

		return columns;
	}

	/**
	 * return -1 if not found
	 * 
	 * @param column
	 * @return
	 */
	public Integer getColumnIndex(String column) {
		Integer index = columnMap.get(column);

		if ((index == null) && (derivedColumnMap != null)) {
			if (derivedColumnMap.get(column) != null) {
				index = derivedColumnMap.get(column);
			}
		}

		return index;
	}

	public Row setColumn(String column, Object data) {
		Integer index = getColumnIndex(column);

		if ((index != null) && (index >= 0)) {
			rowData[index] = ((data == null) ? "" : data.toString());
		} else {
			throw new UndefinedColumnException("Unkown column:" + column);
		}

		return this;
	}

	public String getColumn(String column) {
		String value = null;
		Integer index = getColumnIndex(column);

		if ((index != null) && (index >= 0)) {
			value = rowData[index];
		} else {
			throw new UndefinedColumnException("Unkown column:" + column);
		}

		return value;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;

		for (int i = 0; i < rowData.length; i++) {
			if (rowData[i] != null) {
				hashCode += rowData[i].hashCode();
			}
		}

		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;

		if ((obj != null) && (obj instanceof Row)) {
			Row inRow = (Row) obj;
			if ((this.rowData.length == inRow.rowData.length)
					&& (this.columnMap.equals(inRow.columnMap))) {
				equals = true;
				for (int i = 0; i < rowData.length; i++) {
					if ((rowData[i] == null) && (inRow.rowData[i] == null)) {
						continue;
					} else if ((rowData[i] == null)
							&& (inRow.rowData[i] != null)) {
						equals = false;
						break;
					} else if ((rowData[i] != null)
							&& (inRow.rowData[i] == null)) {
						equals = false;
						break;
					}

					if (rowData[i].equals(inRow.rowData[i]) == false) {
						equals = false;
						break;
					}
				}
			}
		}

		return equals;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();

		int i = 0;
		for (Object data : rowData) {
			if (buff.length() > 0) {
				buff.append(", ");
			}

			String colName = indexMap.get(i);
			if (colName != null) {
				buff.append(colName + "/");
			}

			if (data != null) {
				buff.append(data);
			} else {
				buff.append("null");
			}

			i++;
		}

		return buff.toString();
	}
	
	public String [] getRowdata(){
		return rowData;
	}
	
	public Map<String, Integer> getColumnMap(){
		return columnMap;
	}
	
	public Map<String, Integer> getDerivedColumnMap(){
		return derivedColumnMap;
	}
	
	public Map<Integer, String> getIndexMap(){
		return indexMap;
	}
}
