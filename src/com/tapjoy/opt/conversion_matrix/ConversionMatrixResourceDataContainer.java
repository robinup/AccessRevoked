package com.tapjoy.opt.conversion_matrix;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.hbase.client.HTableInterface;

import com.tapjoy.opt.resource.ResourceDataContainer;

public class ConversionMatrixResourceDataContainer extends  ResourceDataContainer {
	
	private static ConversionMatrixResourceDataContainer instance = new ConversionMatrixResourceDataContainer();

	@SuppressWarnings("rawtypes")
	public HashMap conversionMatrix = null;
	
	public String htabletoken = null;
	
	public HTableInterface rttable = null;
	
	public ArrayList<HTableInterface> auxtables = null;
	
	private ConversionMatrixResourceDataContainer() {
		this.identity = "ConversionMatrix";
	}
	
	public static ConversionMatrixResourceDataContainer getInstance(){
		return instance;
	}
}
