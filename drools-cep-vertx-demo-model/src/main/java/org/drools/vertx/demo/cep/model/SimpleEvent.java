package org.drools.vertx.demo.cep.model;

/**
 * Simple Event.
 * <p/>
 * This event is immutable.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 *
 */
public class SimpleEvent implements Event {

	private String id;
	private long timestampMillis;
	
	/*
	 * TODO: I really would like to have an immutable object, but that gives problesm with JSON deserialization within Vert.x:
	 *  io.vertx.core.json.DecodeException: Failed to decode: No suitable constructor found for type [simple type, class org.drools.vertx.demo.cep.model.SimpleEvent]: can not instantiate from JSON object (missing default constructor or creator, or perhaps need to add/enable type information?)
	 *  
	 *  So, we need to figure out how to write a "creator" for that JSON Decoder.
	 */
	public SimpleEvent() {
	}
	
	public SimpleEvent(final String id, final long timestampMillis) {
		this.id = id;
		this.timestampMillis = timestampMillis;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setTimestampMillis(long timestampMillis) {
		this.timestampMillis = timestampMillis;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getTimestampMillis() {
		return timestampMillis;
	}

}
