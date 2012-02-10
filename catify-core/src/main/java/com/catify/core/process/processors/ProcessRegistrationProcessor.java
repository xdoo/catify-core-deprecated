package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.CacheConstants;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.xml.XmlProcessBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class ProcessRegistrationProcessor extends BaseProcessor {

	private XmlProcessBuilder builder;
	private IMap<String, Object> cache;

	static final Logger LOG = LoggerFactory
			.getLogger(ProcessRegistrationProcessor.class);
	
	public ProcessRegistrationProcessor(XmlProcessBuilder processBuilder){
		this.builder = processBuilder;
		this.cache = Hazelcast.getMap(CacheConstants.PROCESS_CACHE);
	}
	
	public void process(Exchange ex) throws Exception {
		
		ProcessDefinition definition = builder.build(ex.getIn().getBody(com.catify.core.process.xml.model.Process.class));
		
		LOG.info(String.format("process definition created --> name = %s | version = %s | account = %s", definition.getProcessName(), definition.getProcessVersion(), definition.getAccountName()));
		
		//put definition into cache
		this.cache.put(definition.getProcessId(), definition);
		
		if(this.cache.containsKey(definition.getProcessId())){
			LOG.info(String.format("process %s added to process cache --> name = %s | version = %s | account = %s", definition.getProcessId(), definition.getProcessName(), definition.getProcessVersion(), definition.getAccountName()));
		} else {
			LOG.error(String.format("Could not add process definition with id %s to process cache.", definition.getProcessId()));
		}
		
		super.copyBodyAndHeaders(ex);
		
	}

}
