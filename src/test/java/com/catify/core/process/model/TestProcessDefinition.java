package com.catify.core.process.model;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;

import com.catify.core.process.nodes.Node;
import com.catify.core.process.builders.CatifyProcessBuilder;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.nodes.StartNode;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestProcessDefinition extends TestCase {

	public void testMetadata(){
		ProcessDefinition pd = new ProcessDefinition("tester", "testprocess", "1.0");
		
		assertEquals("tester", pd.getAccountName());
		assertEquals("testprocess", pd.getProcessName());
		assertEquals("1.0", pd.getProcessVersion());
		assertEquals(DigestUtils.md5Hex(String.format("%s%s%s", "tester", "testprocess", "1.0")), pd.getProcessId());
	}
	
	public void testNodeHandling(){
		ProcessDefinition pd = new ProcessDefinition("tester", "testprocess", "1.0");
		
		String nodeId = pd.addNode(new StartNode(pd.getProcessId(), "start"));
		Node node = pd.getNode(nodeId);
		
		assertNotNull(node);
		assertEquals("start", node.getNodeName());
	}
	
	public void testTransitionHandling(){
		ProcessDefinition pd = new ProcessDefinition("tester", "testprocess", "1.0");
		
		// foo -> foobar -> bar
		pd.addTransition("foo", "foobar");
		pd.addTransition("foobar", "bar");
		
		List<String> toFoo = pd.getTransitionsToNode("foo");
		assertNull(toFoo);
		
		List<String> fromFoo = pd.getTransitionsFromNode("foo");
		assertEquals(1, fromFoo.size());
		assertEquals("foobar", fromFoo.get(0));
		
		List<String> toFooBar = pd.getTransitionsToNode("foobar");
		assertEquals(1, toFooBar.size());
		assertEquals("foo", toFooBar.get(0));
		
		List<String> fromFooBar = pd.getTransitionsFromNode("foobar");
		assertEquals(1, fromFooBar.size());
		assertEquals("bar", fromFooBar.get(0));
		
		List<String> toBar = pd.getTransitionsToNode("bar");
		assertEquals(1, toBar.size());
		assertEquals("foobar", toBar.get(0));
		
		List<String> fromBar = pd.getTransitionsFromNode("bar");
		assertNull(fromBar);
		
	}
	
	public void testHazelcastSerialization(){
		IMap<String, ProcessDefinition> map = Hazelcast.getMap("definition");
		
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
		
		map.put("1234", definition);
		assertNotNull(map.get("1234"));
		assertEquals("process", definition.getProcessName());
		
	}
	
	
}
