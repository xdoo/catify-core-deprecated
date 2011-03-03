package com.catify.core.rest;

import org.apache.commons.codec.digest.DigestUtils;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.nodes.Node;
import com.catify.core.process.nodes.DecisionNode;
import com.catify.core.process.nodes.EndNode;
import com.catify.core.process.nodes.ForkNode;
import com.catify.core.process.nodes.MergeNode;
import com.catify.core.process.nodes.ReceiveNode;
import com.catify.core.process.nodes.ReplyNode;
import com.catify.core.process.nodes.RequestNode;
import com.catify.core.process.nodes.StartNode;

import junit.framework.TestCase;

public class TestNodeModel extends TestCase {

	public void testGetNodeId() {
		Node node = new StartNode("1234567890", "node");
		assertNotNull(node);
		assertEquals(DigestUtils.md5Hex(String.format("%s%s", "1234567890", "node")), node.getNodeId());
	}

	public void testGetNodeName() {
		Node node = new StartNode("1234567890", "node");
		assertNotNull(node);
		assertEquals("node", node.getNodeName());
	}

	public void testGetNodeTypeEnd() {
		Node node = new EndNode("1234567890", "node");
		assertNotNull(node);
		assertEquals(ProcessConstants.END, node.getNodeType());
	}
	
	public void testGetNodeTypeRequest() {
		RequestNode node = new RequestNode("1234567890", "node", "1");
		assertNotNull(node);
		assertEquals(ProcessConstants.REQUEST, node.getNodeType());
		assertEquals("1", node.getServiceId());
	}
	
	public void testGetNodeTypeReceive() {
		ReceiveNode node = new ReceiveNode("1234567890", "node", 10000);
		assertNotNull(node);
		assertEquals(ProcessConstants.RECEIVE, node.getNodeType());
		assertEquals(10000, node.getTimeout());
	}
	
	public void testGetNodeTypeReply() {
		ReplyNode node = new ReplyNode("1234567890", "node", "1");
		assertNotNull(node);
		assertEquals(ProcessConstants.REPLY, node.getNodeType());
		assertEquals("1", node.getServiceId());
	}
	
	public void testGetNodeTypeMerge() {
		Node node = new MergeNode("1234567890", "node");
		assertNotNull(node);
		assertEquals(ProcessConstants.MERGE, node.getNodeType());
	}
	
	
	public void testGetNodeTypeFork() {
		Node node = new ForkNode("1234567890", "node");
		assertNotNull(node);
		assertEquals(ProcessConstants.FORK, node.getNodeType());
	}
	
	
	public void testGetNodeTypeDecision() {
		Node node = new DecisionNode("1234567890", "node");
		assertNotNull(node);
		assertEquals(ProcessConstants.DECISION, node.getNodeType());
	}
}
