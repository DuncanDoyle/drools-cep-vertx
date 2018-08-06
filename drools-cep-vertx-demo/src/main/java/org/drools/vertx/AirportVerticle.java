package org.drools.vertx;

import java.util.HashMap;
import java.util.Map;

import org.drools.vertx.demo.cep.model.FlightInfoEvent;
import org.drools.vertx.eventbus.EventBusAddressResolver;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


/**
 * Main verticle responsible for managing the {@link FlightVerticles}.
 * <p/>
 * Consumer of {@link FlightInfoEvent}, and based on those events, creates or updates FlightVerticles.
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
	
	private Map<String, Verticle> flightVerticles = new HashMap<String, Verticle>();
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("Starting AirportVerticle verticle.");
		
		//FlightInfoEvent handler.
		vertx.eventBus().<FlightInfoEvent> consumer(Constants.FLIGHT_INFO_EVENT_BUS_ADDRESS, message -> {
			handleFlightInfoEvent(message.body());
		});
		
		LOGGER.info("Bootstrapping KieContainer");
		Future<Void> droolsKieSessionStartFuture = Future.future();
		ReleaseId releaseId = getReleaseId();
			
		vertx.executeBlocking(future -> {
			LOGGER.info("Loading KIE-Container from KJAR with ReleaseId: " + releaseId.toString());
			//this.kieContainer = KIE_SERVICES.newKieContainer(releaseId);
			this.kieContainer = KIE_SERVICES.getKieClasspathContainer();
			
			if (kieContainer == null) {
				future.fail("Unable to load KieContainer.");
				
			}
			// Register channels;
			//kieSession.registerChannel(Constants.DROOLS_CHANNEL_NAME, new DroolsVertxChannel(vertx.eventBus()));

			// Set globals
			//kieSession.setGlobal("commandFactory", new CommandFactory());
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
	
	private void handleFlightInfoEvent(FlightInfoEvent flightInfo) {
		LOGGER.info("Handling FlightInfo with Flight-Code: " + flightInfo.getFlightCode());
		//Check that we have an address for this flight, if not, we create new Verticle
		String flightAddress = EventBusAddressResolver.resolveAddress(flightInfo);
		final Verticle verticle = flightVerticles.get(flightAddress);
		if (verticle == null ) {
			//handle deployment asynchronus ....
			if (kieContainer == null) {
				LOGGER.error("HUHH!!! KieContainer is null.");
			}
			Verticle flightVerticle = new FlightVerticle(flightInfo, kieContainer); 
			vertx.deployVerticle(flightVerticle);
			flightVerticles.put(flightAddress, flightVerticle);
		} else {
			//flight info update. FlightVerticle already exists, so must be an update event.
			//Creating a retry-handler that will retry sending the event 5 times before it drops it ...
			//TODO: There is probably a more generic approach we can use for retries ...
			Handler<AsyncResult<Message<Object>>> retryHandler = new Handler<AsyncResult<Message<Object>>>() {

				private static final int MAX_RETRY_COUNT = 5;
				private int retryCount = 0;
					
				@Override
				public void handle(AsyncResult<Message<Object>> event) {
					if (event.failed()) {
						LOGGER.warn("Failed delivering message. Cause: " + event.cause());
						if (retryCount < MAX_RETRY_COUNT) {
							LOGGER.warn("Retrying ...");
							retryCount++;
							vertx.eventBus().send(flightAddress, flightInfo, this);
						} else {
							LOGGER.error("Max retry count reached .. giving up . Dropping event!!!");
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
