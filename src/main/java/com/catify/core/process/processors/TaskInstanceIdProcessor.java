package com.catify.core.process.processors;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.MessageConstants;

public class TaskInstanceIdProcessor extends BaseProcessor {

	static final Logger LOG = LoggerFactory
			.getLogger(TaskInstanceIdProcessor.class);
	
	public void process(Exchange ex) throws Exception {
		
		//get headers
		Map<String, Object> headers = ex.getIn().getHeaders();
		String instanceId 	= (String) headers.get(MessageConstants.INSTANCE_ID);
		String taskId 		= (String) headers.get(MessageConstants.TASK_ID);
		
		//copy body and headers
		super.copyBodyAndHeaders(ex);
		
		//create task instance id
		ex.getOut().setHeader(MessageConstants.TASK_INSTANCE_ID, DigestUtils.md5Hex(String.format("%s%s", instanceId, taskId)));
		
		
		if(LOG.isDebugEnabled()){
			LOG.debug(String.format("Processing instance '%s' on task '%s' --> task instance id: %s", instanceId, taskId, ex.getOut().getHeader(MessageConstants.TASK_INSTANCE_ID, String.class)));	
		}
	}

}
