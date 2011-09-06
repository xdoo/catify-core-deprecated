package com.catify.core.process.processors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;

/**
 * 
 * removes all entries from the header that are not
 * serializable. we need this for the transport over
 * the wire.
 * 
 * @author claus
 *
 */
public class SerializableHeadersProcessor extends BaseProcessor {

	@Override
	public void process(Exchange ex) throws Exception {
		
		Map<String, Object> headers = ex.getIn().getHeaders();
		List<String> garbage = new ArrayList<String>();
		
		// get all header entries that are not serializable
		Iterator<String> it1 = headers.keySet().iterator();
		while (it1.hasNext()) {
			String key = (String) it1.next();
			if(!(headers.get(key) instanceof Serializable)){
				garbage.add(key);
			}
		}
		
		// remove all header entries that are not serializable
		Iterator<String> it2 = garbage.iterator();
		while (it2.hasNext()) {
			headers.remove((String) it2.next());
		}
		
		super.copyBodyAndHeaders(ex);
	}

}
