package org.drools.vertx;

import java.util.concurrent.TimeUnit;

import org.drools.core.time.SessionPseudoClock;
import org.drools.vertx.demo.cep.model.AbstractFlightEvent;
import org.drools.vertx.demo.cep.model.CrewArrivedEvent;
import org.drools.vertx.demo.cep.model.FlightInfoEvent;
import org.drools.vertx.demo.cep.model.LuggageScanEvent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Verticle representing a flight. This verticle maintains a CEP session, and each flight has its own verticle deployed.
 * <p/>
 * This allows for management of the CEP session per flight (partitioning), and because each flight has its own verticle, we do not have a
 * single verticle responsible for executing the rules, which could give scalability problems, as each verticle instance runs on a single event-loop. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class FlightVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlightVerticle.class);

	private FlightInfoEvent flightInfo;

	private final KieContainer kieContainer;
	
	private final String flightId;
	
	private KieSession kieSession;

	/**
	 * Allows us to create a verticle per flight.
	 * 
	 * @param flightInfo
	 * @param kieContainer
	 * @param eventBusAddress
	 */
	public FlightVerticle(final FlightInfoEvent flightInfoEvent, final KieContainer kieContainer) {
		this.flightInfo = flightInfo;
		this.flightId = Constants.getUniqueFlightId(flightInfoEvent);
		LOGGER.info("Setting KieContainer to: " + kieContainer);
		this.kieContainer = kieContainer;
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		// We need to use a future because creating the KIE-Session can be regarded as a blocking operation.
		
		vertx.executeBlocking(future -> {
			LOGGER.info("Creating KieSession.");
			if (kieContainer == null) {
				LOGGER.error("KieContainer is null!!!!");
			}
			kieSession = kieContainer.newKieSession();
			
			// Register channels;
			//kieSession.registerChannel(Constants.DROOLS_CHANNEL_NAME, new DroolsVertxChannel(vertx.eventBus()));

			// Set globals
			//kieSession.setGlobal("commandFactory", new CommandFactory());

			future.complete();
		}, false, res -> {
			if (res.succeeded()) {
				LOGGER.info("Flight Verticle started succesfully for flight: " + flightId);
				startFuture.complete();
			} else {
				startFuture.fail(res.cause());
			}
		});
		
		//register handlers for all event-types that we're interested in.
		//Note that the addresses we listen on need the same as the addresses resolved by EventBusAddressResolver.
		//TODO Make the creation of the address a concern of a class that's also used by EventBusAddressResolver.
		vertx.eventBus().<FlightInfoEvent> consumer(Constants.FLIGHT_INFO_EVENT_BUS_ADDRESS_PREFIX + flightId, message -> {
			handleFlightInfoEvent(message.body());
		});
		
		//TODO: We might want to consider combining all these channels into a single on if handling of events is generic.
		
		//Luggage
		vertx.eventBus().<LuggageScanEvent> consumer(Constants.LUGGAGE_SCAN_EVENT_EVENT_BUS_ADDRESS_PREFIX + flightId, message -> {
			handleAbstractFlightEvent(message.body());
		});
		
		//Crew
		vertx.eventBus().<CrewArrivedEvent> consumer(Constants.CREW_ARRIVED_EVENT_EVENT_BUS_ADDRESS_PREFIX + flightId, message -> {
			handleAbstractFlightEvent(message.body());
		});
		
		
	}
	
	
	private void handleFlightInfoEvent(FlightInfoEvent event) {
		LOGGER.info("Updating flight info.");
		this.flightInfo = event;
		insertEventAndFireRules(event);
	}
	
	private void handleAbstractFlightEvent(AbstractFlightEvent event) {
		LOGGER.info("Sending AbstractFlighEvent to Drools session.");
		
	}
	
	private void insertEventAndFireRules(AbstractFlightEvent event) {
		LOGGER.debug("Inserting event into CEP session for Flight: " + flightId);
		SessionPseudoClock clock = kieSession.getSessionClock();
		long advanceDeltaTime = event.getTimestampMillis() - clock.getCurrentTime();
		
		if (advanceDeltaTime > 0) {
			clock.advanceTime(advanceDeltaTime, TimeUnit.MILLISECONDS);
		}
		//Asynchronously insert the event and fire the rules.
		LOGGER.debug("Firing rules asynchronously.");
		Future<Void> fireRuleFuture = Future.future();
		vertx.executeBlocking(future -> {
			kieSession.insert(event);
			kieSession.fireAllRules();
			future.complete();
		},false, res -> {
			if (res.succeeded()) {
				fireRuleFuture.complete();
			} else {
				fireRuleFuture.fail(res.cause());
			}
		});
		
	}
	

	@Override
	public void stop() throws Exception {
		super.stop();
		LOGGER.info("Stopping Flight verticle for Flight-ID " + flightId + ". Disposing Drools CEP KieSession.");
		kieSession.dispose();
	}

}
