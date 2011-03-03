package com.catify.core.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

public class LinkedMapTest extends TestCase {

	public void testLinkedMap(){
		MultiMap map = new MultiValueMap();
		map.put("foo", "bar1");
		map.put("foo", "bar2");
		
		List<String> result = (List<String>) map.get("foo");
		
		assertTrue(result.contains("bar1"));
		assertTrue(result.contains("bar2"));
	}
	
	public void testBuilder(){
		MyBuilder builder = new MyBuilder();
		
		builder.add("foo").add("bar").add("foobar");
		
		assertTrue(builder.before.equals("foobar"));
		System.out.println(builder.cache);
	}
	
	public void testIp() throws UnknownHostException{
		System.out.println(InetAddress.getLocalHost().getHostAddress());
		System.out.println(InetAddress.getLocalHost().getCanonicalHostName());
		System.out.println(InetAddress.getLocalHost().getLocalHost());
		System.out.println(InetAddress.getLocalHost().getByName("m171"));
	}
	
}
