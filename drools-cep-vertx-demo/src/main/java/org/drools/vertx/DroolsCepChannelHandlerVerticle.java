package org.drools.vertx;

import org.drools.vertx.demo.cep.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;


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
		
		vertx.eventBus().<Command>consumer(Constants.DROOLS_CHANNEL_EVENT_BUS_ADDRESS, message -> {
			handleCommand(message);
		});
		
		startFuture.complete();
	}
	
	private void handleCommand(Message<Command> message) {
		LOGGER.info("Executing command!");
		Command command = message.body();
		command.execute();
	}
	
}
