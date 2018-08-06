package org.drools.vertx.eventbus;

import org.drools.vertx.demo.cep.model.Event;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class EventBusAddressResolver {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EventBusAddressResolver.class);
	
	public static String resolveAddress(Event event) {
		LOGGER.info("Resolving address for event of type:" + event.getClass().getCanonicalName());
		InternalEventBusAdressResolverModelVisitor visitor = new InternalEventBusAdressResolverModelVisitor();
		event.accept(visitor);
		return visitor.getAddress();
	}
	
}
