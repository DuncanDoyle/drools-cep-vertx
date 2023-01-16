package org.drools.vertx;

import org.drools.vertx.http.HttpRouterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;



public class HttpVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {

		LOGGER.info("Starting Drools CEP HTTP Input verticle.");
		
		// Start the HTTP server.
		LOGGER.info("Starting HTTP server");
		// Note: This seems to be causing blocking threads while booting up.
		// See: https://github.com/vert-x3/issues/issues/244 , seems to be a problem with macOS.
		// vertx.createHttpServer().requestHandler(new DroolsHttpRequestHandler()).listen(8080);
		Future<Void> startHttpServerFuture = Future.future();
		HttpServer httpServer = vertx.createHttpServer();

		httpServer.requestHandler(new HttpRouterBuilder(vertx).buildRouter()::accept);
		httpServer.listen(8080, res -> {
			if (res.succeeded()) {
				LOGGER.info("Started HTTP Server. Listening on port: " + httpServer.actualPort());
				startFuture.complete();
			} else {
				startFuture.fail(res.cause());
			}
		});
	}

	
}
