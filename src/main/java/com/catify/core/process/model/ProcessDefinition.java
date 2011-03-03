package com.catify.core.process.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
// MultiValueMap is not serializable - so we have to use MultiHashMap here
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

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
	private Map<String, Node> nodes;

	public ProcessDefinition(String accountName, String processName, String processVersion){
		
		//generate a unique identifier for the process
		this.processId = DigestUtils.md5Hex(String.format("%s%s%s", accountName, processName, processVersion));
		
		this.accountName 	= accountName;
		this.processName 	= processName;
		this.processVersion = processVersion;
		
		//create the transition maps
		this.leftTransitions 	= new MultiHashMap();
		this.rightTransitions 	= new MultiHashMap();
		
		//create the node map
		this.nodes = new HashMap<String, Node>();
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
	
	public List<String> getTransitionsFromNode(Node node){
		return this.getTransitionsFromNode(node.getNodeId());
	}
	
	public List<String> getTransitionsFromNode(String nodeId){
		return (List<String>) this.rightTransitions.get(nodeId);
	}
	
	public List<String> getTransitionsToNode(Node node){
		return this.getTransitionsToNode(node.getNodeId());
	}
	
	public List<String> getTransitionsToNode(String nodeId){
		return (List<String>) this.leftTransitions.get(nodeId);
	}
	
	public Node getNode(String nodeId){
		return this.nodes.get(nodeId);
	}
	
	public Map<String,Node> getNodes(){
		return this.nodes;
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