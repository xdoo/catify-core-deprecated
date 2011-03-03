package com.catify.core.process.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.catify.core.process.model.ProcessDefinition;
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
import com.catify.core.process.nodes.StartNode;
import com.catify.core.process.nodes.TimerEventNode;
import com.catify.core.process.xml.model.Decision;
import com.catify.core.process.xml.model.End;
import com.catify.core.process.xml.model.Fork;
import com.catify.core.process.xml.model.Line;
import com.catify.core.process.xml.model.Node;
import com.catify.core.process.xml.model.Process;
import com.catify.core.process.xml.model.Receive;
import com.catify.core.process.xml.model.Reply;
import com.catify.core.process.xml.model.Request;
import com.catify.core.process.xml.model.Sleep;
import com.catify.core.process.xml.model.Start;
import com.catify.core.process.xml.model.TimeEvent;

public class XmlProcessBuilder {
	
	private String current;
	private ProcessDefinition definition;
	
	public ProcessDefinition build(Process process){
		
		String accountName = process.getAccountName();
		String processName = process.getProcessName();
		String processVersion = process.getProcessVersion();
		
		this.definition = new ProcessDefinition(accountName, processName, processVersion);
		
		process.setProcessId(this.definition.getProcessId());
		
		this.addStartNode(process.getStart());
		
		this.addNodes(process.getNodes());
		
		return this.definition;
	}
	
	private void addStartNode(Start start){
		
		//	the 'set with current...' does not work, because the 
		//	start node has no parent node
		StartNode startNode = new StartNode(this.definition.getProcessId(), start.getName());
		
		//add node
		definition.addNode(startNode);
		
		//set start node id
		definition.setStartNodeId(startNode.getNodeId());
		
		start.setId(startNode.getNodeId());
		this.current = startNode.getNodeId();
	}
	
	private void addNodes(List<Node> nodes){
		
		Iterator<Node> it = nodes.iterator();
		
		while (it.hasNext()) {
			Node node = (Node) it.next();
			
			if(node instanceof Request){
				this.addRequestNode((Request) node);
			}
			
			if(node instanceof Receive){
				this.addReceiveNode((Receive) node);
			}
			
			if(node instanceof Reply){
				this.addReplyNode((Reply) node);
			}
			
			if(node instanceof Sleep){
				this.addSleepNode((Sleep) node);
			}
			
			if(node instanceof End){
				this.addEndNode((End) node);
			}
			
			if(node instanceof Fork){
				this.addForkNode((Fork) node);
			}
			
			if(node instanceof Decision){
				this.addDecisionNode((Decision) node);
			}			
			
		}
		
	}

	private String addLineNode(Line node, String splitNodeId) {
		
		//set line start node
		node.setId(this.addNodeWithCurrent(new LineNode(this.definition.getProcessId(), node.getName())));
		
		//add nodes after
		this.addNodes(node.getNodes());
		
		//set line end node
		String id = this.addNodeWithCurrent(new LineEndNode(this.definition.getProcessId(), UUID.randomUUID().toString()));
		
		return id;
	}

	private void addDecisionNode(Decision node) {
		String nodeId = this.addNodeWithCurrent(new DecisionNode(this.definition.getProcessId(), node.getName()));
		this.addLines(node.getLine(), nodeId);
		node.setId(nodeId);
	}

	private void addForkNode(Fork node) {
		String nodeId = this.addNodeWithCurrent(new ForkNode(this.definition.getProcessId(), node.getName()));
		this.addLines(node.getLine(), nodeId);
		node.setId(nodeId);
	}
	
	private void addLines(List<Line> lines, String nodeId){
		
		List<String> ids = new ArrayList<String>();
		
		//iterate through the lines
		Iterator<Line> it = lines.iterator();
		while (it.hasNext()) {
			Line line = (Line) it.next();
			ids.add(this.addLineNode(line, nodeId));
		}
		
		//merge node
		this.current = this.definition.addNodeFrom(new MergeNode(this.definition.getProcessId(), UUID.randomUUID().toString()), ids);
	}

	private void addEndNode(End node) {
		EndNode endNode = new EndNode(this.definition.getProcessId(), node.getName());
		this.addNodeWithoutCurrent(endNode);
		node.setId(endNode.getNodeId());
	}

	private void addSleepNode(Sleep node) {
		node.setId(this.addNodeWithCurrent(new SleepNode(this.definition.getProcessId(), node.getName(), node.getTimeEvent().getTime())));
	}

	private void addReplyNode(Reply node) {
		node.setId(this.addNodeWithCurrent(new ReplyNode(this.definition.getProcessId(), node.getName(), "1")));
	}

	private void addReceiveNode(Receive node) {
		node.setId(this.addNodeWithCurrent(new ReceiveNode(this.definition.getProcessId(), node.getName(), node.getTimeEvent().getTime())));
		
		//add timer event
		this.addTimerEventNode(node.getTimeEvent(), node.getName());
		
		//set receive node back to current
		this.current = node.getId();
			
	}
	
	private void addTimerEventNode(TimeEvent node, String parentName){
		node.setId(this.addNodeWithCurrent(new TimerEventNode(this.definition.getProcessId(), String.format("timerevent-%s", parentName))));
		
		//build the tasks after the time event
		this.addNodes(node.getNodes());
	}

	private void addRequestNode(Request node) {
		node.setId(this.addNodeWithCurrent(new RequestNode(this.definition.getProcessId(), node.getName(), "1")));
	}
	
	private String addNodeWithCurrent(com.catify.core.process.nodes.Node node){
		this.definition.addNodeFrom(node, this.current);
		this.current = node.getNodeId();
		return node.getNodeId();
	}
	
	private void addNodeWithoutCurrent(com.catify.core.process.nodes.Node node){
		this.definition.addNodeFrom(node, this.current);
	}

}
