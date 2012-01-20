package com.catify.core.monitor;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

public class HazelcastTest extends CamelTestSupport {

    @Test public void testHazelcastMonitor() throws InterruptedException {
        HazelcastMonitor monitor = new HazelcastMonitor();

        IMap<String, String> foom = Hazelcast.getMap("foo_map");
        IMap<String, String> barm = Hazelcast.getMap("bar_map");

        foom.put("1", "foo1");
        foom.put("2", "foo2");
        barm.put("1", "bar1");

        IQueue<String> fooq = Hazelcast.getQueue("foo_queue");

        fooq.put("foo1");
        fooq.put("foo2");

        monitor.listInstances();
    }

}
