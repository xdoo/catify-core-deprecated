package com.catify.core.process.processors;

import java.util.Iterator;
import java.util.Set;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.event.impl.beans.StateEvent;
import com.catify.core.process.ProcessHelper;
import com.catify.core.process.model.ProcessDefinition;
import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.query.SqlPredicate;

public class InitProcessProcessor extends BaseProcessor {
	
	private IdGenerator idGenerator = Hazelcast.getIdGenerator("ProcessInstanceIdGenerator");
	
	static final Logger LOG = LoggerFactory
			.getLogger(InitProcessProcessor.class);
	
	public void process(Exchange exchange) throws Exception {
		
		super.copyBodyAndHeaders(exchange);
		
//		if there is an instance id set in the header create no new one
		if(!exchange.getIn().getHeaders().containsKey(MessageConstants.INSTANCE_ID)){
			if(LOG.isDebugEnabled()){
				LOG.debug("Nothing to reset - creating new instance id.");
			}
			exchange.getOut().setHeader(MessageConstants.INSTANCE_ID, Long.toString(idGenerator.newId()));
		} else {
			if(LOG.isDebugEnabled()) {
				LOG.debug(String.format("resetting process, because instance id (%s) is set.", exchange.getIn().getHeader(MessageConstants.INSTANCE_ID)));
			}
			this.resetProcess(exchange);
		}
		
		if(LOG.isDebugEnabled()){
			LOG.debug(String.format("Process instance with id '%s' initialized."), exchange.getOut().getHeader(MessageConstants.INSTANCE_ID, String.class));
		}
	}
	
	private void resetProcess(Exchange exchange) {
		IMap<String, StateEvent> nodeCache = Hazelcast.getMap(CacheConstants.NODE_CACHE);
		IMap<String, ProcessDefinition> processCache = Hazelcast.getMap(CacheConstants.PROCESS_CACHE);
		
		String iid = exchange.getIn().getHeader(MessageConstants.INSTANCE_ID, String.class);
		String pn = exchange.getIn().getHeader(MessageConstants.PROCESS_NAME, String.class);
		String pid = exchange.getIn().getHeader(MessageConstants.PROCESS_ID, String.class);
		
		if(LOG.isDebugEnabled()){
			LOG.debug(String.format("Resetting for instance %s of process %s (%s)", iid, pn, pid));
		}
		
		// clean node cache
		Set<String> keySet = nodeCache.keySet(new SqlPredicate(String.format("instanceId=%s AND processName=%s" , iid, pn)));
		
		if(keySet != null){
			
			for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				nodeCache.remove(key);
			}
		}
		
		// reset hit numbers
		ProcessDefinition definition = processCache.get(pid);
		
		if(definition != null) {
			Iterator<String> it = definition.getMergeNodes().iterator();
			while (it.hasNext()) {
				String mergeNodeId = (String) it.next();
				
				// create node instance id
				String mergeNodeTaskInstanceId = ProcessHelper.createTaskInstanceId(iid, mergeNodeId);
				
				// reset awaitet hits
				AtomicNumber atomicNumber = Hazelcast.getAtomicNumber(mergeNodeTaskInstanceId);
				atomicNumber.set(0);
				
				LOG.info(String.format("reset awaited hits for node %s to %s.", mergeNodeId, atomicNumber.get()));
				
			}
			
			if(definition.getMergeNodes().isEmpty()) {
				LOG.info(String.format("No merge node ids found for process %s (%s)", pn, pid));
			}
			
		} else {
			LOG.error(String.format("No process definition found for process id %s!", pid));
		}
	}

}
