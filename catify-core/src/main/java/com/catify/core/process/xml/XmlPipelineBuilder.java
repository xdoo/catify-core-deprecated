/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.catify.core.process.xml;

import java.util.Iterator;

import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.restlet.engine.http.connector.Acceptor;

import com.catify.core.configuration.GlobalConfiguration;
import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.processors.ReadCorrelationProcessor;
import com.catify.core.process.processors.XPathProcessor;
import com.catify.core.process.xml.model.Endpoint;
import com.catify.core.process.xml.model.InPipeline;
import com.catify.core.process.xml.model.OutPipeline;
import com.catify.core.process.xml.model.PipelineExceptionEvent;
import com.catify.core.process.xml.model.Variable;
import com.catify.core.process.xml.model.Variables;

public class XmlPipelineBuilder {

	private GlobalConfiguration config;
	private CorrelationRuleBuilder correlationRuleBuilder;
	// stores all routes that don't belong to the 
	// initial in or out route
	private StringBuilder additionalRoutes;
	
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
		this.additionalRoutes = new StringBuilder();
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(definition.getStartNodeId()).getNodeName();
		
		//open xml route
		openRoutes(builder);
		
		String tab = "\t\t";
			
		Endpoint endpoint = in.getEndpoint();
			
		// open route with id
		builder.append(String.format("\t<route id=\"start-%s\">\n", definition.getProcessId()));
			
		//set route id and from endpoint
		appendFromEndpoint(builder, endpoint, definition.getProcessId(), definition);
			
		//set headers
		this.appendHeaders(builder, definition);
					
			
		//generate instance id
		builder.append(String.format("%s<process ref=\"initProcessProcessor\"/>\n", tab));

		this.appendMulticastStart(builder);
			
		//check if a correlation is needed
		if(in.getCorrelation() != null){
					
			//create correlation rule file
			definition.addCorrelationRule(definition.getProcessId(), this.correlationRuleBuilder.buildCorrelationDefinition(in.getCorrelation().getXpath()));
					
			this.appendCreateCorrelation(builder, definition.getProcessId(), definition.getProcessId());// process & node id are equal in case of a start node
		}
			
		//store payload in cache
		this.appendSavePayload(builder, definition.getProcessId(), in.getVariables());
			
		//send message to queue
		this.appendToQueue(builder, definition.getProcessId(), account, process, version, nodename); // process & node id are equal in case of a start node
			
		this.appendMulticastEnd(builder);
			
		//end route
		this.appendEndRoute(builder);

		// add additional routes
		builder.append(this.additionalRoutes);
		
		closeRoutes(builder);
		
		//FIXME
		System.out.println(builder.toString());
		
		return builder.toString();
	}
	
	/**
	 * creates a xml pipeline declaration to get data from
	 * an external endpoint into the process engine.
	 * 
	 * @param in
	 * @param nodeId
	 * @param definition
	 * @return
	 */
	public String buildInPipeline(InPipeline in, String nodeId, ProcessDefinition definition){
		
		StringBuilder builder = new StringBuilder();
		this.additionalRoutes = new StringBuilder();
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		// open xml route
		openRoutes(builder);
		
		// get the endpoint definition
		Endpoint endpoint = in.getEndpoint();
		
		// open route with id
		builder.append(String.format("\t<route id=\"in-%s\">\n", nodeId));
			
		// add from endpoint clause
		appendFromEndpoint(builder, endpoint, nodeId, definition);
		
		// clean header
		this.appendConstantHeader(builder, ReadCorrelationProcessor.CORRELATION_EXCEPTION_HEADER, "");
			
		// set headers to identify the process
		this.appendHeaders(builder, definition);
		
		// add task id to the message header
		this.appendConstantHeader(builder, MessageConstants.TASK_ID, nodeId);
		
		// store payload inside header
		// -----
		// we have to do this, because we need the
		// payload after correlation
		this.appendSimpleHeader(builder, MessageConstants.TMP_PAYLOAD, "${body}");
		
		// add correlation handling
		this.appendGetCorrelation(builder, nodeId);
			
		// create correlation rule
		if(in.getCorrelation() != null) {
			if(in.getCorrelation().getXpath() != null){
				definition.addCorrelationRule(nodeId, this.correlationRuleBuilder.buildCorrelationDefinition(in.getCorrelation().getXpath()));
				
				// we have to deploy the error handler separate
				// (this is a camel issue)
				if(in.getPipelineExceptionEvent() != null){	
					this.appendGetCorrelationRouteWithExceptionHandler(this.additionalRoutes, nodeId, account, process, version, nodename, in.getPipelineExceptionEvent());
					// append error handler route
					this.appendPipelineExceptionRoute(this.additionalRoutes, nodeId, account, process, version, nodename, in.getPipelineExceptionEvent());
				} else {
					this.appendGetCorrelationRoute(this.additionalRoutes, nodeId);
				}
			}
		}
		
		// restore payload
		// -----
		// transfer the payload into the message
		// body and empty the header
		this.appendSimpleBody(builder, String.format("${header.%s}", MessageConstants.TMP_PAYLOAD));
		this.appendConstantHeader(builder, MessageConstants.TMP_PAYLOAD, "");
		
		//save payload
		this.appendSavePayload(builder, nodeId, in.getVariables());
			
		//send message to queue
		this.appendToQueue(builder, nodeId, account, process, version, nodename);
			
		//end route
		this.appendEndRoute(builder);
		
		// add additional routes
		builder.append(this.additionalRoutes);
				
		this.closeRoutes(builder);
		
		//FIXME
		System.out.println(builder.toString());
		
		return builder.toString();
	}

	public String buildOutPipeline(OutPipeline out, String nodeId, ProcessDefinition definition){
		
		//++++++++++++++++++++++++++++
		// TODO --> correlation
		//++++++++++++++++++++++++++++
		
		
		StringBuilder builder = new StringBuilder();
		this.additionalRoutes = new StringBuilder();
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		//open xml route
		openRoutes(builder);
		
		builder.append(String.format("\t<route id=\"out-pipeline-%s\">\n", nodeId));
		
		//get message from queue
		this.appendGetMessageFromQueue(builder, account, process, version, nodename);
		
		//get payload from cache
		if(out.getVariables() != null){
			//open multicast
			this.appendMulticastStart(builder);
			
			String uri = out.getEndpoint().getUri();
			
			Iterator<Variable> it = out.getVariables().getVariable().iterator();
			while (it.hasNext()) {
				Variable variable = (Variable) it.next();
				this.appendLoadPayload(builder, nodeId, variable.getName(), uri);
			}
			
			//close multicast
			this.appendMulticastEnd(builder);
		} else {
			// if we have no variables we must send a message to the destination
			builder.append(String.format("\t\t<to uri=\"%s\"/>\n", out.getEndpoint().getUri()));
		}
		
		this.appendEndRoute(builder);
		
		// add additional routes
		builder.append(this.additionalRoutes);
		
		closeRoutes(builder);
			
		//FIXME
//		System.out.println(builder.toString());
		
		return builder.toString();
	}
	
	/**
	 * creates the header of a camel routes declaration file:</br>
	 * 
	 * <pre>
	 * &lt;?xml version="1.0" encoding="UTF-8"?>
	 * &lt;routes xmlns="http://camel.apache.org/schema/spring">
	 * </pre>
	 * @param builder
	 */
	private void openRoutes(StringBuilder builder){
		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		builder.append("<routes xmlns=\"http://camel.apache.org/schema/spring\">\n");
	}
	
	/**
	 * sets some header properties to identify the
	 * process inside the pipeline.
	 * 
	 * @param builder
	 * @param definition
	 */
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
	
	private void appendCreateCorrelation(StringBuilder builder, String nodeId, String processId){
		builder.append(String.format("\t\t\t\t<to uri=\"direct:create-correlation-%s\"/>\n", nodeId));
		
		//create correlation route
		appendCreateCorrelationRoute(this.additionalRoutes, processId);
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
	
	/**
	 * adds a pointer to the correlation processor:</br>
	 *
	 * <pre>
	 * &lt;to uri="direct:get-correlation-NODE_ID"/>
	 * </pre>
	 * @param builder 
	 * @param nodeId
	 */
	private void appendGetCorrelation(StringBuilder builder, String nodeId){
		builder.append(String.format("\t\t<to uri=\"direct:get-correlation-%s\"/>\n", nodeId));
	}
	
	/**
	 * creates the correlation route:</br>
	 * 
	 * <pre>
	 * &lt;route id="get-correlation-{NODE_ID}">
	 *   &lt;from uri="direct:get-correlation-{NODE_ID}"/>
	 *   &lt;errorHandler id="{NODE_ID}" type="DeadLetterChannel"
	 *   				  deadLetterUri="seda://pipeline-exception-{NODE_ID}" useOriginalMessage="true"/>
	 *   &lt;to uri="xslt:http://localhost:{PORT}/catify/get_correlation_rule/{NODE_ID}?transformerFactory=transformerFactory"/>
	 *   &lt;process ref="readCorrelationProcessor"/>
	 * &lt;/route>
	 * </pre>
	 * 
	 * @param builder
	 * @param nodeId
	 */
	private void appendGetCorrelationRoute(StringBuilder builder, String nodeId){		

		builder.append(String.format("\t<route id=\"get-correlation-%s\">\n", nodeId, nodeId));
		builder.append(String.format("\t\t<from uri=\"direct:get-correlation-%s\"/>\n", nodeId));
		
		// create correlation id
		this.appendCreateCorrelationRule(builder, nodeId);
		builder.append("\t\t<process ref=\"readCorrelationProcessor\"/>\n");
		
		this.appendEndRoute(builder);
	}
	
	private void appendGetCorrelationRouteWithExceptionHandler(StringBuilder builder, String nodeId, String account, String process, String version, String nodename, PipelineExceptionEvent pipelineExceptionEvent) {
		builder.append(String.format("\t<route id=\"get-correlation-%s\">\n", nodeId, nodeId));
		builder.append(String.format("\t\t<from uri=\"direct:get-correlation-%s\"/>\n", nodeId));
		
		// create correlation id
		this.appendCreateCorrelationRule(builder, nodeId);
		builder.append("\t\t<process ref=\"readCorrelationProcessor\"/>\n");
		
		// create exception handling
		builder.append("\t\t<filter>\n");
		builder.append(String.format("\t\t\t<xpath>$%s = 'yes'</xpath>\n", ReadCorrelationProcessor.CORRELATION_EXCEPTION_HEADER));
		builder.append(String.format("\t\t\t<to uri=\"activemq:queue:pipeline.exception.%s.%s.%s.%s\"/>\n", account, process, version, nodename));
		builder.append("\t\t</filter>\n");
		
		this.appendEndRoute(builder);
	}
	
	/**
	 * <pre>
	 * &lt;route id="pipeline-exception-{NODE_ID}">
	 * 	&lt;from uri="seda://pipeline-exception-{NODE_ID}"/>
	 *  &lt;setBody>
	 *    &lt;simple>${header.catify_tmp_payload}&lt;/simple>
	 *  &lt;/setBody>
	 *  &lt;setHeader headerName="{HAZELCAST_OBJECT_ID}">
	 *    &lt;constant>&lt;/constant>
	 *  &lt;/setHeader>
	 *  &lt;to uri="{URI}"/>
	 * &lt;/route>
	 * </pre>
	 * 
	 * @param builder
	 * @param nodeId
	 * @param ex
	 */
	private void appendPipelineExceptionRoute(StringBuilder builder, String nodeId, String account, String process, String version, String nodename, PipelineExceptionEvent ex) {
		builder.append(String.format("\t<route id=\"pipeline-exception-%s\">\n", nodeId));
		builder.append(String.format("\t\t<from uri=\"activemq:queue:pipeline.exception.%s.%s.%s.%s\"/>\n", account, process, version, nodename));
		builder.append("<transacted/>");
		// TODO --> delete
		builder.append("\t\t<to uri=\"log:EXCEPTION?showAll=true\"/>\n");
		this.appendSimpleBody(builder, String.format("${header.%s}", MessageConstants.TMP_PAYLOAD));
		this.appendConstantHeader(builder, MessageConstants.TMP_PAYLOAD, "");
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", ex.getUri()));
		this.appendEndRoute(builder);
	}
	
	/**
	 * loads the correlation rule from the cache:</br>
	 * 
	 * <pre>
	 * &lt;to uri="xslt:http://localhost:{PORT}/catify/get_correlation_rule/{NODE_ID}?transformerFactory=transformerFactory"/>
	 * </pre>
	 * 
	 * @param builder
	 * @param nodeId
	 */
	private void appendCreateCorrelationRule(StringBuilder builder, String nodeId){
		//transform correlation file
		String correlation = String.format("xslt:http://localhost:%s/catify/get_correlation_rule/%s?transformerFactory=transformerFactory", this.config.getHttpPort(), nodeId);
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", correlation));
	}
	
	/**
	 * adds a link to the save payload route to store the payload(snippets) into the payload cache:</br>
	 * 
	 * <pre>
	 * &lt;to uri="direct:save-payload-{NODE_ID}-{VARIABLE_NAME}"/>
	 * &lt;to uri="direct:save-payload-{NODE_ID}-{VARIABLE_NAME}"/>
	 * ...
	 * </pre>
	 * @param builder
	 * @param nodeId
	 * @param variables 
	 */
	private void appendSavePayload(StringBuilder builder, String nodeId, Variables variables){
		
		if(variables != null) {
			Iterator<Variable> it = variables.getVariable().iterator();
			
			
			while (it.hasNext()) {
				Variable variable = (Variable) it.next();
				builder.append(String.format("\t\t<to uri=\"direct:save-payload-%s-%s\"/>\n", nodeId, variable.getName()));
				
				//create route
				this.appendSavePayloadRoute(this.additionalRoutes, nodeId, variable);
			}
		}
	}
	
	/**
	 * creates the route to save a payload or a snippet of it (@see com.catify.core.process.processors.XPathProcessor) into a variable:</br>
	 * 
	 * <pre>
	 * &lt;route id="save-payload-{NODE_ID}-{VARIABLE_NAME}">
	 *   &lt;from uri=\"direct:save-payload-{NODE_ID}-{VARIABLE_NAME}"/>
	 *   &lt;setHeader headerName="{VARIABLE_XPATH}">
	 *     &lt;constant>{XPATH}&lt;/constant>
	 * 	 &lt;/setHeader>
	 * 	 &lt;process ref="xpathProcessor"/>
	 *   &lt;setHeader headerName="{HAZELCAST_OPERATION}">
	 *     &lt;constant>{HAZELCAST_PUT}&lt;/constant>
	 * 	 &lt;/setHeader>
	 *   &lt;setHeader headerName="{HAZELCAST_OBJECT_ID}">
	 *     &lt;constant>${header.{INSTANCE_ID}}-{VARIBALE_NAME}&lt;/constant>
	 *   &lt;/setHeader>
	 *   &lt;to uri="hazelcast:map:payload-cache"/>
	 * &lt;/route>
	 * </pre>
	 * @param builder
	 * @param nodeId
	 */
	private void appendSavePayloadRoute(StringBuilder builder, String nodeId, Variable variable){
		builder.append(String.format("\t<route id=\"save-payload-%s-%s\">\n", nodeId, variable.getName()));
		builder.append(String.format("\t\t<from uri=\"direct:save-payload-%s-%s\"/>\n", nodeId,variable.getName()));
		
		//get xml snippet from xpath processor
		appendConstantHeader(builder, XPathProcessor.XPATH, variable.getXpath());
		builder.append("\t\t<process ref=\"xpathProcessor\"/>\n");
		
		//save payload into cache
		appendConstantHeader(builder, HazelcastConstants.OPERATION, "put");
		appendSimpleHeader(builder, HazelcastConstants.OBJECT_ID, String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, variable.getName()));
		builder.append(String.format("\t\t<to uri=\"hazelcast:%s%s\"/>\n", HazelcastConstants.MAP_PREFIX, CacheConstants.PAYLOAD_CACHE));
		
		this.appendEndRoute(builder);
		
	}
	
	/**
	 * calls the load payload route
	 * 
	 * <pre>
	 * &lt;to uri="direct:load-payload-{NODE_ID}"/>
	 * </pre>
	 * 
	 * @param builder
	 * @param nodeId
	 */
	private void appendLoadPayload(StringBuilder builder, String nodeId, String variableName, String uri){
		builder.append(String.format("\t\t<to uri=\"direct:load-payload-%s-%s\"/>\n", nodeId, variableName));
		this.appendLoadPayloadRoute(this.additionalRoutes, nodeId, variableName, uri);
	}
	
	/**
	 * 
	 * <pre>
	 * &lt;route id="load-payload-{NODE_ID}-{VARIABLE_NAME}">
	 *   &lt;from uri=\"direct:load-payload-{NODE_ID}-{VARIABLE_NAME}\"/>
	 *   &lt;setHeader headerName="{HAZELCAST_OPERATION}">
	 *     &lt;constant>{HAZELCAST_GET}&lt;/constant>
	 * 	 &lt;/setHeader>
	 *   &lt;setHeader headerName="{HAZELCAST_OBJECT_ID}">
	 *     &lt;constant>${header.{INSTANCE_ID}}-{VARIABLE_NAME}&lt;/constant>
	 *   &lt;/setHeader>
	 *   &lt;to uri="hazelcast:map:payload-cache"/>
	 * </pre>
	 * @param builder
	 * @param nodeId
	 */
	private void appendLoadPayloadRoute(StringBuilder builder, String nodeId, String variableName, String uri){
		builder.append(String.format("\t<route id=\"load-payload-%s-%s\">\n", nodeId, variableName));
		builder.append(String.format("\t\t<from uri=\"direct:load-payload-%s-%s\"/>\n", nodeId, variableName));
		
		//get payload from cache
		appendConstantHeader(builder, HazelcastConstants.OPERATION, "get");
		appendSimpleHeader(builder, HazelcastConstants.OBJECT_ID, String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, variableName));
		builder.append(String.format("\t\t<to uri=\"hazelcast:%s%s\"/>\n", HazelcastConstants.MAP_PREFIX, CacheConstants.PAYLOAD_CACHE));
		builder.append(String.format("\t\t<to uri=\"%s\"/>\n", uri));
		
		this.appendEndRoute(builder);
	}
	
	/**
	 * sends the message into the process:</br>
	 * 
	 * <pre>
	 * &lt;to uri="direct:send-to-queue-{NODE_ID}"/>
	 * </pre>
	 * @param builder
	 * @param nodeId
	 */
	private void appendToQueue(StringBuilder builder, String nodeId, String account, String process, String version, String nodename){
		builder.append(String.format("\t\t<to uri=\"direct:send-to-queue-%s\"/>\n", nodeId));
		
		//create route
		appendToQueueRoute(this.additionalRoutes, nodeId, account, process, version, nodename);
	}
	
	/**
	 * create the route to send a message into the process:</br>
	 * 
	 * <pre>
	 * &lt;route id="send-to-queue-{NODE_ID}">
	 *   &lt;from uri=\"direct:send-to-queue-{NODE_ID}\"/>
	 *   &lt;setBody>
	 *     &lt;constant>&lt;/constant>
	 *   &lt;/setBody>
	 *   &lt;process ref="serializeableHeadersProcessor"/>
	 *   &lt;inOnly uri="activemq:queue:in.{ACCOUNT}.{PROCESS}.{VERSION}.{NODENAME}"/>
	 * &lt;/route>
	 * </pre>
	 * 
	 * @param builder
	 * @param nodeId
	 */
	private void appendToQueueRoute(StringBuilder builder, String nodeId, String account, String process, String version, String nodename){
		builder.append(String.format("\t<route id=\"send-to-queue-%s\">\n", nodeId));
		builder.append(String.format("\t\t<from uri=\"direct:send-to-queue-%s\"/>\n", nodeId));
		
		//delete message body (the body will be saved inside the data grid)
		this.appendConstantBody(builder, "");
		
		//clean header from objects that are not serializable
		builder.append("\t\t<process ref=\"serializeableHeadersProcessor\"/>\n");
		
		//send message to queue
		// TODO --> active mq here
		builder.append(String.format("\t\t<inOnly uri=\"activemq:queue:in.%s.%s.%s.%s\"/>\n", account, process, version, nodename));
		
		this.appendEndRoute(builder);
	}
	
	/**
	 * fills the body with the given value
	 * 
	 * <pre>
	 * 	 &lt;setBody>
	 *     &lt;constant>{VALUE}&lt;/constant>
	 *   &lt;/setBody>
	 * </pre>
	 * @param builder
	 * @param value
	 */
	private void appendConstantBody(StringBuilder builder, String value) {
		builder.append("\t\t<setBody>\n");
		builder.append(String.format("\t\t\t<constant>%s</constant>\n", value));
		builder.append("\t\t</setBody>\n");		
	}
	
	/**
	 * fills the body with the given value
	 * 
	 * <pre>
	 * 	 &lt;setBody>
	 *     &lt;simple>{VALUE}&lt;/simple>
	 *   &lt;/setBody>
	 * </pre>
	 * @param builder
	 * @param value
	 */
	private void appendSimpleBody(StringBuilder builder, String value) {
		builder.append("\t\t<setBody>\n");
		builder.append(String.format("\t\t\t<simple>%s</simple>\n", value));
		builder.append("\t\t</setBody>\n");		
	}
	
	/**
	 * addds the from part to the route:</br>
	 * 
	 * <pre>
	 * &lt;from uri="{CAMEL_URI}"/>
	 * </pre>
	 * @param builder
	 * @param endpoint
	 * @param nodeId
	 * @param definition
	 */
	private void appendFromEndpoint(StringBuilder builder, Endpoint endpoint, String nodeId, ProcessDefinition definition){
		builder.append(String.format("\t\t<from uri=\"%s\"/>\n", endpoint.getUri()));
		
	}
	
	private void appendGetMessageFromQueue(StringBuilder builder, String account, String process, String version, String nodename){
		
		builder.append(String.format("\t\t<from uri=\"activemq:queue:out.%s.%s.%s.%s\"/>\n", account, process, version, nodename));
	}
	
	/**
	 * puts a header declaration into the route definition:</br>
	 * 
	 * <pre>
	 * &lt;setHeader headerName="{HEADER_KEY}">
	 *   &lt;constant>{HEADER_VALUE}&lt;/constant>
	 * &lt;/setHeader>
	 * </pre>
	 * @param builder
	 * @param key
	 * @param value
	 */
	private void appendConstantHeader(StringBuilder builder, String key, String value){
		builder.append(String.format("\t\t<setHeader headerName=\"%s\">\n", key));
		builder.append(String.format("\t\t\t<constant>%s</constant>\n", value));
		builder.append("\t\t</setHeader>\n");
	}
	
	/**
	 * adds a header using simple language:</br>
	 * 
	 * <pre>
	 * &lt;setHeader headerName="{HEADER_KEY}">
	 *   &lt;constant>{SIMPLE_LANGUAGE}&lt;/constant>
	 * &lt;/setHeader>
	 * </pre>
	 * @param builder
	 * @param key
	 * @param value
	 */
	private void appendSimpleHeader(StringBuilder builder, String key, String value){
		builder.append(String.format("\t\t<setHeader headerName=\"%s\">\n", key));
		builder.append(String.format("\t\t\t<simple>%s</simple>\n", value));
		builder.append("\t\t</setHeader>\n");
	}

	/**
	 * end tag for a xml route:</br>
	 * 
	 * <pre>
	 * &lt;/route>
	 * </pre>
	 * @param builder
	 */
	private void appendEndRoute(StringBuilder builder){
		builder.append("\t</route>\n");
	}
	
}
