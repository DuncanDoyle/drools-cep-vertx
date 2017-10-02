package org.drools.vertx.demo.cep.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class CommandMessageCodec implements MessageCodec<Command, Command> {
	
	@Override
	public void encodeToWire(Buffer buffer, Command command) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(command);
		} catch (IOException ioe) {
			throw new RuntimeException("Error while writing Command.", ioe);
		}
		
	    byte[] commandBytes = baos.toByteArray();
	    buffer.appendInt(commandBytes.length);
	    buffer.appendBytes(commandBytes);
	}

	@Override
	public Command decodeFromWire(int pos, Buffer buffer) {
		int length = buffer.getInt(pos);
	    pos += 4;
	    byte[] bytes = buffer.getBytes(pos, pos + length);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		Command command = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(bais);
			command = (Command) ois.readObject();
		} catch (IOException ioe) {
			throw new RuntimeException("Error while reading Command.", ioe);
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("Error reading Command.", cnfe);
		}
		return command;
	}

	@Override
	public Command transform(Command command) {
		//TODO: Since we're basically passing the same reference when the EventBus is local, we might want to make a copy (i.e. serialize and de-serialize).
		return command;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "command";
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

	
}
