package com.catify.core.rest;

import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;

import com.catify.core.process.builders.CatifyProcessBuilder;
import com.catify.core.process.model.ProcessDefinition;

public class TestFluentApi extends TestCase {

	private String processId;

	@Override
	public void setUp(){
		processId 	= DigestUtils.md5Hex(String.format("%s%s%s", "tester", "process", "1.0"));
	}
	
	public void testSimpleProcess(){
		CatifyProcessBuilder process = new CatifyProcessBuilder();
		
		process.start("tester", "process", "1.0", "start")
		.request("rq-01", "1")
		.end("end");
		
		ProcessDefinition definition = process.getProcessDefinition();
		
		assertNotNull(definition);
		
		//nodes
		assertEquals(3, definition.getNodes().size());
		String [] nodes = {"start", "rq-01", "end"};
		this.testNodes(nodes, definition);
		
		//transitions
		assertEquals(this.nodeId("rq-01"), definition.getTransitionsFromNode(this.nodeId("start")).get(0));
		
		assertEquals(this.nodeId("end"), definition.getTransitionsFromNode(this.nodeId("rq-01")).get(0));
		assertEquals(this.nodeId("start"), definition.getTransitionsToNode(this.nodeId("rq-01")).get(0));
		
		assertEquals(this.nodeId("rq-01"), definition.getTransitionsToNode(this.nodeId("end")).get(0));
		
	}
	
	public void testSimpleForkedProcess(){
		CatifyProcessBuilder process = new CatifyProcessBuilder();
		
		process.start("tester", "process", "1.0", "start")
		.fork("f-01")
			.line("l-01").request("rq-01-01", "1").endline()
			.line("l-02").request("rq-02-01", "1").endline()
		.merge("m-01")
		.end("end");
		
		ProcessDefinition definition = process.getProcessDefinition();
		
		assertNotNull(definition);
		
		//nodes
		assertEquals(10, definition.getNodes().size());
		String[] nodes = {"start", "f-01", "l-01", "l-02", "rq-01-01", "rq-02-01", "m-01", "end"};
		this.testNodes(nodes, definition);
		
		//transitions
		assertEquals(2, definition.getTransitionsFromNode(nodeId("f-01")).size());
		assertEquals(2, definition.getTransitionsToNode(nodeId("m-01")).size());
	}
	
	public void testComplexForkedProcess(){
		CatifyProcessBuilder process = new CatifyProcessBuilder();
		
		process.start("tester", "process", "1.0", "start")
		.fork("f-01")
			.line("l-01")
				.fork("f-02")
					.line("l-02-01").request("rq-01-02-01", "1").endline()
					.line("l-02-02").request("rq-01-02-02", "1").endline()
					.line("l-02-03").request("rq-01-02-03", "1").endline()
				.merge("m-02").request("l-01-01", "1").endline()
			.line("l-02").request("rq-02-01", "1").endline()
		.merge("m-01")
		.end("end");
		
		ProcessDefinition definition = process.getProcessDefinition();
		
		assertNotNull(definition);
		
		//nodes
		assertEquals(21, definition.getNodes().size());
		String[] nodes = {"start", "f-01", "l-01", "f-02", "l-02-01", "rq-01-02-01", "l-02-02", "rq-01-02-02", "l-02-03", "rq-01-02-03", "m-02", "l-01-01", "l-02", "rq-02-01", "m-01", "end"};
		this.testNodes(nodes, definition);
		
		//transitions
		assertEquals(2, definition.getTransitionsFromNode(nodeId("f-01")).size());
		assertEquals(2, definition.getTransitionsToNode(nodeId("m-01")).size());
		
		assertEquals(3, definition.getTransitionsFromNode(nodeId("f-02")).size());
		assertEquals(3, definition.getTransitionsToNode(nodeId("m-02")).size());
	}
	
	private String nodeId(String name){
		return DigestUtils.md5Hex(String.format("%s%s", this.processId, name));
	}
	
	private void testNodes(String[] nodes, ProcessDefinition definition){
		for (int i = 0; i < nodes.length; i++) {
			assertNotNull(definition.getNode(this.nodeId(nodes[i])));
		}
		
//		Iterator<String> it = definition.getNodes().keySet().iterator();
//		while (it.hasNext()) {
//			String id = (String) it.next();
//			System.out.print(id + " --> ");
//			System.out.print(this.nodeId(definition.getNode(id).getNodeName()) + " --> ");
//			System.out.println(definition.getNode(id).getNodeName());;
//		}
	}
	
}
