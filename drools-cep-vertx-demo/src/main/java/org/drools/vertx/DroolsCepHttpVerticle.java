package org.drools.vertx;

import java.util.UUID;

import org.drools.vertx.demo.cep.model.Event;
import org.drools.vertx.demo.cep.model.SimpleEvent;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class DroolsCepHttpVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(DroolsCepHttpVerticle.class);
	
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

		httpServer.requestHandler(new DroolsHttpRouterBuilder(vertx).buildRouter()::accept);
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
