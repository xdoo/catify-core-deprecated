package com.catify.core.camel;

import org.apache.camel.test.CamelTestSupport;

import com.hazelcast.core.Hazelcast;

public class ClusterTest extends CamelTestSupport {

	public void testCache(){
		Hazelcast.getMap("foo").put("4714", "hello world");
	}
	
}
