package org.drools.vertx.demo.cep.model;

public interface ModelVisitor {

	void visit(FlightInfoEvent event);
		
	void visit(CrewArrivedEvent event);
	
	void visit(LuggageScanEvent event);
	
}
