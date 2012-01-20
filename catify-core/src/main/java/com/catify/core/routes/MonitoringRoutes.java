package com.catify.core.routes;

import org.apache.camel.builder.RouteBuilder;

public class MonitoringRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("timer://monitor?fixedRate=true&period=60000")
        .routeId("monitor-event")
        .beanRef("hazelcastMonitor");

    }

}