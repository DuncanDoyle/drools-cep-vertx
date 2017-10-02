package org.drools.vertx.demo.cep.model;

/**
 * Represents a scan of luggage at airport.
 * <p/>
 * This event type is immutable.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class LuggageScanEvent implements Event {

	private final String id;
	private final long timestampMillis;
	
	
	public LuggageScanEvent(final String id, final long timestampMillis) {
		this.id = id;
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
