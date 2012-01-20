package com.catify.core.monitor;

import java.util.Iterator;

import org.apache.camel.Handler;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Instance;
import com.hazelcast.core.Member;

public class HazelcastMonitor {

    @Handler
    public void listInstances() {

        StringBuilder builder = new StringBuilder();

        String arr = "\n---------------------------------------------->\n";

        // cluster time
        builder.append("\n\nCLUSTER STATE AT: ").append(Hazelcast.getCluster().getClusterTime());

        // members
        builder.append(arr).append("MEMBERS").append(arr);
        Iterator<Member> memIt = Hazelcast.getCluster().getMembers().iterator();
        while (memIt.hasNext()) {
            Member member = (Member) memIt.next();
            builder.append(member.getInetSocketAddress()).append("\n");
        }

        // instances
        builder.append(arr).append("INSTANCES").append(arr);
        Iterator<Instance> instanceIt = Hazelcast.getInstances().iterator();
        while (instanceIt.hasNext()) {
            Instance instance = (Instance) instanceIt.next();
            String name = (String) instance.getId();
            String[] n = ((String) instance.getId()).split(":");

            if(instance.getInstanceType().isMap()) {
                if(!name.startsWith("c:q:")) {
                	
                	int index;
                	for (index = 0 ; index < n.length; index++ );
                	
                	if (index>1){
                		IMap<Object, Object> map = Hazelcast.getMap(n[1]);
                        builder.append("id --> ").append(instance.getId()).append(" \t\t| type --> ").append(instance.getInstanceType().name());
                        builder.append(" \t\t| value count --> ").append(map.size()).append("\n");
                	}
                	
                	if (index==1){
                		IMap<Object, Object> map = Hazelcast.getMap(n[0]);
                        builder.append("id --> ").append(instance.getId()).append(" \t\t| type --> ").append(instance.getInstanceType().name());
                        builder.append(" \t\t| value count --> ").append(map.size()).append("\n");
                	}
                	
                }
            }

            if(instance.getInstanceType().isQueue()) {
                IQueue<Object> queue = Hazelcast.getQueue(n[1]);
                builder.append("id --> ").append(instance.getId()).append(" \t\t| type --> ").append(instance.getInstanceType().name());
                builder.append(" \t\t| value count --> ").append(queue.size()).append("\n");
            }
        }

        System.out.println(builder.toString());
    }

}
