package com.catify.core.process.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// MultiValueMap is not serializable - so we have to use MultiHashMap here
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.PipelineConstants;
import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.ProcessHelper;
import com.catify.core.process.nodes.Node;
import com.catify.core.process.xml.model.PipelineExceptionEvent;

public class ProcessDefinition implements Serializable {
	
	private static final long serialVersionUID = -4479421879930075250L;
	private String processId;
	private String accountName;
	private String processName;
	private String processVersion;
	
	private String startNodeId;
	
	private MultiMap leftTransitions;
	private MultiMap rightTransitions;
	private MultiMap precedingNodes;
	private Map<String, Node> nodes;
	
	private MultiMap pipelines;
	private Map<String, String> correlationRules;
	
	static final Logger LOG = LoggerFactory.getLogger(ProcessDefinition.class);

	public ProcessDefinition(String accountName, String processName, String processVersion){
		
		//generate a unique identifier for the process
		this.processId = ProcessHelper.createProcessId(accountName, processName, processVersion);
		
		this.accountName 	= accountName;
		this.processName 	= processName;
		this.processVersion = processVersion;
		
		//create the transition maps
		this.leftTransitions 	= new MultiHashMap();
		this.rightTransitions 	= new MultiHashMap();
		
		//create preceding nodes map
		this.precedingNodes 	= new MultiHashMap();
		
		//create the node map
		this.nodes = new HashMap<String, Node>();
		
		//create pipeline map
		this.pipelines = new MultiHashMap();
		
		//create the correlation rule map
		this.correlationRules = new HashMap<String,String>();
	}
	
	public void addTransition(String from, String to){
		rightTransitions.put(from, to);
		leftTransitions.put(to, from);
	}
	
	public String addNode(Node node){
		
		LOG.debug(String.format("addNode() --> nodeId: %s, nodeName: %s", node.getNodeId(), node.getNodeName()));
		
		nodes.put(node.getNodeId(), node);
		return node.getNodeId();
	}
	
	public String addNodeFrom(Node node, String fromNodeId){
		
		LOG.debug(String.format("addNodeFrom() --> nodeId: %s, nodeName: %s, fromNodeId: %s", node.getNodeId(), node.getNodeName(), fromNodeId));
		
		this.addNode(node);
		this.addTransition(fromNodeId, node.getNodeId());
		return node.getNodeId();
	}
	
	/**
	 * the precedingNodes are nodes that are standing in a special
	 * context to the given node - e.g. the nodes inside a line. it's
	 * useful to have a list of these nodes, if you have a fork with 
	 * n parallel lines, but if one is finished all the other should 
	 * be disabled as well. for this case you can easily find out 
	 * what the node ids are.
	 * 
	 * @param node
	 * @param fromNodeId
	 * @param precedingNodes
	 * @return
	 */
	public String addNodeFrom(Node node, String fromNodeId, List<String> precedingNodes){
		
		LOG.debug(String.format("addNodeFrom() --> nodeId: %s, nodeName: %s, fromNodeId: %s, precedingNodes: %s", node.getNodeId(), node.getNodeName(), fromNodeId, precedingNodes.toString()));
		
		Iterator<String> it = precedingNodes.iterator();
		while (it.hasNext()) {
			this.precedingNodes.put(node.getNodeId(), it.next());
		}
		
		return this.addNodeFrom(node, fromNodeId);
	}
	
	public String addNodeFrom(Node node, List<String> fromNodeIds){
		
		LOG.debug(String.format("addNodeFrom() --> nodeId: %s, nodeName: %s, fromNodeIds: %s", node.getNodeId(), node.getNodeName(), fromNodeIds.toString()));
		
		this.addNode(node);
		
		//if a node has more than one incoming transition (e.g. a merge node)
		Iterator<String> it = fromNodeIds.iterator();
		while (it.hasNext()) {
			this.addTransition(it.next(), node.getNodeId());
		}
		
		return node.getNodeId();
	}
	
	public void addNodes(Map<String, Node> nodes){
		nodes.putAll(nodes);
	}
	
	public void addStartPipeline(String pipeline){
		this.pipelines.put(PipelineConstants.START_PIPELINE, pipeline);
	}
	
	public void addInPipeline(String pipeline){
		this.pipelines.put(PipelineConstants.IN_PIPELINE, pipeline);
	}
	
	public void addOutPipeline(String pipeline){
		this.pipelines.put(PipelineConstants.OUT_PIPELINE, pipeline);
	}
	
	public String getStartPipeline(){
		if(this.pipelines.get(PipelineConstants.START_PIPELINE) != null){
			return ((List<String>) this.pipelines.get(PipelineConstants.START_PIPELINE)).get(0);
		} else {
			return null;
		}
	}
	
	public List<String> getInPipelines(){
		return (List<String>) this.pipelines.get(PipelineConstants.IN_PIPELINE);
	}
	
	public List<String> getOutPipelines(){
		return (List<String>) this.pipelines.get(PipelineConstants.OUT_PIPELINE);
	}
	
	public List<String> getPipelines(){
		
		//put all pipelines in one list
		List<String> pipelines = new ArrayList<String>();
		
		if(this.getOutPipelines() != null){
			pipelines.addAll(getOutPipelines());
		}
		
		if(this.getInPipelines() != null){
			pipelines.addAll(this.getInPipelines());
		}
		
		if(this.getStartPipeline() != null){
			pipelines.add(getStartPipeline());
		}
		
		return pipelines;
	}
	
	public void addCorrelationRule(String nodeId, String rule){
		this.correlationRules.put(nodeId, rule);
	}
	
	public String getCorrelationRule(String nodeId){
		return this.correlationRules.get(nodeId);
	}
	
	public Map<String,String> getAllCorrelationRules(){
		return this.correlationRules;
	}
	
	public List<String> getTransitionsFromNode(Node node){
		return this.getTransitionsFromNode(node.getNodeId());
	}
	
	public List<String> getTransitionsFromNode(String nodeId){
		return (List<String>) this.rightTransitions.get(nodeId);
	}
	
	public List<String> getTransitionsToNode(Node node){
		return this.getTransitionsToNode(node.getNodeId());
	}
	
	public List<String> getPrecedingNodesToNode(Node node){
		return (List<String>) this.precedingNodes.get(node.getNodeId());
	}
	
	public List<String> getTransitionsToNode(String nodeId){
		return (List<String>) this.leftTransitions.get(nodeId);
	}
	
	public List<String> getPrecedingNodesToNode(String nodeId){
		return (List<String>) this.precedingNodes.get(nodeId);
	}
	
	public Node getNode(String nodeId){
		return this.nodes.get(nodeId);
	}
	
	public Map<String,Node> getNodes(){
		return this.nodes;
	}
	
	/**
	 * returns the ids of all nodes inside a process.
	 * this can be a useful feature if you want to collect
	 * information (e.g. the states) over all nodes.
	 * 
	 * @return
	 */
	public Set<String> getAllNodeIds(){
		return this.nodes.keySet();
	}
	
	/**
	 * prints out all nodes with id, type (id) and name
	 * 
	 * @return
	 */
	public String nodesToString(){
		
		StringBuffer result = new StringBuffer();
		
		Iterator<String> it = this.getNodes().keySet().iterator();
		
		while (it.hasNext()) {
			String id = (String) it.next();
			
			Node node = this.getNode(id);
			
			result.append(String.format("id --> %s \t type --> %s \t name --> %s  \n", id, node.getNodeType(), node.getNodeName()));
			
		}
		
		return result.toString();
	}
	
	//process properties
	public String getProcessId() {
		return processId;
	}

	public String getAccountName() {
		return accountName;
	}

	public String getProcessName() {
		return processName;
	}

	public String getProcessVersion() {
		return processVersion;
	}

	public String getStartNodeId() {
		return startNodeId;
	}

	public void setStartNodeId(String startNodeId) {
		this.startNodeId = startNodeId;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("-----------------------------------------------------\n");
		builder.append(String.format("list of nodes for process %s (%s)\n", this.getProcessName(), this.getProcessVersion()));
		builder.append("-----------------------------------------------------\n\n");
		
		Iterator<String> it = this.getNodes().keySet().iterator();
		
		while (it.hasNext()) {
			String id = (String) it.next();
			
			Node node = this.getNode(id);
			
			builder.append(String.format("id --> %s \t type --> %s (%s) \t name --> %s  \n", id, this.mapNodeType(node.getNodeType()), node.getNodeType(), node.getNodeName()));
			
			List<String> transitionsToNode = this.getTransitionsToNode(id);
			if(transitionsToNode != null) {
				builder.append("\tincoming transitions:\n");
				Iterator<String> transitionsToNodeIt = transitionsToNode.iterator();
				while (transitionsToNodeIt.hasNext()) {
					String itnid = (String) transitionsToNodeIt.next();
					Node itnode = this.getNode(itnid);
					builder.append(String.format("\t id --> %s \t type --> %s (%s) \t name --> %s  \n", itnid, this.mapNodeType(itnode.getNodeType()), itnode.getNodeType(), itnode.getNodeName()));	
				}
				builder.append("\n");
			}
			
			
			
			List<String> transitionsFromNode = this.getTransitionsFromNode(id);			
			if(transitionsFromNode != null) {
				builder.append("\toutgoing transitions:\n");
				Iterator<String> transitionsFromNodeIt = transitionsFromNode.iterator();
				while (transitionsFromNodeIt.hasNext()) {
					String otnid = (String) transitionsFromNodeIt.next();
					Node otnode = this.getNode(otnid);
					builder.append(String.format("\t id --> %s \t type --> %s (%s) \t name --> %s  \n", otnid, this.mapNodeType( otnode.getNodeType()), otnode.getNodeType(), otnode.getNodeName()));
				}
				builder.append("\n");
			}
			
			builder.append("---------\n\n");
			
		}
		
		return builder.toString();
	}
	
	/**
	 * maps the node type number to a type name
	 * 
	 * @param type
	 * @return
	 */
	public String mapNodeType(int type) {
		
		switch (type) {
		case ProcessConstants.START:
			return "start";
			
		case ProcessConstants.REQUEST:
			return "request";
		
		case ProcessConstants.RECEIVE:
			return "receive";
		
		case ProcessConstants.REPLY:
			return "reply";
		
		case ProcessConstants.FORK:
			return "fork";
			
		case ProcessConstants.DECISION:
			return "decision";
			
		case ProcessConstants.LINE:
			return "line";
			
		case ProcessConstants.LINEEND:
			return "lineend";
			
		case ProcessConstants.SLEEP:
			return "sleep";
			
		case ProcessConstants.TIMEREVENT:
			return "timer event";
			
		case ProcessConstants.EXCEPTIONEVENT:
			return "exception event";
			
		case ProcessConstants.END:
			return "end";
		
		}
		return "no nodetype found";
	}
}
