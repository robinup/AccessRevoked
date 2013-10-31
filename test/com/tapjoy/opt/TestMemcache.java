package com.tapjoy.opt;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import net.spy.memcached.MemcachedClient;

import org.junit.Before;
import org.junit.Test;

import com.tapjoy.opt.config.OverallConfig;

public class TestMemcache {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		// Remember to set up a local Memcache instance before running this test.
		return ;
		
		/*
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("key1", "val1");
		map.put("key2",  "val2");
		
		try {
			MemcachedClient mcClient = new MemcachedClient(new InetSocketAddress(OverallConfig.MEMCACHE_ADDR, OverallConfig.MEMCACHE_PORT));
			mcClient.set("LeiTest",  1000, map);
			HashMap<String, String> map2 = (HashMap<String, String>)mcClient.get("LeiTest");
			assertTrue( map2.size() == 2);
			assertTrue("val2".equals(map2.get("key2")));
			mcClient.shutdown();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

}
