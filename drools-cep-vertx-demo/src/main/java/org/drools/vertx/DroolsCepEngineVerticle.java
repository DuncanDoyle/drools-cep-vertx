package org.drools.vertx;

import org.drools.vertx.demo.cep.model.SimpleEvent;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;


/**
 * Runs the Drools CEP Sessions as a Vert.X verticle.
 * <p/>
 * This is the main entry-point of our application. Startup of this verticle handles
 * <ul>
 * <li>Registration of codecs on the EventBus</li>
 * <li>Creates the config</li>
 * </ul>
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class DroolsCepEngineVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(DroolsCepEngineVerticle.class);

	
	private static final KieServices KIE_SERVICES = KieServices.Factory.get();

	private static KieContainer kieContainer;

	private static KieSession kieSession;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("Starting Drools CEP Engine verticle.");		
	}

	/*
	 * TODO: Determine if this is non-blocking. This comes down to whether 'fireAllRules' processes fast enough. And that basically boils
	 * down to how the rules are written, e.g. do they perform long-running actions in the LHS or RHS ..
	 */
	private void handleSimpleEvent(Message<String> message) {
		LOGGER.info("Handling event.");
		// printRules(kieSession);
		SimpleEvent event = Json.decodeValue(message.body(), SimpleEvent.class);
		kieSession.insert(event);
		kieSession.fireAllRules();
	}
	
	private void handleFlightInfo(Message<String> message) {
		//FlightInfo flightInfo = Json.decodeValue(message.body(), FlightInfo.class);
		
	}
	
	private void printRules(KieSession ksession) {
		LOGGER.info("Printing rules!");
		ksession.getKieBase().getKiePackages().stream().flatMap(kp -> {
			LOGGER.info("Package: " + kp.getName());
			return kp.getRules().stream();
		}).forEach(rule -> {
			LOGGER.info("Rule: " + rule.getName());
		});
	}
	
	/*
	vertx.eventBus().<String> consumer(Constants.SIMPLE_EVENT_EVENT_BUS_ADDRESS, message -> {
		handleEvent(message);
	});
	*/
	
	
	
	

}
