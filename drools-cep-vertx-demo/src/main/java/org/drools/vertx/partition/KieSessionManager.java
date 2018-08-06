package org.drools.vertx.partition;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.KieSession;

public class KieSessionManager {

	private Map<String, KieSession> sessionsPerFlight = new HashMap<String, KieSession>();
	
	
	public KieSession getSession(String flightCode, LocalDate flightDate) {
		
		
		
		return null;
	}
	
	
	
	private String getUniqueFlightId(String flightCode, LocalDate flightDate) {
		return flightDate.toString() + "_" + flightCode;
	}
	
	
	
	
	
}
