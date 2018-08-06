package org.drools.vertx.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.drools.vertx.demo.cep.model.FlightInfoEvent;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Vert.x codec that converts a FlightInfo object into a buffer to be send over the Vert.x EventBus.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class FlightInfoCodec implements MessageCodec<FlightInfoEvent, FlightInfoEvent> {
	
	@Override
	public void encodeToWire(Buffer buffer, FlightInfoEvent flightInfo) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(flightInfo);
		} catch (IOException ioe) {
			throw new RuntimeException("Error while writing FlightInfo.", ioe);
		}
		
	    byte[] flightInfoBytes = baos.toByteArray();
	    buffer.appendInt(flightInfoBytes.length);
	    buffer.appendBytes(flightInfoBytes);
	}

	@Override
	public FlightInfoEvent decodeFromWire(int pos, Buffer buffer) {
		int length = buffer.getInt(pos);
		//Length is defined in the first 4 bytes, so move the position.
	    pos += 4;
	    byte[] bytes = buffer.getBytes(pos, pos + length);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		FlightInfoEvent flightInfo = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(bais);
			flightInfo = (FlightInfoEvent) ois.readObject();
		} catch (IOException ioe) {
			throw new RuntimeException("Error while reading Command.", ioe);
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("Error reading Command.", cnfe);
		}
		return flightInfo;
	}

	@Override
	public FlightInfoEvent transform(FlightInfoEvent flightInfo) {
		//TODO: Since we're basically passing the same reference when the EventBus is local, we might want to make a copy (i.e. serialize and de-serialize).
		return flightInfo;
	}

	@Override
	public String name() {
		return "flightInfo";
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

	
}
