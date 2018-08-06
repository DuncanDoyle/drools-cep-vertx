package org.drools.vertx.demo.cep.model;

import java.time.LocalDate;

/**
 * Represents a scan of luggage at airport.
 * <p/>
 * This event type is immutable.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class LuggageScanEvent extends AbstractFlightEvent {

	public LuggageScanEvent(final String id, final long timestampMillis, final String flightCode, final LocalDate flightDate) {
		super(id, timestampMillis, flightCode, flightDate);
	}
		
}
