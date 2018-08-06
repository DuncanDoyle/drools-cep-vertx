package org.drools.vertx;

import org.drools.vertx.demo.cep.model.AbstractFlightEvent;

public interface Constants {

	public static final String KJAR_GROUPID_CONFIG_KEY = "kjar.groupId";
	public static final String KJAR_ARTIFACTID_CONFIG_KEY = "kjar.artifactId";
	public static final String KJAR_VERSION_CONFIG_KEY = "kjar.version";
	
	
	//BUS ADDRESSES
	public static final String FLIGHT_INFO_EVENT_BUS_ADDRESS = "org.drools.vertx.demo.flightInfoEvent";
	
	public static final String FLIGHT_INFO_EVENT_BUS_ADDRESS_PREFIX = "org.drools.vertx.demo.flightInfoEvent.";
	
	public static final String CREW_ARRIVED_EVENT_EVENT_BUS_ADDRESS_PREFIX = "org.drools.vertx.demo.crewArrivedEvent.";
	
	public static final String LUGGAGE_SCAN_EVENT_EVENT_BUS_ADDRESS_PREFIX = "org.drools.vertx.demo.luggageScanEvent";
	
	public static final String SIMPLE_EVENT_EVENT_BUS_ADDRESS_PREFIX = "org.drools.vertx.demo.simpleEvent";
	
	public static final String DROOLS_CHANNEL_EVENT_BUS_ADDRESS = "org.drools.vertx.demo.channel";
	public static final String EVENT_STORE_EVENT_BUS_ADDRESS = "org.drools.vertx.demo.eventStore";
	
	public static final String GET_EVENTS_EVENT_BUS_ADDRESS = "org.drools.vertx.demo.getEvents";
	
	public static final String DROOLS_CHANNEL_NAME = "VertxChannel";

	//public static final String CREDIT_CARD_TRANSACTIONS_EVENT_BUS_ADDRESS = "org.drools.vertx.demo.ccTransactions";

	public static String getUniqueFlightId(AbstractFlightEvent flightInfo) {
		return flightInfo.getFlightDate().toString() + "_" + flightInfo.getFlightCode();
	}
}

