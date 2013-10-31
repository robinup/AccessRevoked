package com.tapjoy.opt.object_cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.sql.Token;

public class TokenCache {
	private static TokenCache instance = new TokenCache();
	
	private static Logger logger = Logger.getLogger(TokenCache.class);
	
	private static volatile ArrayList<Integer> tokens = null; 
	
	public static TokenCache getInstance() {
		return instance;
	}

	private TokenCache() {
	}
	
	public static class TokenHolder {
		public static ArrayList<Integer> tokens = new ArrayList<Integer>();
		
		public TokenHolder()
		{
			try {
				tokens.add(TokenCache.getTokenForTableKey("conversion_hisotry"));  //that is a typo in vertica, so... 10-30
				tokens.add(TokenCache.getTokenForTableKey("user_big_table"));
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public static void refresh()
		{
			if(tokens == null)
				tokens = new ArrayList<Integer>();
			try {		
				if(tokens.isEmpty())
				{
					tokens.add(TokenCache.getTokenForTableKey("conversion_history")); 
					tokens.add(TokenCache.getTokenForTableKey("user_big_table"));
					return;
				}
				tokens.set(0, TokenCache.getTokenForTableKey("conversion_history"));
				tokens.set(1, TokenCache.getTokenForTableKey("user_big_table"));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static int getToken(int num)
	{
		tokens = TokenHolder.tokens;
		if(tokens.size() < num+1)
			return -1;
		else
			return tokens.get(num);
	}
	
	public static void setToken()
	{	
		TokenHolder.refresh();
		tokens = TokenHolder.tokens;		
	}
	
    public static class VerticaGetCallable implements Callable<String>
    {
    	public VerticaGetCallable(Connection conn, String tablename, CountDownLatch doneSignal)
    	{
    		this.conn = conn;
    		this.tablename = tablename;
    		this.doneSignal = doneSignal;
    	}
    	
    	private Connection conn;
    	private String tablename;
    	private CountDownLatch doneSignal;

		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			String res = Token.getToken(conn, tablename);
			doneSignal.countDown();
			return res;
		}
    	
    }
    
    public static int getTokenForTableKey(String tablename) throws ClassNotFoundException, SQLException
    {
    	Connection conn = VerticaConn.getConnectionNew("verticaprod", "analytics");
    	Connection conn2 = VerticaConn.getConnectionNew("verticaprod2", "analytics");
    	
    	ExecutorService executor = Executors.newFixedThreadPool(2);
    	List<Future<String>> resultlist = new ArrayList<Future<String>>();
    	
    	CountDownLatch doneSignal = new CountDownLatch(2);
    	
    	
    	Callable<String> worker = new VerticaGetCallable(conn, tablename, doneSignal);
    	Future<String> submit = executor.submit(worker);
    	resultlist.add(submit);
    	
    	worker = new VerticaGetCallable(conn2, tablename, doneSignal);
    	Future<String> submit2 = executor.submit(worker);
    	resultlist.add(submit2);
    	
    	int maxtoken = Integer.MIN_VALUE;
    	
    	try {
			doneSignal.await(15, TimeUnit.SECONDS);
			for(Future<String> future: resultlist)
	    	{
				String res = null;
				if( (res = future.get(25, TimeUnit.MILLISECONDS)) != null && !res.isEmpty())
				{
					
					int tmpv = Integer.parseInt(res);
					if(tmpv > maxtoken)
						maxtoken = tmpv;
				}
	    	}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	finally
    	{
    		executor.shutdown();
    	}
    	return maxtoken;
    }
	
}
