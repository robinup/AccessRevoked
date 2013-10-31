package com.tapjoy.opt.common;

import java.util.HashMap;
import java.util.Map;



public class RowFactory {
	private Map<String, Integer> columnMap = new HashMap<String, Integer>();
	private Map<String, Integer> drivedColumnMap = new HashMap<String, Integer>();
	private Map<Integer, String> indexMap = new HashMap<Integer, String>();

	public RowFactory(String[] columns) {
		int index = 0;

		for (String column : columns) {
			indexMap.put(index, column);
			columnMap.put(column, index++);
		}
	}
	
	public RowFactory(String[] columns, String[] derivedColumns) {
		int index = 0;

		for (String column : columns) {
			indexMap.put(index, column);
			columnMap.put(column, index++);
		}

		if(derivedColumns != null) {
			for (String column : derivedColumns) {
				indexMap.put(index, column);
				drivedColumnMap.put(column, index++);
			}			
		}
	}
	
	public RowFactory(Map<String, Integer> columnMap, Map<String, Integer> drivedColumnMap, Map<Integer, String> indexMap){
		this.columnMap = columnMap;
		this.drivedColumnMap = drivedColumnMap;
		this.indexMap = indexMap;
	}
	

	/**
	 * creating the new row
	 * @return
	 */
	public Row newRow() {
		Row row = new Row(columnMap, drivedColumnMap, indexMap);
		
		return row;
	}
}