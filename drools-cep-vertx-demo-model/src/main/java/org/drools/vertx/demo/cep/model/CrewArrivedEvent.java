package org.drools.vertx.demo.cep.model;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

public class CrewArrivedEvent extends AbstractFlightEvent {

		
	public CrewArrivedEvent(final String id, final long timestampMillis, final String flightCode, final LocalDate flightDate) {
		super(id, timestampMillis, flightCode, flightDate);
	}

	

}
