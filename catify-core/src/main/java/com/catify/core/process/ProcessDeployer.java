package com.catify.core.process;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.EventConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.nodes.MergeNode;
import com.catify.core.process.nodes.Node;
import com.catify.core.process.nodes.ReceiveNode;
import com.catify.core.process.nodes.SleepNode;
import com.catify.core.process.processors.TransformDecisionPayloadProcessor;
import com.catify.core.process.routers.impl.CheckMergeRouter;
import com.catify.core.process.routers.impl.ReceiveRouter;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class ProcessDeployer {

	private CamelContext context;
	
	private final static String QUEUE = "activemq:queue:";
	private final static String NEXT = "activemq:queue:node.%s.%s.%s.%s";

	static final Logger LOG = LoggerFactory.getLogger(ProcessDeployer.class);
	static final LoggingLevel LEVEL = LoggingLevel.OFF;

	public ProcessDeployer(CamelContext context) {
		this.context = context;
	}

	public void deployProcess(ProcessDefinition definition) {
		
		// first create process start
		this.addToContext(this.createProcessStart(definition));

		// create routes for the nodes
		this.createProcessRoutes(definition);
		
		// deploy correlation rules
		deployCorrelationRules(definition.getAllCorrelationRules());
		
		// create in and out pipelines
		this.deployPipelines(definition.getPipelines());
		
		LOG.info(String.format("deployed process --> name = %s | version = %s | account = %s", definition.getProcessName(), definition.getProcessVersion(), definition.getAccountName()));

	}

	private void createProcessRoutes(ProcessDefinition definition) {

		Iterator<String> it = definition.getNodes().keySet().iterator();
		while (it.hasNext()) {
			Node node = definition.getNode(it.next());

			switch (node.getNodeType()) {
			case ProcessConstants.START:
				LOG.info(String.format(
						"creating START node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));
				this.addToContext(this.createStartNode(definition));
				break;

			case ProcessConstants.END:
				LOG.info(String.format("creating END node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));
				this.addToContext(this.createEndNode(definition,
						node.getNodeId()));
				break;

			case ProcessConstants.REQUEST:
				LOG.info(String.format(
						"creating REQUEST node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));
				this.addToContext(this.createRequestNode(definition,
						node.getNodeId()));
				break;

			case ProcessConstants.RECEIVE:
				LOG.info(String.format(
						"creating RECEIVE node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));

				// create receive node
				this.addToContext(this.createReceiveNode(
						definition,
						node.getNodeId(),
						ProcessHelper.getNormalNode(definition,
								node.getNodeId())));

				// create timer event
				if(ProcessHelper.getTimerEvent(definition,
						node.getNodeId()) != null){
				this.addToContext(this.createTimerEventNode(
						definition,
						node.getNodeId(),
						ProcessHelper.getTimerEvent(definition,
								node.getNodeId())));
				}

				// TODO --> create other exception events
				break;

			case ProcessConstants.FORK:
				LOG.info(String.format("creating FORK node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));
				this.addToContext(this.createForkNode(definition,
						node.getNodeId()));
				break;

			case ProcessConstants.DECISION:
				LOG.info(String.format(
						"creating DECISION node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));
				this.addToContext(this.createDecisionNode(definition,
						node.getNodeId()));
				break;

			case ProcessConstants.LINE:
				LOG.info(String.format("creating LINE node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));
				this.addToContext(this.createLineNode(definition,
						node.getNodeId()));
				break;

			case ProcessConstants.LINEEND:
				LOG.info(String.format(
						"creating LINEEND node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));
				this.addToContext(this.createLineendNode(definition,
						node.getNodeId()));
				break;

			case ProcessConstants.MERGE:
				LOG.info(String.format(
						"creating MERGE node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));
				this.addToContext(this.createMergeNode(definition,
						node.getNodeId()));
				break;

			case ProcessConstants.SLEEP:
				LOG.info(String.format(
						"creating SLEEP node '%s' with id '%s'.",
						node.getNodeName(), node.getNodeId()));
				this.addToContext(this.createSleepNode(definition,
						node.getNodeId()));
				break;
				
//			case ProcessConstants.EXCEPTIONEVENT:
//				LOG.info(String.format(
//						"creating EXCEPTION node '%s' with id '%s'.",
//						node.getNodeName(), node.getNodeId()));
//				this.addToContext(this.createSleepNode(definition,
//						node.getNodeId()));
//				break;

			default:
				break;
			}

		}
	}
	
	public static void deployCorrelationRules(Map<String, String> allCorrelationRules) {
		
		Iterator<String> it = allCorrelationRules.keySet().iterator();
		
		while (it.hasNext()) {
			
			String id = it.next();
			
			// the xslt templates have to be in the cache BEFORE
			// deploying the pipelines. So we have to put them directly
			// into the correlation rule cache.
			IMap<Object, Object> correlationruleCache = Hazelcast.getMap(CacheConstants.CORRELATION_RULE_CACHE);
			correlationruleCache.put(id, allCorrelationRules.get(id));
		}
	}
	
	private void deployPipelines(List<String> pipelines){
		
		Iterator<String> it = pipelines.iterator();
		
		while (it.hasNext()) {
			
			String pipeline = it.next();
			this.deployPipeline(pipeline);
		}
		
	}
	
	private void deployPipeline(String pipeline){
		
		if(pipeline != null){
			InputStream is = new ByteArrayInputStream(pipeline.getBytes());
			RoutesDefinition routes;
			try {
				routes = context.loadRoutesDefinition(is);
				context.addRouteDefinitions(routes.getRoutes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void addToContext(RouteBuilder builder) {
		try {
			this.context.addRoutes(builder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<Endpoint> getFormattedTransitions(
			ProcessDefinition definition, String nodeId) {

		List<Endpoint> result = new ArrayList<Endpoint>();

		Iterator<String> it = definition.getTransitionsFromNode(nodeId)
				.iterator();
		while (it.hasNext()) {
			result.add(context.getEndpoint(String.format(NEXT, definition.getAccountName(), definition.getProcessName(), definition.getProcessVersion(), definition.getNode(it.next()).getNodeName()
					)));
		}

		return result;
	}

	// ----------------------------------------------------> nodes

	private RouteBuilder createProcessStart(final ProcessDefinition definition) {
		
		LOG.info(String.format("creating start for PROCESS '%s' with id '%s'.",
				definition.getProcessName(), definition.getProcessId()));
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(definition.getStartNodeId()).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				
				// active:mq:in.{processname}.{processversion}
				fromF("%sin.%s.%s.%s.%s",
								QUEUE,
								account,
								process,
								version,
								nodename)
						.transacted()
						.routeId(
								String.format("process-%s",
										definition.getProcessId()))
						.processRef("initProcessProcessor")
						.log(LEVEL,
								"PROCESS",
								String.format(
										"process '%s', version '%s' received message. instanceId --> ${header.%s} ",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.toF(NEXT, account, process, version, nodename);

			}
		};
	}

	private RouteBuilder createStartNode(final ProcessDefinition definition) {
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(definition.getStartNodeId()).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				fromF(NEXT, account, process, version, nodename)
						.transacted()
						.routeId(
								String.format("node-%s",
										definition.getStartNodeId()))
						.onCompletion()
							.to("direct://done")
						.end()
						.setHeader(MessageConstants.TASK_ID,
								constant(definition.getStartNodeId()))
						// ...put it into the cache...
						.to("direct:working")
						.log(LEVEL,
								"START-NODE",
								String.format(
										"start node initialized.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						// create a task instance id...
						.processRef("taskInstanceIdProcessor")
						// ...goto next node...
						.toF(NEXT,account, process, version, definition.getNode(definition.getTransitionsFromNode(definition.getStartNodeId()).get(0)).getNodeName());
			}
		};
	}

	private RouteBuilder createSleepNode(final ProcessDefinition definition,
			final String nodeId) {

		final long time = ((SleepNode) definition.getNode(nodeId)).getTime();
		final String previousTaskId = definition.getTransitionsToNode(nodeId).get(0);
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				// ----------------------------------------
				// first part
				// ----------------------------------------
				// the sleep node has two parts. the first
				// one initializes the timer. the second one
				// runs after the expected timer event has
				// been fired.
				fromF(NEXT, account, process
						, version, nodename)
						.routeId(String.format("node-%s", nodeId))
						.onCompletion()
							.to("direct://waiting")
						.end()
						.log(LEVEL,
								String.format("SLEEP NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"sleeping initialized.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						.setHeader(MessageConstants.TASK_NAME, constant(nodename))
						// ...set state...
						.to("direct:working")
						//destroy state from previous node
						.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, previousTaskId)))
						.to("direct://destroy_with_given_id")
						// create a task instance id...
						.processRef("taskInstanceIdProcessor")
						// set timer
						.setHeader(EventConstants.EVENT_TIME, constant(time))
						.to("direct:set-timer-event");

				// ----------------------------------------
				// second part
				// ----------------------------------------
				fromF("%sevent.%s.%s.%s.%s", QUEUE, account, process, version, nodename)
						.transacted()
						.routeId(String.format("waekup-%s", nodeId))
						.onCompletion()
							.to("direct://done")
						.end()
						.log(LEVEL,
								String.format("SLEEP NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"sleeping ended.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// ...set state...
						.to("direct:working")
						// create a task instance id...
//						.processRef("taskInstanceIdProcessor")
						// ...goto next node...
						.log(LEVEL,
								String.format("SLEEP NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"signaling next node: sending message to 'seda:node-%s'.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getTransitionsFromNode(
												nodeId).get(0),
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.toF(NEXT, account, process, version, definition.getNode(definition.getTransitionsFromNode(nodeId).get(0)).getNodeName() );

			}
		};
	}

	private RouteBuilder createForkNode(final ProcessDefinition definition,
			final String nodeId) {
		
		final String previousTaskId = definition.getTransitionsToNode(nodeId).get(0);
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				fromF(NEXT, account, process, version, nodename)
						.transacted()
						.routeId(String.format("node-%s", nodeId))
						.onCompletion()
							.to("direct://done")
						.end()
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// ...set state...
						.to("direct:working")
						//destroy state from previous node
						.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, previousTaskId)))
						.to("direct://destroy_with_given_id")
						// create a task instance id...
//						.processRef("taskInstanceIdProcessor")
						// ...call all nodes...
						.multicast()
						.to(getFormattedTransitions(definition, nodeId));

			}
		};
	}

	private RouteBuilder createDecisionNode(final ProcessDefinition definition,
			final String nodeId) {
		
		final String previousTaskId = definition.getTransitionsToNode(nodeId).get(0);
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				// ----------------------------------------
				// IMPORTANT
				// ----------------------------------------
				// set for the decision concurrent consumers
				// never higher than '1' because the implementation
				// is NOT thread safe. you will get unexpected results.
				//
				fromF(NEXT, account, process, version, nodename)
						.transacted()
						.routeId(String.format("node-%s", nodeId))
						.onCompletion()
							.to("direct://done")
						.end()
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// ...set state...
						.to("direct://working")
						//destroy state from previous node
						.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, previousTaskId)))
						.to("direct://destroy_with_given_id")
						// create a task instance id...
						.processRef("taskInstanceIdProcessor") // do we need this?
						// ...dynamic router...
						.toF("direct:decide-%s", nodeId)
						.setBody(constant(null));

				// make decision
				fromF("direct:decide-%s", nodeId)
						.routeId(String.format("decide-%s", nodeId))
						// get payload
						.setHeader(
								HazelcastConstants.OBJECT_ID,
								simple(String.format("${header.%s}",
										MessageConstants.INSTANCE_ID)))
						.setHeader(HazelcastConstants.OPERATION,
								constant(HazelcastConstants.GET_OPERATION))
						.toF("hazelcast:%s%s",
								HazelcastConstants.MAP_PREFIX,
								CacheConstants.PAYLOAD_CACHE)
						.process(new TransformDecisionPayloadProcessor())
						//set signal header in line node to be here thread safe...
						.beanRef("decisionRouter", "route");// mark decision route with @DynamicRouter
			}
		};
	}

	private RouteBuilder createLineNode(final ProcessDefinition definition,
			final String nodeId) {

		// the previous node is always a decision or fork. decision
		// should be no problem, because we always will have only one
		// line at the same time. in the case of fork, we'll have n
		// parallel lines. so we have n deletions on the same state. 
		// but this should not an issue at all...
		final String previousTaskId = definition.getTransitionsToNode(nodeId).get(0);
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();

		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				fromF(NEXT, account, process, version, nodename)
						.transacted()
						.routeId(String.format("node-%s", nodeId))
						.onCompletion()
							.to("direct://done")
						.end()
						// cleanse body
						.setBody(constant(null))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// ...put it into the cache...
						.to("direct://working")
						//destroy state from previous node
						.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, previousTaskId)))
						.to("direct://destroy_with_given_id")
						// ...call next node...
						.toF(NEXT, account, process, version, definition.getNode(definition.getTransitionsFromNode(nodeId).get(0)).getNodeName());
			}
		};
	}

	private RouteBuilder createLineendNode(final ProcessDefinition definition,
			final String nodeId) {
		
		final String previousTaskId = definition.getTransitionsToNode(nodeId).get(0);
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				fromF(NEXT, account, process, version, nodename)
						.transacted()
						.routeId(String.format("node-%s", nodeId))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.processRef("taskInstanceIdProcessor")
						// ...put it into the cache...
						.wireTap("direct:working")
						//destroy state from previous node
						.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, previousTaskId)))
						.to("direct://destroy_with_given_id")
						// ...call next node...
						.log(LEVEL,
								String.format("LINE END NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received signal. going to merge node (%s).   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										this.getTransition(),
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.to(this.getTransition())
						// ...remove own state.
						.to("direct:destroy");
			}
			
			private String getTransition(){
				return String.format(NEXT, account, process, version, definition.getNode(definition.getTransitionsFromNode(nodeId).get(0)).getNodeName());
			}
		};
	}

	private RouteBuilder createMergeNode(final ProcessDefinition definition,
			final String nodeId) {
		
		// a merge is the end of n lines. so a merge node
		// has to destroy n states...
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				fromF(NEXT, account, process, version, nodename)
						.transacted()
						.routeId(String.format("node-%s", nodeId))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.processRef("taskInstanceIdProcessor")
						// ...set the awaited hits...
						.setHeader(MessageConstants.AWAITED_HITS,
								constant(getAwaitedHits()))
						// ...get the relevant preceding nodes
						.setHeader(MessageConstants.PRECEDING_NODES , 
								constant(this.getPrecedingNodes()))
						.log(LEVEL,
								String.format("MERGE NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received signal. going to dynamic router.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						// ...check how many nodes have been received yet...
						.dynamicRouter(bean(CheckMergeRouter.class, "route"));

				// if all lines have been ended,
				// clean up and go ahead...
				fromF("direct:cleannode-%s", nodeId)
						.routeId(String.format("cleannode-%s", nodeId))
						// goto next node...
						.toF(NEXT, account, process, version, definition.getNode(definition.getTransitionsFromNode(nodeId).get(0)).getNodeName() )
						// ...remove own state.
						.to("direct:destroy");

			}
			
			/**
			 * calculates the number of hits that the merge node
			 * gets before going to the next node
			 * 
			 * @return
			 */
			private int getAwaitedHits(){
				
				int awaitedHits = ((MergeNode) definition.getNode(nodeId)).getAwaitedHits();
				
				// the -1 stands for 'all' lines
				if(awaitedHits == -1){
					awaitedHits = definition.getTransitionsToNode(nodeId).size();
				}
				
				return awaitedHits;
			}
			
			/**
			 * 
			 * 
			 * @return
			 */
			private List<String> getPrecedingNodes(){
				
				List<String> precedingNodes = new ArrayList<String>();
				
				//get the line end nodes
				Iterator<String> it1 = definition.getTransitionsToNode(nodeId).iterator();
				
				while (it1.hasNext()) {
					
					//get preceding nodes for each line end node
					List<String> precedingNodesToNode = definition.getPrecedingNodesToNode(it1.next());
					Iterator<String> it2 = precedingNodesToNode.iterator();
					
					while (it2.hasNext()) {
						String id = (String) it2.next();
						
						//we only want to block non idempotent nodes
						if(definition.getNode(id).getNodeType() == ProcessConstants.REQUEST){
							precedingNodes.add(id);
						}// end if
						
					}// end while
					
				}// end while
				
				return precedingNodes;
			}
		};
	}

	private RouteBuilder createRequestNode(final ProcessDefinition definition,
			final String nodeId) {
		
		final String previousTaskId = definition.getTransitionsToNode(nodeId).get(0);
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from(String.format(NEXT, account, process, version, nodename))
						.transacted()
						.routeId(String.format("check-node-%s", nodeId))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.to("direct:getState")
						.setBody(simple("${body.state}"))
						/*
						 * this part is important to guarantee idempotence 
						 * in case of e.g. one line merge (first message
						 * wins).
						 */
						.choice()
							.when(body().isNotEqualTo(ProcessConstants.STATE_DONE))
								.toF("direct:node-%s", nodeId)
							.otherwise()
								.log(LEVEL,
								String.format("REQUEST NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received message, but state is 'DONE' - doing nothing.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID));
							
						
						
				fromF("direct:node-%s", nodeId)
						.routeId(String.format("node-%s", nodeId))
						.onCompletion()
							.to("direct://destroy")
						.end()
						.log(LEVEL,
								String.format("REQUEST NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received message and sending request.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						// ...put it into the cache...
						.to("direct:working")
						//destroy state from previous node
						.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, previousTaskId)))
						.to("direct://destroy_with_given_id")
						// ...send request...
						.log(LEVEL,
								String.format("REQUEST NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"sending message to 'hazelcast:%sout_%s'.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										HazelcastConstants.SEDA_PREFIX, 
										nodeId, definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						
						.toF("%sout.%s.%s.%s.%s", QUEUE, account, process, version, nodename)
						// ...goto next node...
						.log(LEVEL,
								String.format("REQUEST NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"signaling next node: sending message to 'seda:node-%s'.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getTransitionsFromNode(
												nodeId).get(0),
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.toF(NEXT, account, process, version, definition.getNode(definition.getTransitionsFromNode(nodeId).get(0)).getNodeName());
			}
		};
	}

	private RouteBuilder createReceiveNode(final ProcessDefinition definition,
			final String nodeId, final String defaultTransition) {

		// get node for timeout
		final ReceiveNode node = (ReceiveNode) definition.getNode(nodeId);
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		final String previousTaskId = definition.getTransitionsToNode(nodeId).get(0);

		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				// initialized from the process
				// -------------------------------------------
				// there're two cases:
				//
				// 1. a message has been arrived yet -->
				// then take the message and 'go!'
				// 2. no message has been arrived yet -->
				// then set state to wait...
				fromF(NEXT ,account, process, version, nodename)
						.transacted()
						.routeId(String.format("node-%s", nodeId))
						.onCompletion()
							.to("direct://waiting")
						.end()
						.log(LEVEL,
								String.format("RECEIVE NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received message (from seda).   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										process,
										version,
										MessageConstants.INSTANCE_ID))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						.to("direct://working")
						//destroy state from previous node
						.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, previousTaskId)))
						.to("direct://destroy_with_given_id")
						.dynamicRouter(bean(new ReceiveRouter(nodeId), "route"));

				// initialized from outside
				// -------------------------------------------
				// there're two cases
				//
				// 1. the current state is 'wait' -->
				// then go!
				// 2. there is no current state at all -->
				// the set state to 'wait' and wait for
				// the initialization from the process
				fromF("%sin.%s.%s.%s.%s", QUEUE, account, process, version, nodename)
						.transacted()
						.routeId(String.format("aqnode-%s", nodeId))
						.onCompletion()
							.to("direct://working")
						.end()
						.to("direct://waiting")
						.log(LEVEL,
								String.format("RECEIVE NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received message from queue.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.processRef("taskInstanceIdProcessor")
						.dynamicRouter(bean(new ReceiveRouter(nodeId), "route"))
						.removeHeader(ReceiveRouter.WAIT);

				// go! case
				// --------------
				fromF("direct:go-%s", nodeId)
						.routeId(String.format("go-%s", nodeId))
						.onCompletion()
							.to("direct://done")
						.end()
						.log(LEVEL,
								String.format("RECEIVE NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received go. going to node '%s' (id: %s).   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getNode(defaultTransition).getNodeName(),
										defaultTransition,
										process,
										version,
										MessageConstants.INSTANCE_ID))
						// goto next node...
						.toF(NEXT, account, process, version, definition.getNode(defaultTransition).getNodeName());

				// wait! case
				// --------------
				fromF("direct:wait-%s", nodeId)
						.routeId(String.format("wait-%s", nodeId))
						.onCompletion()
							.to("direct://waiting")
						.end()
						.to("direct://working")
						.log(LEVEL,
								String.format("RECEIVE NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"waiting for event.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						// set timer
						.setHeader(EventConstants.EVENT_TIME,
								constant(node.getTimeout()))
						.to("direct:set-timer-event")
						// set header for router
						.setHeader(ReceiveRouter.WAIT, constant("wait"));

			}

		};
	}

	private RouteBuilder createTimerEventNode(
			final ProcessDefinition definition, final String parentNodeId,
			final String nodeId) {
		
		final String previousTaskId = definition.getTransitionsToNode(nodeId).get(0);
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				// ----------------------------------------
				// time out event node
				// ----------------------------------------
				fromF("%sevent.%s.%s.%s.%s", QUEUE, account, process, version, definition.getNode(parentNodeId).getNodeName())
						.routeId(String.format("event-%s", parentNodeId))
						.onCompletion()
							.to("direct://done")
						.end()
						.to("direct://working")
						//destroy state from previous node
						.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, previousTaskId)))
						.to("direct://destroy_with_given_id")
						.log(LEVEL,
								String.format("RECEIVE NODE '%s'", definition
										.getNode(parentNodeId).getNodeName()),
								String.format(
										"received time out event.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.setHeader(MessageConstants.TASK_ID,
								constant(parentNodeId))
						// create a task instance id...
						.processRef("taskInstanceIdProcessor")
						// ...goto first node after the event node...
						.log(LEVEL,
								String.format("RECEIVE NODE '%s'", definition
										.getNode(parentNodeId).getNodeName()),
								String.format(
										"sending message to 'seda:node-%s'.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										nodeId,
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.toF(NEXT, account, process, version, nodename);

			}
		};
	}

	private RouteBuilder createEndNode(final ProcessDefinition definition,
			final String nodeId) {
		
		final String previousTaskId = definition.getTransitionsToNode(nodeId).get(0);
		
		final String account = definition.getAccountName();
		final String process = definition.getProcessName();
		final String version = definition.getProcessVersion();
		final String nodename = definition.getNode(nodeId).getNodeName();
		
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				fromF(NEXT, account, process, version, nodename)
				   		.transacted()
						.routeId(String.format("node-%s", nodeId))
						.onCompletion()
							.to("seda://destroy")
						.end()
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// ...put it into the cache...
						.to("direct:working")
						//destroy state from previous node
						.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-%s", MessageConstants.INSTANCE_ID, previousTaskId)))
						.to("direct://destroy_with_given_id")
						.log(LEVEL,
								String.format("END NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"ended part of process or whole process.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID));
			}
		};
	}
}
