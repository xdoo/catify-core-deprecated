package com.catify.core.process.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.catify.core.process.nodes.Node;
import com.catify.core.process.model.ProcessDefinition;

public class BaseProcessBuilder {

	private static final transient Log LOG = LogFactory.getLog(CatifyProcessBuilder.class);
	
	protected List<String> nodeNames = new ArrayList<String>();
	protected String currentNode;
	
	protected ProcessDefinition processDefinition;
	protected String processId;
	
	protected List<String> lineEnds = new ArrayList<String>();
	
	/**
	 * to get one process definition per process, it
	 * is necessary to merge process definitions from
	 * nodes like fork and decision, which can have n
	 * Independent lines. we need this to have a fluent
	 * java api.
	 * 
	 * @param definition
	 */
	public void mergeProcessDefinitions(ProcessDefinition definition){
		
		Map<String, Node> nodes = definition.getNodes();
		
		Iterator<String> it01 = nodes.keySet().iterator();
		while (it01.hasNext()) {
			String nodeId = (String) it01.next();
			
			//add node
			this.processDefinition.addNode(nodes.get(nodeId));
			
			//add transition(s)
			Iterator<String> it02 = definition.getTransitionsFromNode(nodeId).iterator();
			while (it02.hasNext()) {
				this.processDefinition.addTransition(nodeId, it02.next());
			}
		}
		
	}
	
	/**
	 * the name of a node has to be unique. if
	 * a node name is duplicated, then it has to
	 * be modified. this operation simply puts
	 * a '_' at the end of the node name. 
	 * 
	 * @param name
	 * @return
	 */
	protected String addToNodeNames(String name){
		String nn;
		if(!this.nodeNames.contains(name)){
			this.nodeNames.add(name);
			nn = name;
		} else {
			name = String.format("%s_", name);
			nn = this.addToNodeNames(name);
			LOG.error("node name is duplicated!");
		}
		
		return nn;
	}
	
	/**
	 * each sub builder needs some information from
	 * the parent builder. this information has to
	 * be copied to a new process definition.
	 *  
	 * 
	 * @param processBuilder
	 */
	protected void initSubBuilders(BaseProcessBuilder processBuilder){
		
		//create a new process definition
		processDefinition = processBuilder.getProcessDefinition();
		
		//copy name list
		this.nodeNames = processBuilder.getNodeNames();
		
		//copy process id
		this.processId = processBuilder.getProcessId();
	}
	
	/**
	 * convenient method to set also the
	 * current node id.
	 * 
	 * @param processBuilder
	 * @param currentNodeId
	 */
	protected void initSubBuilders(BaseProcessBuilder processBuilder, String currentNodeId){
		
		//set current node
		this.currentNode = currentNodeId;
		
		this.initSubBuilders(processBuilder);
		
	}
	
	/**
	 * creates a randomly generated node
	 * name, if the user doesn't provide
	 * one.
	 * 
	 * @return
	 */
	protected String createNodeName(){
		return UUID.randomUUID().toString();
	}
	
	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}

	public void setProcessDefinition(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}

	public List<String> getNodeNames() {
		return nodeNames;
	}

	public void setNodeNames(List<String> nodeNames) {
		this.nodeNames = nodeNames;
	}

	public String getProcessId() {
		return processId;
	}

	public String getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(String currentNode) {
		this.currentNode = currentNode;
	}
	
}
