package com.catify.core.process.processors;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.MessageConstants;
import com.catify.core.event.impl.beans.PayloadEvent;
import com.catify.core.event.impl.beans.StateEvent;
import com.catify.core.process.xml.XmlProcessBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.sun.tools.javac.util.List;


public class TerminateProcessor extends BaseProcessor {

	static final Logger LOG = LoggerFactory
			.getLogger(TerminateProcessor.class);
	
	public void process(Exchange ex) throws Exception {
		
		Map<String, Object> headers = ex.getIn().getHeaders();
		String instanceId 	= (String) headers.get(MessageConstants.INSTANCE_ID);
		
		// remove corresponding nodes from node-cache
		IMap<String, Object> nodeCacheMap = Hazelcast.getMap("node-cache");
		
		EntryObject e = new PredicateBuilder().getEntryObject();
		Predicate predicate = e.get("instanceId").equal(instanceId);
		Set<Object> filteredNodeCacheSet = (Set<Object>) nodeCacheMap.values(predicate);
		
		System.out.println("instanceID: " + instanceId);
		System.out.println("mapNodeCache.size: " + nodeCacheMap.size());
		
		for (Object object : filteredNodeCacheSet){
			String element = (String) ((StateEvent) object).getNodeId();

			String key = new String(instanceId+ "-" + element);
			System.out.println("mapNodeCache.key: " + key);
			
			nodeCacheMap.remove(key);
		}
		
		System.out.println("mapNodeCache.size: " + nodeCacheMap.size());
		
		
		// remove corresponding payload from payload-cache
		IMap<String, Object> payloadCacheMap = Hazelcast.getMap("payload-cache");
		
		//Show Elements of payloadCache (debug purpose only)
		Set<Object> filteredPayloadCacheSetAll = (Set<Object>) payloadCacheMap.values();
		for (Object object : filteredPayloadCacheSetAll){
			String element = (String) ((PayloadEvent) object).getPayload();
			System.out.println("Payload-cache: " + element);
		}

		//search and delete payload-entries
		EntryObject e2 = new PredicateBuilder().getEntryObject();
		Predicate predicatePayload = e2.get("instanceId").equal(instanceId);
		Set<Object> filteredPayloadCacheSet = (Set<Object>) payloadCacheMap.values(predicatePayload);

		System.out.println("payloadCacheMap.size BEFORE search and delete: " + payloadCacheMap.size());
		
		for (Object object : filteredPayloadCacheSet){
			String element = (String) ((PayloadEvent) object).getInstanceId();			
			payloadCacheMap.remove(element);
		}
		
		System.out.println("payloadCacheMap.size AFTER search and delete: " + payloadCacheMap.size());
		
		
		
		
		
		//copy body and headers
		super.copyBodyAndHeaders(ex);

	}

}
