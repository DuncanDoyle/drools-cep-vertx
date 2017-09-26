package org.drools.vertx.demo.cep.model;

/**
 * Represents a scan of luggage at airport.
 * <p/>
 * This event type is immutable.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class LuggageScanEvent implements Event {

	private final long id;
	private final long timestampMillis;
	
	
	public LuggageScanEvent(final long id, final long timestampMillis) {
		this.id = id;
		this.timestampMillis = timestampMillis;
	}
	
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getTimestampMillis() {
		return timestampMillis;
	}

}
