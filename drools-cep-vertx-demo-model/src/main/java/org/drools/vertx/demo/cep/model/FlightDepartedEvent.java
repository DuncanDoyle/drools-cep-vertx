package org.drools.vertx.demo.cep.model;

public class FlightDepartedEvent extends SimpleEvent {
	
	private FlightInfoEvent flightInfo;
	
	public FlightDepartedEvent(FlightInfoEvent flightInfo) {
		this.flightInfo = flightInfo;
	}

	public FlightInfoEvent getFlightInfo() {
		return flightInfo;
	}

	
	
}
