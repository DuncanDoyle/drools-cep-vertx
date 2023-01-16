package org.drools.vertx;


import static io.vertx.core.logging.LoggerFactory.*;

import org.drools.vertx.codec.FlightInfoCodec;
import org.drools.vertx.demo.cep.command.Command;
import org.drools.vertx.demo.cep.command.CommandMessageCodec;
import org.drools.vertx.demo.cep.model.FlightInfoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
//import io.vertx.core.logging.Logger;
//import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;

/**
 * Startup verticle. Bootstraps all other verticles.
 * 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class MainVerticle extends AbstractVerticle {
	
	static {
		System.setProperty (LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName ());
	    LoggerFactory.getLogger (LoggerFactory.class); // Required for Logback to work in Vertx
	}
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("Starting Main Verticle.");

		// First register our MessageCodecs on the EventBus.
		registerCodecs();

		// This is the main verticle, so setting our own Config.
		DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(setConfig());

		// Create a future per start-up task we want to do asynchronously.
		// Use a CompositeFuture.all to complete the deployment.

		/*
		 * ################################################################################################################################
		 * # Load the PartitionHanlder and determine the number of partitions
		 * ################################################################################################################################
		 */
		//PartitionManager partitionManager = new SimplePartitionManager();

		/*
		 * ################################################################################################################################
		 * # Load the Drools CEP Engine verticle.
		 * ################################################################################################################################
		 */

		// Load the Drools engine and mark the future as complete once we're done.
		/*
		LOGGER.info("Drools CEP Engine Verticle started.");
		Future<Void> droolsKieSessionStartFuture = Future.future();
		ReleaseId releaseId = getReleaseId();
			
		vertx.executeBlocking(future -> {
			LOGGER.info("Loading KIE-Container from KJAR with ReleaseId: " + releaseId.toString());
			kieContainer = KIE_SERVICES.newKieContainer(releaseId);
			kieSession = kieContainer.newKieSession("ksession-rules");

			// Register channels;
			kieSession.registerChannel(Constants.DROOLS_CHANNEL_NAME, new DroolsVertxChannel(vertx.eventBus()));

			// Set globals
			kieSession.setGlobal("commandFactory", new CommandFactory());

			future.complete();
		}, false, res -> {
			if (res.succeeded()) {
				droolsKieSessionStartFuture.complete();
			} else {
				droolsKieSessionStartFuture.fail(res.cause());
			}
		});
		*/
				
		/*
		 * ################################################################################################################################
		 * # Deploy the other verticles
		 * ################################################################################################################################
		 */

		// Airport Verticle
		Future<Void> airportVerticleStartFuture = Future.future();
		vertx.deployVerticle("org.drools.vertx.AirportVerticle", deploymentOptions, res -> {
			if(res.succeeded()) {
				airportVerticleStartFuture.complete();
			} else {
				startFuture.fail(res.cause());
				airportVerticleStartFuture.fail(res.cause());
			}
		});
		
		// Drools CEP Verticle
		Future<Void> cepChannelHandlerVerticleStartFuture = Future.future();
		vertx.deployVerticle("org.drools.vertx.DroolsCepChannelHandlerVerticle", deploymentOptions, res -> {
			if (res.succeeded()) {
				cepChannelHandlerVerticleStartFuture.complete();
			} else {
				cepChannelHandlerVerticleStartFuture.fail(res.cause());
			}
		});

		// Kafka Verticle
		/*
		Future<Void> kafkaEventStoreVerticleStartFuture = Future.future();
		vertx.deployVerticle("org.drools.vertx.KafkaEventStoreVerticle", deploymentOptions, res -> {
			if (res.succeeded()) {
				kafkaEventStoreVerticleStartFuture.complete();
			} else {
				kafkaEventStoreVerticleStartFuture.fail(res.cause());
			}
		});
		*/
		
		// Mongo Verticle
		/*
		Future<Void> mongoEventStoreVerticleStartFuture = Future.future();
		vertx.deployVerticle("org.drools.vertx.MongoEventStoreVerticle", deploymentOptions, res -> {
			if (res.succeeded()) {
				mongoEventStoreVerticleStartFuture.complete();
			} else {
				mongoEventStoreVerticleStartFuture.fail(res.cause());
			}
		});
		*/
		
		/*
		 * ################################################################################################################################
		 * # CompositeFuture that completes this startup when all the other Future's complete.
		 * ################################################################################################################################
		 */
		// Join the futures to mark startFuture as complete.
		//CompositeFuture.all(airportVerticleStartFuture, cepChannelHandlerVerticleStartFuture,
		//		kafkaEventStoreVerticleStartFuture, mongoEventStoreVerticleStartFuture).setHandler(ar -> {
		CompositeFuture.all(airportVerticleStartFuture, cepChannelHandlerVerticleStartFuture).setHandler(ar -> {
					if (ar.succeeded()) {
						//Only deploy the http endpoints if all other verticles have succesfully deployed.
						
						//// HTTP Verticle. Sets up a HTTP server that exposes the RESTful interface of our Flight CEP platform.
						vertx.deployVerticle("org.drools.vertx.HttpVerticle", deploymentOptions, res -> {
							if (res.succeeded()) {
								startFuture.complete();
							} else {
								startFuture.fail(res.cause());
							}
						});
					} else {
						LOGGER.info("Error starting Drools CEP session.!");
						startFuture.fail(ar.cause());
					}
				});
	}
	
	private JsonObject setConfig() {
		LOGGER.info("Setting Config!");
		return context.config().put(Constants.KJAR_GROUPID_CONFIG_KEY, "org.drools.demo").put(Constants.KJAR_ARTIFACTID_CONFIG_KEY, "drools-cep-vertx-demo-kjar")
				.put(Constants.KJAR_VERSION_CONFIG_KEY, "0.0.1-SNAPSHOT");
	}
	
	private void registerCodecs() {
		vertx.eventBus().registerDefaultCodec(Command.class, new CommandMessageCodec());
		vertx.eventBus().registerDefaultCodec(FlightInfoEvent.class, new FlightInfoCodec());
	}
	

}
