package com.catify.core.process.processors;

import org.apache.camel.Exchange;

import com.catify.core.constants.CacheConstants;
import com.catify.core.monitor.HazelcastMonitor;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class EmptyCacheProcessor extends BaseProcessor {

	private IMap<Object, Object> processCache;
	private IMap<Object, Object> timerCache;
	private IMap<Object, Object> validationCache;
	private IMap<Object, Object> payloadCache;
	private IMap<Object, Object> correlationRuleCache;
	private IMap<Object, Object> pipelineCache;
	private IMap<Object, Object> correlationCache;
	private IMap<Object, Object> nodeCache;

	public EmptyCacheProcessor() {
		processCache = Hazelcast.getMap(CacheConstants.PROCESS_CACHE);
		timerCache = Hazelcast.getMap(CacheConstants.TIMER_CACHE);
		validationCache = Hazelcast.getMap(CacheConstants.VALIDATION_CACHE);
		payloadCache = Hazelcast.getMap(CacheConstants.PAYLOAD_CACHE);
		pipelineCache = Hazelcast.getMap(CacheConstants.PIPELINE_CACHE);
		correlationRuleCache = Hazelcast.getMap(CacheConstants.CORRELATION_RULE_CACHE);
		correlationCache = Hazelcast.getMap(CacheConstants.CORRELATION_CACHE);
		nodeCache = Hazelcast.getMap(CacheConstants.NODE_CACHE);
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		processCache.clear();
		timerCache.clear();
		validationCache.clear();
		payloadCache.clear();
		pipelineCache.clear();
		correlationCache.clear();
		correlationRuleCache.clear();
		nodeCache.clear();
		
		HazelcastMonitor.listInstances();

	}

}
