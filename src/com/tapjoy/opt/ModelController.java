package com.tapjoy.opt;

import java.util.ArrayList;
import java.util.HashMap;

import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.resource.ResourceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

public class ModelController {
	
	private static Logger logger = Logger.getLogger(ModelController.class);
	
	private static ArrayList<ResourceManager> allRegModels = null;
	private static HashMap<String, Integer> allRegMap = null;
	private static ArrayList<String> allRegNames = null;
	private static int modelIndex = 0;
	
	public static ArrayList<ResourceManager> getAllRegModels()
	{
		if(allRegModels == null)
			allRegModels = new ArrayList<ResourceManager>();
		return allRegModels;
	}
	
	public static ResourceManager getRegModel(int order)
	{
		if(allRegModels == null || allRegModels.isEmpty())
			 return null;
		return allRegModels.get(order);
	}
	
	public static boolean checkAlgoId(String id)
	{
		if(allRegMap == null || id == null)
			 return false;
		return allRegMap.containsKey(id);
	}
	
	public static int getAlgoIndex(String id)
	{
		if(!checkAlgoId(id))
			return -1;
		else
			return allRegMap.get(id);
	}
	
	public static ArrayList<String> getAllRegNames()
	{
		return allRegNames;
	}
	
	public static String getRegName(int ind)
	{
		return allRegNames.get(ind);
	}
	
	public static void exeModelConigFile(String filename, CommandLine cmd)
	{
		File file = new File(filename);
		if(!file.exists())
		{
			System.out.println("Model configuration file doesn't exist: "+OverallConfig.OPTSOA_HOME_DIR+"/"+OverallConfig.DYN_CONF_DIR+"/"+OverallConfig.MODEL_REG_FILE);
			logger.fatal("Model configuration file doesn't exist: "+OverallConfig.OPTSOA_HOME_DIR+"/"+OverallConfig.DYN_CONF_DIR+"/"+OverallConfig.MODEL_REG_FILE);
			System.exit(1);
		}
		
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			
			while((line = br.readLine()) != null && line.length() > 1)
			{
				String[] sp = line.split(":");
				Class<?> cls;
				try {
					Class<?> configcls = Class.forName(sp[0]+".config.Configuration");
					String idstr = new String();
					if(sp.length < 2)
					{
						idstr = configcls.getDeclaredField("IDKEY").toString();
					}
					else
					{
						idstr = sp[1];
					}
					cls = Class.forName(sp[0]+"."+idstr+"ResourceManager");
					allRegModels.add((ResourceManager) cls.newInstance());
					Field aid = configcls.getDeclaredField("ALGO_ID");
					allRegMap.put(aid.get(null).toString(), modelIndex);
					allRegNames.add(sp[0]);
					modelIndex++;
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public static void startAllReg(CommandLine cmd)
	{   
		   allRegModels = new ArrayList<ResourceManager>();
		   allRegMap = new HashMap<String, Integer>();
		   allRegNames = new ArrayList<String>();
		   
		   exeModelConigFile(OverallConfig.OPTSOA_HOME_DIR+"/"+OverallConfig.DYN_CONF_DIR+"/"+ OverallConfig.MODEL_REG_FILE, cmd);
		   
		   /*AnnotationRunner runner = new AnnotationRunner();
		   Method[] methods = runner.getClass().getMethods();
		   System.out.printf("how many methods? %d \n", methods.length);
		   
		   for (Method method : methods)
		   {
			   Reg annos = method.getAnnotation(Reg.class);
	            if (annos != null) {
	                try {	
	                    allRegModels.add((ResourceManager)(method.invoke(runner, cmd)));
	                    System.out.printf("method invoked! %s\n", method.getName());
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
		   }*/
		   System.out.printf("size of allRegModels = %d\n", allRegModels.size());
	   }
	   
	   public static void stopAll()
	   {
		   
	   }
	   
	   
	   public static boolean registerModel(String algoid)
	   {
		  return true;
	   }
	   
	   public static boolean unregisterModel(int mid)
	   {
		   return true;
	   }
}
