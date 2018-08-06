package org.drools.vertx.partition;

import java.util.Collection;

import org.drools.vertx.demo.cep.model.Event;

/**
 * Partitions in this system are homogeneous in the sense that every partition can get the same type of events and every partition runs the
 * same CEP engine. We don't support different CEP engines in this single Vert.x app, where we route to the correct CEP engine based on content.
 * <p/>
 *  
 * 
 * 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public interface PartitionManager {
	
	
	String getPartitionAddress(Event event);
	
	Collection<String> getAllPartitionAddresses();
	
	
	
	

}
