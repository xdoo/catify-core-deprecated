package com.catify.core.process.xml;

import java.util.Iterator;

import org.apache.camel.component.hazelcast.HazelcastConstants;

import com.catify.core.configuration.GlobalConfiguration;
import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.GlobalConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.xml.model.Endpoint;
import com.catify.core.process.xml.model.FileEndpoint;
import com.catify.core.process.xml.model.FtpEndpoint;
import com.catify.core.process.xml.model.GenericEndpoint;
import com.catify.core.process.xml.model.HazelcastEndpoint;
import com.catify.core.process.xml.model.InPipeline;
import com.catify.core.process.xml.model.OutPipeline;
import com.catify.core.process.xml.model.RestEndpoint;

public class XmlPipelineBuilder {

	private GlobalConfiguration config;
	private CorrelationRuleBuilder correlationRuleBuilder;
	
	public XmlPipelineBuilder(GlobalConfiguration config, CorrelationRuleBuilder correlationRuleBuilder){
		this.config = config;
		this.correlationRuleBuilder = correlationRuleBuilder;
	}
	
	/**
	 * builds a start pipeline
	 * 
	 * 
	 * @param in
	 * @param definition
	 * @return
	 */
	public String buildStartPipeline(InPipeline in, ProcessDefinition definition){
		
		StringBuilder builder = new StringBuilder();
		
		//open xml route
		openRoutes(builder);
		
		Iterator<Endpoint> it = in.getFromEndpoint().getEndpoints().iterator();
		
		boolean correlation = Boolean.FALSE;
		
		while (it.hasNext()) {
			String tab = "\t\t";
			
			Endpoint endpoint = (Endpoint) it.next();
			
			//set route id and from endpoint
			appendFromEndpoint(builder, endpoint, definition.getProcessId(), definition);
			
			//set headers
			this.appendHeaders(builder, definition);
			
			//check if there is a marshaler needed (e.g. from EDI to XML)
			if(in.getMarshaller() != null){
				this.appendMarshaler(builder, in.getMarshaller().getType());
			}
					
			//do the xslt transformation
			this.appendTransformation(builder, definition.getProcessId());
			
			//check if the message has to splitted
			if(in.getSplit() != null){
				tab = "\t\t\t";
				this.appendSplitStart(builder, in.getSplit().getXpath());
			}
			
			//generate instance id
			builder.append(String.format("%s<process ref=\"initProcessProcessor\"/>\n", tab));

			this.appendMulticastStart(builder);
			
			//check if a correlation is needed
			if(in.getCorrelation() != null){
				correlation = Boolean.TRUE;
				
				//create correlation rule file
				definition.addCorrelationRule(definition.getProcessId(), this.correlationRuleBuilder.buildCorrelationDefinition(in.getCorrelation().getXpath()));
				
				this.appendCreateCorrelation(builder, definition.getProcessId());
			}
			
			//store payload in cache
			this.appendSavePayload(builder, definition.getProcessId());
			
			//send message to queue
			this.appendToQueue(builder, definition.getProcessId());
			
			this.appendMulticastEnd(builder);
			
			//check if there is a splitter
			if(in.getSplit() != null){
				builder.append("\t\t</split>\n");
			}
			
			//end route
			this.appendEndRoute(builder);
		}
		
		appendSavePayloadRoute(builder, definition.getProcessId());
		appendToQueueRoute(builder, definition.getProcessId());
		
		if(correlation){
			appendCreateCorrelationRoute(builder, definition.getProcessId());
		}
		
		closeRoutes(builder);
		
		return builder.toString();
	}
	
	public String buildInPipeline(InPipeline in, String nodeId, ProcessDefinition definition){
		
		StringBuilder builder = new StringBuilder();
		
		//open xml route
		openRoutes(builder);
		
		Iterator<Endpoint> it = in.getFromEndpoint().getEndpoints().iterator();
		
		while (it.hasNext()) {
			String tab = "\t\t";
			
			Endpoint endpoint = (Endpoint) it.next();
			
			//set route id and from endpoint
			appendFromEndpoint(builder, endpoint, nodeId, definition);
			
			//set headers
			this.appendHeaders(builder, definition);
			this.appendConstantHeader(builder, MessageConstants.TASK_ID, nodeId);
			
			//check if there is a marshaler needed
			if(in.getMarshaller() != null){
				this.appendMarshaler(builder, in.getMarshaller().getType());
			}
			
			//do the xslt transformation
			this.appendTransformation(builder, nodeId);
			
			//check if the message has to splitted
			if(in.getSplit() != null){
				tab = "\t\t\t";
				this.appendSplitStart(builder, in.getSplit().getXpath());
			}
			
			//get the instance id
			this.appendGetCorrelation(builder, nodeId);
			
			//create correlation rule
			if(in.getCorrelation() != null) {
				if(in.getCorrelation().getXpath() != null){
					definition.addCorrelationRule(nodeId, this.correlationRuleBuilder.buildCorrelationDefinition(in.getCorrelation().getXpath()));
				}
			}
			
			//open multicast
			this.appendMulticastStart(builder);
			
			//save payload
			this.appendSavePayload(builder, nodeId);
			
			//send message to queue
			this.appendToQueue(builder, nodeId);
			
			//close multicast
			this.appendMulticastEnd(builder);
			
			//check if there is a splitter
			if(in.getSplit() != null){
				builder.append("\t\t</split>\n");
			}
			
			//end route
			this.appendEndRoute(builder);
		}
		
		this.appendSavePayloadRoute(builder, nodeId);
		this.appendToQueueRoute(builder, nodeId);
		this.appendGetCorrelationRoute(builder, nodeId);
		
		closeRoutes(builder);
		
		return builder.toString();
	}
	
	public String buildOutPipeline(OutPipeline out, String nodeId){
		
		//++++++++++++++++++++++++++++
		// TODO --> correlation
		//++++++++++++++++++++++++++++
		
		
		StringBuilder builder = new StringBuilder();
		
		//open xml route
		openRoutes(builder);
		
		builder.append(String.format("\t<route id=\"out-pipeline-%s\">\n", nodeId));
		
		//get message from queue
		this.appendGetMessageFromQueue(builder, nodeId);
		
		//get payload from cache
		this.appendLoadPayload(builder, nodeId);
		
		//transform payload
		this.appendTransformation(builder, nodeId);
		
		//if necessary unmarshal payload
		if(out.getMarshaller() != null){
			
		}
		
		//send payload to endpoint
		if(out.getToEndpoint().getFile() != null){
			this.appendToFile(builder, out.getToEndpoint().getFile().getUri());
		}
		
		if(out.getToEndpoint().getFtp() != null){
			this.appendToFtp(builder, out.getToEndpoint().getFtp().getUri());
		}
		
		if(out.getToEndpoint().getHazelcast() != null){
			this.appendToHazelcast(builder, out.getToEndpoint().getHazelcast().getUri());
		}
		
		if(out.getToEndpoint().getRest() != null){
			this.appendToRest(builder, out.getToEndpoint().getRest().getUri());
		}
		
		if(out.getToEndpoint().getGeneric() != null){
			this.appendToGeneric(builder, out.getToEndpoint().getGeneric().getUri());
		}
		
		this.appendEndRoute(builder);
		
		//create save payload route
		this.appendLoadPayloadRoute(builder, nodeId);
		
		closeRoutes(builder);
		
		return builder.toString();
	}

	private void openRoutes(StringBuilder builder){
		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		builder.append("<routes xmlns=\"http://camel.apache.org/schema/spring\">\n");
	}
	
	private void appendHeaders(StringBuilder builder, ProcessDefinition definition){
		appendConstantHeader(builder, MessageConstants.ACCOUNT_NAME, definition.getAccountName());
		appendConstantHeader(builder, MessageConstants.PROCESS_NAME, definition.getProcessName());
		appendConstantHeader(builder, MessageConstants.PROCESS_VERSION, definition.getProcessVersion());
		appendConstantHeader(builder, MessageConstants.PROCESS_ID, definition.getProcessId());
	}
	
	private void appendMarshaler(StringBuilder builder, String type){
		builder.append(String.format("\t\t<process ref=\"%s\"/>\n", type));
	}
	
	private void appendTransformation(StringBuilder builder, String nodeId){
		String transformation = String.format("xslt:http://localhost:%s/catify/get_transformation/%s?transformerFactory=transformerFactory", this.config.getHttpPort(), nodeId);
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", transformation));
	}
	
	private void appendSplitStart(StringBuilder builder, String xpath){
		builder.append("\t\t<split stopOnException=\"false\" streaming=\"true\">\n");
		builder.append(String.format("\t\t\t<xpath>%s</xpath>\n", xpath));
	}
	
	private void closeRoutes(StringBuilder builder) {
		builder.append("</routes>");
	}
	
	private void appendMulticastStart(StringBuilder builder){
		builder.append("\t\t\t<multicast>\n");
	}
	
	private void appendMulticastEnd(StringBuilder builder){
		builder.append("\t\t\t</multicast>\n");
	}
	
	private void appendCreateCorrelation(StringBuilder builder, String nodeId){
		builder.append(String.format("\t\t\t\t<to uri=\"direct:create-correlation-%s\"/>\n", nodeId));
	}
	
	private void appendCreateCorrelationRoute(StringBuilder builder, String nodeId){
		builder.append(String.format("\t<route id=\"create-correlation-%s\">\n", nodeId));
		builder.append(String.format("\t\t<from uri=\"direct:create-correlation-%s\"/>\n", nodeId));
		
		this.appendCreateCorrelationRule(builder, nodeId);
		
		//generate correlation hash
		builder.append("\t\t<process ref=\"writeCorrelationProcessor\"/>\n");
		
		//save correlation context in cache
		appendConstantHeader(builder, HazelcastConstants.OPERATION, "put");
		builder.append(String.format("\t\t<to uri=\"hazelcast:%s%s\"/>\n", HazelcastConstants.MAP_PREFIX, CacheConstants.CORRELATION_CACHE));
		this.appendEndRoute(builder);
		
	}
	
	private void appendGetCorrelation(StringBuilder builder, String nodeId){
		builder.append(String.format("\t\t\t<to uri=\"direct:get-correlation-%s\"/>\n", nodeId));
	}
	
	private void appendGetCorrelationRoute(StringBuilder builder, String nodeId){
		builder.append(String.format("\t<route id=\"get-correlation-%s\">\n", nodeId));
		builder.append(String.format("\t\t<from uri=\"direct:get-correlation-%s\"/>\n", nodeId));
		
		this.appendCreateCorrelationRule(builder, nodeId);
		
		//generate correlation hash
		builder.append("\t\t<process ref=\"readCorrelationProcessor\"/>\n");
		
		this.appendEndRoute(builder);
	}
	
	private void appendCreateCorrelationRule(StringBuilder builder, String nodeId){
		//transform correlation file
		String correlation = String.format("xslt:http://localhost:%s/catify/get_correlation_rule/%s?transformerFactory=transformerFactory", this.config.getHttpPort(), nodeId);
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", correlation));
	}
	
	private void appendSavePayload(StringBuilder builder, String nodeId){
		builder.append(String.format("\t\t\t\t<to uri=\"direct:save-payload-%s\"/>\n", nodeId));
	}
	
	private void appendSavePayloadRoute(StringBuilder builder, String nodeId){
		builder.append(String.format("\t<route id=\"save-payload-%s\">\n", nodeId));
		builder.append(String.format("\t\t<from uri=\"direct:save-payload-%s\"/>\n", nodeId));
		
		//save payload into cache
		appendConstantHeader(builder, HazelcastConstants.OPERATION, "put");
		appendSimpleHeader(builder, HazelcastConstants.OBJECT_ID, String.format("${header.%s}", MessageConstants.INSTANCE_ID));
		builder.append(String.format("\t\t<to uri=\"hazelcast:%s%s\"/>\n", HazelcastConstants.MAP_PREFIX, CacheConstants.PAYLOAD_CACHE));
		
		this.appendEndRoute(builder);
		
	}
	
	private void appendLoadPayload(StringBuilder builder, String nodeId){
		builder.append(String.format("\t\t<to uri=\"direct:load-payload-%s\"/>\n", nodeId));
	}
	
	private void appendLoadPayloadRoute(StringBuilder builder, String nodeId){
		builder.append(String.format("\t<route id=\"load-payload-%s\">\n", nodeId));
		builder.append(String.format("\t\t<from uri=\"direct:load-payload-%s\"/>\n", nodeId));
		
		//save payload into cache
		appendConstantHeader(builder, HazelcastConstants.OPERATION, "get");
		appendSimpleHeader(builder, HazelcastConstants.OBJECT_ID, String.format("${header.%s}", MessageConstants.INSTANCE_ID));
		builder.append(String.format("\t\t<to uri=\"hazelcast:%s%s\"/>\n", HazelcastConstants.MAP_PREFIX, CacheConstants.PAYLOAD_CACHE));
		
		this.appendEndRoute(builder);
	}
	
	private void appendToQueue(StringBuilder builder, String nodeId){
		builder.append(String.format("\t\t\t\t<to uri=\"direct:send-to-queue-%s\"/>\n", nodeId));
	}
	
	private void appendToQueueRoute(StringBuilder builder, String nodeId){
		builder.append(String.format("\t<route id=\"send-to-queue-%s\">\n", nodeId));
		builder.append(String.format("\t\t<from uri=\"direct:send-to-queue-%s\"/>\n", nodeId));
		
		//delete message body
		builder.append("\t\t<setBody>\n");
		builder.append("\t\t\t<constant></constant>\n");
		builder.append("\t\t</setBody>\n");
		
		//send message to queue
		builder.append(String.format("\t\t<inOnly uri=\"activemq:queue:in_%s\"/>\n", nodeId));
		
		this.appendEndRoute(builder);
	}
	
	private void appendFromEndpoint(StringBuilder builder, Endpoint endpoint, String nodeId, ProcessDefinition definition){
		
		if(endpoint instanceof FtpEndpoint){
			builder.append(String.format("\t<route id=\"in-ftp-%s\">\n", nodeId));
			builder.append(String.format("\t\t<from uri=\"%s\"/>\n", endpoint.getUri()));
		}
		
		if(endpoint instanceof HazelcastEndpoint){
			builder.append(String.format("\t<route id=\"in-hazelcast-%s\">\n", nodeId));
			builder.append(String.format("\t\t<from uri=\"%s\"/>\n", endpoint.getUri()));
		}
		
		if(endpoint instanceof FileEndpoint){
			builder.append(String.format("\t<route id=\"in-file-%s\">\n", nodeId));
			builder.append(String.format("\t\t<from uri=\"%s\"/>\n", endpoint.getUri()));
		}
		
		if(endpoint instanceof RestEndpoint){
			builder.append(String.format("\t<route id=\"in-rest-%s\">\n", nodeId));
			
			if(endpoint.getUri() != null){
				builder.append(String.format("\t\t<from uri=\"%s\"/>\n", endpoint.getUri()));
			} else if(nodeId != null) {
				builder.append(String.format("\t\t<from uri=\"restlet:http://localhost:%s/%s/%s/%s/%s?restletMethod=post\"/>\n", this.config.getHttpPort(), definition.getAccountName(), definition.getProcessName(), definition.getProcessVersion(), nodeId ));
			}
		}
		
//		the generic endpoint stands for all endpoints available over apache camel
		if(endpoint instanceof GenericEndpoint){
			builder.append(String.format("\t<route id=\"in-generic-%s\">\n", nodeId));
			builder.append(String.format("\t\t<from uri=\"%s\"/>\n", endpoint.getUri()));
		}
		
		
	}
	
	private void appendGetMessageFromQueue(StringBuilder builder, String nodeId){
		builder.append(String.format("\t\t<from uri=\"activemq:queue:out_%s\"/>\n", nodeId));
	}
	
	private void appendConstantHeader(StringBuilder builder, String key, String value){
		builder.append(String.format("\t\t<setHeader headerName=\"%s\">\n", key));
		builder.append(String.format("\t\t\t<constant>%s</constant>\n", value));
		builder.append("\t\t</setHeader>\n");
	}
	
	private void appendSimpleHeader(StringBuilder builder, String key, String value){
		builder.append(String.format("\t\t<setHeader headerName=\"%s\">\n", key));
		builder.append(String.format("\t\t\t<simple>%s</simple>\n", value));
		builder.append("\t\t</setHeader>\n");
	}
	
	private void appendToGeneric(StringBuilder builder, String uri) {
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", uri));
	}
	
	private void appendToRest(StringBuilder builder, String uri){
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", uri));
	}
	
	private void appendToFile(StringBuilder builder, String uri){
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", uri));
	}
	
	private void appendToFtp(StringBuilder builder, String uri){
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", uri));
	}
	
	private void appendToHazelcast(StringBuilder builder, String uri){
		this.appendSimpleHeader(builder, HazelcastConstants.OBJECT_ID, String.format("${header.%s}", MessageConstants.TASK_INSTANCE_ID));
		this.appendConstantHeader(builder, HazelcastConstants.OPERATION, "put");
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", uri));
	}
	
	private void appendEndRoute(StringBuilder builder){
		builder.append("\t</route>\n");
	}
	
}
