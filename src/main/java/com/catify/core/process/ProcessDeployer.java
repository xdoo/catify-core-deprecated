package com.catify.core.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.drools.KnowledgeBase;
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
import com.catify.core.process.processors.InitProcessProcessor;
import com.catify.core.process.processors.TaskInstanceIdProcessor;
import com.catify.core.process.processors.TransformDecisionPayloadProcessor;
import com.catify.core.process.routers.CheckMergeRouter;
import com.catify.core.process.routers.DecisionRouter;
import com.catify.core.process.routers.ReceiveRouter;

public class ProcessDeployer {

	private CamelContext context;
	private KnowledgeBase kbase;

	static final Logger LOG = LoggerFactory.getLogger(ProcessDeployer.class);
	static final LoggingLevel LEVEL = LoggingLevel.INFO;

	public ProcessDeployer(CamelContext context, KnowledgeBase kbase) {
		this.context = context;
		this.kbase = kbase;
	}

	public void deployProcess(ProcessDefinition definition) {

		LOG.info("deploying process...");

		// first create process start
		this.addToContext(this.createProcessStart(definition));

		// create routes for the nodes
		this.createProcessRoutes(definition);

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
				this.addToContext(this.createTimerEventNode(
						definition,
						node.getNodeId(),
						ProcessHelper.getTimerEvent(definition,
								node.getNodeId())));

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

			default:
				break;
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
			result.add(context.getEndpoint(String.format("seda:node-%s",
					it.next())));
		}

		return result;
	}

	// ----------------------------------------------------> nodes

	private RouteBuilder createProcessStart(final ProcessDefinition definition) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				from(
						String.format("activemq:queue:in_%s",
								definition.getProcessId()))
						.routeId(
								String.format("process-%s",
										definition.getProcessId()))
						.process(new InitProcessProcessor())
						.log(LEVEL,
								"PROCESS",
								String.format(
										"process '%s', version '%s' received message. instanceId --> ${header.%s} ",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.to(String.format("seda:node-%s",
								definition.getStartNodeId()));

			}
		};
	}

	private RouteBuilder createStartNode(final ProcessDefinition definition) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from(
						String.format("seda:node-%s?concurrentConsumers=5",
								definition.getStartNodeId()))
						.routeId(
								String.format("node-%s",
										definition.getStartNodeId()))
						.setHeader(MessageConstants.TASK_ID,
								constant(definition.getStartNodeId()))
						.log(LEVEL,
								"START-NODE",
								String.format(
										"start node initialized.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
						// ...put it into the cache...
						.wireTap("direct:working")
						// ...goto next node...
						.to(String.format(
								"seda:node-%s",
								definition.getTransitionsFromNode(
										definition.getStartNodeId()).get(0)))
						// ...remove own state.
						.to("direct:destroy");
			}
		};
	}

	private RouteBuilder createSleepNode(final ProcessDefinition definition,
			final String nodeId) {

		final long time = ((SleepNode) definition.getNode(nodeId)).getTime();

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
				from(
						String.format("seda:node-%s?concurrentConsumers=5",
								nodeId))
						.routeId(String.format("node-%s", nodeId))
						.log(LEVEL,
								String.format("SLEEP NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"sleeping initialized.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
						// ...set state...
						.wireTap("direct:working")
						// set timer
						.setHeader(EventConstants.EVENT_TIME, constant(time))
						.to("direct:set-timer-event");

				// ----------------------------------------
				// second part
				// ----------------------------------------
				from("activemq:queue:event_" + nodeId)
						.routeId(String.format("waekup-%s", nodeId))
						.log(LEVEL,
								String.format("SLEEP NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"sleeping ended.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
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
						.to(String.format("seda:node-%s", definition
								.getTransitionsFromNode(nodeId).get(0)))
						// ...remove own state.
						.to("direct:destroy");

			}
		};
	}

	private RouteBuilder createForkNode(final ProcessDefinition definition,
			final String nodeId) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from(String.format("seda:node-%s", nodeId))
						.routeId(String.format("node-%s", nodeId))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
						// ...set state...
						.wireTap("direct:working")
						// ...call all nodes...
						.multicast()
						.to(getFormattedTransitions(definition, nodeId))
						// ...remove own state.
						.to("direct:destroy");

			}
		};
	}

	private RouteBuilder createDecisionNode(final ProcessDefinition definition,
			final String nodeId) {
		// TODO Auto-generated method stub
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
				from(
						String.format("seda:node-%s?concurrentConsumers=1",
								nodeId))
						.routeId(String.format("node-%s", nodeId))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
						// ...set state...
						.wireTap("direct:working")
						// ...dynamic router...
						.to(String.format("direct:decide-%s", nodeId))
						.setBody(constant(null))
						// ...remove own state.
						.to("direct:destroy");

				// make decision
				from(String.format("direct:decide-%s", nodeId))
						.routeId(String.format("decide-%s", nodeId))
						// get payload
						.setHeader(
								HazelcastConstants.OBJECT_ID,
								simple(String.format("${header.%s}",
										MessageConstants.INSTANCE_ID)))
						// create fact
						.setHeader(HazelcastConstants.OPERATION,
								constant(HazelcastConstants.GET_OPERATION))
						.to(String.format("hazelcast:%s%s",
								HazelcastConstants.MAP_PREFIX,
								CacheConstants.PAYLOAD_CACHE))
						.process(new TransformDecisionPayloadProcessor())
						.dynamicRouter(bean(new DecisionRouter(kbase), "route"));
			}
		};
	}

	private RouteBuilder createLineNode(final ProcessDefinition definition,
			final String nodeId) {

		// System.out.println(String.format("line --> %s", nodeId));

		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from(
						String.format("seda:node-%s?concurrentConsumers=5",
								nodeId))
						.routeId(String.format("node-%s", nodeId))
						// cleanse body
						.setBody(constant(null))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
						// ...put it into the cache...
						.wireTap("direct:working")
						// ...call next node...
						.to(String.format("seda:node-%s", definition
								.getTransitionsFromNode(nodeId).get(0)))
						// ...remove own state.
						.to("direct:destroy");
			}
		};
	}

	private RouteBuilder createLineendNode(final ProcessDefinition definition,
			final String nodeId) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from(
						String.format("seda:node-%s?concurrentConsumers=5",
								nodeId))
						.routeId(String.format("node-%s", nodeId))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
						// ...put it into the cache...
						.wireTap("direct:working")
						// ...call next node...
						.to(String.format("seda:node-%s", definition
								.getTransitionsFromNode(nodeId).get(0)))
						// ...remove own state.
						.to("direct:destroy");
			}
		};
	}

	private RouteBuilder createMergeNode(final ProcessDefinition definition,
			final String nodeId) {
		return new RouteBuilder() {

			private int awaitedHits = ((MergeNode) definition.getNode(nodeId))
					.getAwaitedHits();

			@Override
			public void configure() throws Exception {
				from(String.format("seda:node-%s", nodeId))
						.routeId(String.format("node-%s", nodeId))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
						// ...set the awaited hits...
						.setHeader(MessageConstants.AWAITED_HITS,
								constant(awaitedHits))
						// ...check how many nodes have been received yet...
						.dynamicRouter(bean(CheckMergeRouter.class, "route"));

				// if all lines have been ended,
				// clean up and go ahead...
				from(String.format("direct:cleannode-%s", nodeId))
						.routeId(String.format("cleannode-%s", nodeId))
						// goto next node...
						.to(String.format("seda:node-%s", definition
								.getTransitionsFromNode(nodeId).get(0)))
						// ...remove own state.
						.to("direct:destroy");

			}
		};
	}

	private RouteBuilder createRequestNode(final ProcessDefinition definition,
			final String nodeId) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from(
						String.format("seda:node-%s?concurrentConsumers=5",
								nodeId))
						.routeId(String.format("node-%s", nodeId))
						.log(LEVEL,
								String.format("REQUEST NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received message.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
						// ...put it into the cache...
						.wireTap("direct:working")
						// ...send request...
						.log(LEVEL,
								String.format("REQUEST NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"sending message to 'activemq:queue:out_%s'.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										nodeId, definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.wireTap(String.format("activemq:queue:out_%s", nodeId))
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
						.to(String.format("seda:node-%s", definition
								.getTransitionsFromNode(nodeId).get(0)))
						// ...remove own state.
						.to("direct:destroy");
			}
		};
	}

	private RouteBuilder createReceiveNode(final ProcessDefinition definition,
			final String nodeId, final String defaultTransition) {

		// get timeout
		final ReceiveNode node = (ReceiveNode) definition.getNode(nodeId);

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
				from(
						String.format("seda:node-%s?concurrentConsumers=5",
								nodeId))
						.routeId(String.format("node-%s", nodeId))
						.log(LEVEL,
								String.format("RECEIVE NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received message from seda.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
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
				from("activemq:queue:in_" + nodeId)
						.routeId(String.format("aqnode-%s", nodeId))
						.log(LEVEL,
								String.format("RECEIVE NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"received message from queue.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.dynamicRouter(bean(new ReceiveRouter(nodeId), "route"))
						.removeHeader(ReceiveRouter.WAIT);

				// go! case
				// --------------
				from(String.format("direct:go-%s", nodeId))
						.routeId(String.format("go-%s", nodeId))
						.log(LEVEL,
								String.format("RECEIVE NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"going to next node.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						// goto next node...
						.to(String.format("seda:node-%s", defaultTransition))
						// ...remove own state.
						.to("direct:destroy");

				// wait! case
				// --------------
				from(String.format("direct:wait-%s", nodeId))
						.routeId(String.format("wait-%s", nodeId))
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
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				// ----------------------------------------
				// time out event node
				// ----------------------------------------
				from("activemq:queue:event_" + parentNodeId)
						.routeId(String.format("event-%s", parentNodeId))
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
						.process(new TaskInstanceIdProcessor())
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
						.to(String.format("seda:node-%s", nodeId))
						// ...remove own state.
						.to("direct:destroy");

			}
		};
	}

	private RouteBuilder createEndNode(final ProcessDefinition definition,
			final String nodeId) {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from(
						String.format("seda:node-%s?concurrentConsumers=5",
								nodeId))
						.routeId(String.format("node-%s", nodeId))
						.log(LEVEL,
								String.format("END NODE '%s'", definition
										.getNode(nodeId).getNodeName()),
								String.format(
										"ended part of process or whole process.   process name --> '%s' | process version --> '%s' | instanceId --> ${header.%s}",
										definition.getProcessName(),
										definition.getProcessVersion(),
										MessageConstants.INSTANCE_ID))
						.setHeader(MessageConstants.TASK_ID, constant(nodeId))
						// .log(String.format("process ${header.%s} ---------------> ended",
						// MessageConstants.INSTANCE_ID))
						// create a task instance id...
						.process(new TaskInstanceIdProcessor())
						// ...put it into the cache...
						.to("direct:working")
						// ...remove own state.
						.to("direct:destroy");
			}
		};
	}
}