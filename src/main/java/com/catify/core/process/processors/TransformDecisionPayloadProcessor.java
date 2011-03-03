package com.catify.core.process.processors;

import org.apache.camel.Exchange;

import com.catify.core.constants.MessageConstants;

public class TransformDecisionPayloadProcessor extends BaseProcessor {

	public void process(Exchange ex) throws Exception {
		
		String nodeId = (String) ex.getIn().getHeader(MessageConstants.TASK_ID);
		String payload = ex.getIn().getBody(String.class);
		String accountId = ex.getIn().getHeader(MessageConstants.ACCOUNT_NAME, String.class);
		
		ex.getOut().setBody(this.createXml(accountId, nodeId, payload));
		
		super.copyHeaders(ex);
	}
	
	private String createXml(String accountId, String nodeId, String payload){
		return String.format("<com.catify.%s.class_%s>\n%s\n</com.catify.%s.class_%s>", accountId, nodeId, payload, accountId, nodeId);
	}

}
