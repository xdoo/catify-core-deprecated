package com.catify.core.process.builders;

import com.catify.core.process.nodes.DecisionNode;
import com.catify.core.process.nodes.EndNode;
import com.catify.core.process.nodes.ForkNode;
import com.catify.core.process.nodes.LineEndNode;
import com.catify.core.process.nodes.LineNode;
import com.catify.core.process.nodes.MergeNode;
import com.catify.core.process.nodes.ReceiveNode;
import com.catify.core.process.nodes.ReplyNode;
import com.catify.core.process.nodes.RequestNode;
import com.catify.core.process.nodes.SleepNode;

public class MainProcessBuilder extends BaseProcessBuilder {
	
	private MainProcessBuilder processBuilder;
	private String nodeId;
	
	public MainProcessBuilder(){
		
	}

	public MainProcessBuilder(MainProcessBuilder processBuilder, String nodeId){
		this.processBuilder = processBuilder;
		this.nodeId = nodeId;
		
		this.initSubBuilders(processBuilder, nodeId);
	}
	
	
	public MainProcessBuilder request(String name, String serviceId) {
		currentNode = processDefinition.addNodeFrom(new RequestNode(processId, this.addToNodeNames(name), serviceId), this.currentNode);
		return this;
	}
	
	public MainProcessBuilder reply(String name, String serviceId){
		currentNode = processDefinition.addNodeFrom(new ReplyNode(processId, this.addToNodeNames(name), serviceId), this.currentNode);
		return this;
	}
	
	public MainProcessBuilder receive(String name, long timeout){
		currentNode = processDefinition.addNodeFrom(new ReceiveNode(processId, this.addToNodeNames(name), timeout), this.currentNode);
		return this;
	}
	
	/**
	 * creates an 'end' node. a end node has
	 * one incoming transition and no outgoing
	 * one. a process can have multiple end 
	 * nodes - so a end node is not automatically
	 * the end of a process instance, but the
	 * end of a specific route inside a process
	 * instance. 
	 * 
	 * @param name
	 * @return
	 */
	public MainProcessBuilder end(String name){		
		processDefinition.addNodeFrom(new EndNode(processId, this.addToNodeNames(name)), this.currentNode);
		return this;
	}
	
	/**
	 * end node with generated name
	 * 
	 * @return
	 */
	public MainProcessBuilder end(){
		return this.end(this.createNodeName("end"));
	}
	
	/**
	 * creates a fork node. a fork node has 
	 * one incoming transition and n outgoing.
	 * 
	 * @param name
	 * @return
	 */
	public MainProcessBuilder fork(String name) {
		currentNode = processDefinition.addNodeFrom(new ForkNode(processId, this.addToNodeNames(name)), this.currentNode);
		return new MainProcessBuilder(this, currentNode);
	}
	
	/**
	 * creates a decision node. a decision node has
	 * one incoming and n outgoing transitions, but only
	 * one of them will be triggered.
	 * 
	 * @param name
	 * @return
	 */
	public MainProcessBuilder decision(String name) {
		currentNode = processDefinition.addNodeFrom(new DecisionNode(processId, this.addToNodeNames(name)), this.currentNode);
		return new MainProcessBuilder(this, currentNode);
	}
	
	/**
	 * creates the merge node and returns back 
	 * to the parent builder.
	 * 
	 * @param name
	 * @return
	 */
	public MainProcessBuilder merge(String name){
		currentNode = processDefinition.addNodeFrom(new MergeNode(processId, this.addToNodeNames(name)), this.lineEnds);
		
		//set current node to the actual merge node
		this.processBuilder.setCurrentNode(currentNode);
		
		//set awaited hits
		MergeNode node = (MergeNode) processDefinition.getNode(currentNode);
		node.setAwaitedHits(processDefinition.getTransitionsToNode(currentNode).size());
		
		return this.processBuilder;
	}
	
	/**
	 * starts a line after a process splitt (fork,
	 * decision).
	 * 
	 * @param name
	 * @return
	 */
	public MainProcessBuilder line(String name) {
		currentNode = processDefinition.addNodeFrom(new LineNode(processId, this.addToNodeNames(name)), this.nodeId);
		return new MainProcessBuilder(this, currentNode);
	}
	
	protected void addEnline(String nodeId){
		this.lineEnds.add(nodeId);
	}
	
	/**
	 * set the end of a line.
	 * 
	 * @return
	 */
	public MainProcessBuilder endline(){
		
		//create node
		currentNode = processDefinition.addNodeFrom(new LineEndNode(processId, this.addToNodeNames(this.createNodeName("lineend"))), this.currentNode);
		
		//set current node to line end
		this.processBuilder.setCurrentNode(currentNode);
		
		//add line end
		this.processBuilder.addEnline(currentNode);
		
		
		return this.processBuilder;
	}
	
	public MainProcessBuilder sleep(String name, long time){
		currentNode = processDefinition.addNodeFrom(new SleepNode(processId, this.addToNodeNames(name), time), this.currentNode);
		return this;
	}
	
	public MainProcessBuilder sleep(long time){
		return this.sleep(this.createNodeName("sleep"), time);
	}
	
	
}
