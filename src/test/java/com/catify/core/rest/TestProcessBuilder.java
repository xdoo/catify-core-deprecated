package com.catify.core.rest;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;

import com.catify.core.process.builders.MainProcessBuilder;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.nodes.EndNode;
import com.catify.core.process.nodes.Node;
import com.catify.core.process.nodes.ReceiveNode;
import com.catify.core.process.nodes.ReplyNode;
import com.catify.core.process.nodes.RequestNode;
import com.catify.core.process.nodes.StartNode;

public class TestProcessBuilder extends TestCase {

	private MainProcessBuilder builder;
	private String startNodeId;
	
	@Override
	public void setUp(){
		this.builder = new com.catify.core.process.builders.CatifyProcessBuilder().start("tester", "testprocess", "1.0", "start");
		this.startNodeId = DigestUtils.md5Hex(String.format("%s%s", this.builder.getProcessDefinition().getProcessId(), "start"));
	}
	
	public void testInit(){
		ProcessDefinition definition = builder.getProcessDefinition();
		
		assertNotNull(definition);
		assertEquals("tester", definition.getAccountName());
		assertEquals("testprocess", definition.getProcessName());
		assertEquals("1.0", definition.getProcessVersion());
	}
	
	public void testStart(){
		ProcessDefinition definition = builder.getProcessDefinition();
		
		assertEquals(1, definition.getNodes().size());
		Node node = definition.getNode(startNodeId);
		assertNotNull(node);
		assertTrue(node instanceof StartNode);
	}
	
	public void testRequest() {
		String name = "request";
		ProcessDefinition definition = builder.request(name, "1").getProcessDefinition();
		
		assertEquals(2, definition.getNodes().size());
		
		Node node = this.getNode(name, definition);		
		assertTrue(node instanceof RequestNode);
		this.checkTransition(definition, node);
		
	}

	public void testReply() {
		String name = "reply";
		ProcessDefinition definition = builder.reply(name, "1").getProcessDefinition();
		
		assertEquals(2, definition.getNodes().size());
		
		Node node = this.getNode(name, definition);
		assertTrue(node instanceof ReplyNode);
		this.checkTransition(definition, node);
	}

	public void testReceive() {
		String name = "receive";
		ProcessDefinition definition = builder.receive(name, 10000).getProcessDefinition();
		
		assertEquals(2, definition.getNodes().size());
		
		Node node = this.getNode(name, definition);
		assertTrue(node instanceof ReceiveNode);
		this.checkTransition(definition, node);
	}

	public void testEnd() {
		String name = "end";
		ProcessDefinition definition = builder.end(name).getProcessDefinition();
		
		assertEquals(2, definition.getNodes().size());
		
		Node node = this.getNode(name, definition);
		assertTrue(node instanceof EndNode);
		this.checkTransition(definition, node);
	}

	public void testMergeProcessDefinitions() {
		ProcessDefinition definition01 = builder.request("foo", "1").end("end1").getProcessDefinition();
	}

	public void testAddToNodeNames() {
		ProcessDefinition definition = builder.request("foo", "1").request("foo", "1").request("foo", "1").getProcessDefinition();
		Map<String, Node> nodes = definition.getNodes();
		
		assertEquals(4, nodes.size());
		
		String nodeId1 = DigestUtils.md5Hex(String.format("%s%s", definition.getProcessId(), "foo_"));
		assertTrue(nodes.containsKey(nodeId1));
		assertEquals("foo_", definition.getNode(nodeId1).getNodeName());
		
		String nodeId2 = DigestUtils.md5Hex(String.format("%s%s", definition.getProcessId(), "foo__"));
		assertTrue(nodes.containsKey(nodeId2));
		assertEquals("foo__", definition.getNode(nodeId2).getNodeName());
	}
	
	public void testMoreThanOneNode(){
		ProcessDefinition definition01 = builder.request("bar-01", "1").request("bar-02", "1").request("bar-03", "1").end("end").getProcessDefinition();
		assertEquals(5, definition01.getNodes().size());
	}
	
	private Node getNode(String name, ProcessDefinition definition){
		Node node = definition.getNode(DigestUtils.md5Hex(String.format("%s%s", definition.getProcessId(), name)));
		assertNotNull(node);
		return node;
	}
	
	private void checkTransition(ProcessDefinition definition, Node node){
		List<String> transitionsToNode = definition.getTransitionsToNode(node);
		assertNotNull(transitionsToNode);
		assertEquals(1, transitionsToNode.size());
		assertEquals(startNodeId, transitionsToNode.get(0));
	}

}
