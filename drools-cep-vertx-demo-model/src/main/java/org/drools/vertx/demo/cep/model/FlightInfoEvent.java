package org.drools.vertx.demo.cep.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

public class FlightInfoEvent extends AbstractFlightEvent {

	
	private List<String> codeShares;

	@JsonSerialize(using = LocalTimeSerializer.class)
	@JsonDeserialize(using = LocalTimeDeserializer.class)
	private LocalTime scheduledDepartureTime;

	private String departureLocation;

	private String arrivalLocation;

	private String aircraftRegistrationNumber;

	private List bookings;

	// private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	@JsonCreator
	public FlightInfoEvent(@JsonProperty("id") final String id, @JsonProperty("timestampMillis") final long timestampMillis,
			@JsonProperty("flightCode") final String flightCode, @JsonProperty("flightDate") final LocalDate flightDate) {
		super(id, timestampMillis, flightCode, flightDate);
	}

	/**
	 * Creates a unique id for this flight based on the DateTime (in millis) and the flightcode.
	 * 
	 * @return unique id of this flight.
	 */
	public String buildFlightId() {
		return getFlightDate().format(DATE_TIME_FORMATTER) + "_" + getFlightCode();
	}

	public List<String> getCodeShares() {
		return codeShares;
	}

	public void setCodeShares(List<String> codeShares) {
		this.codeShares = codeShares;
	}

	public LocalTime getScheduledDepartureTime() {
		return scheduledDepartureTime;
	}

	public void setScheduledDepartureTime(LocalTime scheduledDepartureTime) {
		this.scheduledDepartureTime = scheduledDepartureTime;
	}

	public String getDepartureLocation() {
		return departureLocation;
	}

	public void setDepartureLocation(String departureLocation) {
		this.departureLocation = departureLocation;
	}

	public String getArrivalLocation() {
		return arrivalLocation;
	}

	public void setArrivalLocation(String arrivalLocation) {
		this.arrivalLocation = arrivalLocation;
	}

	public String getAircraftRegistrationNumber() {
		return aircraftRegistrationNumber;
	}

	public void setAircraftRegistrationNumber(String aircraftRegistrationNumber) {
		this.aircraftRegistrationNumber = aircraftRegistrationNumber;
	}

	public List getBookings() {
		return bookings;
	}

	public void setBookings(List bookings) {
		this.bookings = bookings;
	}

	@Override
	public void accept(ModelVisitor visitor) {
		visitor.visit(this);
	}
	
}
