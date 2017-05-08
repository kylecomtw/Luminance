/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.test;

import static junit.framework.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Sean
 */
public class DummyTest {
        private boolean hasSetup = false;
        @Before
        public void setup() {
            hasSetup = true;
            System.out.println("Test message");
        }
        
	@Test
	public void myFirstTest() {
		// Calculator calculator = new Calculator();
		// assertEquals(2, calculator.add(1, 1), "1 + 1 should equal 2");
		// assertEquals("My 1st JUnit 5 test! 😎", testInfo.getDisplayName(), () -> "TestInfo is injected correctly");
                assertTrue(true);
	}
        
        @Test
        public void secondTest() {
            assertTrue(hasSetup);
        }

}
