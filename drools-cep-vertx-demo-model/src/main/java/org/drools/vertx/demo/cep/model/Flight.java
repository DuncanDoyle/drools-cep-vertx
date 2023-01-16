package org.drools.vertx.demo.cep.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

/**
 * JSON String { "id": "1234", "timestampMillis": 1533479595851, "flightCode": "KL-1001", "flightDate": [ 2018, 8, 5 ], "codeShares": [],
 * "scheduledDepartureTime": [ 16, 33, 15, 899000000 ], "departureLocation": "AMS", "arrivalLocation": "LHR", "aircraftRegistrationNumber":
 * "PH-PBA", "bookings": [] }
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 *
 */

public class Flight {

	private final String flightCode;

	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private final LocalDate flightDate;

	private final List<String> codeShares;

	@JsonSerialize(using = LocalTimeSerializer.class)
	@JsonDeserialize(using = LocalTimeDeserializer.class)
	private final LocalTime scheduledDepartureTime;

	private final String departureLocation;

	private final String arrivalLocation;

	private final String aircraftRegistrationNumber;

	private final List bookings;

	// private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	@JsonCreator
	public Flight(@JsonProperty("flightCode") final String flightCode, @JsonProperty("flightDate") final LocalDate flightDate,
			List<String> codeShares, LocalTime scheduledDepartureTime, String departureLocation, String arrivalLocation,
			String aircraftRegistrationNumber, List bookings) {
		this.flightCode = flightCode;
		this.flightDate = flightDate;
		this.codeShares = codeShares;
		this.scheduledDepartureTime = scheduledDepartureTime;
		this.departureLocation = departureLocation;
		this.arrivalLocation = arrivalLocation;
		this.aircraftRegistrationNumber = aircraftRegistrationNumber;
		this.bookings = bookings;
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

	public LocalTime getScheduledDepartureTime() {
		return scheduledDepartureTime;
	}

	public String getDepartureLocation() {
		return departureLocation;
	}

	public String getArrivalLocation() {
		return arrivalLocation;
	}

	public String getAircraftRegistrationNumber() {
		return aircraftRegistrationNumber;
	}

	public List getBookings() {
		return bookings;
	}

	public String getFlightCode() {
		return flightCode;
	}

	public LocalDate getFlightDate() {
		return flightDate;
	}

}
