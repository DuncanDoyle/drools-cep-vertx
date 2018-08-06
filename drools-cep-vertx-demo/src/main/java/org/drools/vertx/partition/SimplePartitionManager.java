package org.drools.vertx.partition;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.drools.vertx.demo.cep.model.Event;

public class SimplePartitionManager implements PartitionManager {

	
	//TODO: We can use shared data between PartitionManagers in multiple Verticles....
	// Next, we need to configure the number of instances per Session in the verticle.
	private Map<Long, String> addresses;
	
	
	
	@Override
	public String getPartitionAddress(Event event) {
		
		
		return null;
	}

	@Override
	public Collection<String> getAllPartitionAddresses() {
		return Collections.unmodifiableCollection(addresses.values());
	}
	
	

}
