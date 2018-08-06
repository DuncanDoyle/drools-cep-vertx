package org.drools.vertx.demo.cep.model;

/**
 * Generic event type.
 * 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public interface Event {
	
	String getId();
	
	long getTimestampMillis();

	void accept(ModelVisitor visitor);
	
}
