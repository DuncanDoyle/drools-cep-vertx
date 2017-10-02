package org.drools.vertx;

import org.drools.vertx.channel.DroolsVertxChannel;
import org.drools.vertx.demo.cep.command.Command;
import org.drools.vertx.demo.cep.command.CommandFactory;
import org.drools.vertx.demo.cep.command.CommandMessageCodec;
import org.drools.vertx.demo.cep.model.SimpleEvent;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Runs the Drools CEP Sessions as a Vert.X verticle.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class DroolsCepEngineVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(DroolsCepEngineVerticle.class);

	private static final String KJAR_GROUPID_CONFIG_KEY = "kjar.groupId";
	private static final String KJAR_ARTIFACTID_CONFIG_KEY = "kjar.artifactId";
	private static final String KJAR_VERSION_CONFIG_KEY = "kjar.version";

	private static final KieServices KIE_SERVICES = KieServices.Factory.get();

	private static KieContainer kieContainer;

	private static KieSession kieSession;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("Starting Drools CEP Engine verticle.");

		//First register our MessageCodecs on the EventBus.
		registerCodecs();
		
		// This is the main verticle, so setting our own Config.
		DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(setConfig());
		

		// Create a future per start-up task we want to do asynchronously.
		// Use a CompositeFuture.all to complete the deployment.

		// Load the Drools engine and mark the future as complete once we're done.
		LOGGER.info("Drools CEP Engine Verticle started.");
		Future<Void> droolsKieSessionStartFuture = Future.future();
		ReleaseId releaseId = getReleaseId();

		vertx.executeBlocking(future -> {
			LOGGER.info("Loading KIE-Container from KJAR with ReleaseId: " + releaseId.toString());
			kieContainer = KIE_SERVICES.newKieContainer(releaseId);
			kieSession = kieContainer.newKieSession("ksession-rules");

			// Register channels;
			kieSession.registerChannel(Constants.DROOLS_CHANNEL_NAME, new DroolsVertxChannel(vertx.eventBus()));
			
			//Set globals
			kieSession.setGlobal("commandFactory", new CommandFactory());
			
			future.complete();
		}, false, res -> {
			if (res.succeeded()) {
				droolsKieSessionStartFuture.complete();
			} else {
				droolsKieSessionStartFuture.fail(res.cause());
			}
		});

		// Register message handler on the eventbus.
		EventBus eb = vertx.eventBus();
		eb.<String> consumer(Constants.SIMPLE_EVENT_EVENT_BUS_ADDRESS, message -> {
			LOGGER.info("Receieved event.");
			handleEvent(message);
		});

		/*
		 * ################################################################################################################################
		 * # Deploy the other verticles
		 * ################################################################################################################################
		 */

		// Deploy the other verticles.
		Future<Void> httpInputVerticleStartFuture = Future.future();
		vertx.deployVerticle("org.drools.vertx.DroolsCepHttpVerticle", deploymentOptions, res -> {
			if (res.succeeded()) {
				httpInputVerticleStartFuture.complete();
			} else {
				startFuture.fail(res.cause());
				httpInputVerticleStartFuture.fail(res.cause());
			}
		});

		Future<Void> cepChannelHandlerVerticleStartFuture = Future.future();
		vertx.deployVerticle("org.drools.vertx.DroolsCepChannelHandlerVerticle", deploymentOptions, res -> {
			if (res.succeeded()) {
				cepChannelHandlerVerticleStartFuture.complete();
			} else {
				cepChannelHandlerVerticleStartFuture.fail(res.cause());
			}
		});

		/*
		 * ################################################################################################################################
		 * # CompositeFuture that completes this startup when all the other Future's complete.
		 * ################################################################################################################################
		 */
		// Join the futures to mark startFuture as complete.
		CompositeFuture.all(droolsKieSessionStartFuture, cepChannelHandlerVerticleStartFuture, httpInputVerticleStartFuture)
				.setHandler(ar -> {
					if (ar.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(ar.cause());
					}
				});

	}

	private JsonObject setConfig() {
		return context.config().put("kjar.groupId", "org.drools.demo").put("kjar.artifactId", "drools-cep-vertx-demo-kjar")
				.put("kjar.version", "0.0.1-SNAPSHOT");
	}

	private ReleaseId getReleaseId() {
		String groupId = config().getString(KJAR_GROUPID_CONFIG_KEY);
		String artifactId = config().getString(KJAR_ARTIFACTID_CONFIG_KEY);
		String version = config().getString(KJAR_VERSION_CONFIG_KEY);
		return KIE_SERVICES.newReleaseId(groupId, artifactId, version);
	}

	/*
	 * TODO: Determine if this is non-blocking. This comes down to whether 'fireAllRules' processes fast enough. And that basically boils
	 * down to how the rules are written, e.g. do they perform long-running actions in the LHS or RHS ..
	 */
	private void handleEvent(Message<String> message) {
		LOGGER.info("Handling event.");
		// printRules(kieSession);
		SimpleEvent event = Json.decodeValue(message.body(), SimpleEvent.class);
		kieSession.insert(event);
		kieSession.fireAllRules();
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
	
	private void registerCodecs() {
		CommandMessageCodec commandMessageCodec =  new CommandMessageCodec();
		vertx.eventBus().registerDefaultCodec(Command.class, commandMessageCodec);
		//vertx.eventBus().registerCodec(commandMessageCodec);
	}

}
