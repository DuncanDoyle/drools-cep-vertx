package org.drools.vertx.demo.cep.model;

/**
 * Simple Event.
 * <p/>
 * This event is immutable.
 * 
 * JSON String
 * {
	"id" : "7d782f84-be1f-4844-a4e7-c436e3945590",
	"timestampMillis" : 1530893957073
   }
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 *
 */
public class SimpleEvent implements Event {

	private String id;
	private long timestampMillis;
	
	/*
	 * TODO: I really would like to have an immutable object, but that gives problesms with JSON deserialization within Vert.x:
	 *  io.vertx.core.json.DecodeException: Failed to decode: No suitable constructor found for type [simple type, class org.drools.vertx.demo.cep.model.SimpleEvent]: can not instantiate from JSON object (missing default constructor or creator, or perhaps need to add/enable type information?)
	 *  
	 *  So, we need to figure out how to write a "creator" for that JSON Decoder if we want to implement immutable events.
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

	@Override
	public void accept(ModelVisitor visitor) {
		// TODO Auto-generated method stub
	}
	
	

}
