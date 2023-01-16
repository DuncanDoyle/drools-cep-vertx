package org.drools.vertx.http;

import java.time.LocalDate;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.drools.vertx.Constants;
import org.drools.vertx.demo.cep.model.CrewArrivedEvent;
import org.drools.vertx.demo.cep.model.FlightInfoEvent;
import org.drools.vertx.demo.cep.model.LuggageScanEvent;
import org.drools.vertx.eventbus.EventBusAddressResolver;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpRouterBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRouterBuilder.class);
	
	private static final String EVENT_TYPE_PATH_PARAM = "eventType";
	
	private static final String EVENT_ID_PATH_PARAM = "eventId";
	
	private final Vertx vertx;
	
	private final EventBus eventBus;
	
	public HttpRouterBuilder(Vertx vertx) {
		this.vertx = vertx;
		eventBus = vertx.eventBus();
	}
	
	
	public Router buildRouter() {
		Router router = Router.router(vertx);
		//Create the BodyHandler first!!!
		router.route().handler(BodyHandler.create());
		
		//Flights
		router.get("/flights").handler(this::handleGetFlights);
		
		
		//FlightInfoEvent
		router.post("/event/flightInfo").handler(this::handleFlightInfo);
		
		//LuggageScanEvent
		router.post("/event/luggageScanEvent").handler(this::handleLuggageScanEvent);
		router.post("/event/crewArrivedEvent").handler(this::handleCrewArrivedEvent);
		
		return router;
	}
	
	
	/*
	 * Expected request.
	 * 
	 * TODO: enhance requests that we can provide.
	 * 
	 * {
	 * 	 airline: 'KL',
	 *   departureBefore: ''
	 *   departureAfter: ''
	 *   arrivalBefore: ''
	 *   arrivalAfter: ''
	 * }
	 * 
	 */
	private void handleGetFlights(RoutingContext context) {
		
		JsonObject getFlightsRequest = new JsonObject();
		
		//Map request params.
		String[] params = {"airline", "departBefore", "departAfter", "arriveBefore", "arriveAfter"};
		
		for (String nextParam: params) {
			String value = context.request().getParam(nextParam);
			if (value != null && !value.isEmpty()) { 
				getFlightsRequest.put(nextParam , context.request().getParam(nextParam));
			}
		}
		
		eventBus.send(Constants.GET_FLIGHTS_EVENT_BUS_ADDRESS, getFlightsRequest, res -> {
			if (res.succeeded()) {
				JsonArray response = (JsonArray) res.result().body();
				LOGGER.info("Response: " + response);
				context.response().setStatusCode(HttpStatus.SC_OK);
				context.response().setChunked(true);
				context.response().write(response.toString());
				context.response().end();
			} else {
				context.response().setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				context.response().end();
			}
		});
	}
	
	
	/**
	 * Unmarshall the flight info and send it to the EventBus.
	 * 
	 * @param context
	 */
	private void handleFlightInfo(RoutingContext context) {
		/*
		 * TODO: We can also simply send the JSON through the EventBus and have the consumer deal with unmarshalling.
		 * That's probably a lot faster as well ...
		 */
		//Flight Events are always send to the generic FlightInfo EventBus. The consumer on that bus (AirportVerticle) is responsible for routing it to the correct flight verticle.
		FlightInfoEvent receivedFlightInfo = Json.decodeValue(context.getBody(), FlightInfoEvent.class);
		eventBus.<String>send(Constants.FLIGHT_INFO_EVENT_BUS_ADDRESS, receivedFlightInfo, res -> {
			if (res.succeeded()) {
				String response = res.result().body();
				LOGGER.info("Response: " + response);
				context.response().setStatusCode(HttpStatus.SC_ACCEPTED);
				//Seems we need to set Chunked if we want to write a response.
				context.response().setChunked(true);
				context.response().write(response);
				context.response().end();
			} else {
				context.response().setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				context.response().end();
			}
		});
		LOGGER.info("Message sent to EventBus!!!!");
		
	}
	
	private void handleLuggageScanEvent(RoutingContext context) {
		LuggageScanEvent luggageScanEvent = new LuggageScanEvent(UUID.randomUUID().toString(), System.currentTimeMillis(), "KL1001", LocalDate.of(2018, 7, 6));
		String luggageScanEventJson = Json.encodePrettily(luggageScanEvent);
		String address = EventBusAddressResolver.resolveAddress(luggageScanEvent);
		LOGGER.info("Sending LuggageScanEvent '" + luggageScanEventJson + "' to EventBus address '" + address + "'.");;
	}
	
	private void handleCrewArrivedEvent(RoutingContext context) {
		CrewArrivedEvent crewArrivedEvent = new CrewArrivedEvent(UUID.randomUUID().toString(), System.currentTimeMillis(), "KL1001", LocalDate.of(2018, 7, 6));
		String crewArrivedEventJson = Json.encodePrettily(crewArrivedEvent);
		String address = EventBusAddressResolver.resolveAddress(crewArrivedEvent);
		LOGGER.info("Sending crewArrivedEvent '" + crewArrivedEventJson + "' to EventBus address '" + address + "'.");;
	}
	
	private void handleGetEvent(RoutingContext context) {
		String eventType = context.request().getParam(EVENT_TYPE_PATH_PARAM);
		String eventId = context.request().getParam(EVENT_ID_PATH_PARAM);
		
		switch (eventType) {
		case ("simpleEvent"):
			//Request Response on the Event Bus to the Kafka consumer verticle.
			LOGGER.info("Retrieving Event with id: " + eventId);
			eventBus.<String>send(Constants.GET_EVENTS_EVENT_BUS_ADDRESS, eventId, res -> {
				if (res.succeeded()) {
					context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8").end(res.result().body());
				} else {
					context.response().setStatusCode(404).putHeader("content-type", "application/json; charset=utf-8").end();
				}
			});
			break;
		default:
			throw new RuntimeException("Unsupported EventType.");
		}
		
		
	}

}
