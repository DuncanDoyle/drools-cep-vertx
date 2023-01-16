package org.drools.vertx;

import java.util.concurrent.TimeUnit;

import org.drools.core.time.SessionPseudoClock;
import org.drools.vertx.channel.DroolsVertxChannel;
import org.drools.vertx.demo.cep.command.CommandFactory;
import org.drools.vertx.demo.cep.model.AbstractFlightEvent;
import org.drools.vertx.demo.cep.model.Flight;
import org.drools.vertx.demo.cep.model.FlightInfoEvent;
import org.drools.vertx.demo.model.mapper.FlightMapper;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;


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

	private Flight flight;
	
	private FlightInfoEvent flightInfoEvent;
	
	private final KieContainer kieContainer;
	
	private final String flightId;
	
	private KieSession kieSession;

	/**
	 * Allows us to create a verticle per flight.
	 * 
	 * @param flight
	 * @param kieContainer
	 * @param eventBusAddress
	 */
	public FlightVerticle(FlightInfoEvent flightInfoEvent, final KieContainer kieContainer) {
		this.flight = FlightMapper.toFlight(flightInfoEvent);
		this.flightInfoEvent = flightInfoEvent;
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
			LOGGER.info("Registering Drools channels.");
			kieSession.registerChannel(Constants.DROOLS_CHANNEL_NAME, new DroolsVertxChannel(vertx.eventBus()));

			// Set the CommandFactory as a global.
			kieSession.setGlobal("commandFactory", new CommandFactory());
			kieSession.setGlobal("flightInfo", this.flightInfoEvent);
			
			//Insert the FlightEvent of this Verticle
			insertEventAndFireRules(flightInfoEvent);

			future.complete();
		}, false, res -> {
			if (res.succeeded()) {
				LOGGER.info("Flight Verticle started succesfully for flight: " + flightId);
				startFuture.complete();
			} else {
				LOGGER.error("Flight Verticle NOT started for flight: " + flightId);
				startFuture.fail(res.cause());
			}
		});
		
		//------------- Register consumers on the EventBus -------------
		//register handlers for all event-types that we're interested in.
		//Note that the addresses we listen on need the same as the addresses resolved by EventBusAddressResolver.
		//TODO Make the creation of the address a concern of a class that's also used by EventBusAddressResolver.
		LOGGER.info("Register event consumers for flight: " + flight.getFlightCode());
		vertx.eventBus().<FlightInfoEvent> consumer(Constants.FLIGHT_INFO_EVENT_BUS_ADDRESS_PREFIX + flightId, message -> {
			handleFlightInfoEvent(message.body());
		});
		
		//All other flight events
		vertx.eventBus().<AbstractFlightEvent> consumer(Constants.ABSTRACT_FLIGHT_EVENT_EVENT_BUS_ADDRESS_PREFIX + flightId, message -> {
			handleAbstractFlightEvent(message.body());
		});
		LOGGER.info("Event consumers registered for flight: " + flight.getFlightCode());
	}
	
	public Flight getFlight() {
		//TODO: We should return an immutable object.
		return flight;
	}
	
	
	
	private void handleFlightInfoEvent(FlightInfoEvent event) {
		LOGGER.info("Updating flight info.");
		this.flightInfoEvent = event;
		//Also update our internal Flight state.
		this.flight = FlightMapper.toFlight(event);
		insertEventAndFireRules(event);
	}
	
	private void handleAbstractFlightEvent(AbstractFlightEvent event) {
		LOGGER.info("Sending AbstractFlighEvent to Drools session. Event is of type: " + event.getClass().getCanonicalName());		
		insertEventAndFireRules(event);
	}
	
	/**
	 * Inserts events, advances the pseudo-clock (if required) and fires the rules.
	 * 
	 * @param event the event to be inserted.
	 */
	private void insertEventAndFireRules(AbstractFlightEvent event) {
		LOGGER.info("Inserting event into CEP session for Flight: " + flightId);
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
