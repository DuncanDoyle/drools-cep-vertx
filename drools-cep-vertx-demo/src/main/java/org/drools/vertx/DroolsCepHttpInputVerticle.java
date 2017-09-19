package org.drools.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DroolsCepHttpInputVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(DroolsCepHttpInputVerticle.class);

	
	@Override
	public void start(Future<Void> startFuture) throws Exception {

		LOGGER.info("Starting Drools CEP HTTP Input verticle.");
		
		// Start the HTTP server.
		LOGGER.info("Starting HTTP server");
		//Note: This seems to be causing blocking threads while booting up.
		//See: https://github.com/vert-x3/issues/issues/244 , seems to be a problem with macOS.
		//vertx.createHttpServer().requestHandler(new DroolsHttpRequestHandler()).listen(8080);
		Future<Void> startHttpServerFuture = Future.future();
		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(new DroolsHttpRequestHandler());
		httpServer.listen(8080, res -> {
			if (res.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(res.cause());
			}
		});
		
		

	}
	
	
	

}
