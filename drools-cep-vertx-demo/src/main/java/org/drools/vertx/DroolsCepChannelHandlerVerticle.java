package org.drools.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Implemens a simple Drools Channel that handles RHS actions. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class DroolsCepChannelHandlerVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(DroolsCepChannelHandlerVerticle.class);
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("Drools CEP ChannelHandler Verticle started.");
		startFuture.complete();
	}

	
	
	
}
