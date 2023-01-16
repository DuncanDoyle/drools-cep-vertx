package org.drools.vertx.demo.cep.rules;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;

public class KJarTest {

	@Test
	public void testNumberOfRules() {
		KieServices kieServices = KieServices.Factory.get();
		
		KieContainer kieContainer = kieServices.newKieClasspathContainer();
		
		//Count the number of rules.
		
		int numberOfRules = 0;
		 
		for (KiePackage nextKiePackage: kieContainer.getKieBase().getKiePackages()) {
			numberOfRules = numberOfRules + nextKiePackage.getRules().size();
		}
		
		assertEquals(2, numberOfRules);
	}
}

