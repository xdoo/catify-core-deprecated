package com.catify.persistence.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.CacheConstants;
import com.catify.core.process.ProcessDeployer;

public class JpaPersistenceRoutes extends RouteBuilder {

	static final Logger LOG = LoggerFactory.getLogger(ProcessDeployer.class);
	static final LoggingLevel WARN = LoggingLevel.WARN;
	
	@Override
	public void configure() throws Exception {		
	    from("seda:jpaCorrelationCacheStore")
	    .to("jpa://com.catify.persistence.beans.CorrelationCache?persistenceUnit=CatifyJpaPU");
	    
	    from("seda:jpaCorrelationRuleCacheStore")
//	    .to("log:CORRELATIONARULECACHE?showAll=true")
    	.to("jpa://com.catify.persistence.beans.CorrelationRuleCache?persistenceUnit=CatifyJpaPU");
	    
	    from("seda:jpaNodeCacheStore")
	    .to("jpa://com.catify.persistence.beans.NodeCache?persistenceUnit=CatifyJpaPU");
	    
	    from("seda:jpaPayloadCacheStore")
    	.to("jpa://com.catify.persistence.beans.PayloadCache?persistenceUnit=CatifyJpaPU");
	    
	    from("seda:jpaTimerCacheStore")
    	.to("jpa://com.catify.persistence.beans.TimerCache?persistenceUnit=CatifyJpaPU");
    }
		

}
