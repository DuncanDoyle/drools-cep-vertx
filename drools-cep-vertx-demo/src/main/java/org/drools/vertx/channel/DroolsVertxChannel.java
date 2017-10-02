package org.drools.vertx.channel;

import org.drools.vertx.Constants;
import org.drools.vertx.demo.cep.command.Command;
import org.drools.vertx.demo.cep.command.CommandMessageCodec;
import org.kie.api.runtime.Channel;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;

public class DroolsVertxChannel implements Channel {

	private final EventBus eb;
	
	private MessageCodec codec = new CommandMessageCodec();
	
	public DroolsVertxChannel(EventBus eb) {
		this.eb = eb;
	}

	@Override
	public void send(Object object) {
		DeliveryOptions options = new DeliveryOptions().setCodecName("command");	
		eb.send(Constants.DROOLS_CHANNEL_EVENT_BUS_ADDRESS, (Command) object, options);
	}
	
}
