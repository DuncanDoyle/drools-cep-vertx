package org.drools.vertx.partition;

import org.drools.vertx.demo.cep.model.Event;

@FunctionalInterface
public interface PartitionHandler {
	
	/**
	 * Method that applies the ParitionHandler function to an event.
	 * <p>
	 * This allows to do something like: <code>PartitionHanlder.getAddress(e -> {
	 * 		return "MY_ADDRESS";  // or any other address based on the event-data.
	 * }, event);</code> 
	 */
	static String getAddress(PartitionHandler ph, Event event) {
		return ph.getPartitionEventBusAddress(event);
	}
 	
	//The function.	
	String getPartitionEventBusAddress(Event event);
	
}
