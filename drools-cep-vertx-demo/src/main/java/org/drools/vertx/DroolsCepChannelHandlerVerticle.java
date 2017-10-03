package org.drools.vertx;

import org.drools.vertx.demo.cep.command.Command;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Implements a simple Drools Channel that handles RHS actions. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class DroolsCepChannelHandlerVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(DroolsCepChannelHandlerVerticle.class);
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("Drools CEP ChannelHandler Verticle started.");
		
		vertx.eventBus().consumer(Constants.DROOLS_CHANNEL_EVENT_BUS_ADDRESS, message -> {
			handleCommand(message);
		});
		
		startFuture.complete();
	}
	
	private void handleCommand(Message message) {
		LOGGER.info("Executing command!");
		Command command = (Command) message.body();
		command.execute();
	}
	
}
