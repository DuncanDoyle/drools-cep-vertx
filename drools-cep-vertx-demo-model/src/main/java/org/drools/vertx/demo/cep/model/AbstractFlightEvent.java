package org.drools.vertx.demo.cep.model;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

public class AbstractFlightEvent extends SimpleEvent {

	private String flightCode;
	
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate flightDate;

	
	public AbstractFlightEvent(final String id, final long timestampMillis, final String flightCode, final LocalDate flightDate) {
		super(id, timestampMillis);
		this.flightCode = flightCode;
		this.flightDate = flightDate;
	}
	
	public String getFlightCode() {
		return flightCode;
	}

	public LocalDate getFlightDate() {
		return flightDate;
	}
	
}
