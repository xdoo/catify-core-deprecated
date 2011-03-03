package com.catify.core.process.processors;

import org.apache.camel.Exchange;

import com.catify.core.constants.CacheConstants;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.xml.XmlProcessBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class ProcessRegistrationProcessor extends BaseProcessor {

	private XmlProcessBuilder builder;
	private IMap<String, Object> cache;

	public ProcessRegistrationProcessor(){
		this.builder = new XmlProcessBuilder();
		this.cache = Hazelcast.getMap(CacheConstants.PROCESS_CACHE);
	}
	
	public void process(Exchange ex) throws Exception {
		
		ProcessDefinition definition = builder.build(ex.getIn().getBody(com.catify.core.process.xml.model.Process.class));
		
		//put definition into cache
		this.cache.put(definition.getProcessId(), definition);
		
		super.copyBodyAndHeaders(ex);
		
	}

}
