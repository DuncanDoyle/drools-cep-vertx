package org.drools.vertx;

import java.util.HashMap;
import java.util.Map;

import org.drools.vertx.demo.cep.model.Flight;
import org.drools.vertx.demo.cep.model.FlightInfoEvent;
import org.drools.vertx.demo.model.mapper.FlightMapper;
import org.drools.vertx.eventbus.EventBusAddressResolver;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Main verticle responsible for managing the {@link FlightVerticles}.
 * <p/>
 * Consumer of {@link Flight}, and based on those events, creates or updates FlightVerticles.
 * <p/>
 * This verticle is also responsible for the Drools KieContainer.
 * 
 * This verticle has the knowledge of the mapping between events of a certain flight and the address to which to send those events.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class AirportVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(AirportVerticle.class);

	private static final KieServices KIE_SERVICES = KieServices.Factory.get();

	private KieContainer kieContainer;

	private Map<String, FlightVerticle> flightVerticleRegistry = new HashMap<String, FlightVerticle>();

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("Starting AirportVerticle verticle.");

		// ------------- Register consumers on the EventBus -------------
		// FlightInfoEvent handler.
		vertx.eventBus().<FlightInfoEvent> consumer(Constants.FLIGHT_INFO_EVENT_BUS_ADDRESS, message -> {
			handleFlightInfoEvent(message);
		});
		
		vertx.eventBus().<JsonObject> consumer(Constants.GET_FLIGHTS_EVENT_BUS_ADDRESS, message -> {
			handleGetFlights(message);
		});
		

		// ------------- Bootstrap KIE Container -------------
		LOGGER.info("Bootstrapping KieContainer");
		Future<Void> droolsKieSessionStartFuture = Future.future();
		ReleaseId releaseId = getReleaseId();

		vertx.executeBlocking(future -> {
			LOGGER.info("Loading KIE-Container from KJAR with ReleaseId: " + releaseId.toString());
			// this.kieContainer = KIE_SERVICES.newKieContainer(releaseId);
			this.kieContainer = KIE_SERVICES.getKieClasspathContainer();

			if (kieContainer == null) {
				future.fail("Unable to load KieContainer.");

			}

			LOGGER.info("Bootstrapped KieContainer!");
			future.complete();
		}, false, res -> {
			if (res.succeeded()) {
				startFuture.complete();
				LOGGER.info("Verticle started!!! KieContainer is: " + this.kieContainer);
			} else {
				startFuture.fail(res.cause());
			}
		});

	}

	@Override
	public void stop() throws Exception {
		LOGGER.info("Stopping Airport Verticle");
	}
	
	private void handleGetFlights(Message<JsonObject> message) {
		//TODO: Implement filtering. Currently we simply return all flights.
		JsonArray flights = new JsonArray();
		
		for (Map.Entry<String, FlightVerticle> nextFlight: flightVerticleRegistry.entrySet()) {
			Flight flight = nextFlight.getValue().getFlight();
			flights.add(JsonObject.mapFrom(flight));
		}
		
		message.reply(flights);
	}

	private void handleFlightInfoEvent(Message<FlightInfoEvent> message) {
		FlightInfoEvent flightInfo = message.body();
		
		
		LOGGER.info("Handling FlightInfo with Flight-Code: " + flightInfo.getFlightCode());
		// Check that we have an address for this flight, if not, we create new Verticle
		String flightAddress = EventBusAddressResolver.resolveAddress(flightInfo);
		final Verticle verticle = flightVerticleRegistry.get(flightAddress);
		if (verticle == null) {

			if (kieContainer == null) {
				String errorMessage = "HUHH!!! KieContainer is null.";
				LOGGER.error(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			// handle verticle deployment asynchronously ....
			FlightVerticle flightVerticle = new FlightVerticle(flightInfo, kieContainer);
			vertx.deployVerticle(flightVerticle, new Handler<AsyncResult<String>>() {

				@Override
				public void handle(AsyncResult<String> event) {
					if (event.succeeded()) {
						// Register the flightverticle in our internal registry 9
						LOGGER.info("Registering flight verticle for flight: " + flightInfo.getFlightCode());
						flightVerticleRegistry.put(flightAddress, flightVerticle);
						LOGGER.info("Sending FlightVerticle reply.");
						message.reply("{ flightVerticle : " + flightAddress + "}");
					} else {
						message.reply("{ errorMessage: error }" + event.cause());
					}
				}
			});

		} else {
			// flight info update. FlightVerticle already exists, so must be an update event.
			// Creating a retry-handler that will retry sending the event 5 times before it drops it ...
			// TODO: There is probably a more generic approach we can use for retries ...
			Handler<AsyncResult<Message<Object>>> retryHandler = new Handler<AsyncResult<Message<Object>>>() {

				private static final int MAX_RETRY_COUNT = 5;
				private int retryCount = 0;

				@Override
				public void handle(AsyncResult<Message<Object>> event) {
					if (event.succeeded()) {
						message.reply("{ flightVerticle : " + flightAddress + "}");
					} else {
						LOGGER.warn("Failed delivering message. Cause: " + event.cause());
						if (retryCount < MAX_RETRY_COUNT) {
							LOGGER.warn("Retrying ...");
							retryCount++;
							vertx.eventBus().send(flightAddress, flightInfo, this);
						} else {
							LOGGER.error("Max retry count reached .. giving up . Dropping event!!!");
							message.fail(500, "Error delivering flight update message.");
						}
					}
				}
			};
			vertx.eventBus().send(flightAddress, flightInfo, retryHandler);
		}
	}

	private ReleaseId getReleaseId() {
		String groupId = config().getString(Constants.KJAR_GROUPID_CONFIG_KEY);
		String artifactId = config().getString(Constants.KJAR_ARTIFACTID_CONFIG_KEY);
		String version = config().getString(Constants.KJAR_VERSION_CONFIG_KEY);
		return KIE_SERVICES.newReleaseId(groupId, artifactId, version);
	}

}
