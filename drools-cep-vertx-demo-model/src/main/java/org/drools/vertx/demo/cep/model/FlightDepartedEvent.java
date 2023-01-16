package org.drools.vertx.demo.cep.model;

public class FlightDepartedEvent extends SimpleEvent {
	
	private Flight flightInfo;
	
	public FlightDepartedEvent(Flight flightInfo) {
		this.flightInfo = flightInfo;
	}

	public Flight getFlightInfo() {
		return flightInfo;
	}

	
	
}
