package org.drools.vertx;

import java.util.UUID;

import org.drools.vertx.demo.cep.model.Event;
import org.drools.vertx.demo.cep.model.SimpleEvent;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class DroolsHttpRouterBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(DroolsHttpRouterBuilder.class);
	
	private static final String EVENT_TYPE_PATH_PARAM = "eventType";
	
	private static final String EVENT_ID_PATH_PARAM = "eventId";
	
	private static final String SIMPLE_EVENT_EVENT_BUS_ADDRESS = "org.drools.vertx.demo.simpleEvents";
	
	private final Vertx vertx;
	
	private final EventBus eventBus;
	
	public DroolsHttpRouterBuilder(Vertx vertx) {
		this.vertx = vertx;
		eventBus = vertx.eventBus();
	}
	
	
	public Router buildRouter() {
		Router router = Router.router(vertx);

		router.route("/").handler(BodyHandler.create());

		// eventType is the path-param.
		router.post("/event/:" + EVENT_TYPE_PATH_PARAM).handler(this::handlePostEvent);
		router.get("/event/:" + EVENT_TYPE_PATH_PARAM + "/:" + EVENT_ID_PATH_PARAM).handler(this::handelGetEvent);
		
		return router;
	}
	
	
	private void handlePostEvent(RoutingContext context) {
		String eventType = context.request().getParam(EVENT_TYPE_PATH_PARAM);

		switch (eventType) {
		case ("simpleEvent"):
			LOGGER.info("Processing SimpleEvent!");
			//Generate some random stuff for now.
			Event event = new SimpleEvent(UUID.randomUUID().toString(), System.currentTimeMillis());
			String eventJson = Json.encodePrettily(event);
			
			LOGGER.info("Sending SimpleEvent '" + eventJson + "' to EventBus address '" + SIMPLE_EVENT_EVENT_BUS_ADDRESS + "'.");;
			eventBus.publish(SIMPLE_EVENT_EVENT_BUS_ADDRESS, eventJson);
			//And send a reply
			context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(event.getId()));
			
			break;
		default:
			throw new RuntimeException("Unsupported EventType.");
		}
	}
	
	private void handelGetEvent(RoutingContext context) {
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
