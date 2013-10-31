package com.tapjoy.opt.model_assigner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.tapjoy.opt.config.OverallConfig;

public class AssignerManager {
	
	private static Logger logger = Logger.getLogger(AssignerManager.class);
	
	public static HashMap<String, String> applist = new HashMap<String, String>();
	
	private static ArrayList<ModelAssigner> allAssigners = null;
	private static ArrayList<String> allAssignerNames = null;
	private static ArrayList<ModelAssigner> allFeaturedAssigners = null;
	private static ArrayList<String> allFeaturedAssignerNames = null;
	
	private static ModelAssigner regAssigner = null;
	private static ModelAssigner regFeaturedAssigner = null;
	
	public static String assignerName = "";
	public static String featuredAssignerName = "";
	
	public static ArrayList<ModelAssigner> getAllRegAssigners()
	{
		if(allAssigners == null)
			allAssigners = new ArrayList<ModelAssigner>();
		return allAssigners;
	}
	
	public static ModelAssigner getRegAssigner()
	{
		if(allAssigners == null || allAssigners.isEmpty())
			 return null;
		return regAssigner;
	}
	
	public static boolean registerAssigner(String regname)
	{
		int ind = 0;
		for(String name: allAssignerNames)
		{
			if(name.equals(regname))
			{
				assignerName = regname;
				regAssigner = allAssigners.get(ind);
				return true;
			}
			ind++;
		}
		return false;
	}
	
	public static ArrayList<ModelAssigner> getFeaturedAllRegAssigners()
	{
		if(allFeaturedAssigners == null)
			allFeaturedAssigners = new ArrayList<ModelAssigner>();
		return allFeaturedAssigners;
	}
	
	public static ModelAssigner getFeaturedRegAssigner()
	{
		if(allFeaturedAssigners == null || allFeaturedAssigners.isEmpty())
			 return null;
		return regFeaturedAssigner;
	}
	
	public static boolean registerFeaturedAssigner(String regname)
	{
		int ind = 0;
		for(String name: allFeaturedAssignerNames)
		{
			if(name.equals(regname))
			{
				featuredAssignerName = regname;
				regFeaturedAssigner = allFeaturedAssigners.get(ind);
				return true;
			}
			ind++;
		}
		return false;
	}
	
	public static void loadAssigner()
	{
		String filename = OverallConfig.OPTSOA_HOME_DIR+"/"+OverallConfig.DYN_CONF_DIR+"/"+OverallConfig.ASSIGNER_REG_FILE;
		File file = new File(filename);
		if(!file.exists())
		{
			System.out.println("Assigner configuration file doesn't exist: "+filename);
			logger.fatal("Assigner configuration file doesn't exist: "+filename);
			System.exit(1);
		}
		BufferedReader br;
		String line = null;
		boolean regflag = false, featureregflag = false;
		
		try {
			br = new BufferedReader(new FileReader(filename));	

			while((line = br.readLine()) != null && line.length() > 1)
			{
				String[] sp = line.split(":");
				Class<?> cls;
				try {
					if(sp.length < 2)
					{
						continue;
					}
					if(sp[1].endsWith("Assigner"))
						cls = Class.forName(sp[0]+"."+sp[1]);
					else
						cls = Class.forName(sp[0]+"."+sp[1]+"Assigner");
					if(sp[1].startsWith("Feature"))
					{		
						if(allFeaturedAssigners == null || allFeaturedAssignerNames == null)
						{
							allFeaturedAssigners = new ArrayList<ModelAssigner>();
							allFeaturedAssignerNames = new ArrayList<String>();
						}
						allFeaturedAssigners.add((ModelAssigner)(cls.getConstructor().newInstance()));
						allFeaturedAssignerNames.add(sp[1]);
						if(!featureregflag)
						{
							regFeaturedAssigner = allFeaturedAssigners.get(0);
							System.out.println("Featured assigner "+allFeaturedAssignerNames.get(0)+" registered.");
							featureregflag = true;
						}
					}
					else
					{	
						if(allAssigners == null || allAssignerNames == null)
						{
							allAssigners = new ArrayList<ModelAssigner>();
							allAssignerNames = new ArrayList<String>();
						}
						allAssigners.add((ModelAssigner)(cls.getConstructor().newInstance()));
						allAssignerNames.add(sp[1]);
						if(!regflag)
						{
							regAssigner = allAssigners.get(0);
							System.out.println("Assigner "+allAssignerNames.get(0)+" registered.");
							regflag = true;
						}
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		System.out.println(allAssigners.size()+" assigners and "+allFeaturedAssigners.size()+" featured assigners are loaded!");
	}
	
	@SuppressWarnings("resource")
	public static boolean preloadAssigner()
	{
		BufferedReader br;
		String line = null;
		
		try {
			br = new BufferedReader(new FileReader(OverallConfig.OPTSOA_HOME_DIR+OverallConfig.DYN_CONF_DIR+"/"+OverallConfig.PUBOPT_APPLIST_FILE));	

			while((line = br.readLine()) != null && line.length() > 1)
			{
				String[] sp = line.split("\\t");
				applist.put(sp[0].trim(), sp[1].trim());
			}
			loadAssigner();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return true;
	}

}