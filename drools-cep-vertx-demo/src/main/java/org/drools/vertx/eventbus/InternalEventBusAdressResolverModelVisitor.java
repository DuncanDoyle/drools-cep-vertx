package org.drools.vertx.eventbus;

import org.drools.vertx.Constants;
import org.drools.vertx.demo.cep.model.CrewArrivedEvent;
import org.drools.vertx.demo.cep.model.FlightInfoEvent;
import org.drools.vertx.demo.cep.model.LuggageScanEvent;
import org.drools.vertx.demo.cep.model.ModelVisitor;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

class InternalEventBusAdressResolverModelVisitor implements ModelVisitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventBusAddressResolver.class);

	private String address;
	
	public String getAddress() {
		return address;
	}
	
	@Override
	public void visit(FlightInfoEvent event) {
		address = Constants.FLIGHT_INFO_EVENT_BUS_ADDRESS_PREFIX + Constants.getUniqueFlightId(event);
	}

	@Override
	public void visit(CrewArrivedEvent event) {
		address = Constants.ABSTRACT_FLIGHT_EVENT_EVENT_BUS_ADDRESS_PREFIX + Constants.getUniqueFlightId(event);
	}

	@Override
	public void visit(LuggageScanEvent event) {
		address = Constants.ABSTRACT_FLIGHT_EVENT_EVENT_BUS_ADDRESS_PREFIX + Constants.getUniqueFlightId(event);
	}
	

}
