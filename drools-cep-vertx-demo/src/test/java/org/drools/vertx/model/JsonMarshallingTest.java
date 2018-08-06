package org.drools.vertx.model;

import java.time.LocalDate;
import java.time.LocalTime;

import org.drools.vertx.demo.cep.model.FlightInfoEvent;

import io.vertx.core.json.Json;

public class JsonMarshallingTest {

	
	
	public void testJsonMarshalling() {
		FlightInfoEvent bla = new FlightInfoEvent("1234", System.currentTimeMillis(), "KL-1001", LocalDate.now());
		bla.setScheduledDepartureTime(LocalTime.now());
		bla.setAircraftRegistrationNumber("PH-PBA");
		bla.setDepartureLocation("AMS");
		bla.setArrivalLocation("LHR");
		//bla.
		
		System.out.println(Json.encode(bla));
	}
	
}
