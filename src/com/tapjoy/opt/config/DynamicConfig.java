package com.tapjoy.opt.config;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.Field;

//import java.nio.channels.FileLock;   

import org.apache.log4j.Logger;

import com.tapjoy.opt.ModelController;

/*
 * @author ljiang
 *
 * TODO -- allow the configurations be dynamically updated from config files
 *         implement change listeners for each dynamic config 
 */

public class DynamicConfig {

	private static Logger logger = Logger.getLogger(DynamicConfig.class);

	public static String configfilename = OverallConfig.OPTSOA_HOME_DIR+"/"+OverallConfig.DYN_CONF_DIR+"/config.dat";

	public static void getClassParams(String classname, BufferedWriter bw)
	{
		try {
			Class<?> c = Class.forName(classname);
			getClassInfo(c, bw, "", true);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void getClassInfo(Class<?> c, BufferedWriter bw, String classprefix, boolean flag) throws IOException
	{
			Package p = c.getPackage();

			if(flag)
			{
				bw.write("Package:\n  "+(p != null ? p.getName() : "-- No Package --")+"\n");
				bw.write("Class:\n "+(c != null ? c.getName(): "-- No Class --")+"\n\n");
				System.out.println("Class:\n "+(c != null ? c.getName(): "-- No Class --")+"\n\n");
			}

			//0.get generic value
			for (Field mbr : c.getFields()) {
					try {	
						String fieldstr = ((Field)mbr).toGenericString(); 
						System.out.format("  %s%n", fieldstr);
						String[] fieldstrsplit = fieldstr.split(" ");
						boolean finalvarflag = false;
						for(String substr: fieldstrsplit)
						{
							if(substr.equals("final"))
							{
								finalvarflag = true;
								break;
							}
						}
						if(finalvarflag)
							continue;

						if(!flag)
							fieldstr = fieldstrsplit[fieldstrsplit.length-1].substring(classprefix.length()+1);
						else
						{
							fieldstr = fieldstrsplit[fieldstrsplit.length-1].substring(c.getName().length()+1);
							classprefix = c.getName();
						}
						//System.out.println(c.getName()+"\n");
						fieldstr = "  "+fieldstr;
						//it asks all configurable parameters to be "public static" and type-compatible, otherwise it goes to NullPointerException and continue next field
						Object value = mbr.get(null);
						//1.get type of the field
						if(value instanceof Integer)
						{
							//2. get type-specific value (switch statement)
							bw.write(fieldstr+" type=int value="+value.toString()+"\n");
						}
						else if(value instanceof Float)
						{
							//2. get type-specific value (switch statement)
							//System.out.printf("%s type=float value=%f\n", fieldstr, (float)value);
							bw.write(fieldstr+" type=float value="+value.toString()+"\n");
						}
						else if(value instanceof Double)
						{
							//2. get type-specific value (switch statement)
							//System.out.printf("%s type=double value=%f\n", fieldstr, (double)value);
							bw.write(fieldstr+" type=double value="+value.toString()+"\n");
						}
						else if(value instanceof String)
						{
							//System.out.printf("%s type=String value=%s\n", fieldstr, (String)value);
							bw.write(fieldstr+" type=String value=\""+value+"\"\n");
						}
						else if(value instanceof Boolean)
						{
							//System.out.printf("%s type=boolean value=%s\n", fieldstr, ((Boolean)value).toString());
							bw.write(fieldstr+" type=boolean value="+value.toString()+"\n");
						}
						else if(value instanceof Integer[])
						{
							//System.out.printf("%s type=int[] value=[", fieldstr);
							bw.write(fieldstr+" type=int[] value=[");
							Integer[] tmpvalue = (Integer[])value;
							for(int i=0; i< tmpvalue.length; i++)
							{
								//System.out.printf("%d", tmpvalue[i]);
								bw.write(tmpvalue[i].toString());
								if(i < tmpvalue.length-1)
									//System.out.printf(",");
									bw.write(",");
								else
									//System.out.printf("]\n");
									bw.write("]\n");
							}
						}
						else if(value instanceof Float[])
						{
							//System.out.printf("%s type=float[] value=[", fieldstr);
							bw.write(fieldstr+" type=float[] value=[");
							Float[] tmpvalue = (Float[])value;
							for(int i=0; i< tmpvalue.length; i++)
							{
								bw.write(tmpvalue[i].toString());
								if(i < tmpvalue.length-1)
									//System.out.printf(",");
									bw.write(",");
								else
									//System.out.printf("]\n");
									bw.write("]\n");
							}
						}
						else if(value instanceof Double[])
						{
							//System.out.printf("%s type=double[] value=[", fieldstr);
							bw.write(fieldstr+" type=double[] value=[");
							Double[] tmpvalue = (Double[])value;
							for(int i=0; i< tmpvalue.length; i++)
							{
								bw.write(tmpvalue[i].toString());
								if(i < tmpvalue.length-1)
									//System.out.printf(",");
									bw.write(",");
								else
									//System.out.printf("]\n");
									bw.write("]\n");
							}
						}
						else if(value instanceof String[])
						{
							//System.out.printf("%s type=String[] value=[", fieldstr);
							bw.write(fieldstr+" type=String[] value=[");
							String[] tmpvalue = (String[])value;
							for(int i=0; i< tmpvalue.length; i++)
							{
								bw.write("\""+tmpvalue[i]+"\"");
								if(i < tmpvalue.length-1)
									//System.out.printf(",");
									bw.write(",");
								else
									//System.out.printf("]\n");
									bw.write("]\n");
							}
						}
						else if(value instanceof Boolean[])
						{
							//System.out.printf("%s type=boolean[] value=[", fieldstr);
							bw.write(fieldstr+" type=boolean[] value=[");
							Boolean[] tmpvalue = (Boolean[])value;
							for(int i=0; i< tmpvalue.length; i++)
							{
								bw.write(tmpvalue[i].toString());
								if(i < tmpvalue.length-1)
									//System.out.printf(",");
									bw.write(",");
								else
									//System.out.printf("]\n");
									bw.write("]\n");
							}
						}

				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				catch (NullPointerException e)
				{
					logger.warn("some fields are not configurable: type incompatible (e.g. HashMap) or not 'public static'.");
					continue;
				}
			}

			for (Class<?> mbr : c.getDeclaredClasses())
			{
				getClassInfo(mbr, bw, classprefix, false);
			}

			if(flag)
				bw.write("\n");
	}

	//TBD -- add file lock during config file read for updating 08/06
	public static boolean updateParamsFromConfigFile()
	{
		File file = new File(configfilename);
		if(!file.exists()) 
		{
			return false;
		}

		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(configfilename));
			boolean curclassflag = false;
			boolean curpackageflag = false;
			Class<?> curclass = null;
			String upclassstr = "";
			String subclass = "";
			Package curp = null;
			while((line = br.readLine()) != null)
			{
				line = line.trim();
				if(line.length() <= 1)
					continue;
				if(curclassflag)
				{
					curclass = Class.forName(line);
					upclassstr = line;
					subclass = line;
					if(curp != null && !curp.equals(curclass.getPackage()))
					{
						logger.warn("Illegal format for configuration file: class doesn't match the corresponding package! Updating aborted.\n");
						System.out.printf("%s %s\n", line, curp.getName());
						return true;
					}
					curclassflag = false;
					continue;
				}
				if(line.startsWith("Class"))
				{
					curclassflag = true;
					continue;
				}
				if(curpackageflag)
				{
					curp = Package.getPackage(line);
					curpackageflag = false;
					continue;
				}
				if(line.startsWith("Package"))
				{
					curpackageflag = true;
					continue;
				}

				String[] split = line.split(" ", 3);
				String varname = split[0];
				String typename = split[1].split("=",2)[1];
				String valuestr = split[2].split("=",2)[1];

				if(varname.contains("."))
				{
					 subclass = upclassstr;
					 if(varname.contains("$"))
					 {
						 String[] subclassnames = varname.split("$");
						 for(int i=0; i< subclassnames.length-1; i++)
							 subclass += "$"+subclassnames[i];
						 varname = subclassnames[subclassnames.length-1];
					 }
					 String[] lastclassstr = varname.split("\\.");
					 subclass += "$"+lastclassstr[0];
					 try
					 {
					 curclass = Class.forName(subclass);
					 }
					 catch (ClassNotFoundException e)
					 {
						 System.out.printf("%s\n", subclass);
					 }
					 varname = lastclassstr[1];
				}
				//get the field and then do type conversion
				//support 10 types for now (int, float, double, String, boolean and corresponding []s)
				try {
					if(typename.equals("int"))
					{
							Field mbr = curclass.getDeclaredField(varname);
							int value = 0;
							try
							{
							  value = mbr.getInt(null);
							  int newvalue = Integer.parseInt(valuestr);
							  if(newvalue != value)
							  {
									mbr.setInt(null, newvalue);
									System.out.printf("%s set to %s\n", subclass+"."+mbr.getName(), valuestr);
							  }
							}
							catch (IllegalArgumentException e)
							{
								Object oldvalue = mbr.get(null);
								Integer newvalue = Integer.parseInt(valuestr);
								if(newvalue.intValue() != ((Integer)oldvalue).intValue())
								{
									mbr.set(null, newvalue);
									System.out.printf("%s set to %s\n", subclass+"."+mbr.getName(), valuestr);
								}
							}

					}
					else if(typename.equals("float"))
					{
						Field mbr = curclass.getDeclaredField(varname);
						Object value = mbr.get(null);
						float newvalue = Float.parseFloat(valuestr);
						if(Math.abs(((Float)value).floatValue() - newvalue) > 1e-6)
						{
							mbr.setFloat(null, newvalue);
							System.out.printf("%s set to %s\n", subclass+"."+mbr.getName(), valuestr);
						}
					}
					else if(typename.equals("double"))
					{
						Field mbr = curclass.getDeclaredField(varname);
						Object value = mbr.get(null);
						double newvalue = Double.parseDouble(valuestr);
						if(Math.abs(((Double)value).doubleValue() - newvalue) > 1e-6)
						{
							mbr.setDouble(null, newvalue);
							System.out.printf("%s set to %s\n", subclass+"."+mbr.getName(), valuestr);
						}
					}
					else if(typename.equals("String"))
					{
						Field mbr = curclass.getDeclaredField(varname);
						Object value = mbr.get(null);
						valuestr = valuestr.substring(1,valuestr.length()-1);
						if(!((String)value).equals(valuestr))
						{
							mbr.set(null, valuestr);
							System.out.printf("%s set to %s\n", subclass+"."+mbr.getName(), valuestr);
						}
					}
					else if(typename.equals("boolean"))
					{
						Field mbr = curclass.getDeclaredField(varname);
						Object value = mbr.get(null);
						boolean newvalue = Boolean.parseBoolean(valuestr);
						if(!((Boolean)value).equals(newvalue))
						{
							mbr.setBoolean(null, newvalue);
							System.out.printf("%s set to %s\n", subclass+"."+mbr.getName(), valuestr);
						}
					}
					else if(typename.equals("int[]"))
					{
						Field mbr = curclass.getDeclaredField(varname);
						Object value = mbr.get(null);
						valuestr = valuestr.substring(valuestr.indexOf('[')+1, valuestr.indexOf(']'));
						String[] valuestrs = valuestr.split(","); 
						int[] newvalue = new int[valuestrs.length];
						for(int i=0; i< valuestrs.length; i++)
						{
							newvalue[i] = Integer.parseInt(valuestrs[i].trim());
						}
						if(valuestrs.length != ((int[])value).length)
						{
							mbr.set(null, newvalue);
							System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
						}
						else
						{
						for(int i=0; i< valuestrs.length; i++)
						{
							if(newvalue[i] != ((int[])value)[i])
							{
								mbr.set(null, newvalue);
								System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
								break;
							}
						}
						}
					}
					else if(typename.equals("float[]"))
					{
						Field mbr = curclass.getDeclaredField(varname);
						Object value = mbr.get(null);
						valuestr = valuestr.substring(valuestr.indexOf('[')+1, valuestr.indexOf(']'));
						String[] valuestrs = valuestr.split(","); 
						float[] newvalue = new float[valuestrs.length];
						for(int i=0; i< valuestrs.length; i++)
						{
							newvalue[i] = Float.parseFloat(valuestrs[i].trim());
						}
						if(valuestrs.length != ((float[])value).length)
						{
							mbr.set(null, newvalue);
							System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
							continue;
						}
						else
						{
						for(int i=0; i< valuestrs.length; i++)
						{
							if(Math.abs(newvalue[i] - ((float[])value)[i]) > 1e-6)
							{
								mbr.set(null, newvalue);
								System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
								break;
							}
						}
						}
					}
					else if(typename.equals("double[]"))
					{
						Field mbr = curclass.getDeclaredField(varname);
						Object value = mbr.get(null);
						valuestr = valuestr.substring(valuestr.indexOf('[')+1, valuestr.indexOf(']'));
						String[] valuestrs = valuestr.split(","); 
						double[] newvalue = new double[valuestrs.length];
						for(int i=0; i< valuestrs.length; i++)
						{
							newvalue[i] = Double.parseDouble(valuestrs[i].trim());
						}
						if(valuestrs.length != ((float[])value).length)
						{
							mbr.set(null, newvalue);
							System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
						}
						else	
						{
						for(int i=0; i< valuestrs.length; i++)
						{
							if(Math.abs(newvalue[i] - ((float[])value)[i]) > 1e-6)
							{
								mbr.set(null, newvalue);
								System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
								break;
							}
						}
						}
					}
					else if(typename.equals("String[]"))
					{
						Field mbr = curclass.getDeclaredField(varname);
						Object value = mbr.get(null);
						valuestr = valuestr.substring(valuestr.indexOf('[')+1, valuestr.indexOf(']'));
						String[] newvalue = valuestr.split(","); 
						for(int i=0; i< newvalue.length; i++)
						{
							newvalue[i] = newvalue[i].trim();
							newvalue[i] = newvalue[i].substring(1, newvalue[i].length()-1);
						}
						if(newvalue.length != ((String[])value).length)
						{
							mbr.set(null, newvalue);
							System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
						}
						else
						{
						for(int i=0; i< newvalue.length; i++)
						{
							if(!newvalue[i].equals(((String[])value)[i]))
							{
								mbr.set(null, newvalue);
								System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
								break;
							}
						}
						}
					}
					else if(typename.equals("boolean[]"))
					{
						Field mbr = curclass.getDeclaredField(varname);
						Object value = mbr.get(null);
						valuestr = valuestr.substring(valuestr.indexOf('[')+1, valuestr.indexOf(']'));
						String[] valuestrs = valuestr.split(","); 
						boolean[] newvalue = new boolean[valuestrs.length];
						for(int i=0; i< valuestrs.length; i++)
						{
							newvalue[i] = Boolean.parseBoolean(valuestrs[i].trim().toLowerCase());
						}
						if(valuestrs.length != ((boolean[])value).length)
						{
							mbr.set(null, newvalue);
							System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
						}
						else
						{
						for(int i=0; i< valuestrs.length; i++)
						{
							if(newvalue[i] != ((boolean[])value)[i])
							{
								mbr.set(null, newvalue);
								System.out.printf("%s set to [%s]\n", subclass+"."+mbr.getName(), valuestr);
								break;
							}
						}
						}
					}
				} catch (NoSuchFieldException e) {
					System.out.printf("field not found: %s %s %s %s\n", upclassstr, varname, typename, valuestr);
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					logger.warn("Configuration file not in a right format. But ok, let's move on to next line.");
				}

				curclass = Class.forName(upclassstr);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch(ArrayIndexOutOfBoundsException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		return true;
	}

	public static void writeConfigFile() throws IOException
	{
		boolean filedir = (new File(OverallConfig.OPTSOA_HOME_DIR+"/"+OverallConfig.DYN_CONF_DIR)).mkdirs();
		/*if(!filedir)
		{
			System.out.println("Configuration directory can't be created at "+OverallConfig.OPTSOA_HOME_DIR+"/"+OverallConfig.DYN_CONF_DIR);
			logger.fatal("Configuration directory can't be created at "+ OverallConfig.OPTSOA_HOME_DIR+"/"+OverallConfig.DYN_CONF_DIR);
			System.exit(1);
		}*/
		File file = new File(configfilename);
		if(file.exists()) //each launch of OptSOA, we have a new config file
		{
			file.delete();
		}

		file.createNewFile();
		
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);	
		//below are all the Java packages for configuration now
		getClassParams("com.tapjoy.opt.config.OverallConfig", bw);
		for(String modelclass: ModelController.getAllRegNames())
		{
			getClassParams(modelclass+".config.Configuration", bw);
		}
		getClassParams("com.tapjoy.opt.model_assigner.AssignerManager", bw);
		//getClassParams("com.tapjoy.opt.linear_regression.config.Configuration", bw);
		//getClassParams("com.tapjoy.opt.conversion_matrix.config.Configuration", bw);
		getClassParams("com.tapjoy.opt.vertica_score.config.Configuration", bw);
		getClassParams("com.tapjoy.opt.vertica_score.config.ConfigurationApp", bw);
		getClassParams("com.tapjoy.opt.vertica_score.config.ConfigurationAudition", bw);
		getClassParams("com.tapjoy.opt.vertica_score.config.ConfigurationCountries", bw);
		getClassParams("com.tapjoy.opt.vertica_score.config.ConfigurationHardPromotion", bw);
		getClassParams("com.tapjoy.opt.vertica_score.config.ConfigurationLookback", bw);
		getClassParams("com.tapjoy.opt.vertica_score.config.ConfigurationSegment", bw);
		getClassParams("com.tapjoy.opt.vertica_score.config.ConfigurationTargetApp", bw);
		getClassParams("com.tapjoy.opt.vertica_score.config.ConfigurationTJMCountries", bw);
		bw.close();
	}

	//the WatchDir class is quoted from an Oracle java tutorial
	//http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
	public static class WatchDir {

	    private final WatchService watcher;
	    private final Map<WatchKey,Path> keys;
	    private final boolean recursive;
	    private boolean trace = false;

	    @SuppressWarnings("unchecked")
	    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
	        return (WatchEvent<T>)event;
	    }

	    /**
	     * Register the given directory with the WatchService
	     */
	    private void register(Path dir) throws IOException {
	        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
	        if (trace) {
	            Path prev = keys.get(key);
	            if (prev == null) {
	                System.out.format("register: %s\n", dir);
	            } else {
	                if (!dir.equals(prev)) {
	                    System.out.format("update: %s -> %s\n", prev, dir);
	                }
	            }
	        }
	        keys.put(key, dir);
	    }

	    /**
	     * Register the given directory, and all its sub-directories, with the
	     * WatchService.
	     */
	    private void registerAll(final Path start) throws IOException {
	        // register directory and sub-directories
	        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
	            @Override
	            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
	                throws IOException
	            {
	                register(dir);
	                return FileVisitResult.CONTINUE;
	            }
	        });
	    }

	    /**
	     * Creates a WatchService and registers the given directory
	     */
	    WatchDir(Path dir, boolean recursive) throws IOException {
	        this.watcher = FileSystems.getDefault().newWatchService();
	        this.keys = new HashMap<WatchKey,Path>();
	        this.recursive = recursive;

	        if (recursive) {
	            System.out.format("Scanning %s ...\n", dir);
	            registerAll(dir);
	            System.out.println("Done.");
	        } else {
	            register(dir);
	        }

	        // enable trace after initial registration
	        this.trace = true;
	    }

	    /**
	     * Process all events for keys queued to the watcher
	     */
	    @SuppressWarnings("rawtypes")
		void processEvents() {
	    	String bakconfigname = "";
	    	boolean lastflag = false;
	    	for(int i=0; i< configfilename.length(); i++)
	    	{
	    		char tmpchar = configfilename.charAt(i);
	    		if(!lastflag || tmpchar != '/')
	    			bakconfigname += tmpchar;
	    		if(tmpchar == '/')
	    			lastflag = true;
	    		else
	    			lastflag = false;
	    	}
	    	configfilename = bakconfigname;
	    	
	        for (;;) {

	            // wait for key to be signalled
	            WatchKey key;
	            try {
	                key = watcher.take();
	            } catch (InterruptedException x) {
	                return;
	            }

	            Path dir = keys.get(key);
	            if (dir == null) {
	                System.err.println("WatchKey not recognized!!");
	                continue;
	            }

	            for (WatchEvent<?> event: key.pollEvents()) {
	                WatchEvent.Kind kind = event.kind();

	                // TBD - provide example of how OVERFLOW event is handled
	                if (kind == OVERFLOW) {
	                    continue;
	                }

	                // Context for directory entry event is the file name of entry
	                WatchEvent<Path> ev = cast(event);
	                Path name = ev.context();
	                Path child = dir.resolve(name);

	                // print out event
	                System.out.format("%s: %s\n", event.kind().name(), child);

	                // if directory is created, and watching recursively, then
	                // register it and its sub-directories
	                if (recursive && (kind == ENTRY_CREATE)) {
	                    try {
	                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
	                            registerAll(child);
	                        }
	                    } catch (IOException x) {
	                        // ignore to keep sample readbale
	                    }
	                }
	                if(!event.kind().name().equals("ENTRY_DELETE") && child.toString().equals(configfilename))
	                {
	                	if(updateParamsFromConfigFile() == false)
	                	{
	                		System.out.println("Auto configuration file doesn't exist at: "+configfilename);
	                		logger.info("Auto configuration file doesn't exist at: "+configfilename);
	                	}
	                }
	            }

	            // reset key and remove from set if directory no longer accessible
	            boolean valid = key.reset();
	            if (!valid) {
	                keys.remove(key);
	                // all directories are inaccessible
	                if (keys.isEmpty()) {
	                    break;
	                }
	            }
	        }
	    }

	}

	public static void initDynamicConfig() throws IOException
	{
		writeConfigFile();
		Path dir = Paths.get(OverallConfig.OPTSOA_HOME_DIR+"/"+OverallConfig.DYN_CONF_DIR);
		new WatchDir(dir, false).processEvents();
	}
	
	public static void main(String[] args) throws IOException
	{
		initDynamicConfig();
	}
	
	
}