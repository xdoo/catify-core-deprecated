package com.catify.core.process.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
// MultiValueMap is not serializable - so we have to use MultiHashMap here
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

import com.catify.core.constants.PipelineConstants;
import com.catify.core.process.nodes.Node;

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

	public ProcessDefinition(String accountName, String processName, String processVersion){
		
		//generate a unique identifier for the process
		this.processId = DigestUtils.md5Hex(String.format("%s%s%s", accountName, processName, processVersion));
		
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
		nodes.put(node.getNodeId(), node);
		return node.getNodeId();
	}
	
	public String addNodeFrom(Node node, String fromNodeId){
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
		
		Iterator<String> it = precedingNodes.iterator();
		while (it.hasNext()) {
			this.precedingNodes.put(node.getNodeId(), it.next());
		}
		
		return this.addNodeFrom(node, fromNodeId);
	}
	
	public String addNodeFrom(Node node, List<String> fromNodeIds){
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
}
