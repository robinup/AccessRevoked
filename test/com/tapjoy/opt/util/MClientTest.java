package com.tapjoy.opt.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import net.spy.memcached.MemcachedClient;

import org.junit.Before;
import org.junit.Test;

import com.tapjoy.opt.config.OverallConfig;

public class MClientTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		try {
			MemcachedClient mc = new MemcachedClient(new InetSocketAddress("optbackup", 11211));
			/*
			mc.set("LeiTest", 100, "This is a test test test test test test 123456789012345678901234567890");
			Thread.sleep(10);
			String result = (String)mc.get("LeiTest");
			System.out.println("Leitest -- " + result);
			*/
			
			HashSet <String> ldset = new HashSet<String>();
			for(int i=1; i<4000; i++) {
				ldset.add("Lallalla, LeiTest .....................................................................................     ..... " + i);
			}
			mc.set("LeiTest2", 100, ldset);
			Set <String> ldset2 = (HashSet <String>) mc.get("LeiTest2");
			System.out.println("Leitest -- " + ldset2.size());
			//System.out.println("Leitest -- " + ldset2.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
