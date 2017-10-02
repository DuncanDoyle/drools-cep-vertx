package org.drools.vertx.demo.cep.command;

import java.io.Serializable;
import java.util.function.Consumer;

import org.junit.Test;

public class CommandFactoryTest {

	
	@Test
	public void testBuildCommandWithSerializableLambda() {
		CommandFactory commandFactory = new CommandFactory();
		
		Command command = commandFactory.buildCommand((Serializable & Command)() -> {
			System.out.println("Just a test.!");
		});
		
		command.execute();
		
		
		
		
	}
}