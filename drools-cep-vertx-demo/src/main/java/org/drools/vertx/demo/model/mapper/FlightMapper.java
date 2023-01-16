package org.drools.vertx.demo.model.mapper;

import org.drools.vertx.demo.cep.model.Flight;
import org.drools.vertx.demo.cep.model.FlightInfoEvent;

public class FlightMapper {
	
	public static Flight toFlight(FlightInfoEvent event) {
		return new Flight(event.getFlightCode(), event.getFlightDate(), event.getCodeShares(),
				event.getScheduledDepartureTime(), event.getDepartureLocation(), event.getArrivalLocation(),
				event.getAircraftRegistrationNumber(), event.getBookings());
	}
	
}
