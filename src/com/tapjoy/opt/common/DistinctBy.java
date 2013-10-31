package com.tapjoy.opt.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class DistinctBy {
	private String distinctByColumns[];
	// key row factory
	private RowFactory keyRowFactory;

	private Set<Row> distinctKeySet = new HashSet<Row>();

	public void init(String distinctByColumns[]) {
		this.distinctByColumns = distinctByColumns;
		keyRowFactory = new RowFactory(distinctByColumns);
	}
	
	/**
	 * removing the duplicated rows with the same key values
	 * 
	 * @param row
	 */
	public void proceess(List<Row> rowList) {
		if ((distinctByColumns != null) && (distinctByColumns.length != 0)) {
			
			Iterator<Row> iter = rowList.iterator();
			while (iter.hasNext()) {
				Row row = iter.next();

				// preparing the key row
				Row key = keyRowFactory.newRow();
				
				for(String column:key.getColumnList()) {
					Object data = row.getColumn(column);
					key.setColumn(column, data);					
				}				

				if(distinctKeySet.contains(key)) {
					iter.remove();
				}
				else {
					distinctKeySet.add(key);
				}
			}
		}
	}
}
