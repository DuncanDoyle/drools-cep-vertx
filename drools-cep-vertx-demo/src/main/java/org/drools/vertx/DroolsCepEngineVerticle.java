package org.drools.vertx;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
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

		// This is the main verticle, so setting our own Config.
		DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(getConfig());

		// Create a future per start-up task we want to do asynchronously.
		// Use a CompositeFuture.all to complete the deployment.

		// Load the Drools engine and mark the future as complete once we're done.
		LOGGER.info("Drools CEP Engine Verticle started.");
		Future<Void> droolsKieSessionStartFuture = Future.future();
		ReleaseId releaseId = getReleaseId();

		vertx.executeBlocking(future -> {
			kieContainer = KIE_SERVICES.newKieContainer(releaseId);
			kieSession = kieContainer.newKieSession();
			
			future.complete();
		}, false, res -> {
			if (res.succeeded()) {
				droolsKieSessionStartFuture.complete();
			} else {
				droolsKieSessionStartFuture.fail(res.cause());
			}
		});

		// Deploy the other verticles.
		Future<Void> httpInputVerticleStartFuture = Future.future();
		vertx.deployVerticle("org.drools.vertx.DroolsCepHttpInputVerticle", deploymentOptions, res -> {
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

	private JsonObject getConfig() {

		return new JsonObject().put("kjar.groupId", "org.drools.vertx").put("kjar.artifactId", "drools-cep-vertx-demo-kjar")
				.put("kjar.version", "0.0.1-SNAPSHOT");

	}

	private ReleaseId getReleaseId() {
		String groupId = config().getString(KJAR_GROUPID_CONFIG_KEY);
		String artifactId = config().getString(KJAR_ARTIFACTID_CONFIG_KEY);
		String version = config().getString(KJAR_VERSION_CONFIG_KEY);
		return KIE_SERVICES.newReleaseId(groupId, artifactId, version);
	}

}
